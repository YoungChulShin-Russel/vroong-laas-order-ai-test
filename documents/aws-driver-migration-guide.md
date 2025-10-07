# AWS Advanced JDBC Driver 전환 가이드

## 개요

이 문서는 기존 `ReplicationRoutingDataSource` 구현에서 AWS Advanced JDBC Driver로 전환한 내용을 설명합니다.

---

## 변경 사항 요약

### Before (기존 구현)

```java
// ReplicationRoutingDataSource (254줄)
// - @Transactional 기반 자동 라우팅
// - Default READ Pool
// - @Transactional 없는 조회도 Reader Pool 사용

@Service
public class OrderQueryService {
    // @Transactional 없어도 Reader Pool
    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }
}
```

### After (AWS Driver)

```java
// AWS Advanced JDBC Driver (IAM, Failover, ReadWriteSplitting)
// - Cluster Endpoint 하나로 통합
// - IAM 인증 (비밀번호 불필요)
// - Failover 1-2초 자동 처리

@Service
public class OrderQueryService {
    // ⭐ @Transactional(readOnly=true) 필수
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }
}
```

---

## ⚠️ 필수 작업: Service Layer에 @Transactional 추가

### 1. UseCase에 @Transactional 추가

#### 쓰기 UseCase

```java
// core/application/order/usecase/CreateOrderUseCase.java
@UseCase
public class CreateOrderUseCase {
    
    private final OrderStore orderStore;
    
    // ⭐ 쓰기 작업: @Transactional 필수
    @Transactional
    public Order execute(CreateOrderCommand command) {
        Order order = Order.create(...);
        return orderStore.store(order);
    }
}
```

#### 읽기 UseCase

```java
// core/application/order/usecase/GetOrderUseCase.java
@UseCase
public class GetOrderUseCase {
    
    private final OrderReader orderReader;
    
    // ⭐ 읽기 작업: @Transactional(readOnly=true) 필수
    @Transactional(readOnly = true)
    public Order execute(Long orderId) {
        return orderReader.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
```

#### 여러 조회를 포함하는 UseCase

```java
// core/application/order/usecase/GetOrderSummaryUseCase.java
@UseCase
public class GetOrderSummaryUseCase {
    
    private final OrderReader orderReader;
    private final PaymentReader paymentReader;
    
    // ⭐ 단일 트랜잭션 안에서 여러 조회 (Connection 재사용)
    @Transactional(readOnly = true)
    public OrderSummary execute(Long orderId) {
        Order order = orderReader.findById(orderId).orElseThrow();
        Payment payment = paymentReader.findByOrderId(orderId).orElseThrow();
        
        return new OrderSummary(order, payment);
    }
}
```

### 2. 왜 필요한가?

AWS Driver는 **명시적으로 `@Transactional(readOnly=true)`를 설정해야만** Reader Pool을 사용합니다.

```java
// ❌ @Transactional 없으면 → Writer Pool 사용 (비효율)
public Order findById(Long id) {
    return orderRepository.findById(id).orElseThrow();
}

// ✅ @Transactional(readOnly=true) → Reader Pool 사용
@Transactional(readOnly = true)
public Order findById(Long id) {
    return orderRepository.findById(id).orElseThrow();
}
```

### 3. Repository는 어떻게 되나?

Spring Data JPA의 `SimpleJpaRepository`는 이미 클래스 레벨에 `@Transactional(readOnly = true)`가 있습니다.

```java
// Spring Data JPA 내부 (자동)
@Repository
@Transactional(readOnly = true)  // ⭐ 클래스 레벨
public class SimpleJpaRepository<T, ID> {
    
    public Optional<T> findById(ID id) {
        // readOnly = true (자동)
    }
    
    @Transactional  // ⭐ 메서드 레벨에서 readOnly = false로 오버라이드
    public <S extends T> S save(S entity) {
        // readOnly = false
    }
}
```

**따라서:**
- ✅ Repository를 **직접 호출**하면 자동으로 Reader Pool
- ⚠️ Service Layer에서 **여러 Repository를 호출**하면 Connection 여러 번 생성
- ✅ Service Layer에 `@Transactional(readOnly=true)` 추가하면 Connection 재사용

---

## UseCase 작성 패턴

### 패턴 1: 단순 조회 (Repository 직접 호출)

```java
@UseCase
public class GetOrderUseCase {
    
    private final OrderRepository orderRepository;
    
    // SimpleJpaRepository의 @Transactional(readOnly=true) 사용
    // 추가 어노테이션 불필요
    public Order execute(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
```

**이 경우는 괜찮습니다.** SimpleJpaRepository가 알아서 처리합니다.

### 패턴 2: 복잡한 조회 (여러 Repository 호출)

```java
@UseCase
public class GetOrderSummaryUseCase {
    
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    
    // ⭐ @Transactional(readOnly=true) 필수 (Connection 재사용)
    @Transactional(readOnly = true)
    public OrderSummary execute(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        
        return new OrderSummary(order, payment);
    }
}
```

**이 경우는 필수입니다.** Connection 재사용으로 성능 최적화.

### 패턴 3: 쓰기 작업

```java
@UseCase
public class CreateOrderUseCase {
    
    private final OrderStore orderStore;
    
    // ⭐ @Transactional 필수 (Writer Pool)
    @Transactional
    public Order execute(CreateOrderCommand command) {
        Order order = Order.create(...);
        return orderStore.store(order);
    }
}
```

### 패턴 4: 여러 Aggregate 수정

```java
@UseCase
public class ProcessOrderPaymentUseCase {
    
    private final OrderStore orderStore;
    private final PaymentStore paymentStore;
    
    // ⭐ @Transactional 필수 (원자성 보장)
    @Transactional
    public void execute(ProcessPaymentCommand command) {
        // 여러 Aggregate를 하나의 트랜잭션으로
        Order order = orderStore.findById(command.orderId()).orElseThrow();
        order.confirmPayment();
        orderStore.store(order);
        
        Payment payment = Payment.create(command.orderId(), command.amount());
        paymentStore.store(payment);
    }
}
```

---

## 체크리스트

### UseCase 작성 시

- [ ] 단순 조회 (Repository 1개) → `@Transactional` 생략 가능
- [ ] 복잡한 조회 (Repository 2개 이상) → `@Transactional(readOnly=true)` 필수
- [ ] 쓰기 작업 → `@Transactional` 필수
- [ ] 여러 Aggregate 수정 → `@Transactional` 필수

### 마이그레이션 시

- [x] `ReplicationRoutingDataSource.java` 삭제
- [x] `ProductionDataSourceConfig.java` 단순화
- [x] `application-prod.yml` AWS Driver 설정 추가
- [x] IAM Role/Policy 설정 (문서: `aws-iam-database-auth-guide.md`)
- [ ] 모든 UseCase에 적절한 `@Transactional` 추가
- [ ] 테스트 실행 (특히 조회 성능 확인)
- [ ] Production 배포 (Canary 배포 권장)

---

## import 추가

UseCase에서 `@Transactional`을 사용하려면 import를 추가해야 합니다:

```java
import org.springframework.transaction.annotation.Transactional;
```

**주의:** 
- ✅ `org.springframework.transaction.annotation.Transactional` (Spring)
- ❌ `jakarta.transaction.Transactional` (JTA - 사용하지 말 것)

---

## 참고 자료

- [AWS IAM Database Authentication 가이드](./aws-iam-database-auth-guide.md)
- [AWS Advanced JDBC Driver Wiki](https://github.com/aws/aws-advanced-jdbc-wrapper/wiki)
- [Spring @Transactional 공식 문서](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)
