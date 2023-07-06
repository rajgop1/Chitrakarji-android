package com.rajgopaljakmola.chitrakarji;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.instamojo.android.Instamojo;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

//import com.instamojo.android.Instamojo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
//        implements PaymentResultListener
        implements Instamojo.InstamojoPaymentCallback {

    private Button payButton;

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imagePreview;
    private Uri imageUri; // To store the selected image's URI
    private LinearLayout layoutAddFrame;
    private EditText editName;
    private EditText editEmail;
    private EditText editPhone;

    private EditText editPincode;
    private EditText editAddress;
    private RadioButton radioHome;
    private RadioGroup radioGroupPage;

    private RadioGroup radioGroupNumOfFaces;

    private RadioGroup radioGroupPickupType;
    private RadioGroup radioGroupAddFrame;

    private Button btnSelectImage;
    private String encodeImageString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);
        Instamojo.getInstance().initialize(this, Instamojo.Environment.PRODUCTION);

        payButton = findViewById(R.id.btn_pay);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createOrderUsingVolley();
//                createOrderUsingVolleyRazorpay();
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
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPhone = findViewById(R.id.editPhone);
        editPincode = findViewById(R.id.editPincode);
        editAddress = findViewById(R.id.editAddress);
        radioHome = findViewById(R.id.radioHomePickup);
        radioGroupPage = findViewById(R.id.radioGroupPageSize);
        radioGroupNumOfFaces = findViewById(R.id.radioGroupNumOfFaces);
        radioGroupPickupType = findViewById(R.id.radioGroupPickupType);
        radioGroupAddFrame = findViewById(R.id.radioGroupAddFrame);

        layoutAddFrame = findViewById(R.id.addFrameGroup);
        // Image selection button click event
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // RadioGroup change event to show/hide "Add Frame" option
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
        String url = "http://192.168.241.38:3000/create-order";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        String[] formData = getFormData();

        String pageSize = formData[0];
        String numberOfFaces = formData[1];
        String pickupType = formData[2];
        String addFrame = formData[3];
        String name = formData[4];
        String email = formData[5];
        String phone = formData[6];
        String pincode = formData[7];
        String address = formData[8];


        JSONObject jsonBody = new JSONObject();

        String userOrderIdFromApp = String.valueOf(getOrderID(pageSize, numberOfFaces, pickupType, addFrame));
        try {
            jsonBody.put("userOrderIdFromApp", userOrderIdFromApp);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject respObj = new JSONObject(response);
                    String orderId = respObj.getString("order_id");
                    Log.v("volley response id", orderId);
                    Instamojo.getInstance().initiatePayment(MainActivity.this, orderId, MainActivity.this);

                } catch (JSONException e) {
                    Log.v("volley response", String.valueOf(e));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("volley response", String.valueOf(error));
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                // on below line we are passing our key
                // and value pair to our parameters.
                params.put("userOrderIdFromApp", userOrderIdFromApp);
                params.put("name", name);
                params.put("email", email);
                params.put("phone", phone);

                return params;
            }
        };
        queue.add(request);

    }

    private void sendOrderDetailsUsingVolley() {

        String url = "http://192.168.241.38:3000/payment-result";


        JSONObject jsonBody = new JSONObject();
        String[] formData = getFormData();

        String pageSize = formData[0];
        String numberOfFaces = formData[1];
        String pickupType = formData[2];
        String addFrame = formData[3];
        String name = formData[4];
        String email = formData[5];
        String phone = formData[6];
        String pincode = formData[7];
        String address = formData[8];


        try {
            jsonBody.put("PageSize", pageSize);
            jsonBody.put("NumberOfFaces", numberOfFaces);
            jsonBody.put("PickupType", pickupType);
            jsonBody.put("AddFrame", addFrame);
            jsonBody.put("Name", name);
            jsonBody.put("Email", email);
            jsonBody.put("Phone", phone);
            jsonBody.put("Pincode", pincode);
            jsonBody.put("Address", address);
            jsonBody.put("Image", encodeImageString);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        final String requestBody = jsonBody.toString();

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject respObj = new JSONObject(response);
                    String orderId = respObj.getString("order_id");
                    Log.v("volley response id", orderId);
                    Instamojo.getInstance().initiatePayment(MainActivity.this, orderId, MainActivity.this);

                } catch (JSONException e) {
                    Log.v("volley response", String.valueOf(e));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("volley response", String.valueOf(error));
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        queue.add(request);


    }

    public String[] getFormData() {
        String[] formData = new String[9];

        String pageSize = "";
        String numberOfFaces = "";
        String pickupType = "";
        String addFrame = "No";
        String name = String.valueOf(editName.getText());
        String email = String.valueOf(editEmail.getText());
        String phone = String.valueOf(editPhone.getText());
        String pincode = String.valueOf(editPincode.getText());
        String address = String.valueOf(editAddress.getText());

        if (radioGroupPage.getCheckedRadioButtonId() == R.id.radioA2) {
            pageSize = "A2";
        } else if (radioGroupPage.getCheckedRadioButtonId() == R.id.radioA3) {
            pageSize = "A3";
        } else if (radioGroupPage.getCheckedRadioButtonId() == R.id.radioA4) {
            pageSize = "A4";
        }

        if (radioGroupNumOfFaces.getCheckedRadioButtonId() == R.id.radio1Face) {
            numberOfFaces = "1 Face";
        } else if (radioGroupNumOfFaces.getCheckedRadioButtonId() == R.id.radio2Faces) {
            numberOfFaces = "2 Faces";
        } else if (radioGroupNumOfFaces.getCheckedRadioButtonId() == R.id.radio3Faces) {
            numberOfFaces = "3 Faces";
        }

        if (radioGroupPickupType.getCheckedRadioButtonId() == R.id.radioHomePickup) {
            pickupType = "Home";
        } else if (radioGroupPickupType.getCheckedRadioButtonId() == R.id.radioStorePickup) {
            pickupType = "Store";
        }

        if (radioGroupAddFrame.getCheckedRadioButtonId() == R.id.radioYesFrame) {
            addFrame = "Yes";
        } else if (radioGroupAddFrame.getCheckedRadioButtonId() == R.id.radioNoFrame) {
            addFrame = "No";
        }

        formData[0] = pageSize;
        formData[1] = numberOfFaces;
        formData[2] = pickupType;
        formData[3] = addFrame;
        formData[4] = name;
        formData[5] = email;
        formData[6] = phone;
        formData[7] = pincode;
        formData[8] = address;

        return formData;
    }


//    private void createOrderUsingVolleyRazorpay() {
//        String url = "http://192.168.0.102:3000/create-order-razorpay";
//        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
//        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                try {
//                    JSONObject respObj = new JSONObject(response);
//                    String orderId = respObj.getString("id");
//                    Log.v("volley response id", orderId);
//                    startPayment(orderId);
//
//                } catch (JSONException e) {
//                    Log.v("volley response", String.valueOf(e));
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.v("volley response", String.valueOf(error));
//            }
//        });
//        queue.add(request);
//
//    }

    // razorpay
//    @Override
//    public void onPaymentSuccess(String s) {
//        Log.v("razorpay result pay", s);
//        Toast.makeText(this, "Payment completed", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onPaymentError(int i, String s) {
//        Log.v("razorpay result pay", s);
//        Toast.makeText(this, "Payment Error", Toast.LENGTH_SHORT).show();
//    }


    @Override
    public void onInstamojoPaymentComplete(String s, String s1, String s2, String s3) {
        Toast.makeText(this, "Payment completed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPaymentCancelled() {
        Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
        sendOrderDetailsUsingVolley();
    }

    @Override
    public void onInitiatePaymentFailure(String s) {
        Toast.makeText(this, "Payment failure", Toast.LENGTH_SHORT).show();
        Log.v("volley response failure", s);
    }

    // Handle the selected image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);

                    // Get the orientation information from the image's metadata
                    ExifInterface exifInterface = null;
                    exifInterface = new ExifInterface(inputStream);

                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    // Reset the stream to decode the bitmap
                    inputStream = getContentResolver().openInputStream(imageUri);

                    // Decode the input stream into a bitmap
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        // Rotate the bitmap according to the orientation information
                        bitmap = rotateBitmap(bitmap, orientation);

                        imagePreview.setImageBitmap(bitmap);
                        encodeBitmapImage(bitmap);
                    } else {
                        Log.v("Error", "Failed to decode the bitmap");
                    }
                } catch (FileNotFoundException e) {
                    Log.v("Error file not found", String.valueOf(e));
                } catch (IOException e) {
                    Log.v("Error reading metadata", String.valueOf(e));
                }
            } else {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imagePreview.setImageBitmap(bitmap);
                    encodeBitmapImage(bitmap);
                } catch (FileNotFoundException e) {
                    Log.v("Error file not found", String.valueOf(e));
                }
            }
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1, -1);
                break;
            default:
                return bitmap; // No rotation needed
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void encodeBitmapImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytesofimage = byteArrayOutputStream.toByteArray();
        encodeImageString = android.util.Base64.encodeToString(bytesofimage, Base64.DEFAULT);
    }

    public int getOrderID(String pageSize, String numberOfFaces, String pickupType, String addFrame) {
        if (pageSize.equals("A2") && numberOfFaces.equals("1 Face") && pickupType.equals("Store") && addFrame.equals("Yes")) {
            return 1;
        } else if (pageSize.equals("A2") && numberOfFaces.equals("1 Face") && pickupType.equals("Store") && addFrame.equals("No")) {
            return 2;
        } else if (pageSize.equals("A2") && numberOfFaces.equals("2 Faces") && pickupType.equals("Store") && addFrame.equals("Yes")) {
            return 3;
        } else if (pageSize.equals("A2") && numberOfFaces.equals("2 Faces") && pickupType.equals("Store") && addFrame.equals("No")) {
            return 4;
        } else if (pageSize.equals("A2") && numberOfFaces.equals("3 Faces") && pickupType.equals("Store") && addFrame.equals("Yes")) {
            return 5;
        } else if (pageSize.equals("A2") && numberOfFaces.equals("3 Faces") && pickupType.equals("Store") && addFrame.equals("No")) {
            return 6;
        } else if (pageSize.equals("A3") && numberOfFaces.equals("1 Face") && pickupType.equals("Store") && addFrame.equals("Yes")) {
            return 7;
        } else if (pageSize.equals("A3") && numberOfFaces.equals("1 Face") && pickupType.equals("Store") && addFrame.equals("No")) {
            return 8;
        } else if (pageSize.equals("A3") && numberOfFaces.equals("2 Faces") && pickupType.equals("Store") && addFrame.equals("Yes")) {
            return 9;
        } else if (pageSize.equals("A3") && numberOfFaces.equals("2 Faces") && pickupType.equals("Store") && addFrame.equals("No")) {
            return 10;
        } else if (pageSize.equals("A3") && numberOfFaces.equals("3 Faces") && pickupType.equals("Store") && addFrame.equals("Yes")) {
            return 11;
        } else if (pageSize.equals("A3") && numberOfFaces.equals("3 Faces") && pickupType.equals("Store") && addFrame.equals("No")) {
            return 12;
        } else if (pageSize.equals("A4") && numberOfFaces.equals("1 Face") && pickupType.equals("Store") && addFrame.equals("Yes")) {
            return 13;
        } else if (pageSize.equals("A4") && numberOfFaces.equals("1 Face") && pickupType.equals("Store") && addFrame.equals("No")) {
            return 14;
        } else if (pageSize.equals("A4") && numberOfFaces.equals("2 Faces") && pickupType.equals("Store") && addFrame.equals("Yes")) {
            return 15;
        } else if (pageSize.equals("A4") && numberOfFaces.equals("2 Faces") && pickupType.equals("Store") && addFrame.equals("No")) {
            return 16;
        } else if (pageSize.equals("A4") && numberOfFaces.equals("3 Faces") && pickupType.equals("Store") && addFrame.equals("Yes")) {
            return 17;
        } else if (pageSize.equals("A4") && numberOfFaces.equals("3 Faces") && pickupType.equals("Store") && addFrame.equals("No")) {
            return 18;
        } else if (pageSize.equals("A2") && numberOfFaces.equals("1 Face") && pickupType.equals("Home") && addFrame.equals("Yes")) {
            return 19;
        } else if (pageSize.equals("A2") && numberOfFaces.equals("1 Face") && pickupType.equals("Home") && addFrame.equals("No")) {
            return 20;
        } else if (pageSize.equals("A2") && numberOfFaces.equals("2 Faces") && pickupType.equals("Home") && addFrame.equals("Yes")) {
            return 21;
        } else if (pageSize.equals("A2") && numberOfFaces.equals("2 Faces") && pickupType.equals("Home") && addFrame.equals("No")) {
            return 22;
        } else if (pageSize.equals("A2") && numberOfFaces.equals("3 Faces") && pickupType.equals("Home") && addFrame.equals("Yes")) {
            return 23;
        } else if (pageSize.equals("A2") && numberOfFaces.equals("3 Faces") && pickupType.equals("Home") && addFrame.equals("No")) {
            return 24;
        } else if (pageSize.equals("A3") && numberOfFaces.equals("1 Face") && pickupType.equals("Home") && addFrame.equals("Yes")) {
            return 25;
        } else if (pageSize.equals("A3") && numberOfFaces.equals("1 Face") && pickupType.equals("Home") && addFrame.equals("No")) {
            return 26;
        } else if (pageSize.equals("A3") && numberOfFaces.equals("2 Faces") && pickupType.equals("Home") && addFrame.equals("Yes")) {
            return 27;
        } else if (pageSize.equals("A3") && numberOfFaces.equals("2 Faces") && pickupType.equals("Home") && addFrame.equals("No")) {
            return 28;
        } else if (pageSize.equals("A3") && numberOfFaces.equals("3 Faces") && pickupType.equals("Home") && addFrame.equals("Yes")) {
            return 29;
        } else if (pageSize.equals("A3") && numberOfFaces.equals("3 Faces") && pickupType.equals("Home") && addFrame.equals("No")) {
            return 30;
        } else if (pageSize.equals("A4") && numberOfFaces.equals("1 Face") && pickupType.equals("Home") && addFrame.equals("Yes")) {
            return 31;
        } else if (pageSize.equals("A4") && numberOfFaces.equals("1 Face") && pickupType.equals("Home") && addFrame.equals("No")) {
            return 32;
        } else if (pageSize.equals("A4") && numberOfFaces.equals("2 Faces") && pickupType.equals("Home") && addFrame.equals("Yes")) {
            return 33;
        } else if (pageSize.equals("A4") && numberOfFaces.equals("2 Faces") && pickupType.equals("Home") && addFrame.equals("No")) {
            return 34;
        } else if (pageSize.equals("A4") && numberOfFaces.equals("3 Faces") && pickupType.equals("Home") && addFrame.equals("Yes")) {
            return 35;
        } else if (pageSize.equals("A4") && numberOfFaces.equals("3 Faces") && pickupType.equals("Home") && addFrame.equals("No")) {
            return 36;
        }

        // Return a default value if no matching condition is found
        return -1;
    }

}