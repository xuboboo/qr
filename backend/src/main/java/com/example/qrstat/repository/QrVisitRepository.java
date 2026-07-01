package com.example.qrstat.repository;

import com.example.qrstat.dto.DayStatResponse;
import com.example.qrstat.model.QrVisit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            visit.setLocation(rs.getString("location"));
            visit.setUserAgent(rs.getString("user_agent"));
            visit.setReferer(rs.getString("referer"));
            visit.setVisitDate(rs.getDate("visit_date"));
            visit.setCreatedAt(rs.getTimestamp("created_at"));
            return visit;
        }
    };

    public void insert(QrVisit visit) {
        jdbcTemplate.update(
                "INSERT INTO qr_visit(qr_code, ip, location, user_agent, referer, visit_date, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_DATE, CURRENT_TIMESTAMP)",
                visit.getQrCode(),
                trim(visit.getIp(), 64),
                trim(visit.getLocation(), 200),
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

    public long countTodayUv(String code) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT ip) FROM qr_visit WHERE qr_code = ? AND visit_date = CURRENT_DATE",
                Long.class,
                code
        );
        return count == null ? 0L : count;
    }

    public List<DayStatResponse> countByDay(String code, LocalDate startDate) {
        return jdbcTemplate.query(
                "SELECT CAST(visit_date AS VARCHAR) AS \"day\", COUNT(*) AS pv, COUNT(DISTINCT ip) AS uv " +
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

    public int deleteByQrCode(String code) {
        return jdbcTemplate.update("DELETE FROM qr_visit WHERE qr_code = ?", code);
    }

    public int deleteByQrCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return 0;
        }
        String placeholders = String.join(",", codes.stream().map(c -> "?").toArray(String[]::new));
        return jdbcTemplate.update("DELETE FROM qr_visit WHERE qr_code IN (" + placeholders + ")", codes.toArray());
    }

    public long countAllPv() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM qr_visit", Long.class);
        return count == null ? 0L : count;
    }

    public long countAllUv() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(DISTINCT ip) FROM qr_visit", Long.class);
        return count == null ? 0L : count;
    }

    public long countTodayAllPv() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM qr_visit WHERE visit_date = CURRENT_DATE",
                Long.class
        );
        return count == null ? 0L : count;
    }

    public long countTodayAllUv() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT ip) FROM qr_visit WHERE visit_date = CURRENT_DATE",
                Long.class
        );
        return count == null ? 0L : count;
    }

    public List<QrVisit> findNullLocation(int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM qr_visit WHERE location IS NULL ORDER BY id ASC LIMIT ?",
                mapper,
                limit
        );
    }

    public int updateLocation(Long id, String location) {
        return jdbcTemplate.update(
                "UPDATE qr_visit SET location = ? WHERE id = ?",
                location,
                id
        );
    }

    public long countNullLocation() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM qr_visit WHERE location IS NULL", Long.class);
        return count == null ? 0L : count;
    }

    public List<Map<String, Object>> countByReferer(String code) {
        String sql = code == null
            ? "SELECT COALESCE(referer, 'direct') AS referer, COUNT(*) AS count FROM qr_visit GROUP BY referer ORDER BY count DESC LIMIT 10"
            : "SELECT COALESCE(referer, 'direct') AS referer, COUNT(*) AS count FROM qr_visit WHERE qr_code = ? GROUP BY referer ORDER BY count DESC LIMIT 10";
        if (code == null) {
            return jdbcTemplate.queryForList(sql);
        }
        return jdbcTemplate.queryForList(sql, code);
    }

    public List<Map<String, Object>> countByHour(String code) {
        String sql = code == null
            ? "SELECT HOUR(created_at) AS \"hour\", COUNT(*) AS count FROM qr_visit GROUP BY HOUR(created_at) ORDER BY \"hour\""
            : "SELECT HOUR(created_at) AS \"hour\", COUNT(*) AS count FROM qr_visit WHERE qr_code = ? GROUP BY HOUR(created_at) ORDER BY \"hour\"";
        if (code == null) {
            return jdbcTemplate.queryForList(sql);
        }
        return jdbcTemplate.queryForList(sql, code);
    }

    public List<Map<String, Object>> countByDevice(String code) {
        String baseSql = code == null
            ? "SELECT user_agent FROM qr_visit"
            : "SELECT user_agent FROM qr_visit WHERE qr_code = ?";
        List<String> userAgents = code == null
            ? jdbcTemplate.queryForList(baseSql, String.class)
            : jdbcTemplate.queryForList(baseSql, String.class, code);

        Map<String, Long> deviceCount = new java.util.HashMap<>();
        for (String ua : userAgents) {
            String device = com.example.qrstat.util.UserAgentUtil.detectDevice(ua);
            deviceCount.merge(device, 1L, Long::sum);
        }

        return deviceCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("device", e.getKey());
                    map.put("count", e.getValue());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Map<String, Object>> countByBrowser(String code) {
        String baseSql = code == null
            ? "SELECT user_agent FROM qr_visit"
            : "SELECT user_agent FROM qr_visit WHERE qr_code = ?";
        List<String> userAgents = code == null
            ? jdbcTemplate.queryForList(baseSql, String.class)
            : jdbcTemplate.queryForList(baseSql, String.class, code);

        Map<String, Long> browserCount = new java.util.HashMap<>();
        for (String ua : userAgents) {
            String browser = com.example.qrstat.util.UserAgentUtil.detectBrowser(ua);
            browserCount.merge(browser, 1L, Long::sum);
        }

        return browserCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("browser", e.getKey());
                    map.put("count", e.getValue());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Map<String, Object>> countByLocation(String code) {
        String sql = code == null
            ? "SELECT COALESCE(location, '未知') AS location, COUNT(*) AS count FROM qr_visit GROUP BY location ORDER BY count DESC LIMIT 10"
            : "SELECT COALESCE(location, '未知') AS location, COUNT(*) AS count FROM qr_visit WHERE qr_code = ? GROUP BY location ORDER BY count DESC LIMIT 10";
        if (code == null) {
            return jdbcTemplate.queryForList(sql);
        }
        return jdbcTemplate.queryForList(sql, code);
    }

    private String trim(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
