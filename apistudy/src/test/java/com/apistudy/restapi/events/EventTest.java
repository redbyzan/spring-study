package com.apistudy.restapi.events;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

// junit은 파라미터를 가질 수 없는데 dependency를 추가하고 아래 애노테이션 추가시 파라미터 추가 가능
// 이를 통해 중복된 테스트 코드 제거 가능
//@RunWith(JUnitParamsRunner.class)
class EventTest {

    @DisplayName("builder test")
    @Test
    void builder() throws Exception{
        Event event = Event.builder()
                .name("test")
                .description("test")
                .build();
        assertThat(event).isNotNull();
    }

    @DisplayName("setter, getter test")
    @Test
    void javaBean() throws Exception{
        // given
        String name = "Event";
        String description = "Spring";

        // when
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);

        //then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }


    @ParameterizedTest
    @CsvSource({
            "0, 0, true",
            "0, 100, false",
            "100, 0, false",
    })
    public void testFree(int basePrice, int maxPrice, boolean isFree) {
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();

        event.update();

        assertThat(event.isFree()).isEqualTo(isFree);
    }


    // 단위 테스트
    @Test
    void testfOffline() throws Exception{
        //given
        Event event = Event.builder()
                .location("강남역")
                .build();
        //when
        event.update();

        //then
        assertThat(event.isOffline()).isTrue();

        //given
        event = Event.builder()
                .build();
        //when
        event.update();

        //then
        assertThat(event.isOffline()).isFalse();
    }

}
