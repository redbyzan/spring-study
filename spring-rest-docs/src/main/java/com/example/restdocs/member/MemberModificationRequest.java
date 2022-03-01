package com.example.restdocs.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberModificationRequest {
    
    @Max(10)
    private int age;

    public static MemberModificationRequest of(int age){
        return MemberModificationRequest.builder()
                .age(age)
                .build();
    }


}
