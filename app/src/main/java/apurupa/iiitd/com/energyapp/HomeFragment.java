package apurupa.iiitd.com.energyapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.sql.Time;
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
public class HomeFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    FirebaseDatabase database=FirebaseDatabase.getInstance();
    FirebaseAuth mauth = FirebaseAuth.getInstance();
    private DatabaseReference ref;
    SharedPreferences pref;
    private static final String TAG="HOME FRAGMENT";
    private static final String mypref="UserDetails";

    private View view;

    private String aptno,presentApt;
    ArrayList<Long> powerValues = new ArrayList<>();
    ArrayList<Date> timestampValues = new ArrayList<>();
    private GraphView graph;
    private TextView mAvgValueTextView,mCompareTextView;
    private TextView mUnitsConsumed;
    ArrayList<Long> energyValues = new ArrayList<>();
    private LineChart mChart;
    private TextView xAxisName;
    private Spinner mCompareSpinner;
    private HashMap<String ,LineGraphSeries<DataPoint>>  seriesMap= new HashMap<String ,LineGraphSeries<DataPoint>>();
    private View mScrollView;
    private LinearLayout achartcontainer;
    private TextView mNodataTextView;
    private ProgressBar mProgressBar;
    private EditText mCompareEditText;
    int[] Colors=new int[]
            {Color.YELLOW,Color.GREEN,Color.RED,Color.MAGENTA,
                    Color.CYAN,Color.parseColor("#dd2c00"),Color.parseColor("#004d40"),
                    Color.parseColor("#4e342e"),Color.parseColor("#e64a19")};


    String[] AlertDialogItems = new String[]{
            "Building avg",
            "Building min",
            "Building max",

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

    boolean[] Selectedtruefalse = new boolean[]{
           false,
            false,
            false,
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
            false

    };
    AlertDialog.Builder alertdialogbuilder;
    private ArrayList<String> optionsSelected;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedapt=mCompareSpinner.getSelectedItem().toString().trim();
        Log.e(TAG,"On Item selected"+selectedapt);
        ref=database.getReference().child("dynamic_data").child(selectedapt);
        if(!selectedapt.equals("None")) {
            Log.e(TAG, "Item selected" + selectedapt);
            readData(ref, selectedapt, Color.RED);
        }
        else
        {
            clearGraphs();
        }
    }

    private void clearGraphs() {
        if(seriesMap.size()==0) {return;}
        Log.e(TAG, "On Item Clear Graphs " + seriesMap.keySet().toString());
        Iterator<Map.Entry<String, LineGraphSeries<DataPoint>>> iter = seriesMap.entrySet().iterator();
        while (iter.hasNext()) {
            Log.e(TAG,"claear graph loop "+presentApt);
            Map.Entry<String, LineGraphSeries<DataPoint>> entry = iter.next();
            Log.e(TAG,"clear graph"+entry.getKey());
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

    private void resetAllvalues() {

        powerValues = new ArrayList<>();
        timestampValues = new ArrayList<>();
        //seriesMap= new HashMap<String ,LineGraphSeries<DataPoint>>();

        mNodataTextView.setVisibility(View.INVISIBLE);
        //mYearAvgValueTextView.setText("");
        //mYearUnitsConsumed.setText("");
        //mCompareSelectEditText.setText("");

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);

    }
    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.dynamicCompareEditText:
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

    private void onOkClicked() {
        Log.e(TAG,"On Item ok clicked " + seriesMap.keySet().toString());

        clearGraphs();
        resetAllvalues();
        optionsSelected=new ArrayList<String>();

        mCompareEditText.setText("");
        mCompareEditText.setTextSize(15);
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

       // presentApt=selectedapt;


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
                color=Color.YELLOW;
                String text= mCompareEditText.getText().toString();
                if(TextUtils.isEmpty(text)) { mCompareEditText.setText(selectedapt);}
                else { mCompareEditText.setText(text+ "," + apt); }

                ref=database.getReference().child("dynamic_data").child(apt);
            }
            else if(apt.equals("Building min"))
            {
                apt="min";
                selectedapt="Building min";
                color=Color.GREEN;

                String text= mCompareEditText.getText().toString();
                if(TextUtils.isEmpty(text)) { mCompareEditText.setText(selectedapt);}
                else { mCompareEditText.setText(text+ "," + apt); }
                ref=database.getReference().child("dynamic_data").child(apt);

            }
            else if(apt.equals("Building max"))
            {
                apt="max";
                selectedapt="Building max";
                color=Color.RED;
                String text= mCompareEditText.getText().toString();

                Log.e(TAG,"previous text " + text);
                if(TextUtils.isEmpty(text)) { mCompareEditText.setText(selectedapt);}
                else { mCompareEditText.setText(text+ "," + apt); }
                ref=database.getReference().child("dynamic_data").child(apt);


            }
            else {
                if(selectedapt.equals("Apartment802") || selectedapt.equals("Apartment302"))
                {
                    color=Color.MAGENTA;
                }
                String text= mCompareEditText.getText().toString();
                if(TextUtils.isEmpty(text)) { mCompareEditText.setText(apt);}
                else { mCompareEditText.setText(text+ "," + apt); }
                ref=database.getReference().child("dynamic_data").child(selectedapt);

            }

           readData(ref, selectedapt, color);
            Log.e(TAG,"After read data############");


        }
        else

        {
            Log.e(TAG,"IN else#######");
            clearGraphs();
            String text= mCompareEditText.getText().toString();
            mCompareEditText.setText(text+ " " + apt);

        }
        //  }



    }





    public interface OnGetDataListener {
        //make new interface for call back
        void onSuccess(DataSnapshot dataSnapshot);
        void onStart();
        void onFailure();
    }
    public HomeFragment() {
        // Required empty public constructor


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        SharedPreferences prefs = getActivity().getSharedPreferences(mypref,
                Context.MODE_PRIVATE);
        aptno= prefs.getString("Apartment_no",null);

        Log.e(TAG,"Present apartment in oncreateview"+aptno);
        String apt="Apartment"+aptno;
        presentApt=apt;

        view=inflater.inflate(R.layout.fragment_home, container, false);
        mAvgValueTextView= (TextView) view.findViewById(R.id.avg_powerValueTextView);
        mAvgValueTextView.setVisibility(View.VISIBLE);
        mUnitsConsumed=(TextView)view.findViewById(R.id.dynamic_units_ValueTextView);
        mUnitsConsumed.setVisibility(View.VISIBLE);

        graph = (GraphView)view.findViewById(R.id.graph);
        mCompareEditText=(EditText)view.findViewById(R.id.dynamicCompareEditText);
        mCompareEditText.setEnabled(false);
        mCompareEditText.setOnClickListener(this);

        mCompareTextView=(TextView)view.findViewById(R.id.CompareTextView);

        mNodataTextView=(TextView)view.findViewById(R.id.dynamicNoDataTextView);
        mNodataTextView.setVisibility(View.INVISIBLE);

//        achartcontainer=(LinearLayout)view.findViewById(R.id.chart_container);
//
//       plotAChart();
        mProgressBar=(ProgressBar)view.findViewById(R.id.dynamicprogressBar_cyclic);
        hideProgressBar();


        Log.e(TAG,"In home fragment");
        Log.e(TAG,aptno+apt);


       // configureGraph();
        showProgressBar();
        ref=database.getReference().child("dynamic_data").child(apt);
        presentApt=apt;
        readData(ref,apt,Color.BLUE);

        return view;


    }

    private void plotAChart() {

        String[] mMonth = new String[] {
                "Jan", "Feb" , "Mar", "Apr", "May", "Jun",
                "Jul", "Aug" , "Sep", "Oct", "Nov", "Dec"
        };
        int[] x_values = { 1,2,3,4,5,6,7,8 };
        int[] y_values = { 1000,1500,1700,2000,2500,3000,3500,3600};
        XYSeries expenseSeries = new XYSeries("Expense");
        for(int i=0;i<x_values.length;i++){
            expenseSeries.add(x_values[i], y_values[i]);
        }

        XYMultipleSeriesDataset xyMultipleSeriesDataset = new XYMultipleSeriesDataset();
        xyMultipleSeriesDataset.addSeries(expenseSeries);

        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(Color.GREEN);
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setFillPoints(true);
        renderer.setLineWidth(3);


        renderer.setChartValuesTextSize(20);

        renderer.setDisplayChartValues(true);


        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();

     multiRenderer.setMarginsColor(Color.GRAY);

// Disable Pan on two axis
        multiRenderer.setPanEnabled(false, false);
//        multiRenderer.setYAxisMax(35);
//        multiRenderer.setYAxisMin(0);



        multiRenderer.setFitLegend(true);
        multiRenderer.setLegendTextSize(20);
        multiRenderer.setShowGrid(true); // we show the grid
        multiRenderer.setShowGridX(true);
        multiRenderer.setShowGridY(true);

        multiRenderer.setXLabels(0);
        multiRenderer.setLabelsTextSize(25);
        multiRenderer.setLabelsColor(Color.BLUE);
        multiRenderer.setChartTitle("Expense Chart");
        multiRenderer.setXTitle("Year 2016");
        multiRenderer.setYTitle("Amount in Dollars");
        multiRenderer.setChartTitleTextSize(28);

        multiRenderer.setAxisTitleTextSize(24);
        multiRenderer.setMargins(new int[] { 2,30, 8, 10 });
        multiRenderer.setZoomButtonsVisible(false);

        multiRenderer.setZoomEnabled(true, true);
      //  multiRenderer.setZoomLimits(new double[] {500,4000});


        for(int i=0;i<x_values.length;i++){
            multiRenderer.addXTextLabel(i+1, mMonth[i]);
        }


        // transparent margins


       multiRenderer.addSeriesRenderer(renderer);



        View chart = ChartFactory.getLineChartView(getContext(), xyMultipleSeriesDataset, multiRenderer);

        achartcontainer.addView(chart);


    }

    private void configureGraph() {

        graph.setTitle(" ");
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();

        gridLabel.setHorizontalAxisTitle("Hour of Day");
        gridLabel.setVerticalAxisTitle("Power(W)");



        gridLabel.setNumHorizontalLabels(7); // only 4 because of the spacegraph.getGridLabelRenderer().setHumanRounding(false);
        gridLabel.setVerticalLabelsAlign(Paint.Align.CENTER);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setBorderColor(Color.BLUE);

        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalableY(true);
        graph.getViewport().setScrollableY(true);
    }
    private void addItemsOnSpinners() {
        List<String> adult_number_list=new ArrayList<String>();

        adult_number_list.add("None");
        if(aptno.equals("802"))
        {
            adult_number_list.add("Apartment302");
        }

     else
        {
            adult_number_list.add("Apartment802");
        }



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_spinner_item,adult_number_list);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCompareSpinner.setAdapter(adapter);
        int spinnerPosition = mCompareSpinner.pointToPosition(1,1);

        mCompareSpinner.setSelection(spinnerPosition);

        mCompareSpinner.setOnItemSelectedListener(this);

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

          public Date date,start_date;
          public Calendar cal;
          public Timestamp timestamp;

          @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                //whatever you need to do with the data
//              if(!TextUtils.isEmpty(aptname))
//              {
//                  return;
//              }
              Log.e(TAG,"In on success" +aptname);
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
                      mNodataTextView.setVisibility(View.VISIBLE);
                      mNodataTextView.setText("No Data Available");

                          mCompareEditText.setEnabled(true);
                          //onOkClicked();

                      return;
                  }
              }

              LineGraphSeries<DataPoint> existence = seriesMap.get(aptname);

              Log.e(TAG,"Existence"+ aptname + existence + seriesMap.keySet().toString());
              if(existence!=null)
              {
                  return;
              }

                Log.e(TAG,"In on success");
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                List<String> xLabelsList = new ArrayList<String>();

                List<Entry> entries1 = new ArrayList<Entry>();
                Date x=null;
                Long y;
                int size= (int) dataSnapshot.getChildrenCount();
                Log.e(TAG,dataSnapshot.getChildren().toString());
                int i=0;
                for (DataSnapshot children: dataSnapshot.getChildren()) {
                    // TODO: handle the post
                    Log.e(TAG, "KKL" + children.getValue().toString());
                    Map<String, Object> dt = (Map<String, Object>) children.getValue();
                    Log.e(TAG, String.valueOf(i));

                    String str= (String) dt.get("power");
                    Log.e(TAG, "Check"+(String) dt.get("power"));
                    if (!TextUtils.isEmpty(str)) {

                        y = Double.valueOf((String) dt.get("power")).longValue();
                        String str_date = (String) dt.get("timestamp");
                        DateFormat formatter;

                        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                           date = formatter.parse(str_date);
                            timestamp = new Timestamp(date.getTime());

                            if (i == 0) {
                               start_date=date;
                                i=i+1;
                            }
                            Log.e(TAG,"Timestamp"+timestamp);
                            cal = Calendar.getInstance();
                            cal.setTime(date);
                            int xval=cal.get(Calendar.HOUR_OF_DAY);

                            String newString = new SimpleDateFormat("HH:mm").format(date);

                           xLabelsList.add(newString);
                            Log.e(TAG, "X :" + xval + "Y: " + y + "GR"+newString + "DATE " + date );

                            powerValues.add(y);
                            timestampValues.add(timestamp);
                            //long energy = Double.valueOf((String) dt.get("energy")).longValue();
                            //energyValues.add(energy);
                            series.appendData(new DataPoint(date, y), true, size);

                            entries1.add(new Entry(xval, y));

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }



                        i = i + 1;
                    }
                }
                Log.e(TAG,"data done");


              series.setTitle(aptname);
              series.setColor(color);
              series.setDrawDataPoints(true);
              series.setDataPointsRadius(5);



              graph.getLegendRenderer().setVisible(true);
              graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
              graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);


              hideProgressBar();
              Log.e(TAG,"Series"+seriesMap.keySet().toString());
              graph.addSeries(series);


              seriesMap.put(aptname,series);

              Log.e(TAG,"after add Series"+seriesMap.keySet().toString());
              GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
              graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.BOTH);

              gridLabel.setHorizontalAxisTitle("Hour of Day");
              gridLabel.setVerticalAxisTitle("Power(W)");

              // graph.getViewport().setMaxX(24);
              gridLabel.setLabelVerticalWidth(7);

              DateAsXAxisLabelFormatter datelabel = new DateAsXAxisLabelFormatter(getActivity(), new SimpleDateFormat("HH:mm"));

              gridLabel.setLabelFormatter(datelabel);
             // graph.getGridLabelRenderer().setHumanRounding(false);
              gridLabel.setNumHorizontalLabels(6); // only 4 because of the spacegraph.getGridLabelRenderer().setHumanRounding(false);

              gridLabel.setHorizontalLabelsAngle(165);
              gridLabel.setVerticalLabelsAlign(Paint.Align.CENTER);

              graph.getViewport().setXAxisBoundsManual(true);


              Calendar maxcal = Calendar.getInstance(); // creates calendar
              maxcal.setTime(date); // sets calendar time/date
              maxcal.add(Calendar.HOUR_OF_DAY,1); // adds one hour
              date=maxcal.getTime(); // ret
              Log.e(TAG,"MAx X%%%%%"+date.getTime()+ new SimpleDateFormat("HH:mm").format(date.getTime()));
              Log.e(TAG,"X labels .....#####"+xLabelsList.toString());
              graph.getViewport().setMaxX(date.getTime());
              graph.getViewport().setMinX(start_date.getTime());


              graph.getViewport().setScalable(true);

              graph.getViewport().setScrollable(true);
              graph.getViewport().setScrollableY(true);
              graph.getViewport().setScalableY(true);


              Log.e("TAG","AvgText"+mAvgValueTextView.getText());
              graph.setTitle("Comparison of trends");


              if(TextUtils.isEmpty(mAvgValueTextView.getText())) {
                  graph.setTitle("Today's Consumption for your Apartment "+aptno);

                  double avg = calculateAvgConsumption(powerValues);
                  avg = round(avg);
                  mAvgValueTextView.setText(String.valueOf(avg));

                  double totalEnergy = (double) (avg * powerValues.size() * 10) / (60);
                  double units = round(totalEnergy / 1000);
                  mUnitsConsumed.setText(String.valueOf(units));
                  mCompareEditText.setEnabled(true);

                  //addItemsOnSpinners();


              }

              Log.e(TAG,"one option done");

              Log.e(TAG,presentApt + " " +aptname);
              if(aptname==presentApt)
              {
                  Log.e(TAG,"Initial loading");
                  mCompareEditText.setEnabled(true);
                  onOkClicked();

              }

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


    private double calculateAvgConsumption(ArrayList<Long> Values) {
        int n =Values.size();
        int sum=0;

        for (int i = 0; i < n; i++)
            sum += Values.get(i);

        Log.e(TAG,"sum"+sum);
        return ((double) sum) / n;
    }

}
