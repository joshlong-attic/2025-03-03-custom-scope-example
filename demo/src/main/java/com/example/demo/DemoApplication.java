package com.example.demo;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    static CustomScopeConfigurer threadCustomScopeConfigurer() {
        var configurer = new CustomScopeConfigurer();
        configurer.addScope(ThreadScope.SCOPE_NAME, new SimpleThreadScope());
        return configurer;
    }


    @Bean
    ApplicationRunner runner(MyRunnable myRunnable) {
        return _ -> {
            for (var i = 0; i < 10; i++)
                new Thread(myRunnable).start();
        };
    }
}


@Component
@ThreadScope
class Step {

    private final String uuid;

    Step() {
        this.uuid = java.util.UUID.randomUUID().toString();
    }

    String uuid() {
        return this.uuid;
    }
}

@Component
class MyRunnable implements Runnable {

    private final Step step;

    MyRunnable(Step step) {
        this.step = step;
    }

    @Override
    public void run() {
        System.out.println(this.step.uuid());
    }
}

// NB: this is key! it must be proxyMode = ScopedProxyMode.TARGET_CLASS!
@Scope(value = ThreadScope.SCOPE_NAME, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface ThreadScope {

    String SCOPE_NAME = "thread";
}
