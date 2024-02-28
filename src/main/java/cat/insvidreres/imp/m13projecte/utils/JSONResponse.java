package cat.insvidreres.imp.m13projecte.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JSONResponse {
    private int responseNo;
    private String date;
    private String message;
    private List<Object> data = new ArrayList<>();

    public JSONResponse(int responseNo, String date, String message, List<Object> data) {
        this.responseNo = responseNo;
        this.date = date;
        this.message = message;
        this.data = data;
    }

    public JSONResponse(int responseNo, String date, String message) {
        this.responseNo = responseNo;
        this.date = date;
        this.message = message;
    }

    public int getResponseNo() {
        return responseNo;
    }

    public void setResponseNo(int responseNo) {
        this.responseNo = responseNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Object> getData() {
        return data;
    }

    public void setData(Object data) {
        this.data.add(data);
    }
}
