package com.rajgopaljakmola.chitrakarji;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements PaymentResultListener
//        implements Instamojo.InstamojoPaymentCallback
{

    private Button payButton;

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imagePreview;
    private Uri imageUri; // To store the selected image's URI
    private LinearLayout layoutAddFrame;
    private EditText editPincode;
    private EditText editAddress;
    private RadioButton radioHome;

    private Button btnSelectImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

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
                if (item.getItemId() == R.id.home) {
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else if (item.getItemId() == R.id.about) {
                    startActivity(new Intent(MainActivity.this, AboutUs.class));
                }
                return false;
            }
        });

        // Initialize views
        imagePreview = findViewById(R.id.imagePreview);
        Button btnSelectImage = findViewById(R.id.btnSelectImage);
        imagePreview = findViewById(R.id.imagePreview);
        editPincode = findViewById(R.id.editPincode);
        editAddress = findViewById(R.id.editAddress);
        radioHome = findViewById(R.id.radioHomePickup);
        layoutAddFrame = findViewById(R.id.addFrameGroup);
        // Image selection button click event
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // RadioGroup change event to show/hide "Add Frame" option
        RadioGroup radioGroupPickupType = findViewById(R.id.radioGroupPickupType);
        radioGroupPickupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                handlePickupTypeSelection();
            }
        });

    }

    private String getSelectedRadioButtonText(int radioGroupId) {
        RadioGroup radioGroup = findViewById(radioGroupId);
        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
            return selectedRadioButton.getText().toString();
        }
        return "";
    }

    // Helper method to handle Pickup Type selection and show/hide additional fields
    private void handlePickupTypeSelection() {
        RadioButton radioButtonStore = findViewById(R.id.radioStorePickup);
        RadioButton radioButtonHome = findViewById(R.id.radioHomePickup);
        EditText editPincode = findViewById(R.id.editPincode);
        EditText editAddress = findViewById(R.id.editAddress);
        LinearLayout layoutAdditionalFields = findViewById(R.id.layoutAdditionalFields);

        if (radioButtonStore.isChecked()) {
            layoutAddFrame.setVisibility(View.VISIBLE);
            layoutAdditionalFields.setVisibility(View.GONE);
        } else if (radioButtonHome.isChecked()) {
            layoutAddFrame.setVisibility(View.GONE);
            layoutAdditionalFields.setVisibility(View.VISIBLE);
            editPincode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String pincode = editPincode.getText().toString().trim();

                    if (pincode.equals("246149")) {
                        layoutAddFrame.setVisibility(View.VISIBLE);
                    } else {
                        layoutAddFrame.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }


    // Method to open image chooser
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);

        return true;
    }

    private void startPayment(String id) {
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
            options.put("prefill.contact", "9988776655");
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch (Exception e) {
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
        Log.v("razorpay result pay", s);
        Toast.makeText(this, "Payment completed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.v("razorpay result pay", s);
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

    // Handle the selected image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imagePreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}