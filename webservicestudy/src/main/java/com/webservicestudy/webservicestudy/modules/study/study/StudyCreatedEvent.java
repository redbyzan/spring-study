package com.webservicestudy.webservicestudy.modules.study.study;

import com.webservicestudy.webservicestudy.modules.study.Study;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;

@Data
@RequiredArgsConstructor
public class StudyCreatedEvent {
    private final Study study;
}
