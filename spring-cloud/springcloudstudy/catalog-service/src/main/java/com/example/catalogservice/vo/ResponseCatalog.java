package com.example.catalogservice.vo;


// vo와 dto 차이점
// vo는 데이터 그 자체로 의미를 담고 있는 객체,
// dto와 동일한 개념인데 차이점은 read only 속성을 가졌다고 보면 된다.
// 계층간의 데이터 전달에는 dto를 사용하고
// 클라이언트쪽으로 반환에는 vo를 사용하는 것 같다.

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseCatalog {
    private String productId;
    private String productName;
    private Integer unitPrice;
    private Integer stock;
    private LocalDateTime createdAt;

}
