# 코드 구조
```
order-service/
│
├── core/                                    # 비즈니스 로직 모듈
│   └── src/main/java/com/company/order/
│       ├── domain/                          # Domain Layer
│       │   ├── order/
│       │   │   ├── model/
│       │   │   │   ├── Order.java
│       │   │   │   ├── OrderItem.java
│       │   │   │   ├── OrderStatus.java
│       │   │   │   ├── Money.java
│       │   │   │   ├── Address.java
│       │   │   │   └── Weight.java
│       │   │   ├── repository/
│       │   │   │   └── OrderRepository.java
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
│               ├── support/
│               │   ├── OrderRetriever.java
│               │   ├── OrderPreparationService.java
│               │   ├── OrderCompletionService.java
│               │   └── OrderCancellationService.java
│               │
│               ├── port/
│               │   └── out/
│               │       ├── OrderQueryPort.java
│               │       ├── PaymentPort.java
│               │       ├── InventoryPort.java
│               │       └── EventPublishPort.java
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
│       │   │   │   │   └── AddressEmbed.java
│       │   │   │   ├── adapter/
│       │   │   │   │   ├── OrderRepositoryAdapter.java
│       │   │   │   │   └── OrderJpaRepository.java
│       │   │   │   └── query/
│       │   │   │       └── OrderQueryAdapter.java
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
│       │   │   ├── InventoryFeignClient.java
│       │   │   └── DeliveryFeignClient.java
│       │   ├── adapter/
│       │   │   ├── PaymentAdapter.java
│       │   │   ├── InventoryAdapter.java
│       │   │   └── DeliveryAdapter.java
│       │   └── dto/
│       │       ├── PaymentApiRequest.java
│       │       └── PaymentApiResponse.java
│       │
│       ├── messaging/
│       │   ├── adapter/
│       │   │   ├── KafkaEventPublisher.java
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
- 역할: 순수 비즈니스 로직
- 특징:
  - JPA, Spring 어노테이션 없음
  - 순수 Java + Lombok만 사용
  - 비즈니스 규칙과 로직 포함
  - 자기 검증 구현
- 포함 요소:
  - model/ - Entity, Value Object
  - repository/ - Repository 인터페이스
  - service/ - Domain Service (순수 계산)
  - event/ - Domain Event
  - exception/ - Domain 예외
  - policy/ - 비즈니스 정책
- 예시:
  - Order.java - 주문 Aggregate Root
  - OrderPricingService.java - 가격 계산 Domain Service
  - Money.java - 금액 Value Object

## core/application/
- 역할: Use Case 실행 및 흐름 조정
- 특징:
  - UseCase별 파일 분리 (하나의 파일 = 하나의 execute())
  - Port(인터페이스)만 의존
  - 트랜잭션은 Support Service에서 관리
  - Domain과 Infrastructure 연결
- 포함 요소:
  - usecase/command/ - 명령 Use Case
  - usecase/query/ - 조회 Use Case
  - support/ - Application 지원 서비스 (트랜잭션 관리)
  - port/out/ - 외부 의존성 인터페이스
  - dto/ - Application 계층 DTO
  - admin/ - 관리자용 서비스
- 예시:
  - CreateOrderUseCase.java - 주문 생성 Use Case
  - OrderPreparationService.java - 주문 준비 Support Service
  - OrderQueryPort.java - 조회 Port 인터페이스

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
- core/:
  - 순수 Java만 사용
  - JPA, Spring 어노테이션 금지
  - Infrastructure 의존 금지
- infrastructure/:
  - JpaEntity with from/toDomain 메서드
  - Adapter 패턴 사용
  - Port 인터페이스 구현
- api/, admin/, worker/:
  - Use Case만 호출
  - DTO 변환
  - 비즈니스 로직 금지