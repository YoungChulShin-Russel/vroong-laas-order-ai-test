package vroong.laas.order.api.web.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

@DisplayName("WebApiControllerAdvice 테스트")
class WebApiControllerAdviceTest {

  private WebApiControllerAdvice advice;

  @BeforeEach
  void setUp() {
    advice = new WebApiControllerAdvice();
  }

  @Test
  @DisplayName("IllegalArgumentException 처리 - 400 Bad Request 응답")
  void handleIllegalArgumentException() {
    // given
    IllegalArgumentException exception = new IllegalArgumentException("출발지는 필수입니다");

    // when
    ResponseEntity<ProblemDetail> response = advice.handleIllegalArgumentException(exception);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(400);
    assertThat(response.getBody().getDetail()).isEqualTo("출발지는 필수입니다");
    assertThat(response.getBody().getProperties().get("exception"))
        .isEqualTo("IllegalArgumentException");
    assertThat(response.getBody().getProperties().get("timestamp")).isNotNull();
  }

  @Test
  @DisplayName("Exception 처리 - 500 Internal Server Error 응답")
  void handleException() {
    // given
    Exception exception = new RuntimeException("예상치 못한 오류");

    // when
    ResponseEntity<ProblemDetail> response = advice.handleException(exception);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(500);
    assertThat(response.getBody().getDetail()).isEqualTo("서버 오류가 발생했습니다");
    assertThat(response.getBody().getProperties().get("exception")).isEqualTo("RuntimeException");
    assertThat(response.getBody().getProperties().get("timestamp")).isNotNull();
  }

  @Test
  @DisplayName("IllegalArgumentException - 빈 메시지도 처리된다")
  void handleIllegalArgumentException_withEmptyMessage() {
    // given
    IllegalArgumentException exception = new IllegalArgumentException("");

    // when
    ResponseEntity<ProblemDetail> response = advice.handleIllegalArgumentException(exception);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDetail()).isEqualTo("");
    assertThat(response.getBody().getProperties().get("exception"))
        .isEqualTo("IllegalArgumentException");
    assertThat(response.getBody().getProperties().get("timestamp")).isNotNull();
  }

  // MethodArgumentNotValidException 테스트는 통합 테스트에서 검증
  // Mock으로 테스트하기 어려운 Spring Framework의 내부 구조 때문
}
