# AWS Aurora MySQL 설정 가이드

## 📋 목차

1. [Aurora Cluster 생성](#1-aurora-cluster-생성)
2. [Endpoint 확인](#2-endpoint-확인)
3. [환경 변수 설정](#3-환경-변수-설정)
4. [배포 체크리스트](#4-배포-체크리스트)
5. [Failover 테스트](#5-failover-테스트)
6. [모니터링 설정](#6-모니터링-설정)
7. [트러블슈팅](#7-트러블슈팅)

---

## 1. Aurora Cluster 생성

### AWS Console에서 생성

1. **RDS → Create database**
2. **Engine options**
   - Engine type: **Amazon Aurora**
   - Edition: **Aurora (MySQL Compatible)**
   - Version: **Aurora MySQL 3.x (MySQL 8.0 compatible)** 권장

3. **Templates**
   - Production (운영 환경)
   - Dev/Test (개발/테스트 환경)

4. **Settings**
   - DB cluster identifier: `order-cluster` (예시)
   - Master username: `admin` (예시)
   - Master password: 강력한 비밀번호 설정

5. **DB instance class**
   - **일 30만건 트래픽 기준:**
     - Writer: `db.r6g.large` (2 vCPU, 16 GB RAM)
     - Reader: `db.r6g.large` × 2대 이상

6. **Availability & durability**
   - Create an Aurora Replica: **Yes**
   - Number of replicas: **2개 이상** 권장

7. **Connectivity**
   - VPC: 애플리케이션과 같은 VPC
   - Subnet group: Private subnet 권장
   - Public access: **No** (보안)
   - VPC security group: RDS 전용 Security Group

8. **Additional configuration**
   - Initial database name: `order`
   - DB cluster parameter group: 필요시 커스텀
   - Backup retention period: **7일** 이상 권장
   - Enable Enhanced Monitoring: **Yes** (60초)

---

## 2. Endpoint 확인

### Cluster 생성 후 Endpoint 확인

AWS Console → RDS → Databases → `order-cluster`

#### 1. Cluster Endpoint (Writer)
```
order-cluster.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com
```
- **용도:** 쓰기 작업 (INSERT, UPDATE, DELETE)
- **특징:** 항상 Primary Instance를 가리킴

#### 2. Reader Endpoint
```
order-cluster.cluster-ro-xxxxx.ap-northeast-2.rds.amazonaws.com
```
- **용도:** 읽기 작업 (SELECT)
- **특징:** 여러 Read Replica에 자동 로드밸런싱

#### 3. Instance Endpoints (참고용)
```
order-cluster-instance-1.xxxxx.ap-northeast-2.rds.amazonaws.com
order-cluster-instance-2.xxxxx.ap-northeast-2.rds.amazonaws.com
```
- **용도:** 특정 인스턴스 직접 접근 (일반적으로 사용 안 함)

---

## 3. 환경 변수 설정

### Production 환경 변수

```bash
# Writer Endpoint
export DB_WRITER_ENDPOINT="order-cluster.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com"

# Reader Endpoint
export DB_READER_ENDPOINT="order-cluster.cluster-ro-xxxxx.ap-northeast-2.rds.amazonaws.com"

# Credentials
export DB_USERNAME="admin"
export DB_PASSWORD="your-strong-password"
```

### Kubernetes ConfigMap/Secret (권장)

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: order-service-config
data:
  DB_WRITER_ENDPOINT: "order-cluster.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com"
  DB_READER_ENDPOINT: "order-cluster.cluster-ro-xxxxx.ap-northeast-2.rds.amazonaws.com"

---
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: order-service-secret
type: Opaque
stringData:
  DB_USERNAME: "admin"
  DB_PASSWORD: "your-strong-password"
```

### AWS Secrets Manager 사용 (더 안전)

1. **Secret 생성**
   ```bash
   aws secretsmanager create-secret \
     --name order-service/database \
     --secret-string '{
       "username": "admin",
       "password": "your-strong-password",
       "writerEndpoint": "order-cluster.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com",
       "readerEndpoint": "order-cluster.cluster-ro-xxxxx.ap-northeast-2.rds.amazonaws.com"
     }'
   ```

2. **애플리케이션에서 조회**
   - Spring Cloud AWS 사용
   - Startup 시 Secret 로드

---

## 4. 배포 체크리스트

### 배포 전 확인 사항

- [ ] Aurora Cluster 생성 완료
- [ ] Writer/Reader Endpoint 확인
- [ ] Security Group 설정 (애플리케이션 → RDS 허용)
- [ ] 환경 변수 설정 완료
- [ ] Flyway 마이그레이션 스크립트 준비
- [ ] Connection Pool 설정 확인 (Writer: 20, Reader: 50)
- [ ] SSL 인증서 확인 (RDS CA 인증서)

### 배포 후 확인 사항

- [ ] 애플리케이션 정상 시작
- [ ] Writer Pool Connection 정상
- [ ] Reader Pool Connection 정상
- [ ] `@Transactional(readOnly=true)` → Reader 라우팅 확인
- [ ] `@Transactional` → Writer 라우팅 확인
- [ ] HikariCP 메트릭 정상
- [ ] 로그에 오류 없음

---

## 5. Failover 테스트

### Writer Failover 테스트

#### 1. 수동 Failover

```bash
# AWS CLI로 Failover 실행
aws rds failover-db-cluster \
  --db-cluster-identifier order-cluster \
  --target-db-instance-identifier order-cluster-instance-2
```

#### 2. 예상 동작

```
1. Primary Instance 장애 감지 (1-2초)
2. AWS Advanced JDBC Wrapper가 자동으로 새로운 Writer 감지
3. Connection Pool 재연결
4. 애플리케이션 정상 작동 (다운타임 1-2초)
```

#### 3. 로그 확인

```
2025-01-07 12:00:00 INFO software.aws.rds - Failover detected
2025-01-07 12:00:01 INFO software.aws.rds - New writer: order-cluster-instance-2
2025-01-07 12:00:02 INFO HikariPool - Connection pool reinitialized
```

### Reader Failover 테스트

```
1. Read Replica 1대 중단
2. Reader Endpoint가 자동으로 남은 Replica로 트래픽 분산
3. 애플리케이션에는 영향 없음 (투명)
```

---

## 6. 모니터링 설정

### CloudWatch Metrics

#### RDS 메트릭 (필수)

- **DatabaseConnections** (Writer/Reader 각각)
  - Threshold: Writer 80%, Reader 90%
- **CPUUtilization**
  - Threshold: 80%
- **FreeableMemory**
  - Threshold: < 1GB
- **ReadLatency / WriteLatency**
  - Threshold: > 100ms

#### Application 메트릭

```yaml
# HikariCP Metrics
management:
  metrics:
    enable:
      hikari: true
  endpoints:
    web:
      exposure:
        include: prometheus
```

**Prometheus Metrics:**
- `hikari_connections_active{pool="OrderWriterPool"}`
- `hikari_connections_active{pool="OrderReaderPool"}`
- `hikari_connections_idle{pool="OrderWriterPool"}`
- `hikari_connections_idle{pool="OrderReaderPool"}`

### CloudWatch Alarms

```bash
# Writer Connection Pool 알람
aws cloudwatch put-metric-alarm \
  --alarm-name order-writer-pool-high \
  --metric-name hikari_connections_active \
  --namespace OrderService \
  --statistic Average \
  --period 60 \
  --threshold 16 \
  --comparison-operator GreaterThanThreshold \
  --evaluation-periods 2
```

---

## 7. 트러블슈팅

### 문제 1: Connection Timeout

**증상:**
```
java.sql.SQLTransientConnectionException: Communications link failure
```

**원인:**
1. Security Group 설정 오류
2. Endpoint 주소 오류
3. 네트워크 문제

**해결:**
```bash
# 1. Security Group 확인
# RDS Security Group이 애플리케이션 Security Group으로부터 3306 포트 허용하는지 확인

# 2. Endpoint 접근 테스트
telnet order-cluster.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com 3306

# 3. MySQL Client로 직접 연결 테스트
mysql -h order-cluster.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com \
      -u admin -p order
```

---

### 문제 2: readOnly 라우팅 안 됨

**증상:**
```
@Transactional(readOnly=true)인데 Writer로 연결됨
```

**원인:**
LazyConnectionDataSourceProxy가 없거나 설정 오류

**확인:**
```java
// ProductionDataSourceConfig에서 확인
@Bean
@Primary
public DataSource dataSource(...) {
    return new LazyConnectionDataSourceProxy(routingDataSource);
    // ↑ 이것이 있어야 함!
}
```

---

### 문제 2-1: @Transactional 없는 조회가 Writer로 감

**증상:**
```
@Transactional 없는 조회인데 Writer Pool 사용됨
```

**원인:**
Default DataSource가 WRITE로 설정됨

**확인:**
```java
// ProductionDataSourceConfig에서 확인
routingDataSource.setDefaultTargetDataSource(readerDataSource);
// ↑ READ가 Default여야 함!
```

**올바른 동작:**
- @Transactional 없음 → READ Pool (자동 최적화)
- @Transactional → WRITE Pool
- @Transactional(readOnly=true) → READ Pool

---

### 문제 3: Too many connections

**증상:**
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: 
Too many connections
```

**원인:**
Connection Pool 설정이 Aurora max_connections보다 큼

**해결:**
```yaml
# Aurora max_connections 확인 (기본: 1000)
# Writer Pool (20) + Reader Pool (50) = 70
# 여유있게 설정되어야 함

order:
  datasource:
    write:
      hikari:
        maximum-pool-size: 20  # ← 조정
    read:
      hikari:
        maximum-pool-size: 50  # ← 조정
```

---

### 문제 4: Failover가 느림 (60초+)

**증상:**
Writer Failover 시 1분 이상 다운타임

**원인:**
AWS Advanced JDBC Wrapper 설정 누락

**확인:**
```yaml
# wrapperPlugins 확인
jdbc-url: jdbc:aws-wrapper:mysql://...?wrapperPlugins=failover,efm2
#                                                    ^^^^^^^^ 필수!
```

---

## 참고 자료

- [AWS Aurora MySQL Documentation](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/Aurora.AuroraMySQL.html)
- [AWS Advanced JDBC Wrapper GitHub](https://github.com/aws/aws-advanced-jdbc-wrapper)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Spring Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)

---

## 추가 최적화

### Read Replica Auto Scaling

```bash
# CloudWatch Metrics 기반 Auto Scaling 설정
# CPU 70% 이상 시 Replica 자동 추가 (최대 5대)
aws application-autoscaling register-scalable-target \
  --service-namespace rds \
  --resource-id cluster:order-cluster \
  --scalable-dimension rds:cluster:ReadReplicaCount \
  --min-capacity 2 \
  --max-capacity 5
```

### Query Performance Insights

```bash
# Performance Insights 활성화
# 쿼리 성능 분석 및 최적화
aws rds modify-db-cluster \
  --db-cluster-identifier order-cluster \
  --enable-performance-insights \
  --performance-insights-retention-period 7
```

---

**Production 배포 전 이 문서를 다시 한 번 확인하세요!** ✅
