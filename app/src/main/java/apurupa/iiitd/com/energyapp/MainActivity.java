package apurupa.iiitd.com.energyapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GoogleActivity";
    private static final String mypref="UserDetails";
    private static final int RC_SIGN_IN = 9001;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private DatabaseReference dref;

    FirebaseDatabase database;
    private DatabaseReference ref;
    private String personName,personPhotoUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG,"In create"+ FirebaseInstanceId.getInstance().getToken());


        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        database= FirebaseDatabase.getInstance();
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        Log.e(TAG,"In start");



        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.e(TAG, "In start current user "+String.valueOf(currentUser));

        updateUI(currentUser);
    }
    // [END on_start_check_user]

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                personName = account.getDisplayName();
                personPhotoUrl = account.getPhotoUrl().toString();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }


    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.e(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "signInWithCredential:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            final String uid=user.getUid();
                            //User new_user=new User(user.getUid(),user.getEmail(),"AWW");
                            //Log.e(TAG,"type "+ user.getEmail().getClass());
                            //new_user.setEmail_id(user.getEmail().toString());
                            //new_user.setUid(String.valueOf(user.getUid()));
                            //new_user.setApartment_no("AWW".toString());

                            //Log.e(TAG,"type "+ new_user.getClass().isArray());

                            //Map<String,Object> new_users_list = new HashMap<>();
                            //new_users_list.put(uid,new_user);
                            //List<User> list=new ArrayList<User>();
                            //list.add(new_user);





                          ref= database.getReference().child("users").child(uid);

                             ref.addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    User puser = dataSnapshot.getValue(User.class);
                                    Log.e(TAG," On data change "+user + "eixst "+String.valueOf(dataSnapshot.exists()));

                                    if(!dataSnapshot.exists())
                                    {



                                        Intent I =new Intent(getApplicationContext(),ApartmentDetailsActivity.class);
                                        startActivity(I);




                                    }

                                    else
                                    {
                                        Log.e(TAG ,"Already saved "+user);
                                        updateUI(user);

                                    }

                                    // do something with this user or save it
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                       // hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    // [START signin]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
       // hideProgressDialog()
        Log.e(TAG,"In update UI");
        if (user != null) {
            SharedPreferences prefs = getSharedPreferences(mypref,
                    Context.MODE_PRIVATE);
            if (prefs.getString("email_id",null)==null || prefs.getString("Apartment_no",null)==null)
            {
                Log.e(TAG,"SHared are null");
                getApartmentNo(user.getEmail(),user.getUid(),prefs);


            }

            else {
                String st=checkRegistrationStatus(user.getEmail(),null);
//                if(st.equals("accepted")) {
//                    Intent i = new Intent(getApplicationContext(), TabBarActivity.class);
//                    startActivity(i);
//                }
//                else {
//                    //move to a new screen
//                    Log.e(TAG,"Status is still "+ st);
//                    Intent i = new Intent(getApplicationContext(), StatusPendingActivity.class);
//                    startActivity(i);
//
//                }
            }
        } else {

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);


        }
    }

    private String checkRegistrationStatus(final String pemail, final User puser) {
        final String[] presStatus = new String[1];

        checkStatus(new OnGetDataListener() {
           // public String[] presStatus;

            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {

                Log.e(TAG,"In check registration"+pemail);

              final String email1=pemail.replace(".", ",");
                Log.e(TAG,email1);

                if(!dataSnapshot.exists())
                {
                    Log.e(TAG,"In check reg.datadoes not exist");
                }
                for (DataSnapshot children: dataSnapshot.getChildren()) {
                    Object val = children.getValue();
                    if(children.getKey().toString().equals(email1))
                    {
                        presStatus[0] = (String) children.getValue();
                        break;
                    }
                    Log.e(TAG,"datasnapshot vals"+val + "child" + children);

                }

                Log.e(TAG,"Retrieving status" +presStatus[0]);
                if(presStatus[0]!=null && presStatus[0].equals("pending"))
                {
                    //move to the status pending screen
                    Log.e(TAG,"Moving to status pending screen" + pemail);

                    Intent i = new Intent(getApplicationContext(), StatusPendingActivity.class).putExtra("email",pemail);;

                 //   i.putExtra("Apartment_no", puser.aptno); // Storing string
                    i.putExtra("username",personName);
                    i.putExtra("photourl",personPhotoUrl);
                    startActivity(i);
                }
                else

                {
//                   Bundle intent=getIntent().getExtras();
//                    if(intent!=null)
//                    {
//                        String req_email=intent.getString("requestor_email");
//                        for (String key : getIntent().getExtras().keySet()) {
//                            String value = getIntent().getExtras().getString(key);
//                            Log.e(TAG, "Key: " + key + " Value: " + value);
//                        }
//                        Intent i = new Intent(getApplicationContext(), AcceptRequest.class);
//                        i.putExtra("requstor_email",req_email);
//                        startActivity(i);
//
//                    }

                    if(puser ==null)
                    {
                        Log.e(TAG,"puser is null");
                        Intent i = new Intent(getApplicationContext(), TabBarActivity.class);
                        startActivity(i);

                    }
                    else

                    {
                        Log.e(TAG,"puser is not null");
                        final String[] no = new String[1];
                        SharedPreferences prefs = getSharedPreferences(mypref,
                                Context.MODE_PRIVATE);
                        final SharedPreferences.Editor editor = prefs.edit();
                        no[0] = puser.aptno;

                        editor.putString("email_id", puser.email); // Storing string
                        editor.putString("Apartment_no", puser.aptno); // Storing string
                        editor.putString("username",personName);
                        editor.putString("photourl",personPhotoUrl);

                        editor.commit();

                        Log.e(TAG, "puser" + puser.aptno);
                        Intent i = new Intent(getApplicationContext(), TabBarActivity.class);
                        startActivity(i);

                    }
                }
          }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {

            }
            //return presStatus[0];
        });


        return presStatus[0];
    }


    public void checkStatus(final OnGetDataListener listener) {

        listener.onStart();


        ref=database.getReference().child("status");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long startTime = System.currentTimeMillis();
                Log.e(TAG,"before on data change");
                listener.onSuccess(dataSnapshot);
                Log.e(TAG,"On data change");
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                Log.e(TAG, String.valueOf(elapsedTime));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure();
            }
        });

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            signIn();
        }


    }
//
//    private String getApartmentNo(String email,String uid,SharedPreferences prefs) {
//
//        final String[] no = new String[1];
//        final SharedPreferences.Editor editor = prefs.edit();
//
//
//        ref=database.getReference().child("users").child(uid.trim());
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                User puser = dataSnapshot.getValue(User.class);
//                Log.e(TAG,String.valueOf(puser));
//                if(dataSnapshot.exists())
//                {
//                    Log.e(TAG,String.valueOf(puser.aptno)+String.valueOf(puser.email));
//                    no[0] =puser.aptno;
//
//                    editor.putString("email_id",puser.email); // Storing string
//                    editor.putString("Apartment_no", puser.aptno); // Storing string
//                    editor.commit();
//
//                    Log.e(TAG,"puser"+puser.aptno);
//
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//
//
//        });
////        Log.e(TAG,"In user not null");
////        Log.e(TAG,"email"+user.getEmail());
////        Log.e("email",user.getEmail());
////        Log.e("uid",user.getUid());
//        //findViewById(R.id.sign_in_button).setVisibility(View.GONE);
//
//
//        return no[0];
//    }


    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(DataSnapshot dataSnapshot);
        void onStart();
        void onFailure();
    }
    public void getData(String uid, final OnGetDataListener listener) {
        Log.e(TAG,"get time series" +uid.trim());
        listener.onStart();


        ref=database.getReference().child("users").child(uid.trim());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long startTime = System.currentTimeMillis();
                Log.e(TAG,"before on data change");
                listener.onSuccess(dataSnapshot);
                Log.e(TAG,"On data change");
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                Log.e(TAG, String.valueOf(elapsedTime));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure();
            }
        });

    }

    private void getApartmentNo(String email, String uid, final SharedPreferences prefs) {

        getData(uid, new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                final String[] no = new String[1];
                final SharedPreferences.Editor editor = prefs.edit();
                User puser = dataSnapshot.getValue(User.class);
                if (puser==null)
                {
                    // user details not saved in database
                    Log.e(TAG,"In if puser");
                    Intent I =new Intent(getApplicationContext(),ApartmentDetailsActivity.class);
                    startActivity(I);


                }
                else {
                    Log.e(TAG, "puser" + String.valueOf(puser));
                    Log.e(TAG, "On data Get listener" + String.valueOf(puser.aptno) + String.valueOf(puser.email));
                    //String email1=puser.email.replace(".", ",");

                    if (dataSnapshot.exists()) {
                        String st=checkRegistrationStatus(puser.email,puser);
                        Log.e(TAG,"After check registration"+st);
//                        if(st!=null && st.equals("accepted")) {
//                            no[0] = puser.aptno;
//
//                            editor.putString("email_id", puser.email); // Storing string
//                            editor.putString("Apartment_no", puser.aptno); // Storing string
//                            editor.putString("username",personName);
//                            editor.putString("photourl",personPhotoUrl);
//
//                            editor.commit();
//
//                            Log.e(TAG, "puser" + puser.aptno);
//                            Intent i = new Intent(getApplicationContext(), TabBarActivity.class);
//                            startActivity(i);
//                        }
//                        else {
//                            //move to a new screen
//                            Log.e(TAG,"Status is still "+ st);
//                            Intent i = new Intent(getApplicationContext(), StatusPendingActivity.class);
//                            startActivity(i);
//
//                        }
                        Log.e(TAG, "In datanapshot exists" + String.valueOf(puser.aptno) + String.valueOf(puser.email));


                    }
//                    Intent i = new Intent(getApplicationContext(), TabBarActivity.class);
//                    startActivity(i);
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {

            }

        });
        return ;
    }

        }
