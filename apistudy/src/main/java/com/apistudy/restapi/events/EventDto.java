package com.apistudy.restapi.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {

    // string 값은 반드시 들어와야함 -> notempty
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;

    @NotNull // 시간 값들은 null이면 안된다
    private LocalDateTime beginEnrollmentDateTime;
    @NotNull
    private LocalDateTime closeEnrollmentDateTime;
    @NotNull
    private LocalDateTime beginEventDateTime;
    @NotNull
    private LocalDateTime endEventDateTime;

    private String location;
    @Min(0)
    private int basePrice;
    @Min(0)
    private int maxPrice;
    @Min(0)
    private int limitOfEnrollment;
}
