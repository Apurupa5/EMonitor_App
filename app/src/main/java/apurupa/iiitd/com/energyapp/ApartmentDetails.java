package apurupa.iiitd.com.energyapp;

import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NB VENKATESHWARULU on 3/29/2018.
 */
public class ApartmentDetails {

    public  String ApartmentNo;
    public List<String> email;
    public String adults;
    public String children;
   // public String family_group;
    public  ApartmentDetails()
    {

    }

    public ApartmentDetails(List<String> email, String ApartmentNo, String adults, String children)
    {
        this.email=email;
        this.ApartmentNo=ApartmentNo;
        this.adults=adults;
        this.children=children;
        //this.family_group=family_group;


    }
}
