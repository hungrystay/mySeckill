package cn.nihan.service;

import cn.nihan.dto.Exposer;
import cn.nihan.dto.SeckillExecution;
import cn.nihan.entity.Seckill;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"
        , "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> list = seckillService.getSeckillList();
        Assert.assertEquals(4, list.size());
    }

    @Test
    public void getById() throws Exception {
        Seckill seckill = seckillService.getById(1000);
        Assert.assertEquals(100, seckill.getNumber());
    }

    @Test
    public void testExportSeckillUrl() throws Exception {
        Exposer exposer = seckillService.exportSeckillUrl(1004);
        logger.info("exposer={}", exposer);
        System.out.println(exposer.getMd5());
    }

    @Test
    public void testExecuteSeckill() throws Exception {
        Exposer exposer = seckillService.exportSeckillUrl(1004);
        SeckillExecution seckillExecution = seckillService.executeSeckill(1004, 15207114731L, exposer.getMd5());
        System.out.println(seckillExecution.getStateInfo());
    }

    @Test
    public void executeSeckillProcedure() {
        Exposer exposer = seckillService.exportSeckillUrl(1004);
        SeckillExecution seckillExecution = seckillService.executeSeckillProcedure(1004, 15207114736L, exposer.getMd5());
        System.out.println(seckillExecution.getStateInfo());
    }

}