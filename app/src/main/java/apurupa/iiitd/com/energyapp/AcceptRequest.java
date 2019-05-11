package apurupa.iiitd.com.energyapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

public class AcceptRequest extends AppCompatActivity implements View.OnClickListener {

    private TextView mTextView;
    private Button mAcceptButton, mIgnoreButton;
    private String requestor_email;
    private FirebaseDatabase database;
    private static final String TAG = "FCM Accept REquest";
    private static final String mypref = "UserDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "In Accept request");
        setContentView(R.layout.activity_accept_request);
        mTextView = (TextView) findViewById(R.id.AcceptRequestTextView);
        requestor_email = getIntent().getStringExtra("requestor_email");
        mTextView.setText("You have app installation request from " + requestor_email);
        Log.e(TAG, "requestor_email" + requestor_email);
        mAcceptButton = (Button) findViewById(R.id.acceptButton);
        mIgnoreButton = (Button) findViewById(R.id.ignoreButton);
        mAcceptButton.setOnClickListener(this);
        mIgnoreButton.setOnClickListener(this);
        database = FirebaseDatabase.getInstance();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.acceptButton:
                Intent T1 = new Intent(this, TabBarActivity.class);
                startActivity(T1);
                acceptRequest();
                break;
            case R.id.ignoreButton:
                Intent T = new Intent(this, TabBarActivity.class);
                startActivity(T);
                break;
        }
    }

    private void acceptRequest() {
        Log.e(TAG, "In Accept request method");
        String st = "status".toString();
        String email1 = requestor_email.replace(".", ",");
        database.getReference().child(st).child(email1).setValue("accepted");

        SharedPreferences prefs = this.getSharedPreferences(mypref,
                Context.MODE_PRIVATE);
        String aptno = prefs.getString("Apartment_no", null);
        saveApartmentData(requestor_email, aptno);
    }


    private void saveApartmentData(final String requestor_email, final String aptno) {
        final DatabaseReference ref = database.getReference().child("apartment_details").child(aptno);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ApartmentDetails puser = dataSnapshot.getValue(ApartmentDetails.class);

                if (!dataSnapshot.exists()) {

                    Log.e(TAG, "after save data");
                } else {
                    //Already present
                    List<String> prevlist = puser.email;
                    prevlist.add(requestor_email);
                    puser.email = prevlist;
                    ref.setValue(puser);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
