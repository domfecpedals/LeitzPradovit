package com.example.focusassist;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;



public class MainActivity extends Activity implements CvCameraViewListener2 {
private static final String TAG = "FocusAssistProject";

private JavaCamResView mOpenCvCameraView;
private List<Camera.Size> mResolutionList;

private MenuItem[] mResolutionMenuItems;
private MenuItem[] mFocusListItems;
private MenuItem[] mFlashListItems;

private SubMenu mResolutionMenu;
private SubMenu mFocusMenu;
private SubMenu mFlashMenu;
private Mat mIntermediateMat;
private double previousValue;


Button Connect;
private BluetoothAdapter mBluetoothAdapter = null;
private BluetoothSocket btSocket = null;
private OutputStream outStream = null;
private static String address = "98:D3:31:B0:9C:87";
private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
private InputStream inStream = null;
private boolean btonoff=false;

private Mat mGrayMat;

private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
    @Override
    public void onManagerConnected(int status) {
        switch (status) {
            case LoaderCallbackInterface.SUCCESS:
            {
                Log.i(TAG, "OpenCV loaded successfully");                    
                mOpenCvCameraView.enableView();                  

            } break;
            default:
            {
                super.onManagerConnected(status);
            } break;
        }
    }
};

public MainActivity() {
    Log.i(TAG, "Instantiated new " + this.getClass());
}

@Override
public void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "called onCreate");
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_main);

    // Link customized camera
    mOpenCvCameraView = (JavaCamResView) findViewById(R.id.test_view);
    mOpenCvCameraView.setCvCameraViewListener(this); 
    
    Connect = (Button) findViewById(R.id.connect);
    Connect.setOnClickListener(new ConnectOnClickListener());
    
    
    CheckBt();
    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
   

}

private void CheckBt() {
	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	if (!mBluetoothAdapter.isEnabled()) {
		Toast.makeText(getApplicationContext(), "Bluetooth Disabled !",
				Toast.LENGTH_SHORT).show();
	}

	if (mBluetoothAdapter == null) {
		Toast.makeText(getApplicationContext(),
				"Bluetooth null !", Toast.LENGTH_SHORT)
				.show();
	}
}
public void Connect() {
	Log.d(TAG, address);
	BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
	Log.d(TAG, "Connecting to ... " + device);
	mBluetoothAdapter.cancelDiscovery();
	try {
		btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
		btSocket.connect();
		btonoff=true;
		Log.d(TAG, "Connection made.");
	} catch (IOException e) {
		try {
			btSocket.close();
		} catch (IOException e2) {
			Log.d(TAG, "Unable to end the connection");
		}
		Log.d(TAG, "Socket creation failed");
	}
	
//	beginListenForData();
}


private void writeData(String data) {
	try {
		outStream = btSocket.getOutputStream();
	} catch (IOException e) {
		Log.d(TAG, "Bug BEFORE Sending stuff", e);
	}

	String message = data;
	byte[] msgBuffer = message.getBytes();

	try {
		outStream.write(msgBuffer);
	} catch (IOException e) {
		Log.d(TAG, "Bug while sending stuff", e);
	}
}


public class ConnectOnClickListener implements OnClickListener {
	public void onClick(View v) {
		
		Connect();
		Log.i(TAG, "Connect Button pressed");
		
	}
}

@Override
public void onPause()
{
    super.onPause();
    if (mOpenCvCameraView != null)
        mOpenCvCameraView.disableView();
}

@Override
public void onResume()
{
    super.onResume();
    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback);


}

public void onDestroy() {
    super.onDestroy();
    if (mOpenCvCameraView != null)
        mOpenCvCameraView.disableView();
}

public void onCameraViewStarted(int width, int height) {        

    mGrayMat = new Mat(height, width, CvType.CV_8UC1);
    
    mIntermediateMat = new Mat();
    
    previousValue=0;
    
    boolean bo=mIntermediateMat.empty();
    
    Log.i("", "halkshflakjflajfsl" + bo);
    Motor.start();

}

public void onCameraViewStopped() { 
    mGrayMat.release();
}

public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
	  mGrayMat=inputFrame.rgba();
	  Mat gray = inputFrame.gray();
      Mat InterMat=new Mat();
      Size sizeRgba = mGrayMat.size();

      Mat rgbaInnerWindow;

    int rows = (int) sizeRgba.height;
    int cols = (int) sizeRgba.width;
    int left = cols / 4;
    int top = rows / 8;
    int width = cols * 1 / 2;
    int height = rows * 3 / 4;
    
    
    
    // Crop image 
    Mat grayInnerWindow = gray.submat(top, top + height, left, left + width);
    rgbaInnerWindow = mGrayMat.submat(top, top + height, left, left + width);
    // Run Sobel filter
    Imgproc.Sobel(grayInnerWindow, InterMat, CvType.CV_8U, 1, 1);
    // Scale
    Core.convertScaleAbs(InterMat, InterMat, 10, 0);
    // Swap cropped area
    Imgproc.cvtColor(InterMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
    
    
    
//    // Compute sum of all gradient values in area
//    byte buff[] = new byte[cols*rows];
//    InterMat.get(0, 0, buff);
//    // tol is the sum of gradient value for current frame, it will be reset every time before store in
//    double tol=0;
//    for(int i = 0; i < cols*rows; i++)
//    {
//       tol=tol+buff[i] ;
//    }
//    Log.i("the total value is", String.valueOf(tol));
//    
//    
//    if (tol>previousValue) // Test if this is too sensitive, if so, add a threshold value to the difference
//    {
//    	Log.i("Motor direction", "Left");
//    }
//    else
//    {
//    	Log.i("Motor direction", "Right");
//    }
 
//     need a double to store sum of previous frame, so they can be compared
//    previousValue=tol;
    
    grayInnerWindow.release();
    rgbaInnerWindow.release();
    
    mIntermediateMat=InterMat;
    
    return mGrayMat;
}

@Override
public boolean onCreateOptionsMenu(Menu menu) {
	
    mOpenCvCameraView.setOrientation();
    List<String> mFocusList = new LinkedList<String>();
    int idx =0;

    mFocusMenu = menu.addSubMenu("Focus");

    mFocusList.add("Auto");
    mFocusList.add("Continuous Video");
    mFocusList.add("EDOF");
    mFocusList.add("Fixed");
    mFocusList.add("Infinity");
    mFocusList.add("Makro");
    mFocusList.add("Continuous Picture");

    mFocusListItems = new MenuItem[mFocusList.size()];

    ListIterator<String> FocusItr = mFocusList.listIterator();
    while(FocusItr.hasNext()){
        // add the element to the mDetectorMenu submenu
        String element = FocusItr.next();
        mFocusListItems[idx] = mFocusMenu.add(2,idx,Menu.NONE,element);
        idx++;
    }



    List<String> mFlashList = new LinkedList<String>();
    idx = 0;

    mFlashMenu = menu.addSubMenu("Flash");

    mFlashList.add("Auto");
    mFlashList.add("Off");
    mFlashList.add("On");
    mFlashList.add("Red-Eye");
    mFlashList.add("Torch");

    mFlashListItems = new MenuItem[mFlashList.size()];

    ListIterator<String> FlashItr = mFlashList.listIterator();
    while(FlashItr.hasNext()){
        // add the element to the mDetectorMenu submenu
        String element = FlashItr.next();
        mFlashListItems[idx] = mFlashMenu.add(3,idx,Menu.NONE,element);
        idx++;
    }



    mResolutionMenu = menu.addSubMenu("Resolution");
    mResolutionList = mOpenCvCameraView.getResolutionList();
    mResolutionMenuItems = new MenuItem[mResolutionList.size()];

    ListIterator<Camera.Size> resolutionItr = mResolutionList.listIterator();
    idx = 0;
    while(resolutionItr.hasNext()) {
        Camera.Size element = resolutionItr.next();
        mResolutionMenuItems[idx] = mResolutionMenu.add(1, idx, Menu.NONE,
                Integer.valueOf((int) element.width).toString() + "x" + Integer.valueOf((int) element.height).toString());
        idx++;
     }

    return true;
}

public boolean onOptionsItemSelected(MenuItem item) {
    Log.e(TAG, "called onOptionsItemSelected; selected item: " + item);
   if (item.getGroupId() == 1)
    {
	   
        int id = item.getItemId();
        Camera.Size resolution = mResolutionList.get(id);
        mOpenCvCameraView.setResolution(resolution);
        resolution = mOpenCvCameraView.getResolution();
        Log.e("test","test");
        String caption = Integer.valueOf((int) resolution.width).toString() + "x" + Integer.valueOf((int) resolution.height).toString();
        Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
    } 
   
   
    else if (item.getGroupId()==2){

       int focusType = item.getItemId();
       //String caption = "Focus Mode: "+ (String)item.getTitle();
       //Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();

       mOpenCvCameraView.setFocusMode(this, focusType);
    }
    else if (item.getGroupId()==3){

       int flashType = item.getItemId();
       //String caption = "Flash Mode: "+ (String)item.getTitle();
       //Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
       mOpenCvCameraView.setFlashMode(this, flashType);
    }

    return true;
}  


Thread Motor = new Thread()
{
    @Override
    public void run() {
        try {
            while(true) {
                sleep(100);
                Log.i(TAG,"this is a multithread");
                Size sizeRgba = mIntermediateMat.size();
                int rows = (int) sizeRgba.height;
                int cols = (int) sizeRgba.width;
                byte buff[] = new byte[cols*rows];
                mIntermediateMat.get(0, 0, buff);
                // tol is the sum of gradient value for current frame, it will be reset every time before store in
                double tol=0;
                for(int i = 0; i < cols*rows; i++)
                {
                   tol=tol+buff[i] ;
                }
            //    Log.i("the total value is", String.valueOf(tol));
                
              double diff=(tol-previousValue)/1000; 
              
              Log.i("the total value is", String.valueOf(diff));
              
              String command;
              
              if (diff>30) // Test if this is too sensitive, if so, add a threshold value to the difference
              {
            	  command="1";
              	Log.i("Motor direction", "Left");
              }
              else if(diff<-30)
              {
            	  command="2";
              	Log.i("Motor direction", "Right");
              }
              else
              {
            	  command="3";
            	  Log.i("Motor direction","Stop");
              }
              
              previousValue=tol;  //Update previous value
              
              if (btonoff==true){
              writeData(command);
              }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
};
}