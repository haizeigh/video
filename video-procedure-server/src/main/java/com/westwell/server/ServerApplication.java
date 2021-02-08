package com.westwell.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@ComponentScan(basePackages = {"com.learn.api", "com.learn.englishserver", "com.alibaba.dubbo"})
//@ImportResource({"classpath:provider.xml"})
//@ComponentScan(basePackages = {"com.handu.open.dubbo.monitor"})
//@Import({ DubboConfig.class})
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

}
