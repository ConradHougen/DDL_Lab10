package com.example.lab10servicedemo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class ServiceHelper {
    /** EEG service */
	private Context mContext;
	private String mBTAdress;
	private String mUsername;
    private boolean mIsBound = false;
	private Messenger mService = null;		// Messenger for communicating with the service
    private boolean mIsGrit = false;
    private boolean mIsLeftGrit = false;
    private boolean mIsRightGrit = false;
    private boolean mIsSmile = false;
    private boolean mIsLeftBlink = false;
    private boolean mIsRightBlink = false;
    
    // Bluetooth state returned from Service
    static final int BTSTATE_NONE = 0;       // we're doing nothing
    static final int BTSTATE_LISTEN = 1;     // now listening for incoming connections
    static final int BTSTATE_CONNECTING = 2; // now initiating an outgoing connection
    static final int BTSTATE_CONNECTED = 3;  // now connected to a remote device
    static final int BTSTATE_ERROR = 4;  	 // error
    
    // Service messages
    public static final int MSG_REGISTER_CLIENT = 2;		// Pass parameters, initiate BT connection and notification
    public static final int MSG_SEND = 10;					// Write to Bluetooth device
    public static final int MSG_SEND_RESULT = 11;			// Writing result
    public static final int MSG_BT_STATUS_CHANGE = 6;		// Feedback for Bluetooth status
    public static final int MSG_ERR = 28;					// Error message from EEG service
    public static final int MSG_RAW_DATA = 29;		
    public static final int MSG_DATA_NOISE = 30;		// Signal quality
    public static final int MSG_GRIT_DATA = 40;			
    public static final int MSG_SMILE_DATA = 41;			
    public static final int MSG_LEFT_BLINK_DATA = 42;	
    public static final int MSG_RIGHT_BLINK_DATA = 43;		
    public static final int MSG_LEFT_GRIT_DATA = 44;		
    public static final int MSG_RIGHT_GRIT_DATA = 45;		
	public static final int MSG_TRAIN = 31;
	public static final int MSG_TRAIN_RESULT = 32;
    
    /** Bundle keys for passing parameters to service in the beginning */
    private final String KEY_BT_ADDR = "bt_addr";
    private final String KEY_FILENAME = "filename";
    private final String KEY_USERNAME = "username";
    private String mFilename;
    private EEGHandler mHandler;
    
    public static interface EEGHandler {
    	public void onSmile(boolean status, int index);
    	public void onGrit(boolean status, int index);
    	public void onLeftGrit(boolean status, int index);
    	public void onRightGrit(boolean status, int index);
    	public void onLeftBlink(boolean status, int index);
    	public void onRightBlink(boolean status, int index);
    	public void onDataNoise(float[] quality);
    	public void onRawData(float[][] data, int index);
    	public void onTraining(int result);
    	
    	public void onBTConnected();
    	public void onBTConnecting();
    	public void onBTDisconnected();
    	public void onBTError();
    	public void onWriteResult(int r);
    }
    
    
	public ServiceHelper(Context c, EEGHandler handler, String username, String btAddress) {
		mContext = c;
		mHandler = handler;
		mBTAdress = btAddress;
		mUsername = username;
	}
	
	public void startService() {
        // Bind to the service
        mContext.bindService(new Intent("edu.colorado.eegpoc.GET_EEG"), mConnection,
            Context.BIND_AUTO_CREATE);
	}
	
	public void stopService() {
    	// Unbind from the service
        if (mIsBound) {
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
	}
	
	public void write(byte[] msg) {
        Message m = Message.obtain(null, MSG_SEND);
        m.replyTo = mDataServiceMessenger;
        Bundle bundle = new Bundle();
        // Bluetooth device to connect
        bundle.putByteArray("write", msg);
        m.setData(bundle);
        try {
			mService.send(m);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void minitrain(float[] data1, float[] data2) {
        Message m = Message.obtain(null, MSG_TRAIN);
        m.replyTo = mDataServiceMessenger;
        Bundle bundle = new Bundle();
        for (int i = 0; i < 2; i++) {
        	bundle.putFloatArray("chan1", data1);
        	bundle.putFloatArray("chan2", data2);
        }
        m.setData(bundle);
        try {
			mService.send(m);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mIsBound = true;
            // Send out handler to service to get its feedback
            try {
                Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
                msg.replyTo = mDataServiceMessenger;
                Bundle bundle = new Bundle();
                // Bluetooth device to connect
                bundle.putString(KEY_BT_ADDR, mBTAdress);
                // Data file name. Only store data when user is login
                if (mUsername != null) {
	    			Calendar calendar = Calendar.getInstance();
	    			Date now = calendar.getTime();
	    			Timestamp currTime = new Timestamp(now.getTime()); 
	    			String fileTimestamp = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(currTime);
	    			String[] btAddrTokens = mBTAdress.split(":");
	    			int length = btAddrTokens.length;
	    			mFilename = fileTimestamp + "-" + mUsername + "-" + mContext.getClass().getSimpleName() + 
	    					"-" + btAddrTokens[length-2] + btAddrTokens[length-1];
	    			bundle.putString(KEY_FILENAME, mFilename);
	    			bundle.putString(KEY_USERNAME, mUsername);
                }
                //change frequency
                bundle.putInt("chrome",9);
                msg.setData(bundle);
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mIsBound = false;
        }
    };
    
    /**
     * All the message send from service are handled here.
     */
    private Handler mDataServiceHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
    		int index;
            switch (msg.what) {
            case MSG_ERR:
            	String errmsg = msg.getData().getString("error");
            	Toast.makeText(mContext, "Error message from engine: " + errmsg, Toast.LENGTH_LONG).show();
            	break;
            case MSG_BT_STATUS_CHANGE:
        		switch (msg.arg1) {
        		case BTSTATE_CONNECTED:
        			mHandler.onBTConnected();
        			break;
        		case BTSTATE_CONNECTING:
        			mHandler.onBTConnecting();
        			break;
        		case BTSTATE_NONE:
        			mHandler.onBTDisconnected();
        			break;
        		case BTSTATE_ERROR:
        			stopService();
        			mHandler.onBTError();
        			break;
        		}
				break;
            case MSG_TRAIN_RESULT:
            	mHandler.onTraining(msg.arg1);
				break;
            case MSG_SEND_RESULT:
            	mHandler.onWriteResult(msg.arg1);
				break;
            case MSG_GRIT_DATA:
            	mIsGrit = msg.getData().getBoolean("grit");
            	index = msg.getData().getInt("index");
            	mHandler.onGrit(mIsGrit, index);
            	break;
            case MSG_SMILE_DATA:
            	mIsSmile = msg.getData().getBoolean("smile");
            	index = msg.getData().getInt("index");
            	mHandler.onSmile(mIsSmile, index);
            	break;
            case MSG_LEFT_BLINK_DATA:
            	mIsLeftBlink = msg.getData().getBoolean("leftblink");
            	index = msg.getData().getInt("index");
            	mHandler.onLeftBlink(mIsLeftBlink, index);
            	break;
            case MSG_RIGHT_BLINK_DATA:
            	mIsRightBlink = msg.getData().getBoolean("rightblink");
            	index = msg.getData().getInt("index");
            	mHandler.onRightBlink(mIsRightBlink, index);
            	break;
            case MSG_LEFT_GRIT_DATA:
            	mIsLeftGrit = msg.getData().getBoolean("lgrit");
            	index = msg.getData().getInt("index");
            	mHandler.onRightGrit(mIsLeftGrit, index);
            	break;
            case MSG_RIGHT_GRIT_DATA:
            	mIsRightGrit = msg.getData().getBoolean("rgrit");
            	index = msg.getData().getInt("index");
            	mHandler.onLeftGrit(mIsRightGrit, index);
            	break;
            case MSG_RAW_DATA:
            	Bundle b1 = msg.getData();
            	int size = b1.getInt("size");
            	float[][] rawData = new float[size][];
            	for (int i = 0; i < size; i++) {
            		rawData[i] = b1.getFloatArray("chan"+i);
            	}
            	index = msg.getData().getInt("index");
            	mHandler.onRawData(rawData, index);
            case MSG_DATA_NOISE:
            	float[] noise = msg.getData().getFloatArray("quality");
            	if (noise != null && noise[0] != -1) mHandler.onDataNoise(noise);
				break;
            default:
                super.handleMessage(msg);
            }
        }
    };
    
    public String getFilename() {
    	return mFilename;
    }
    /**
     * Target we publish for service to send messages back here.
     */
    final Messenger mDataServiceMessenger = new Messenger(mDataServiceHandler);

}