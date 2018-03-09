package byob.beersnob6;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList;
import com.amazonaws.models.nosql.BottleCountDO;
import com.amazonaws.models.nosql.TempDO;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/*
 * Fridge Status Activity
 *
 * Lists current temperature, current bottle count, and datetime last fridge content image was captured.
 */

public class FridgeStatusActivity extends AppCompatActivity {
    DynamoDBMapper dynamoDBMapper;
    String currentBottleCount, currentTemp, recentImageDate;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridgestatus);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String title = "Beer Snob Fridge Status";
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        // Set up Amazon Dyanamo DB Client
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        // Set up text view string
        getCurrentBottleCount();
        getCurrentTemperature();
        String message = "\nBottle Count: \n\nTemperature:  °F\n\nFridge Contents Image Last Taken: ";

        TextView report = (TextView)findViewById(R.id.report);
        report.setTextColor(Color.WHITE);
        report.setText(message);

        android.widget.Button Refresh;
        Refresh = (android.widget.Button)findViewById(R.id.button7);
        Refresh.setTextSize(20);
        Refresh.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                Log.d("Onclick","Im Alive!");
                getCurrentBottleCount();
                String message = "\nBottle Count: " + currentBottleCount + "\n\nTemperature: " + currentTemp +
                        " °F\n\nFridge Contents Image Last Taken: " + recentImageDate;
                TextView report = (TextView)findViewById(R.id.report);
                report.setTextColor(Color.WHITE);
                report.setText(message);
            }



        });
    }

    public void getCurrentBottleCount(){

        new Thread(new Runnable() {
            @Override
            public void run() {

                // Get the current date string
                Calendar cal = Calendar.getInstance();
                String tempDate = getDate(cal);
                Log.d("ProcessFinish", tempDate);

                BottleCountDO bottleCount = new BottleCountDO();
                bottleCount.setUserId("0");
                bottleCount.setTime("time");
                bottleCount.setCount("count");

                PaginatedList<BottleCountDO> results;

                // Get the most recent list of bottle counts, based on day of the month.
                do{
                    /* Set up query to dynamoDB, output all temperatures from day of query */
                    Condition rangedKeyCondition = new Condition()
                            .withComparisonOperator(ComparisonOperator.BEGINS_WITH)
                            .withAttributeValueList(new AttributeValue().withS(tempDate));

                    DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                            .withRangeKeyCondition("time",rangedKeyCondition)
                            .withConsistentRead(false)
                            .withHashKeyValues(bottleCount);

                    results = dynamoDBMapper.query(BottleCountDO.class, queryExpression);

                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    tempDate = getDate(cal);
                    Log.d("ProcessFinish", tempDate);
                } while(results.size() == 0);

                BottleCountDO tempItem;
                SimpleDateFormat formatter;
                String date, count;
                Date temp, mostRecent;
                mostRecent = null;
                String mostRecentBottleCount = "0";

                // Find the most recent bottle count
                for(int i = 0; i < results.size(); i++){
                    tempItem = results.get(i);
                    formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                    count = tempItem.getCount();
                    date = tempItem.getTime();
                    date = date.replaceAll(" ", "T");
                    date = date.substring(0,date.length() - 3);
                    date = date.concat("-08:00"); //Time Zone PST
                    try {
                        if (i == 0) {
                            mostRecent = formatter.parse(date);
                            mostRecentBottleCount = count;
                            Log.d("ProcessFinish", "First update to bottle count: "+mostRecentBottleCount+" DATE: "+mostRecent.toString());
                        }
                        else{
                            temp = formatter.parse(date);
                            if(/*mostRecent != null && */temp.after(mostRecent)){
                                mostRecent = temp;
                                mostRecentBottleCount = count;
                            }
                            Log.d("ProcessFinish", "Most recent update to bottle count: "+mostRecentBottleCount+" DATE: "+mostRecent.toString());
                        }
                    }catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                }

                currentBottleCount = mostRecentBottleCount;
                recentImageDate = mostRecent.toString();
                Log.d("ProcessFinish", "BOTTLE COUNT ="+mostRecentBottleCount);

            }
        }).start();
    }

    public void getCurrentTemperature(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                // Get the current date string
                Calendar cal = Calendar.getInstance();
                String tempDate = getDate(cal);
                Log.d("ProcessFinish", tempDate);

                TempDO myTemp = new TempDO();
                myTemp.setUserId("0");
                myTemp.setTime("time");
                myTemp.setTemp("temp");

                PaginatedList<TempDO> results;

                // Get the most recent list of bottle counts, based on day of the month.
                do{
                    /* Set up query to dynamoDB, output all temperatures from day of query */
                    Condition rangedKeyCondition = new Condition()
                            .withComparisonOperator(ComparisonOperator.BEGINS_WITH)
                            .withAttributeValueList(new AttributeValue().withS(tempDate));

                    DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                            .withRangeKeyCondition("time",rangedKeyCondition)
                            .withConsistentRead(false)
                            .withHashKeyValues(myTemp);

                    results = dynamoDBMapper.query(TempDO.class, queryExpression);

                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    tempDate = getDate(cal);
                    Log.d("ProcessFinish", tempDate);
                } while(results.size() == 0);

                TempDO tempItem;
                SimpleDateFormat formatter;
                String date, itemTemp;
                Date temp, mostRecent;
                mostRecent = null;
                String mostRecentTemp = "0";

                // Find the most recent bottle count
                for(int i = 0; i < results.size(); i++){
                    tempItem = results.get(i);
                    formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                    itemTemp = tempItem.getTemp();
                    date = tempItem.getTime();
                    date = date.replaceAll(" ", "T");
                    date = date.substring(0,date.length() - 3);
                    date = date.concat("-08:00"); //Time Zone PST
                    try {
                        if (i == 0) {
                            mostRecent = formatter.parse(date);
                            mostRecentTemp = itemTemp;
                            Log.d("ProcessFinish", "First update to TEMP: "+mostRecentTemp+" DATE: "+mostRecent.toString());
                        }
                        else{
                            temp = formatter.parse(date);
                            if(mostRecent != null && temp.after(mostRecent)){
                                mostRecent = temp;
                                mostRecentTemp = itemTemp;
                            }
                            Log.d("ProcessFinish", "Most recent update to TEMP: "+mostRecentTemp+" DATE: "+mostRecent.toString());
                        }
                    }catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                }

                currentTemp = mostRecentTemp;
                Log.d("ProcessFinish", "TEMPERATURE ="+mostRecentTemp);

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
