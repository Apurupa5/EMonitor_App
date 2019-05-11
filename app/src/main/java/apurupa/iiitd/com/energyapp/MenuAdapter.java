package apurupa.iiitd.com.energyapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Created by NB VENKATESHWARULU on 3/28/2018.
 */


public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private Context context;
    private List<String> itemList;
    private Integer[] imgid;
    private GoogleApiClient mGoogleApiClient;
    private static final String mypref="UserDetails";



    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder


    public static class ViewHolder extends RecyclerView.ViewHolder  {
        private final CardView l1;
        // each data item is just a string in this case
        public TextView mTextView;
        public ImageView mImageView;
        //private LinearLayout l1;
        public ViewHolder(View v, final GoogleApiClient apiclient,final Context context) {
            super(v);

            mTextView = (TextView) v.findViewById(R.id.listitem);
            mImageView=(ImageView)v.findViewById(R.id.icon);
            //l1=(LinearLayout)itemView.findViewById(R.id.item_linearlayout);
            Log.e("View HolD", "H444");
            l1 = (CardView) itemView.findViewById(R.id.card_view);
            l1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Log.e("CLICKED", String.valueOf(getAdapterPosition()));
                    if (getAdapterPosition() == 0) {
                        // UI for updating apartment details
                        Intent I =new Intent(context.getApplicationContext(),UpdateApartmentDetailsActivity.class);
                        context.startActivity(I);



                    }
                    //Notification clicked
                    else if (getAdapterPosition() == 1) {

                        Log.e("User", String.valueOf(FirebaseAuth.getInstance().getCurrentUser()));
                        Intent I =new Intent(context.getApplicationContext(),NotificationActivity.class);
                        context.startActivity(I);
////


                    }
                    //Logout clicked
                    else if (getAdapterPosition() == 2) {

                        Log.e("User", String.valueOf(FirebaseAuth.getInstance().getCurrentUser()));
                        signOut(apiclient,context);

                    }


                    //  ((MainActivity) context).userItemClick(getAdapterPosition());
                }
            });


        }

        private void signOut(GoogleApiClient apiclient,final Context context) {


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
                                Intent I =new Intent(context.getApplicationContext(),MainActivity.class);
                                context.startActivity(I);
                            }
                        });
        }


    }

    // Provide a suitable constructor (depends on the kind of dataset)

    public MenuAdapter(Context context, List<String> itemList, Integer[] imgid) {
        this.context = context;
        this.itemList = itemList;
        this.imgid=imgid;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();
        mGoogleApiClient.connect();
    }


    // Create new views (invoked by the layout manager)
    @Override
    public MenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = ( LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_item_row, parent, false));




        ViewHolder vh = new ViewHolder( v,mGoogleApiClient,context);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.mTextView.setText(itemList.get(position));
        holder.mImageView.setImageResource(imgid[position]);


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return itemList.size();
    }
}

