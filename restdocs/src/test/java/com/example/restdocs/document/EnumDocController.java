package com.example.restdocs.document;

import com.example.restdocs.EnumType;
import com.example.restdocs.member.MemberStatus;
import com.example.restdocs.member.Sex;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test")
public class EnumDocController {


    @GetMapping("/docs")
    public ApiResponseDto<EnumDocs> findAll() {

        // 문서화 하고 싶은 -> EnumDocs 클래스에 담긴 모든 Enum 값 생성
        Map<String, String> memberStatus = getDocs(MemberStatus.values());
        Map<String, String> sex = getDocs(Sex.values());

        // 전부 담아서 반환 -> 테스트에서는 이걸 꺼내 해석하여 조각을 만들면 된다.
        return ApiResponseDto.of(EnumDocs.builder()
                .memberStatus(memberStatus)
                .Sex(sex)
                .build()
        );

    }

    private Map<String, String> getDocs(EnumType[] enumTypes) {
        return Arrays.stream(enumTypes)
                .collect(Collectors.toMap(EnumType::getId, EnumType::getText));
    }

}
