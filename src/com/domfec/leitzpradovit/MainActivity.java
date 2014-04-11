package com.domfec.leitzpradovit;

import java.io.IOException;
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
import com.domfec.leitzpradovit.R;

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

	// //////////////
	private Mat mIntermediateMat;
	private double previousValue;
	private double currentValue;
	private Mat mGrayMat;
	private boolean isFocused=true;
	private boolean isInitiated=false;
	private final Object signal = new Object();
	
	
	////////////////
	private int mRatioX;
	private int mRatioY;
	int mLeft;
	int mTop;
	int mLeft2;
	int mTop2;
	

	////////////////
	Button Connect;
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private static String address = "98:D3:31:B0:9C:87";
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// private InputStream inStream = null;
	private boolean btonoff = false;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			Log.i(TAG, "OpenCV loading?");
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();

			}
				break;
			default: {
				Log.i(TAG, "OpenCV not loaded");
				super.onManagerConnected(status);
			}
				break;
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
		Connect = (Button) findViewById(R.id.zoomin);
		Connect.setOnClickListener(new ZoomInOnClickListener());
		Connect = (Button) findViewById(R.id.zoomout);
		Connect.setOnClickListener(new ZoomOutOnClickListener());
		Connect = (Button) findViewById(R.id.up);
		Connect.setOnClickListener(new UpOnClickListener());
		Connect = (Button) findViewById(R.id.down);
		Connect.setOnClickListener(new DownOnClickListener());
		Connect = (Button) findViewById(R.id.left);
		Connect.setOnClickListener(new LeftOnClickListener());
		Connect = (Button) findViewById(R.id.focus);
		Connect.setOnClickListener(new FocusOnClickListener());
		
		
		
		
		CheckBt();

		// BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

	}

	private void CheckBt() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(getApplicationContext(), "Bluetooth Disabled !",
					Toast.LENGTH_SHORT).show();
		}

		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(), "Bluetooth null !",
					Toast.LENGTH_SHORT).show();
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
			btonoff = true;
			Log.d(TAG, "Connection made.");
			Toast.makeText(getApplicationContext(), "Connection Created!",
					Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				Log.d(TAG, "Unable to end the connection");
			}
			Log.d(TAG, "Socket creation failed");
		}
		// beginListenForData();
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
	
	
	public class ZoomInOnClickListener implements OnClickListener{
		public void onClick(View v){
			mRatioX=mRatioX-1;
			mRatioY=mRatioY-1;
			
		}
	}
	
	public class ZoomOutOnClickListener implements OnClickListener{
		public void onClick(View v){
			mRatioX=mRatioX+1;
			mRatioY=mRatioY+1;
		}
	}

	public class UpOnClickListener implements OnClickListener{
		public void onClick(View v){
          mTop=mTop+1;
		}
	}
	public class DownOnClickListener implements OnClickListener{
		public void onClick(View v){
          mTop=mTop-1;
		}
	}
	
	public class LeftOnClickListener implements OnClickListener{
		public void onClick(View v){
			mLeft=mLeft+1;
		}
	}
	
    public void setUnpaused() {
        isFocused = false;
        synchronized(signal) {
       	 signal.notify();
       	 }
    }
	
	public class FocusOnClickListener implements OnClickListener{
		public void onClick(View v){
			//mLeft=mLeft-1;
		    isFocused=false;
		    setUnpaused();

		}
	}
	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this,
				mLoaderCallback);

	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {

		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this,
				mLoaderCallback)) {
			Log.e(TAG, "failed");
		}
		mGrayMat = new Mat(height, width, CvType.CV_8UC1);
		mIntermediateMat = new Mat();
		previousValue = 0;
		mRatioX=5;
		mRatioY=8;
		mTop=8;
		mLeft=8;
		Motor.start();

	}

	public void onCameraViewStopped() {
		mGrayMat.release();
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mGrayMat = inputFrame.rgba();
		Mat gray = inputFrame.gray();
		Mat InterMat = new Mat();
		Size sizeRgba = mGrayMat.size();
		Mat rgbaInnerWindow;

		// Later will define these param in global, so it can be accessed and
		// modified by the view
		int rows = (int) sizeRgba.height;
		int cols = (int) sizeRgba.width;
		int left = cols /mLeft;
		int top = rows /mTop;
		int width = cols * mRatioX/mRatioY;
		int height = rows * mRatioX/mRatioY;

		// Crop image
		Mat grayInnerWindow = gray
				.submat(top, top + height, left, left + width);
		rgbaInnerWindow = mGrayMat
				.submat(top, top + height, left, left + width);
		// Run Sobel filter
		Imgproc.Sobel(grayInnerWindow, InterMat, CvType.CV_8U, 1, 1);
		// Scale
		Core.convertScaleAbs(InterMat, InterMat, 10, 0);
		// Swap cropped area
		Imgproc.cvtColor(InterMat, rgbaInnerWindow, Imgproc.COLOR_GRAY2BGRA, 4);
		grayInnerWindow.release();
		rgbaInnerWindow.release();
		mIntermediateMat = InterMat;
		
		Size InnersizeRgba = mIntermediateMat.size();
		int Innerrows = (int) InnersizeRgba.height;
		int Innercols = (int) InnersizeRgba.width;
		byte buff[] = new byte[Innercols * Innerrows];
		mIntermediateMat.get(0, 0, buff);
//		// tol is the sum of gradient value for current frame, it
//		// will be reset every time before store in
		double tol = 0;
		for (int i = 0; i < Innercols * Innerrows; i++) {
			tol = tol + buff[i];
		}
		
		currentValue=tol;
		
		return mGrayMat;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// mOpenCvCameraView.setOrientation();
		List<String> mFocusList = new LinkedList<String>();
		int idx = 0;

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
		while (FocusItr.hasNext()) {
			// add the element to the mDetectorMenu submenu
			String element = FocusItr.next();
			mFocusListItems[idx] = mFocusMenu.add(2, idx, Menu.NONE, element);
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
		while (FlashItr.hasNext()) {
			// add the element to the mDetectorMenu submenu
			String element = FlashItr.next();
			mFlashListItems[idx] = mFlashMenu.add(3, idx, Menu.NONE, element);
			idx++;
		}

		mResolutionMenu = menu.addSubMenu("Resolution");
		mResolutionList = mOpenCvCameraView.getResolutionList();
		mResolutionMenuItems = new MenuItem[mResolutionList.size()];

		ListIterator<Camera.Size> resolutionItr = mResolutionList
				.listIterator();
		idx = 0;
		while (resolutionItr.hasNext()) {
			Camera.Size element = resolutionItr.next();
			mResolutionMenuItems[idx] = mResolutionMenu.add(1, idx, Menu.NONE,
					Integer.valueOf((int) element.width).toString() + "x"
							+ Integer.valueOf((int) element.height).toString());
			idx++;
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.e(TAG, "called onOptionsItemSelected; selected item: " + item);
		if (item.getGroupId() == 1) {

			int id = item.getItemId();
			Camera.Size resolution = mResolutionList.get(id);
			mOpenCvCameraView.setResolution(resolution);
			resolution = mOpenCvCameraView.getResolution();
			Log.e("test", "test");
			String caption = Integer.valueOf((int) resolution.width).toString()
					+ "x" + Integer.valueOf((int) resolution.height).toString();
			Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
		}

		else if (item.getGroupId() == 2) {

			int focusType = item.getItemId();
			// String caption = "Focus Mode: "+ (String)item.getTitle();
			// Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();

			mOpenCvCameraView.setFocusMode(this, focusType);
		} else if (item.getGroupId() == 3) {

			int flashType = item.getItemId();
			// String caption = "Flash Mode: "+ (String)item.getTitle();
			// Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
			mOpenCvCameraView.setFlashMode(this, flashType);
		}

		return true;
	}

	Thread Motor = new Thread() {
		@Override
		public void run() {
			
			//boolean isFocused=false;
			
			try {
				while (true) {
					
//	                if (isFocused) {
//	                	continue;
//	                }
//					
			         while(isFocused) { // pause point 1
			             synchronized(signal){
			             signal.wait();
			             }
			          }
			         
			         
					// Check if just start, if so, give motor initial speed and set flag true
					if (!isInitiated){
						if (btonoff == true) {
							writeData("1");
						}
						isInitiated=true;
					}
			        // Record current value as past value
					previousValue=currentValue;
					// Keep motor running
					sleep(500);
                    // Read current and compare with past value
					double diff = (currentValue - previousValue) / 1000;

					Log.i("the total value is", String.valueOf(diff));

					String command;
					

					if (diff > 20) // Test if this is too sensitive, if so, add
									// a threshold value to the difference
					{
						command = "1";
						isFocused=false;
						Log.i("Motor direction", "Left");
					} else if (diff < -20) {
						command = "2";
						isFocused=false;
						Log.i("Motor direction", "Right");
					} else {
						command = "3";
						isFocused=true;
						isInitiated=false;
						Log.i("Motor direction", "Stop");
					}

					if (btonoff == true) {
						writeData(command);
					}
				

				}
			//	}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


	};
}