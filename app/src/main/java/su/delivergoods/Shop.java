package su.delivergoods;

import java.io.Serializable;
import java.util.List;

public class Shop implements Serializable {
    private Integer id; // id
    private String name; // 商家名称
    private Float longitude; // 经度
    private Float latitude; // 纬度
    private Float scope; // 范围(米)
    private Integer index; // 排序

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getScope() {
        return scope;
    }

    public void setScope(Float scope) {
        this.scope = scope;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
