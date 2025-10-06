-- ===================================
-- Order Aggregate (주문 도메인)
-- Created: 2025-01-06
-- ===================================

-- ===================================
-- 1. orders 테이블 (주문)
-- ===================================
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    order_number VARCHAR(50) NOT NULL UNIQUE COMMENT '주문번호',
    status VARCHAR(30) NOT NULL COMMENT '주문상태: CREATED, ASSIGNED, DELIVERED, CANCELLED',
    ordered_at TIMESTAMP NOT NULL COMMENT '주문 생성 시각',
    delivered_at TIMESTAMP NULL COMMENT '배송 완료 시각',
    cancelled_at TIMESTAMP NULL COMMENT '취소 시각',
    
    -- BaseEntity 컬럼
    entity_status VARCHAR(20) NOT NULL COMMENT 'Entity 상태: ACTIVE, DELETED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    
    -- ConcurrentEntity 컬럼 (낙관적 락)
    version BIGINT NOT NULL DEFAULT 0 COMMENT '버전 (낙관적 락)',
    
    INDEX idx_order_number (order_number),
    INDEX idx_status (status),
    INDEX idx_ordered_at (ordered_at),
    INDEX idx_entity_status (entity_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='주문';

-- ===================================
-- 2. order_items 테이블 (주문 아이템)
-- ===================================
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    order_id BIGINT NOT NULL COMMENT '주문 ID (FK)',
    item_name VARCHAR(200) NOT NULL COMMENT '상품명',
    quantity INT NOT NULL COMMENT '수량',
    price DECIMAL(19, 2) NOT NULL COMMENT '단가',
    category VARCHAR(100) NULL COMMENT '카테고리',
    
    -- Weight (무게)
    weight DECIMAL(10, 3) NULL COMMENT '무게 (kg)',
    
    -- Volume (부피)
    volume_length DECIMAL(10, 2) NULL COMMENT '길이 (cm)',
    volume_width DECIMAL(10, 2) NULL COMMENT '너비 (cm)',
    volume_height DECIMAL(10, 2) NULL COMMENT '높이 (cm)',
    volume_cbm DECIMAL(10, 4) NULL COMMENT 'CBM (m³)',
    
    -- BaseEntity 컬럼
    entity_status VARCHAR(20) NOT NULL COMMENT 'Entity 상태: ACTIVE, DELETED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    
    INDEX idx_order_id (order_id),
    INDEX idx_entity_status (entity_status),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='주문 아이템';

-- ===================================
-- 3. order_locations 테이블 (주문 위치 정보)
-- ===================================
CREATE TABLE order_locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    order_id BIGINT NOT NULL COMMENT '주문 ID (FK)',
    
    -- 출발지 (Origin) 정보
    origin_contact_name VARCHAR(100) NULL COMMENT '출발지 연락처 이름',
    origin_contact_phone_number VARCHAR(20) NULL COMMENT '출발지 연락처 전화번호',
    origin_entrance_password VARCHAR(50) NULL COMMENT '출발지 출입 비밀번호',
    origin_entrance_guide VARCHAR(500) NULL COMMENT '출발지 출입 안내',
    origin_request_message VARCHAR(1000) NULL COMMENT '출발지 요청사항',
    origin_latitude DECIMAL(10, 7) NULL COMMENT '출발지 위도',
    origin_longitude DECIMAL(10, 7) NULL COMMENT '출발지 경도',
    origin_jibnun_address VARCHAR(300) NULL COMMENT '출발지 지번주소',
    origin_road_address VARCHAR(300) NULL COMMENT '출발지 도로명주소',
    origin_detail_address VARCHAR(300) NULL COMMENT '출발지 상세주소',
    
    -- 도착지 (Destination) 정보
    destination_contact_name VARCHAR(100) NULL COMMENT '도착지 연락처 이름',
    destination_contact_phone_number VARCHAR(20) NULL COMMENT '도착지 연락처 전화번호',
    destination_entrance_password VARCHAR(50) NULL COMMENT '도착지 출입 비밀번호',
    destination_entrance_guide VARCHAR(500) NULL COMMENT '도착지 출입 안내',
    destination_request_message VARCHAR(1000) NULL COMMENT '도착지 요청사항',
    destination_latitude DECIMAL(10, 7) NULL COMMENT '도착지 위도',
    destination_longitude DECIMAL(10, 7) NULL COMMENT '도착지 경도',
    destination_jibnun_address VARCHAR(300) NULL COMMENT '도착지 지번주소',
    destination_road_address VARCHAR(300) NULL COMMENT '도착지 도로명주소',
    destination_detail_address VARCHAR(300) NULL COMMENT '도착지 상세주소',
    
    -- BaseEntity 컬럼
    entity_status VARCHAR(20) NOT NULL COMMENT 'Entity 상태: ACTIVE, DELETED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    
    INDEX idx_order_id (order_id),
    INDEX idx_entity_status (entity_status),
    INDEX idx_origin_latlng (origin_latitude, origin_longitude),
    INDEX idx_destination_latlng (destination_latitude, destination_longitude),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='주문 위치 정보';

-- ===================================
-- 4. order_delivery_policies 테이블 (배송 정책)
-- ===================================
CREATE TABLE order_delivery_policies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    order_id BIGINT NOT NULL COMMENT '주문 ID (FK)',
    delivery_policy_json TEXT NOT NULL COMMENT '배송 정책 JSON',
    
    -- BaseEntity 컬럼
    entity_status VARCHAR(20) NOT NULL COMMENT 'Entity 상태: ACTIVE, DELETED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    
    INDEX idx_order_id (order_id),
    INDEX idx_entity_status (entity_status),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='배송 정책';
