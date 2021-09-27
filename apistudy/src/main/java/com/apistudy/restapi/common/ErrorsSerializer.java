package com.apistudy.restapi.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.validation.Errors;

import java.io.IOException;

// json으로 바꿔주기 위해서 objectMapper을 사용했었다. 그럼 이 만들 serializer을 objectmapper에 등록해야한다.
@JsonComponent // 이 애노테이션 하나면 등록이 끝난다.
// errors를 json으로 serialize 하기 위해서는 JsonSerializer를 상속받아 재정의하면 된다.
public class ErrorsSerializer extends JsonSerializer<Errors> {
    @Override
    public void serialize(Errors errors, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        // jsongenerator로 json 만든다고 보면 되고 errors를 키값으로 안에 배열 생성 startarray
        gen.writeFieldName("errors");
        gen.writeStartArray();
        // BindingError에는 FieldError와 GlobalError(ObjectError)가 있다.
        // 이전에 validatior에서 error을 담아줄 때 rejectvalue를 사용하면서 field를 적어줬다.
        // 이러면 fieldError에 속한다.
        // 만약 여러개의 값이 조합되어 에러가 발생한 경우 그냥 errors.reject("에러코드","메시지")로 주게 되는데
        // 이렇게 되면 globalerror에 들어가게 된다.
        errors.getFieldErrors().forEach( e->{
            try {
                // 각각의 에러마다 object로 만들어서 json 형식으로 채워넣는다.
                gen.writeStartObject(); // object json 만들고
                // 본격적으로 json object의 필드를 채운다
                gen.writeStringField("field",e.getField());
                gen.writeStringField("objectName",e.getObjectName());
                gen.writeStringField("code",e.getCode());
                gen.writeStringField("defaultMessage",e.getDefaultMessage());

                Object rejectedValue = e.getRejectedValue(); // 있을 수도 없을 수도 있음
                if (rejectedValue!=null){
                    gen.writeStringField("rejectedValue",rejectedValue.toString());
                }
                gen.writeEndObject();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        // global error json으로 만들기 -> gobal이니까 오류 필드가 없을 것임\
        // 따라서 위에 작성한 것에서 field를 제외
        errors.getGlobalErrors().forEach(e->{
            try {
                // 각각의 에러마다 object로 만들어서 json 형식으로 채워넣는다.
                gen.writeStartObject(); // object json 만들고
                // 본격적으로 json object의 필드를 채운다
                gen.writeStringField("objectName",e.getObjectName());
                gen.writeStringField("code",e.getCode());
                gen.writeStringField("defaultMessage",e.getDefaultMessage());
                gen.writeEndObject();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        gen.writeEndArray();


    }
}
