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
import com.amazonaws.models.nosql.BottleCountDO;
import com.amazonaws.models.nosql.TempDO;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
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
 * Activity to show data analytics of the fridge's temeprature and bottle count.
 *
 * Uses data fetched from Amazon Dynamo DB to create the graphs.
 * There are 2 graphs: Fridge Temperature Graph for Today, and Maximum Bottle Count Over the Past 7 Days
 */

public class DataAnalyticsActivity extends AppCompatActivity{
    ArrayList<Date>  datelist;
    ArrayList<String>  templist, countlist, daylist;
    ArrayList<Integer> bottleCountList;
    ArrayList<DataPoint> dataPoints;
    GraphView graph;
    GridLabelRenderer render;
    LineGraphSeries<DataPoint> series;
    BarGraphSeries<DataPoint> barSeries;
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
                graph.removeAllSeries();
                graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(DataAnalyticsActivity.this, DateFormat.getTimeInstance()));
                graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
                graph.setTitle("Fridge Temperature Graph for Today");
                render.setVerticalAxisTitle("Temperature (°F)");

                graphTempData();
            }

        });

        android.widget.Button BottleCount;
        BottleCount = (android.widget.Button)findViewById(R.id.button5);
        BottleCount.setTextSize(20);
        BottleCount.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                graph.removeAllSeries();
                graph.setTitle("Maximum Bottle Count Over the Past 7 Days");
                render.setVerticalAxisTitle("Number of Bottles");

                graphBottleWeekData();
            }



        });

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
                Calendar cal = Calendar.getInstance(/*TimeZone.getTimeZone("UTC")*/);
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
                series = new LineGraphSeries<>();

                /* Fill temperature and date lists using DynamoDB data*/
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

    public void graphAllBottleData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                BottleCountDO hashKey = new BottleCountDO();
                hashKey.setUserId("0");
                hashKey.setCount("Count");
                hashKey.setTime("time");

                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withConsistentRead(false)
                        .withHashKeyValues(hashKey);

                PaginatedList<BottleCountDO> result = dynamoDBMapper.query(BottleCountDO.class, queryExpression);

                /* Parse output from query */
                Gson gson = new Gson();
                StringBuilder stringBuilder = new StringBuilder();

                SimpleDateFormat formatter;
                BottleCountDO tempItem;
                String date;
                countlist = new ArrayList<String>();
                datelist = new ArrayList<Date>();
                dataPoints = new ArrayList<DataPoint>();
                series = new LineGraphSeries<>();

                /* Fill bottle count and date lists */
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
                        countlist.add(tempItem.getCount());
                    }
                    catch(Exception e) {
                        System.out.println(e.getMessage());
                    }

                    Log.d("ProcessFinish", jsonFormOfItem);
                }

                /* Insert data points into graph */
                for(int i = 0; i < datelist.size(); i++){
                    try{
                        dataPoints.add(new DataPoint(datelist.get(i), Double.parseDouble(countlist.get(i))));
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

    public void graphBottleWeekData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Get the current date string
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, -6);
                String tempDate = getDate(cal);
                Log.d("ProcessFinish", tempDate);

                String[] strDays = new String[] { "Sun", "Mon", "Tues", "Wed", "Thurs",
                        "Fri", "Sat" };

                BottleCountDO bottleCount = new BottleCountDO();
                bottleCount.setUserId("0");
                bottleCount.setTime("time");
                bottleCount.setCount("count");

                PaginatedList<BottleCountDO> results;

                int goBack = 0;
                Integer prevCount = 0;
                BottleCountDO tempItem;
                Integer current = 0;
                Integer maxCount = 0;
                daylist = new ArrayList<String>();
                bottleCountList = new ArrayList<Integer>();
                dataPoints = new ArrayList<DataPoint>();
                barSeries = new BarGraphSeries<>();

                // Find the first prevCount of bottles (aka before the point 7 days previous from today)
                do{
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    tempDate = getDate(cal);
                    goBack++;

                    Condition rangedKeyCondition = new Condition()
                            .withComparisonOperator(ComparisonOperator.BEGINS_WITH)
                            .withAttributeValueList(new AttributeValue().withS(tempDate));

                    DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                            .withRangeKeyCondition("time",rangedKeyCondition)
                            .withConsistentRead(false)
                            .withHashKeyValues(bottleCount);

                    results = dynamoDBMapper.query(BottleCountDO.class, queryExpression);

                    if(results.size() != 0){
                        for (int y = 0; y < results.size(); y++) {
                            tempItem = results.get(y);
                            current = Integer.parseInt(tempItem.getCount());
                            if(current > maxCount){
                                maxCount = current;
                            }
                        }
                        prevCount = maxCount;
                        maxCount = 0;
                        break;
                    }

                } while(goBack >= 3); // only check 3 days prior

                // Set the calendar back to 7 days prior to today
                cal.add(Calendar.DAY_OF_MONTH, goBack);
                tempDate = getDate(cal);

                Log.d("ProcessFinsih", "PrevCount = "+prevCount);


                // For each day over the past 7 days, get max bottle count data and day of the week
                for(int x = 0; x < 7; x++){
                    Condition rangedKeyCondition = new Condition()
                            .withComparisonOperator(ComparisonOperator.BEGINS_WITH)
                            .withAttributeValueList(new AttributeValue().withS(tempDate));

                    DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                            .withRangeKeyCondition("time",rangedKeyCondition)
                            .withConsistentRead(false)
                            .withHashKeyValues(bottleCount);

                    results = dynamoDBMapper.query(BottleCountDO.class, queryExpression);

                    // Find max number of bottles for the given calendar day
                    if(results.size() == 0){
                        maxCount = prevCount;
                    }
                    else {
                        for (int y = 0; y < results.size(); y++) {
                            tempItem = results.get(y);
                            current = Integer.parseInt(tempItem.getCount());
                            if(current > maxCount){
                                maxCount = current;
                            }
                        }
                        prevCount = maxCount;
                    }

                    daylist.add(strDays[cal.get(Calendar.DAY_OF_WEEK)-1]);
                    bottleCountList.add(maxCount);

                    maxCount = 0;
                    cal.add(Calendar.DAY_OF_MONTH, +1);
                    tempDate = getDate(cal);
                }

                // Add series to the graph
                for(int i = 0; i < daylist.size(); i++){
                    try{
                        dataPoints.add(new DataPoint(i, bottleCountList.get(i)));
                        barSeries.appendData(dataPoints.get(i), true, 220);
                    }catch(Exception e){
                        Log.d("ProcessFinish", "ERROR : " + e.toString());
                    }
                }

                // Set the x-axis as days of the week from the past 7 days including today
                StaticLabelsFormatter labels = new StaticLabelsFormatter(graph);
                String[] days = daylist.toArray(new String[daylist.size()]);
                Log.d("ProcessFinish", "Days= "+days.toString());
                labels.setHorizontalLabels(days);
                graph.getGridLabelRenderer().setNumHorizontalLabels(7); // set to 7 for past 7 days
                graph.getGridLabelRenderer().setLabelFormatter(labels);

                // styling the colors to alternate yellow, orange
                barSeries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                    @Override
                    public int get(DataPoint data) {
                        if(data.getX() % 2 == 0){
                            return Color.rgb(255, 236, 0); //gold
                        }
                        else{
                            return Color.rgb(255, 158, 0); // orange
                        }
                    }
                });
                barSeries.setSpacing(30);
                barSeries.setDrawValuesOnTop(true);
                barSeries.setValuesOnTopColor(Color.WHITE);
                graph.addSeries(barSeries);
            }

        }).start();
    }

    public String getDate(Calendar cal){
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // because returns int 0 to 11
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String tempDate = String.format("%4d-%02d-%02d", year, month, day);
        return tempDate;
    }
}
