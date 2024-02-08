package cat.insvidreres.imp.m13projecte.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JSONResponse {
    private int responseNo;
    private String message;
    private List<Object> data = new ArrayList<>();

    public int getResponseNo() {
        return responseNo;
    }

    public void setResponseNo(int responseNo) {
        this.responseNo = responseNo;
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
