package vroong.laas.order.api.web.common.dto;

import vroong.laas.order.core.domain.order.EntranceInfo;

/**
 * 출입 정보 DTO
 *
 * <p>공통으로 사용되는 건물 출입 정보
 */
public record EntranceInfoDto(
    String password,       // 선택
    String guide,          // 선택
    String requestMessage  // 선택
) {

  /** EntranceInfoDto → EntranceInfo Domain 변환 */
  public EntranceInfo toDomain() {
    return new EntranceInfo(password, guide, requestMessage);
  }
}
