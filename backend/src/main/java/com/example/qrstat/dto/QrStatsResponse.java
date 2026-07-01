package com.example.qrstat.dto;

import java.util.ArrayList;
import java.util.List;

public class QrStatsResponse {

    private QrCodeResponse qrCode;
    private Long pv;
    private Long uv;
    private Long todayPv;
    private Long todayUv;
    private List<DayStatResponse> last7Days = new ArrayList<DayStatResponse>();
    private List<VisitResponse> latestVisits = new ArrayList<VisitResponse>();

    public QrCodeResponse getQrCode() {
        return qrCode;
    }

    public void setQrCode(QrCodeResponse qrCode) {
        this.qrCode = qrCode;
    }

    public Long getPv() {
        return pv;
    }

    public void setPv(Long pv) {
        this.pv = pv;
    }

    public Long getUv() {
        return uv;
    }

    public void setUv(Long uv) {
        this.uv = uv;
    }

    public Long getTodayPv() {
        return todayPv;
    }

    public void setTodayPv(Long todayPv) {
        this.todayPv = todayPv;
    }

    public Long getTodayUv() {
        return todayUv;
    }

    public void setTodayUv(Long todayUv) {
        this.todayUv = todayUv;
    }

    public List<DayStatResponse> getLast7Days() {
        return last7Days;
    }

    public void setLast7Days(List<DayStatResponse> last7Days) {
        this.last7Days = last7Days;
    }

    public List<VisitResponse> getLatestVisits() {
        return latestVisits;
    }

    public void setLatestVisits(List<VisitResponse> latestVisits) {
        this.latestVisits = latestVisits;
    }
}
