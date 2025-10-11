package vroong.laas.order.api.docs;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enum 값들을 REST Docs 문서에 자동으로 포함시키기 위한 헬퍼 클래스
 *
 * <p>Enum이 추가/변경되면 자동으로 문서에 반영됩니다.
 */
public class EnumDocs {

  /**
   * Enum 값들을 ", "로 구분하여 문자열로 반환
   *
   * @param enumClass Enum 클래스
   * @return "VALUE1, VALUE2, VALUE3" 형태의 문자열
   */
  public static String format(Class<? extends Enum<?>> enumClass) {
    return Stream.of(enumClass.getEnumConstants())
        .map(Enum::name)
        .collect(Collectors.joining(", "));
  }

  /**
   * 필수 필드용 제약조건 문자열 생성
   *
   * @param enumClass Enum 클래스
   * @return "필수, 가능한 값: VALUE1, VALUE2, VALUE3"
   */
  public static String formatRequired(Class<? extends Enum<?>> enumClass) {
    return "필수, 가능한 값: " + format(enumClass);
  }

  /**
   * 선택 필드용 제약조건 문자열 생성
   *
   * @param enumClass Enum 클래스
   * @return "선택, 가능한 값: VALUE1, VALUE2, VALUE3"
   */
  public static String formatOptional(Class<? extends Enum<?>> enumClass) {
    return "선택, 가능한 값: " + format(enumClass);
  }
}


