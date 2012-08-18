/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iqengines.sdk.Barcode;

import java.util.Map;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.iqengines.sdk.Barcode.Decoder.BarcodeFormat;
import com.iqengines.sdk.Barcode.Decoder.DecodeHintType;
import com.iqengines.sdk.Barcode.Decoder.MultiFormatReader;
import com.iqengines.sdk.Barcode.Decoder.ReaderException;
import com.iqengines.sdk.Barcode.Decoder.Result;
import com.iqengines.sdk.Barcode.Decoder.common.HybridBinarizer;
import com.iqengines.sdk.Barcode.result.ParsedResult;
import com.iqengines.sdk.Barcode.result.ResultParser;
import com.iqengines.sdk.IQE;
import com.iqengines.sdk.Utils;

 public final class BarcodeHandler extends Handler {
	 
 public static final int CMD_SUCCESS = 1;
 public static final int CMD_FAIL = 2;
 public static final int CMD_DECODE = 3;
 public static final int CMD_QUIT = 4;

 /**
  * Maximum dimension of a processed picture.
  */
 private int MaxSize;
 
 private static final String TAG = BarcodeHandler.class.getSimpleName();
 private IQE iqe;

 private final MultiFormatReader multiFormatReader;
 
 /**
  * Class builder
  * 
  * @param hints
  * 	Set the barcode formats to search for. See {@ link BarcodeFormatManager}.
  * @param iqe
  * 	The {@ link IQE}  object instantiating the BarcodeHandler.
  */
 
 
  BarcodeHandler (Map<DecodeHintType, Object> hints,IQE iqe) {
	
	this.iqe=iqe;  
	  
    multiFormatReader = new MultiFormatReader();
    multiFormatReader.setHints(hints);
  }
  
  /**
   * Interface between IQE and the barcode decoder.
   */

  @Override
  public void handleMessage(Message message) {
  
    switch (message.what) {
      case CMD_DECODE:
    	  MaxSize = (int) (((YuvImage) message.obj).getWidth()*0.8);
          decode((YuvImage) message.obj,message.arg1);
          break;
      case CMD_SUCCESS:
    	  iqe.obtainMessage(IQE.CMD_SUCCESS_BARCODE,message.arg1,message.arg2, (String) message.obj).sendToTarget();
	      break;
      case CMD_FAIL:
    	  iqe.obtainMessage(IQE.CMD_NO_RESULT_BARCODE,message.arg1,0).sendToTarget();
    	  break;
      case CMD_QUIT:
    	  Looper.myLooper().quit();
    	  break;
      
    }
  }

  /**
   * Decodes the data within the viewfinder rectangle, and times how long it took. 	
   *
   * @param data   The YUV previewed frame.
   */
  public void decode(YuvImage yuv, int callType) {
	
    byte[] data = Utils.rotateCounterClockwise(yuv);
    int width = yuv.getHeight();
    int height = yuv.getWidth();
	  
	// Start of the search  
    long start = System.currentTimeMillis();
    Result rawResult = null;
    Rect ProcessRect = Utils.getProcessRect(width, height, MaxSize);
    PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data,width,height,ProcessRect.left,ProcessRect.top,ProcessRect.width(),ProcessRect.height(),false);
    data = null;
    if (source != null) {
      BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
      source = null;
      try {
        rawResult = multiFormatReader.decodeWithState(bitmap);
      } catch (ReaderException re) {
        // continue
      } finally {
        multiFormatReader.reset();
      }
    }
    int formatDimension = 0;	
    
    // dealing with results
    // you can have more information about the barcode using the methods from the Result and ParsedResult classes. 
    if (rawResult != null) {
    	   BarcodeFormat resultFormat = rawResult.getBarcodeFormat();
    	   if (resultFormat == BarcodeFormat.UPC_A ||
    			   resultFormat == BarcodeFormat.UPC_E ||
    					   resultFormat == BarcodeFormat.EAN_13 ||
    							   resultFormat == BarcodeFormat.EAN_8 ||
    									   resultFormat == BarcodeFormat.CODE_128 )
    		   formatDimension = 1;
    	   else
    		   formatDimension = 2;
    	   ParsedResult result = ResultParser.parseResult(rawResult);
    	   // String barcodeType = result.getType();
    	   String contents = result.getDisplayResult();
    	   contents.replace("\r", "");  
      // Don't log the barcode contents for security.
      long end = System.currentTimeMillis();
     
      Log.d(TAG, "Found barcode in " + (end - start) + " ms");  
      
      this.obtainMessage(CMD_SUCCESS, callType, formatDimension, contents).sendToTarget();
    } else {
    	Message.obtain(this, BarcodeHandler.CMD_FAIL,callType,0, yuv).sendToTarget();
    
    }
    
  }
  
}