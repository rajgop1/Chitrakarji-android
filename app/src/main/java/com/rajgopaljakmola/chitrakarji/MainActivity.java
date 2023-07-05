package com.rajgopaljakmola.chitrakarji;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

//import com.instamojo.android.Instamojo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
    implements PaymentResultListener
//        implements Instamojo.InstamojoPaymentCallback
{

    private Button payButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Instamojo.getInstance().initialize(this, Instamojo.Environment.PRODUCTION);

        payButton = findViewById(R.id.btn_pay);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                createOrderUsingVolley();
                createOrderUsingVolleyRazorpay();
            }
        });

        Checkout.preload(getApplicationContext());



        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Home");
        toolbar.inflateMenu(R.menu.home_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.home){
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
                else if(item.getItemId() == R.id.about){
                    startActivity(new Intent(MainActivity.this, AboutUs.class));
                }
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);

        return true ;
    }

    private void startPayment(String id){
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_wDCGpsS5OH8cTs");

        checkout.setImage(R.drawable.ic_launcher_background);
        final Activity activity = this;
        try {
            JSONObject options = new JSONObject();

            options.put("name", "Merchant Name");
            options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg");
            options.put("order_id", id);//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", "50000");//pass amount in currency subunits
            options.put("prefill.email", "gaurav.kumar@example.com");
            options.put("prefill.contact","9988776655");
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch(Exception e) {
            Log.e("razorpay result", "Error in starting Razorpay Checkout", e);
        }

    }

    private void createOrderUsingVolley() {
        String url = "http://192.168.0.101:3000/create-order";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject respObj = new JSONObject(response);
                    String orderId = respObj.getString("order_id");
                    Log.v("volley response id", orderId);
//                    Instamojo.getInstance().initiatePayment(MainActivity.this, orderId, MainActivity.this);

                } catch (JSONException e) {
                    Log.v("volley response", String.valueOf(e));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("volley response", String.valueOf(error));
            }
        });
        queue.add(request);

    }
    private void createOrderUsingVolleyRazorpay() {
        String url = "http://192.168.0.102:3000/create-order-razorpay";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject respObj = new JSONObject(response);
                    String orderId = respObj.getString("id");
                    Log.v("volley response id", orderId);
                    startPayment(orderId);

                } catch (JSONException e) {
                    Log.v("volley response", String.valueOf(e));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("volley response", String.valueOf(error));
            }
        });
        queue.add(request);

    }

    @Override
    public void onPaymentSuccess(String s) {
        Log.v("razorpay result pay", s );
        Toast.makeText(this, "Payment completed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.v("razorpay result pay", s );
        Toast.makeText(this, "Payment Error", Toast.LENGTH_SHORT).show();
    }


//    @Override
//    public void onInstamojoPaymentComplete(String s, String s1, String s2, String s3) {
//        Toast.makeText(this, "Payment completed", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onPaymentCancelled() {
//        Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onInitiatePaymentFailure(String s) {
//        Toast.makeText(this, "Payment failure", Toast.LENGTH_SHORT).show();
//        Log.v("volley response failure", s);
//    }
}