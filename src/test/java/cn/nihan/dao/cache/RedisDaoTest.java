package cn.nihan.dao.cache;

import cn.nihan.dao.SeckillDao;
import cn.nihan.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * @author nihan
 * @date 2018/5/19   20:51
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {
    @Autowired
    private  RedisDao redisDao;

    @Autowired
    private SeckillDao seckillDao;

    private long id = 1001;

    @Test
    public void testGetSeckill() throws Exception {
        Seckill seckill = redisDao.getSeckill(id);
        if(seckill == null) {
            seckill = seckillDao.queryById(id);
            if(null != seckill) {
                String result = redisDao.putSeckill(seckill);
                System.out.println(result);
                seckill = redisDao.getSeckill(id);
                System.out.println(seckill);
            }
        }
        System.out.println(seckill);
    }

    @Test
    public void putSeckill() throws Exception {
        Seckill seckill =  new Seckill();
        seckill.setCreateTime(new Date());
        seckill.setEndTime(new Date());
        seckill.setName("手机");
        seckill.setNumber(100);
        seckill.setSeckillId(123);
        String str = redisDao.putSeckill(seckill);
        System.out.println(str);
    }

}