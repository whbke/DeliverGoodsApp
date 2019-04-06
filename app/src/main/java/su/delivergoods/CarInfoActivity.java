package su.delivergoods;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;

public class CarInfoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "ShopListActivity";
    private static CarInfoActivity m_instance = null;

    private List<Shop> m_shopList = new ArrayList<>();
//    private Shop m_currentShop = null;
    private String m_carId = null;
    private JSONObject m_carInfoJson = null;
    private static final Integer SHOP_MENU_START_ID = 100;

    protected static final int MSG_UPDATE_CAR_INFO = 0;

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_CAR_INFO:
                    _updateCarInfoUI();
                    break;
                default:
                    break;
            };
        }
    };

    public static CarInfoActivity getInstance() {
        return m_instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        m_instance = this;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        updateCarInfo();
    }

    /**
     * 刷新商店列表
     */
    public void updateCarInfo() {
        /////////////////////////////////////////获取Shop List////////////////////////////////
        // 1.创建OkHttpClient对象
        // 2.通过new FormBody()调用build方法,创建一个RequestBody,可以用add添加键值对
        RequestBody requestBody = new FormBody.Builder()
                .build();
        // 3.创建Request对象，设置URL地址，将RequestBody作为post方法的参数传入
        Request request = new Request.Builder().url(DgUrls.getCarInfo())
                .get().build();
        // 4.创建一个call对象,参数就是Request请求对象
        Call call = DgUrls.okHttpClient.newCall(request);
        // 5.请求加入调度,重写回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.show(CarInfoActivity.this,"获取路线失败");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if ( response.isSuccessful() ) {
                    HttpResponseMsg res = HttpResponseMsg.parse(((RealResponseBody) response.body()).string());
                    if (res.getStatus()) {
                        if ( res.getResult() instanceof JSONObject ) {
                            m_carInfoJson = (JSONObject) res.getResult();
                            new Thread() {
                                public void run() {
                                    Message message = Message.obtain(); //获取消息的载体
                                    message.what = MSG_UPDATE_CAR_INFO;
                                    handler.sendMessage(message);
                                };
                            }.start();
                        } else {
                            ToastUtils.show(CarInfoActivity.this, "获取路线格式错误！");
                        }
                    } else {
                        ToastUtils.show(CarInfoActivity.this, "获取路线错误！");
                    }
                } else {
                    ToastUtils.show(CarInfoActivity.this, "服务端错误！");
                }
            }
        });
    }

    private void _updateCarInfoUI() {
        synchronized (this) {
            if ( m_carInfoJson == null ) {
                return;
            }
            m_carId = m_carInfoJson.getString("id");
            /////////////////////////////////////////清空////////////////////////////////
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            for (Shop shop : m_shopList) {
                navigationView.getMenu().removeItem(SHOP_MENU_START_ID + shop.getId());
            }
            JSONObject joRoute = m_carInfoJson.getJSONObject("route");
            if (joRoute != null) {
                updateShopList(joRoute.getJSONArray("shops"));
            }
            // 增加商店按钮
            for (Shop shop : m_shopList) {
                int menuId = shop.getId() + SHOP_MENU_START_ID;
                navigationView.getMenu().add(menuId, menuId, menuId, shop.getName());
            }
            // 显示车上的商品列表
            int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 1.0 / 2);
            TableLayout table = (TableLayout) findViewById(R.id.table_car_goods);
            table.removeAllViews();
            {
                Resources resource = (Resources) getBaseContext().getResources();
                ColorStateList csl = (ColorStateList) resource.getColorStateList(R.color.colorTableHeader);

                TableRow row = new TableRow(CarInfoActivity.this);

                TextView nameView = new TextView(CarInfoActivity.this);
                nameView.setText("商品名称");
                nameView.setPadding(0, 0, 0, 0);
                nameView.setWidth(width);
                nameView.setTextColor(csl);
                row.addView(nameView);

                TextView numberView = new TextView(CarInfoActivity.this);
                numberView.setText("装车数量");
                numberView.setPadding(0, 0, 0, 0);
                numberView.setWidth(width);
                numberView.setTextColor(csl);
                row.addView(numberView);

                table.addView(row);
            }
            JSONArray joGoodsList = m_carInfoJson.getJSONArray("goods");
            for (int gIdx = 0; gIdx < joGoodsList.size(); ++gIdx) {
                JSONObject joGoods = joGoodsList.getJSONObject(gIdx);
                TableRow row = new TableRow(CarInfoActivity.this);

                TextView nameView = new TextView(CarInfoActivity.this);
                nameView.setText(joGoods.getString("name"));
                nameView.setPadding(0, 0, 0, 0);
                nameView.setWidth(width);
                row.addView(nameView);

                TextView numberView = new TextView(CarInfoActivity.this);
                numberView.setText(joGoods.getString("currentNumber") + joGoods.getString("unitName"));
                numberView.setPadding(0, 0, 0, 0);
                numberView.setWidth(width);
                row.addView(numberView);

                table.addView(row);
            }
        }
    }

    /**
     * JSON 转 shop List
     * @param joShopArray
     * @return
     */
    private void updateShopList(JSONArray joShopArray) {
        if ( joShopArray == null || joShopArray.isEmpty() ) {
            ToastUtils.show(CarInfoActivity.this, "没有商家！");
        }
        m_shopList.clear();
        int  failCount = 0;
        for ( int shopIdx=0; shopIdx<joShopArray.size(); ++shopIdx ) {
            JSONObject joShop = joShopArray.getJSONObject(shopIdx);
            if ( joShop == null ) {
                ++ failCount;
            } else {
                Shop shop = new Shop();
                shop.setId( joShop.getInteger("id") );
                shop.setName( joShop.getString("name") );
                shop.setLongitude( joShop.getFloat("longitude") );
                shop.setLatitude( joShop.getFloat("latitude") );
                shop.setScope( joShop.getFloat("scope") );
                shop.setIndex( joShop.getInteger("index") );
                m_shopList.add(shop);
            }
        }
        if ( failCount > 0 ) {
            ToastUtils.show( CarInfoActivity.this, "有" + failCount + "个商家加载失败！" );
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.car_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if ( id == R.id.nav_car_info ) {

        } else if (id == R.id.nav_car_refresh) {
//            Intent it = new Intent(CarInfoActivity.this, CarInfoActivity.class);
//            startActivity(it);
//            finish();//关闭自己
//            overridePendingTransition(0, 0);
            Intent intent = getIntent();
            overridePendingTransition(0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
        } else {
            for (Shop shop : m_shopList) {
                if (id == SHOP_MENU_START_ID + shop.getId()) {
//                    m_currentShop = shop;
                    Intent it = new Intent();
                    it.putExtra("shop", shop);
                    it.putExtra("carId", m_carId);
                    it.setAction("shop_detail_action");
                    it.addCategory("shop_detail_category");
                    startActivity(it);
                }
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
