package apurupa.iiitd.com.energyapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UpdateApartmentDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mUpdateButton;
    private Button mBackButton;
    private Spinner mAdultSpinner,mChildrenSpinner;
    private TextView mApartmentView,mAdultView,mChildrenView;
    private TextView mApartmentNo;

    private static final String TAG="Update Apartment";
    private static final String mypref="UserDetails";
    FirebaseDatabase database;
    private DatabaseReference ref;
    private FirebaseAuth mauth;
    private String presentApt;
    private String aptno;
    private String nadults,nchildren;
    private int apos,cpos;
    private List<String> demail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_apartment_details);


        mUpdateButton = (Button)findViewById(R.id.updateButton);
        mBackButton=(Button)findViewById(R.id.backButton);

        mAdultSpinner=(Spinner)findViewById(R.id.UpdateAdultsSpinner);
        mChildrenSpinner=(Spinner)findViewById(R.id.UpdateChildrenSpinner);

        mApartmentNo=(TextView)findViewById(R.id.UpdateApartmentNo_text_input);



        mApartmentView=(TextView)findViewById(R.id.ApartmentNoTextView);
        mAdultView=(TextView)findViewById(R.id.AdultsTextView);
        mChildrenView=(TextView)findViewById(R.id.ChildrenTextView);


        mUpdateButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);

        SharedPreferences prefs = this.getSharedPreferences(mypref,
                Context.MODE_PRIVATE);
        aptno= prefs.getString("Apartment_no",null);
        mApartmentNo.setText(String.valueOf(aptno));
        String apt="Apartment"+aptno;
        presentApt=apt;


        database= FirebaseDatabase.getInstance();
        Log.e(TAG,"db"+String.valueOf(database));
        mauth = FirebaseAuth.getInstance();


        getDataFromDatabase(aptno);


    }


    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(DataSnapshot dataSnapshot);
        void onStart();
        void onFailure();
    }

    public void getData(String aptno, final OnGetDataListener listener) {
        Log.e(TAG,"get time series" +aptno.trim());
        listener.onStart();


        ref=database.getReference().child("apartment_details").child(aptno);

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


    private void getDataFromDatabase(String aptno) {

        getData(aptno, new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {

                ApartmentDetails ap_details= dataSnapshot.getValue(ApartmentDetails.class);
                if (ap_details==null)
                {
                    // user details not saved in database
                    Log.e(TAG,"Details not present");


                }
                else {

                    if (dataSnapshot.exists()) {

                       nadults= ap_details.adults;
                        nchildren=ap_details.children;
                        demail=ap_details.email;

                        Log.e(TAG,"Datasnapshot " + nadults +"CCCC" +nchildren + demail);

                    }

                }

                addItemsOnSpinners();

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







    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.backButton:
                //back button pressed
                Log.e(TAG,"back pressed");
                finish();
//                Intent I =new Intent(this,MenuFragment.class);
//                startActivity(I);
                break;
            case R.id.updateButton:
                int  cpos1=mChildrenSpinner.getSelectedItemPosition();
                int apos1=mAdultSpinner.getSelectedItemPosition();
                Log.e(TAG,"Selected "+cpos1 +" sf" + apos1 + cpos + apos);
                if(cpos1==cpos && apos1==apos)
                {
                    //no changes has been made
                    Log.e(TAG,"No changes");
                    finish();
                }
                else
                {
                    Log.e(TAG,"In else");
                    String email="";
                    String curr_email = mauth.getCurrentUser().getEmail().toString().trim();
                    String uid = mauth.getUid().toString().trim();
                    String selectedadults = mAdultSpinner.getSelectedItem().toString().trim();
                    String selectedchildren = mChildrenSpinner.getSelectedItem().toString().trim();
                    if(!demail.contains(curr_email))
                    {
                       demail.add(curr_email);
                    }

                    ApartmentDetails ad = new ApartmentDetails(demail, aptno, selectedadults, selectedchildren);
                    database.getReference().child("apartment_details").child(ad.ApartmentNo).setValue(ad);
                    finish();
                }

        }

    }


    private void addItemsOnSpinners() {
        Log.e(TAG,"In add items " + nchildren + nadults);
        List<String> adult_number_list=new ArrayList<String>();

        adult_number_list.add("1");
        adult_number_list.add("2");
        adult_number_list.add("3");
        adult_number_list.add("4");
        adult_number_list.add(">=5");



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(UpdateApartmentDetailsActivity.this,
                android.R.layout.simple_spinner_item,adult_number_list);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAdultSpinner.setAdapter(adapter);
        int pos=adult_number_list.indexOf(nadults);
        apos=pos;

        int spinnerPosition = mAdultSpinner.pointToPosition(pos+1,1);

        mAdultSpinner.setSelection(pos);
      //  mAdultSpinner.setSelection(spinnerPosition);


        List<String> children_number_list=new ArrayList<String>();

        children_number_list.add("None");
        children_number_list.add("1");
        children_number_list.add("2");
        children_number_list.add("3");
        children_number_list.add(">=4");

        adapter = new ArrayAdapter<String>(UpdateApartmentDetailsActivity.this,
                android.R.layout.simple_spinner_item,children_number_list);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChildrenSpinner.setAdapter(adapter);
        pos=children_number_list.indexOf(nchildren);
        Log.e(TAG,"Index "+nchildren + pos + "ADult" + nadults + apos);
        cpos=pos;
        spinnerPosition = mChildrenSpinner.pointToPosition(pos+1,1);
       // mChildrenSpinner.setSelection(spinnerPosition);

        mChildrenSpinner.setSelection(pos);


        Log.e(TAG,"Child "+mChildrenSpinner.getSelectedItemPosition());
    }

}
