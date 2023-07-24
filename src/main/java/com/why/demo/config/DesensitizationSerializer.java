package com.why.demo.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.why.demo.common.annotation.Desensitization;
import com.why.demo.domain.enums.DesensitizationEnum;

import java.io.IOException;
import java.util.Objects;

/**
 * 数据脱敏配置
 */
public final class DesensitizationSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private DesensitizationEnum dataMaskEnum;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(dataMaskEnum.getFunction().apply(value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Desensitization desensitizationAnnotation = property.getAnnotation(Desensitization.class);
        if (Objects.nonNull(desensitizationAnnotation)&&Objects.equals(String.class, property.getType().getRawClass())) {
            this.dataMaskEnum = desensitizationAnnotation.desensitizationType();
            return this;
        }
        return prov.findValueSerializer(property.getType(), property);
    }

}
