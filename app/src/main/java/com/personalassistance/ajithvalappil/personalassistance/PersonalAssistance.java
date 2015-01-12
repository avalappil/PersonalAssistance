package com.personalassistance.ajithvalappil.personalassistance;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.app.AlertDialog;
import android.view.SurfaceView;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import java.io.*;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.DialogInterface;


public class PersonalAssistance extends ActionBarActivity implements SurfaceHolder.Callback{

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;
    Button buttonTakePicture;
    TextView textView;
    TextView xdata;
    TextView ydata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_assistance);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        textView = (TextView)findViewById(R.id.textView);
        xdata = (TextView)findViewById(R.id.xdata);
        ydata = (TextView)findViewById(R.id.ydata);

        controlInflater = LayoutInflater.from(getBaseContext());


        buttonTakePicture = (Button)findViewById(R.id.buttonTakePicture);
        buttonTakePicture.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                camera.takePicture(myShutterCallback,
                        myPictureCallback_RAW, myPictureCallback_JPG);
            }});

        RelativeLayout layoutBackground = (RelativeLayout)findViewById(R.id.background);
        layoutBackground.setOnClickListener(new RelativeLayout.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                buttonTakePicture.setEnabled(false);
                camera.autoFocus(myAutoFocusCallback);
            }});

    }

    FaceDetectionListener faceDetectionListener
            = new FaceDetectionListener(){

        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {

            if (faces.length == 0){
                System.out.println(" No Face Detected! ");
            }else{
                System.out.println(String.valueOf(faces.length) + " Face Detected :) ");
                textView.setText(String.valueOf(faces.length) + " Face Detected :) ");
                if (faces.length > 0) {
                    // We could see if there's more than one face and do something in that case. What though?
                    Rect rect = faces[0].rect;
                    float x = (rect.left + rect.right)*0.5f;
                    float y = (rect.top + rect.bottom)*0.5f;
                    System.out.println("x: " + x + "y: " + y);
                    xdata.setText(String.valueOf(x));
                    ydata.setText(String.valueOf(y));
                    // If the face is on the left, turn left, if it's on the right, turn right.
                    // Speed is proportional to how far to the side of the image the face is.
                    // The coordinates we get from face detection are from -1000 to 1000.
                    // NXT motor speed is from -100 to 100.
                }
            }


        }
    };

    PictureCallback myPictureCallback_JPG = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub
   /*Bitmap bitmapPicture
    = BitmapFactory.decodeByteArray(arg0, 0, arg0.length); */

            Uri uriTarget = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, new ContentValues());

            OutputStream imageFileOS;
            try {
                imageFileOS = getContentResolver().openOutputStream(uriTarget);
                imageFileOS.write(arg0);
                imageFileOS.flush();
                imageFileOS.close();

                System.out.println("Image saved: " + uriTarget.toString());

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            camera.startPreview();
            camera.startFaceDetection();
        }
    };


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        if(previewing){
            camera.stopFaceDetection();
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null){
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();

                System.out.println(String.valueOf(
                        "Max Face: " + camera.getParameters().getMaxNumDetectedFaces()));
                camera.startFaceDetection();
                previewing = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera = Camera.open();
        camera.setFaceDetectionListener(faceDetectionListener);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera.stopFaceDetection();
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }

    AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // TODO Auto-generated method stub
            buttonTakePicture.setEnabled(true);
        }};

    ShutterCallback myShutterCallback = new ShutterCallback(){

        @Override
        public void onShutter() {
            // TODO Auto-generated method stub

        }};

    PictureCallback myPictureCallback_RAW = new PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub

        }};

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_personal_assistance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                finish();
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }

}
