package com.westwell.api.common.configs;

import com.westwell.api.DetectionServiceGrpc;
import com.westwell.api.FeatureServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class GrpcConfig {

    @Bean("grpcTaskExecutor")
    public Executor grpcTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程池大小
        executor.setCorePoolSize(10);
        //最大线程数
        executor.setMaxPoolSize(50);
        //队列容量
        executor.setQueueCapacity(1000);
        //活跃时间
        executor.setKeepAliveSeconds(60);
        //线程名字前缀
        executor.setThreadNamePrefix("grpc-taskExecutor-");
//        优雅关机
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(3);

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    @Bean("detect")
    public ManagedChannel managedChannel1(@Qualifier("grpcTaskExecutor") Executor executor){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("10.66.65.200", 50051)
                .usePlaintext(true)
                .executor(executor)
                .build();
        return channel;
    }

    @Bean("feature")
    public ManagedChannel managedChannel2(@Qualifier("grpcTaskExecutor") Executor executor){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("10.66.66.20", 50051)
                .usePlaintext(true)
                .executor(executor)
                .build();
        return channel;
    }


    @Bean
    public FeatureServiceGrpc.FeatureServiceBlockingStub featureServiceBlockingStub(@Qualifier("feature") ManagedChannel managedChannel){
        return FeatureServiceGrpc.newBlockingStub(managedChannel);
    }


    @Bean
    public DetectionServiceGrpc.DetectionServiceBlockingStub detectionServiceBlockingStub(@Qualifier("detect") ManagedChannel managedChannel){
        return DetectionServiceGrpc.newBlockingStub(managedChannel);
    }
}
