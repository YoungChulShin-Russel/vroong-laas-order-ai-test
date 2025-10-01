package vroong.laas.order.infrastructure.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtil {

  private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
      .addModule(new JavaTimeModule())
      .addModule(new ParameterNamesModule())
      .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .defaultPropertyInclusion(
          JsonInclude.Value.construct(
              JsonInclude.Include.NON_NULL,
              JsonInclude.Include.NON_NULL))
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();

  public static ObjectMapper objectMapper() {
    return OBJECT_MAPPER;
  }

  public static String toJson(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return OBJECT_MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("JSON 직렬화에 실패했습니다.", ex);
    }
  }

  public static <T> T fromJson(String json, Class<T> type) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      return OBJECT_MAPPER.readValue(json, type);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("JSON 역직렬화에 실패했습니다.", ex);
    }
  }

  public static <T> T fromJson(String json, TypeReference<T> type) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      return OBJECT_MAPPER.readValue(json, type);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("JSON 역직렬화에 실패했습니다.", ex);
    }
  }
}
