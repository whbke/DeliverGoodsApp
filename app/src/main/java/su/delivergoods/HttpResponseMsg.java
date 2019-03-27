package su.delivergoods;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class HttpResponseMsg {
    private Boolean status = false;
    private JSON result;
    private String msg;

    public static HttpResponseMsg parse(String res) {
        HttpResponseMsg obj = new HttpResponseMsg();
        JSONObject joRes = JSONObject.parseObject(res);
        obj.status = joRes.getBoolean("status");
        obj.result = (JSON)joRes.get("result");
        obj.msg = joRes.getString("msg");
        return obj;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public JSON getResult() {
        return result;
    }

    public void setResult(JSON result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
