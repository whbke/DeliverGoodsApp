package su.delivergoods;

import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.internal.http.RealResponseBody;
import okio.BufferedSink;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    public static String mysession = null;
    private EditText et_username;
    private EditText et_password;
    private Button bt_signin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // 获取资源控件实例
        et_username = (EditText) findViewById(R.id.username);
        et_password = (EditText) findViewById(R.id.password);
        bt_signin = (Button) findViewById(R.id.loginButton);

        // 注册登录按钮
        bt_signin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 获取用户输入的数据
                String strUsername = et_username.getText().toString();
                String strPassword = et_password.getText().toString();
                // 判断用户名和密码是否正确
                // 1.创建OkHttpClient对象
                // 2.通过new FormBody()调用build方法,创建一个RequestBody,可以用add添加键值对
                RequestBody  requestBody = new FormBody.Builder()
                        .add("username",strUsername)
                        .add("password",strPassword)
                        .build();
                // 3.创建Request对象，设置URL地址，将RequestBody作为post方法的参数传入
                Request request = new Request.Builder().url(DgUrls.getLoginUrl())
                        .post(requestBody).build();
                // 4.创建一个call对象,参数就是Request请求对象
                Call call = DgUrls.okHttpClient.newCall(request);
                // 5.请求加入调度,重写回调方法
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        ToastUtils.show(LoginActivity.this,"登录失败");
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if ( response.isSuccessful() ) {
                            HttpResponseMsg res = HttpResponseMsg.parse(((RealResponseBody) response.body()).string());
                            if (res.getStatus()) {
                                mysession = ((JSONObject)res.getResult()).getString("sessionid");
                                Intent it = new Intent();
                                it.setAction("shop_list_action");
                                it.addCategory("shop_list_category");
                                startActivity(it);
                                finish();//关闭自己
                                overridePendingTransition(0, 0);
                            } else {
                                ToastUtils.show(LoginActivity.this, "用户名或密码错误！");
                            }
                        } else {
                            ToastUtils.show(LoginActivity.this, "服务端错误！");
                        }
                    }
                });
            }
        });
    }
}
