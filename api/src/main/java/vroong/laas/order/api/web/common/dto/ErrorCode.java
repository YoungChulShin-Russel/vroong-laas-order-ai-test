package vroong.laas.order.api.web.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * API 에러 코드
 *
 * <p>모든 에러 코드는 이 enum으로 중앙 관리합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // 4xx: 클라이언트 에러
  /** 입력값 검증 실패 (Bean Validation) */
  VALIDATION_ERROR("입력값 검증에 실패했습니다"),

  /** 잘못된 입력 (Domain 검증 실패) */
  INVALID_INPUT("잘못된 입력입니다"),

  /** 인증 실패 */
  UNAUTHORIZED("인증에 실패했습니다"),

  /** 권한 없음 */
  FORBIDDEN("접근 권한이 없습니다"),

  /** 리소스를 찾을 수 없음 */
  NOT_FOUND("요청한 리소스를 찾을 수 없습니다"),

  // 5xx: 서버 에러
  /** 알 수 없는 서버 에러 */
  INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다"),

  /** 데이터베이스 에러 */
  DATABASE_ERROR("데이터베이스 오류가 발생했습니다"),

  /** 외부 API 에러 */
  EXTERNAL_API_ERROR("외부 API 호출에 실패했습니다");

  /** 에러 메시지 */
  private final String message;

  /**
   * ErrorCode의 name()을 반환 (예: "INVALID_INPUT")
   *
   * @return 에러 코드 문자열
   */
  public String getCode() {
    return this.name();
  }
}
