package com.personalassistance.ajithvalappil.personalassistance;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.app.AlertDialog;
import android.view.SurfaceView;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.View;
import java.io.*;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.DialogInterface;
import java.util.*;
import android.os.Handler;
import android.os.Message;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.widget.*;
import android.content.*;


public class PersonalAssistance extends ActionBarActivity implements SurfaceHolder.Callback{

    //Camera
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;
    Button buttonTakePicture;
    TextView textView;
    TextView xdata;
    TextView ydata;

    //bluetooth
    static List<String> items = new ArrayList<String>();
    static boolean processingComplete = false;
    static boolean isDevicesConnected = false;
    static boolean containsDevicesConnected = false;
    Button connectBlu;
    RelativeLayout aMainLayout;
    RelativeLayout aBluListLayout;
    BluetoothController aBluetoothController = new BluetoothController();
    public BluetoothAdapter btAdapter = null;
    public BluetoothSocket btSocket = null;
    static final int REQUEST_ENABLE_BT = 0;
    ArrayAdapter<String> mArrayAdapter;
    ListView mBluAdapter;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();

            if (bundle.containsKey("devicelist")){
                String cnt  = bundle.getString("devicelist");
                System.out.println("No of devices.....>> " + cnt);
            }

            if (bundle.containsKey("connected")){
                String msgData  = bundle.getString("connected");
                System.out.println("Complete.....>> " + msgData);
                Button connectBlu=(Button)findViewById(R.id.connect);
                if (msgData!=null && msgData.equalsIgnoreCase("Connected")){
                    connectBlu.setText("Disconnect");
                }else if (msgData!=null && msgData.equalsIgnoreCase("Disconnected")){
                    connectBlu.setText("Connect");
                }
                System.out.println("Complete.....");
            }
            if (bundle.containsKey("message")){
                String msgData  = bundle.getString("message");
                System.out.println("Complete.....>> " + msgData);
                TextView voiceToText = (TextView)findViewById(R.id.textView);
                voiceToText.append("Controller:" + msgData);
            }
        }
    };

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

        connectBlu=(Button)findViewById(R.id.connect);

    }

    FaceDetectionListener faceDetectionListener
            = new FaceDetectionListener(){

        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {

            if (faces.length == 0){
                //System.out.println(" No Face Detected! ");
            }else{
                //System.out.println(String.valueOf(faces.length) + " Face Detected");
                textView.setText(String.valueOf(faces.length) + " Face Detected");
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

        aMainLayout = (RelativeLayout)findViewById(R.id.background);
        aBluListLayout = (RelativeLayout)findViewById(R.id.aBluListLayout);
        connectBlu=(Button)findViewById(R.id.connect);

        mBluAdapter = (ListView)findViewById(R.id.listView);
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, items);
        mBluAdapter.setAdapter(mArrayAdapter);
        mBluAdapter.setChoiceMode(mBluAdapter.CHOICE_MODE_SINGLE);

        try {
            System.out.println("Starting.....");
            Toast.makeText(this, "Starting...", Toast.LENGTH_SHORT).show();
            aBluetoothController = new BluetoothController();
            aBluetoothController.setProcessType("init");
            System.out.println("init.....");
            System.out.println("Thread started.....");
            aBluetoothController.start();
            System.out.println("wait for complete started.....");
            Toast.makeText(this, "Waiting for devices...", Toast.LENGTH_SHORT).show();
            aBluetoothController.join();
            System.out.println("Complete.....");
            System.out.println("aBluetoothController.isDeviceHasBluetooth() >>" + aBluetoothController.isDeviceHasBluetooth());
            System.out.println("aBluetoothController.isDeviceBluetoothIsOn() >>" + aBluetoothController.isDeviceBluetoothIsOn());
            if (aBluetoothController.isDeviceHasBluetooth()){
                if (!aBluetoothController.isDeviceBluetoothIsOn()){
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    btAdapter = aBluetoothController.getBtAdapter();
                }else{
                    btAdapter = aBluetoothController.getBtAdapter();
                }
                aBluetoothController = new BluetoothController();
                aBluetoothController.setBtAdapter(btAdapter);
                aBluetoothController.setHandler(handler);
                aBluetoothController.setProcessType("getlist");
                aBluetoothController.start();
                System.out.println("wait for complete started.....");
            }else{
                Toast.makeText(this, "No bluetooth devices...", Toast.LENGTH_LONG).show();
            }
        }catch (Exception ee){
            ee.printStackTrace();
        }

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

    public void openBluetoothList(View view){
        try {

            if (containsDevicesConnected){
                mBluAdapter.setAdapter(mArrayAdapter);
                mBluAdapter.setVisibility(view.VISIBLE);
                aMainLayout.setVisibility(view.INVISIBLE);
                aBluListLayout.setVisibility(view.VISIBLE);
            }else{
                Toast.makeText(this, "No paired bluetooth devices...", Toast.LENGTH_LONG).show();
                aMainLayout.setVisibility(view.VISIBLE);
                aBluListLayout.setVisibility(view.INVISIBLE);
            }
        }catch (Exception ee){
            ee.printStackTrace();
        }
    }

}
