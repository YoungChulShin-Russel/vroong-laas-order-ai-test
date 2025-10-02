# 코드 구조
```
order-service/
│
├── core/                                    # 비즈니스 로직 모듈
│   └── src/main/java/com/company/order/
│       ├── domain/                          # Domain Layer
│       │   ├── order/
│       │   │   ├── Order.java
│       │   │   ├── OrderItem.java
│       │   │   ├── OrderStatus.java
│       │   │   ├── Origin.java
│       │   │   ├── Destination.java
│       │   │   ├── DeliveryPolicy.java
│       │   │   └── required/                   # ⭐ 모든 외부 의존성 Port
│       │   │       ├── OrderStore.java          # 영속성 (쓰기)
│       │   │       ├── OrderReader.java         # 영속성 (읽기)
│       │   │       ├── EmailSender.java
│       │   │       ├── EventPublisher.java
│       │   │       └── PaymentGateway.java
│       │   │   ├── event/
│       │   │   │   ├── OrderCreatedEvent.java
│       │   │   │   ├── OrderCancelledEvent.java
│       │   │   │   └── OrderStatusChangedEvent.java
│       │   │   └── exception/
│       │   │       ├── OrderNotFoundException.java
│       │   │       ├── OrderNotCancellableException.java
│       │   │       └── InvalidOrderException.java
│       │   │
│       │   ├── payment/
│       │   │   ├── model/
│       │   │   │   ├── Payment.java
│       │   │   │   ├── PaymentMethod.java
│       │   │   │   └── PaymentStatus.java
│       │   │   ├── repository/
│       │   │   │   └── PaymentRepository.java
│       │   │   └── exception/
│       │   │       └── PaymentProcessingException.java
│       │   │
│       │   ├── coupon/
│       │   │   ├── model/
│       │   │   │   ├── Coupon.java
│       │   │   │   ├── CouponType.java
│       │   │   │   └── CouponStatus.java
│       │   │   ├── repository/
│       │   │   │   └── CouponRepository.java
│       │   │   └── exception/
│       │   │       ├── CouponExpiredException.java
│       │   │       └── CouponNotFoundException.java
│       │   │
│       │   ├── customer/
│       │   │   ├── model/
│       │   │   │   ├── Customer.java
│       │   │   │   └── CustomerGrade.java
│       │   │   ├── repository/
│       │   │   │   └── CustomerRepository.java
│       │   │   └── exception/
│       │   │       └── CustomerNotFoundException.java
│       │   │
│       │   ├── service/                     # Domain Service
│       │   │   ├── OrderPricingService.java
│       │   │   ├── OrderValidationService.java
│       │   │   └── RefundService.java
│       │   │
│       │   └── policy/
│       │       ├── ShippingFeePolicy.java
│       │       └── TieredShippingFeePolicy.java
│       │
│       └── application/                     # Application Layer
│           ├── UseCase.java                 # 마커 인터페이스
│           │
│           └── order/
│               ├── usecase/
│               │   ├── command/
│               │   │   ├── CreateOrderUseCase.java
│               │   │   ├── CancelOrderUseCase.java
│               │   │   ├── ChangeAddressUseCase.java
│               │   │   ├── CreateOrderCommand.java
│               │   │   ├── CancelOrderCommand.java
│               │   │   └── ChangeAddressCommand.java
│               │   │
│               │   └── query/
│               │       ├── GetOrderUseCase.java
│               │       ├── GetMyOrdersUseCase.java
│               │       ├── SearchOrdersUseCase.java
│               │       ├── GetOrderQuery.java
│               │       ├── GetMyOrdersQuery.java
│               │       └── SearchOrdersQuery.java
│               │
│               ├── dto/
│               │   ├── OrderPreparationResult.java
│               │   ├── OrderSummary.java
│               │   ├── OrderPrice.java
│               │   └── OrderSearchCondition.java
│               │
│               └── admin/
│                   ├── OrderAdminQueryService.java
│                   └── OrderAdminCommandService.java
│
├── infrastructure/                          # 기술 구현 모듈
│   └── src/main/java/com/company/order/infrastructure/
│       ├── storage/
│       │   ├── db/
│       │   │   ├── order/
│       │   │   │   ├── entity/
│       │   │   │   │   ├── OrderJpaEntity.java
│       │   │   │   │   ├── OrderItemJpaEntity.java
│       │   │   │   │   └── OriginEmbed.java
│       │   │   │   ├── OrderRepositoryAdapter.java    # ⭐ Domain Repository 구현
│       │   │   │   └── OrderJpaRepository.java        # Spring Data JPA
│       │   │   │
│       │   │   ├── payment/
│       │   │   │   ├── entity/
│       │   │   │   │   └── PaymentJpaEntity.java
│       │   │   │   └── adapter/
│       │   │   │       └── PaymentRepositoryAdapter.java
│       │   │   │
│       │   │   ├── coupon/
│       │   │   │   ├── entity/
│       │   │   │   │   └── CouponJpaEntity.java
│       │   │   │   └── adapter/
│       │   │   │       └── CouponRepositoryAdapter.java
│       │   │   │
│       │   │   └── customer/
│       │   │       ├── entity/
│       │   │       │   └── CustomerJpaEntity.java
│       │   │       └── adapter/
│       │   │           └── CustomerRepositoryAdapter.java
│       │   │
│       │   └── cache/
│       │       ├── order/
│       │       │   ├── OrderCacheAdapter.java
│       │       │   └── OrderCacheDto.java
│       │       └── config/
│       │           └── RedisConfig.java
│       │
│       ├── external/
│       │   ├── client/
│       │   │   ├── PaymentFeignClient.java
│       │   │   └── DeliveryFeignClient.java
│       │   ├── adapter/                        # ⭐ Application Port 구현
│       │   │   ├── PaymentGatewayAdapter.java
│       │   │   └── DeliveryAdapter.java
│       │   └── dto/
│       │       ├── PaymentApiRequest.java
│       │       └── PaymentApiResponse.java
│       │
│       ├── messaging/
│       │   ├── adapter/                        # ⭐ Application Port 구현
│       │   │   ├── EmailSenderAdapter.java
│       │   │   ├── SmsSenderAdapter.java
│       │   │   ├── EventPublisherAdapter.java
│       │   │   └── OrderEventConsumer.java
│       │   └── config/
│       │       └── KafkaConfig.java
│       │
│       └── config/
│           ├── JpaConfig.java
│           └── QueryDslConfig.java
│
├── api/                                     # 고객용 API 모듈
│   └── src/main/java/com/company/order/api/
│       ├── ApiApplication.java
│       │
│       ├── web/
│       │   └── order/
│       │       ├── OrderController.java
│       │       ├── OrderPaymentController.java
│       │       ├── OrderCancellationController.java
│       │       ├── dto/
│       │       │   ├── request/
│       │       │   │   ├── CreateOrderRequest.java
│       │       │   │   ├── CancelOrderRequest.java
│       │       │   │   └── ChangeAddressRequest.java
│       │       │   └── response/
│       │       │       ├── OrderResponse.java
│       │       │       ├── OrderDetailResponse.java
│       │       │       └── OrderSummaryResponse.java
│       │       └── config/
│       │           ├── ApiSecurityConfig.java
│       │           └── ApiExceptionHandler.java
│       │
│       └── grpc/
│           └── order/
│               ├── OrderGrpcService.java
│               └── mapper/
│                   └── OrderGrpcMapper.java
│
├── admin/                                   # 관리자용 모듈
│   └── src/main/java/com/company/order/admin/
│       ├── AdminApplication.java
│       │
│       └── web/
│           └── order/
│               ├── OrderAdminController.java
│               ├── OrderStatisticsController.java
│               ├── dto/
│               │   ├── request/
│               │   │   ├── AdminCancelRequest.java
│               │   │   └── OrderSearchRequest.java
│               │   └── response/
│               │       ├── OrderAdminResponse.java
│               │       └── OrderStatisticsResponse.java
│               └── config/
│                   └── AdminSecurityConfig.java
│
└── worker/                                  # 배치 모듈
    └── src/main/java/com/company/order/worker/
        ├── WorkerApplication.java
        │
        └── job/
            └── order/
                ├── OrderCleanupJob.java
                ├── OrderStatisticsJob.java
                └── config/
                    └── BatchConfig.java
```

# 주요 디렉토리별 설명
## core/domain/
- 역할: 순수 비즈니스 로직 + Port 인터페이스 정의
- 특징:
  - JPA, Spring 어노테이션 없음
  - 순수 Java + Lombok만 사용
  - 비즈니스 규칙과 로직 포함
  - 자기 검증 구현
  - **Port 인터페이스 정의** (Repository + Required Port)
- 포함 요소:
  - Order.java, OrderItem.java - Entity
  - OrderRepository.java - Repository 인터페이스 ⭐
  - required/ - Required Port 인터페이스 ⭐
    - EmailSender.java
    - EventPublisher.java
    - PaymentGateway.java
  - service/ - Domain Service (순수 계산)
  - event/ - Domain Event
  - exception/ - Domain 예외
- 예시:
  - Order.java - 주문 Aggregate Root
  - OrderRepository.java - Repository 인터페이스
  - required/EmailSender.java - Required Port 인터페이스

**중요:**
- **모든 Port는 Domain Layer에 위치** (DIP 적용)
- Infrastructure에서 Adapter로 구현

## core/application/
- 역할: Use Case 실행 및 흐름 조정
- 특징:
  - UseCase별 파일 분리 (하나의 파일 = 하나의 execute())
  - Domain의 Port만 의존 (구체 클래스)
  - Domain과 Infrastructure 연결
  - 트랜잭션 관리는 UseCase에서 직접 처리
- 포함 요소:
  - usecase/command/ - 명령 Use Case (구체 클래스)
  - usecase/query/ - 조회 Use Case (구체 클래스)
  - command/ - Command DTO
  - query/ - Query DTO
  - dto/ - Application 계층 DTO
  - admin/ - 관리자용 서비스
- 예시:
  - CreateOrderUseCase.java - 주문 생성 Use Case (구체 클래스)
  - CancelOrderUseCase.java - 주문 취소 Use Case (구체 클래스)
  - CreateOrderCommand.java - Command DTO

**중요:**
- **모든 Port는 Domain Layer에 위치** (`core/domain/order/`)
- **UseCase는 구체 클래스** (In Port 없음)

### 트랜잭션 전략

**기본 원칙**: Repository Adapter에서 트랜잭션 관리, 여러 Aggregate 수정 시에만 UseCase에서 TransactionTemplate 사용

#### 1) 기본 - Repository Adapter에 @Transactional

**적용 조건:**
- 단일 Aggregate만 수정하는 대부분의 경우
- 가장 일반적인 패턴

**예시:**
```java
// Infrastructure - Repository Adapter
@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {
    
    private final OrderJpaRepository jpaRepository;
    
    @Transactional  // ⭐ Repository Adapter에서 트랜잭션
    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = OrderJpaEntity.from(order);
        OrderJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
    
    @Transactional(readOnly = true)
    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id)
            .map(OrderJpaEntity::toDomain);
    }
}

// UseCase - 트랜잭션 없음
@UseCase
@RequiredArgsConstructor
public class CancelOrderUseCase {
    
    private final OrderRepository orderRepository;
    
    public void execute(CancelOrderCommand command) {
        // Repository에서 트랜잭션 처리
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException());
        
        order.cancel(command.getReason());
        
        orderRepository.save(order);  // 여기서 짧은 트랜잭션
    }
}
```

**특징:**
- ✅ 짧은 트랜잭션 (메서드 단위)
- ✅ DB 커넥션 최소 점유
- ✅ UseCase 코드 단순
- ✅ 외부 API 호출 시 트랜잭션 걱정 없음
- ⚠️ 낙관적 락(@Version) 필수

---

#### 2) 여러 Aggregate 수정 - TransactionTemplate

**적용 조건:**
- **여러 Aggregate를 동시에 수정해야 하는 경우 (가장 중요)**
- 원자성이 필요한 비즈니스 로직
- 예: Order + Coupon, Order + Customer 동시 수정

**예시:**
```java
@UseCase
@RequiredArgsConstructor
public class CreateOrderUseCase {
    
    private final TransactionTemplate transactionTemplate;
    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;
    
    public Order execute(CreateOrderCommand command) {
        
        // ===== 여러 Aggregate를 하나의 트랜잭션으로 =====
        Order order = transactionTemplate.execute(status -> {
            
            // 1. Coupon 사용 (Aggregate 1)
            Coupon coupon = null;
            if (command.getCouponId() != null) {
                coupon = couponRepository.findById(command.getCouponId())
                    .orElseThrow(() -> new CouponNotFoundException());
                coupon.use();
                couponRepository.save(coupon);  // Coupon 수정
            }
            
            // 2. Order 생성 (Aggregate 2)
            Order o = Order.create(
                command.getUserId(),
                command.getItems(),
                command.getDeliveryAddress(),
                coupon
            );
            
            // 3. 모두 한 트랜잭션으로 커밋
            return orderRepository.save(o);  // Order 수정
        });
        
        return order;
    }
}
```

**특징:**
- ✅ Order와 Coupon을 동시에 수정
- ✅ 원자성 보장 (둘 다 성공 or 둘 다 실패)
- ✅ 일관성 유지
- ⚠️ 낙관적 락(@Version) 필수

---

#### 3) 외부 API 호출 사이 - TransactionTemplate 분리

외부 API 호출이 필요한 경우, 트랜잭션을 분리해서 DB 커넥션 점유 최소화

**예시:**
```java
@UseCase
@RequiredArgsConstructor
public class ProcessOrderPaymentUseCase {
    
    private final TransactionTemplate transactionTemplate;
    private final OrderRepository orderRepository;
    private final PaymentPort paymentPort;  // 외부 API
    
    public void execute(ProcessPaymentCommand command) {
        
        // ===== 트랜잭션 1: 주문 조회/검증 =====
        Order order = transactionTemplate.execute(status -> {
            Order o = orderRepository.findById(command.getOrderId())
                .orElseThrow();
            
            if (!o.isPayable()) {
                throw new OrderNotPayableException();
            }
            
            return o;
        });
        
        // ===== 외부 API (트랜잭션 밖) =====
        PaymentResult payment = paymentPort.process(
            order.getId(),
            order.getTotalAmount()
        );
        
        // ===== 트랜잭션 2: 결제 완료 처리 =====
        transactionTemplate.executeWithoutResult(status -> {
            Order o = orderRepository.findById(order.getId())
                .orElseThrow();
            o.completePayment(payment);
            orderRepository.save(o);
        });
    }
}
```

**특징:**
- ✅ 트랜잭션 경계가 코드에서 명확히 보임
- ✅ 외부 API 호출 시 DB 커넥션 점유 안 함
- ✅ 성능 최적화 가능

**트랜잭션 흐름:**
```
TX1: 주문 조회/검증 (0.01초) → 커밋
  ↓
외부 API: 결제 처리 (3초) - DB 커넥션 점유 안 함
  ↓
TX2: 주문 완료 처리 (0.01초) → 커밋

총 DB 커넥션 점유: 0.02초 (vs @Transactional: 3.02초)
```

---

#### 주의사항:

**1) RuntimeException 사용**
- Domain Exception은 RuntimeException을 상속해야 함
- TransactionTemplate은 CheckedException을 던질 수 없음

```java
// ✅ 올바른 예외 정의
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}

// ❌ CheckedException 사용 불가
public class OrderNotFoundException extends Exception {  // 컴파일 에러
}
```

**2) 보상 트랜잭션 필수**
- 트랜잭션이 분리되면 부분 실패 가능
- 실패 시 이미 커밋된 작업을 취소하는 로직 필요

**3) 낙관적 락 권장**
- 트랜잭션이 분리되면 동시성 이슈 발생 가능
- @Version을 사용한 낙관적 락 권장

```java
// Domain
public class Order {
    private Long id;
    private Long version;  // 동시성 제어
    // ...
}

// JpaEntity
@Entity
public class OrderJpaEntity {
    @Id
    private Long id;
    
    @Version  // 낙관적 락
    private Long version;
}
```

---

#### 선택 가이드:

| 조건 | 방법 |
|------|------|
| 외부 API 호출 없음 | @Transactional |
| 외부 API가 트랜잭션 전/후에만 | @Transactional |
| 외부 API가 트랜잭션 사이에 | TransactionTemplate |
| DB 커넥션 최적화 필요 | TransactionTemplate |
| 단순한 CRUD | @Transactional |
| 복잡한 Saga 패턴 | TransactionTemplate |

## infrastructure/storage/db/
- 역할: 데이터베이스 접근 (JPA)
- 특징:
  - JpaEntity 사용 (@Entity, @Table)
  - from(Domain) / toDomain() 메서드로 변환
  - Adapter 패턴으로 Repository 구현
  - QueryDSL로 동적 쿼리
- 포함 요소:
  - entity/ - JPA Entity
  - adapter/ - Repository 구현체
  - query/ - QueryDSL 쿼리 구현
- 예시:
  - OrderJpaEntity.java - JPA Entity
  - OrderRepositoryAdapter.java - Repository 구현
  - OrderQueryAdapter.java - QueryDSL 조회

## infrastructure/storage/cache/
- 역할: 캐시 처리 (Redis)
- 특징:
  - RedisTemplate 사용
  - Cache DTO로 변환
  - TTL 설정
- 포함 요소:
  - order/ - 주문 캐시 Adapter
  - config/ - Redis 설정
- 예시:
  - OrderCacheAdapter.java - 캐시 Adapter
  - OrderCacheDto.java - 캐시용 DTO

## infrastructure/external/
- 역할: 외부 API 연동
- 특징:
  - Feign Client 사용
  - Adapter 패턴으로 Port 구현
  - 외부 API 오류를 Domain 예외로 변환
- 포함 요소:
  - client/ - Feign Client 인터페이스
  - adapter/ - Port 구현체
  - dto/ - 외부 API DTO
- 예시:
  - PaymentFeignClient.java - 결제 서비스 Feign Client
  - PaymentAdapter.java - PaymentPort 구현
  - PaymentApiRequest.java - 외부 API 요청 DTO

## infrastructure/messaging/
- 역할: Kafka 이벤트 처리
- 특징:
  - KafkaTemplate 사용
  - Event 발행 및 구독
  - 비동기 처리
- 포함 요소:
  - adapter/ - Event Publisher/Consumer
  - config/ - Kafka 설정
- 예시:
  - KafkaEventPublisher.java - 이벤트 발행
  - OrderEventConsumer.java - 이벤트 수신

## infrastructure/config/
- 역할: Infrastructure 공통 설정
- 특징:
  - JPA, QueryDSL 설정
  - 각종 Bean 설정
- 예시:
  - JpaConfig.java - JPA 설정
  - QueryDslConfig.java - QueryDSL 설정

## api/web/
- 역할: HTTP API 제공 (고객용)
- 특징:
  - @RestController 사용
  - Use Case만 호출
  - Request → Command 변환
  - Domain → Response 변환
  - 비즈니스 로직 금지
- 포함 요소:
  - order/ - 주문 Controller
  - dto/request/ - 요청 DTO (Bean Validation)
  - dto/response/ - 응답 DTO
  - config/ - Security, ExceptionHandler
- 예시:
  - OrderController.java - 주문 REST API
  - CreateOrderRequest.java - 주문 생성 요청 DTO
  - OrderResponse.java - 주문 응답 DTO

## api/grpc/
- 역할: gRPC API 제공 (고객용)
- 특징:
  - @GrpcService 사용
  - Protobuf 메시지 변환
  - Use Case 호출
- 포함 요소:
  - order/ - 주문 gRPC Service
  - mapper/ - Protobuf ↔ Domain 변환
- 예시:
  - OrderGrpcService.java - gRPC Service 구현 
  - OrderGrpcMapper.java - Protobuf 변환

## admin/web/
- 역할: 관리자용 API
- 특징:
  - 관리자 전용 기능
  - 통계, 대량 작업, 강제 처리
  - Admin Service 사용
- 포함 요소:
  - order/ - 주문 관리 Controller
  - dto/ - 관리자용 DTO
  - config/ - Admin Security 설정
- 예시:
  - OrderAdminController.java - 주문 관리 API
  - OrderStatisticsController.java - 통계 API
  - OrderAdminResponse.java - 관리자용 응답 DTO

## worker/job/
- 역할: 배치 작업
- 특징:
  - Spring Batch 사용
  - 스케줄링 (@Scheduled)
  - 정리, 통계 계산 등
- 포함 요소:
  - order/ - 주문 배치 Job
  - config/ - Batch 설정
- 예시:
  - OrderCleanupJob.java - 만료 주문 정리
  - OrderStatisticsJob.java - 통계 계산
  - BatchConfig.java - Batch 설정

## 핵심 원칙

### core/ (Domain & Application)
- **Domain Layer:**
  - 순수 Java만 사용
  - JPA, Spring 어노테이션 금지
  - Infrastructure 의존 금지
  - 비즈니스 규칙과 로직만 포함
  - **모든 외부 의존성 Port는 required/에 정의**
  - RuntimeException 사용 (CheckedException 금지)

- **Application Layer:**
  - UseCase별 파일 분리 (1 UseCase = 1 execute())
  - UseCase는 구체 클래스 (In Port 없음)
  - Domain required/의 Port 인터페이스만 의존
  - 트랜잭션 관리:
    - 간단한 경우: `@Transactional`
    - 복잡한 경우: `TransactionTemplate`
  - Domain 로직 호출만 (직접 구현 금지)

### infrastructure/
- JpaEntity with from/toDomain 메서드
- Adapter 패턴으로 Port 구현
- Domain ↔ JpaEntity 변환 책임
- 외부 시스템 연동 구현
- 기술 관심사만 포함

### api/, admin/, worker/
- UseCase만 호출
- Request ↔ Command/Query 변환
- Domain ↔ Response 변환
- 비즈니스 로직 금지
- 트랜잭션 관리 금지 (UseCase에 위임)