package apurupa.iiitd.com.energyapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MenuFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private MenuAdapter mAdapter;
    private List<String> optionsList= Arrays.asList("Update Apartment Details","Notifications","Logout");
    private Integer[] imgid={
            R.drawable.ic_edit_profile,R.drawable.ic_notifications_black_24dp,R.drawable.ic_logout,

    };
    private ImageView mdisplaypic;
    private TextView mprofilename,museremail;
    private static final String mypref="UserDetails";
    private static final String TAG="Menu Fragment";


    public MenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_menu, container, false);

        mdisplaypic=(ImageView)view.findViewById(R.id.userProfilePic);
        mprofilename=(TextView)view.findViewById(R.id.userProfileName);
        museremail=(TextView)view.findViewById(R.id.userEmail);


        SharedPreferences prefs = getActivity().getSharedPreferences(mypref,
                Context.MODE_PRIVATE);
        Log.e(TAG,"testing"+museremail.getText().toString() + "Email "+ prefs.getString("email_id",null));

        mprofilename.setText(prefs.getString("username",null));
        museremail.setText(prefs.getString("email_id",null));

        String personPhotoUrl=prefs.getString("photourl",null);
        Glide.with(view.getContext()).load(personPhotoUrl)
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mdisplaypic);

        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        // specify an adapter

        mAdapter = new MenuAdapter(getActivity(), optionsList,imgid);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setNestedScrollingEnabled(false);



        Log.e("In menu","Hello");

        return view;




    }

}
