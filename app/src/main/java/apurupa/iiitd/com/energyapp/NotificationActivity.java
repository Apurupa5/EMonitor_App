package apurupa.iiitd.com.energyapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private NotificationAdapter mAdapter;
    private List<String> optionsList = new ArrayList<String>();

    private List<String> statusesList = new ArrayList<String>();


    private TextView mprofilename, museremail;
    private static final String mypref = "UserDetails";
    private static final String TAG = "Notification Activity";
    private String aptno, email;
    FirebaseDatabase database;
    private DatabaseReference ref;
    private FirebaseAuth mauth;
    private TextView mNoNotificationTextView;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        mNoNotificationTextView=(TextView)findViewById(R.id.noNotificationtextView);
        mNoNotificationTextView.setVisibility(View.INVISIBLE);

        // toolbar
        ActionBar toolbar =getSupportActionBar();

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        SharedPreferences prefs = this.getSharedPreferences(mypref,
                Context.MODE_PRIVATE);
        aptno = prefs.getString("Apartment_no", null);
        email = prefs.getString("email_id", null);


        database= FirebaseDatabase.getInstance();
        mauth = FirebaseAuth.getInstance();
        mRecyclerView = (RecyclerView) findViewById(R.id.notification_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mProgressBar=(ProgressBar)findViewById(R.id.notificationprogressBar_cyclic);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        showProgressBar();

//
//
//        if(checkofficialmail(email))
//        {
//            // present user is official user
//        }
//        else
//        {
//            // present user is not official user
//        }

        // specify an adapter
        
        getApartmentRegistrations(aptno);





    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
               finish();
                break;
        }
        return true;
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);

    }
    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);

    }
    private void getAllRequests(final ArrayList<String> mails_list) {

        final boolean[] result = new boolean[1];
       // final ArrayList<String> mails_list =getApartmentRegistrations(aptno);
        String databaseurl = "https://energyapp-7492d.firebaseio.com/status.json";
        StringRequest jsObjRequest = new StringRequest(Request.Method.GET, databaseurl,
                new Response.Listener<String>() {


                    @Override
                    public void onResponse(String response) {

                        Log.e(TAG, "In on response getAlllrequests" + String.valueOf(response));
                        try {
                            JSONObject objData = null;
                            objData= new JSONObject(String.valueOf(response));
                            Log.e(TAG,"After converting to json obj ####"+objData);
                            Iterator<String> iter = objData.keys();
                            while (iter.hasNext()) {
                                String key = iter.next();
                                String pemail = key.replace(",", ".");
                                Log.e(TAG,"MAils list########"+mails_list+ pemail);
                                if (mails_list.contains(pemail)) {

                                    Log.e(TAG,"present");
                                    optionsList.add(pemail);
                                    statusesList.add((String) objData.get(key));
                                    Log.e(TAG,"op"+optionsList+statusesList);
                                }
                            }
                            Log.e(TAG,"After all reuests"+ optionsList +statusesList);

                            if(optionsList.size()==0)
                            {
                                mNoNotificationTextView.setVisibility(View.VISIBLE);

                            }
                            hideProgressBar();
                            mAdapter = new NotificationAdapter(NotificationActivity.this,optionsList,statusesList );

                            mRecyclerView.setAdapter(mAdapter);

                            mRecyclerView.setNestedScrollingEnabled(false);
                            Log.e(TAG,"After adapter set"+ optionsList +statusesList);
                }
                        catch (JSONException e) {
                            // Something went wrong!
                        }

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


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds
        Log.e(TAG, "max retries" + DefaultRetryPolicy.DEFAULT_MAX_RETRIES);
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);




            }

    private ArrayList<String> getApartmentRegistrations(String apartno) {
        final ArrayList<String> mails_list=new ArrayList<String>();
        String databaseurl = "https://energyapp-7492d.firebaseio.com/users.json";
        StringRequest jsObjRequest = new StringRequest(Request.Method.GET, databaseurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e(TAG, "In on response getApartmentRegistraiton" + String.valueOf(response));
                        try {
                            JSONObject objData = null;
                            objData= new JSONObject(String.valueOf(response));
                            Log.e(TAG,"After converting to json obj"+objData);
                            Iterator<String> iter = objData.keys();
                            while (iter.hasNext()) {
                                String key = iter.next();
                                try {
                                    JSONObject value = (JSONObject) objData.get(key);
                                    Log.e(TAG,"key " + value);
                                    JSONObject valueObj=value;
                                 
                                    String apt = (String) valueObj.get("aptno");
                                    if(apt.equals(aptno))
                                    {
                                        String mail= (String) valueObj.get("email");
                                        mails_list.add(mail);
                                     

                                    }
                                } catch (JSONException e) {
                                    // Something went wrong!
                                }
                            }

                            getAllRequests(mails_list);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


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


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds
        Log.e(TAG, "max retries" + DefaultRetryPolicy.DEFAULT_MAX_RETRIES);
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);


        return mails_list;
    }


    private boolean checkofficialmail(final String email1) {

        final boolean[] result = new boolean[1];
        String databaseurl = "https://energyapp-7492d.firebaseio.com/official_email.json";
        StringRequest jsObjRequest = new StringRequest(Request.Method.GET, databaseurl,
                new Response.Listener<String>() {
                   

                    @Override
                    public void onResponse(String response) {

                        Log.e(TAG, "In on response" + String.valueOf(response));
                        try {
                            JSONObject objData = null;
                            objData= new JSONObject(String.valueOf(response));
                            Log.e(TAG,"After converting to json obj"+objData);
                            String official_mail= (String) objData.get(aptno);

                            
                            if(email1.equals(official_mail))
                                    {
                                       result[0] =true;
                                    }
                            else{
                               result[0] =false;
                            }
                                } catch (JSONException e) {
                                    // Something went wrong!
                                }
                         
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


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds
        Log.e(TAG, "max retries" + DefaultRetryPolicy.DEFAULT_MAX_RETRIES);
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);

        return result[0];

    }


}
