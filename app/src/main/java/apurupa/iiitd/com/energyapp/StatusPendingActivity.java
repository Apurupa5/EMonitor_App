package apurupa.iiitd.com.energyapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StatusPendingActivity extends AppCompatActivity implements View.OnClickListener {

    private String email;
    private static final String TAG = "StatusPendingActivity";
    private Intent serviceintent;
    private String aptno;
    private String personName,personPhotoUrl;
    private Button mSignOut;
    private GoogleApiClient mGoogleApiClient;
    private static final String mypref="UserDetails";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_pending);
        mSignOut=(Button)findViewById(R.id.SignOutButton);
        mSignOut.setOnClickListener(this);
        email=getIntent().getStringExtra("email");
        aptno=getIntent().getStringExtra("Apartment_no"); // Storing string
        personName=getIntent().getStringExtra("username");
        personPhotoUrl=getIntent().getStringExtra("photourl");
        Log.e(TAG,"In status pending activity"+ email);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();
        mGoogleApiClient.connect();

        enable_service();
           // checkStatus(email);


    }
    private void enable_service() {

        serviceintent=new Intent(this, PushService.class).putExtra("checkmail",email);
        serviceintent.putExtra("Apartment_no",aptno); // Storing string
        serviceintent.putExtra("username",personName);
        serviceintent.putExtra("photourl",personPhotoUrl);

        startService(serviceintent);

    }
    private void checkStatus(final String email) {

        JSONObject obj = null;
        JSONObject objData = null;
        JSONObject dataobjData = null;
        String databaseurl = "https://energyapp-7492d.firebaseio.com/status.json";


        StringRequest jsObjRequest = new StringRequest(Request.Method.GET, databaseurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e(TAG, "In on response" + String.valueOf(response));


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("False", error + "");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //   Log.e(TAG,"In get Headers"+getHeaders());

                Map<String, String> params = new HashMap<String, String>();

                params.put("Content-Type", "application/json");
                return params;
            }
        };

//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
//                (Request.Method.GET, databaseurl, null, new Response.Listener<JSONObject>() {
//
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        mTextView.setText("Response: " + response.toString());
//                    }
//                }, new Response.ErrorListener() {
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // TODO: Handle error
//
//                    }
//                });
//
//// Access the RequestQueue through your singleton class.
//        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds
        Log.e(TAG, "max retries" + DefaultRetryPolicy.DEFAULT_MAX_RETRIES);
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);


    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.SignOutButton:
                stopService(serviceintent);
                signOut(mGoogleApiClient,this);


        }


    }

    private void signOut(GoogleApiClient apiclient, final Context context) {


        SharedPreferences preferences =context.getSharedPreferences(mypref,Context.MODE_PRIVATE);

        Log.e("MEnuAdapter", String.valueOf(preferences.getAll()));
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        // Firebase sign out
        FirebaseAuth.getInstance().signOut();

        Auth.GoogleSignInApi.signOut(apiclient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // ...
                        Log.e("Sign out ","Logout Success");
                        Intent I =new Intent(context,MainActivity.class);
                        context.startActivity(I);
                    }
                });
    }
}
