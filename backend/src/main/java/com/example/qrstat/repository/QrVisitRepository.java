package com.example.qrstat.repository;

import com.example.qrstat.dto.DayStatResponse;
import com.example.qrstat.model.QrVisit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
public class QrVisitRepository {

    private final JdbcTemplate jdbcTemplate;

    public QrVisitRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<QrVisit> mapper = new RowMapper<QrVisit>() {
        @Override
        public QrVisit mapRow(ResultSet rs, int rowNum) throws SQLException {
            QrVisit visit = new QrVisit();
            visit.setId(rs.getLong("id"));
            visit.setQrCode(rs.getString("qr_code"));
            visit.setIp(rs.getString("ip"));
            visit.setUserAgent(rs.getString("user_agent"));
            visit.setReferer(rs.getString("referer"));
            visit.setVisitDate(rs.getDate("visit_date"));
            visit.setCreatedAt(rs.getTimestamp("created_at"));
            return visit;
        }
    };

    public void insert(QrVisit visit) {
        jdbcTemplate.update(
                "INSERT INTO qr_visit(qr_code, ip, user_agent, referer, visit_date, created_at) VALUES (?, ?, ?, ?, CURRENT_DATE, CURRENT_TIMESTAMP)",
                visit.getQrCode(),
                trim(visit.getIp(), 64),
                trim(visit.getUserAgent(), 1000),
                trim(visit.getReferer(), 2000)
        );
    }

    public long countPv(String code) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM qr_visit WHERE qr_code = ?", Long.class, code);
        return count == null ? 0L : count;
    }

    public long countUv(String code) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT ip) FROM qr_visit WHERE qr_code = ?", Long.class, code);
        return count == null ? 0L : count;
    }

    public long countTodayPv(String code) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM qr_visit WHERE qr_code = ? AND visit_date = CURRENT_DATE",
                Long.class,
                code
        );
        return count == null ? 0L : count;
    }

    public List<DayStatResponse> countByDay(String code, LocalDate startDate) {
        return jdbcTemplate.query(
                "SELECT CAST(visit_date AS VARCHAR) AS day, COUNT(*) AS pv, COUNT(DISTINCT ip) AS uv " +
                        "FROM qr_visit WHERE qr_code = ? AND visit_date >= ? GROUP BY visit_date ORDER BY visit_date ASC",
                new RowMapper<DayStatResponse>() {
                    @Override
                    public DayStatResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new DayStatResponse(rs.getString("day"), rs.getLong("pv"), rs.getLong("uv"));
                    }
                },
                code,
                java.sql.Date.valueOf(startDate)
        );
    }

    public List<QrVisit> latestVisits(String code, int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM qr_visit WHERE qr_code = ? ORDER BY id DESC LIMIT ?",
                mapper,
                code,
                limit
        );
    }

    public List<QrVisit> latestVisitsAll(int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM qr_visit ORDER BY id DESC LIMIT ?",
                mapper,
                limit
        );
    }

    private String trim(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
