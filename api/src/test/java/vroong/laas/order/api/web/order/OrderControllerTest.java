package vroong.laas.order.api.web.order;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vroong.laas.order.api.config.RestDocsConfiguration;
import vroong.laas.order.api.web.common.response.WebApiControllerAdvice;
import vroong.laas.order.api.web.common.dto.AddressDto;
import vroong.laas.order.api.web.common.dto.ContactDto;
import vroong.laas.order.api.web.common.dto.EntranceInfoDto;
import vroong.laas.order.api.web.common.dto.LatLngDto;
import vroong.laas.order.api.web.order.dto.DeliveryPolicyDto;
import vroong.laas.order.api.web.order.dto.DestinationDto;
import vroong.laas.order.api.web.order.dto.OrderItemDto;
import vroong.laas.order.api.web.order.dto.OriginDto;
import vroong.laas.order.api.web.order.request.CreateOrderRequest;
import vroong.laas.order.core.application.order.OrderFacade;
import vroong.laas.order.core.domain.order.DeliveryPolicy;
import vroong.laas.order.core.domain.order.Destination;
import vroong.laas.order.core.domain.order.Order;
import vroong.laas.order.core.domain.order.OrderItem;
import vroong.laas.order.core.domain.order.OrderNumber;
import vroong.laas.order.core.domain.order.OrderStatus;
import vroong.laas.order.api.docs.EnumDocs;
import vroong.laas.order.core.domain.order.Origin;
import vroong.laas.order.core.domain.order.exception.OrderNotFoundException;
import vroong.laas.order.core.domain.order.EntranceInfo;
import vroong.laas.order.core.domain.shared.Address;
import vroong.laas.order.core.domain.shared.Contact;
import vroong.laas.order.core.domain.shared.LatLng;
import vroong.laas.order.core.domain.shared.Money;

/**
 * OrderController REST Docs 테스트
 *
 * <p>REST Docs를 사용한 API 문서 자동 생성
 *
 * <p>@WebMvcTest: Controller Layer Slice Test
 * - 빠른 실행 (Controller만 로드)
 * - ExceptionHandler 수동 Import
 * - REST Docs 문서 생성
 */
@WebMvcTest(
    controllers = OrderController.class,
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.REGEX,
        pattern = "vroong.laas.order.api.web.common.logging.*"
    )
)
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class, WebApiControllerAdvice.class})
class OrderControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private OrderFacade orderFacade;

  @Test
  @DisplayName("주문 생성 API - 성공")
  void createOrder_success() throws Exception {
    // given - Request 생성
    CreateOrderRequest request = createOrderRequest();

    // given - Mock Order 생성
    Order order = createMockOrder();
    given(orderFacade.createOrder(any())).willReturn(order);

    // when & then
    mockMvc
        .perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.orderNumber").value("ORD-20250112-000001"))
        .andExpect(jsonPath("$.status").value("CREATED"))
        // REST Docs 문서화
        .andDo(
            document(
                "order-create",
                requestFields(getCreateOrderRequestFields()),
                responseFields(getOrderResponseFields())));
  }

  @Test
  @DisplayName("주문 조회 API - 성공")
  void getOrder_success() throws Exception {
    // given
    Long orderId = 1L;
    Order order = createMockOrder();
    given(orderFacade.getOrderById(orderId)).willReturn(order);

    // when & then
    mockMvc
        .perform(get("/api/v1/orders/{orderId}", orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.orderNumber").value("ORD-20250112-000001"))
        .andExpect(jsonPath("$.status").value("CREATED"))
        // REST Docs 문서화
        .andDo(
            document(
                "order-get",
                pathParameters(
                    parameterWithName("orderId").description("주문 ID")
                ),
                responseFields(getOrderResponseFields())));
  }

  @Test
  @DisplayName("주문 조회 API - 존재하지 않는 주문 (404)")
  void getOrder_notFound() throws Exception {
    // given
    Long orderId = 999L;
    given(orderFacade.getOrderById(orderId))
        .willThrow(new OrderNotFoundException(orderId));

    // when & then
    mockMvc
        .perform(get("/api/v1/orders/{orderId}", orderId))
        .andExpect(status().isBadRequest())  // WebApiControllerAdvice에서 400으로 처리
        .andExpect(jsonPath("$.detail").exists())
        .andExpect(jsonPath("$.properties.errorCode").value("ORDER_NOT_FOUND"))
        // REST Docs 문서화
        .andDo(
            document(
                "order-get-not-found",
                pathParameters(
                    parameterWithName("orderId").description("주문 ID")
                ),
                responseFields(
                    fieldWithPath("type")
                        .type(JsonFieldType.STRING)
                        .description("문제 타입 URI (RFC 7807)")
                        .attributes(key("constraints").value("선택"))
                        .optional(),
                    fieldWithPath("title")
                        .type(JsonFieldType.STRING)
                        .description("HTTP 상태 코드 제목")
                        .attributes(key("constraints").value("필수")),
                    fieldWithPath("status")
                        .type(JsonFieldType.NUMBER)
                        .description("HTTP 상태 코드")
                        .attributes(key("constraints").value("필수, 400")),
                    fieldWithPath("detail")
                        .type(JsonFieldType.STRING)
                        .description("에러 상세 메시지")
                        .attributes(key("constraints").value("필수")),
                    fieldWithPath("instance")
                        .type(JsonFieldType.STRING)
                        .description("에러 발생 URI")
                        .attributes(key("constraints").value("선택"))
                        .optional(),
                    fieldWithPath("properties")
                        .type(JsonFieldType.OBJECT)
                        .description("추가 속성 (커스텀 에러 정보)")
                        .attributes(key("constraints").value("필수")),
                    fieldWithPath("properties.timestamp")
                        .type(JsonFieldType.STRING)
                        .description("에러 발생 시각")
                        .attributes(key("constraints").value("필수, ISO 8601 형식")),
                    fieldWithPath("properties.errorCode")
                        .type(JsonFieldType.STRING)
                        .description("에러 코드")
                        .attributes(key("constraints").value("필수, ORDER_NOT_FOUND")),
                    fieldWithPath("properties.exception")
                        .type(JsonFieldType.STRING)
                        .description("예외 클래스명")
                        .attributes(key("constraints").value("필수, OrderNotFoundException"))
                )));
  }

  @Test
  @DisplayName("주문 조회 API (주문번호) - 성공")
  void getOrderByOrderNumber_success() throws Exception {
    // given
    String orderNumber = "ORD-20250112-000001";
    Order order = createMockOrder();
    given(orderFacade.getOrderByNumber(orderNumber)).willReturn(order);

    // when & then
    mockMvc
        .perform(get("/api/v1/orders/number/{orderNumber}", orderNumber))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.orderNumber").value("ORD-20250112-000001"))
        .andExpect(jsonPath("$.status").value("CREATED"))
        // REST Docs 문서화
        .andDo(
            document(
                "order-get-by-order-number",
                pathParameters(
                    parameterWithName("orderNumber").description("주문 번호")
                ),
                responseFields(getOrderResponseFields())));
  }

  @Test
  @DisplayName("주문 조회 API (주문번호) - 존재하지 않는 주문 (404)")
  void getOrderByOrderNumber_notFound() throws Exception {
    // given
    String orderNumber = "ORD-99999999-999999";
    given(orderFacade.getOrderByNumber(orderNumber))
        .willThrow(new OrderNotFoundException(OrderNumber.of(orderNumber)));

    // when & then
    mockMvc
        .perform(get("/api/v1/orders/number/{orderNumber}", orderNumber))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").exists())
        .andExpect(jsonPath("$.properties.errorCode").value("ORDER_NOT_FOUND"))
        // REST Docs 문서화
        .andDo(
            document(
                "order-get-by-order-number-not-found",
                pathParameters(
                    parameterWithName("orderNumber").description("주문 번호")
                ),
                responseFields(
                    fieldWithPath("type")
                        .type(JsonFieldType.STRING)
                        .description("문제 타입 URI (RFC 7807)")
                        .attributes(key("constraints").value("선택"))
                        .optional(),
                    fieldWithPath("title")
                        .type(JsonFieldType.STRING)
                        .description("HTTP 상태 코드 제목")
                        .attributes(key("constraints").value("필수")),
                    fieldWithPath("status")
                        .type(JsonFieldType.NUMBER)
                        .description("HTTP 상태 코드")
                        .attributes(key("constraints").value("필수, 400")),
                    fieldWithPath("detail")
                        .type(JsonFieldType.STRING)
                        .description("에러 상세 메시지")
                        .attributes(key("constraints").value("필수")),
                    fieldWithPath("instance")
                        .type(JsonFieldType.STRING)
                        .description("에러 발생 URI")
                        .attributes(key("constraints").value("선택"))
                        .optional(),
                    fieldWithPath("properties")
                        .type(JsonFieldType.OBJECT)
                        .description("추가 속성 (커스텀 에러 정보)")
                        .attributes(key("constraints").value("필수")),
                    fieldWithPath("properties.timestamp")
                        .type(JsonFieldType.STRING)
                        .description("에러 발생 시각")
                        .attributes(key("constraints").value("필수, ISO 8601 형식")),
                    fieldWithPath("properties.errorCode")
                        .type(JsonFieldType.STRING)
                        .description("에러 코드")
                        .attributes(key("constraints").value("필수, ORDER_NOT_FOUND")),
                    fieldWithPath("properties.exception")
                        .type(JsonFieldType.STRING)
                        .description("예외 클래스명")
                        .attributes(key("constraints").value("필수, OrderNotFoundException"))
                )));
  }

  // ===== Helper Methods =====

  /**
   * Order Request 공통 필드 정의 (주문 생성용)
   *
   * <p>타입, 필수/선택 여부, 제약조건 포함
   */
  private FieldDescriptor[] getCreateOrderRequestFields() {
    return new FieldDescriptor[] {
      fieldWithPath("items")
          .type(JsonFieldType.ARRAY)
          .description("주문 아이템 목록")
          .attributes(key("constraints").value("필수, 최소 1개"))
          .optional(),
      fieldWithPath("items[].itemName")
          .type(JsonFieldType.STRING)
          .description("아이템 이름")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("items[].quantity")
          .type(JsonFieldType.NUMBER)
          .description("수량")
          .attributes(key("constraints").value("필수, 최소 1")),
      fieldWithPath("items[].price")
          .type(JsonFieldType.NUMBER)
          .description("가격")
          .attributes(key("constraints").value("필수, 0 이상")),
      fieldWithPath("items[].category")
          .type(JsonFieldType.STRING)
          .description("카테고리")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("items[].weightInKg")
          .type(JsonFieldType.NUMBER)
          .description("무게 (kg)")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("items[].widthInCm")
          .type(JsonFieldType.NUMBER)
          .description("너비 (cm)")
          .attributes(key("constraints").value("선택, 0 이상"))
          .optional(),
      fieldWithPath("items[].heightInCm")
          .type(JsonFieldType.NUMBER)
          .description("높이 (cm)")
          .attributes(key("constraints").value("선택, 0 이상"))
          .optional(),
      fieldWithPath("items[].depthInCm")
          .type(JsonFieldType.NUMBER)
          .description("깊이 (cm)")
          .attributes(key("constraints").value("선택, 0 이상"))
          .optional(),

      fieldWithPath("origin")
          .type(JsonFieldType.OBJECT)
          .description("출발지 정보")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.contact")
          .type(JsonFieldType.OBJECT)
          .description("출발지 담당자 정보")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.contact.name")
          .type(JsonFieldType.STRING)
          .description("담당자 이름")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.contact.phoneNumber")
          .type(JsonFieldType.STRING)
          .description("담당자 전화번호")
          .attributes(key("constraints").value("필수, 형식: 010-XXXX-XXXX")),
      fieldWithPath("origin.address")
          .type(JsonFieldType.OBJECT)
          .description("출발지 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.address.jibnunAddress")
          .type(JsonFieldType.STRING)
          .description("지번 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.address.roadAddress")
          .type(JsonFieldType.STRING)
          .description("도로명 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.address.detailAddress")
          .type(JsonFieldType.STRING)
          .description("상세 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.latLng")
          .type(JsonFieldType.OBJECT)
          .description("출발지 좌표")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.latLng.latitude")
          .type(JsonFieldType.NUMBER)
          .description("위도")
          .attributes(key("constraints").value("필수, -90 ~ 90")),
      fieldWithPath("origin.latLng.longitude")
          .type(JsonFieldType.NUMBER)
          .description("경도")
          .attributes(key("constraints").value("필수, -180 ~ 180")),
      fieldWithPath("origin.entranceInfo")
          .type(JsonFieldType.OBJECT)
          .description("출발지 출입 정보")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("origin.entranceInfo.password")
          .type(JsonFieldType.STRING)
          .description("출입 비밀번호")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("origin.entranceInfo.guide")
          .type(JsonFieldType.STRING)
          .description("출입 안내")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("origin.entranceInfo.requestMessage")
          .type(JsonFieldType.STRING)
          .description("출입 요청 메시지")
          .attributes(key("constraints").value("선택"))
          .optional(),

      fieldWithPath("destination")
          .type(JsonFieldType.OBJECT)
          .description("도착지 정보")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.contact")
          .type(JsonFieldType.OBJECT)
          .description("도착지 담당자 정보")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.contact.name")
          .type(JsonFieldType.STRING)
          .description("담당자 이름")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.contact.phoneNumber")
          .type(JsonFieldType.STRING)
          .description("담당자 전화번호")
          .attributes(key("constraints").value("필수, 형식: 010-XXXX-XXXX")),
      fieldWithPath("destination.address")
          .type(JsonFieldType.OBJECT)
          .description("도착지 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.address.jibnunAddress")
          .type(JsonFieldType.STRING)
          .description("지번 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.address.roadAddress")
          .type(JsonFieldType.STRING)
          .description("도로명 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.address.detailAddress")
          .type(JsonFieldType.STRING)
          .description("상세 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.latLng")
          .type(JsonFieldType.OBJECT)
          .description("도착지 좌표")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.latLng.latitude")
          .type(JsonFieldType.NUMBER)
          .description("위도")
          .attributes(key("constraints").value("필수, -90 ~ 90")),
      fieldWithPath("destination.latLng.longitude")
          .type(JsonFieldType.NUMBER)
          .description("경도")
          .attributes(key("constraints").value("필수, -180 ~ 180")),
      fieldWithPath("destination.entranceInfo")
          .type(JsonFieldType.OBJECT)
          .description("도착지 출입 정보")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("destination.entranceInfo.password")
          .type(JsonFieldType.STRING)
          .description("출입 비밀번호")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("destination.entranceInfo.guide")
          .type(JsonFieldType.STRING)
          .description("출입 안내")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("destination.entranceInfo.requestMessage")
          .type(JsonFieldType.STRING)
          .description("출입 요청 메시지")
          .attributes(key("constraints").value("선택"))
          .optional(),

      fieldWithPath("deliveryPolicy")
          .type(JsonFieldType.OBJECT)
          .description("배송 정책")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("deliveryPolicy.alcoholDelivery")
          .type(JsonFieldType.BOOLEAN)
          .description("주류 배송 여부")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("deliveryPolicy.contactlessDelivery")
          .type(JsonFieldType.BOOLEAN)
          .description("비대면 배송 여부")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("deliveryPolicy.reservedDelivery")
          .type(JsonFieldType.BOOLEAN)
          .description("예약 배송 여부")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("deliveryPolicy.reservedDeliveryStartTime")
          .type(JsonFieldType.STRING)
          .description("예약 배송 시작 시간")
          .attributes(key("constraints").value("선택, ISO 8601 형식"))
          .optional(),
      fieldWithPath("deliveryPolicy.pickupRequestTime")
          .type(JsonFieldType.STRING)
          .description("픽업 요청 시간")
          .attributes(key("constraints").value("필수, ISO 8601 형식"))
    };
  }

  /**
   * Order Response 공통 필드 정의
   *
   * <p>모든 Order 조회/생성 API에서 공통으로 사용하는 Response 필드
   * <p>타입, 필수/선택 여부, 제약조건 포함
   */
  private FieldDescriptor[] getOrderResponseFields() {
    return new FieldDescriptor[] {
      fieldWithPath("id")
          .type(JsonFieldType.NUMBER)
          .description("주문 ID")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("orderNumber")
          .type(JsonFieldType.STRING)
          .description("주문 번호")
          .attributes(key("constraints").value("필수, 고유값")),
      fieldWithPath("status")
          .type(JsonFieldType.STRING)
          .description("주문 상태")
          .attributes(key("constraints").value(EnumDocs.formatRequired(OrderStatus.class))),
      
      fieldWithPath("items")
          .type(JsonFieldType.ARRAY)
          .description("주문 아이템 목록")
          .attributes(key("constraints").value("필수, 최소 1개")),
      fieldWithPath("items[].itemName")
          .type(JsonFieldType.STRING)
          .description("아이템 이름")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("items[].quantity")
          .type(JsonFieldType.NUMBER)
          .description("수량")
          .attributes(key("constraints").value("필수, 최소 1")),
      fieldWithPath("items[].price")
          .type(JsonFieldType.NUMBER)
          .description("가격")
          .attributes(key("constraints").value("필수, 0 이상")),
      fieldWithPath("items[].category")
          .type(JsonFieldType.STRING)
          .description("카테고리")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("items[].weightInKg")
          .type(JsonFieldType.NUMBER)
          .description("무게 (kg)")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("items[].widthInCm")
          .type(JsonFieldType.NUMBER)
          .description("너비 (cm)")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("items[].heightInCm")
          .type(JsonFieldType.NUMBER)
          .description("높이 (cm)")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("items[].depthInCm")
          .type(JsonFieldType.NUMBER)
          .description("깊이 (cm)")
          .attributes(key("constraints").value("선택"))
          .optional(),

      fieldWithPath("origin")
          .type(JsonFieldType.OBJECT)
          .description("출발지 정보")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.contact")
          .type(JsonFieldType.OBJECT)
          .description("출발지 담당자 정보")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.contact.name")
          .type(JsonFieldType.STRING)
          .description("담당자 이름")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.contact.phoneNumber")
          .type(JsonFieldType.STRING)
          .description("담당자 전화번호")
          .attributes(key("constraints").value("필수, 형식: 010-XXXX-XXXX")),
      fieldWithPath("origin.address")
          .type(JsonFieldType.OBJECT)
          .description("출발지 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.address.jibnunAddress")
          .type(JsonFieldType.STRING)
          .description("지번 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.address.roadAddress")
          .type(JsonFieldType.STRING)
          .description("도로명 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.address.detailAddress")
          .type(JsonFieldType.STRING)
          .description("상세 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.latLng")
          .type(JsonFieldType.OBJECT)
          .description("출발지 좌표")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("origin.latLng.latitude")
          .type(JsonFieldType.NUMBER)
          .description("위도")
          .attributes(key("constraints").value("필수, -90 ~ 90")),
      fieldWithPath("origin.latLng.longitude")
          .type(JsonFieldType.NUMBER)
          .description("경도")
          .attributes(key("constraints").value("필수, -180 ~ 180")),
      fieldWithPath("origin.entranceInfo")
          .type(JsonFieldType.OBJECT)
          .description("출발지 출입 정보")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("origin.entranceInfo.password")
          .type(JsonFieldType.STRING)
          .description("출입 비밀번호")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("origin.entranceInfo.guide")
          .type(JsonFieldType.STRING)
          .description("출입 안내")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("origin.entranceInfo.requestMessage")
          .type(JsonFieldType.STRING)
          .description("출입 요청 메시지")
          .attributes(key("constraints").value("선택"))
          .optional(),

      fieldWithPath("destination")
          .type(JsonFieldType.OBJECT)
          .description("도착지 정보")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.contact")
          .type(JsonFieldType.OBJECT)
          .description("도착지 담당자 정보")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.contact.name")
          .type(JsonFieldType.STRING)
          .description("담당자 이름")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.contact.phoneNumber")
          .type(JsonFieldType.STRING)
          .description("담당자 전화번호")
          .attributes(key("constraints").value("필수, 형식: 010-XXXX-XXXX")),
      fieldWithPath("destination.address")
          .type(JsonFieldType.OBJECT)
          .description("도착지 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.address.jibnunAddress")
          .type(JsonFieldType.STRING)
          .description("지번 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.address.roadAddress")
          .type(JsonFieldType.STRING)
          .description("도로명 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.address.detailAddress")
          .type(JsonFieldType.STRING)
          .description("상세 주소")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.latLng")
          .type(JsonFieldType.OBJECT)
          .description("도착지 좌표")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("destination.latLng.latitude")
          .type(JsonFieldType.NUMBER)
          .description("위도")
          .attributes(key("constraints").value("필수, -90 ~ 90")),
      fieldWithPath("destination.latLng.longitude")
          .type(JsonFieldType.NUMBER)
          .description("경도")
          .attributes(key("constraints").value("필수, -180 ~ 180")),
      fieldWithPath("destination.entranceInfo")
          .type(JsonFieldType.OBJECT)
          .description("도착지 출입 정보")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("destination.entranceInfo.password")
          .type(JsonFieldType.STRING)
          .description("출입 비밀번호")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("destination.entranceInfo.guide")
          .type(JsonFieldType.STRING)
          .description("출입 안내")
          .attributes(key("constraints").value("선택"))
          .optional(),
      fieldWithPath("destination.entranceInfo.requestMessage")
          .type(JsonFieldType.STRING)
          .description("출입 요청 메시지")
          .attributes(key("constraints").value("선택"))
          .optional(),

      fieldWithPath("deliveryPolicy")
          .type(JsonFieldType.OBJECT)
          .description("배송 정책")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("deliveryPolicy.alcoholDelivery")
          .type(JsonFieldType.BOOLEAN)
          .description("주류 배송 여부")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("deliveryPolicy.contactlessDelivery")
          .type(JsonFieldType.BOOLEAN)
          .description("비대면 배송 여부")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("deliveryPolicy.reservedDelivery")
          .type(JsonFieldType.BOOLEAN)
          .description("예약 배송 여부")
          .attributes(key("constraints").value("필수")),
      fieldWithPath("deliveryPolicy.reservedDeliveryStartTime")
          .type(JsonFieldType.STRING)
          .description("예약 배송 시작 시간")
          .attributes(key("constraints").value("선택, ISO 8601 형식"))
          .optional(),
      fieldWithPath("deliveryPolicy.pickupRequestTime")
          .type(JsonFieldType.STRING)
          .description("픽업 요청 시간")
          .attributes(key("constraints").value("필수, ISO 8601 형식")),

      fieldWithPath("orderedAt")
          .type(JsonFieldType.STRING)
          .description("주문 시각")
          .attributes(key("constraints").value("필수, ISO 8601 형식")),
      fieldWithPath("deliveredAt")
          .type(JsonFieldType.STRING)
          .description("배송 완료 시각")
          .attributes(key("constraints").value("선택, ISO 8601 형식"))
          .optional(),
      fieldWithPath("cancelledAt")
          .type(JsonFieldType.STRING)
          .description("취소 시각")
          .attributes(key("constraints").value("선택, ISO 8601 형식"))
          .optional()
    };
  }

  private CreateOrderRequest createOrderRequest() {
    OrderItemDto item =
        new OrderItemDto(
            "테스트 상품",
            2,
            BigDecimal.valueOf(15000),
            "식품",
            BigDecimal.valueOf(1.5),
            BigDecimal.valueOf(10),
            BigDecimal.valueOf(20),
            BigDecimal.valueOf(30));

    ContactDto contact = new ContactDto("홍길동", "010-1234-5678");
    AddressDto address = new AddressDto("서울시 강남구", "테헤란로 123", "1층");
    LatLngDto latLng = new LatLngDto(BigDecimal.valueOf(37.123), BigDecimal.valueOf(127.456));
    EntranceInfoDto entranceInfo = new EntranceInfoDto("1234", "정문 출입", "문을 열어주세요");

    OriginDto origin = new OriginDto(contact, address, latLng, entranceInfo);
    DestinationDto destination = new DestinationDto(contact, address, latLng, entranceInfo);
    DeliveryPolicyDto deliveryPolicy =
        new DeliveryPolicyDto(false, false, false, null, Instant.now());

    return new CreateOrderRequest(List.of(item), origin, destination, deliveryPolicy);
  }

  private Order createMockOrder() {
    OrderItem item =
        new OrderItem(
            "테스트 상품",
            2,
            new Money(BigDecimal.valueOf(15000)),
            "식품",
            null,
            null);

    Contact contact = new Contact("홍길동", "010-1234-5678");
    Address address = new Address("서울시 강남구", "테헤란로 123", "1층");
    LatLng latLng = new LatLng(BigDecimal.valueOf(37.123), BigDecimal.valueOf(127.456));
    EntranceInfo entranceInfo = new EntranceInfo("1234", "정문 출입", "문을 열어주세요");

    Origin origin = new Origin(contact, address, latLng, entranceInfo);
    Destination destination = new Destination(contact, address, latLng, entranceInfo);
    DeliveryPolicy deliveryPolicy =
        new DeliveryPolicy(false, false, false, null, Instant.now());

    return new Order(
        1L,
        OrderNumber.of("ORD-20250112-000001"),
        OrderStatus.CREATED,
        List.of(item),
        origin,
        destination,
        deliveryPolicy,
        Instant.now(),
        null,
        null);
  }
}

