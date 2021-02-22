package com.westwell.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {"com.westwell.server", "com.westwell.api"})
//@ImportResource({"classpath:provider.xml"})
//@ComponentScan(basePackages = {"com.handu.open.dubbo.monitor"})
//@Import({ DubboConfig.class})
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

}
