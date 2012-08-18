package com.iqengines.sdk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera.Size;
import android.net.wifi.WifiManager;
import android.util.Log;



@SuppressLint("NewApi")
public class Utils {
    private static String TAG = Utils.class.getName();
    private static boolean DEBUG = true;

    
    /**
     * Method used to crop a Bitmap picture.
     * 
     * @param origBmp 
     * 		  The {@link Bitmap} image Image to be cropped.
     * 
     * @param targetSize 
     * 		  An {@link Integer} The size required. 
     * 
     * @return A {@link Bitmap} cropped image.
     */
    
    
    public static Bitmap cropBitmap(Bitmap origBmp, int targetSize) {
        final int w = origBmp.getWidth();
        final int h = origBmp.getHeight();

        float scale = ((float) targetSize) / (w < h ? w : h);

        Matrix matrix = new Matrix();
        if (w > h) {
            matrix.postRotate(90);
        }
        matrix.postScale(scale, scale);

        if (DEBUG)
            Log.d(TAG, "origBmp: width=" + w + ", height=" + h);

        int pad = (int) ((float) (w > h ? w : h) - ((float) targetSize) / scale) / 2;
        if (DEBUG)
            Log.d(TAG, "pad=" + pad);

        int new_w = w - (w > h ? 2 * pad : 0);
        int new_h = h - (w > h ? 0 : 2 * pad);
        if (DEBUG)
            Log.d(TAG, "new_w=" + new_w + ", new_h=" + new_h);

        Bitmap thumb = Bitmap.createBitmap(origBmp, w > h ? pad : 0, w > h ? 0 : pad, new_w, new_h,
                matrix, true);

        if (DEBUG) {
            Log.d(TAG, "tumb dim:" + thumb.getWidth() + "x" + thumb.getHeight());
        }

        return thumb;
    }
    
    /**
     * Method used to crop a YUV picture.
     * 
     * @param origBmp 
     * 		  The {@link YuvImage} to be cropped.
     * 
     * @param targetSize 
     * 		  An {@link Integer} The size required. 
     * 
     * @return A {@link File} representing the cropped YUV compressed to JPEG.
     */
   
    
    
    public static File cropYuv(YuvImage origYuv, int targetSize, Context ctx) {
    	
    	int w = origYuv.getWidth();
    	int h = origYuv.getHeight();
    	
    	int left = (int)(targetSize >= w ? 0 : (float)( (w-targetSize)/2 ) );
    	int right = (int)(targetSize >= w ? w : targetSize + (float)( (w-targetSize)/2 ) );
    	int top = (int)(targetSize >= h ? 0 : (float)( (h-targetSize)/2 ) );
    	int bottom = (int)(targetSize >= h ? h : targetSize + (float)( (w-targetSize)/2 ) );
    	
    	File dir = ctx.getDir("snapshots", Context.MODE_PRIVATE);
        File of = new File(dir, "snapshot.jpg");
        try {
            FileOutputStream fo = new FileOutputStream(of);
            
            try {
                origYuv.compressToJpeg(new Rect(left, top, right, bottom), 80, fo);
            }finally {
                fo.close();
            }
            
        }catch (IOException e) {	
        	Log.e(TAG, "Can't store picture", e);
            throw new RuntimeException(e);
        }
        
        return of;

    }    
  
   
    
    /**
    * Transform a {@link Bitmap} picture into a {@link File} to be analyzed.
    * Pictures are first compressed to a JPEG format.
    * 
    * @param ctx 
    *        The {@link context}.
    * @param bmp
    * 		 The {@link Bitmap} to be converted.
    * 
    * @return The {@link File} object.
    * 
    * @throws RuntimeException
    **/

    
    public static File saveBmpToFile(Context ctx, Bitmap bmp) {
    	File dir = ctx.getDir("snapshots", Context.MODE_PRIVATE);
        File of = new File(dir, "snapshot.jpg");
        try {
            FileOutputStream fo = new FileOutputStream(of);
            
            try {
                bmp.compress(CompressFormat.JPEG, 80, fo); 
            }finally {
            	fo.close();
            }
            
        } 
        catch (IOException e) {
        	Log.e(TAG, "Can't store picture", e);
            throw new RuntimeException(e);
        }
        
        return of;
    }
    
    
    /**
    * Transform a YUV picture into a File to be analyzed.
    * Pictures are first compressed to a JPEG format.
    * 
    * @param ctx
    * 		 The {@link context}.
    * @param yuv
    * 		 The {@link YuvImage} to be converted.
    * 
    * @return The {@link File} object.
    * 
    * @throws RuntimeException
    **/
    
    
    public static File saveYuvToFile(Context ctx, YuvImage yuv) {
        File dir = ctx.getDir("scanshots", Context.MODE_PRIVATE);
        File of = new File(dir, "scanshot.jpg");
        try {
            FileOutputStream fo = new FileOutputStream(of);
            
            try {
                yuv.compressToJpeg(new Rect(0,0,yuv.getWidth(),yuv.getHeight()), 80, fo);
            }finally {
                fo.close();
            }
            
        }catch (IOException e) {	
        	Log.e(TAG, "Can't store picture", e);
            throw new RuntimeException(e);
        }
        
        return of;
    }
    
    
    /**
     * @param ctx
     * 		  The {@link context}.
     * 
     * @return A {@link String} representing the device's MAC address
     */
    
    
    public static String getDeviceId(Context ctx) {
        WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        String wmac = wm.getConnectionInfo().getMacAddress();
        return wmac;
    }
    
    /**
     * Gives a cropped and centered rectangle representing the processed image.
     * 
     * @param Width 
     * 		  an {@link integer}, the width in pixel of the displayed image.
     * @param Height
     * 		  an {@link integer}, the height in pixel of the displayed image.
     * @param MaxSize
     * 		  an {@link integer}, the maximum dimension of a processed image.
     * @return
     *        a cropped and centered {@link Rect} with maximum dimension < MaxSize
     */

    public static Rect getProcessRect(int Width, int Height, int MaxSize){
    	
    	int left = (int)(Width > MaxSize ? (int)((Width-MaxSize)/2) :0);
    	int top = (int)(Height > MaxSize ? (int)((Height-MaxSize)/2) :0);
    	int right = (int)(Width > MaxSize ? (int)((Width+MaxSize)/2) :Width);
    	int bottom = (int)(Height > MaxSize ? (int)((Height+MaxSize)/2) :Height);
    	
    	return new Rect(left,top,right,bottom);
    }
    
    private native static void rotateData(byte[] src, int width, int height, byte[] dst);
    
    public static byte[] rotateCounterClockwise(YuvImage yuv){
    	byte[] rotatedData = new byte[yuv.getYuvData().length];
    	int height = yuv.getHeight();
        int width = yuv.getWidth();
        rotateData(yuv.getYuvData(), width, height, rotatedData);
    	return rotatedData;
    }
    
    static Bitmap convertFrameToBmp(byte[] frame, Size framePreviewSize) {
        Log.d(TAG, "frame.length=" + frame.length + ", framePreviewSize.width="
                + framePreviewSize.width + ", framePreviewSize.height=" + framePreviewSize.height);

        final int frameWidth = framePreviewSize.width;
        final int frameHeight = framePreviewSize.height;
        final int frameSize = frameWidth * frameHeight;
        int[] rgba = new int[frameSize];
        for (int i = 0; i < frameHeight; ++i)
            for (int j = 0; j < frameWidth; ++j) {
                int y = (0xff & ((int) frame[i * frameWidth + j]));
                int u = (0xff & ((int) frame[frameSize + (i >> 1) * frameWidth + (j & ~1) + 0]));
                int v = (0xff & ((int) frame[frameSize + (i >> 1) * frameWidth + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                rgba[i * frameWidth + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }

        Bitmap bmp = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0, frameWidth, 0, 0, frameWidth, frameHeight);
        return bmp;
    }
    
    public static Bitmap rotateBitmap(Bitmap bMap, int angle){   
        Matrix mat = new Matrix();
        mat.postRotate(angle);
        Bitmap bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(), bMap.getHeight(), mat, true);
    	return bMapRotate;
	}
}

	
