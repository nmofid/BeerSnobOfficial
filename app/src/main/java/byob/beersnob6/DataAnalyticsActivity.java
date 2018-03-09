package byob.beersnob6;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList;
import com.amazonaws.models.nosql.TempDO;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
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
import java.util.TimeZone;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.mobile.auth.core.IdentityManager;

/**
 * Activity to show temperature graphed over time.
 *
 * Uses temperature data fetched from Amazon Dynamo DB to create the graph.
 */

public class DataAnalyticsActivity extends AppCompatActivity implements AsyncResponse{
    List<String>  allelements;
    ArrayList<Date>  datelist;
    ArrayList<String>  templist;
    ArrayList<DataPoint> dataPoints;
    GraphView graph;
    GridLabelRenderer render;
    LineGraphSeries<DataPoint> series, series_test;
    DynamoDBMapper dynamoDBMapper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_analytics);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        String title = "Beer Snob Analytics";
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();


        graph = (GraphView) findViewById(R.id.graph);
        graph.setTitleColor(Color.WHITE);
        render = graph.getGridLabelRenderer();
        render.setGridColor(Color.WHITE);
        render.setVerticalAxisTitleColor(Color.WHITE);
        render.setVerticalLabelsColor(Color.WHITE);
        render.setHorizontalLabelsColor(Color.WHITE);
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
        Temp = (android.widget.Button)findViewById(R.id.button8);
        Temp.setTextSize(20);
        Temp.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                //Intent i = new Intent(getApplicationContext(), FridgeStatusActivity.class);
                //startActivity(i);

                Log.d("Onclick","Im Alive!");
                graph.removeAllSeries();
                graph.setTitle("Fridge Temperature Data Analytics");
                render.setVerticalAxisTitle("Temperature (°F)");

                //https://github.com/kosalgeek/generic_asynctask <--use this for the AsyncRespone not built in Android Studio Library
                //PostResponseAsyncTask tempqueryTask = new PostResponseAsyncTask(DataAnalyticsActivity.this, DataAnalyticsActivity.this);
                //tempqueryTask.execute("http://192.168.1.117/tempquery.php");
                graphTempData();

            }



        });

        android.widget.Button BottleCount;
        BottleCount = (android.widget.Button)findViewById(R.id.button5);
        BottleCount.setTextSize(20);
        BottleCount.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Log.d("Onclick","Im Alive!");
                graph.removeAllSeries();
                graphTempData();
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

    public void graphTempData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                TempDO myTemp = new TempDO();
                myTemp.setUserId("0");
                myTemp.setTemp("temp");
                myTemp.setTime("time");

                /* Get the current date */
                Calendar cal = Calendar.getInstance(/*TimeZone.getTimeZone("UTC")*/); //Pi records date log in UTC
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1; // because returns int 0 to 11
                int day = cal.get(Calendar.DAY_OF_MONTH);
                String currentDate = String.format("%4d-%02d-%02d", year, month, day);
                Log.d("ProcessFinish", currentDate);

                /* Set up query to dynamoDB, output all temperatures from day of query */
                Condition rangedKeyCondition = new Condition()
                        .withComparisonOperator(ComparisonOperator.BEGINS_WITH)
                        .withAttributeValueList(new AttributeValue().withS(currentDate));

                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withRangeKeyCondition("time",rangedKeyCondition)
                        .withConsistentRead(false)
                        .withHashKeyValues(myTemp);

                PaginatedList<TempDO> result = dynamoDBMapper.query(TempDO.class, queryExpression);

                /* Parse output from query */
                Gson gson = new Gson();
                StringBuilder stringBuilder = new StringBuilder();

                SimpleDateFormat formatter;
                TempDO tempItem;
                String date;
                templist = new ArrayList<String>();
                datelist = new ArrayList<Date>();
                dataPoints = new ArrayList<DataPoint>();

                /* Fill temperature and date lists */
                for(int i = 0; i < result.size(); i++){
                    tempItem = result.get(i);
                    String jsonFormOfItem = gson.toJson(tempItem);
                    stringBuilder.append(jsonFormOfItem + "\n\n");

                    formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                    date = tempItem.getTime();
                    date = date.replaceAll(" ", "T");
                    date = date.substring(0,date.length() - 3);
                    date = date.concat("-08:00"); //Time Zone PST

                    try {
                        datelist.add(formatter.parse(date));
                        templist.add(tempItem.getTemp());
                    }
                    catch(Exception e) {
                        System.out.println(e.getMessage());
                    }

                    Log.d("ProcessFinish", jsonFormOfItem);
                }

                /* Insert data points into graph */
                for(int i = 0; i < datelist.size(); i++){
                    try{
                        dataPoints.add(new DataPoint(datelist.get(i), Double.parseDouble(templist.get(i))));
                        series.appendData(dataPoints.get(i), true, 220);
                    }
                    catch(Exception ex) {
                        Log.d("ProcessFinish", "ERROR : " + ex.toString());
                    }
                }

                series.setColor(Color.YELLOW);
                graph.addSeries(series);


            }
        }).start();
    }
}
