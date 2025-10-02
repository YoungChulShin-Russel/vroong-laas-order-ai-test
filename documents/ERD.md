# Order Service ERD

## 엔티티 관계도

```mermaid
erDiagram
    orders {
        bigint id PK
        varchar order_number UK "주문번호"
        varchar status "주문 상태"
        timestamp ordered_at "주문 일시"
        timestamp delivered_at "배송 완료 일시"
        timestamp cancelled_at "취소 일시"
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    order_items {
        bigint id PK
        bigint order_id FK "주문 ID"
        varchar item_name "상품명"
        int quantity "수량"
        decimal price "가격"
        varchar category "카테고리"
        decimal weight "무게"
        decimal volume_length "길이"
        decimal volume_width "너비"
        decimal volume_height "높이"
        decimal volume_cbm "CBM"
        timestamp created_at
        timestamp updated_at
    }

    order_locations {
        bigint id PK
        bigint order_id FK "주문 ID"
        varchar origin_contact_name "출발지 연락처명"
        varchar origin_contact_phone_number "출발지 전화번호"
        varchar origin_entrance_password "출발지 출입 비밀번호"
        varchar origin_entrance_guide "출발지 출입 안내"
        varchar origin_request_message "출발지 요청사항"
        decimal origin_latitude "출발지 위도"
        decimal origin_longitude "출발지 경도"
        varchar origin_jibnun_address "출발지 지번주소"
        varchar origin_road_address "출발지 도로명주소"
        varchar origin_detail_address "출발지 상세주소"
        varchar destination_contact_name "도착지 연락처명"
        varchar destination_contact_phone_number "도착지 전화번호"
        varchar destination_entrance_password "도착지 출입 비밀번호"
        varchar destination_entrance_guide "도착지 출입 안내"
        varchar destination_request_message "도착지 요청사항"
        decimal destination_latitude "도착지 위도"
        decimal destination_longitude "도착지 경도"
        varchar destination_jibnun_address "도착지 지번주소"
        varchar destination_road_address "도착지 도로명주소"
        varchar destination_detail_address "도착지 상세주소"
        timestamp created_at
        timestamp updated_at
    }

    order_delivery_policies {
        bigint id PK
        bigint order_id FK "주문 ID"
        text delivery_policy_json "배송 정책 JSON"
        timestamp created_at
        timestamp updated_at
    }

    orders ||--o{ order_items : "1:N"
    orders ||--|| order_locations : "1:1"
    orders ||--|| order_delivery_policies : "1:1"
```

## 테이블 설명

### orders (주문)
- 주문의 기본 정보를 저장
- `order_number`: 비즈니스 키로 사용되는 주문번호
- `status`: 주문 상태 (ENUM)
- 주문/배송완료/취소 시간 추적

### order_items (주문 상품)
- 주문에 포함된 상품들의 정보
- 상품의 물리적 특성(무게, 부피) 포함
- 1개 주문에 여러 상품 가능 (1:N 관계)

### order_locations (주문 위치)
- 주문의 출발지와 도착지 정보
- 연락처, 주소, 위치정보, 출입안내 등 포함
- 1개 주문당 1개 위치정보 (1:1 관계)

### order_delivery_policies (주문 배송 정책)
- 배송 정책을 JSON 형태로 저장
- 복잡한 배송 규칙을 유연하게 저장
- 1개 주문당 1개 배송정책 (1:1 관계)

## 관계 설명

1. **orders ↔ order_items**: 1:N 관계
   - 하나의 주문에 여러 상품이 포함될 수 있음

2. **orders ↔ order_locations**: 1:1 관계
   - 하나의 주문에 하나의 위치정보(출발지+도착지)

3. **orders ↔ order_delivery_policies**: 1:1 관계
   - 하나의 주문에 하나의 배송정책