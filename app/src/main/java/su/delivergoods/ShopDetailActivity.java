package su.delivergoods;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;

public class ShopDetailActivity extends AppCompatActivity {
    private static final String TAG = "ShopDetailActivity";
    private Shop m_shop = null;
    private String m_carId = null;
//    private JSONArray m_goodsListJson = null;
    private JSONObject m_ShopNoteTodayJson = null;
    private Map<Integer, JSONObject> m_viewId2goodsInfo = new HashMap<>();

    private static final int MSG_UPDATE_GOODS_LIST = 0;

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_GOODS_LIST:
                    updateGoosListUI();
                    break;
                default:
                    break;
            }
            ;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);
        m_shop = (Shop) getIntent().getSerializableExtra("shop");
        m_carId = (String) getIntent().getSerializableExtra("carId");
        registerShopDetailAction();
        refreshNoteInfo();
    }

    private void refreshNoteInfo() {
        /////////////////////////////////////////获取Shop Detail////////////////////////////////
        // 1.创建OkHttpClient对象
        // 2.通过new FormBody()调用build方法,创建一个RequestBody,可以用add添加键值对
        RequestBody requestBody = new FormBody.Builder()
                .build();
        // 3.创建Request对象，设置URL地址，将RequestBody作为post方法的参数传入
//        Request request = new Request.Builder().url(DgUrls.getShopGoodsList(""+m_shop.getId()))
//                .get().build();
        Request request = new Request.Builder().url(DgUrls.getShopNoteToday(m_carId,""+m_shop.getId()))
                .get().build();
        // 4.创建一个call对象,参数就是Request请求对象
        Call call = DgUrls.okHttpClient.newCall(request);
        // 5.请求加入调度,重写回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.show(ShopDetailActivity.this,"获取商店信息失败");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if ( response.isSuccessful() ) {
                    HttpResponseMsg res = HttpResponseMsg.parse(((RealResponseBody) response.body()).string());
                    if (res.getStatus()) {
                        if ( res.getResult() instanceof JSONObject) {
                            synchronized (this) {
                                m_ShopNoteTodayJson = (JSONObject) res.getResult();
                                new Thread() {
                                    public void run() {
                                        Message message = Message.obtain(); //获取消息的载体
                                        message.what = MSG_UPDATE_GOODS_LIST;
                                        handler.sendMessage(message);
                                    };
                                }.start();
                            }
                        } else {
                            ToastUtils.show(ShopDetailActivity.this, "格式错误！");
                        }
                    } else {
                        ToastUtils.show(ShopDetailActivity.this, "获取失败！");
                    }
                } else {
                    ToastUtils.show(ShopDetailActivity.this, "服务端错误！");
                }
            }
        });
    }

    private void updateGoosListUI() {
        synchronized (this) {
            if ( m_ShopNoteTodayJson == null ) {
                return;
            }
            ((TextView) findViewById(R.id.text_total_price_val)).setBackgroundColor( getResources().getColor(R.color.colorEditReadonly));;

            ((TextView) findViewById(R.id.text_total_price_val)).setText(m_ShopNoteTodayJson.getString("totalPrice"));
            ((EditText)findViewById(R.id.edit_actual_price_val)).setText(m_ShopNoteTodayJson.getString("actualPrice"));
            ((EditText)findViewById(R.id.edit_bookkeeping_val)).setText(m_ShopNoteTodayJson.getString("bookkeeping"));
            JSONArray goodsList = m_ShopNoteTodayJson.getJSONArray("goodsList");
            int width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 1.0 / 6);
            // 显示商店上的商品列表
            TableLayout table = (TableLayout) findViewById(R.id.table_shop_goods);
            table.removeAllViews();
            m_viewId2goodsInfo.clear();
            {
                Resources resource = (Resources) getBaseContext().getResources();
                ColorStateList csl = (ColorStateList) resource.getColorStateList(R.color.colorTableHeader);

                TableRow row = new TableRow(ShopDetailActivity.this);

                TextView nameView = new TextView(ShopDetailActivity.this);
                nameView.setText("商品名称");
                nameView.setTextColor(csl);
                nameView.setPadding(0, 0, 0, 0);
                nameView.setWidth(width);
                row.addView(nameView);

                TextView priceView = new TextView(ShopDetailActivity.this);
                priceView.setText("价格");
                priceView.setTextColor(csl);
                priceView.setPadding(0, 0, 0, 0);
                priceView.setWidth(width);
                row.addView(priceView);

                TextView remainderNumberView = new TextView(ShopDetailActivity.this);
                remainderNumberView.setText("剩余数量");
                remainderNumberView.setTextColor(csl);
                remainderNumberView.setPadding(0, 0, 0, 0);
                remainderNumberView.setWidth(width);
                row.addView(remainderNumberView);

                TextView targetNumberView = new TextView(ShopDetailActivity.this);
                targetNumberView.setText("目标数量");
                targetNumberView.setTextColor(csl);
                targetNumberView.setPadding(0, 0, 0, 0);
                targetNumberView.setWidth(width);
                row.addView(targetNumberView);

                TextView deliveryNumberView = new TextView(ShopDetailActivity.this);
                deliveryNumberView.setText("卸载数量");
                deliveryNumberView.setTextColor(csl);
                deliveryNumberView.setPadding(0, 0, 0, 0);
                deliveryNumberView.setWidth(width);
                row.addView(deliveryNumberView);

                table.addView(row);
            }
            for (int gIdx = 0; gIdx < goodsList.size(); ++gIdx) {
                JSONObject joGoods = goodsList.getJSONObject(gIdx);
                TableRow row = new TableRow(ShopDetailActivity.this);

                TextView nameView = new TextView(ShopDetailActivity.this);
                nameView.setText(joGoods.getString("name"));
                nameView.setPadding(0, 0, 0, 0);
                nameView.setWidth(width);
                row.addView(nameView);

                TextView priceView = new TextView(ShopDetailActivity.this);
                priceView.setText(joGoods.getString("price"));
                priceView.setPadding(0, 0, 0, 0);
                priceView.setWidth(width);
                row.addView(priceView);

                TextView remainderNumberView = new TextView(ShopDetailActivity.this);
                remainderNumberView.setText(joGoods.getString("currentNumberInCar") + joGoods.getString("unitName"));
                remainderNumberView.setPadding(0, 0, 0, 0);
                remainderNumberView.setWidth(width);
                row.addView(remainderNumberView);

                TextView targetNumberView = new TextView(ShopDetailActivity.this);
                targetNumberView.setText(joGoods.getString("targetNumber") + joGoods.getString("unitName"));
                targetNumberView.setPadding(0, 0, 0, 0);
                targetNumberView.setWidth(width);
                row.addView(targetNumberView);

                EditText deliveryNumberView = new EditText(ShopDetailActivity.this);
                int viewId = View.generateViewId();
                deliveryNumberView.setId(viewId);
                m_viewId2goodsInfo.put(viewId, joGoods);
                deliveryNumberView.setPadding(0, 0, 0, 0);
                deliveryNumberView.setInputType(InputType.TYPE_CLASS_NUMBER);
                deliveryNumberView.setWidth(width);
                deliveryNumberView.setText(joGoods.getString("currentNumberDelivery"));
                row.addView(deliveryNumberView);

                deliveryNumberView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        TextView totalPriceView = (TextView)findViewById(R.id.text_total_price_val);
                        totalPriceView.setBackgroundColor( getResources().getColor(R.color.colorEditTextChange));
                    }
                });

                TextView deliveryNumberUnitView = new TextView(ShopDetailActivity.this);
                deliveryNumberUnitView.setText(joGoods.getString("unitName"));
                deliveryNumberUnitView.setPadding(0, 0, 0, 0);
                deliveryNumberUnitView.setWidth(width);
                row.addView(deliveryNumberUnitView);

                table.addView(row);
            }
        }
    }

    /*
    提交订单
    {
        "carId": 1,
        "shopId": 1,
        "noteId": 1,
        "actualPrice": 4,
        "bookkeeping": 2,
        "goodsList": [
            {
                "id": 1,
                "goodsId": 1,
                "number": 10,
                "unitId": 1
            }
        ]
    }
     */
    private void commitShopNote() {
        // 获取用户输入的数据
        String carId = m_ShopNoteTodayJson.getString("carId");
        String shopId = m_ShopNoteTodayJson.getString("shopId");
        String noteId = m_ShopNoteTodayJson.getString("noteId");
        Float actualPrice = Float.valueOf(((EditText)findViewById(R.id.edit_actual_price_val)).getText().toString());
        Float bookkeeping = Float.valueOf(((EditText)findViewById(R.id.edit_bookkeeping_val)).getText().toString());
        JSONArray goodsListArray = new JSONArray();
        for ( Map.Entry<Integer, JSONObject> entry: m_viewId2goodsInfo.entrySet() ) {
            JSONObject goodsItem = entry.getValue();
            goodsItem.put("id", goodsItem.getInteger("id"));
            goodsItem.put("goodsId", goodsItem.getInteger("goodsId"));
            goodsItem.put("number", Integer.valueOf(((EditText)findViewById(entry.getKey())).getText().toString()));
            goodsItem.put("unitId", goodsItem.getInteger("unitId"));
            goodsListArray.add(goodsItem);
        }
        JSONObject postJson = new JSONObject();
        postJson.put("carId", carId);
        postJson.put("shopId", shopId);
        postJson.put("noteId", noteId);
        postJson.put("actualPrice", actualPrice);
        postJson.put("bookkeeping", bookkeeping);
        postJson.put("goodsList", goodsListArray);

        TableLayout table = (TableLayout) findViewById(R.id.table_shop_goods);
        // 判断用户名和密码是否正确
        // 1.创建OkHttpClient对象
        // 2.通过new FormBody()调用build方法,创建一个RequestBody,可以用add添加键值对
        RequestBody  requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), postJson.toJSONString());
        // 3.创建Request对象，设置URL地址，将RequestBody作为post方法的参数传入
        Request request = new Request.Builder().url(DgUrls.commitShopNote())
                .post(requestBody).build();
        // 4.创建一个call对象,参数就是Request请求对象
        Call call = DgUrls.okHttpClient.newCall(request);
        // 5.请求加入调度,重写回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.show(ShopDetailActivity.this,"提交失败");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if ( response.isSuccessful() ) {
                    HttpResponseMsg res = HttpResponseMsg.parse(((RealResponseBody) response.body()).string());
                    if (res.getStatus()) {
                        refreshNoteInfo();
                        // 刷新CarInfo TODO
                    } else {
                        ToastUtils.show(ShopDetailActivity.this, "提交失败！");
                    }
                } else {
                    ToastUtils.show(ShopDetailActivity.this, "服务端错误！");
                }
            }
        });
    }

    /**
     * 注册shop detail view action
     */
    private void registerShopDetailAction() {
        Button navigationButton = (Button) findViewById(R.id.action_navigation);
        navigationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if ( m_shop != null ) {
                    navigation(m_shop);
                } else {
                    ToastUtils.show(ShopDetailActivity.this, "请选择一个商店");
                }
            }
        });

        ((Button)findViewById(R.id.action_commit_note)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( m_shop != null ) {
                    commitShopNote();
                } else {
                    ToastUtils.show(ShopDetailActivity.this, "请选择一个商店");
                }
            }
        });
    }
    /**
     * 判断是否安装目标应用
     * @param packageName 目标应用安装后的包名
     * @return 是否已安装目标应用
     */
    private boolean isInstallByread(String packageName) {
        return new File("/data/data/" + packageName).exists();
    }

    /**
     * 导航接口
     * @param shop
     */
    private void navigation( Shop shop ) {
        try {
            if ( shop.getLatitude() == null || shop.getLongitude() == null ) {
                ToastUtils.show(ShopDetailActivity.this, "商店位置信息错误！");
                return;
            }

            Intent intent = Intent.getIntent(
                    "intent://map/direction?origin=我的位置" +
                            "&destination=latlng:"+shop.getLatitude()+","+shop.getLongitude()+"|name:"+ shop.getName() +
                            "&mode=driving" +
                            "&coord_type=bd09ll" +
                            "&src=yourCompanyName|yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
            if(isInstallByread("com.baidu.BaiduMap")){
                startActivity(intent);
                Log.e(TAG, "百度地图客户端已经安装") ;
            }else {
                Log.e(TAG, "没有安装百度地图客户端") ;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
