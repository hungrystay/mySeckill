package cn.nihan.dao;

import cn.nihan.entity.Seckill;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

/**
 * Created by nihan on 18/05/15.
 * 配置spring和junit整合，这样junit在启动时就会加载spring容器
 */
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {
    @Resource
    private SeckillDao seckillDao;

    @Test
    public void queryById() throws Exception {
        Seckill seckill = seckillDao.queryById(1000);
        System.out.println(seckill.getName());
    }

    @Test
    public void reduceNumber() throws Exception {
        int i = seckillDao.reduceNumber(1000,new Date());
        Assert.assertEquals(1,i);
    }

    @Test
    public void queryAll() throws Exception {
       List<Seckill> list = seckillDao.queryAll(0,3);
       System.out.println("list=======");
       System.out.println(list);
       Assert.assertEquals(3,list.size());
    }
}