# LaaS Order Service with AI

> AI와 함께 Order Service를 테스트로 만들어보는 저장소

## 📘 도메인 정책

**주문 서비스의 핵심 도메인 규칙과 정책은 [도메인정책.md](./도메인정책.md)를 참고하세요.**

주요 내용:
- 주문번호 포맷 규칙 (`ORD-` Prefix)
- 주문 상태 전이 (CREATED → DELIVERED/CANCELLED)
- 필수/선택 속성
- 이벤트 기반 처리

---

## 🏗️ 아키텍처

### Layered Architecture with DDD & DIP

```
┌─────────────────────────────────────────────────────────┐
│                   Interface Layer                        │
│              (API Controllers, gRPC)                     │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                  Application Layer                       │
│         UseCase (구체 클래스, In Port 없음)               │
└────────────────────────┬────────────────────────────────┘
                         │ 의존
┌────────────────────────▼────────────────────────────────┐
│                   Domain Layer                           │
│      (Entities, Value Objects, Domain Services)          │
│      required/ ⭐ (모든 외부 의존성 Port)                 │
│      ├── OrderStore, OrderReader                         │
│      └── EmailSender, EventPublisher                     │
│                  (순수 Java만 사용)                       │
└─────────────────────────────────────────────────────────┘
                         ↑ DIP (의존성 역전)
┌────────────────────────┼────────────────────────────────┐
│               Infrastructure Layer                       │
│     OrderStoreAdapter, OrderReaderAdapter ⭐              │
│     EmailSenderAdapter, EventPublisherAdapter ⭐          │
└─────────────────────────────────────────────────────────┘
```

**특징:**
- ✅ **Layered Architecture** (계층형 구조)
- ✅ **DDD** (Domain-Driven Design)
- ✅ **DIP** (Dependency Inversion Principle)
- ✅ **모든 외부 의존성 Port는 required/에 위치** (일관성)
- ❌ **In Port 없음** (UseCase는 구체 클래스)

**구조:**
```
core/domain/order/
├── Order.java
└── required/                    # ⭐ 모든 외부 의존성 Port
    ├── OrderStore.java          # 영속성 (쓰기)
    ├── OrderReader.java         # 영속성 (읽기)
    ├── EmailSender.java
    ├── EventPublisher.java
    └── PaymentGateway.java

core/application/order/
├── usecase/
│   └── CreateOrderUseCase.java  # 구체 클래스 (In Port 없음)
└── command/
    └── CreateOrderCommand.java

infrastructure/
├── storage/db/order/
│   ├── OrderStoreAdapter        # Store 구현
│   └── OrderReaderAdapter       # Reader 구현
└── messaging/
    ├── EmailSenderAdapter       # Port 구현
    └── EventPublisherAdapter
```

### 모듈 구조

```
vroong-laas-order-ai-test/
├── core/                    # Domain + Application Layer
│   └── src/main/java/vroong/laas/order/core/
│       ├── domain/         # 순수 도메인 모델 + Port 인터페이스
│       │   ├── order/      # Order Aggregate
│       │   │   ├── Order.java
│       │   │   └── required/             ⭐ 모든 외부 의존성 Port
│       │   │       ├── OrderStore.java       # 영속성 (쓰기)
│       │   │       ├── OrderReader.java      # 영속성 (읽기)
│       │   │       ├── EmailSender.java
│       │   │       ├── EventPublisher.java
│       │   │       └── PaymentGateway.java
│       │   └── shared/     # 공유 Value Objects
│       └── application/    # Use Cases (구체 클래스)
│           └── order/
│               ├── usecase/
│               │   └── CreateOrderUseCase.java  # In Port 없음
│               └── command/
│                   └── CreateOrderCommand.java
├── infrastructure/         # Infrastructure Layer (Port 구현)
│   └── src/main/java/vroong/laas/order/infrastructure/
│       ├── storage/db/     # JPA Entities
│       │   └── order/
│       │       ├── OrderStoreAdapter.java   ⭐ Store 구현
│       │       └── OrderReaderAdapter.java  ⭐ Reader 구현
│       └── messaging/      # Kafka, Email 등
│           ├── EmailSenderAdapter.java      ⭐ 구현
│           └── EventPublisherAdapter.java   ⭐ 구현
└── api/                    # Interface Layer
    └── src/main/java/vroong/laas/order/api/
        ├── web/           # REST Controllers
        └── grpc/          # gRPC Services
```

---

## 🛠️ 기술 스택

### Core
- **Java 25**
- **Spring Boot 4.0.0-M3**
- **Gradle 9.1.0**

### 데이터베이스
- **MySQL 8.0.27** (Local - Docker)
- **AWS Aurora MySQL 3.x** (Production - MySQL 8.0 호환)
- **CQRS 패턴** - Read/Write DataSource 분리 (Spring `ReplicationRoutingDataSource`)
  - ✅ **Read/Write Splitting**: `@Transactional` → Writer, `@Transactional(readOnly=true)` 또는 없음 → Reader
  - ✅ **성능 최적화**: `Propagation.SUPPORTS`로 불필요한 트랜잭션 오버헤드 제거 (조회 성능 ~50% 향상)
  - ✅ **AWS Driver와 결합**: Writer는 Cluster Endpoint, Reader는 Reader Endpoint
- **AWS Advanced JDBC Driver 2.6.4** - Aurora 최적화
  - ✅ **빠른 Failover**: DNS 대기 없이 1-2초 내 자동 전환
  - ✅ **Reader LoadBalancing**: 여러 Reader 인스턴스 자동 분산
- **HikariCP** - Connection Pool (Writer: 20, Reader: 50)
- **Flyway 11.x** - 스키마 버전 관리 (Local 환경에서만 자동 실행)

### 라이브러리

| 라이브러리 | 용도 | 링크 |
|-----------|------|------|
| **Lombok** | 보일러플레이트 코드 제거 (`@Getter`, `@Builder` 등) | [projectlombok.org](https://projectlombok.org/) |
| **Spring Data JPA** | 데이터베이스 접근 (Infrastructure Layer) | [spring.io/projects/spring-data-jpa](https://spring.io/projects/spring-data-jpa) |
| **AWS Advanced JDBC Driver** | Aurora MySQL 최적화 (빠른 Failover 1-2초, Reader LoadBalancing) | [github.com/aws/aws-advanced-jdbc-wrapper](https://github.com/aws/aws-advanced-jdbc-wrapper) |
| **HikariCP** | Connection Pool (고성능 JDBC Connection Pool) | [github.com/brettwooldridge/HikariCP](https://github.com/brettwooldridge/HikariCP) |
| **Flyway** | 데이터베이스 마이그레이션 (Local 환경 자동 실행) | [flywaydb.org](https://flywaydb.org/) |
| **Spring Boot Actuator** | Health Check, Metrics, Kubernetes Probe 지원 | [docs.spring.io/spring-boot/reference/actuator](https://docs.spring.io/spring-boot/reference/actuator/index.html) |
| **Micrometer Prometheus** | Prometheus 메트릭 수집 (운영 모니터링) | [micrometer.io](https://micrometer.io/) |
| **JUnit 5** | 테스트 프레임워크 | [junit.org/junit5](https://junit.org/junit5/) |
| **AssertJ** | 가독성 좋은 Assertion | [assertj.github.io](https://assertj.github.io/doc/) |
| **Mockito** | Mock 객체 생성 (Application/Interface Layer 테스트) | [site.mockito.org](https://site.mockito.org/) |
| **Fixture Monkey** | 테스트 데이터 자동 생성 (v1.1.15) | [naver.github.io/fixture-monkey](https://naver.github.io/fixture-monkey/) |

### 버전 관리

라이브러리 버전은 `gradle.properties`에서 중앙 관리합니다.

```properties
# gradle.properties
fixtureMonkeyVersion=1.1.15
```

---

## 🧪 테스트 전략

### 계층별 테스트

| 계층 | 테스트 방법 | 주요 도구 |
|------|------------|----------|
| **Domain** | 순수 Java 단위 테스트 (Spring Context 없음) | JUnit 5, AssertJ, Fixture Monkey |
| **Application** | UseCase 테스트 (Port는 Mock) | JUnit 5, Mockito |
| **Infrastructure** | Repository 통합 테스트 | `@DataJpaTest`, H2 |
| **Interface** | Controller 테스트 | `@WebMvcTest`, MockMvc |

### Fixture Monkey 활용

테스트 데이터는 Fixture Monkey로 자동 생성합니다.

```java
// 설정
FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
    .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
    .defaultNotNull(true)
    .build();

// 사용 예시
Order order = fixtureMonkey.giveMeBuilder(Order.class)
    .set("status", OrderStatus.CREATED)
    .sample();
```

**자세한 테스트 가이드는 [.cursor/rules/07-testing.mdc](./.cursor/rules/07-testing.mdc)를 참고하세요.**

---

## 📦 빌드 및 실행

### 로컬 환경 구성

**1. Docker 컨테이너 실행 (MySQL)**
```bash
cd scripts
docker-compose up -d

# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f mysql

# 컨테이너 종료
docker-compose down

# 데이터 초기화 (개발 시)
docker-compose down -v  # 볼륨 삭제
docker-compose up -d
```

**서비스 정보:**
- MySQL: `localhost:3306`
  - Database: `order`
  - User: `order_user`
  - Password: `order_password`

### 전체 빌드
```bash
./gradlew build
```

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :core:test
./gradlew :infrastructure:test
./gradlew :api:test

# 특정 클래스 테스트
./gradlew :core:test --tests "vroong.laas.order.core.domain.order.OrderTest"
```

### 애플리케이션 실행
```bash
# 1. Docker 컨테이너 실행 (위 참고)
cd scripts
docker-compose up -d

# 2. 애플리케이션 실행 (Flyway 자동 마이그레이션)
cd ..
./gradlew :api:bootRun

# ✅ 로그에서 Flyway 마이그레이션 확인
# INFO v.l.o.i.common.config.FlywayConfig - ===== Flyway Migration Starting =====
# INFO o.f.core.internal.command.DbMigrate - Successfully applied 1 migration
```

### 데이터베이스 확인
```bash
# 테이블 목록 확인
docker exec order-mysql mysql -u order_user -porder_password order -e "SHOW TABLES;"

# 특정 테이블 구조 확인
docker exec order-mysql mysql -u order_user -porder_password order -e "DESC orders;"

# Flyway 마이그레이션 이력 확인
docker exec order-mysql mysql -u order_user -porder_password order \
  -e "SELECT installed_rank, version, description, installed_on FROM flyway_schema_history;"
```

---

## 📚 문서

- **[도메인 정책](./도메인정책.md)** - 핵심 비즈니스 규칙 ⭐
- **[Flyway 마이그레이션 가이드](./documents/flyway-guide.md)** - DB 스키마 버전 관리 ⭐
- **[Actuator 가이드](./documents/actuator-guide.md)** - Health Check, Kubernetes Probe 설정 ⭐
- **[AWS Aurora MySQL 설정 가이드](./documents/aws-aurora-setup.md)** - Production 환경 배포 ⭐
- **[아키텍처](./documents/architecture.md)** - 전체 시스템 구조
- **[ERD](./documents/ERD.md)** - 데이터베이스 스키마
- **[개발 가이드](./.cursor/rules/)** - 계층별 코딩 규칙

### 개발 가이드 목록
- `00-workflow.mdc` - 작업 흐름 및 커뮤니케이션
- `01-overview.mdc` - 프로젝트 개요
- `02-domain.mdc` - Domain Layer 규칙
- `03-application.mdc` - Application Layer 규칙
- `04-infrastructure.mdc` - Infrastructure Layer 규칙
- `05-interface.mdc` - Interface Layer 규칙
- `06-validation.mdc` - 유효성 검증 규칙
- `07-testing.mdc` - 테스트 작성 가이드

### Flyway 마이그레이션
- **Entity 변경 시 반드시 Flyway 마이그레이션 파일 추가**
- 파일 네이밍: `V{YYYYMMDD}_{NNN}__{Description}.sql`
- 예시: `V20250106_001__Create_order_aggregate.sql`
- **자세한 가이드는 [Flyway 마이그레이션 가이드](./documents/flyway-guide.md) 참고**

---

## 🎯 핵심 원칙

### Domain-Driven Design (DDD)
1. **Domain Layer는 순수 Java만 사용** (Spring, JPA 의존성 없음)
2. **Aggregate Root**를 중심으로 불변식 유지
3. **Value Object**로 도메인 개념 명확히 표현

### Layered Architecture with DDD & DIP
1. **Layered Architecture** (계층형 구조)
2. **DDD** (Domain-Driven Design 적용)
3. **DIP** (의존성 역전 - 모든 Port는 Domain에)
4. **모든 Port는 Domain Layer에 위치** (외부 의존성 분리)
5. **UseCase는 구체 클래스** (In Port 없음)
6. **Infrastructure가 Domain Port를 구현** (Adapter 패턴)

### 테스트 주도
1. **모든 도메인 로직은 테스트 필수**
2. **Fixture Monkey**로 다양한 테스트 케이스 생성
3. **Given-When-Then** 패턴 준수

---

## 🤝 기여 가이드

1. 새로운 기능 개발 전 [도메인정책.md](./도메인정책.md) 확인
2. 계층별 규칙([.cursor/rules/](./.cursor/rules/)) 준수
3. **Entity 변경 시 Flyway 마이그레이션 파일 추가** ([가이드](./documents/flyway-guide.md))
4. 테스트 코드 작성 필수
5. 린터 에러 해결 후 커밋

### Entity 변경 시 체크리스트
- [ ] Domain Entity 변경 완료
- [ ] JPA Entity 변경 완료
- [ ] Flyway 마이그레이션 파일 생성 (`V{YYYYMMDD}_{NNN}__{Description}.sql`)
- [ ] 로컬에서 마이그레이션 테스트 완료
- [ ] 테이블 구조 확인 완료
- [ ] 롤백 스크립트 준비 (문서화)

---

## 📧 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.
