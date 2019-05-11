package apurupa.iiitd.com.energyapp;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by NB VENKATESHWARULU on 5/21/2018.
 */
public class DatabaseHelper {



//    private void saveApartmentData(final String requestor_email, final String aptno) {
//      //  final DatabaseReference ref = database.getReference().child("apartment_details").child(aptno);
//
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                ApartmentDetails puser = dataSnapshot.getValue(ApartmentDetails.class);
//
//                if (!dataSnapshot.exists()) {
//
//                    //Log.e(TAG, "after save data");
//                } else {
//                    //Already present
//                    List<String> prevlist = puser.email;
//                    prevlist.add(requestor_email);
//                    puser.email = prevlist;
//                    ref.setValue(puser);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
}

