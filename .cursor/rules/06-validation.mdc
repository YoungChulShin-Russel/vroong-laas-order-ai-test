# Validation 규칙

## 검증 레이어별 책임

### 전체 흐름

```
Controller (형식 검증)
    ↓
Command (기본 정합성 검증)
    ↓
Application Support (비즈니스 규칙 사전 검증)
    ↓
Domain Service (복잡한 비즈니스 규칙 검증)
    ↓
Domain Entity (핵심 불변식 검증)
```

## 1. Controller - 형식 검증

### 위치
`api/web/*/dto/request/`

### 책임
- HTTP 요청의 형식 검증
- Bean Validation 사용
- null, 빈 값, 타입, 길이, 범위 등

### 검증 내용
- ✅ null 체크
- ✅ 빈 문자열 체크
- ✅ 길이 제한
- ✅ 숫자 범위
- ✅ 이메일 형식
- ✅ 정규식 패턴

### 예시

```java
// api/web/order/dto/request/CreateOrderRequest.java
public record CreateOrderRequest(
    @NotNull(message = "상품 목록은 필수입니다")
    @Size(min = 1, max = 100, message = "상품은 1~100개까지 주문 가능합니다")
    List<OrderItemRequest> items,
    
    @NotBlank(message = "배송지는 필수입니다")
    @Size(max = 500, message = "배송지는 500자 이내로 입력해주세요")
    String deliveryAddress,
    
    @Pattern(regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다")
    String phoneNumber,
    
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email,
    
    Long couponId
) {
    public record OrderItemRequest(
        @NotNull(message = "상품 ID는 필수입니다")
        @Positive(message = "상품 ID는 양수여야 합니다")
        Long productId,
        
        @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다")
        @Max(value = 100, message = "수량은 최대 100개까지 가능합니다")
        int quantity,
        
        @Positive(message = "가격은 양수여야 합니다")
        BigDecimal price
    ) {}
}
```

**검증 실패 시:**
```json
{
  "code": "VALIDATION_ERROR",
  "message": "입력값 검증 실패",
  "errors": {
    "items": "상품 목록은 필수입니다",
    "phoneNumber": "올바른 전화번호 형식이 아닙니다"
  }
}
```

---

## 2. Command - 기본 정합성 검증

### 위치
`core/application/*/usecase/command/`

### 책임
- 데이터 정합성 검증
- null 체크
- 빈 컬렉션 체크
- 중복 체크
- 논리적 모순 체크

### 검증 내용
- ✅ 필수 값 체크
- ✅ 빈 리스트/컬렉션 체크
- ✅ 중복 데이터 체크
- ✅ 상호 배타적 필드 체크
- ✅ 조건부 필수 필드 체크

### 예시

```java
// core/application/order/usecase/command/CreateOrderCommand.java
@Builder
public record CreateOrderCommand(
    Long userId,
    List<OrderItemCommand> items,
    String deliveryAddress,
    String phoneNumber,
    Long couponId
) {
    // Compact Constructor에서 검증
    public CreateOrderCommand {
        // 필수 값 체크
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다");
        }
        
        // 빈 컬렉션 체크
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("주문 상품이 없습니다");
        }
        
        // 필수 문자열 체크
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("배송지는 필수입니다");
        }
        
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("전화번호는 필수입니다");
        }
        
        // 중복 상품 체크
        long distinctCount = items.stream()
            .map(OrderItemCommand::productId)
            .distinct()
            .count();
        
        if (distinctCount != items.size()) {
            throw new IllegalArgumentException("중복된 상품이 있습니다");
        }
        
        // 각 OrderItem 검증
        items.forEach(item -> {
            if (item.productId() == null || item.productId() <= 0) {
                throw new IllegalArgumentException("유효하지 않은 상품 ID입니다");
            }
            if (item.quantity() < 1 || item.quantity() > 100) {
                throw new IllegalArgumentException("수량은 1~100 사이여야 합니다");
            }
        });
    }
    
    public record OrderItemCommand(
        Long productId,
        int quantity
    ) {}
}
```

```java
// core/application/order/usecase/command/CancelOrderCommand.java
@Builder
public record CancelOrderCommand(
    Long orderId,
    String reason,
    CancelType cancelType,
    
    // 조건부 필수 필드
    Long userId,
    Long adminId,
    String adminMemo,
    String systemCode
) {
    public CancelOrderCommand {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 주문 ID입니다");
        }
        
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("취소 사유는 필수입니다");
        }
        
        if (cancelType == null) {
            throw new IllegalArgumentException("취소 타입은 필수입니다");
        }
        
        // 타입별 필수 필드 검증
        switch (cancelType) {
            case CUSTOMER -> {
                if (userId == null) {
                    throw new IllegalArgumentException("고객 ID는 필수입니다");
                }
            }
            case ADMIN -> {
                if (adminId == null) {
                    throw new IllegalArgumentException("관리자 ID는 필수입니다");
                }
                if (adminMemo == null || adminMemo.isBlank()) {
                    throw new IllegalArgumentException("관리자 메모는 필수입니다");
                }
            }
            case SYSTEM -> {
                if (systemCode == null || systemCode.isBlank()) {
                    throw new IllegalArgumentException("시스템 코드는 필수입니다");
                }
            }
        }
    }
}
```

---

## 3. Application Support - 비즈니스 규칙 사전 검증

### 위치
`core/application/*/support/`

### 책임
- 저장소 조회를 통한 존재 여부 확인
- 상태 확인
- 권한 확인
- 기간 확인
- 유효성 확인

### 검증 내용
- ✅ 엔티티 존재 여부
- ✅ 엔티티 상태 확인 (활성화, 만료 등)
- ✅ 권한 확인
- ✅ 유효 기간 확인
- ✅ 사용 가능 여부 확인

### 예시

```java
// core/application/order/support/OrderPreparationHelper.java
@Component
@RequiredArgsConstructor
public class OrderPreparationHelper {
    
    private final CustomerStore customerStore;
    private final CouponReader couponReader;
    private final ProductReader productReader;
    private final OrderValidationService orderValidationService;
    
    public OrderPreparationResult prepare(CreateOrderCommand command) {
        
        // 1. Customer 존재 및 상태 확인
        Customer customer = customerStore.findById(command.userId())
            .orElseThrow(() -> new CustomerNotFoundException(
                "고객을 찾을 수 없습니다: " + command.userId()
            ));
        
        if (!customer.isActive()) {
            throw new InactiveCustomerException(
                "비활성화된 고객입니다"
            );
        }
        
        if (customer.isSuspended()) {
            throw new CustomerSuspendedException(
                "정지된 고객입니다"
            );
        }
        
        // 2. Product 존재 및 판매 가능 여부 확인
        List<Product> products = command.items().stream()
            .map(item -> {
                Product product = productReader.findById(item.productId())
                    .orElseThrow(() -> new ProductNotFoundException(
                        "상품을 찾을 수 없습니다: " + item.productId()
                    ));
                
                if (!product.isOnSale()) {
                    throw new ProductNotOnSaleException(
                        "판매 중이 아닌 상품입니다: " + product.getName()
                    );
                }
                
                if (product.getStock() < item.quantity()) {
                    throw new InsufficientStockException(
                        "재고가 부족합니다: " + product.getName()
                    );
                }
                
                return product;
            })
            .toList();
        
        // 3. Coupon 존재 및 유효성 확인
        Coupon coupon = null;
        if (command.couponId() != null) {
            coupon = couponReader.findById(command.couponId())
                .orElseThrow(() -> new CouponNotFoundException(
                    "쿠폰을 찾을 수 없습니다: " + command.couponId()
                ));
            
            if (coupon.isExpired()) {
                throw new CouponExpiredException(
                    "만료된 쿠폰입니다"
                );
            }
            
            if (coupon.isUsed()) {
                throw new CouponAlreadyUsedException(
                    "이미 사용된 쿠폰입니다"
                );
            }
            
            if (!coupon.isOwnedBy(customer.getId())) {
                throw new CouponOwnershipException(
                    "본인 소유의 쿠폰이 아닙니다"
                );
            }
        }
        
        // 4. Domain Service로 비즈니스 규칙 검증 위임
        orderValidationService.validateOrderCreation(
            command.items(),
            coupon,
            customer
        );
        
        return OrderPreparationResult.builder()
            .customer(customer)
            .products(products)
            .coupon(coupon)
            .items(command.items())
            .build();
    }
}
```

```java
// core/application/order/support/OrderRetriever.java
@Component
@RequiredArgsConstructor
public class OrderRetriever {
    
    private final OrderStore orderStore;
    
    public Order getOrder(Long orderId) {
        return orderStore.findByIdWithItems(orderId)
            .orElseThrow(() -> new OrderNotFoundException(
                "주문을 찾을 수 없습니다: " + orderId
            ));
    }
    
    public Order getOrderWithOwnershipCheck(Long orderId, Long userId) {
        Order order = getOrder(orderId);
        
        // 권한 확인
        if (!order.isOwnedBy(userId)) {
            throw new OrderAccessDeniedException(
                "해당 주문에 접근할 권한이 없습니다"
            );
        }
        
        return order;
    }
}
```

---

## 4. Domain Service - 복잡한 비즈니스 규칙 검증

### 위치
`core/domain/service/`

### 책임
- 여러 Aggregate를 조합한 복잡한 규칙 검증
- 정책 기반 검증
- 계산 기반 검증

### 검증 내용
- ✅ 일일 주문 한도
- ✅ 등급별 할인 제한
- ✅ 쿠폰 적용 조건
- ✅ 최소/최대 주문 금액
- ✅ 배송 가능 지역

### 예시

```java
// core/domain/service/OrderValidationService.java
@DomainService
public class OrderValidationService {
    
    /**
     * 주문 생성 비즈니스 규칙 검증
     */
    public void validateOrderCreation(
        List<OrderItemCommand> items,
        Coupon coupon,
        Customer customer
    ) {
        // 1. 일일 주문 한도 확인
        validateDailyOrderLimit(customer);
        
        // 2. 최소 주문 금액 확인
        validateMinimumOrderAmount(items, customer.getGrade());
        
        // 3. 최대 주문 금액 확인
        validateMaximumOrderAmount(items, customer.getGrade());
        
        // 4. 쿠폰 적용 가능 여부 확인
        if (coupon != null) {
            validateCouponApplicability(items, coupon, customer);
        }
        
        // 5. 등급별 구매 제한 확인
        validateGradeRestriction(items, customer.getGrade());
    }
    
    /**
     * 일일 주문 한도 검증
     */
    private void validateDailyOrderLimit(Customer customer) {
        int dailyLimit = switch (customer.getGrade()) {
            case VIP -> 10;
            case GOLD -> 7;
            case SILVER -> 5;
            case BRONZE -> 3;
        };
        
        if (customer.getTodayOrderCount() >= dailyLimit) {
            throw new DailyOrderLimitExceededException(
                String.format("일일 주문 한도(%d건)를 초과했습니다", dailyLimit)
            );
        }
    }
    
    /**
     * 최소 주문 금액 검증
     */
    private void validateMinimumOrderAmount(
        List<OrderItemCommand> items,
        CustomerGrade grade
    ) {
        Money totalAmount = calculateTotalAmount(items);
        
        Money minimumAmount = switch (grade) {
            case VIP -> Money.of(0);
            case GOLD -> Money.of(10_000);
            case SILVER -> Money.of(20_000);
            case BRONZE -> Money.of(30_000);
        };
        
        if (totalAmount.isLessThan(minimumAmount)) {
            throw new MinimumOrderAmountException(
                String.format(
                    "최소 주문 금액은 %s원입니다",
                    minimumAmount.value()
                )
            );
        }
    }
    
    /**
     * 최대 주문 금액 검증
     */
    private void validateMaximumOrderAmount(
        List<OrderItemCommand> items,
        CustomerGrade grade
    ) {
        Money totalAmount = calculateTotalAmount(items);
        
        Money maximumAmount = switch (grade) {
            case VIP -> Money.of(10_000_000);
            case GOLD -> Money.of(5_000_000);
            case SILVER -> Money.of(3_000_000);
            case BRONZE -> Money.of(1_000_000);
        };
        
        if (totalAmount.isGreaterThan(maximumAmount)) {
            throw new MaximumOrderAmountException(
                String.format(
                    "최대 주문 금액은 %s원입니다",
                    maximumAmount.value()
                )
            );
        }
    }
    
    /**
     * 쿠폰 적용 가능 여부 검증
     */
    private void validateCouponApplicability(
        List<OrderItemCommand> items,
        Coupon coupon,
        Customer customer
    ) {
        Money totalAmount = calculateTotalAmount(items);
        
        // 최소 사용 금액 확인
        if (totalAmount.isLessThan(coupon.getMinimumAmount())) {
            throw new CouponMinimumAmountException(
                String.format(
                    "쿠폰 사용 가능 최소 금액은 %s원입니다",
                    coupon.getMinimumAmount().value()
                )
            );
        }
        
        // 등급 제한 확인
        if (!coupon.isApplicableForGrade(customer.getGrade())) {
            throw new CouponGradeRestrictionException(
                "해당 등급에서는 사용할 수 없는 쿠폰입니다"
            );
        }
    }
    
    /**
     * 등급별 구매 제한 검증
     */
    private void validateGradeRestriction(
        List<OrderItemCommand> items,
        CustomerGrade grade
    ) {
        int maxQuantityPerItem = switch (grade) {
            case VIP -> 100;
            case GOLD -> 50;
            case SILVER -> 30;
            case BRONZE -> 10;
        };
        
        items.forEach(item -> {
            if (item.quantity() > maxQuantityPerItem) {
                throw new ItemQuantityLimitException(
                    String.format(
                        "상품당 최대 구매 가능 수량은 %d개입니다",
                        maxQuantityPerItem
                    )
                );
            }
        });
    }
    
    private Money calculateTotalAmount(List<OrderItemCommand> items) {
        // 계산 로직
        return Money.ZERO;
    }
}
```

---

## 5. Domain Entity - 핵심 불변식 검증

### 위치
`core/domain/*/model/`

### 책임
- 엔티티의 핵심 불변식 유지
- 상태 일관성 보장
- 계산 결과 검증

### 검증 내용
- ✅ 필수 값 존재
- ✅ 상태 전이 규칙
- ✅ 계산 결과 일치
- ✅ 불변식 유지
- ✅ 비즈니스 제약사항

### 예시

```java
// core/domain/order/model/Order.java
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {
    
    private Long id;
    private Long userId;
    private String orderNumber;
    private OrderStatus status;
    private List<OrderItem> items;
    private Address deliveryAddress;
    private Money totalAmount;
    private LocalDateTime createdAt;
    
    public static Order create(
        Long userId,
        List<OrderItem> items,
        Address deliveryAddress,
        Money totalAmount
    ) {
        Order order = Order.builder()
            .userId(userId)
            .orderNumber(generateOrderNumber())
            .status(OrderStatus.PENDING)
            .items(new ArrayList<>(items))
            .deliveryAddress(deliveryAddress)
            .totalAmount(totalAmount)
            .createdAt(LocalDateTime.now())
            .build();
        
        // 생성 시 검증
        order.validate();
        
        return order;
    }
    
    /**
     * 핵심 불변식 검증
     */
    private void validate() {
        // 1. 필수 값 검증
        if (userId == null || userId <= 0) {
            throw new InvalidOrderException("유효하지 않은 사용자 ID입니다");
        }
        
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new InvalidOrderException("주문번호는 필수입니다");
        }
        
        if (items == null || items.isEmpty()) {
            throw new EmptyOrderException("주문 상품이 없습니다");
        }
        
        if (deliveryAddress == null) {
            throw new InvalidOrderException("배송지는 필수입니다");
        }
        
        if (totalAmount == null || totalAmount.isNegative()) {
            throw new InvalidOrderException("유효하지 않은 주문 금액입니다");
        }
        
        // 2. 각 OrderItem 검증
        items.forEach(OrderItem::validate);
        
        // 3. 계산된 금액과 실제 금액 일치 검증
        Money calculatedAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
        
        if (!totalAmount.equals(calculatedAmount)) {
            throw new OrderAmountMismatchException(
                String.format(
                    "주문 금액 불일치 - 입력: %s, 계산: %s",
                    totalAmount.value(),
                    calculatedAmount.value()
                )
            );
        }
    }
    
    /**
     * 취소 가능 여부 확인 (비즈니스 규칙)
     */
    public boolean isCancellable() {
        return status == OrderStatus.PENDING || 
               status == OrderStatus.PAID;
    }
    
    /**
     * 주문 취소 (상태 전이 검증)
     */
    public void cancel(String reason) {
        if (!isCancellable()) {
            throw new OrderNotCancellableException(
                String.format(
                    "현재 상태(%s)에서는 취소할 수 없습니다",
                    status
                )
            );
        }
        
        if (reason == null || reason.isBlank()) {
            throw new InvalidOrderException("취소 사유는 필수입니다");
        }
        
        this.status = OrderStatus.CANCELLED;
    }
    
    /**
     * 배송지 변경 (상태 검증)
     */
    public void changeAddress(Address newAddress) {
        if (!isModifiable()) {
            throw new OrderNotModifiableException(
                "배송 준비 중이거나 배송 중인 주문은 수정할 수 없습니다"
            );
        }
        
        if (newAddress == null) {
            throw new InvalidOrderException("배송지는 필수입니다");
        }
        
        this.deliveryAddress = newAddress;
    }
    
    public boolean isModifiable() {
        return status == OrderStatus.PENDING;
    }
}
```

---

## 검증 레이어별 정리

| 레이어 | 위치 | 검증 내용 | 예시 |
|--------|------|-----------|------|
| **Controller** | api/web/*/dto/request/ | 형식 검증 | @NotNull, @Size, @Email |
| **Command** | core/application/*/usecase/command/ | 정합성 검증 | null 체크, 중복 체크 |
| **Application Support** | core/application/*/support/ | 비즈니스 규칙 사전 검증 | 존재 여부, 상태, 권한 |
| **Domain Service** | core/domain/service/ | 복잡한 비즈니스 규칙 | 일일 한도, 등급별 제한 |
| **Domain Entity** | core/domain/*/model/ | 핵심 불변식 | 상태 전이, 계산 일치 |

## 중요 원칙
1. 검증은 가능한 빠른 레이어에서
2. 각 레이어는 자신의 책임에 맞는 검증만
3. Domain Entity는 항상 유효한 상태 유지
4. 명확한 예외 메시지 제공
5. 검증 실패 시 구체적인 원인 전달
</artifact>

---