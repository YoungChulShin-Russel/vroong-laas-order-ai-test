# AWS IAM Database Authentication 설정 가이드

## 개요

AWS Advanced JDBC Driver의 IAM 인증을 사용하면:
- ✅ DB 비밀번호 불필요 (IAM 토큰 자동 생성/갱신)
- ✅ 15분마다 자동 토큰 갱신
- ✅ IAM 정책으로 접근 제어
- ✅ CloudTrail로 접근 감사

---

## 1. Aurora MySQL IAM 인증 활성화

### 1.1 기존 클러스터에 IAM 인증 활성화

```bash
# AWS CLI로 클러스터 수정
aws rds modify-db-cluster \
  --db-cluster-identifier order-cluster \
  --enable-iam-database-authentication \
  --apply-immediately
```

### 1.2 신규 클러스터 생성 시

```bash
aws rds create-db-cluster \
  --db-cluster-identifier order-cluster \
  --engine aurora-mysql \
  --engine-version 8.0.mysql_aurora.3.05.2 \
  --master-username admin \
  --master-user-password <temporary-password> \
  --enable-iam-database-authentication  # ⭐ IAM 인증 활성화
```

---

## 2. Aurora MySQL DB 사용자 생성

### 2.1 DB에 IAM 인증용 사용자 생성

```sql
-- MySQL에 접속 (임시 비밀번호 사용)
mysql -h your-cluster.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com \
      -u admin \
      -p

-- IAM 인증 전용 사용자 생성
CREATE USER 'order_user' IDENTIFIED WITH AWSAuthenticationPlugin AS 'RDS';

-- 권한 부여
GRANT SELECT, INSERT, UPDATE, DELETE ON order.* TO 'order_user';
GRANT CREATE TEMPORARY TABLES ON order.* TO 'order_user';

-- 권한 확인
SHOW GRANTS FOR 'order_user';

-- 결과:
-- GRANT USAGE ON *.* TO `order_user`@`%` IDENTIFIED WITH AWSAuthenticationPlugin AS 'RDS'
-- GRANT SELECT, INSERT, UPDATE, DELETE, CREATE TEMPORARY TABLES ON `order`.* TO `order_user`@`%`
```

**중요:**
- `IDENTIFIED WITH AWSAuthenticationPlugin AS 'RDS'` → IAM 인증 사용
- 일반 비밀번호 인증과 병행 불가 (IAM 전용)

---

## 3. IAM Role 생성 (EKS Pod용)

### 3.1 Trust Policy (IRSA - IAM Role for Service Account)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::123456789012:oidc-provider/oidc.eks.ap-northeast-2.amazonaws.com/id/EXAMPLED539D4633E53DE1B71EXAMPLE"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "oidc.eks.ap-northeast-2.amazonaws.com/id/EXAMPLED539D4633E53DE1B71EXAMPLE:sub": "system:serviceaccount:order-service:order-api-sa"
        }
      }
    }
  ]
}
```

### 3.2 IAM Policy (RDS Connect 권한)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "rds-db:connect"
      ],
      "Resource": [
        "arn:aws:rds-db:ap-northeast-2:123456789012:dbuser:cluster-ABCDEFGHIJKLMNOP/order_user"
      ]
    }
  ]
}
```

**Resource ARN 형식:**
```
arn:aws:rds-db:<region>:<account-id>:dbuser:<db-cluster-resource-id>/<db-username>
```

**DB Cluster Resource ID 확인:**
```bash
aws rds describe-db-clusters \
  --db-cluster-identifier order-cluster \
  --query 'DBClusters[0].DbClusterResourceId' \
  --output text

# 출력: cluster-ABCDEFGHIJKLMNOP
```

### 3.3 IAM Role 생성

```bash
# Policy 생성
aws iam create-policy \
  --policy-name OrderServiceRDSConnectPolicy \
  --policy-document file://rds-connect-policy.json

# Role 생성
aws iam create-role \
  --role-name OrderServiceRDSConnectRole \
  --assume-role-policy-document file://trust-policy.json

# Policy를 Role에 연결
aws iam attach-role-policy \
  --role-name OrderServiceRDSConnectRole \
  --policy-arn arn:aws:iam::123456789012:policy/OrderServiceRDSConnectPolicy
```

---

## 4. Kubernetes Service Account 설정

### 4.1 ServiceAccount 생성

```yaml
# k8s/service-account.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: order-api-sa
  namespace: order-service
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::123456789012:role/OrderServiceRDSConnectRole
```

```bash
kubectl apply -f k8s/service-account.yaml
```

### 4.2 Deployment에 ServiceAccount 연결

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-api
  namespace: order-service
spec:
  template:
    spec:
      serviceAccountName: order-api-sa  # ⭐ ServiceAccount 연결
      containers:
      - name: order-api
        image: order-api:latest
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_CLUSTER_ENDPOINT
          value: "order-cluster.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com"
        - name: DB_USERNAME
          value: "order_user"
        # DB_PASSWORD는 설정하지 않음 (IAM 토큰 자동 생성)
```

---

## 5. application-prod.yml 설정 (완료됨)

```yaml
spring:
  datasource:
    driver-class-name: software.amazon.jdbc.Driver
    url: jdbc:mysql:aws://order-cluster.cluster-xxxxx.ap-northeast-2.rds.amazonaws.com:3306/order?wrapperPlugins=iam,readWriteSplitting,failover
    username: order_user
    # password는 설정하지 않음 (IAM 토큰 자동 생성)
```

**AWS Driver가 자동으로:**
1. IAM Role에서 임시 자격증명 획득
2. RDS IAM 토큰 생성 (15분 유효)
3. 토큰으로 DB 연결
4. 15분마다 자동 갱신

---

## 6. 동작 확인

### 6.1 로컬 테스트 (AWS CLI Profile 사용)

```bash
# AWS CLI Profile 설정
export AWS_PROFILE=order-service-dev

# 애플리케이션 실행
./gradlew :api:bootRun --args='--spring.profiles.active=prod'

# 로그 확인
# [software.amazon.jdbc] IAM authentication token generated successfully
# [software.amazon.jdbc] Connected to Writer instance: order-cluster-instance-1
```

### 6.2 Production 배포 후 확인

```bash
# Pod 로그 확인
kubectl logs -f deployment/order-api -n order-service

# IAM 인증 성공 로그:
# [software.amazon.jdbc] IAM authentication token generated successfully
# [software.amazon.jdbc] Connection established to cluster: order-cluster

# DB 접속 확인
kubectl exec -it deployment/order-api -n order-service -- \
  curl localhost:8080/actuator/health

# 응답:
# {"status":"UP","components":{"db":{"status":"UP"}}}
```

### 6.3 CloudTrail로 접근 감사

```bash
# RDS 접근 이벤트 확인
aws cloudtrail lookup-events \
  --lookup-attributes AttributeKey=ResourceType,AttributeValue=AWS::RDS::DBCluster \
  --max-results 10

# IAM 토큰 생성 이벤트 확인
aws cloudtrail lookup-events \
  --lookup-attributes AttributeKey=EventName,AttributeValue=GenerateDataKeyWithoutPlaintext
```

---

## 7. 트러블슈팅

### 7.1 "Access denied for user" 에러

```
Error: Access denied for user 'order_user'@'...' (using password: YES)
```

**원인:**
- DB 사용자가 `AWSAuthenticationPlugin`으로 생성되지 않음

**해결:**
```sql
-- 기존 사용자 삭제
DROP USER IF EXISTS 'order_user';

-- IAM 인증용으로 재생성
CREATE USER 'order_user' IDENTIFIED WITH AWSAuthenticationPlugin AS 'RDS';
GRANT SELECT, INSERT, UPDATE, DELETE ON order.* TO 'order_user';
```

### 7.2 "IAM token generation failed" 에러

```
Error: Failed to generate IAM authentication token
```

**원인:**
- IAM Role에 `rds-db:connect` 권한 없음
- ServiceAccount에 IAM Role 연결 안 됨

**해결:**
```bash
# IAM Role 권한 확인
aws iam get-role-policy \
  --role-name OrderServiceRDSConnectRole \
  --policy-name OrderServiceRDSConnectPolicy

# ServiceAccount 확인
kubectl describe sa order-api-sa -n order-service
# Annotations에 eks.amazonaws.com/role-arn 있는지 확인
```

### 7.3 "Connection timeout" 에러

**원인:**
- Security Group에서 EKS Pod IP 허용 안 됨

**해결:**
```bash
# Aurora Security Group에 EKS Node Security Group 추가
aws ec2 authorize-security-group-ingress \
  --group-id sg-aurora-xxxxx \
  --source-group sg-eks-node-xxxxx \
  --protocol tcp \
  --port 3306
```

---

## 8. 보안 Best Practices

### 8.1 최소 권한 원칙

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "rds-db:connect",
      "Resource": "arn:aws:rds-db:*:*:dbuser:cluster-ABCDEFGHIJKLMNOP/order_user"
      // ⚠️ 특정 사용자만 지정 (와일드카드 금지)
    }
  ]
}
```

### 8.2 Connection Lifetime 설정

```yaml
hikari:
  max-lifetime: 1800000  # 30분 (IAM 토큰 15분 유효 × 2)
  # Connection이 30분마다 교체되면서 새 토큰으로 재연결
```

### 8.3 SSL/TLS 강제

```yaml
url: jdbc:mysql:aws://...?useSSL=true&requireSSL=true
# ⭐ SSL 필수 (IAM 토큰은 암호화 전송 필요)
```

---

## 9. 비용 고려사항

### 9.1 IAM 인증 비용

- ✅ **IAM 토큰 생성: 무료**
- ✅ **CloudTrail 로깅: 무료** (관리 이벤트)
- ⚠️ Connection 재연결 빈도 증가 (max-lifetime 최적화 필요)

### 9.2 Connection Pool 최적화

```yaml
hikari:
  maximum-pool-size: 50  # IAM 인증은 Connection 재사용 중요
  minimum-idle: 10
  max-lifetime: 1800000  # 30분 (너무 짧으면 재연결 빈번)
```

---

## 참고 자료

- [AWS IAM Database Authentication](https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/UsingWithRDS.IAMDBAuth.html)
- [AWS Advanced JDBC Driver - IAM Plugin](https://github.com/aws/aws-advanced-jdbc-wrapper/wiki/UsingTheIamAuthenticationPlugin)
- [EKS IRSA (IAM Roles for Service Accounts)](https://docs.aws.amazon.com/eks/latest/userguide/iam-roles-for-service-accounts.html)
