package byob.beersnob6;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Carissa Kane on 1/18/18.
 */

public class GraphActivity extends Activity{

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_activity);

        Intent myIntent = getIntent();
        String graphType = myIntent.getStringExtra("graphType");
        System.out.print("graphType = "+graphType);

    }
}
