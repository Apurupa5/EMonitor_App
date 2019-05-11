package apurupa.iiitd.com.energyapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ApartmentDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private static ApartmentDetails AD =null ;
    private Button mSaveButton;
    private Spinner mAdultSpinner,mChildrenSpinner,mFamilyGroupSpinner;
    private TextView mApartmentView,mAdultView,mChildrenView,mFamilyGroupView;
    private EditText mApartmentNo;

    private String TAG="Apartment Details";
    private static final String mypref="UserDetails";



    FirebaseDatabase database;
    private DatabaseReference ref;
    private FirebaseAuth mauth;
    private Button mCancelButton;
    private String official_email;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apartment_details);


        mSaveButton = (Button)findViewById(R.id.saveButton);
        //mCancelButton=(Button)findViewById(R.id.cancelButton);
        mAdultSpinner=(Spinner)findViewById(R.id.AdultsSpinner);
        mChildrenSpinner=(Spinner)findViewById(R.id.ChildrenSpinner);
       // mFamilyGroupSpinner=(Spinner)findViewById(R.id.FamilyGroupSpinner);
        mApartmentNo=(EditText)findViewById(R.id.ApartmentNo_text_input);



        mApartmentView=(TextView)findViewById(R.id.ApartmentNoTextView);
        mAdultView=(TextView)findViewById(R.id.AdultsTextView);
        mChildrenView=(TextView)findViewById(R.id.ChildrenTextView);
       // mFamilyGroupView=(TextView)findViewById(R.id.FamilyGroupTextView);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();
        mGoogleApiClient.connect();
        mSaveButton.setOnClickListener(this);
       // mCancelButton.setOnClickListener(this);
        addItemsOnSpinners();

        database= FirebaseDatabase.getInstance();
        mauth = FirebaseAuth.getInstance();
    }

    private void addItemsOnSpinners() {
        List<String> adult_number_list=new ArrayList<String>();

        adult_number_list.add("1");
        adult_number_list.add("2");
        adult_number_list.add("3");
        adult_number_list.add("4");
        adult_number_list.add(">=5");



        ArrayAdapter<String>adapter = new ArrayAdapter<String>(ApartmentDetailsActivity.this,
                android.R.layout.simple_spinner_item,adult_number_list);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAdultSpinner.setAdapter(adapter);
        int spinnerPosition = mAdultSpinner.pointToPosition(1,1);

        mAdultSpinner.setSelection(spinnerPosition);


        List<String> children_number_list=new ArrayList<String>();

        children_number_list.add("None");
        children_number_list.add("1");
        children_number_list.add("2");
        children_number_list.add("3");
        children_number_list.add(">=4");



        adapter = new ArrayAdapter<String>(ApartmentDetailsActivity.this,
                android.R.layout.simple_spinner_item,children_number_list);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChildrenSpinner.setAdapter(adapter);
        spinnerPosition = mAdultSpinner.pointToPosition(1,1);
        mChildrenSpinner.setSelection(spinnerPosition);

//        List<String> family_group_list=new ArrayList<String>();
//
//        family_group_list.add("Working Couple");
//        family_group_list.add("Couple");
//        family_group_list.add("Single");
//
//
//        adapter = new ArrayAdapter<String>(ApartmentDetailsActivity.this,
//                android.R.layout.simple_spinner_item,family_group_list);
//
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mFamilyGroupSpinner.setAdapter(adapter);
        //spinnerPosition = mAdultSpinner.pointToPosition(1,1);

        //mFamilyGroupSpinner.setSelection(spinnerPosition);
    }

    @Override
    public void onBackPressed()
    {
        // code here to show dialog
        return;
      //  super.onBackPressed();  // optional depending on your needs
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.saveButton:
                Log.e(TAG, "In on click");
                String nadults = mAdultSpinner.getSelectedItem().toString().trim();
                String nchildren = mChildrenSpinner.getSelectedItem().toString().trim();
               // String family_grp = mFamilyGroupSpinner.getSelectedItem().toString().trim();
                String aptno = mApartmentNo.getText().toString().trim();
                int flag = 0;
                Log.e(TAG, nadults);
                Log.e(TAG, String.valueOf(aptno.equals("")));
             //   Log.e(TAG, nchildren + family_grp);

                if (TextUtils.isEmpty(aptno)) {
                    flag = 1;
                    mApartmentNo.setError("Please fill Apartment No");
                    return;
                }

                if (flag == 0) {

                    if (mauth != null) {
                        String email = mauth.getCurrentUser().getEmail().toString().trim();
                        String uid = mauth.getUid().toString().trim();


                        List<String> elist=new ArrayList<>();
                        elist.add(email);
                        AD = new ApartmentDetails(elist, aptno, nadults, nchildren);


                        Log.e(TAG, email + uid);
                        Log.e(TAG, mauth.getCurrentUser().getUid().toString().trim());
                        saveDataToDatabase(email, uid, AD);
                    }
                }
                break;
//            case R.id.cancelButton:
////                Intent returnIntent = new Intent();
////                setResult(RESULT_CANCELED, returnIntent);
////                finishActivity(1);
//                break;
        }

    }

    private void saveDataToDatabase(final String email, final String uid, final ApartmentDetails ad) {

        Log.e(TAG,"In save data");

        // check if the email is official email or not
        User new_user=new User(uid,email,ad.ApartmentNo);
        database.getReference().child("users").child(uid).setValue(new_user);

        String token=FirebaseInstanceId.getInstance().getToken();
        String new_email=email.replace(".",",");
        database.getReference().child("tokens").child(new_email).setValue(token);

      checkofficialemail( uid,email,ad);
////
//        ref= database.getReference().child("apartment_details").child(ad.ApartmentNo);
//
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                ApartmentDetails puser = dataSnapshot.getValue(ApartmentDetails.class);
//
//                if(!dataSnapshot.exists())
//                {
//                    ref.setValue(ad);
//                    Log.e(TAG,"after save data");
//                                    }
//
//                else
//                {
//                    //Already present
//                    List<String> prevlist=puser.email;
//                    prevlist.add(email);
//                    ad.email=prevlist;
//                    ref.setValue(ad);
//                }
//
//
//                // do something with this user or save it
//
//                User new_user=new User(uid,email,ad.ApartmentNo);
//                database.getReference().child("users").child(uid).setValue(new_user);
//
//                String token=FirebaseInstanceId.getInstance().getToken();
//                database.getReference().child("tokens").child(email).setValue(token);
//
//
//
//                SharedPreferences pref = getApplicationContext().getSharedPreferences( "UserDetails",  Context.MODE_PRIVATE); // 0 - for private mode
//                SharedPreferences.Editor editor = pref.edit();
//
//                editor.putString("email_id",email); // Storing string
//                editor.putString("Apartment_no",ad.ApartmentNo); // Storing string
//                editor.putString("username",mauth.getCurrentUser().getDisplayName());
//                editor.putString("photourl", String.valueOf(mauth.getCurrentUser().getPhotoUrl()));
//
//                editor.commit();
//
//                Intent I =new Intent(getApplicationContext(),TabBarActivity.class);
//                startActivity(I);
//        }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
//
//            }
//        });
//
//
//        database.getReference().child("apartment_details").child(ad.ApartmentNo).setValue(ad);
//        Log.e(TAG,"after save data");



    }

    private void checkofficialemail(final String uid, final String email, final ApartmentDetails ad) {

        ref= database.getReference().child("official_email").child(ad.ApartmentNo);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {



                if(!dataSnapshot.exists())
                {

                    Log.e(TAG,"Official email data does not exist");
                }

                else
                {
                    //Already present
                    Object dt = dataSnapshot.getValue();
                    Log.e(TAG,"Available"+dt.toString());
                    official_email=dt.toString();
                    if(!official_email.equals(email))
                    {
                        //save status in database

                        String st="status".toString();
                        String email1=email.replace(".", ",");
                        database.getReference().child(st).child(email1).setValue("pending");
                        Log.e(TAG,"Emails###########"+email+" "+email1);
                        sendFCMPush(email); //sending request to the official user.

                    }
                    else
                    {
                        saveofficialData(uid,email,ad);



                    }




                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

            }
        });

    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Uncomment the below code to Set the message and title from the strings.xml file
        //builder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);

        //Setting message manually and performing action on button click
        builder.setMessage("You need permission from the admin to register for this apartment. Permission request sent.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Access Requested");
        alert.show();
        setContentView(R.layout.activity_apartment_details);

    }


    private void saveofficialData(final String uid, final String email, final ApartmentDetails ad) {
        ref= database.getReference().child("apartment_details").child(ad.ApartmentNo);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ApartmentDetails puser = dataSnapshot.getValue(ApartmentDetails.class);

                if(!dataSnapshot.exists())
                {
                    ref.setValue(ad);
                    Log.e(TAG,"after save data");
                }

                else
                {
                    //Already present
                    List<String> prevlist=puser.email;
                    prevlist.add(email);
                    ad.email=prevlist;
                    ref.setValue(ad);
                }



                SharedPreferences pref = getApplicationContext().getSharedPreferences( "UserDetails",  Context.MODE_PRIVATE); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();

                editor.putString("email_id",email); // Storing string
                editor.putString("Apartment_no",ad.ApartmentNo); // Storing string
                editor.putString("username",mauth.getCurrentUser().getDisplayName());
                editor.putString("photourl", String.valueOf(mauth.getCurrentUser().getPhotoUrl()));

                editor.commit();

                Intent I =new Intent(getApplicationContext(),TabBarActivity.class);
                startActivity(I);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendFCMPush(String from_email) {
     getToken(from_email);


    }

    private void notifyUser(final String from_email, String token) {
        String SERVER_KEY = getString(R.string.server_api_key);
        String msg = from_email;
        String title = "Installation Request";
        //token = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG,"token"+token);
        //String token="eLQF6sozPp8:APA91bG2FkdG3aD4jFztuW8YoLmbG1k4TSALkHwJVKIcdcwxJcgDdbUp_CDbKj42VzZIRo8LXYw_056-YOK4IvqKydhAkdvmEbNKcYgkUXYdKdJt9wH8EYwbz-qWMJ3QSHEkwSm0nu2p";
        JSONObject obj = null;
        JSONObject objData = null;
        JSONObject dataobjData = null;

        try {
            obj = new JSONObject();
            objData = new JSONObject();

            objData.put("From", msg);
            objData.put("title", title);
            objData.put("sound", "default");
            objData.put("icon", "icon_name"); //   icon_name
            objData.put("tag", token);
            objData.put("priority", "high");
            objData.put("body",msg);

            dataobjData = new JSONObject();
            dataobjData.put("text", msg);
            dataobjData.put("title", title);

            obj.put("to", token);
            //obj.put("priority", "high");

            obj.put("notification", objData);
            obj.put("data", dataobjData);
            Log.e("return here>>", obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.e(TAG,"FCM url" +getString(R.string.FCM_PUSH_URL) );
        Log.e(TAG,"Json obj in notify #####"+obj.toString());
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.FCM_PUSH_URL), obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("True", response + "");
                        checkStatus(from_email);
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
                Log.e(TAG,"Server api key" +getString(R.string.server_api_key) );
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "key=" +getString(R.string.server_api_key));
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);
    }

    private void checkStatus(String from_email) {
                       String email1=from_email.replace(".",",");
                        DatabaseReference ref1 = database.getReference().child("status").child(email1);

                         ref1.addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(DataSnapshot dataSnapshot) {
                                 String status= (String) dataSnapshot.getValue();
                                 Log.e(TAG,"Retrieving status" + status);
                                 if(status.equals("accepted"))
                                 {
                                     String email = mauth.getCurrentUser().getEmail().toString().trim();
                                     String uid = mauth.getUid().toString().trim();
                                     saveofficialData(uid, email,AD);

                                 }
                                 else
                                 {
                                    showAlertDialog();
                                 }
                             }

                             @Override
                             public void onCancelled(DatabaseError databaseError) {

                             }
                         });

    }


    private void getToken(final String from_email) {
        Log.e(TAG,"In method get token");
        String databaseurl = "https://energyapp-7492d.firebaseio.com/tokens.json";
        final String token_email=official_email.replace(".",",");

        StringRequest jsObjRequest = new StringRequest(Request.Method.GET, databaseurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e(TAG, "In on response #########" + String.valueOf(response));
                       try {
                            JSONObject objData = null;
                            objData= new JSONObject(String.valueOf(response));

                           Log.e(TAG,"Hello" +token_email);
                           String token=null;
                           if(objData.has(token_email)) {
                               token = (String) objData.get(token_email);
                               Log.e(TAG, "After converting to json obj" + objData + "token " + token);
                               Log.e(TAG, "Hello2");
                               Log.e(TAG, "Token" + token);
                               notifyUser(from_email,token);
                           }
                           else
                           {
                               Log.e(TAG,"not present");
                               showOfficialAlertDialog();

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
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);

    }

    private void showOfficialAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Uncomment the below code to Set the message and title from the strings.xml file
        //builder.setMessage(R.string.dialog_message) .setTitle(R.string.dialog_title);

        //Setting message manually and performing action on button click
        builder.setMessage("Please register with a official mail id")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        signOut(mGoogleApiClient,getApplicationContext());
                        //finish();
                    }
                });

        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Invalid User!");
        alert.show();
        setContentView(R.layout.activity_apartment_details);
    }

    private void signOut(GoogleApiClient apiclient, final Context context) {


        SharedPreferences preferences =context.getSharedPreferences(mypref,Context.MODE_PRIVATE);
        final String uid = mauth.getUid().toString().trim();
        Log.e(TAG, String.valueOf(preferences.getAll()));
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
                        Log.e(TAG,"Sign out "+ "Logout Success");

                        database.getReference().child("users").child(uid).setValue(null);

                        Intent I =new Intent(context,MainActivity.class);
                        I.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(I);
                    }
                });
    }

}
