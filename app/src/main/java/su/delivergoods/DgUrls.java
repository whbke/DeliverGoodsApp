package su.delivergoods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class DgUrls {
    private static ConcurrentHashMap<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();
    public static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .cookieJar(new CookieJar()
            {//这里可以做cookie传递，保存等操作
                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies)
                {//可以做保存cookies操作
                    cookieStore.put(url.host(), cookies);
                }
                @Override
                public List<Cookie> loadForRequest(HttpUrl url)
                {//加载新的cookies
                    List<Cookie> cookies = cookieStore.get(url.host());
                    return cookies != null ? cookies : new ArrayList<Cookie>();
                }
            })
            .build();

    public static final String address_server = "http://192.168.2.106:8000";
    public static String getLoginUrl() {
        return address_server + "/dg/login";
    }
    public static String getCarInfo() {
        return address_server + "/dg/getCarInfo";
    }
    public static String getShopGoodsList(String shopId) {
        return address_server + "/dg/getShopGoodsList?shopId=" + shopId;
    }
}
