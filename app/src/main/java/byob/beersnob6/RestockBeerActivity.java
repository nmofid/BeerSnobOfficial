package byob.beersnob6;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Restock Beer Activity
 */

public class RestockBeerActivity extends AppCompatActivity {
    private final String url = "http://www.amazon.com/fresh";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restockbeer);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String title = "Beer Snob Restock Page";
        SpannableString s = new SpannableString(title);
        s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        TextView report = (TextView)findViewById(R.id.textView);
        report.setTextColor(Color.WHITE);
        report.setText("Resock Beer at Amazon's Store");

        android.widget.Button Restock;
        Restock = (android.widget.Button)findViewById(R.id.button9);
        Restock.setTextSize(20);
        Restock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("Onclick","Im Alive!");
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                (RestockBeerActivity.this).startActivity(intent);

            }

        });

    }
}
