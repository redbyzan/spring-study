package com.webservicestudy.webservicestudy.infra.config;

import com.webservicestudy.webservicestudy.modules.notification.NotificationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final NotificationInterceptor notificationInterceptor;

    // 인터셉터 추가
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        List<String> staticResourcesPath = Arrays.stream(StaticResourceLocation.values())
                // StaticResourceLocation 리스트들을 하나의 리스트로 합침
                .flatMap(StaticResourceLocation::getPatterns)
                .collect(Collectors.toList());
        staticResourcesPath.add("/node_modules/**");
        // 인터셉터 적용 제외 범위
        registry.addInterceptor(notificationInterceptor)
                .excludePathPatterns(staticResourcesPath);
    }
}
