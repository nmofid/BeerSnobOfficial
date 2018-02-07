package byob.beersnob6;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.kosalgeek.asynctask.AsyncResponse;
import com.kosalgeek.asynctask.PostResponseAsyncTask;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nikka Mofid on 1/11/2018.
 */

public class DataAnalyticsActivity extends AppCompatActivity implements AsyncResponse{
    List<String>  allelements;
    ArrayList<Date>  datelist;
    ArrayList<String>  templist;
    ArrayList<DataPoint> dataPoints;
    GraphView graph;
    LineGraphSeries<DataPoint> series, series_test;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_analytics);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<>();

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, DateFormat.getTimeInstance()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

        // set manual x bounds to have nice steps
        //graph.getViewport().setMinX(datelist.get(0).getTime());
        //graph.getViewport().setMaxX(datelist.get(datelist.size()).getTime());
        //graph.getViewport().setXAxisBoundsManual(true);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        //graph.getGridLabelRenderer().setHumanRounding(false);



        android.widget.Button Temp;
        Temp = (android.widget.Button)findViewById(R.id.button5);
        Temp.setTextSize(20);
        Temp.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                //Intent i = new Intent(getApplicationContext(), FridgeStatusActivity.class);
                //startActivity(i);

                Log.d("Onclick","Im Alive!");
                graph.removeAllSeries();
                //https://github.com/kosalgeek/generic_asynctask <--use this for the AsyncRespone not built in Android Studio Library
                PostResponseAsyncTask tempqueryTask = new PostResponseAsyncTask(DataAnalyticsActivity.this, DataAnalyticsActivity.this);
                tempqueryTask.execute("http://192.168.1.117/tempquery.php");

            }



        });

    }

    @Override
    public void processFinish(String output) {
        //you can get 'output' from here as a string
        //if the incoming data belongs to temp then do put it into an array
        SimpleDateFormat formatter;

            String regex = "\\s*\\btemp\\b\\s*";
            output = output.replaceAll(regex, "");
            output = output.replaceAll("'", "");
            allelements = Arrays.asList(output.split("\\s*,\\s*"));
            templist = new ArrayList<String>();
            datelist = new ArrayList<Date>();
            dataPoints = new ArrayList<DataPoint>();
             series = new LineGraphSeries<>();
             series_test = new LineGraphSeries<>();
            for (int i = 0; i < allelements.size(); i++){
                if(i%2 == 0){
                    templist.add(allelements.get(i));


                }
                else{
                    //replace space with 'T' to match premade pattern for the String formatter
                    String date = allelements.get(i);
                    date = date.replaceAll(" ", "T");
                    date = date.substring(0,date.length() - 3);
                    date = date.concat("-07:00");
                    //System.out.print(date+",");
                    //create new date formatter object
                   formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

                    try {
                        datelist.add(formatter.parse(date));
                    }
                    catch(Exception e) {
                        System.out.println(e.getMessage());
                    }

                }

            }
        Log.d("ProcessFinish","Im Alive!");


            for(int i = 0; i < datelist.size(); i++){
                //create an array of Data Points with dates on the x and temp on the y
                Log.d("ProcessFinish", "datelist = "+datelist.get(i));
                try{
                    dataPoints.add(new DataPoint(datelist.get(i), Double.parseDouble(templist.get(i))));
                    series.appendData(dataPoints.get(i), true, 220);
                }
                catch(Exception ex) {
                    Log.d("ProcessFinish", "ERROR : " + ex.toString());
                }
            }

            System.out.println(templist);
            System.out.println(datelist);
            graph.addSeries(series);

    }
}
