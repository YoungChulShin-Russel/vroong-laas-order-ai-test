# Infrastructure Layer 규칙

## 위치
`infrastructure/`

## 책임
- 기술 구현 (JPA, Kafka, Feign, Redis)
- Domain Port 인터페이스 구현 (Store/Reader)
- 외부 시스템 연동

## 저장소 구조

**infrastructure/**
- **storage/** - 저장소
  - **db/** - 데이터베이스 (JPA)
    - order/
      - entity/
      - adapter/
    - payment/
    - coupon/
  - **cache/** - 캐시 (Redis)
    - order/
    - config/
- **external/** - 외부 API
  - client/
  - adapter/
- **messaging/** - 메시징 (Kafka)
  - adapter/
  - config/
- **config/** - 공통 설정

## JPA Entity 규칙

### 반드시 해야 할 것
1. @Entity, @Table 등 JPA 어노테이션 사용
2. from(Domain) 정적 메서드로 Domain → Entity 변환
3. toDomain() 인스턴스 메서드로 Entity → Domain 변환
4. @NoArgsConstructor(access = AccessLevel.PROTECTED)
5. Getter만 사용 (Setter 금지)

### 절대 하지 말 것
1. Entity에 비즈니스 로직 작성 금지
2. Entity가 Domain을 상속받는 것 금지
3. EAGER fetch 사용 금지
4. 양방향 연관관계 최소화

### JPA Entity 템플릿

```java
// infrastructure/storage/db/order/entity/OrderJpaEntity.java
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, unique = true)
    private String orderNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    @Embedded
    private AddressEmbed deliveryAddress;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItemJpaEntity> items = new ArrayList<>();
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Domain → Entity 변환
    public static OrderJpaEntity from(Order domain) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.id = domain.getId();
        entity.userId = domain.getUserId();
        entity.orderNumber = domain.getOrderNumber();
        entity.status = domain.getStatus();
        entity.totalAmount = domain.getTotalAmount().value();
        entity.deliveryAddress = AddressEmbed.from(domain.getDeliveryAddress());
        entity.items = domain.getItems().stream()
            .map(OrderItemJpaEntity::from)
            .collect(Collectors.toList());
        entity.createdAt = domain.getCreatedAt();
        return entity;
    }
    
    // Entity → Domain 변환
    public Order toDomain() {
        return Order.builder()
            .id(this.id)
            .userId(this.userId)
            .orderNumber(this.orderNumber)
            .status(this.status)
            .totalAmount(Money.of(this.totalAmount))
            .deliveryAddress(this.deliveryAddress.toDomain())
            .items(this.items.stream()
                .map(OrderItemJpaEntity::toDomain)
                .collect(Collectors.toList()))
            .createdAt(this.createdAt)
            .build();
    }
}
```

```java
// infrastructure/storage/db/order/entity/OrderItemJpaEntity.java
@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private BigDecimal unitPrice;
    
    public static OrderItemJpaEntity from(OrderItem domain) {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.id = domain.getId();
        entity.productId = domain.getProductId();
        entity.productName = domain.getProductName();
        entity.quantity = domain.getQuantity();
        entity.unitPrice = domain.getUnitPrice().value();
        return entity;
    }
    
    public OrderItem toDomain() {
        return OrderItem.builder()
            .id(this.id)
            .productId(this.productId)
            .productName(this.productName)
            .quantity(this.quantity)
            .unitPrice(Money.of(this.unitPrice))
            .build();
    }
}
```

```java
// infrastructure/storage/db/order/entity/AddressEmbed.java
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AddressEmbed {
    
    @Column(name = "zip_code", nullable = false)
    private String zipCode;
    
    @Column(name = "street", nullable = false)
    private String street;
    
    @Column(name = "detail")
    private String detail;
    
    public static AddressEmbed from(Address domain) {
        return new AddressEmbed(
            domain.zipCode(),
            domain.street(),
            domain.detail()
        );
    }
    
    public Address toDomain() {
        return new Address(zipCode, street, detail);
    }
}
```

## Store Adapter 규칙

### 위치
`infrastructure/storage/db/*/adapter/`

### 책임
- OrderStore 인터페이스 구현
- JpaRepository 사용
- Domain ↔ Entity 변환
- **@Transactional 관리 (트랜잭션 경계)**

### Store Adapter 템플릿

```java
// infrastructure/storage/db/order/adapter/OrderStoreAdapter.java
@Repository
@RequiredArgsConstructor
public class OrderStoreAdapter implements OrderStore {
    
    private final OrderJpaRepository jpaRepository;
    
    @Transactional  // ⭐ 여기서 트랜잭션 관리
    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = OrderJpaEntity.from(order);
        OrderJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }
    
    @Transactional(readOnly = true)  // ⭐ 읽기 전용 트랜잭션
    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id)
            .map(OrderJpaEntity::toDomain);
    }
    
    @Transactional(readOnly = true)
    @Override
    public Optional<Order> findByIdWithItems(Long id) {
        return jpaRepository.findByIdWithItems(id)
            .map(OrderJpaEntity::toDomain);
    }
    
    @Transactional
    @Override
    public void delete(Order order) {
        jpaRepository.deleteById(order.getId());
    }
}
```

**특징:**
- ✅ 메서드 단위로 짧은 트랜잭션
- ✅ UseCase는 트랜잭션 관리 불필요
- ✅ DB 커넥션 최소 점유
- ⚠️ 여러 Aggregate 수정 시 UseCase에서 TransactionTemplate 사용

```java
// infrastructure/storage/db/order/adapter/OrderJpaRepository.java
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {
    
    @Query("SELECT o FROM OrderJpaEntity o JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderJpaEntity> findByIdWithItems(@Param("id") Long id);
    
    Optional<OrderJpaEntity> findByOrderNumber(String orderNumber);
}
```

## Reader Adapter 규칙

### 위치
`infrastructure/storage/db/*/adapter/`

### 책임
- OrderReader 인터페이스 구현
- QueryDSL로 동적 쿼리 작성
- 복잡한 조회 쿼리 처리

### Reader Adapter 템플릿

```java
// infrastructure/storage/db/order/adapter/OrderReaderAdapter.java
@Repository
@RequiredArgsConstructor
public class OrderReaderAdapter implements OrderReader {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public Page<Order> findByUserId(Long userId, Pageable pageable) {
        QOrderJpaEntity order = QOrderJpaEntity.orderJpaEntity;
        
        List<OrderJpaEntity> results = queryFactory
            .selectFrom(order)
            .where(order.userId.eq(userId))
            .orderBy(order.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
        
        long total = queryFactory
            .select(order.count())
            .from(order)
            .where(order.userId.eq(userId))
            .fetchOne();
        
        List<Order> orders = results.stream()
            .map(OrderJpaEntity::toDomain)
            .collect(Collectors.toList());
        
        return new PageImpl<>(orders, pageable, total);
    }
    
    @Override
    public Page<Order> searchOrders(
        OrderSearchCondition condition,
        Pageable pageable
    ) {
        QOrderJpaEntity order = QOrderJpaEntity.orderJpaEntity;
        
        List<OrderJpaEntity> results = queryFactory
            .selectFrom(order)
            .where(
                userIdEq(condition.getUserId()),
                statusEq(condition.getStatus()),
                orderNumberContains(condition.getOrderNumber()),
                createdAtBetween(condition.getStartDate(), condition.getEndDate())
            )
            .orderBy(order.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
        
        long total = queryFactory
            .select(order.count())
            .from(order)
            .where(
                userIdEq(condition.getUserId()),
                statusEq(condition.getStatus()),
                orderNumberContains(condition.getOrderNumber()),
                createdAtBetween(condition.getStartDate(), condition.getEndDate())
            )
            .fetchOne();
        
        List<Order> orders = results.stream()
            .map(OrderJpaEntity::toDomain)
            .collect(Collectors.toList());
        
        return new PageImpl<>(orders, pageable, total);
    }
    
    @Override
    public boolean existsByOrderNumber(String orderNumber) {
        QOrderJpaEntity order = QOrderJpaEntity.orderJpaEntity;
        
        Integer count = queryFactory
            .selectOne()
            .from(order)
            .where(order.orderNumber.eq(orderNumber))
            .fetchFirst();
        
        return count != null;
    }
    
    // 동적 쿼리 조건
    private BooleanExpression userIdEq(Long userId) {
        return userId != null ? order.userId.eq(userId) : null;
    }
    
    private BooleanExpression statusEq(OrderStatus status) {
        return status != null ? order.status.eq(status) : null;
    }
    
    private BooleanExpression orderNumberContains(String orderNumber) {
        return orderNumber != null ? order.orderNumber.contains(orderNumber) : null;
    }
    
    private BooleanExpression createdAtBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return null;
        }
        return order.createdAt.between(
            start.atStartOfDay(),
            end.atTime(23, 59, 59)
        );
    }
}
```

## 외부 API Adapter 규칙

### 위치
`infrastructure/external/`

### 책임
- 외부 시스템 연동
- Application Port 구현
- Feign Client 사용

### 외부 API Adapter 템플릿

```java
// infrastructure/external/client/PaymentFeignClient.java
@FeignClient(name = "payment-service", url = "${payment.service.url}")
public interface PaymentFeignClient {
    
    @PostMapping("/api/v1/payments")
    PaymentApiResponse processPayment(@RequestBody PaymentApiRequest request);
    
    @PostMapping("/api/v1/payments/{paymentId}/cancel")
    void cancelPayment(@PathVariable Long paymentId);
    
    @PostMapping("/api/v1/payments/{paymentId}/refund")
    void refundPayment(
        @PathVariable Long paymentId,
        @RequestBody RefundApiRequest request
    );
}
```

```java
// infrastructure/external/adapter/PaymentAdapter.java
@Component
@RequiredArgsConstructor
public class PaymentAdapter implements PaymentPort {
    
    private final PaymentFeignClient paymentClient;
    
    @Override
    public Payment processPayment(Long orderId, Money amount, PaymentMethod method) {
        PaymentApiRequest request = PaymentApiRequest.builder()
            .orderId(orderId)
            .amount(amount.value())
            .method(method.name())
            .build();
        
        try {
            PaymentApiResponse response = paymentClient.processPayment(request);
            
            return Payment.builder()
                .id(response.paymentId())
                .orderId(orderId)
                .amount(Money.of(response.amount()))
                .method(method)
                .status(PaymentStatus.valueOf(response.status()))
                .build();
                
        } catch (FeignException e) {
            throw new PaymentProcessingException("결제 처리 실패", e);
        }
    }
    
    @Override
    public void cancelPayment(Long paymentId) {
        try {
            paymentClient.cancelPayment(paymentId);
        } catch (FeignException e) {
            throw new PaymentCancellationException("결제 취소 실패", e);
        }
    }
}
```

## Kafka Event Publisher 규칙

### 위치
`infrastructure/messaging/adapter/`

### Kafka Event Publisher 템플릿

```java
// infrastructure/messaging/adapter/KafkaEventPublisher.java
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Override
    public void publish(DomainEvent event) {
        String topic = resolveTopicName(event);
        String key = event.getAggregateId().toString();
        
        kafkaTemplate.send(topic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event: {}", event, ex);
                } else {
                    log.info("Event published successfully: {}", event);
                }
            });
    }
    
    private String resolveTopicName(DomainEvent event) {
        return switch (event) {
            case OrderCreatedEvent e -> "order.created";
            case OrderCancelledEvent e -> "order.cancelled";
            case OrderStatusChangedEvent e -> "order.status-changed";
            default -> throw new IllegalArgumentException("Unknown event type");
        };
    }
}
```

```java
// infrastructure/messaging/adapter/OrderEventConsumer.java
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {
    
    private final OrderStore orderStore;
    
    @KafkaListener(topics = "inventory.reserved", groupId = "order-service")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        Order order = orderStore.findById(event.orderId())
            .orElseThrow();
        
        order.confirmInventoryReserved();
        orderStore.save(order);
    }
    
    @KafkaListener(topics = "payment.completed", groupId = "order-service")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        Order order = orderStore.findById(event.orderId())
            .orElseThrow();
        
        order.completePayment(event.paymentId());
        orderStore.save(order);
    }
}
```

## Cache Adapter 규칙

### 위치
`infrastructure/storage/cache/`

### Cache Adapter 템플릿

```java
// infrastructure/storage/cache/order/OrderCacheAdapter.java
@Component
@RequiredArgsConstructor
public class OrderCacheAdapter {
    
    private final RedisTemplate<String, OrderCacheDto> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "order:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    
    public Optional<Order> findById(Long orderId) {
        String key = CACHE_KEY_PREFIX + orderId;
        OrderCacheDto cached = redisTemplate.opsForValue().get(key);
        
        return Optional.ofNullable(cached)
            .map(OrderCacheDto::toDomain);
    }
    
    public void save(Order order) {
        String key = CACHE_KEY_PREFIX + order.getId();
        OrderCacheDto dto = OrderCacheDto.from(order);
        redisTemplate.opsForValue().set(key, dto, CACHE_TTL);
    }
    
    public void evict(Long orderId) {
        String key = CACHE_KEY_PREFIX + orderId;
        redisTemplate.delete(key);
    }
}
```

## 설정 파일

### JPA Config

```java
// infrastructure/config/JpaConfig.java
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "vroong.laas.order.infrastructure.storage.db")
public class JpaConfig {
}
```

### QueryDSL Config

```java
// infrastructure/config/QueryDslConfig.java
@Configuration
public class QueryDslConfig {
    
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
```

### Feign Config

```java
// infrastructure/config/FeignConfig.java
@Configuration
public class FeignConfig {
    
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}
```

## 절대 금지 사항
1. Entity에 비즈니스 로직 작성 금지
2. EAGER fetch 사용 금지
3. Entity가 Domain을 상속하는 것 금지
4. Adapter에서 직접 Domain 로직 실행 금지
5. 양방향 연관관계 남발 금지

## 중요 원칙
1. Entity는 단순 데이터 구조체
2. from/toDomain으로 명확한 변환
3. Adapter는 인터페이스 구현만
4. QueryDSL로 동적 쿼리
5. 외부 API 오류는 Domain 예외로 변환
</artifact>

---