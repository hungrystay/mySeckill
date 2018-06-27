package cn.nihan.service.impl;

import cn.nihan.dao.SeckillDao;
import cn.nihan.dao.SuccessKilledDao;
import cn.nihan.dao.cache.RedisDao;
import cn.nihan.dto.Exposer;
import cn.nihan.dto.SeckillExecution;
import cn.nihan.entity.Seckill;
import cn.nihan.entity.SuccessKilled;
import cn.nihan.enums.SeckillStatEnum;
import cn.nihan.exception.RepeatKillException;
import cn.nihan.exception.SeckillCloseException;
import cn.nihan.exception.SeckillException;
import cn.nihan.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author nihan
 * @date 2018/5/15   21:03
 */
@Service
public class SeckillServiceImpl implements SeckillService {
    //日志对象
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    //加入一个混淆字符串，避免用户猜出我们的MD5值
    private final String salt = "henrynihan@gmail.com";

    //注入Service依赖
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        //先从redis中查找
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            // 访问数据库读取数据
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                // 放入redis
                redisDao.putSeckill(seckill);
            }
        }
        Date startTime = seckill.getStartTime();//秒杀开始时间
        Date endTime = seckill.getEndTime();//秒杀结束时间
        Date nowTime = new Date();//当前时间
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            //秒杀未开始，或秒杀已结束
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        //秒杀开启，返回商品的id，和MD5
        String MD5 = getMD5(seckillId);
        return new Exposer(true, MD5, seckillId);

    }

    //MD5的作用，目前不是太确定，根据web层的实际操作看情况确定
    private String getMD5(long seckillId) {
        String base = seckillId + "/" + salt;
        System.out.println("========= base = {}" + base);
        System.out.println("hahahahahahahhahahahahahahahahahaahhahhahahaa======teststestest");
        logger.info("base = {}", base);
        String MD5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return MD5;
    }

    /**
     * 使用注解控制事务方法的优点:
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控制
     */
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {

        //增加这一步的意义？
        //要想执行秒杀操作，必须拿到MD5值，
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            //秒杀数据被重写了
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑:减库存+增加购买明细
        Date nowTime = new Date();
        try {
            //否则更新了库存，秒杀成功,增加明细
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            //看是否该明细被重复插入，即用户是否重复秒杀
            if (insertCount <= 0) {
                throw new RepeatKillException("seckill repeated");
            } else {
                //减库存,热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    //没有更新库存记录，说明秒杀结束 rollback
                    throw new SeckillCloseException("seckill is closed");
                } else {
                    //秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息 commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //所以编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error :" + e.getMessage());
        }
    }

    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStatEnum.DATE_REWRITE);
        }
        Date nowTime = new Date();
        HashMap map = new HashMap<String, Object>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", nowTime);
        map.put("result", null);
        // 执行储存过程,result被复制
        try {
            seckillDao.killByProcedure(map);
            // 获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
            } else {
                return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
        }
    }
}

