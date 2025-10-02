# Application Layer 규칙

## 위치
`core/application/`

## 책임
- Use Case 실행 및 흐름 조정
- 트랜잭션 관리
- Domain과 Infrastructure 연결

## Use Case 마커 인터페이스

```java
// core/application/UseCase.java
/**
 * Use Case 마커 인터페이스
 * 
 * 규칙:
 * 1. 반드시 execute() 메서드를 구현해야 함
 * 2. execute()는 COMMAND 타입을 파라미터로 받음
 * 3. 반환 타입은 Use Case의 필요에 따라 자유롭게 지정
 * 4. 하나의 UseCase = 하나의 파일 = 하나의 execute() 메서드
 */
public interface UseCase<COMMAND> {
    // 마커 인터페이스 (메서드 없음)
}
```

## Use Case 구현 규칙

### 반드시 해야 할 것
1. UseCase<COMMAND> 인터페이스 구현
2. 메서드명은 무조건 execute()
3. 파라미터는 COMMAND 타입 하나만
4. 반환 타입은 자유롭게 (Order, void, Page 등)
5. @UseCase (또는 @Service) 어노테이션
6. 하나의 UseCase = 하나의 파일
7. 하나의 파일 = 하나의 execute() 메서드만
8. 50줄 이내로 작성
9. Port(인터페이스)만 의존

### 절대 하지 말 것
1. execute() 외 다른 메서드명 사용 금지
2. 파라미터가 여러 개인 경우 금지
3. Infrastructure 직접 의존 금지
4. 비즈니스 로직 포함 금지
5. 하나의 파일에 여러 UseCase 메서드 금지

## 트랜잭션 관리 규칙

### 원칙 1: 기본 - Repository Adapter에 @Transactional

**단일 Aggregate만 수정하는 대부분의 경우**

```java
// Infrastructure - Repository Adapter
@Repository
@RequiredArgsConstructor
public class OrderStoreAdapter implements OrderStore {
    
    private final OrderJpaRepository jpaRepository;
    
    @Transactional  // ⭐ Repository Adapter에 트랜잭션
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
public class CancelOrderUseCase implements UseCase<CancelOrderCommand> {
    
    private final OrderStore orderStore;
    private final EventPublisher eventPublisher;
    
    public void execute(CancelOrderCommand command) {
        // Repository에서 트랜잭션 처리됨
        Order order = orderStore.findById(command.orderId()).orElseThrow();
        
        order.cancel(command.reason());
        
        orderStore.save(order);  // 여기서 짧은 트랜잭션
        
        // 이벤트는 트랜잭션 밖에서 발행
        eventPublisher.publish(OrderCancelledEvent.from(order));
    }
}
```

**특징:**
- ✅ 짧은 트랜잭션 (메서드 단위)
- ✅ DB 커넥션 최소 점유
- ✅ UseCase 코드 단순
- ⚠️ 낙관적 락(@Version) 필수

---

### 원칙 2: 여러 Aggregate 수정 - TransactionTemplate

**여러 Aggregate를 하나의 트랜잭션으로 묶어야 하는 경우**

```java
@UseCase
@RequiredArgsConstructor
public class CreateOrderUseCase implements UseCase<CreateOrderCommand> {
    
    private final TransactionTemplate transactionTemplate;  // ⭐ 여러 Aggregate 수정용
    private final OrderStore orderStore;
    private final CustomerStore customerStore;
    private final CouponStore couponStore;
    
    public Order execute(CreateOrderCommand command) {
        
        // ===== 트랜잭션: 여러 Aggregate를 하나로 묶음 =====
        Order order = transactionTemplate.execute(status -> {
            
            // 1. Customer 조회 및 검증
            Customer customer = customerStore.findById(command.userId())
                .orElseThrow(() -> new CustomerNotFoundException());
            
            if (!customer.isActive()) {
                throw new InactiveCustomerException();
            }
            
            // 2. Coupon 사용 (다른 Aggregate)
            Coupon coupon = null;
            if (command.couponId() != null) {
                coupon = couponStore.findById(command.couponId())
                    .orElseThrow(() -> new CouponNotFoundException());
                coupon.use();
                couponStore.save(coupon);  // Coupon Aggregate 수정
            }
            
            // 3. Order 생성 (또 다른 Aggregate)
            Order o = Order.create(
                customer.getId(),
                command.items(),
                command.deliveryAddress(),
                coupon
            );
            
            // 4. 모두 한 트랜잭션으로 커밋
            return orderStore.save(o);  // Order Aggregate 수정
        });
        
        return order;
    }
}
```

**사용 시기:**
- ✅ 여러 Aggregate를 동시에 수정 (Order + Coupon)
- ✅ 원자성이 필요한 경우
- ✅ 일관성이 중요한 비즈니스 로직

**주의사항:**
- ⚠️ 트랜잭션 안에 외부 API 호출 금지
- ⚠️ 트랜잭션을 가능한 짧게 유지

---

### 원칙 3: 외부 API 호출 사이 - TransactionTemplate 분리

**외부 API 호출이 트랜잭션 사이에 끼어있는 경우**

```java
@UseCase
@RequiredArgsConstructor
public class ProcessOrderPaymentUseCase implements UseCase<ProcessPaymentCommand> {
    
    private final TransactionTemplate transactionTemplate;
    private final OrderStore orderStore;
    private final PaymentPort paymentPort;  // 외부 API
    
    public void execute(ProcessPaymentCommand command) {
        
        // ===== 트랜잭션 1: 주문 조회 및 검증 =====
        Order order = transactionTemplate.execute(status -> {
            Order o = orderStore.findById(command.orderId()).orElseThrow();
            
            if (!o.isPayable()) {
                throw new OrderNotPayableException();
            }
            
            return o;
        });
        
        // ===== 외부 API (트랜잭션 밖) =====
        PaymentResult payment = paymentPort.process(order.getId(), order.getTotalAmount());
        
        // ===== 트랜잭션 2: 결제 완료 처리 =====
        transactionTemplate.executeWithoutResult(status -> {
            Order o = orderStore.findById(order.getId()).orElseThrow();
            o.completePayment(payment);
            orderStore.save(o);
        });
    }
}
```

**사용 시기:**
- ✅ 외부 API 호출이 DB 작업 사이에 있는 경우
- ✅ 외부 API 응답을 받아서 DB에 반영해야 하는 경우

**트랜잭션 흐름:**
```
TX1: 주문 조회/검증 (0.01초) → 커밋
  ↓
외부 API: 결제 처리 (3초) - DB 커넥션 점유 안 함
  ↓
TX2: 주문 완료 처리 (0.01초) → 커밋

총 DB 커넥션 점유: 0.02초 (vs 3.02초)
```

## Use Case 템플릿

### Command UseCase (반환값 있음)

```java
// core/application/order/usecase/command/CreateOrderUseCase.java
@UseCase
@RequiredArgsConstructor
public class CreateOrderUseCase implements UseCase<CreateOrderCommand> {
    
    private final OrderStore orderStore;
    private final OrderReader orderReader;
    
    @Transactional
    public Order execute(CreateOrderCommand command) {
        
        // Reader로 중복 체크
        boolean exists = orderReader.existsByOrderNumber(command.orderNumber());
        if (exists) {
            throw new DuplicateOrderNumberException();
        }
        
        // Order 생성
        Order order = Order.create(
            command.userId(),
            command.items(),
            command.deliveryAddress(),
            command.totalAmount()
        );
        
        // Store로 저장
        return orderStore.save(order);
    }
}
```

### Command UseCase (반환값 없음)

```java
// core/application/order/usecase/command/CancelOrderUseCase.java
@UseCase
@RequiredArgsConstructor
public class CancelOrderUseCase implements UseCase<CancelOrderCommand> {
    
    private final OrderStore orderStore;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public void execute(CancelOrderCommand command) {
        Order order = orderStore.findById(command.orderId())
            .orElseThrow();
        
        // 권한 체크
        if (!order.isOwnedBy(command.userId())) {
            throw new OrderAccessDeniedException();
        }
        
        order.cancel(command.reason());
        
        orderStore.save(order);
        
        eventPublisher.publish(OrderCancelledEvent.from(order));
    }
}
```

### Query UseCase

```java
// core/application/order/usecase/query/GetMyOrdersUseCase.java
@UseCase
@RequiredArgsConstructor
public class GetMyOrdersUseCase implements UseCase<GetMyOrdersQuery> {
    
    private final OrderReader orderReader;
    
    @Transactional(readOnly = true)
    public Page<OrderSummary> execute(GetMyOrdersQuery query) {
        Page<Order> orders = orderReader.findByUserId(
            query.userId(),
            query.pageable()
        );
        
        return orders.map(OrderSummary::from);
    }
}
```

## Application Support 규칙 (선택 사항)

### 위치
`core/application/*/support/`

**중요: domain/service/가 아님!**

### 책임
- UseCase의 반복되는 조회/검증 로직 제거
- **비즈니스 로직 포함 금지**
- **트랜잭션 관리는 UseCase에서**

### 반드시 해야 할 것
1. 조회 및 검증만 수행
2. Store/Reader 의존 가능
3. 간단한 헬퍼 메서드만

### 절대 하지 말 것
1. **@Transactional 사용 금지** (UseCase에서 관리)
2. **비즈니스 로직 작성 금지**
3. **복잡한 흐름 제어 금지**

### Application Support 템플릿

#### Retriever (조회 헬퍼) - 권장

```java
// core/application/order/support/OrderRetriever.java
@Component
@RequiredArgsConstructor
public class OrderRetriever {
    
    private final OrderStore orderStore;
    
    public Order getOrder(Long orderId) {
        return orderStore.findByIdWithItems(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
    
    public Order getOrderWithOwnershipCheck(Long orderId, Long userId) {
        Order order = getOrder(orderId);
        
        if (!order.isOwnedBy(userId)) {
            throw new OrderAccessDeniedException(
                "해당 주문에 접근할 권한이 없습니다"
            );
        }
        
        return order;
    }
}
```

**사용 예시:**
```java
@UseCase
@RequiredArgsConstructor
public class CancelOrderUseCase {
    
    private final OrderRetriever orderRetriever;  // ✅ 조회 헬퍼
    private final OrderStore orderStore;
    
    @Transactional  // ✅ UseCase에서 트랜잭션
    public void execute(CancelOrderCommand command) {
        // Retriever로 조회 및 권한 체크
        Order order = orderRetriever.getOrderWithOwnershipCheck(
            command.orderId(),
            command.userId()
        );
        
        // 비즈니스 로직
        order.cancel(command.reason());
        
        // 저장
        orderStore.save(order);
    }
}
```

## Command/Query 설계

### Command 기본 템플릿

```java
// core/application/order/usecase/command/CreateOrderCommand.java
@Builder
public record CreateOrderCommand(
    Long userId,
    List<OrderItemCommand> items,
    String deliveryAddress,
    Long couponId
) {
    // 생성 시점 검증
    public CreateOrderCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다");
        }
        
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("주문 상품이 없습니다");
        }
        
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("배송지가 없습니다");
        }
        
        // 중복 상품 체크
        long distinctCount = items.stream()
            .map(OrderItemCommand::productId)
            .distinct()
            .count();
        
        if (distinctCount != items.size()) {
            throw new IllegalArgumentException("중복된 상품이 있습니다");
        }
    }
    
    public record OrderItemCommand(
        Long productId,
        int quantity
    ) {
        public OrderItemCommand {
            if (productId == null || productId <= 0) {
                throw new IllegalArgumentException("유효하지 않은 상품 ID입니다");
            }
            if (quantity < 1 || quantity > 100) {
                throw new IllegalArgumentException("수량은 1~100 사이여야 합니다");
            }
        }
    }
}
```

### 상황별 입력값이 다른 경우

#### Command에 Optional 필드 + 타입 구분

```java
// core/application/order/usecase/command/CancelOrderCommand.java
@Builder
public record CancelOrderCommand(
    // 공통 필드
    Long orderId,
    String reason,
    
    // 타입 구분
    CancelType cancelType,  // CUSTOMER, ADMIN, SYSTEM
    
    // Optional 필드 (타입별로 필요)
    Long userId,        // CUSTOMER만
    String adminMemo,   // ADMIN만
    Long adminId,       // ADMIN만
    String systemCode   // SYSTEM만
) {
    // 타입별 검증
    public CancelOrderCommand {
        if (cancelType == null) {
            throw new IllegalArgumentException("취소 타입은 필수입니다");
        }
        
        switch (cancelType) {
            case CUSTOMER -> {
                if (userId == null) {
                    throw new IllegalArgumentException("고객 ID는 필수입니다");
                }
            }
            case ADMIN -> {
                if (adminId == null || adminMemo == null) {
                    throw new IllegalArgumentException("관리자 정보는 필수입니다");
                }
            }
            case SYSTEM -> {
                if (systemCode == null) {
                    throw new IllegalArgumentException("시스템 코드는 필수입니다");
                }
            }
        }
    }
}
```

#### UseCase에서 직접 처리

```java
@UseCase
@RequiredArgsConstructor
public class CancelOrderUseCase implements UseCase<CancelOrderCommand> {
    
    private final OrderRetriever orderRetriever;
    private final OrderStore orderStore;
    private final PaymentPort paymentPort;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public void execute(CancelOrderCommand command) {
        
        // 1. 타입별 조회
        Order order = retrieveOrder(command);
        
        // 2. 타입별 취소 처리
        processCancellation(order, command);
        
        // 3. 저장 및 이벤트
        orderStore.save(order);
        eventPublisher.publish(OrderCancelledEvent.from(order, command.cancelType()));
    }
    
    private Order retrieveOrder(CancelOrderCommand command) {
        return switch (command.cancelType()) {
            case CUSTOMER -> orderRetriever.getOrderWithOwnershipCheck(
                command.orderId(),
                command.userId()
            );
            case ADMIN, SYSTEM -> orderRetriever.getOrder(command.orderId());
        };
    }
    
    private void processCancellation(Order order, CancelOrderCommand command) {
        switch (command.cancelType()) {
            case CUSTOMER -> order.cancel(command.reason());
            case ADMIN -> {
                order.cancelByAdmin(command.reason(), command.adminMemo());
                if (order.isPaid()) {
                    paymentPort.refund(order.getPaymentId());
                }
            }
            case SYSTEM -> order.cancelBySystem(command.systemCode(), command.reason());
        }
    }
}
```

## Port 규칙

### 위치
`core/application/*/port/out/`

### Store vs Reader 구분

#### OrderStore (쓰기)

```java
// core/application/order/port/out/OrderStore.java
public interface OrderStore {
    Order save(Order order);
    Optional<Order> findById(Long id);
    Optional<Order> findByIdWithItems(Long id);  // Aggregate 로드
    void delete(Order order);
}
```

#### OrderReader (읽기)

```java
// core/application/order/port/out/OrderReader.java
public interface OrderReader {
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> searchOrders(OrderSearchCondition condition, Pageable pageable);
    OrderStatistics calculateStatistics(LocalDate start, LocalDate end);
    boolean existsByOrderNumber(String orderNumber);
}
```

### 규칙
- UseCase는 Store/Reader(인터페이스)에 의존
- Infrastructure에서 Adapter로 구현
- 쓰기는 Store, 읽기는 Reader로 명확히 구분

## 중요 원칙

### 1. UseCase 설계
- 하나의 UseCase = 하나의 파일 = 하나의 execute()
- UseCase는 Port(인터페이스)만 의존
- 비즈니스 로직은 Domain에, UseCase는 흐름 제어만

### 2. 트랜잭션 전략
**기본: Repository Adapter에 @Transactional**
- 단일 Aggregate 수정
- UseCase는 트랜잭션 없음
- 짧은 트랜잭션

**여러 Aggregate 수정: TransactionTemplate**
- 여러 Aggregate를 하나의 트랜잭션으로
- UseCase에서 명시적 제어

**외부 API 사이: TransactionTemplate 분리**
- 트랜잭션을 여러 개로 쪼개기
- DB 커넥션 최소 점유

### 3. Store/Reader 구분
- Store: 쓰기 작업 (save, delete)
- Reader: 읽기 작업 (find, search, exists)

### 4. Support Service
- 조회/검증 헬퍼만
- 트랜잭션 관리 금지
- 비즈니스 로직 금지
</artifact>

---