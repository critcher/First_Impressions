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
import android.widget.LinearLayout;


public class HomeScreen extends ActionBarActivity implements PictureReadyListener{
ThreadedCamera tc = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        Log.e("onCreate","" + android.os.Process.myTid());
        Log.e("cam id", "" + CameraUtils.getFrontCameraId());

        LinearLayout preview = (LinearLayout) findViewById(R.id.picLayout);
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("button clicked","hmmm");
                if(tc != null) {
                    tc.takePicture();
                }
            }
        });

        if(CameraUtils.checkForFrontCam(this)) {
            Log.e("starting up again","yay");
            cameraHolder ch = new cameraHolder(this,CameraUtils.getFrontCameraId(), this);
            preview.addView(ch);
            tc = ch.getThreadedCam();
        }
    }

    @Override
    public void onPictureReady(Bitmap bmap) {
        final ImageView imgView = (ImageView) findViewById(R.id.imageView);
        final Bitmap b_img = FaceDetectorUtils.getFace(bmap);
        imgView.post(new Runnable() {
            public void run() {
                imgView.setImageBitmap(b_img);
                Log.e("UI","image should be there " + (b_img!=null));
            }
        });
        tc.getCamera().startPreview();
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

}
