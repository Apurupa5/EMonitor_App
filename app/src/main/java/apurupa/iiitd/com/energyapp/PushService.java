package apurupa.iiitd.com.energyapp;

/**
 * Created by NB VENKATESHWARULU on 5/14/2018.
 */

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class PushService extends Service {
    // constant
    public static final long NOTIFY_INTERVAL = 5 * 1000; // 10 seconds

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;
    private ProgressDialog pDialog;
    private static String firebase_url ="https://energyapp-7492d.firebaseio.com/status.json";
    public ArrayList<String> longitudelist=new ArrayList<String>();
    public ArrayList<String> latitudelist=new ArrayList<String>();
    public static String TAG="PushService";
    public boolean flag;
    private String check_email,presStatus="pending";
    private String aptno,personName,personPhotoUrl;
    private static final String mypref="UserDetails";


    @Override
    public IBinder onBind(Intent intent) {
        String data=(String) intent.getExtras().get("checkmail");
        Log.e(TAG,"In on Bind"+data);
        return null;
    }


    public int onStartCommand (Intent intent, int flags, int startId) {
        String data=(String) intent.getExtras().get("checkmail");
        //aptno=intent.getStringExtra("Apartment_no"); // Storing string

        personName=intent.getStringExtra("username");
        personPhotoUrl=intent.getStringExtra("photourl");
        check_email=data;

        aptno= retrieveAptno(check_email);
        Log.e(TAG,"In on start Command"+ aptno);

        return flags;
    }
    @Override
    public void onCreate() {
        // cancel if already existed


        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
            Log.e(TAG,"In on create");

        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    static public boolean isURLReachable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL("http://192.168.58.165");   // Change to "http://google.com" for www  test.
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(10 * 1000);          // 10 s.
                urlc.connect();
                if (urlc.getResponseCode() == 200) {        // 200 = "OK" code (http connection is fine).
                    Log.wtf("Connection", "Success !");
                    return true;
                } else {
                    return false;
                }
            } catch (MalformedURLException e1) {
                return false;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isNetworkConnected()) {
                        Log.e(TAG,"In Display Timer Task");
                        String status = "available";
                        new getRegistrationStatus().execute(status);
                        if(presStatus.equals("accepted"))
                        {
                            mTimer.cancel();
                            mTimer.purge();
                            disableService();
                            Log.e(TAG,"setting preferences");


                            SharedPreferences prefs =getSharedPreferences(mypref,
                                    Context.MODE_PRIVATE);
                            aptno= prefs.getString("Apartment_no",null);

                            Log.e(TAG,"Present apartment in checking "+aptno);

                            Log.e(TAG,"After setting preferences");
                            Intent i = new Intent(getApplicationContext(), TabBarActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            startActivity(i);

                        }

//                        Intent I = new Intent("location_update");
//                        I.putStringArrayListExtra("longitude", longitudelist);
//                        I.putStringArrayListExtra("latitude", latitudelist);
//                        sendBroadcast(I);
                    }

                }

            });
        }


    }
//    @Override
//    public boolean stopService(Intent name) {
//        // TODO Auto-generated method stub
//
//        return super.stopService(name);
//
//    }

    private void disableService() {
        stopSelf();
    }

    private String retrieveAptno(final String check_email) {
        JSONObject obj = null;

        JSONObject dataobjData = null;
        String databaseurl = "https://energyapp-7492d.firebaseio.com/users.json";

        final String[] aptno = {null};

        StringRequest jsObjRequest = new StringRequest(Request.Method.GET, databaseurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e(TAG, "In on response" + String.valueOf(response));
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
                                    String mail= (String) valueObj.get("email");
                                    if(mail.equals(check_email))
                                    {
                                        aptno[0] = (String) valueObj.get("aptno");
                                        Log.e(TAG,"Aptnnnnn"+aptno[0]);
                                        //aptno=aptno[0];
                                        SharedPreferences prefs = getSharedPreferences(mypref,
                                                Context.MODE_PRIVATE);
                                        final SharedPreferences.Editor editor = prefs.edit();


                                        editor.putString("email_id",check_email); // Storing string
                                        editor.putString("Apartment_no",aptno[0]); // Storing string
                                        editor.putString("username",personName);
                                        editor.putString("photourl",personPhotoUrl);

                                        editor.commit();
                                        break;
                                    }
                                } catch (JSONException e) {
                                    // Something went wrong!
                                }
                            }
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

        return aptno[0];
    }


    private boolean checkserverconnectivity() {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //Your code goes here
                    flag= isURLReachable(getApplicationContext());
                    Log.e("SERVER",String.valueOf(flag));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        Log.e("SERVER2",String.valueOf(flag));
        return flag;
    }

    class getRegistrationStatus extends AsyncTask<String, String, String> {

        /**
         Show Progress Dialog
         * */
        private String authstatus="";
        @Override
        protected void onPreExecute() {
            Log.e(TAG,"In pre Execute");
            super.onPreExecute();
//            pDialog = new ProgressDialog(TrialMapsActivity.this);
//            pDialog.setMessage("Getting Details..");
//            pDialog.setIndeterminate(false);
//            pDialog.setCancelable(true);
//            pDialog.show();
        }

        /**
         * Creating account
         * */
        protected String doInBackground(String... args) {
            String response = "";

                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(firebase_url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            Log.e(TAG,"response"+response);
            return response;


        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String response) {
            // dismiss the dialog once done
            // pDialog.dismiss();
            Log.e(TAG,"In post execute");

            try {

                JSONObject jsonresponse=new JSONObject(response);
                String key=check_email.replace(".",",");
                String status= (String) jsonresponse.get(key);
                presStatus=status;
                Log.e(TAG,"status"+status);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
   }