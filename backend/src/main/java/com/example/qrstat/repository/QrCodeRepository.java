package com.example.qrstat.repository;

import com.example.qrstat.model.QrCode;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class QrCodeRepository {

    private final JdbcTemplate jdbcTemplate;

    public QrCodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<QrCode> mapper = new RowMapper<QrCode>() {
        @Override
        public QrCode mapRow(ResultSet rs, int rowNum) throws SQLException {
            QrCode qrCode = new QrCode();
            qrCode.setId(rs.getLong("id"));
            qrCode.setCode(rs.getString("code"));
            qrCode.setName(rs.getString("name"));
            qrCode.setTargetUrl(rs.getString("target_url"));
            qrCode.setEnabled(rs.getInt("enabled") == 1);
            qrCode.setCreatedAt(rs.getTimestamp("created_at"));
            qrCode.setUpdatedAt(rs.getTimestamp("updated_at"));
            return qrCode;
        }
    };

    public void insert(QrCode qrCode) {
        jdbcTemplate.update(
                "INSERT INTO qr_code(code, name, target_url, enabled, created_at, updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                qrCode.getCode(),
                qrCode.getName(),
                qrCode.getTargetUrl(),
                qrCode.getEnabled() != null && qrCode.getEnabled() ? 1 : 0
        );
    }

    public void update(String code, String name, String targetUrl) {
        jdbcTemplate.update(
                "UPDATE qr_code SET name = ?, target_url = ?, updated_at = CURRENT_TIMESTAMP WHERE code = ?",
                name,
                targetUrl,
                code
        );
    }

    public void updateEnabled(String code, boolean enabled) {
        jdbcTemplate.update(
                "UPDATE qr_code SET enabled = ?, updated_at = CURRENT_TIMESTAMP WHERE code = ?",
                enabled ? 1 : 0,
                code
        );
    }

    public QrCode findByCode(String code) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM qr_code WHERE code = ?", mapper, code);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean existsByCode(String code) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM qr_code WHERE code = ?", Integer.class, code);
        return count != null && count > 0;
    }

    public List<QrCode> findAll() {
        return jdbcTemplate.query("SELECT * FROM qr_code ORDER BY id DESC", mapper);
    }
}
