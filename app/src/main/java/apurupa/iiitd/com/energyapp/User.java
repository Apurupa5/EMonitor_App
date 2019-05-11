package apurupa.iiitd.com.energyapp;

/**
 * Created by NB VENKATESHWARULU on 3/29/2018.
 */
public class User {
    public String email;
    public String aptno;
    public String uid;
    public  User()
    {

    }

    public User(String uid,String email,String aptno)
    {
        this.email=email;
        this.uid=uid;
        this.aptno=aptno;
    }
}
