package apurupa.iiitd.com.energyapp;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
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

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.model.CalendarItemStyle;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarPredicate;


import static java.lang.Math.round;


/**
 * A simple {@link Fragment} subclass.
 */
public class monthFragment extends Fragment implements View.OnClickListener {


    FirebaseDatabase database=FirebaseDatabase.getInstance();
    FirebaseAuth mauth = FirebaseAuth.getInstance();
    private DatabaseReference ref;
    SharedPreferences pref;
    private static final String mypref="UserDetails";
    ArrayList<Long> powerValues = new ArrayList<>();
    ArrayList<Date> timestampValues = new ArrayList<>();
    ArrayList<Long> energyValues = new ArrayList<>();

    private TextView mNoDataMonthTextView;
    private EditText mselectMonthEditText,mCompareSelectEditText;
    private int selectedYear,selectedMonth;
    private String TAG="MonthFragment";
    private TextView mMonthAvgValueTextView,mMonthUnitsConsumed, mMonthCompareTextView;
    private String aptno;
    private GraphView graph;
    private DatePicker picker;
    private long[] resampled_powerValues;
    private int[] count;
    private long[] resampled_energyValues;
    private Spinner mMonthCompareSpinner;
    private String presentApt;
    private HashMap<String ,LineGraphSeries<DataPoint>> seriesMap= new HashMap<String ,LineGraphSeries<DataPoint>>();
    private HorizontalCalendar mhorizontalCalendar;
    private ProgressBar mProgressBar;
    private ArrayList<String> optionsSelected;

    AlertDialog.Builder alertdialogbuilder;

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

    boolean originalselected[]=Selectedtruefalse;
    private boolean datareading=false;
    private MonitorObject myMonitorObject;
    private TextView mMonthAvgTextView;

    public monthFragment() {
        // Required empty public constructor
    }


//    @Override
//    public void onBackPressed() {
//        Log.e(TAG, "onBackPressed Called");
//        configureGraph();
//
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_month, container, false);

        mNoDataMonthTextView=(TextView)view.findViewById(R.id.monthNoDataTextView);

        mMonthAvgTextView=(TextView)view.findViewById(R.id.month_avg_powerTextView);


        mMonthAvgValueTextView= (TextView) view.findViewById(R.id.month_avg_powerValueTextView);
        mMonthAvgValueTextView.setVisibility(View.VISIBLE);

        mMonthUnitsConsumed= (TextView) view.findViewById(R.id.month_units_ValueTextView);
        mMonthUnitsConsumed.setVisibility(View.VISIBLE);


        mCompareSelectEditText=(EditText)view.findViewById(R.id.monthCompareEditText);
        mCompareSelectEditText.setEnabled(false);
        mCompareSelectEditText.setOnClickListener(this);

        mProgressBar=(ProgressBar)view.findViewById(R.id.monthprogressBar_cyclic);
        hideProgressBar();

        graph = (GraphView)view.findViewById(R.id.month_graph);
        configureGraph();


        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.YEAR,-4);
        startDate.add(Calendar.MONTH,-3);
/* ends after 1 month from now */
        Calendar endDate = Calendar.getInstance();
        // endDate.add(Calendar.MONTH);

        Log.e(TAG, "Start Date " +startDate);
        Log.e(TAG,"End date " + endDate);
        mhorizontalCalendar = new HorizontalCalendar.Builder(view, R.id.monthHorizontalCalendar)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)

                .mode(HorizontalCalendar.Mode.MONTHS)
                .configure()

                .formatMiddleText("MMM")
                .formatBottomText("yyyy")
                .showTopText(false)
                .showBottomText(true)

                .textColor(Color.LTGRAY,Color.WHITE)
                .colorTextBottom(Color.parseColor("#009688"),Color.WHITE)
                .colorTextMiddle(Color.parseColor("#009688"),Color.WHITE)

                .selectedDateBackground(getResources().getDrawable(R.color.colorPrimaryDark))

                .sizeMiddleText(14)
                .sizeBottomText(11)
                .end()
                .defaultSelectedDate(endDate)
                .build();
        mhorizontalCalendar.setRange(startDate,endDate);
        mhorizontalCalendar.refresh();
        Log.e(TAG,"On create Selected Date " + mhorizontalCalendar.getSelectedDate());

        Log.e(TAG,"On create Selected Date 2 " + mhorizontalCalendar.getSelectedDatePosition());

        selectedMonth=(endDate.get(Calendar.MONTH));
        String mname=getMonthName(selectedMonth);
        selectedYear=endDate.get(Calendar.YEAR);
        plotGraph();



        mhorizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                //do something
                selectedMonth=(date.get(Calendar.MONTH));
                String mname=getMonthName(selectedMonth);
                selectedYear=date.get(Calendar.YEAR);

                Log.e(TAG,"In side calendar listener");
                Log.e(TAG,"check " + date);
                Log.e(TAG,"Horizontal "+ mname + selectedYear + " " + selectedMonth +" " + date.get(Calendar.DATE));
                plotGraph();




            }
        });






        return view;
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
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.monthCompareEditText:
                showMultiSelectDialogBox();
                break;
        }

    }

    private void showMultiSelectDialogBox() {

       // Selectedtruefalse=originalselected;
        Log.e(TAG,"Selected"+Selectedtruefalse[0]);
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

        alertdialogbuilder.setTitle("Choose any 3");


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

    private void addComparisons() {

        Random rnd=new Random();


       // for(int i=0;i<selectedapts.size();i++)
        //{
        resetAllvalues();

        //int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
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
            String month_name = getMonthName(selectedMonth);

            Log.e(TAG,"On Item Selected " + month_name);

            LineGraphSeries<DataPoint> hashval=seriesMap.get(apt);
            Log.e(TAG,"On Item hashval "+ hashval);

            if(!selectedapt.equals("None")) {


                Log.e(TAG, "On Item selected" + selectedapt + seriesMap.keySet().toString());
                resetAllvalues();
                showProgressBar();
                if(apt.equals("Building avg"))
                {
                    selectedapt="Building avg";
                    Log.e(TAG,"In apartment avg");
                    apt="avg";

                    ref=database.getReference().child(dbname).child(apt).child(month_name);
                    String text= mCompareSelectEditText.getText().toString();
                    if(TextUtils.isEmpty(text)) { mCompareSelectEditText.setText(selectedapt);}
                    else { mCompareSelectEditText.setText(text+ "," + apt); }

                }
                else if(apt.equals("Building min"))
                {
                    apt="min";
                    selectedapt="Building min";
                    ref=database.getReference().child(dbname).child(apt).child(month_name);
                    String text= mCompareSelectEditText.getText().toString();
                    if(TextUtils.isEmpty(text)) { mCompareSelectEditText.setText(selectedapt);}
                    else { mCompareSelectEditText.setText(text+ "," + apt); }

                }
                else if(apt.equals("Building max"))
                {
                    apt="max";
                    selectedapt="Building max";
                    ref=database.getReference().child(dbname).child(apt).child(month_name);
                    String text= mCompareSelectEditText.getText().toString();

                    Log.e(TAG,"previous text " + text);
                    if(TextUtils.isEmpty(text)) { mCompareSelectEditText.setText(selectedapt);}
                    else { mCompareSelectEditText.setText(text+ "," + apt); }


                }
                else {
                    ref = database.getReference().child(dbname).child(selectedapt).child(month_name);
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

    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(DataSnapshot dataSnapshot);
        void onStart();
        void onFailure();
    }



    private void plotGraph() {


        Log.e(TAG,"In plot graph");
        mMonthAvgValueTextView.setText("");
        mMonthUnitsConsumed.setText("");
        mCompareSelectEditText.setText("");
        mCompareSelectEditText.setEnabled(false);
        mMonthAvgTextView.setText("Your Avg power consumed");
        seriesMap= new HashMap<String ,LineGraphSeries<DataPoint>>();

        configureGraph();
        resetAllvalues();
        graph.removeAllSeries();

        showProgressBar();




        SharedPreferences prefs = getActivity().getSharedPreferences(mypref,
                Context.MODE_PRIVATE);
        aptno= prefs.getString("Apartment_no",null);
        String apt="Apartment"+aptno;
        String dbname="data_"+selectedYear;
        presentApt=apt;



        ref=database.getReference().child(dbname);

        Log.e(TAG,"In plot graph "+ dbname + " " +ref);

            if (ref != null) {
                ref = ref.child(apt);
                String month_name = getMonthName(selectedMonth);
                ref = ref.child(month_name);
                //readData(ref);
                readData(ref,apt, Color.BLUE);
                   }


    }

    private void resetAllvalues() {

        powerValues = new ArrayList<>();
        timestampValues = new ArrayList<>();
       // seriesMap= new HashMap<String ,LineGraphSeries<DataPoint>>();

        mNoDataMonthTextView.setVisibility(View.INVISIBLE);
        //mMonthAvgValueTextView.setText("");
       // mMonthUnitsConsumed.setText("");
       // mCompareSelectEditText.setText("");

    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);

    }
    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);

    }


    private double calculateAvgConsumption(long[] Values) {
        int n =Values.length;
        long sum=0;

        for (int i = 0; i < n; i++)
            sum += Values[i];

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
                datareading=true;
                listener.onSuccess(dataSnapshot);
                Log.e(TAG,"On data change after on sucess");
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                Log.e(TAG, String.valueOf(elapsedTime));
                datareading=false;

//                if(myMonitorObject!=null) {
//                    synchronized (myMonitorObject) {
//                        myMonitorObject.notify();
//                    }
//                }
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
                        //return;
                    }
                    else {
                        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
                        mNoDataMonthTextView.setVisibility(View.VISIBLE);
                        mNoDataMonthTextView.setText("No Data Available");
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

                    Map<String, Object> dt = (Map<String, Object>) children.getValue();
                    String str= (String) dt.get("power");
                   // Log.e(TAG, "Check"+(String) dt.get("power"));

                    if (!TextUtils.isEmpty(str)) {

                        y = Double.valueOf((String) dt.get("power")).longValue();

                        String str_date = (String) dt.get("timestamp");
                        //Log.e(TAG,"String date"+str_date);
                        java.text.DateFormat formatter;
                        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {

                            Date date = formatter.parse(str_date);
                            timestamp = new Timestamp(date.getTime());

                          //  Log.e(TAG,"Timestamp"+timestamp);
                            cal = Calendar.getInstance();
                            cal.setTime(date);


                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        powerValues.add(y);
                        timestampValues.add(timestamp);

                    }
                }

                resample_data(series,aptname);

                series.setTitle(aptname);
                series.setColor(color);
                series.setDrawDataPoints(true);
                series.setDataPointsRadius(5);


                graph.setTitle("Power Consumption in " + getMonthName(selectedMonth) +" " + selectedYear );


                graph.getLegendRenderer().setVisible(true);
                graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
                graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
                //graph.getLegendRenderer().setTextSize(15);

                graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.BOTH);

                if(TextUtils.isEmpty(mMonthAvgValueTextView.getText())) {

                    mMonthAvgTextView.setText("Your avg power(W) in "+getMonthName(selectedMonth) +" " + selectedYear );
                    double avg = calculateAvgConsumption(resampled_powerValues);
                    avg = round(avg);
                    mMonthAvgValueTextView.setText(String.valueOf(avg));


                    double units = (avg * powerValues.size()) / 1000;
                    mMonthUnitsConsumed.setText(String.valueOf(units));
                    //mCompareSelectEditText.setEnabled(true);
                    //mMonthCompareSpinner.setEnabled(true);
                   // addItemsOnSpinners();
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
                Log.e(TAG, " On Started");
                datareading=true;
                Log.e(TAG,"Data res"+datareading);

            }

            @Override
            public void onFailure() {

                Log.e(TAG,"On Failure");
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

    private void configureGraph() {


        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        // graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        // graph.getViewport().setDrawBorder(true);

        gridLabel.setLabelVerticalWidth(5);
        gridLabel.setHorizontalAxisTitle("Day of Month");
        gridLabel.setVerticalAxisTitle("Power(W)");

        //graph.setTitle("Power Consumption in " + mselectMonthEditText.getText() );


        gridLabel.setNumHorizontalLabels(7); // only 4 because of the spacegraph.getGridLabelRenderer().setHumanRounding(false);
        gridLabel.setVerticalLabelsAlign(Paint.Align.CENTER);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(32);
        graph.getViewport().setMinX(0);

        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalableY(true);
        graph.getViewport().setScrollableY(true);
        graph.setTitle(" ");
    }



    private void resample_data(LineGraphSeries<DataPoint> series,String aptname) {

        ArrayList<Date> resampled_timestampValues = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH,selectedMonth);
        int numDays = calendar.getActualMaximum(Calendar.DATE);

        resampled_powerValues = new long[numDays];
        Arrays.fill(resampled_powerValues, 0);

       count= new int[numDays];
        Arrays.fill(resampled_powerValues, 0);

        resampled_energyValues = new long[numDays];
        Arrays.fill(resampled_energyValues, 0);

        int size=powerValues.size();
        int k=1;
        float pmax=0;
        for(int i=0;i<size;i++)
        {

            Calendar cal = Calendar.getInstance();
            cal.setTime(timestampValues.get(i));
            int present_day=cal.get(Calendar.DAY_OF_MONTH);
            resampled_powerValues[present_day-1]=  resampled_powerValues[present_day-1]+powerValues.get(i);
            //resampled_energyValues[present_day-1]=  resampled_energyValues[present_day-1]+energyValues.get(i);
            count[present_day-1]=count[present_day-1]+1;
            if(powerValues.get(i)>pmax)
            {
                pmax=powerValues.get(i);
            }

        }

        Log.e(TAG,"Before resampling max " + aptname + " " + pmax);
        float max=0;
       for(int i=0;i<resampled_powerValues.length;i++)
       {
           if(count[i]!=0) {
               resampled_powerValues[i] = (long) (resampled_powerValues[i] / count[i]);
               if(resampled_powerValues[i]>max)
               {
                   max=resampled_powerValues[i];
               }
              // resampled_energyValues[i] = (long) (resampled_energyValues[i] / count[i]);
           }
           series.appendData(new DataPoint(i+1, resampled_powerValues[i]), true, resampled_powerValues.length);

       }



        Log.e(TAG,"data done for apt " +aptname + "  "+ max );

        hideProgressBar();
        graph.addSeries(series);
        seriesMap.put(aptname,series);
    }



}



    
