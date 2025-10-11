package vroong.laas.order.api.config;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

/**
 * REST Docs 테스트 설정
 *
 * <p>REST Docs를 사용한 API 문서화를 위한 설정을 제공합니다.
 *
 * <p>주요 기능:
 *
 * <ul>
 *   <li>요청/응답 Pretty Print
 *   <li>문서 스니펫 자동 생성
 * </ul>
 *
 * <p><strong>참고:</strong> @WebMvcTest는 ObjectMapper를 자동으로 제공하지 않으므로 명시적으로 정의합니다.
 */
@TestConfiguration
public class RestDocsConfiguration {

  /**
   * REST Docs 문서화 핸들러
   *
   * <p>각 테스트에서 .andDo(restDocs.document(...))로 사용
   *
   * @return RestDocumentationResultHandler
   */
  @Bean
  public RestDocumentationResultHandler restDocs() {
    return document(
        "{class-name}/{method-name}",
        preprocessRequest(prettyPrint()),
        preprocessResponse(prettyPrint()));
  }

  /**
   * ObjectMapper Bean (테스트용)
   *
   * <p>@WebMvcTest는 Jackson Auto Configuration을 포함하지 않으므로 명시적으로 정의합니다.
   *
   * <p>Java 8 Time API (Instant, LocalDateTime 등) 지원을 위한 JavaTimeModule을 등록합니다.
   */
  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }
}

