package com.example.busapp;

public class Bus {
    private String totalCount;
    private String BSTOPID; //정류소ID
    private String ROUTEID; //노선ID
    private String REST_STOP_COUNT;    //몇 정거장 전
    private String ARRIVALESTIMATETIME; //몇초전

    public Bus(String ROUTEID, String REST_STOP_COUNT, String ARRIVALESTIMATETIME, String BSTOPID){
        this.ROUTEID = ROUTEID;
        this.REST_STOP_COUNT =REST_STOP_COUNT;
        this.ARRIVALESTIMATETIME = ARRIVALESTIMATETIME;
        this.BSTOPID = BSTOPID;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public String getBSTOPID() {
        return BSTOPID;
    }

    public void setBSTOPID(String BSTOPID) {
        this.BSTOPID = BSTOPID;
    }

    public String getROUTEID() {
        return ROUTEID;
    }

    public void setROUTEID(String ROUTEID) {
        this.ROUTEID = ROUTEID;
    }

    public String getREST_STOP_COUNT() {
        return REST_STOP_COUNT;
    }

    public void setREST_STOP_COUNT(String REST_STOP_COUNT) {
        this.REST_STOP_COUNT = REST_STOP_COUNT;
    }

    public String getARRIVALESTIMATETIME() {
        return ARRIVALESTIMATETIME;
    }

    public void setARRIVALESTIMATETIME(String ARRIVALESTIMATETIME) {
        this.ARRIVALESTIMATETIME = ARRIVALESTIMATETIME;
    }

    public int getLASTBUSYN() {
        return LASTBUSYN;
    }

    public void setLASTBUSYN(int LASTBUSYN) {
        this.LASTBUSYN = LASTBUSYN;
    }

    private int LASTBUSYN; //막차코드 0:일반 1:막차
}
