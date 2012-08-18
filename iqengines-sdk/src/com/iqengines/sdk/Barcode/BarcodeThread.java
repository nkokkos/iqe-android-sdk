/*
3 * Copyright (C) 2008 ZXing authors
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

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import android.os.Looper;

import com.iqengines.sdk.Barcode.Decoder.BarcodeFormat;
import com.iqengines.sdk.Barcode.Decoder.DecodeHintType;
import com.iqengines.sdk.IQE;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class BarcodeThread extends Thread {

  public static final String BARCODE_BITMAP = "barcode_bitmap";

  private final Map<DecodeHintType,Object> hints;
  public BarcodeHandler handler;
  private IQE iqe;
  
  
  public BarcodeThread(Collection<BarcodeFormat> decodeFormats,IQE iqe, String characterSet, String string) {
	super(string);
	this.iqe = iqe;
    hints = new EnumMap<DecodeHintType,Object>(DecodeHintType.class);
    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

    if (characterSet != null) {
      hints.put(DecodeHintType.CHARACTER_SET, characterSet);
    }
    
  }
  
  @Override
  public void run() {
    Looper.prepare();
    handler = new BarcodeHandler(hints,iqe);
    Looper.loop();
  }

}
