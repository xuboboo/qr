CREATE TABLE IF NOT EXISTS qr_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    target_url VARCHAR(2000) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS qr_visit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    qr_code VARCHAR(64) NOT NULL,
    ip VARCHAR(64),
    user_agent VARCHAR(1000),
    referer VARCHAR(2000),
    visit_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_qr_code_code ON qr_code(code);
CREATE INDEX IF NOT EXISTS idx_qr_visit_code ON qr_visit(qr_code);
CREATE INDEX IF NOT EXISTS idx_qr_visit_date ON qr_visit(visit_date);
CREATE INDEX IF NOT EXISTS idx_qr_visit_code_date ON qr_visit(qr_code, visit_date);

-- 新增 location 字段（幂等）
ALTER TABLE qr_visit ADD COLUMN IF NOT EXISTS location VARCHAR(200);
