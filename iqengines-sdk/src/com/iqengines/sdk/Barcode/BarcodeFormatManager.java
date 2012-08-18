package com.iqengines.sdk.Barcode;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Intent;
import android.net.Uri;

import com.iqengines.sdk.Barcode.Decoder.BarcodeFormat;

/**
 * This class gives the Barcode Formats handled by the decoder.
 */


public class BarcodeFormatManager {

	private static final Pattern COMMA_PATTERN = Pattern.compile(",");

	  public static final Collection<BarcodeFormat> IQ_FORMATS;
	  
	  /**
	   * Those are the formats handled by IQE.
	   * You can add any of the Barcodeformat mentioned in that class
	   */
	  
	  static {
		  IQ_FORMATS = EnumSet.of(BarcodeFormat.UPC_A,
                  BarcodeFormat.UPC_E,
                  BarcodeFormat.EAN_13,
                  BarcodeFormat.EAN_8,
                  BarcodeFormat.CODE_128,
                  BarcodeFormat.QR_CODE,
                  BarcodeFormat.DATA_MATRIX);  
	  }
	    
	  public static final Collection<BarcodeFormat> PRODUCT_FORMATS;
	  public static final Collection<BarcodeFormat> ONE_D_FORMATS;
	  public static final Collection<BarcodeFormat> QR_CODE_FORMATS = EnumSet.of(BarcodeFormat.QR_CODE);
	  public static final Collection<BarcodeFormat> DATA_MATRIX_FORMATS = EnumSet.of(BarcodeFormat.DATA_MATRIX);
	  static {
	    PRODUCT_FORMATS = EnumSet.of(BarcodeFormat.UPC_A,
	                                 BarcodeFormat.UPC_E,
	                                 BarcodeFormat.EAN_13,
	                                 BarcodeFormat.EAN_8,
	                                 BarcodeFormat.RSS_14);
	    ONE_D_FORMATS = EnumSet.of(BarcodeFormat.CODE_39,
	                               BarcodeFormat.CODE_93,
	                               BarcodeFormat.CODE_128,
	                               BarcodeFormat.ITF);
	    ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
	  }

	  private BarcodeFormatManager() {}

	  static Collection<BarcodeFormat> parseDecodeFormats(Intent intent) {
	    List<String> scanFormats = null;
	    String scanFormatsString = intent.getStringExtra(Scan.FORMATS);
	    if (scanFormatsString != null) {
	      scanFormats = Arrays.asList(COMMA_PATTERN.split(scanFormatsString));
	    }
	    return parseDecodeFormats(scanFormats, intent.getStringExtra(Scan.MODE));
	  }

	  static Collection<BarcodeFormat> parseDecodeFormats(Uri inputUri) {
	    List<String> formats = inputUri.getQueryParameters(Scan.FORMATS);
	    if (formats != null && formats.size() == 1 && formats.get(0) != null){
	      formats = Arrays.asList(COMMA_PATTERN.split(formats.get(0)));
	    }
	    return parseDecodeFormats(formats, inputUri.getQueryParameter(Scan.MODE));
	  }

	  private static Collection<BarcodeFormat> parseDecodeFormats(Iterable<String> scanFormats,
	                                                              String decodeMode) {
	    if (scanFormats != null) {
	      Collection<BarcodeFormat> formats = EnumSet.noneOf(BarcodeFormat.class);
	      try {
	        for (String format : scanFormats) {
	          formats.add(BarcodeFormat.valueOf(format));
	        }
	        return formats;
	      } catch (IllegalArgumentException iae) {
	        // ignore it then
	      }
	    }
	    if (decodeMode != null) {
	      if (Scan.PRODUCT_MODE.equals(decodeMode)) {
	        return PRODUCT_FORMATS;
	      }
	      if (Scan.QR_CODE_MODE.equals(decodeMode)) {
	        return QR_CODE_FORMATS;
	      }
	      if (Scan.DATA_MATRIX_MODE.equals(decodeMode)) {
	        return DATA_MATRIX_FORMATS;
	      }
	      if (Scan.ONE_D_MODE.equals(decodeMode)) {
	        return ONE_D_FORMATS;
	      }
	    }
	    return null;
	  }
	 
	  public static final class Scan {
		    /**
		     * Send this intent to open the Barcodes app in scanning mode, find a barcode, and return
		     * the results.
		     */
		    public static final String ACTION = "com.google.zxing.client.android.SCAN";

		    /**
		     * By default, sending this will decode all barcodes that we understand. However it
		     * may be useful to limit scanning to certain formats. Use
		     * {@link android.content.Intent#putExtra(String, String)} with one of the values below.
		     *
		     * Setting this is effectively shorthand for setting explicit formats with {@link #FORMATS}.
		     * It is overridden by that setting.
		     */	
		    public static final String MODE = "SCAN_MODE";

		    /**
		     * Decode only UPC and EAN barcodes. This is the right choice for shopping apps which get
		     * prices, reviews, etc. for products.
		     */
		    public static final String PRODUCT_MODE = "PRODUCT_MODE";

		    /**
		     * Decode only 1D barcodes.
		     */
		    public static final String ONE_D_MODE = "ONE_D_MODE";

		    /**
		     * Decode only QR codes.
		     */
		    public static final String QR_CODE_MODE = "QR_CODE_MODE";

		    /**
		     * Decode only Data Matrix codes.
		     */
		    public static final String DATA_MATRIX_MODE = "DATA_MATRIX_MODE";
		    
		    /**
		     * Decode format required by IqEngines.
		     */
		    
		    public static final String IQ_MODE = "IQ_MODE";

		    /**
		     * Comma-separated list of formats to scan for. The values must match the names of
		     * {@link com.google.zxing.BarcodeFormat}s, e.g. {@link com.google.zxing.BarcodeFormat#EAN_13}.
		     * Example: "EAN_13,EAN_8,QR_CODE"
		     *
		     * This overrides {@link #MODE}.
		     */
		    public static final String FORMATS = "SCAN_FORMATS";

		    /**
		     * @see com.google.zxing.DecodeHintType#CHARACTER_SET
		     */
		    public static final String CHARACTER_SET = "CHARACTER_SET";

		    /**
		     * Optional parameters to specify the width and height of the scanning rectangle in pixels.
		     * The app will try to honor these, but will clamp them to the size of the preview frame.
		     * You should specify both or neither, and pass the size as an int.
		     */
		    public static final String WIDTH = "SCAN_WIDTH";
		    public static final String HEIGHT = "SCAN_HEIGHT";

		    /**
		     * Desired duration in milliseconds for which to pause after a successful scan before
		     * returning to the calling intent. Specified as a long, not an integer!
		     * For example: 1000L, not 1000.
		     */
		    public static final String RESULT_DISPLAY_DURATION_MS = "RESULT_DISPLAY_DURATION_MS";

		    /**
		     * Prompt to show on-screen when scanning by intent. Specified as a {@link String}.
		     */
		    public static final String PROMPT_MESSAGE = "PROMPT_MESSAGE";

		    /**
		     * If a barcode is found, Barcodes returns RESULT_OK to 
		     * {@link android.app.Activity#onActivityResult(int, int, android.content.Intent)}
		     * of the app which requested the scan via
		     * {@link android.app.Activity#startActivityForResult(android.content.Intent, int)}
		     * The barcodes contents can be retrieved with
		     * {@link android.content.Intent#getStringExtra(String)}. 
		     * If the user presses Back, the result code will be
		     * RESULT_CANCELED.
		     */
		    public static final String RESULT = "SCAN_RESULT";

		    /**
		     * Call intent.getStringExtra(RESULT_FORMAT) to determine which barcode format was found.
		     * See Contents.Format for possible values.
		     */
		    public static final String RESULT_FORMAT = "SCAN_RESULT_FORMAT";

		    /**
		     * Call intent.getByteArrayExtra(RESULT_BYTES) to get a {@code byte[]} of raw bytes in the
		     * barcode, if available.
		     */
		    public static final String RESULT_BYTES = "SCAN_RESULT_BYTES";

		    /**
		     * Key for the value of {@link com.google.zxing.ResultMetadataType#ORIENTATION}, if available.
		     * Call intent.getIntExtra(RESULT_ORIENTATION).
		     */
		    public static final String RESULT_ORIENTATION = "SCAN_RESULT_ORIENTATION";

		    /**
		     * Key for the value of {@link com.google.zxing.ResultMetadataType#ERROR_CORRECTION_LEVEL}, if available.
		     * Call intent.getStringExtra(RESULT_ERROR_CORRECTION_LEVEL).
		     */
		    public static final String RESULT_ERROR_CORRECTION_LEVEL = "SCAN_RESULT_ERROR_CORRECTION_LEVEL";

		    /**
		     * Prefix for keys that map to the values of {@link com.google.zxing.ResultMetadataType#BYTE_SEGMENTS},
		     * if available. The actual values will be set under a series of keys formed by adding 0, 1, 2, ...
		     * to this prefix. So the first byte segment is under key "SCAN_RESULT_BYTE_SEGMENTS_0" for example.
		     */
		    public static final String RESULT_BYTE_SEGMENTS_PREFIX = "SCAN_RESULT_BYTE_SEGMENTS_";

		    /**
		     * Setting this to false will not save scanned codes in the history.
		     */
		    public static final String SAVE_HISTORY = "SAVE_HISTORY";

		    private Scan() {
		    }

	  }
}


