package byob.beersnob6;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.amazonaws.mobile.client.AWSMobileClient;

public class MainActivity extends AppCompatActivity {
    android.widget.Button FridgeStatus,FridgeCamera,RestockBeer,DataAnalytics ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AWSMobileClient.getInstance().initialize(this).execute();

        setContentView(R.layout.activity_main);

        FridgeStatus = (android.widget.Button)findViewById(R.id.button1);
        FridgeStatus.setTextSize(20);
        FridgeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FridgeStatusActivity.class);
                startActivity(i);
            }
        });

        FridgeCamera = (android.widget.Button)findViewById(R.id.button2);
        FridgeCamera.setTextSize(20);
        FridgeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FridgeCameraActivity.class);
                startActivity(i);
            }
        });

        RestockBeer = (android.widget.Button)findViewById(R.id.button3);
        RestockBeer.setTextSize(20);
        RestockBeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RestockBeerActivity.class);
                startActivity(i);
            }
        });

        DataAnalytics = (android.widget.Button)findViewById(R.id.button4);
        DataAnalytics.setTextSize(20);
        DataAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DataAnalyticsActivity.class);
                Log.d("Mytag","Im Alive!");
                startActivity(i);
            }
        });

    }
}
