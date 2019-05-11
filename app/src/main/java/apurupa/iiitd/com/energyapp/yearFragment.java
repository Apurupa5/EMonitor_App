package apurupa.iiitd.com.energyapp;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AlertDialogLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import devs.mulham.horizontalcalendar.HorizontalCalendar;


import static java.lang.Math.round;


/**
 * A simple {@link Fragment} subclass.
 */
public class yearFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {


    FirebaseDatabase database=FirebaseDatabase.getInstance();
    FirebaseAuth mauth = FirebaseAuth.getInstance();
    private DatabaseReference ref;
    SharedPreferences pref;
    private static final String mypref="UserDetails";

    private TextView mselectYearTextView;
    private EditText mselectYearEditText;
    private int selectedYear;
    private String TAG="YearFragment";
    private TextView mYearAvgValueTextView;
    private String aptno;
    private GraphView graph;
    private DatePicker picker;
    private long[] resampled_powerValues;
    private int[] count;
    private long[] resampled_energyValues;
    private ProgressBar mProgressBar;
    private HorizontalCalendar mhorizontalCalendar;

    ArrayList<Long> powerValues = new ArrayList<>();
    ArrayList<Integer> timestampValues = new ArrayList<>();
    ArrayList<Long> energyValues = new ArrayList<>();
    private TextView mYearUnitsConsumed;
    private EditText mCompareSelectEditText;
    private ArrayList<String> optionsSelected;

    int[] Colors=new int[]
            {Color.YELLOW,Color.GREEN,Color.RED,Color.MAGENTA,
                    Color.CYAN,Color.parseColor("#dd2c00"),Color.parseColor("#004d40"),
                    Color.parseColor("#4e342e"),Color.parseColor("#e64a19")};



    String[] AlertDialogItems = new String[]{

            "Building avg",
            "Building min",
            "Building max",
            // "201","202","203",
            //"301","302","303",
            //"401","402","403",
            //"501",
            "502","503",
            //"601","602","603",
            //"701",
            "702",
            //"703"
            "801","802","803",
            //"901","902","903",
            //"1001","1003"

    };

    List<String> ItemsIntoList;

    boolean[] Selectedtruefalse = new boolean[]{

//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
//            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false

    };

    private TextView mNoDataYearTextView;
    private HashMap<String ,LineGraphSeries<DataPoint>> seriesMap= new HashMap<String ,LineGraphSeries<DataPoint>>();
    AlertDialog.Builder alertdialogbuilder;
    private String presentApt;
    boolean originalselected[]=Selectedtruefalse;
    private boolean datareading=false;
    private MonitorObject myMonitorObject;
    private TextView mYearAvgTextView;
    private ImageButton mcalendarbutton;
    private ArrayList<String> previousSelectedOptions;

    public yearFragment() {
        // Required empty public constructor
    }

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(DataSnapshot dataSnapshot);
        void onStart();
        void onFailure();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

      View view= inflater.inflate(R.layout.fragment_year, container, false);
        mselectYearTextView=(TextView)view.findViewById(R.id.selectYearTextView);
        mselectYearEditText=(EditText)view.findViewById(R.id.selectYearEditText);
       // mselectYearEditText.setOnClickListener(this);

        mYearAvgValueTextView= (TextView) view.findViewById(R.id.year_avg_powerValueTextView);
        mYearAvgValueTextView.setVisibility(View.VISIBLE);
        mYearAvgTextView=(TextView)view.findViewById(R.id.year_avg_powerTextView);


        mYearUnitsConsumed= (TextView) view.findViewById(R.id.year_units_ValueTextView);
        mYearUnitsConsumed.setVisibility(View.VISIBLE);

        mNoDataYearTextView=(TextView)view.findViewById(R.id.yearNoDataTextView);


        mCompareSelectEditText=(EditText)view.findViewById(R.id.yearCompareEditText);
        mCompareSelectEditText.setEnabled(false);

        mCompareSelectEditText.setOnClickListener(this);

        mcalendarbutton=(ImageButton)view.findViewById(R.id.yearcalendarbutton);
        mcalendarbutton.setOnClickListener(this);

        graph = (GraphView)view.findViewById(R.id.year_graph);
        mProgressBar=(ProgressBar)view.findViewById(R.id.yearprogressBar_cyclic);
        hideProgressBar();

        configureGraph();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.yearcalendarbutton:
                Log.e(TAG,"In case " + mselectYearEditText.getText());
                final Calendar c = Calendar.getInstance();

                int y = c.get(Calendar.YEAR) + 4;
                int m = c.get(Calendar.MONTH) - 4;
                int d = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dpd = new DatePickerDialog(getActivity(), AlertDialog.THEME_HOLO_DARK,this, y, m, d) {
                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        int day = getContext().getResources().getIdentifier("android:id/day", null, null);
                        int month = getContext().getResources().getIdentifier("android:id/month", null, null);
                        if (day != 0) {
                            View dayPicker = findViewById(day);
                            if (dayPicker != null) {
                                //Set Day view visibility Off/Gone
                                dayPicker.setVisibility(View.GONE);
                            }
                        }
                        if (month != 0) {
                            View monthPicker = findViewById(month);
                            if (monthPicker != null) {
                                //Set Day view visibility Off/Gone
                                monthPicker.setVisibility(View.GONE);

                            }
                        }
                    }


                };
                DatePicker dp = dpd.getDatePicker();
                c.add(Calendar.YEAR, -4);

                dp.setMinDate(c.getTimeInMillis());//get the current day

                Calendar nc = Calendar.getInstance();


                dp.setMaxDate(nc.getTimeInMillis());
                dpd.show();
                break;
            case R.id.yearCompareEditText:
                showMultiSelectDialogBox();

        }
    }

    private void clearGraphs() {
        if(seriesMap.size()==0) {return;}
        Log.e(TAG, "On Item Clear Graphs " + seriesMap.keySet().toString());
        Iterator<Map.Entry<String, LineGraphSeries<DataPoint>>> iter = seriesMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, LineGraphSeries<DataPoint>> entry = iter.next();

            String name=entry.getKey();
            Log.e("Clear Graph", name);
            if(!name.equals(presentApt))
            {
                //int index=Arrays.asList(AlertDialogItems).indexOf(name);
                //  boolean val=Selectedtruefalse[index];
                //if(!val) {

                //if(optionsSelected==null)
                graph.removeSeries(entry.getValue());
                iter.remove();

                // }
            }
        }

        Log.e(TAG, "On Item Clear Graphs aftr " + seriesMap.keySet().toString());
    }
    private void showMultiSelectDialogBox() {
        Log.e(TAG,"In alert dialog builder");
        alertdialogbuilder = new AlertDialog.Builder(this.getActivity());

        ItemsIntoList = Arrays.asList(AlertDialogItems);

        final int[] count = {0};

        for(int i=0;i<Selectedtruefalse.length;i++)

        {
            if(Selectedtruefalse[i]==true) {count[0]++;}
        }
        alertdialogbuilder.setMultiChoiceItems(AlertDialogItems, Selectedtruefalse, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                count[0] += isChecked ? 1 : -1;
                Log.e(TAG,"On click dialog "+which);
                Selectedtruefalse[which] = isChecked;

                if (count[0] > 3) {
                    Toast.makeText(getActivity(), "You cannot select more than 3.", Toast.LENGTH_SHORT).show();
                    Selectedtruefalse[which] = false;
                    count[0]--;
                    ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                }



            }
        });

        alertdialogbuilder.setCancelable(false);



        alertdialogbuilder.setTitle("Choose Apartments to Compare with(At max 3 items)");

        alertdialogbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                onOkClicked();

            }
        });

        alertdialogbuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = alertdialogbuilder.create();

        dialog.show();

    }

    private void onOkClicked() {
        Log.e(TAG,"On Item ok clicked " + seriesMap.keySet().toString());

        clearGraphs();
        resetAllvalues();
        optionsSelected=new ArrayList<String>();

        mCompareSelectEditText.setText("");
        mCompareSelectEditText.setTextSize(15);
        int a = 0;


        List<String> selectedapts=new ArrayList<String>();


        while(a < Selectedtruefalse.length)
        {
            resetAllvalues();

            boolean value = Selectedtruefalse[a];

            if(value) {
                Log.e(TAG, String.valueOf(value));
                Log.e(TAG, AlertDialogItems[a]);
                String apt = AlertDialogItems[a];
                selectedapts.add(apt);

            }


            a++;
        }
        //   previousSelectedOptions=new ArrayList<String>();
        optionsSelected= (ArrayList<String>) selectedapts;
        //  previousSelectedOptions=(ArrayList<String>)optionsSelected.clone();
        Log.e(TAG,"Final options selected"+optionsSelected.toString());
        if(optionsSelected.size()!=0) addComparisons();
    }

    private void addComparisons() {

        Random rnd=new Random();


        // for(int i=0;i<selectedapts.size();i++)
        //{
        resetAllvalues();


        //Log.e(TAG,"On Item Color " +color);

        Log.e(TAG,optionsSelected.toString());

        String apt=optionsSelected.get(0);
        int ind=0;
        for(int i=0;i<AlertDialogItems.length;i++)
        {
            if(AlertDialogItems[i].equals(apt))
            {
                ind=i;
                break;
            }
        }

        String selectedapt="Apartment"+apt.trim();

        Log.e(TAG,"On Item selected"+selectedapt);
        String dbname="data_"+selectedYear;

       // int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
       int color=Colors[ind];

        LineGraphSeries<DataPoint> hashval=seriesMap.get(apt);
        Log.e(TAG,"On Item hashval "+ hashval);

        if(!selectedapt.equals("None")) {


            Log.e(TAG, "On Item selected" + selectedapt + seriesMap.keySet().toString());
            resetAllvalues();
            showProgressBar();
            if(apt.equals(AlertDialogItems[0]))
            {
                selectedapt="Building avg";
                Log.e(TAG,"In apartment avg");
                apt="avg";
                color=Color.YELLOW;

                ref=database.getReference().child(dbname).child(apt);
                String text= mCompareSelectEditText.getText().toString();
                if(TextUtils.isEmpty(text)) { mCompareSelectEditText.setText(selectedapt);}
                else { mCompareSelectEditText.setText(text+ "," + apt); }

            }
            else if(apt.equals(AlertDialogItems[1]))
            {
                apt="min";
                selectedapt="Building min";
                color=Color.GREEN;
                ref=database.getReference().child(dbname).child(apt);
                String text= mCompareSelectEditText.getText().toString();
                if(TextUtils.isEmpty(text)) { mCompareSelectEditText.setText(selectedapt);}
                else { mCompareSelectEditText.setText(text+ "," + apt); }

            }
            else if(apt.equals(AlertDialogItems[2]))
            {
                apt="max";
                selectedapt="Building max";
                color=Color.RED;
                ref=database.getReference().child(dbname).child(apt);
                String text= mCompareSelectEditText.getText().toString();

                Log.e(TAG,"previous text " + text);
                if(TextUtils.isEmpty(text)) { mCompareSelectEditText.setText(selectedapt);}
                else { mCompareSelectEditText.setText(text+ "," + apt); }


            }
            else {
//                if(selectedapt.equals("Apartment802") || selectedapt.equals("Apartment302"))
//                {
//                    color=Color.MAGENTA;
//                }
                ref = database.getReference().child(dbname).child(selectedapt);
                String text= mCompareSelectEditText.getText().toString();
                if(TextUtils.isEmpty(text)) { mCompareSelectEditText.setText(apt);}
                else { mCompareSelectEditText.setText(text+ "," + apt); }

            }

            Log.e(TAG,"In alert datareading" + datareading);
            readData(ref, selectedapt, color);
            Log.e(TAG,"After read data############");


        }
        else
        {
            clearGraphs();
            String text= mCompareSelectEditText.getText().toString();
            mCompareSelectEditText.setText(text+ " " + apt);

        }
        //  }



    }

    private void resetAllvalues() {

        powerValues = new ArrayList<>();
        timestampValues = new ArrayList<>();
       //seriesMap= new HashMap<String ,LineGraphSeries<DataPoint>>();

        mNoDataYearTextView.setVisibility(View.INVISIBLE);
        //mYearAvgValueTextView.setText("");
        //mYearUnitsConsumed.setText("");
        //mCompareSelectEditText.setText("");

    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.e(TAG, String.valueOf(year));
        selectedYear=year;
        Log.e(TAG,"In case " + mselectYearEditText.getText() + selectedYear);


        plotGraph();

//                if(previousSelectedOptions!=null) {
//            Log.e(TAG, "options in plot " + previousSelectedOptions.size());
//            optionsSelected=previousSelectedOptions;
//            if(optionsSelected.size()!=0) addComparisons();
//        }

    }
    private void plotGraph() {


        mYearAvgValueTextView.setText("");
        mYearUnitsConsumed.setText("");
        mCompareSelectEditText.setText("");
        mCompareSelectEditText.setEnabled(false);
        mYearAvgTextView.setText("Your Avg power consumed");
        seriesMap= new HashMap<String ,LineGraphSeries<DataPoint>>();

        configureGraph();
        resetAllvalues();

        mselectYearEditText.setText(String.valueOf(selectedYear));

        graph.removeAllSeries();
        showProgressBar();


        SharedPreferences prefs = getActivity().getSharedPreferences(mypref,
                Context.MODE_PRIVATE);
        aptno= prefs.getString("Apartment_no",null);
        String apt="Apartment"+aptno;
        String dbname="data_"+selectedYear;
        presentApt=apt;

        ref=database.getReference().child(dbname);
        if(ref!=null)
        {
            ref=ref.child(apt);

        }


      readData(ref, apt, Color.BLUE);



    }

    private void configureGraph() {

        graph.setTitle(" ");
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        // graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        // graph.getViewport().setDrawBorder(true);

       // gridLabel.setLabelVerticalWidth(5);
        gridLabel.setHorizontalAxisTitle("Month in Year");
        gridLabel.setVerticalAxisTitle("Power(W)");

        //graph.setTitle("Power Consumption in " + mselectMonthEditText.getText() );


        gridLabel.setNumHorizontalLabels(7); // only 4 because of the spacegraph.getGridLabelRenderer().setHumanRounding(false);
        gridLabel.setVerticalLabelsAlign(Paint.Align.CENTER);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(13);
        graph.getViewport().setMinX(1);

        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalableY(true);
        graph.getViewport().setScrollableY(true);
    }



    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);

    }
    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);

    }

    private double calculateAvgConsumption(ArrayList<Long> Values) {
        int n =Values.size();
        long sum=0;

        for (int i = 0; i < n; i++)
            sum += Values.get(i);

        Log.e(TAG,"sum"+sum);
        return ((double) sum) / n;
    }


    private int getMonthNumber(String selectedMonth) {
        Date date = null;
        try {
            date = new SimpleDateFormat("MMMM").parse(selectedMonth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }

    public void getTimeSeriesData(DatabaseReference ref, final OnGetDataListener listener) {
        Log.e(TAG,"get time series");
        listener.onStart();
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


    public void readData(DatabaseReference ref, final String aptname, final int color) {
        getTimeSeriesData(ref, new OnGetDataListener() {
            ArrayList<Long> month_powerValues = new ArrayList<>();
            ArrayList<Date> month_timestampValues = new ArrayList<>();
            ArrayList<Long> month_energyValues = new ArrayList<>();
            public Calendar cal;
            public Timestamp timestamp;



            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                //whatever you need to do with the data


                Log.e(TAG,"IN on successs "+ aptname);
                if(!dataSnapshot.exists())
                {

                    hideProgressBar();
                    Log.e(TAG,"FSS " +"After hide progress");
                    if(aptname!=presentApt)
                    {
                        Toast.makeText(getActivity(),"Data is not available for "+aptname,Toast.LENGTH_SHORT).show();
                     //   return;
//                        if(optionsSelected!=null && optionsSelected.size()!=0)
//                        {
//                            Log.e(TAG,"Final options selected in on succes before"+optionsSelected.toString());
//                            String at=optionsSelected.get(0);
//                            // Log.e(TAG,"options before @@@@#########" + previousSelectedOptions.size());
//
//                            optionsSelected.remove(0);
//                            Log.e(TAG,"Final options selected in on success after"+optionsSelected.toString());
//                            if(optionsSelected.size()!=0)addComparisons();
//
//                            //   Log.e(TAG,"options @@@@#########" + previousSelectedOptions.size());
//                        }

                    }
                    else {
                        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
                        mNoDataYearTextView.setVisibility(View.VISIBLE);
                        mNoDataYearTextView.setText("No Data Available");
                        return;
                    }
                }

                LineGraphSeries<DataPoint> existence = seriesMap.get(aptname);
                Log.e(TAG,"Existence"+ aptname + existence);

                if(existence!=null)
                {
                    return;
                }

                Log.e(TAG,"In on success");

                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                Date x=null;
                Long y;
                int size= (int) dataSnapshot.getChildrenCount();

                int i=0;
                for (DataSnapshot children: dataSnapshot.getChildren()) {
                    // TODO: handle the post

                    String month=children.getKey();
                   // Log.e(TAG+"Resample ",month);
                    int month_no=getMonthNumber(month);
                    ArrayList<Map<String, Object>>innerChild= (ArrayList<Map<String, Object>>) children.getValue();
                    month_powerValues = new ArrayList<>();
                    month_timestampValues = new ArrayList<>();


                    for(int j=0;j<innerChild.size();j++)
                    {

                        Map<String, Object> dt = innerChild.get(j);

                        String str= (String) dt.get("power");

                       // Log.e(TAG, "Check"+(String) dt.get("power"));
                        if (!TextUtils.isEmpty(str)) {

                              y = Double.valueOf((String) dt.get("power")).longValue();

                              String str_date = (String) dt.get("timestamp");
                              //Log.e(TAG, "String date" + str_date);
                              java.text.DateFormat formatter;
                             formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {

                            Date date = formatter.parse(str_date);
                            timestamp = new Timestamp(date.getTime());


                            cal = Calendar.getInstance();
                            cal.setTime(date);


                        } catch (ParseException e) {
                            e.printStackTrace();
                        }




                        month_powerValues.add(y);
                        month_timestampValues.add(timestamp);


                    }

                      }

                    //Log.e(TAG,"PS"+month_powerValues.size());
                    resample_data(month_no,month_powerValues,month_timestampValues);


                }
                int sz=powerValues.size();
                for(int k=0;k<sz;k++)
                {
                    series.appendData(new DataPoint(k+1, powerValues.get(k)), true,sz);

                }
                hideProgressBar();

                Log.e(TAG,"!!!!!!!!!size"+sz);



//                graph.getGridLabelRenderer().setNumHorizontalLabels(7); // only 4 because of the spacegraph.getGridLabelRenderer().setHumanRounding(false);
//                graph.getViewport().setXAxisBoundsManual(true);
//                graph.getViewport().setMaxX(sz+1);
//                graph.getViewport().setMinX(1);
                graph.addSeries(series);

                series.setTitle(aptname);
                series.setColor(color);
                series.setDrawDataPoints(true);
                series.setDataPointsRadius(5);
                seriesMap.put(aptname,series);

                Log.e(TAG,"in success" +graph.toString() + seriesMap.keySet());

                graph.getLegendRenderer().setVisible(true);
                graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
                graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);

                //graph.getLegendRenderer().setTextSize(15);

                graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.BOTH);


//                graph.getViewport().setScalable(true);
//                graph.getViewport().setScalableY(true);

                graph.setTitle("Power consumption in " + selectedYear);
                Log.e(TAG,"IS empty"+TextUtils.isEmpty(mYearAvgValueTextView.getText()));

                if(TextUtils.isEmpty(mYearAvgValueTextView.getText())) {

                    mYearAvgTextView.setText("Your avg power(W) in "+ selectedYear );

                    double avg=calculateAvgConsumption(powerValues);
                    avg=round(avg);
                    mYearAvgValueTextView.setText(String.valueOf(avg));


                    double units=(avg*powerValues.size())/1000;
                    mYearUnitsConsumed.setText(String.valueOf(units));

                   // onOkClicked();
                   // mCompareSelectEditText.setEnabled(true);
                    //mMonthCompareSpinner.setEnabled(true);
                    // addItemsOnSpinners();
                }


                Log.e(TAG,"one option done");

                if(optionsSelected!=null && optionsSelected.size()!=0)
                {
                    Log.e(TAG,"Final options selected in on succes before"+optionsSelected.toString());
                    String at=optionsSelected.get(0);
                   // Log.e(TAG,"options before @@@@#########" + previousSelectedOptions.size());

                    optionsSelected.remove(0);
                    Log.e(TAG,"Final options selected in on success after"+optionsSelected.toString());
                    if(optionsSelected.size()!=0)addComparisons();

                 //   Log.e(TAG,"options @@@@#########" + previousSelectedOptions.size());
                }



                if(aptname==presentApt)
                {
                    Log.e(TAG,"Initial loading");
                    onOkClicked();
                    mCompareSelectEditText.setEnabled(true);
                }
            }
            @Override
            public void onStart() {
                //whatever you need to do onStart
                Log.d("ONSTART", "Started");
            }

            @Override
            public void onFailure() {

            }
        });
    }

    private void resample_data(int monthno, ArrayList<Long> month_powerValues, ArrayList<Date> month_timestampValues) {

        int size=month_powerValues.size();
        int k=1;
        long sum_power=0,sum_energy=0;
        int count=0;

        for(int i=0;i<size;i++)
        {

            sum_power=sum_power+month_powerValues.get(i);
           count=count+1;

        }
        //Log.e(TAG+"Resample sum ", + monthno +  " " + sum_power + " " +sum_energy);

        sum_power=(long)(sum_power/count);

      Log.e(TAG+"Resample power", + monthno +  " " + sum_power + " " +sum_energy + month_timestampValues.get(0));
       // Log.e(TAG+"Resample energy", + monthno + " " +sum_energy);
        powerValues.add(sum_power);
        timestampValues.add(monthno);

    }

}
