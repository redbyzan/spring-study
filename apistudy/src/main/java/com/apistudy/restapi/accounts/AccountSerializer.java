package com.apistudy.restapi.accounts;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

// createEvent에서 매니저 정보에 account를 넣어주면서
// docs에 manager 비밀번호 정보까지 다 들어가버린다
// 이럴때는 dto로 modelmapper로 부어서 사용해도 된다
//강의에서는 jsonserializer을 사용한다

//전에는 error를 시리얼라이즈를 @jsoncomponent를 붙여서 objectmapper에 등록되게 만들었따.
// 하지만 이번엔 이 애노테이션을 붙이면 안된다.
// 이 컴포넌트를 붙여버리면 json으로 account를 내릴때마다 아래 정의한 대로만 내려간다
// 따라서 이거는 event에서 manager라는 필드를 serialize 할때만 사용해야 한다.
// event 엔티티의 manaver 필드에 애노테이션을 붙여줘야 한다.

// xml에 있는 jsonserializer 사용해야한다
public class AccountSerializer extends JsonSerializer<Account> {

    @Override
    public void serialize(Account account, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id",account.getId());
        gen.writeEndObject();
    }
}
