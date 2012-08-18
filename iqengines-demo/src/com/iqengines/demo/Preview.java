
package com.iqengines.demo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
   
	private boolean DEBUG =true;
    
	private static final String TAG = Preview.class.getSimpleName();
    
    private static long AUTO_FOCUS_INTERVAL = 1500;
    
    public static final int CMD_SCAN = 1; 
    
    public static final int CMD_IMAGE_COPIED = 2;
    
    private SurfaceHolder mHolder;
    
    private Handler mHandler;
    
    Camera mCamera;

    Size mPreviewSize;
    
    ScanningHandler mPreviewHandler;
    
    Thread mPreviewThread;
    
    AtomicBoolean mPreviewThreadRun = new AtomicBoolean(false);
    
    private int angle;
    
    private Thread mAutofocusThread;
    
    private Boolean mAutoFocus;
    
    private byte[] mLastFrameCopy;

    private FrameReceiver mFrameReceiver;

    private Size mFramePreviewSize;
    


    public interface FrameReceiver {
        public void onFrameReceived(byte[] frameBuffer, Size framePreviewSize);
    }

    public Preview(Context context) {
        this(context, null);		
    }

    public Preview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new Handler();
        mHolder = getHolder();
        mHolder.addCallback(this);
        // this is needed for old android version
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private Size getOptimalSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double) w / h;
        if (DEBUG) Log.d(TAG, "target view size: " + w + "x" + h + ", target ratio=" + targetRatio);
        
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;
        int targetWidth = w;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            boolean fitToView = size.width <= w && size.height <= h;
            if (DEBUG) Log.d(TAG, "Supported preview size: " + size.width + "x" + size.height + ", ratio="
                    + ratio + ", fitToView=" + fitToView);
            if (!fitToView) {
                // we can not use preview size bigger than surface dimensions
                // skipping
                continue;
            }
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }

            double hypot = Math.hypot(size.height - targetHeight, size.width - targetWidth);
            if (hypot < minDiff) {
                optimalSize = size;
                minDiff = hypot;
            }
        }

        if (optimalSize == null) {
        	if (DEBUG) Log.d(TAG,
                    "Cannot find preview that matchs the aspect ratio, ignore the aspect ratio requirement");

            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (size.width > w || size.height > h) {
                    // we can not use preview size bigger than surface
                    // dimensions
                    continue;
                }

                double hypot = Math.hypot(size.height - targetHeight, size.width - targetWidth);
                if (hypot < minDiff) {
                    optimalSize = size;
                    minDiff = hypot;
                }
            }
        }

        if (optimalSize == null) {
            throw new RuntimeException("Unable to determine optimal preview size");
        }
        if (DEBUG) Log.d(TAG, "optimalSize.width=" + optimalSize.width + ", optimalSize.height="
                + optimalSize.height);

        return optimalSize;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    	
    	if (mCamera == null) {
    		if (DEBUG) Log.e(TAG, "mCamera == null !");        
            return;
        }
        Camera.Parameters params = mCamera.getParameters();
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();

        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                angle = 90;
                break;
            case Surface.ROTATION_90:
                angle = 0;
                break;
            case Surface.ROTATION_180:
                angle = 270;
                break;
            case Surface.ROTATION_270:
                angle = 180;
                break;
            default:
                throw new AssertionError("Wrong surface rotation value");
        }
        setDisplayOrientation(params, angle);
        
        if (mPreviewSize == null) {
            // h and w get inverted on purpose
            mPreviewSize = getOptimalSize(params.getSupportedPreviewSizes(), width > height ? width
                    : height, width > height ? height : width);
        }
        	
        params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        
        mCamera.setParameters(params);
        
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.e(TAG, "Can't set preview display", e);
        }
        
        startPreview();
        
		mFramePreviewSize = mCamera.getParameters().getPreviewSize();
	    
	    int bitsPerPixel = 12;
        mLastFrameCopy = new byte[mFramePreviewSize.height*mFramePreviewSize.width * bitsPerPixel / 8];
        PreviewCallbackScan();
        mPreviewThreadRun.set(true);
	    scan();
    }
    
    class AutoFocusRunnable implements Runnable {
    	
    	@Override
    	public void run() {
        	if (mAutoFocus){
        		if (mCamera != null) {
        			try {
        				mCamera.autoFocus(new AutoFocusCallback() {
        					@Override
        					public void onAutoFocus(boolean success, Camera camera) {
        						mHandler.postDelayed(AutoFocusRunnable.this, AUTO_FOCUS_INTERVAL);
        					}
        				});
        			
                	} catch (Exception e) {
                		Log.w(TAG, "Unable to auto-focus", e);
                    	mHandler.postDelayed(AutoFocusRunnable.this, AUTO_FOCUS_INTERVAL);
                	}
            	}
        	}
        }
		
    };
    
    void startAutofocus() {
        mAutoFocus = true;
        mAutofocusThread = new Thread(new AutoFocusRunnable(),"Autofocus Thread");
        mAutofocusThread.start();
    }
    
    void stopPreview() {
    	mAutoFocus = false;
    	if (mCamera!=null) mCamera.cancelAutoFocus();
    	mAutofocusThread=null;
    	if (mCamera!=null) mCamera.stopPreview();
    }
    
    void startPreview(){
    	if (mCamera!=null){
    		mCamera.startPreview();
    		startAutofocus();
    	}
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	if(mCamera==null){
    		try {
    			mCamera = Camera.open();
    		} catch (RuntimeException e) {
      	      Toast.makeText(getContext(),
      	    		  "Unable to connect to camera. " + "Perhaps it's being used by another app.",
     	               Toast.LENGTH_LONG).show();
    		}
    	}
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	
        if (mCamera != null) {
            synchronized (this) {
            	mCamera.setPreviewCallback(null);           	
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }
    }

    private void setDisplayOrientation(Camera.Parameters params, int angle) {
        try {
            Method method = mCamera.getClass().getMethod("setDisplayOrientation", new Class[] {
                int.class
            });
            if (method != null)
                method.invoke(mCamera, new Object[] {
                    angle
                });
        } catch (Exception e) {
        	if (DEBUG) Log.d(TAG, "Can't call Camera.setDisplayOrientation on this device, trying another way");
            if (angle == 90 || angle == 270) params.set("orientation", "portrait");
            else if (angle == 0 || angle == 180)  params.set("orientation", "landscape");
        }
        params.setRotation(angle);
    }

    
    public class PreviewThread extends Thread{
    	
    	public PreviewThread(String string){
    		super(string);
    	}

    	@Override
    	public void run() {
    		Looper.prepare();
    		Thread.currentThread().setPriority(MIN_PRIORITY);
    		mPreviewHandler = new ScanningHandler();
    		Looper.loop();
    	};
    }

    public void setFrameReceiver(FrameReceiver receiver) {
        if (DEBUG) Log.d(TAG,"set Frame Receiver");
        mFrameReceiver = receiver;
        
    }

    private Object mLastFrameCopyLock = new Object();
    
    public void copyLastFrame(byte[] frame) {
    	
    	synchronized(mLastFrameCopyLock){
    		if (DEBUG) Log.d(TAG,"copying frame");
	        System.arraycopy(frame, 0, mLastFrameCopy, 0, frame.length);
    	}
    	mPreviewHandler.obtainMessage(CMD_IMAGE_COPIED).sendToTarget();
    }
    
    public byte[] getLastFrameCopy() {
    	
    	synchronized(mLastFrameCopyLock){
    		return mLastFrameCopy;
    	}
    } 
    
    public void scan(){
    	if (DEBUG) Log.d(TAG,"<<<<<<<<<<<<<< scan called >>>>>>>>>>>>>>>>");
    	removeAllMessages();
    	mPreviewHandler.obtainMessage(CMD_SCAN).sendToTarget();
    }
    
    /**
     * @return the default angle of the camera
     */
    
    public int getAngle(){
    	return angle;
    }
    
     public void PreviewCallbackScan(){
    	
    	mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {

                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                        	if (data == null) {
                                return;
                            }
                            copyLastFrame(data);
                        }
    	});	
    }
     
     public class ScanningHandler extends Handler{
    	 
    	 @Override
    	 public void handleMessage(Message message){
    		 switch(message.what){
    		 
    		 	case(CMD_SCAN):
    		 		if (mPreviewThreadRun.get()){
    		 			mCamera.addCallbackBuffer(mLastFrameCopy);
    		 			break;
    		 		}
    		 	break;
    		 
    		 	case(CMD_IMAGE_COPIED):
    		 		if (mPreviewThreadRun.get()){
    		 			if (DEBUG)  Log.d(TAG,"frame copied");
    		 			mFrameReceiver.onFrameReceived(getLastFrameCopy(), mFramePreviewSize);
    		 			break;
    		 		}
    		 	break;
    		}
    	}
    	 
     }
     
     public void removeAllMessages(){
    	 mPreviewHandler.removeMessages(CMD_SCAN);
     	 mPreviewHandler.removeMessages(CMD_IMAGE_COPIED);
     }

}
