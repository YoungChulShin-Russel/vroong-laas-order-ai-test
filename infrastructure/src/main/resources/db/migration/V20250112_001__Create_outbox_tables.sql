-- ===================================
-- Outbox Pattern Tables
-- Created: 2025-01-12
-- ===================================

-- ===================================
-- 1. outbox_events 테이블 (메인 Outbox)
-- ===================================
CREATE TABLE outbox_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    event_id VARCHAR(100) NOT NULL UNIQUE COMMENT '이벤트 ID (UUID)',
    event_type VARCHAR(100) NOT NULL COMMENT '이벤트 타입 (ORDER_CREATED, ORDER_CANCELLED 등)',
    event_key VARCHAR(100) NOT NULL COMMENT '이벤트 키 (파티셔닝용, 주문번호 등)',
    payload TEXT NOT NULL COMMENT '이벤트 페이로드 (JSON)',
    status VARCHAR(20) NOT NULL COMMENT '상태: PENDING, PUBLISHED, FAILED, DLQ',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    last_error TEXT NULL COMMENT '마지막 에러 메시지',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    published_at TIMESTAMP NULL COMMENT '발행 완료 시각',
    
    -- 인덱스
    INDEX idx_status_id (status, id),
    INDEX idx_event_id (event_id),
    INDEX idx_created_at (created_at),
    INDEX idx_published_at (published_at),
    INDEX idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Outbox 이벤트 (메인)';

-- ===================================
-- 2. outbox_events_dlq 테이블 (Dead Letter Queue)
-- ===================================
CREATE TABLE outbox_events_dlq (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'PK',
    event_id VARCHAR(100) NOT NULL COMMENT '이벤트 ID (원본)',
    event_type VARCHAR(100) NOT NULL COMMENT '이벤트 타입',
    event_key VARCHAR(100) NOT NULL COMMENT '이벤트 키',
    payload TEXT NOT NULL COMMENT '이벤트 페이로드 (JSON)',
    retry_count INT NOT NULL COMMENT '재시도 횟수 (실패 시점)',
    last_error TEXT NOT NULL COMMENT '마지막 에러 메시지',
    created_at TIMESTAMP NOT NULL COMMENT '원본 생성 시각',
    failed_at TIMESTAMP NOT NULL COMMENT '실패 시각',
    dlq_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'DLQ 이동 시각',
    
    -- 인덱스
    INDEX idx_event_id (event_id),
    INDEX idx_dlq_created_at (dlq_created_at),
    INDEX idx_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Outbox 이벤트 DLQ (실패 이벤트)';

