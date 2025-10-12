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
│                    Facade                                │
└────────────────────────┬────────────────────────────────┘
                         │ 의존
┌────────────────────────▼────────────────────────────────┐
│                   Domain Layer                           │
│      (Entities, Value Objects, Domain Services)          │
│      - OrderCreator, OrderReader (Domain Services)       │
│      required/ ⭐ (모든 외부 의존성 Port)                 │
│      ├── OrderRepository                                 │
│      └── OutboxEventClient                               │
│                  (순수 Java만 사용)                       │
└─────────────────────────────────────────────────────────┘
                         ↑ DIP (의존성 역전)
┌────────────────────────┼────────────────────────────────┐
│               Infrastructure Layer                       │
│     OrderRepositoryAdapter ⭐                             │
│     KafkaOutboxEventClient ⭐                             │
└─────────────────────────────────────────────────────────┘
```

**특징:**
- ✅ **Layered Architecture** (계층형 구조)
- ✅ **DDD** (Domain-Driven Design)
- ✅ **DIP** (Dependency Inversion Principle)
- ✅ **Facade Pattern** (Application Layer)
- ✅ **모든 외부 의존성 Port는 required/에 위치** (일관성)

**구조:**
```
core/domain/order/
├── Order.java                   # Aggregate Root
├── OrderCreator.java            # Domain Service (생성)
├── OrderReader.java             # Domain Service (조회)
├── command/
│   └── CreateOrderCommand.java  # Domain Command
└── required/                    # ⭐ 모든 외부 의존성 Port
    └── OrderRepository.java     # 영속성 (통합)

core/domain/address/
├── AddressRefiner.java          # Domain Service (주소 정제)
├── required/
│   └── AddressRefinementClient.java  # 역지오코딩 Port
└── exception/
    └── AddressRefineFailedException.java  # Domain Exception

core/domain/outbox/
├── OutboxEventAppender.java     # Domain Service
├── OutboxEventType.java         # Enum
└── required/
    └── OutboxEventClient.java   # Outbox Port

core/application/order/
└── OrderFacade.java             # Facade (주소 정제 + Order 생성)

infrastructure/
├── storage/db/order/
│   └── OrderRepositoryAdapter   # Repository 구현
├── external/address/
│   ├── AddressRefinementAdapter        # Fallback Chain (Neogeo → Naver → Kakao)
│   ├── provider/
│   │   ├── NeogeoReverseGeocodingProvider  # Neogeo 구현
│   │   ├── NaverReverseGeocodingProvider   # Naver 구현
│   │   └── KakaoReverseGeocodingProvider   # Kakao 구현
│   └── config/
│       └── AddressRefinementConfig         # Fallback 순서 설정
└── outbox/
    ├── KafkaOutboxEventClient   # Outbox 구현
    └── KafkaOutboxEventMapper   # Domain → Kafka Payload
```

### 모듈 구조

```
vroong-laas-order-ai-test/
├── core/                    # Domain + Application Layer
│   └── src/main/java/vroong/laas/order/core/
│       ├── domain/         # 순수 도메인 모델 + Port 인터페이스
│       │   ├── order/      # Order Aggregate
│       │   │   ├── Order.java              # Aggregate Root
│       │   │   ├── OrderCreator.java       # Domain Service (생성)
│       │   │   ├── OrderReader.java        # Domain Service (조회)
│       │   │   ├── command/
│       │   │   │   └── CreateOrderCommand.java  # Domain Command
│       │   │   └── required/               ⭐ 모든 외부 의존성 Port
│       │   │       └── OrderRepository.java     # 영속성 (통합)
│       │   ├── address/    # 주소 정제
│       │   │   ├── AddressRefiner.java          # Domain Service (주소 정제)
│       │   │   ├── required/
│       │   │   │   └── AddressRefinementClient.java  # 역지오코딩 Port
│       │   │   └── exception/
│       │   │       └── AddressRefineFailedException.java  # Domain Exception
│       │   ├── outbox/     # Outbox Pattern
│       │   │   ├── OutboxEventAppender.java     # Domain Service
│       │   │   ├── OutboxEventType.java         # Enum
│       │   │   └── required/
│       │   │       └── OutboxEventClient.java   # Outbox Port
│       │   └── shared/     # 공유 Value Objects
│       └── application/    # Facade (Application Layer)
│           └── order/
│               └── OrderFacade.java        # Facade (주소 정제 + Order 생성)
├── infrastructure/         # Infrastructure Layer (Port 구현)
│   └── src/main/java/vroong/laas/order/infrastructure/
│       ├── storage/db/     # JPA Entities
│       │   └── order/
│       │       └── OrderRepositoryAdapter.java   ⭐ Repository 구현
│       ├── external/       # 외부 서비스 연동
│       │   └── address/    # 주소 정제
│       │       ├── AddressRefinementAdapter.java        # Fallback Chain 구현
│       │       ├── provider/
│       │       │   ├── ReverseGeocodingProvider.java        # 공통 인터페이스
│       │       │   ├── NeogeoReverseGeocodingProvider.java  # Neogeo 구현
│       │       │   ├── NaverReverseGeocodingProvider.java   # Naver 구현
│       │       │   └── KakaoReverseGeocodingProvider.java   # Kakao 구현
│       │       └── config/
│       │           └── AddressRefinementConfig.java         # Fallback 순서 설정
│       └── outbox/         # Outbox Pattern
│           ├── KafkaOutboxEventClient.java   ⭐ OutboxEventClient 구현
│           └── KafkaOutboxEventMapper.java   # Domain → Kafka Payload
└── api/                    # Interface Layer
    └── src/main/java/vroong/laas/order/api/
        ├── web/           # REST Controllers
        └── grpc/          # gRPC Services
```

---

## 🗺️ 주소 정제 (Address Refinement)

### 개요

주문 생성 시, 사용자가 입력한 주소가 부정확할 수 있으므로 **위/경도 좌표 기반 역지오코딩**으로 정확한 주소로 정제합니다.

### Fallback Chain

역지오코딩 서비스 장애에 대비하여 **3단계 Fallback Chain**을 구성합니다:

```
1순위: Neogeo (내부 서비스)
   ↓ 실패
2순위: Naver (외부 서비스)
   ↓ 실패
3순위: Kakao (외부 서비스)
   ↓ 모두 실패
AddressRefineFailedException 발생 → Order 생성 실패
```

**Fallback 조건:**
- HTTP 4xx, 5xx 에러
- Timeout (기본 3초)
- 네트워크 에러

### 아키텍처

```
OrderFacade (Application Layer)
  1. AddressRefiner.refine(latLng, originalAddress)  # Domain Service
     ↓
  2. AddressRefinementClient.refineByReverseGeocoding()  # Port (required/)
     ↓
  3. AddressRefinementAdapter.refineByReverseGeocoding()  # Infrastructure
     ↓
  4. Fallback Chain 순회:
     - NeogeoReverseGeocodingProvider.reverseGeocode()  # 1순위
     - NaverReverseGeocodingProvider.reverseGeocode()   # 2순위
     - KakaoReverseGeocodingProvider.reverseGeocode()   # 3순위
```

**특징:**
- ✅ **Fallback 순서 설정 가능** (`application.yml`)
- ✅ **환경별 Provider 조합** (Local/Prod 다르게 설정 가능)
- ✅ **상세한 로그** (시도/성공/실패 기록)

### 설정 예시

```yaml
# application.yml
address:
  refinement:
    # Fallback 순서 (환경별로 변경 가능)
    fallback-order:
      - neogeo
      - naver
      - kakao
    
    # Provider별 설정
    neogeo:
      url: ${NEOGEO_URL:http://neogeo-service}
      timeout-ms: 3000
    
    naver:
      url: https://naveropenapi.apigw.ntruss.com
      client-id: ${NAVER_CLIENT_ID}
      client-secret: ${NAVER_CLIENT_SECRET}
      timeout-ms: 3000
    
    kakao:
      url: https://dapi.kakao.com
      api-key: ${KAKAO_API_KEY}
      timeout-ms: 3000
```

**환경별 설정 변경 예시:**

```yaml
# application-prod.yml
address:
  refinement:
    # Production에서는 Naver를 1순위로
    fallback-order:
      - naver
      - kakao
      - neogeo
```

### 상세 가이드

**더 자세한 주소 정제 정책은 [도메인정책.md](./도메인정책.md)의 "주소 정제" 섹션을 참고하세요.**

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
| **Spring Kafka** | Kafka Producer/Consumer (Outbox 라이브러리 Auto-Configuration 활성화) | [spring.io/projects/spring-kafka](https://spring.io/projects/spring-kafka) |
| **Spring Boot Starter JDBC** | JdbcTemplate 제공 (Outbox 라이브러리 Auto-Configuration 활성화) | [spring.io/projects/spring-boot](https://spring.io/projects/spring-boot) |
| **Vroong MSA Kafka Event** | Kafka 이벤트 표준 (v1.0.7) | Internal Library |
| **Vroong MSA Kafka Event Publisher** | Kafka Outbox 패턴 구현 (v0.0.15) | Internal Library |
| **JUnit 5** | 테스트 프레임워크 | [junit.org/junit5](https://junit.org/junit5/) |
| **AssertJ** | 가독성 좋은 Assertion | [assertj.github.io](https://assertj.github.io/doc/) |
| **Mockito** | Mock 객체 생성 (Application/Interface Layer 테스트) | [site.mockito.org](https://site.mockito.org/) |
| **Fixture Monkey** | 테스트 데이터 자동 생성 (v1.1.15) | [naver.github.io/fixture-monkey](https://naver.github.io/fixture-monkey/) |
| **Spring REST Docs** | REST API 문서 자동 생성 (Asciidoctor) | [spring.io/projects/spring-restdocs](https://spring.io/projects/spring-restdocs) |

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
| **Application** | Facade 테스트 (Domain Service는 Mock) | JUnit 5, Mockito |
| **Infrastructure** | Repository/Adapter 통합 테스트 | `@DataJpaTest`, H2 |
| **Interface** | Controller 테스트 (API 문서 자동 생성) | `@WebMvcTest`, MockMvc, REST Docs |

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

### REST Docs - API 문서 자동 생성

Controller 테스트와 함께 API 문서를 자동으로 생성합니다.

```java
@WebMvcTest(
    controllers = OrderController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "vroong.laas.order.api.web.common.logging.*"
    )
)
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class, WebApiControllerAdvice.class})
class OrderControllerTest {
    
    @Test
    void createOrder_success() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andDo(document("order-create",
                requestFields(
                    fieldWithPath("items[]").description("주문 아이템 목록"),
                    // ...
                ),
                responseFields(
                    fieldWithPath("id").description("주문 ID"),
                    // ...
                )
            ));
    }
}
```

**주요 특징:**
- ✅ `@WebMvcTest` 사용 (Web Layer만 로드, 빠른 실행)
- ✅ `@AutoConfigureRestDocs` 자동 설정
- ✅ 커스텀 템플릿으로 Constraints 컬럼 자동 추가 (`src/test/resources/org/springframework/restdocs/templates/asciidoctor/`)
- ✅ FixtureMonkey 사용 안 함 (일관된 문서 생성 위해 고정 데이터 사용)
- ✅ `WebApiControllerAdvice` 명시적 Import (전역 예외 처리)
- ✅ `RequestResponseLoggingFilter` 제외 (테스트 환경에서 불필요)

**자세한 테스트 가이드는 [.cursor/rules/07-testing.mdc](./.cursor/rules/07-testing.mdc)를 참고하세요.**

---

## 📡 API 응답 표준

### 응답 형식

모든 API 응답은 다음 두 가지 형태를 따릅니다:

#### 1. 성공 응답 (2xx)

일반 객체를 직접 반환합니다.

```json
// 200 OK - 조회 성공
{
  "id": 1,
  "orderNumber": "ORD-20251005123045001",
  "status": "CREATED",
  "items": [...],
  "orderedAt": "2025-10-12T07:20:16Z"
}
```

| 상태 코드 | 용도 | 반환 형식 |
|----------|------|----------|
| **200 OK** | 조회, 수정 성공 | 객체 직접 반환 (OrderResponse, PageResponse 등) |
| **201 Created** | 생성 성공 | 생성된 객체 반환 (OrderResponse) |
| **204 No Content** | 삭제 성공 | 응답 Body 없음 |

#### 2. 에러 응답 (4xx, 5xx)

**RFC 7807 (Problem Details for HTTP APIs)** 표준을 따릅니다.

```json
// 400 Bad Request - 클라이언트 입력 에러
{
  "status": 400,
  "title": "Bad Request",
  "detail": "주문을 찾을 수 없습니다. ID: 999",
  "properties": {
    "timestamp": "2025-10-12T07:20:16.360318Z",
    "errorCode": "ORDER_NOT_FOUND",
    "exception": "OrderNotFoundException"
  }
}

// 503 Service Unavailable - 재시도 가능 에러
{
  "status": 503,
  "title": "Service Unavailable",
  "detail": "모든 역지오코딩 서비스가 실패했습니다",
  "properties": {
    "timestamp": "2025-10-12T07:20:16.360318Z",
    "errorCode": "ADDRESS_REFINE_FAILED",
    "exception": "AddressRefineFailedException",
    "retryable": true  // ⭐ 재시도 가능 여부 (부릉 내부 표준)
  }
}
```

### 재시도 가능한 에러 (5xx) ⭐

일시적 장애로 인한 에러는 재시도 가능함을 명시합니다.

**HTTP 상태:**
- **503 Service Unavailable** - 외부 서비스 일시적 장애

**헤더:**
- `Retry-After`: 재시도 권장 시간(초)

**응답 필드:**
- `retryable: true` - 재시도 가능 표시 (부릉 내부 표준)

**예시:**
```http
HTTP/1.1 503 Service Unavailable
Retry-After: 60

{
  "status": 503,
  "title": "Service Unavailable",
  "detail": "모든 역지오코딩 서비스가 실패했습니다",
  "properties": {
    "errorCode": "ADDRESS_REFINE_FAILED",
    "retryable": true
  }
}
```

**재시도 가능 에러 타입:**
- `ADDRESS_REFINE_FAILED` - 주소 정제 실패 (역지오코딩 서비스 장애)
- (향후 추가 예정)

### 에러 코드 목록

| 에러 코드 | HTTP 상태 | 설명 | 재시도 가능 |
|----------|----------|------|------------|
| `ORDER_NOT_FOUND` | 400 | 주문을 찾을 수 없음 | ❌ |
| `INVALID_INPUT` | 400 | 잘못된 입력 값 | ❌ |
| `VALIDATION_ERROR` | 400 | Bean Validation 실패 | ❌ |
| `ADDRESS_REFINE_FAILED` | 503 | 주소 정제 실패 | ✅ |
| `INTERNAL_SERVER_ERROR` | 500 | 예상하지 못한 서버 에러 | ❌ |

### 클라이언트 가이드

#### 에러 처리 예시 (TypeScript)

```typescript
async function createOrder(request: CreateOrderRequest) {
  try {
    const response = await api.post('/api/v1/orders', request);
    return response.data;
    
  } catch (error) {
    if (error.response?.status === 503) {
      const retryAfter = error.response.headers['retry-after'];
      const retryable = error.response.data.properties?.retryable;
      
      if (retryable) {
        // 재시도 로직
        await sleep(retryAfter * 1000);
        return createOrder(request);  // 재시도
      }
    }
    
    // 에러 코드별 처리
    const errorCode = error.response?.data.properties?.errorCode;
    switch (errorCode) {
      case 'ORDER_NOT_FOUND':
        // 주문 없음 처리
        break;
      case 'VALIDATION_ERROR':
        // 유효성 검증 에러 처리
        const fieldErrors = error.response.data.properties.fieldErrors;
        break;
      default:
        // 일반 에러 처리
    }
  }
}
```

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
4. **Domain Service**로 비즈니스 로직 구현 (OrderCreator, OrderReader)

### Layered Architecture with DDD & DIP
1. **Layered Architecture** (계층형 구조)
2. **DDD** (Domain-Driven Design 적용)
3. **DIP** (의존성 역전 - 모든 Port는 Domain에)
4. **Facade Pattern** (Application Layer는 Domain Service 조합)
5. **모든 Port는 Domain Layer의 required/에 위치** (외부 의존성 분리)
6. **Infrastructure가 Domain Port를 구현** (Adapter 패턴)

### Outbox Pattern
1. **DB 트랜잭션과 이벤트 발행의 원자성 보장**
2. **OutboxEventAppender** (Domain Service)로 Outbox 저장
3. **KafkaOutboxEventClient** (Adapter)로 외부 라이브러리 연동
4. **별도 Worker가 Outbox → Kafka 전송** (비동기)

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
