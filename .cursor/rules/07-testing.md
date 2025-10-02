# 테스트 작성 규칙

이 문서는 테스트 코드 작성 시 따라야 할 규칙과 가이드라인을 정의합니다.

---

## 🚨 핵심 원칙

### ⛔ 절대 규칙: 테스트 없는 코드는 완료가 아님

**모든 작업에는 테스트가 필수입니다.**

- ✅ 프로덕션 코드 작성 → 테스트 코드 작성
- ✅ 테스트가 통과해야 작업 완료
- ❌ "나중에 테스트 추가" 금지

---

## 🛠 기술 스택

### 필수 라이브러리

```gradle
dependencies {
    // JUnit 5
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
    
    // AssertJ (가독성 좋은 assertion)
    testImplementation 'org.assertj:assertj-core:3.24.0'
    
    // Mockito (Mock 객체)
    testImplementation 'org.mockito:mockito-core:5.5.0'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.5.0'
    
    // Fixture Monkey (테스트 객체 생성)
    testImplementation 'com.navercorp.fixturemonkey:fixture-monkey-starter:1.0.0'
    
    // Spring Boot Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### 주요 도구

| 도구 | 용도 | 사용 위치 |
|------|------|-----------|
| **JUnit 5** | 테스트 프레임워크 | 모든 테스트 |
| **AssertJ** | Assertion | 모든 테스트 |
| **Mockito** | Mock 객체 | Application, Interface Layer |
| **Fixture Monkey** | 테스트 객체 생성 | 모든 테스트 |
| **@DataJpaTest** | Repository 테스트 | Infrastructure Layer |
| **@WebMvcTest** | Controller 테스트 | Interface Layer |

---

## 📋 계층별 테스트 전략

### 1. Domain Layer 테스트

**특징:**
- 순수 Java 단위 테스트
- 외부 의존성 없음
- 비즈니스 로직 검증

**테스트 대상:**
- Domain Entity
- Value Object
- Domain Service
- Domain Exception

**예시:**
```java
// core/src/test/java/vroong/laas/order/domain/order/OrderTest.java
class OrderTest {
    
    private FixtureMonkey fixtureMonkey;
    
    @BeforeEach
    void setUp() {
        fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();
    }
    
    @Test
    @DisplayName("주문을 취소하면 상태가 CANCELLED로 변경된다")
    void cancel_order_changes_status_to_cancelled() {
        // given
        Order order = fixtureMonkey.giveMeBuilder(Order.class)
            .set("status", OrderStatus.PENDING)
            .sample();
        
        String reason = "고객 요청";
        
        // when
        order.cancel(reason);
        
        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelReason()).isEqualTo(reason);
    }
    
    @Test
    @DisplayName("이미 취소된 주문은 다시 취소할 수 없다")
    void cannot_cancel_already_cancelled_order() {
        // given
        Order order = fixtureMonkey.giveMeBuilder(Order.class)
            .set("status", OrderStatus.CANCELLED)
            .sample();
        
        // when & then
        assertThatThrownBy(() -> order.cancel("재취소"))
            .isInstanceOf(OrderAlreadyCancelledException.class)
            .hasMessage("이미 취소된 주문입니다");
    }
}
```

---

### 2. Application Layer 테스트

**특징:**
- UseCase 테스트
- Mock으로 Port 대체
- 흐름 검증 (Given-When-Then)

**테스트 대상:**
- UseCase
- Command/Query

**예시:**
```java
// core/src/test/java/vroong/laas/order/application/order/CancelOrderUseCaseTest.java
@ExtendWith(MockitoExtension.class)
class CancelOrderUseCaseTest {
    
    @InjectMocks
    private CancelOrderUseCase sut;
    
    @Mock
    private OrderStore orderStore;
    
    @Mock
    private EventPublisher eventPublisher;
    
    private FixtureMonkey fixtureMonkey;
    
    @BeforeEach
    void setUp() {
        fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();
    }
    
    @Test
    @DisplayName("주문을 취소하고 이벤트를 발행한다")
    void cancel_order_and_publish_event() {
        // given
        Long orderId = 1L;
        String reason = "고객 요청";
        
        Order order = fixtureMonkey.giveMeBuilder(Order.class)
            .set("id", orderId)
            .set("status", OrderStatus.PENDING)
            .sample();
        
        given(orderStore.findById(orderId))
            .willReturn(Optional.of(order));
        
        CancelOrderCommand command = new CancelOrderCommand(orderId, reason);
        
        // when
        sut.execute(command);
        
        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        
        verify(orderStore).save(order);
        verify(eventPublisher).publish(any(OrderCancelledEvent.class));
    }
    
    @Test
    @DisplayName("존재하지 않는 주문은 취소할 수 없다")
    void cannot_cancel_non_existing_order() {
        // given
        Long orderId = 999L;
        
        given(orderStore.findById(orderId))
            .willReturn(Optional.empty());
        
        CancelOrderCommand command = new CancelOrderCommand(orderId, "사유");
        
        // when & then
        assertThatThrownBy(() -> sut.execute(command))
            .isInstanceOf(OrderNotFoundException.class);
        
        verify(orderStore, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
}
```

---

### 3. Infrastructure Layer 테스트

**특징:**
- Repository 통합 테스트
- @DataJpaTest 사용
- 실제 DB(H2) 사용

**테스트 대상:**
- Repository Adapter
- JPA Entity
- Query

**예시:**
```java
// infrastructure/src/test/java/vroong/laas/order/infrastructure/storage/db/order/OrderStoreAdapterTest.java
@DataJpaTest
@Import(OrderStoreAdapter.class)
class OrderStoreAdapterTest {
    
    @Autowired
    private OrderStoreAdapter orderStoreAdapter;
    
    @Autowired
    private OrderJpaRepository orderJpaRepository;
    
    private FixtureMonkey fixtureMonkey;
    
    @BeforeEach
    void setUp() {
        fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();
    }
    
    @Test
    @DisplayName("주문을 저장하고 조회할 수 있다")
    void save_and_find_order() {
        // given
        Order order = fixtureMonkey.giveMeBuilder(Order.class)
            .set("id", null)  // 신규 주문
            .set("status", OrderStatus.PENDING)
            .sample();
        
        // when
        Order saved = orderStoreAdapter.save(order);
        Order found = orderStoreAdapter.findById(saved.getId()).orElseThrow();
        
        // then
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
    
    @Test
    @DisplayName("주문과 아이템을 함께 조회할 수 있다")
    void find_order_with_items() {
        // given
        OrderJpaEntity entity = fixtureMonkey.giveMeBuilder(OrderJpaEntity.class)
            .set("id", null)
            .size("items", 3)
            .sample();
        
        OrderJpaEntity saved = orderJpaRepository.save(entity);
        
        // when
        Order order = orderStoreAdapter.findByIdWithItems(saved.getId()).orElseThrow();
        
        // then
        assertThat(order.getItems()).hasSize(3);
    }
}
```

---

### 4. Interface Layer 테스트

**특징:**
- Controller 테스트
- @WebMvcTest 사용
- API 계약 검증

**테스트 대상:**
- Controller
- Request/Response DTO
- Exception Handler

**예시:**
```java
// api/src/test/java/vroong/laas/order/api/web/order/OrderControllerTest.java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CancelOrderUseCase cancelOrderUseCase;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private FixtureMonkey fixtureMonkey;
    
    @BeforeEach
    void setUp() {
        fixtureMonkey = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();
    }
    
    @Test
    @DisplayName("주문 취소 API - 성공")
    void cancel_order_success() throws Exception {
        // given
        Long orderId = 1L;
        
        CancelOrderRequest request = fixtureMonkey.giveMeBuilder(CancelOrderRequest.class)
            .set("reason", "고객 요청")
            .sample();
        
        // when & then
        mockMvc.perform(
                post("/api/orders/{orderId}/cancel", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        
        verify(cancelOrderUseCase).execute(any(CancelOrderCommand.class));
    }
    
    @Test
    @DisplayName("주문 취소 API - 유효성 검증 실패")
    void cancel_order_validation_fail() throws Exception {
        // given
        Long orderId = 1L;
        
        CancelOrderRequest request = new CancelOrderRequest(null);  // reason 누락
        
        // when & then
        mockMvc.perform(
                post("/api/orders/{orderId}/cancel", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
    }
}
```

---

## 🎯 Fixture Monkey 사용 규칙

### 1. 설정

**공통 Fixture 설정 클래스 작성:**

```java
// src/test/java/vroong/laas/order/fixtures/FixtureConfig.java
@TestConfiguration
public class FixtureConfig {
    
    @Bean
    public FixtureMonkey fixtureMonkey() {
        return FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .defaultNotNull(true)
            .build();
    }
}
```

---

### 2. 기본 사용법

#### 단일 객체 생성
```java
// 랜덤 객체
Order order = fixtureMonkey.giveMeOne(Order.class);

// 여러 객체
List<Order> orders = fixtureMonkey.giveMe(Order.class, 10);
```

#### 커스터마이징
```java
// Builder 패턴
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .set("status", OrderStatus.CONFIRMED)
    .set("totalAmount", Money.of(10000))
    .set("items[0].quantity", 5)
    .sample();

// 필드 제외
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .setNull("cancelReason")
    .sample();

// 범위 지정
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .size("items", 3, 5)  // 3~5개
    .sample();
```

---

### 3. 재사용 가능한 Fixture 클래스

**위치:** `src/test/java/.../fixtures/`

**네이밍:** `{Entity}Fixtures.java`

**예시:**
```java
// src/test/java/vroong/laas/order/fixtures/OrderFixtures.java
@Component
public class OrderFixtures {
    
    private final FixtureMonkey fixtureMonkey;
    
    public OrderFixtures(FixtureMonkey fixtureMonkey) {
        this.fixtureMonkey = fixtureMonkey;
    }
    
    // 기본 주문
    public ArbitraryBuilder<Order> order() {
        return fixtureMonkey.giveMeBuilder(Order.class)
            .set("status", OrderStatus.PENDING);
    }
    
    // 확정된 주문
    public ArbitraryBuilder<Order> confirmedOrder() {
        return order()
            .set("status", OrderStatus.CONFIRMED)
            .set("confirmedAt", LocalDateTime.now());
    }
    
    // 취소된 주문
    public ArbitraryBuilder<Order> cancelledOrder() {
        return order()
            .set("status", OrderStatus.CANCELLED)
            .set("cancelReason", "고객 요청");
    }
    
    // 특정 금액의 주문
    public ArbitraryBuilder<Order> orderWithAmount(Money amount) {
        return order()
            .set("totalAmount", amount);
    }
}
```

**사용:**
```java
@SpringBootTest
class OrderServiceTest {
    
    @Autowired
    private OrderFixtures orderFixtures;
    
    @Test
    void test() {
        // 확정된 주문 생성
        Order order = orderFixtures.confirmedOrder().sample();
        
        // 추가 커스터마이징
        Order customOrder = orderFixtures.confirmedOrder()
            .set("totalAmount", Money.of(50000))
            .sample();
    }
}
```

---

### 4. 계층별 Fixture 구성

```
src/test/java/vroong/laas/order/fixtures/
├── domain/
│   ├── OrderFixtures.java          # Domain Entity
│   ├── OrderItemFixtures.java
│   └── MoneyFixtures.java          # Value Object
├── application/
│   ├── CreateOrderCommandFixtures.java   # Command
│   └── CancelOrderCommandFixtures.java
├── infrastructure/
│   └── OrderJpaEntityFixtures.java       # JPA Entity
└── api/
    ├── CreateOrderRequestFixtures.java   # Request DTO
    └── OrderResponseFixtures.java        # Response DTO
```

---

## 📝 테스트 작성 가이드

### 1. Given-When-Then 패턴

모든 테스트는 Given-When-Then 구조를 따릅니다.

```java
@Test
void test_example() {
    // given (준비)
    // - 테스트 데이터 생성
    // - Mock 동작 정의
    Order order = fixtureMonkey.giveMeOne(Order.class);
    given(orderStore.findById(1L)).willReturn(Optional.of(order));
    
    // when (실행)
    // - 테스트할 메서드 호출
    Order result = orderService.getOrder(1L);
    
    // then (검증)
    // - 결과 검증
    // - 상태 변경 검증
    // - Mock 호출 검증
    assertThat(result).isEqualTo(order);
    verify(orderStore).findById(1L);
}
```

---

### 2. 테스트 네이밍 규칙

**형식:** `{테스트_대상}_{조건}_{예상_결과}`

```java
// ✅ 좋은 예
@Test
void cancel_order_with_pending_status_changes_to_cancelled() { }

@Test
void cancel_already_cancelled_order_throws_exception() { }

// ❌ 나쁜 예
@Test
void test1() { }

@Test
void cancelTest() { }
```

---

### 3. @DisplayName 활용

```java
@Test
@DisplayName("대기 중인 주문을 취소하면 상태가 CANCELLED로 변경된다")
void cancel_order_with_pending_status_changes_to_cancelled() {
    // ...
}
```

---

### 4. 예외 케이스 테스트

**필수 테스트 케이스:**
- ✅ 정상 케이스 (Happy Path)
- ✅ 예외 케이스 (Exception)
- ✅ 경계값 케이스 (Boundary)
- ✅ Null 케이스

**예시:**
```java
@Test
@DisplayName("주문 취소 - 정상 케이스")
void cancel_order_success() { }

@Test
@DisplayName("주문 취소 - 존재하지 않는 주문")
void cancel_order_not_found() {
    assertThatThrownBy(() -> orderService.cancel(999L))
        .isInstanceOf(OrderNotFoundException.class);
}

@Test
@DisplayName("주문 취소 - 이미 취소된 주문")
void cancel_order_already_cancelled() {
    assertThatThrownBy(() -> order.cancel("reason"))
        .isInstanceOf(OrderAlreadyCancelledException.class);
}

@Test
@DisplayName("주문 취소 - null reason")
void cancel_order_with_null_reason() {
    assertThatThrownBy(() -> order.cancel(null))
        .isInstanceOf(IllegalArgumentException.class);
}
```

---

### 5. AssertJ 활용

```java
// 기본 검증
assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

// 컬렉션 검증
assertThat(orders)
    .hasSize(3)
    .extracting("status")
    .containsOnly(OrderStatus.PENDING);

// 예외 검증
assertThatThrownBy(() -> order.cancel("reason"))
    .isInstanceOf(OrderAlreadyCancelledException.class)
    .hasMessage("이미 취소된 주문입니다");

// 객체 검증
assertThat(order)
    .extracting("id", "status", "totalAmount")
    .containsExactly(1L, OrderStatus.PENDING, Money.of(10000));
```

---

### 6. Mockito 활용

```java
// Mock 동작 정의
given(orderStore.findById(1L)).willReturn(Optional.of(order));

// void 메서드 Mock
willDoNothing().given(eventPublisher).publish(any());

// 예외 던지기
given(orderStore.findById(999L))
    .willThrow(new OrderNotFoundException());

// 호출 검증
verify(orderStore).save(order);
verify(eventPublisher).publish(any(OrderCancelledEvent.class));

// 호출 횟수 검증
verify(orderStore, times(1)).save(order);
verify(eventPublisher, never()).publish(any());

// 인자 캡처
ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
verify(orderStore).save(captor.capture());
assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
```

---

## ✅ 테스트 체크리스트

매 작업마다 다음을 확인하세요:

### Domain Layer
- [ ] Entity 생성 테스트
- [ ] 비즈니스 로직 테스트
- [ ] 예외 케이스 테스트
- [ ] Value Object 불변성 테스트

### Application Layer
- [ ] UseCase 정상 흐름 테스트
- [ ] UseCase 예외 케이스 테스트
- [ ] Mock 호출 검증
- [ ] Command/Query 유효성 테스트

### Infrastructure Layer
- [ ] Repository 저장/조회 테스트
- [ ] Query 성능 테스트 (N+1 체크)
- [ ] 트랜잭션 격리 테스트

### Interface Layer
- [ ] API 정상 응답 테스트
- [ ] API 예외 응답 테스트
- [ ] 유효성 검증 테스트
- [ ] 인증/인가 테스트

---

## 🚫 금지 사항

### 1. 테스트 없이 코드 작성 금지
```java
// ❌ 이렇게 하지 마세요
// 프로덕션 코드만 작성하고 "나중에 테스트 추가"
```

### 2. Thread.sleep() 사용 금지
```java
// ❌ 나쁜 예
@Test
void async_test() throws Exception {
    asyncService.process();
    Thread.sleep(1000);  // 금지!
    verify(repository).save(any());
}

// ✅ 좋은 예 - Awaitility 사용
@Test
void async_test() {
    asyncService.process();
    
    await().atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> {
            verify(repository).save(any());
        });
}
```

### 3. 테스트 간 의존성 금지
```java
// ❌ 나쁜 예 - 테스트 순서에 의존
private static Order sharedOrder;

@Test
@Order(1)
void create_order() {
    sharedOrder = orderService.create(...);  // 금지!
}

@Test
@Order(2)
void cancel_order() {
    orderService.cancel(sharedOrder.getId());  // 금지!
}

// ✅ 좋은 예 - 각 테스트가 독립적
@Test
void cancel_order() {
    Order order = fixtureMonkey.giveMeOne(Order.class);
    orderService.cancel(order.getId());
}
```

### 4. 과도한 Mock 사용 금지
```java
// ❌ 나쁜 예 - 너무 많은 Mock
@Mock private Service1 service1;
@Mock private Service2 service2;
@Mock private Service3 service3;
// ... 10개 이상의 Mock

// 💡 힌트: Mock이 너무 많다면 설계를 다시 검토하세요
```

---

## 📚 참고 자료

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Fixture Monkey Documentation](https://naver.github.io/fixture-monkey/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

---

이 규칙을 따라 견고하고 유지보수 가능한 테스트 코드를 작성하세요! 🚀

