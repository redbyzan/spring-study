package com.webservicestudy.webservicestudy.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync // 이 애노테이션만 적고 나둬도 aync하게 동작 가능하다.
// async 에서 처리하는 에러도 이걸 처리할 수 있는 헨들러를 등록할 수 있다. implement AsyncConfigurer
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        log.info("processors count {}",processors);
        // 리스너가 튜브고 이벤트가 사람이라고 생각
        // 풀장에 튜브가 10개밖에 없음 사람들이 3명와서 튜브 타고 있음 = activethread(현재일하고 있는 스레드 개수)
        // 사람듶이 10명와서 꽉차고 11명이 오면 줄을 세운다 그게 setqueuecapacity
        // 근데 줄이 꽉차서 (큐가 꽉차서) 51번째 사람이 오면
        // 큐에서 맨 앞에서 기다리고 있는 사람에게 튜브하나 만들어서 준다
        // 언제까지? maxpoolsize가 찰때까지
        // maxpoolsize까지 꽉차면 더이상 executor가 더이상 task 처리 불가능
        executor.setCorePoolSize(processors);
        executor.setMaxPoolSize(processors*2);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60); // maxpoolsize로 인해 덤으로 더 돌아다니는 튜브는 60초 후에 수거해서 정리
        executor.setThreadNamePrefix("AsyncExecutor-"); // executor 이름주기 나중에 로깅에서 찾기 편한
        executor.initialize(); // 초기화후 반환
        return executor;
    }
}














