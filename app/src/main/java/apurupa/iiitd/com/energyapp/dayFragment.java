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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.text.DateFormat;
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

import static java.lang.Math.round;


/**
 * A simple {@link Fragment} subclass.
 */
public class dayFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    FirebaseDatabase database=FirebaseDatabase.getInstance();
    FirebaseAuth mauth = FirebaseAuth.getInstance();
    private DatabaseReference ref;
    SharedPreferences pref;
    private static final String mypref="UserDetails";
    ArrayList<Long> powerValues = new ArrayList<>();
    ArrayList<Date> timestampValues = new ArrayList<>();
    ArrayList<Long> energyValues = new ArrayList<>();

    private TextView mselectDayTextView;
    private EditText mselectDayEditText,mCompareSelectEditText;
    private int selectedYear,selectedMonth,selectedDay;
    private String TAG="DAyfragment";
    private TextView mDayAvgValueTextView,mDayUnitsConsumed;
    private String aptno;
    private GraphView graph;
    private int present_day;
    private ImageButton mCalendarButton;

    private ProgressBar mProgressBar;
    private ArrayList<String> optionsSelected;


    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(DataSnapshot dataSnapshot);
        void onStart();
        void onFailure();
    }

    public dayFragment() {
        // Required empty public constructor
    }



    String[] AlertDialogItems = new String[]{
            "Building avg(All apartments avg)",
            "Building min(All apartments min)",
            "Building max((All apartments max)",
            //"201","202","203",
            //"301","302","303",
            //"401","402","403",
            //"501",
            "502","503",
            //"601","602","603",
            //"701",
            "702",
            //,"703",
            "801","802","803"
            //"901","902","903",
            //"1001","1003"

    };

    List<String> ItemsIntoList;

    int[] Colors=new int[]
            {Color.YELLOW,Color.GREEN,Color.RED,Color.MAGENTA,
                    Color.CYAN,Color.parseColor("#dd2c00"),Color.parseColor("#004d40"),
                    Color.parseColor("#4e342e"),Color.parseColor("#e64a19")};


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

    private TextView mNoDataDayTextView;
    private HashMap<String ,LineGraphSeries<DataPoint>> seriesMap= new HashMap<String ,LineGraphSeries<DataPoint>>();
    AlertDialog.Builder alertdialogbuilder;
    private String presentApt;
    boolean originalselected[]=Selectedtruefalse;
    private boolean datareading=false;

    private TextView mDayAvgTextView;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.e(TAG,"In oncreate view");
        View view= inflater.inflate(R.layout.fragment_day, container, false);



        mselectDayTextView=(TextView)view.findViewById(R.id.selectDayTextView);     

        mselectDayEditText=(EditText)view.findViewById(R.id.selectDayEditText);
        //mselectDayEditText.setOnClickListener(this);

        mCalendarButton=(ImageButton)view.findViewById(R.id.calendarbutton);
        mCalendarButton.setOnClickListener(this);


        mDayAvgTextView=(TextView)view.findViewById(R.id.day_avg_powerTextView);
        mDayAvgValueTextView= (TextView) view.findViewById(R.id.day_avg_powerValueTextView);
        mDayAvgValueTextView.setVisibility(View.VISIBLE);

        mDayUnitsConsumed= (TextView) view.findViewById(R.id.day_units_ValueTextView);
        mDayUnitsConsumed.setVisibility(View.VISIBLE);


        mNoDataDayTextView=(TextView)view.findViewById(R.id.dayNoDataTextView);
        mCompareSelectEditText=(EditText)view.findViewById(R.id.dayCompareEditText);
        mCompareSelectEditText.setEnabled(false);
        mCompareSelectEditText.setOnClickListener(this);

        mProgressBar=(ProgressBar)view.findViewById(R.id.dayprogressBar_cyclic);
        hideProgressBar();



        graph = (GraphView)view.findViewById(R.id.day_graph);
        configureGraph();







        return view;
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.calendarbutton:
                final Calendar c = Calendar.getInstance();
                final int[] dates = new int[3];

                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

    //        DatePickerBuilder builder = new DatePickerBuilder(this, listener)
    //                .pickerType(CalendarView.ONE_DAY_PICKER);
    //
    //        DatePicker datePicker = builder.build();
    //        datePicker.show();

                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), AlertDialog.THEME_TRADITIONAL, this, mYear, mMonth, mDay);


                DatePicker dp = datePickerDialog.getDatePicker();
                c.add(Calendar.YEAR, -4);
                c.add(Calendar.MONTH, -3);
                dp.setMinDate(c.getTimeInMillis());

                Calendar nc = Calendar.getInstance();


                nc=Calendar.getInstance();
                nc.add(Calendar.DAY_OF_MONTH,-1);

                dp.setMaxDate(nc.getTimeInMillis());
                // datePickerDialog.getDatePicker().setMinDate();
                datePickerDialog.show();
                break;
            case R.id.dayCompareEditText:
                showMultiSelectDialogBox();
                break;
        }
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

                graph.removeSeries(entry.getValue());
                iter.remove();

                // }
            }
        }

        Log.e(TAG, "On Item Clear Graphs aftr " + seriesMap.keySet().toString());
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mselectDayEditText.setText(dayOfMonth + "-" + (month + 1) + "-" + year);
        selectedDay=dayOfMonth;
        selectedMonth=month;
        selectedYear=year;

        plotGraph();

    }

    private void addComparisons() {

        Random rnd=new Random();


        // for(int i=0;i<selectedapts.size();i++)
        //{
        resetAllvalues();

       // int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
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

        int color=Colors[ind];
        String selectedapt="Apartment"+apt.trim();

        Log.e(TAG,"On Item selected"+selectedapt);
        String dbname="data_"+selectedYear;


        LineGraphSeries<DataPoint> hashval=seriesMap.get(apt);
        Log.e(TAG,"On Item hashval "+ hashval);

        String monthname=getMonthName(selectedMonth);
        if(!selectedapt.equals("None")) {


            Log.e(TAG, "On Item selected" + selectedapt + seriesMap.keySet().toString());
            resetAllvalues();
            showProgressBar();
            if(apt.equals(AlertDialogItems[0]))
            {
                selectedapt="Building avg";
                Log.e(TAG,"In apartment avg");
                apt="avg";

                ref=database.getReference().child(dbname).child(apt).child(monthname);
                String text= mCompareSelectEditText.getText().toString();
                if(TextUtils.isEmpty(text)) { mCompareSelectEditText.setText(selectedapt);}
                else { mCompareSelectEditText.setText(text+ "," + apt); }

            }
            else if(apt.equals(AlertDialogItems[1]))
            {
                apt="min";
                selectedapt="Building min";
                ref=database.getReference().child(dbname).child(apt).child(monthname);
                String text= mCompareSelectEditText.getText().toString();
                if(TextUtils.isEmpty(text)) { mCompareSelectEditText.setText(selectedapt);}
                else { mCompareSelectEditText.setText(text+ "," + apt); }

            }
            else if(apt.equals(AlertDialogItems[2]))
            {
                apt="max";
                selectedapt="Building max";
                ref=database.getReference().child(dbname).child(apt).child(monthname);
                String text= mCompareSelectEditText.getText().toString();

                Log.e(TAG,"previous text " + text);
                if(TextUtils.isEmpty(text)) { mCompareSelectEditText.setText(selectedapt);}
                else { mCompareSelectEditText.setText(text+ "," + apt); }


            }
            else {
                ref=database.getReference().child(dbname).child(selectedapt).child(monthname);
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
    private void plotGraph() {

        mDayAvgValueTextView.setText("");
        mDayUnitsConsumed.setText("");
        mCompareSelectEditText.setText("");
        mCompareSelectEditText.setEnabled(false);
        mDayAvgTextView.setText("Your Avg power consumed");
        seriesMap= new HashMap<String ,LineGraphSeries<DataPoint>>();


        configureGraph();
        resetAllvalues();

        Log.e(TAG,"In plot Graph");
        showProgressBar();
        graph.removeAllSeries();
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
            String month_name=getMonthName(selectedMonth);
            ref=ref.child(month_name);
        }

        readData(ref,apt,Color.BLUE);





    }


    private void resetAllvalues() {

        powerValues = new ArrayList<>();
        timestampValues = new ArrayList<>();
        //seriesMap= new HashMap<String ,LineGraphSeries<DataPoint>>();

        mNoDataDayTextView.setVisibility(View.INVISIBLE);
//        mDayAvgValueTextView.setText("");
//        mDayUnitsConsumed.setText("");
//        mCompareSelectEditText.setText("");

    }
    private void configureGraph() {


        graph.setTitle(" ");
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        // graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        // graph.getViewport().setDrawBorder(true);

        gridLabel.setLabelVerticalWidth(5);
        gridLabel.setHorizontalAxisTitle("Hour in the Day");
        gridLabel.setVerticalAxisTitle("Power(W)");

        //graph.setTitle("Power Consumption in " + mselectMonthEditText.getText() );


        gridLabel.setNumHorizontalLabels(7); // only 4 because of the spacegraph.getGridLabelRenderer().setHumanRounding(false);
        gridLabel.setVerticalLabelsAlign(Paint.Align.CENTER);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(23);
        graph.getViewport().setMinX(0);


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
        int sum=0;

        for (int i = 0; i < n; i++)
            sum += Values.get(i);

        Log.e(TAG,"sum"+sum);
        return ((double) sum) / n;
    }


    private String getMonthName(int selectedMonth) {
        Calendar cal=Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");

        cal.set(Calendar.MONTH,selectedMonth);
        String month_name = month_date.format(cal.getTime());

        Log.e("",""+month_name);
        return month_name;
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
            public Calendar cal;
            public Timestamp timestamp;

            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                //whatever you need to do with the data

                Log.e(TAG,"IN on successs");
                if(!dataSnapshot.exists())
                {

                    hideProgressBar();
                    Log.e(TAG,"FSS " +"After hide progress");
                    if(aptname!=presentApt)
                    {
                        Toast.makeText(getActivity(),"Data is not available for "+aptname,Toast.LENGTH_SHORT).show();

                        if(optionsSelected!=null && optionsSelected.size()!=0)
                        {
                            Log.e(TAG,"Final options selected in on succes before"+optionsSelected.toString());
                            String at=optionsSelected.get(0);

                            optionsSelected.remove(0);
                            Log.e(TAG,"Final options selected in on success after"+optionsSelected.toString());
                            if(optionsSelected.size()!=0)addComparisons();

                            return;

                        }

                    }
                    else {
                        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
                        mNoDataDayTextView.setVisibility(View.VISIBLE);
                        mNoDataDayTextView.setText("No Data Available");
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

                Log.e(TAG,"In on success");
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                Date x=null;
                Long y;
                int size= (int) dataSnapshot.getChildrenCount();

                int i=0;
                for (DataSnapshot children: dataSnapshot.getChildren()) {
                    // TODO: handle the post

                    Map<String, Object> dt = (Map<String, Object>) children.getValue();

                    String str= (String) dt.get("power");

                    Log.e(TAG, "Check"+(String) dt.get("power"));
                    if (!TextUtils.isEmpty(str)) {

                        y = Double.valueOf((String) dt.get("power")).longValue();

                        String str_date = (String) dt.get("timestamp");
                        Log.e(TAG,"String date"+str_date);
                        DateFormat formatter;
                        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {

                            Date date = formatter.parse(str_date);
                           timestamp = new Timestamp(date.getTime());

                            Log.e(TAG,"Timestamp"+timestamp);
                            cal = Calendar.getInstance();
                            cal.setTime(date);
                            present_day=cal.get(Calendar.DAY_OF_MONTH);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if(present_day==selectedDay) {
                            Log.e(TAG,"In if ");

                            powerValues.add(y);
                            timestampValues.add(timestamp);
                            int hour=cal.get(Calendar.HOUR_OF_DAY);


                            series.appendData(new DataPoint(hour, y), true, size);


                        }

                    }
                }
                Log.e(TAG,"data done power size" + powerValues.size());
                hideProgressBar();
                if(powerValues.size()==0)
                {
                    Log.e(TAG,"data done power size in if" + powerValues.size());

                    if(aptname==presentApt) {
                    mNoDataDayTextView.setVisibility(View.VISIBLE);
                    mNoDataDayTextView.setText("No Data Available");

                       // return;
                    }
                    else
                    {
                        Toast.makeText(getActivity(),"Data is not available for "+aptname,Toast.LENGTH_SHORT).show();

                    }
                }

                series.setTitle(aptname);
                series.setColor(color);
                series.setDrawDataPoints(true);
                series.setDataPointsRadius(5);
                seriesMap.put(aptname,series);

                Log.e(TAG,"Before adding "+ series);
                graph.addSeries(series);


                graph.getLegendRenderer().setVisible(true);
                graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
                graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
                //graph.getLegendRenderer().setTextSize(15);

                graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.BOTH);


                graph.setTitle("Power Consumption on " + mselectDayEditText.getText() );



//                graph.getViewport().setScalable(true);
//                graph.getViewport().setScrollable(true);
//                graph.getViewport().setScalableY(true);
//                graph.getViewport().setScrollableY(true);

                if(TextUtils.isEmpty(mDayAvgValueTextView.getText())) {
                    mDayAvgTextView.setText("Your avg power(W) on "+mselectDayEditText.getText() );

                    double avg = calculateAvgConsumption(powerValues);
                    avg = round(avg);
                    mDayAvgValueTextView.setText(String.valueOf(avg));


                    double units = (avg * powerValues.size()) / 1000;
                    mDayUnitsConsumed.setText(String.valueOf(units));
                    //mCompareSelectEditText.setEnabled(true);

                }



                Log.e(TAG,"one option done");

                if(optionsSelected!=null && optionsSelected.size()!=0)
                {
                    Log.e(TAG,"Final options selected in on succes before"+optionsSelected.toString());
                    String at=optionsSelected.get(0);

                    optionsSelected.remove(0);
                    Log.e(TAG,"Final options selected in on success after"+optionsSelected.toString());
                    if(optionsSelected.size()!=0)addComparisons();

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
}
