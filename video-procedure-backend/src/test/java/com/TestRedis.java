package com;

import com.westwell.backend.common.utils.RedisUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith( SpringRunner.class)
public class TestRedis {

    @Resource
    RedisUtils redisUtils;

    @Test
    public void test1(){
        redisUtils.set("a", "bxxxxxxxxxxxxxxxxxxxx");
        System.out.println(redisUtils.get("a"));
    }
}
