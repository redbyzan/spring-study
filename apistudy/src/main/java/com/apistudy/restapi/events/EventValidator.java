package com.apistudy.restapi.events;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;

// 정상적으로 값이 들어오지만 논리적으로 오류인 경우를 걸러줄 것
// 빈으로 등록하고 사용
@Component
public class EventValidator {

    public void validate(EventDto eventDto, Errors errors){
        // max가 0이면 무제한이잠 0이 아닌데 역전이 발생하면 논리적 오류
        if (eventDto.getBasePrice() > eventDto.getMaxPrice() && eventDto.getMaxPrice()!=0){
            // 에러에 에러 코드 넣기
            // 문제의필드, 에러코드, 메시지
            errors.rejectValue("basePrice","wrongValue","BasePrice is wrong.");
            errors.rejectValue("MaxPrice","wrongValue","MaxPrice is wrong.");
        }

        LocalDateTime endEventDateTime = eventDto.getEndEventDateTime();
        // 이벤트 끝나는 시점이 시작시점보다 전인 경우
        if(endEventDateTime.isBefore(eventDto.getBeginEventDateTime())||
                endEventDateTime.isBefore(eventDto.getCloseEnrollmentDateTime())||
                endEventDateTime.isBefore(eventDto.getBeginEnrollmentDateTime())){
            errors.rejectValue("endEventDateTime","wrongValue","endEventDateTime is Wrong.");
        }
    }
    // todo beginEventDateTime
    // todo closeEnrollmentDatetime
}
