# Spring Boot Actuator 가이드

> Order Service의 Health Check, Metrics, Kubernetes Probe 설정 가이드

---

## 📋 목차

1. [개요](#개요)
2. [Actuator 엔드포인트](#actuator-엔드포인트)
3. [Health Check](#health-check)
4. [Kubernetes Probe 설정](#kubernetes-probe-설정)
5. [Prometheus Metrics](#prometheus-metrics)
6. [Graceful Shutdown](#graceful-shutdown)
7. [환경별 설정](#환경별-설정)
8. [모니터링 대시보드](#모니터링-대시보드)

---

## 개요

Spring Boot Actuator는 애플리케이션의 상태를 모니터링하고 관리할 수 있는 프로덕션 레벨의 기능을 제공합니다.

### 주요 기능
- ✅ **Health Check** - 애플리케이션 및 의존성(DB, Disk 등) 상태 확인
- ✅ **Metrics** - JVM, HTTP, 커스텀 메트릭 수집
- ✅ **Kubernetes Probe** - Liveness/Readiness/Startup Probe 지원
- ✅ **Prometheus** - Prometheus 메트릭 수집 (운영 모니터링)
- ✅ **Graceful Shutdown** - 진행 중인 요청 완료 후 종료

---

## Actuator 엔드포인트

### 기본 엔드포인트

| 엔드포인트 | 용도 | Local | Production |
|-----------|------|-------|------------|
| `/actuator/health` | 전체 Health Check | ✅ | ✅ |
| `/actuator/health/liveness` | Liveness Probe (Pod 재시작 여부) | ✅ | ✅ |
| `/actuator/health/readiness` | Readiness Probe (트래픽 수신 여부) | ✅ | ✅ |
| `/actuator/info` | 애플리케이션 정보 | ✅ | ✅ |
| `/actuator/metrics` | 메트릭 목록 | ✅ | ❌ |
| `/actuator/prometheus` | Prometheus 메트릭 | ✅ | ✅ |
| `/actuator/env` | 환경 변수 | ✅ | ❌ |
| `/actuator/loggers` | 로그 레벨 조회/변경 | ✅ | ❌ |
| `/actuator/threaddump` | 스레드 덤프 | ✅ | ❌ |
| `/actuator/heapdump` | 힙 덤프 | ✅ | ❌ |

### 접근 방법

**Local 환경:**
```bash
# 전체 Health Check
curl http://localhost:8080/actuator/health

# Liveness Probe
curl http://localhost:8080/actuator/health/liveness

# Readiness Probe
curl http://localhost:8080/actuator/health/readiness

# 메트릭 목록
curl http://localhost:8080/actuator/metrics

# 특정 메트릭 (JVM 메모리)
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Prometheus 메트릭
curl http://localhost:8080/actuator/prometheus
```

**Production 환경:**
```bash
# Health Check만 노출 (보안)
curl https://order-api.example.com/actuator/health
curl https://order-api.example.com/actuator/info
curl https://order-api.example.com/actuator/prometheus
```

---

## Health Check

### Health Check 응답 예시

#### 정상 상태 (UP)

**Local 환경 (상세 정보 노출):**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 123456789012,
        "threshold": 10485760,
        "exists": true
      }
    },
    "livenessState": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    },
    "readinessState": {
      "status": "UP"
    }
  }
}
```

**Production 환경 (최소 정보만 노출):**
```json
{
  "status": "UP"
}
```

#### 장애 상태 (DOWN)

```json
{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN",
      "details": {
        "error": "org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection"
      }
    }
  }
}
```

### Health Indicator 구성

Order Service는 다음 Health Indicator를 사용합니다:

1. **DB Health** - MySQL 연결 상태
   - Local: 기본 DB Health Check
   - Production: 커스텀 DataSource Health Check (Writer/Reader 분리)
   - `SELECT 1` 쿼리로 검증

2. **Disk Space Health** - 디스크 용량
   - 임계값: 10MB
   - 임계값 미만 시 DOWN

3. **Liveness State** - Pod 살아있는지 확인
   - 애플리케이션 내부 오류 감지
   - DOWN 시 Kubernetes가 Pod 재시작

4. **Readiness State** - 트래픽 수신 가능한지 확인
   - 시작 중: NOT_READY
   - 실행 중: READY
   - 종료 중: NOT_READY

---

## Probe와 DB Health Check 분리 전략

### 핵심 원칙 ⭐

**Kubernetes Probe에서 DB Health Check를 제외합니다.**

```
/actuator/health/liveness  → DB 제외 (livenessState만)
/actuator/health/readiness → DB 제외 (readinessState만)
/actuator/health           → DB 포함 (모니터링용)
```

### 왜 DB를 제외하는가?

#### 1. DB 장애는 일시적일 수 있음
```
Aurora Failover: 1-2초
→ HikariCP 자동 재시도
→ 자동 복구 가능
```

#### 2. Probe는 애플리케이션 자체 상태만 체크해야 함
```
✅ JVM이 살아있나?
✅ 스레드 풀이 정상인가?
✅ 요청을 받을 수 있나?

❌ DB가 연결되나? (애플리케이션 외부)
```

#### 3. DB 장애는 애플리케이션 내부에서 처리
```
HikariCP:
  - connectionTimeout
  - maxLifetime
  - 자동 재시도

AWS Advanced JDBC Wrapper:
  - Failover Plugin (1-2초)
  - Read Replica 로드밸런싱
```

### 설정

**application.yml:**
```yaml
management:
  endpoint:
    health:
      group:
        liveness:
          include: livenessState       # ⭐ DB 제외
        readiness:
          include: readinessState      # ⭐ DB 제외
  
  health:
    db:
      enabled: true  # 모니터링용으로는 활성화
```

### Health Check 응답

#### /actuator/health/liveness (Probe용)
```json
{
  "status": "UP",
  "components": {
    "livenessState": {"status": "UP"}
  }
}
```

#### /actuator/health/readiness (Probe용)
```json
{
  "status": "UP",
  "components": {
    "readinessState": {"status": "UP"}
  }
}
```

#### /actuator/health (모니터링용)
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},           // ⭐ DB 포함
    "diskSpace": {"status": "UP"},
    "livenessState": {"status": "UP"},
    "readinessState": {"status": "UP"}
  }
}
```

### DB 장애 시 동작 흐름

```
1. DB 장애 발생 (Aurora Failover 중)
   ↓
2. /actuator/health → DB: DOWN
   → Prometheus 감지 → Slack 알림 🔔
   ↓
3. /actuator/health/liveness → UP
   → Pod 재시작 안 함 ✅
   ↓
4. /actuator/health/readiness → UP
   → 트래픽 계속 받음 ✅
   ↓
5. HikariCP가 재시도
   → Aurora Failover 완료 (1-2초)
   → 자동 복구 ✅
   ↓
6. /actuator/health → DB: UP
   → Slack 알림 (복구됨) 🎉
```

### 모니터링 설정

**Prometheus:**
```promql
# DB 장애 알람
health_component_status{component="db"} == 0

# Slack 알림 설정
ALERT DatabaseDown
  IF health_component_status{component="db"} == 0
  FOR 1m
  ANNOTATIONS {
    summary = "Database is DOWN"
  }
```

---

## Kubernetes Probe 설정

### Probe 종류

| Probe | 용도 | 실패 시 동작 | 엔드포인트 |
|-------|------|-------------|-----------|
| **Liveness Probe** | Pod가 살아있는지 체크 | Pod 재시작 | `/actuator/health/liveness` |
| **Readiness Probe** | 트래픽 수신 가능한지 체크 | Service에서 제외 | `/actuator/health/readiness` |
| **Startup Probe** | 애플리케이션 시작 완료 체크 | Pod 재시작 | `/actuator/health/liveness` |

### Kubernetes Deployment 설정

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
    spec:
      containers:
      - name: order-service
        image: order-service:latest
        ports:
        - containerPort: 8080
        
        # Startup Probe (애플리케이션 시작 완료 체크)
        # 최대 30초 * 10회 = 5분 대기
        startupProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 10     # 시작 후 10초 대기
          periodSeconds: 3            # 3초마다 체크
          failureThreshold: 10        # 10번 실패 시 재시작
        
        # Liveness Probe (Pod 재시작 여부)
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 0      # Startup Probe 성공 후 즉시 시작
          periodSeconds: 10           # 10초마다 체크
          failureThreshold: 3         # 3번 실패 시 재시작
          timeoutSeconds: 3           # 3초 타임아웃
        
        # Readiness Probe (트래픽 수신 여부)
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 0      # Startup Probe 성공 후 즉시 시작
          periodSeconds: 5            # 5초마다 체크
          failureThreshold: 3         # 3번 실패 시 트래픽 차단
          timeoutSeconds: 3           # 3초 타임아웃
        
        # Graceful Shutdown
        lifecycle:
          preStop:
            exec:
              command: ["/bin/sh", "-c", "sleep 15"]
        
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

### Probe 전략

#### Startup Probe
- **목적**: 애플리케이션이 완전히 시작될 때까지 대기
- **설정**: `initialDelaySeconds: 10`, `periodSeconds: 3`, `failureThreshold: 10`
- **최대 대기 시간**: 10초 + (3초 × 10회) = 40초
- **실패 시**: Pod 재시작

#### Liveness Probe
- **목적**: 애플리케이션이 데드락 등으로 멈춰있는지 체크
- **설정**: `periodSeconds: 10`, `failureThreshold: 3`
- **실패 조건**: 30초 (10초 × 3회) 동안 응답 없음
- **실패 시**: Pod 재시작

#### Readiness Probe
- **목적**: 트래픽을 받을 준비가 되었는지 체크
- **설정**: `periodSeconds: 5`, `failureThreshold: 3`
- **실패 조건**: 15초 (5초 × 3회) 동안 NOT_READY
- **실패 시**: Service에서 제외 (트래픽 차단)

### Probe 흐름

```
1. Pod 시작
   ↓
2. Startup Probe 시작 (최대 40초)
   - /actuator/health/liveness 체크
   - 성공할 때까지 Liveness/Readiness Probe는 실행 안 됨
   ↓
3. Startup Probe 성공
   ↓
4. Liveness Probe + Readiness Probe 시작
   - Liveness: 10초마다 체크 (Pod 재시작 여부)
   - Readiness: 5초마다 체크 (트래픽 수신 여부)
   ↓
5. Readiness Probe UP
   - Service에 Pod 추가 → 트래픽 수신 시작
```

---

## Prometheus Metrics

### Prometheus 메트릭 수집

Production 환경에서는 Prometheus가 `/actuator/prometheus` 엔드포인트에서 메트릭을 수집합니다.

**Prometheus 설정 (prometheus.yml):**
```yaml
scrape_configs:
  - job_name: 'order-service'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['order-service:8080']
```

### 주요 메트릭

#### JVM 메트릭
- `jvm_memory_used_bytes` - JVM 메모리 사용량
- `jvm_memory_max_bytes` - JVM 최대 메모리
- `jvm_gc_pause_seconds_count` - GC 횟수
- `jvm_gc_pause_seconds_sum` - GC 시간
- `jvm_threads_live_threads` - 활성 스레드 수

#### HTTP 메트릭
- `http_server_requests_seconds_count` - HTTP 요청 수
- `http_server_requests_seconds_sum` - HTTP 요청 처리 시간
- `http_server_requests_seconds_max` - HTTP 최대 응답 시간

#### 시스템 메트릭
- `system_cpu_usage` - 시스템 CPU 사용률
- `process_cpu_usage` - 프로세스 CPU 사용률
- `system_load_average_1m` - 1분 평균 부하

#### HikariCP 메트릭
- `hikari_connections_active` - 활성 커넥션 수
- `hikari_connections_idle` - 유휴 커넥션 수
- `hikari_connections_pending` - 대기 중인 커넥션 수
- `hikari_connections_max` - 최대 커넥션 수

### Grafana 대시보드

Prometheus + Grafana로 실시간 모니터링:

**추천 대시보드:**
- [Spring Boot 2.1 Statistics](https://grafana.com/grafana/dashboards/10280)
- [JVM (Micrometer)](https://grafana.com/grafana/dashboards/4701)
- [HikariCP](https://grafana.com/grafana/dashboards/11681)

---

## Graceful Shutdown

### 설정

**application.yml:**
```yaml
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s

server:
  shutdown: graceful
```

### 동작 방식

1. **SIGTERM 신호 수신**
   ```
   Kubernetes가 Pod에 SIGTERM 전송
   ```

2. **새로운 요청 거부**
   ```
   Readiness Probe → NOT_READY
   Service에서 Pod 제거 (트래픽 차단)
   ```

3. **진행 중인 요청 완료 대기**
   ```
   최대 30초 동안 대기
   - 진행 중인 HTTP 요청 완료
   - 진행 중인 트랜잭션 커밋
   ```

4. **애플리케이션 종료**
   ```
   - DataSource 연결 종료
   - Thread Pool 종료
   - 리소스 정리
   ```

### Kubernetes와 연동

```yaml
# Deployment 설정
lifecycle:
  preStop:
    exec:
      command: ["/bin/sh", "-c", "sleep 15"]  # 15초 대기 (트래픽 완전히 차단)
```

**종료 흐름:**
```
1. Kubernetes가 Pod에 SIGTERM 전송
   ↓
2. preStop 실행 (15초 대기)
   - Readiness Probe → NOT_READY
   - Service에서 Pod 제거
   - 기존 연결에서 새 요청 차단
   ↓
3. Graceful Shutdown 시작 (최대 30초)
   - 진행 중인 요청 완료
   ↓
4. 애플리케이션 종료
```

**총 대기 시간:** 15초 (preStop) + 30초 (graceful) = 45초

---

## 환경별 설정

### Local 환경

**목적:** 개발/디버깅 편의

**application-local.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: '*'  # 모든 엔드포인트 노출
  
  endpoint:
    health:
      show-details: always  # 항상 상세 정보 노출
```

**노출 엔드포인트:**
- ✅ 모든 Actuator 엔드포인트
- ✅ Health Check 상세 정보
- ✅ 환경 변수, 로그 레벨 등

**접근 방법:**
```bash
# 모든 엔드포인트 목록
curl http://localhost:8080/actuator

# Health Check (상세 정보 포함)
curl http://localhost:8080/actuator/health | jq

# 환경 변수
curl http://localhost:8080/actuator/env | jq

# 로그 레벨 변경
curl -X POST http://localhost:8080/actuator/loggers/vroong.laas.order \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

### Production 환경

**목적:** 보안 및 성능

**application-prod.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus  # 필요한 것만 노출
  
  endpoint:
    health:
      show-details: never  # 상세 정보 숨김
  
  metrics:
    export:
      prometheus:
        enabled: true
        step: 1m  # 1분마다 메트릭 수집
```

**노출 엔드포인트:**
- ✅ `/actuator/health` (상세 정보 없음)
- ✅ `/actuator/info`
- ✅ `/actuator/prometheus` (Prometheus 전용)
- ❌ 기타 모든 엔드포인트 (보안)

**접근 방법:**
```bash
# Health Check (상태만)
curl https://order-api.example.com/actuator/health
# → {"status":"UP"}

# Prometheus 메트릭
curl https://order-api.example.com/actuator/prometheus
```

---

## 모니터링 대시보드

### CloudWatch 알림 (AWS)

**CloudWatch Alarm 설정:**

```yaml
# CloudWatch Alarm - Health Check 실패
AlarmName: order-service-health-check-failed
MetricName: HealthCheckStatus
Threshold: 1
EvaluationPeriods: 2
DatapointsToAlarm: 2
ComparisonOperator: LessThanThreshold
```

### Prometheus + Grafana

**Prometheus 쿼리 예시:**

```promql
# HTTP 요청 성공률
sum(rate(http_server_requests_seconds_count{status=~"2.."}[5m])) 
/ 
sum(rate(http_server_requests_seconds_count[5m])) * 100

# P95 응답 시간
histogram_quantile(0.95, 
  sum(rate(http_server_requests_seconds_bucket[5m])) by (le, uri)
)

# HikariCP 커넥션 사용률
hikari_connections_active / hikari_connections_max * 100

# JVM 메모리 사용률
jvm_memory_used_bytes{area="heap"} 
/ 
jvm_memory_max_bytes{area="heap"} * 100
```

### Slack 알림 설정

**AlertManager 설정:**
```yaml
route:
  receiver: 'slack-notifications'
  group_by: ['alertname']
  
receivers:
  - name: 'slack-notifications'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/YOUR/WEBHOOK/URL'
        channel: '#order-service-alerts'
        title: '{{ range .Alerts }}{{ .Annotations.summary }}{{ end }}'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
```

---

## 문제 해결

### Health Check가 DOWN인 경우

**1. DB 연결 실패**
```bash
# Health Check 상세 확인
curl http://localhost:8080/actuator/health | jq

# DB 상태
{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN",
      "details": {
        "error": "CannotGetJdbcConnectionException"
      }
    }
  }
}

# 해결 방법:
# - DB 서비스 확인 (MySQL/Aurora 실행 중?)
# - 환경 변수 확인 (DB_USERNAME, DB_PASSWORD)
# - 네트워크 확인 (Security Group, VPC)
```

**2. Disk Space 부족**
```bash
{
  "status": "DOWN",
  "components": {
    "diskSpace": {
      "status": "DOWN",
      "details": {
        "free": 5242880,
        "threshold": 10485760
      }
    }
  }
}

# 해결 방법:
# - 디스크 공간 확보
# - 로그 파일 정리
# - 임계값 조정 (application.yml)
```

### Kubernetes Pod가 재시작되는 경우

**원인 확인:**
```bash
# Pod 이벤트 확인
kubectl describe pod order-service-xxx

# 마지막 로그 확인
kubectl logs order-service-xxx --previous

# Probe 실패 확인
Events:
  Type     Reason     Message
  ----     ------     -------
  Warning  Unhealthy  Liveness probe failed: HTTP probe failed with statuscode: 503
```

**해결 방법:**
1. Startup Probe 시간 증가
2. Liveness Probe failureThreshold 증가
3. 애플리케이션 시작 시간 최적화

---

## 참고 자료

- [Spring Boot Actuator 공식 문서](https://docs.spring.io/spring-boot/reference/actuator/index.html)
- [Micrometer 공식 문서](https://micrometer.io/docs)
- [Kubernetes Probe 가이드](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
- [Prometheus 공식 문서](https://prometheus.io/docs/)
- [Grafana 대시보드](https://grafana.com/grafana/dashboards/)

---

## 다음 단계

1. **[AWS Aurora MySQL 설정](./aws-aurora-setup.md)** - Production 환경 배포
2. **[Flyway 마이그레이션](./flyway-guide.md)** - DB 스키마 관리
3. **[아키텍처](./architecture.md)** - 전체 시스템 구조
