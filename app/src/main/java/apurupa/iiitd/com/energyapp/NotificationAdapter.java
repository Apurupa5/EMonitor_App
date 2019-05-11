package apurupa.iiitd.com.energyapp;

/**
 * Created by NB VENKATESHWARULU on 5/20/2018.
 */
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{
    private Context context;
    private ArrayList<String> mail_list,status_list;
    private static final String TAG = "Notification Adapter";
    private FirebaseDatabase database;
    private static final String mypref = "UserDetails";


    public NotificationAdapter(Context context, List<String> maillist, List<String> statuslist) {
        this.mail_list = (ArrayList<String>) maillist;
        this.status_list = (ArrayList<String>) statuslist;
        this.context=context;
        Log.e(TAG,"Mails"+this.mail_list);

    }

    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_layout, viewGroup, false);
        Log.e(TAG,"In view Holder");

        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Log.e(TAG,"In on bind View Holder "+ position +mail_list.get(position) );
        holder.mEmailTextView.setText("Req. from: " + mail_list.get(position));
        holder.mStatusTextView.setText("Pres status: "+status_list.get(position));
       // holder.mLinearLayout.setBackgroundColor(Color.LTGRAY);

    }

    @Override
    public int getItemCount() {
        Log.e(TAG,"Mail list size"+mail_list.size());

        return mail_list.size();
    }

    public void addItem(String country) {
       // countries.add(country);
       // notifyItemInserted(countries.size());
    }

    public void removeItem(int position) {
       // countries.remove(position);
        notifyItemRemoved(position);
       // notifyItemRangeChanged(position, countries.size());
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private final CardView l1;
        // each data item is just a string in this case
        public TextView mEmailTextView,mStatusTextView;
        public LinearLayout mLinearLayout;

        public ViewHolder(View view,final Context context) {
            super(view);
            mEmailTextView = (TextView) view.findViewById(R.id.request_email_listitem);
            mStatusTextView = (TextView) view.findViewById(R.id.request_status_listitem);
            mLinearLayout= (LinearLayout) view.findViewById(R.id.row_linearlayout);
            l1 = (CardView) itemView.findViewById(R.id.notification_list_card_view);
            l1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Log.e("CLICKED", String.valueOf(getAdapterPosition()));
                   int pos=getAdapterPosition();
                    String mailid=mail_list.get(pos);
                    String status=status_list.get(pos);

                    showDialog(status,mailid,pos);


                    //  ((MainActivity) context).userItemClick(getAdapterPosition());
                }
            });

        }

        private void showDialog(String status, final String mailid, final int pos) {
            AlertDialog.Builder builder;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(context);
            } else {
                builder = new AlertDialog.Builder(context);
            }
            if (status.equals("pending")) {
                builder.setTitle("Installation Request")
                        .setMessage("You have an app installation request from " + mailid)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                             accceptRequest(mailid);
                             status_list.set(pos,"accepted");
                    }
                })
                        .setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();

            }
            else
            {
                builder.setTitle("Request Accepted")
                        .setMessage("You have already accepted the app installation request from " + mailid)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete

                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();


            }
        }

        private void accceptRequest(String mailid) {
                database= FirebaseDatabase.getInstance();
                Log.e(TAG,"In Accept request method");
                String st="status".toString();
                String email1=mailid.replace(".", ",");
                database.getReference().child(st).child(email1).setValue("accepted");
                mStatusTextView.setText("Status: accepted");


                SharedPreferences prefs = context.getSharedPreferences(mypref,
                        Context.MODE_PRIVATE);
                String aptno = prefs.getString("Apartment_no", null);
                saveApartmentData(mailid, aptno);


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
}
