package cn.nihan.dao;

import cn.nihan.entity.SuccessKilled;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {
    @Resource
    private SuccessKilledDao successKilledDao;

    @Test
    public void insertSuccessKilled() throws Exception {
        int  i = successKilledDao.insertSuccessKilled(1000,1522);
        Assert.assertEquals(1,i);
        System.out.println("i=" + i);
    }

    @Test
    public void queryByIdWithSeckill() throws Exception {
       SuccessKilled successKilled =  successKilledDao.queryByIdWithSeckill(1000,1522);
       System.out.println(successKilled.getSeckill());
    }

}