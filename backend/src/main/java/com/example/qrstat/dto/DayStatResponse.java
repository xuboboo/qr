package com.example.qrstat.dto;

public class DayStatResponse {

    private String date;
    private Long pv;
    private Long uv;

    public DayStatResponse() {
    }

    public DayStatResponse(String date, Long pv, Long uv) {
        this.date = date;
        this.pv = pv;
        this.uv = uv;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
}
