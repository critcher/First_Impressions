package nora.clayton.firstimpressions;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;


public class HomeScreen extends ActionBarActivity {
ThreadedCamera tc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("button clicked","hmmm");
                tc.takePicture();
            }
        });


        tc = new ThreadedCamera(CameraUtils.getFrontCameraId(), this);
        Log.e("onCreate","" + android.os.Process.myTid());
        tc.startCam();
        preview.addView(tc.ch);


        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        int picIndex = R.drawable.lena;
        Drawable pic = getResources().getDrawable(picIndex);
        Bitmap pic2 = FaceDetectorUtils.drawableToBitmap(pic);
        Bitmap b_img = FaceDetectorUtils.getFace(pic2);
        imgView.setImageBitmap(b_img);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPause(){
        super.onPause();
        tc.stopCam();
    }
}
