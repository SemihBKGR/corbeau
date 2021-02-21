package com.smh.PostBlogWebApp.configuration;


import com.smh.PostBlogWebApp.service.CounterService;
import com.smh.PostBlogWebApp.service.PostService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@EnableAsync
@Configuration
public class CounterSchedule {

    private final PostService postService;
    private final CounterService counterService;

    @Autowired
    public CounterSchedule(PostService postService,
                           CounterService counterService) {
        this.postService = postService;
        this.counterService = counterService;
    }

    @Scheduled(fixedDelay = 600_000)
    public void takeFromBufferToRedis(){
        counterService.getCounterBuffer().forEach((urlEndpoint, incrementValue) -> {
            counterService.saveIncrement(urlEndpoint, incrementValue);
            counterService.getCounterBuffer().put(urlEndpoint,0);
        });

    }

    @Scheduled(fixedDelay = 3_600_000)
    public void takeFromRedisToDatabase(){
        counterService.getCounterBuffer().keySet().forEach((urlEndpoint)->{
            int count= counterService.getIncrement(urlEndpoint);
            postService.updateViewCount(urlEndpoint,count);
        });
        postService.clearAllCaches();
    }




}
