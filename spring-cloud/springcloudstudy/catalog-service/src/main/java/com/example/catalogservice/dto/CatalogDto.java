package com.example.catalogservice.dto;

import lombok.Data;

import java.io.Serializable;

@Data
// serializable?
// 자바 직렬화란 자바 시스템 내부에서 사용되는 객체 또는
// 데이터를 외부의 자바 시스템에서도 사용할 수 있도록 바이트(byte)
// 형태로 데이터 변환하는 기술과 바이트로 변환된 데이터를
// 다시 객체로 변환하는 기술(역직렬화)을 아울러서 이야기합니다.
public class CatalogDto implements Serializable {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;

    private String orderId;
    private String userId;
}
