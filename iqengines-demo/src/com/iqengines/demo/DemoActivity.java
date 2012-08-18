package com.iqengines.demo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.validator.routines.UrlValidator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iqengines.sdk.IQE;
import com.iqengines.sdk.IQE.OnResultCallback;
import com.iqengines.sdk.Utils;

public class DemoActivity extends Activity {

	/**
	 * Account settings. You can obtain the required keys after you've signed up
	 * for visionIQ.
	 */

	// Insert your API key here (find it at iengines.com --> developer center
	// --> settings).
	static final String KEY = "";
	// Insert your secret key here (find it at iengines.com --> developer center
	// --> settings).
	static final String SECRET = "";

	/**
	 * Settings.
	 */
	
	// Activates the local search.
	static final boolean SEARCH_OBJECT_LOCAL = true;
	
	// Activates the barcode scanning
	static boolean SEARCH_OBJECT_BARCODE = true;
	
	// Activates the scan search.
	static boolean SEARCH_OBJECT_SCAN = true;
	
	// Activates the snap search
	static boolean SEARCH_OBJECT_SNAP = true;
	
	// Activates the remote search.
	static final boolean SEARCH_OBJECT_REMOTE = true;

	// Maximum duration of a remote search.
	static final long REMOTE_MATCH_MAX_DURATION = 10000;
	


	
	static final int MAX_ITEM_HISTORY = 20;

	static final boolean PROCESS_ASYNC = true;

	static final boolean DEBUG = true;

	private static final String TAG = DemoActivity.class.getSimpleName();

	private Handler handler;

	private Preview preview;

	private ImageButton snapButton;

	private ImageButton btnShowList;

	private ImageButton tutoButton;

	List<HistoryItem> history;

	private HistoryItemDao historyItemDao;

	static HistoryListAdapter historyListAdapter;

	static IQE iqe;

	private AlertDialog ad;
	
	private QueryProgressDialog pd;

	private AtomicBoolean activityRunning = new AtomicBoolean(false);
	
	private Preview.FrameReceiver mreceiver;
	
	/**
	 * Checks whether local search is possible on this hardware.
	 * 
	 * @return A {@link Boolean} true if local search is possible.
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.d(TAG, "onCreate");
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		handler = new Handler();
		initHistory();
		initHistoryListView();
		initUI();
		initIqSdk();
		mreceiver = new DemoFrameReceiver();
		preview.mPreviewThread = preview.new PreviewThread("Preview Thread");
		preview.mPreviewThread.start();
	}

	private void initHistory() {
		historyItemDao = new HistoryItemDao(this);
		history = historyItemDao.loadAll();
		if (history == null) {
			history = new ArrayList<HistoryItem>();
		}
	}

	private void initHistoryListView() {
		historyListAdapter = new HistoryListAdapter(this);
		btnShowList = (ImageButton) findViewById(R.id.historyButton);
		btnShowList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				preview.mPreviewThreadRun.set(false);
				Intent intent = new Intent(DemoActivity.this,HistoryActivity.class);
				startActivity(intent);
			}
		});
	}

	private void initIqSdk() {
		iqe = new IQE(this, SEARCH_OBJECT_REMOTE,SEARCH_OBJECT_LOCAL,
				SEARCH_OBJECT_BARCODE, onResultCallback, KEY, SECRET);
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.d(TAG,"onDestroy");
		iqe.destroy();
		super.onDestroy();
	}

	// some code unused in specific configurations.
	private void initUI() {
		preview = (Preview) findViewById(R.id.preview);
		tutoButton = (ImageButton) findViewById(R.id.tutoButton);
		tutoButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				preview.mPreviewThreadRun.set(false);
				Intent intent = new Intent(DemoActivity.this,
						TutorialActivity.class);
				startActivity(intent);
			}
		});
		
		pd = new QueryProgressDialog(this);
		
		snapButton = (ImageButton) findViewById(R.id.capture);
		snapButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {				
				if (DEBUG) Log.d(TAG,"*********** snap button pushed ***********");
				iqe.goScan();
				stopScanning();
				
				snapButton
				.setImageResource(R.drawable.btn_ic_camera_shutter);
				
				if (preview.mCamera == null) {
					return;
				}

				if (SEARCH_OBJECT_REMOTE) {
					showCenteredProgressDialog("Uploading...");
				} else {
					showCenteredProgressDialog("Searching...");
				}
				
				preview.mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						if (data == null) {
							return;
						}
						preview.copyLastFrame(data);
						freezePreview();
						YuvImage yuv = new YuvImage(preview.getLastFrameCopy(),
								ImageFormat.NV21, preview.mPreviewSize.width,
								preview.mPreviewSize.height, null);
						// initiate the snap search
						processImageSnap(yuv);
					}
				});
				

			}
		});

		if (!SEARCH_OBJECT_SNAP) {
			snapButton.setVisibility(View.GONE);
		}
		
	}

	@Override
	public void onResume() {
		super.onResume();
		activityRunning.set(true);
		iqe.resume();
		preview.setFrameReceiver(mreceiver);
		if(preview.mCamera!=null){
			unfreezePreview();
		}
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.d(TAG, "onPause");
		
		stopScanning();
		iqe.pause();
		historyItemDao.saveAll(history);
		preview.stopPreview();
		activityRunning.set(false);
		super.onPause();
	}

	private void startScanning() {
		if(SEARCH_OBJECT_SCAN){
			if(DEBUG) Log.d(TAG,"start scanning");
			preview.mPreviewThreadRun.set(true);
			iqe.goScan();
			preview.scan();
		}
	}
	
	private void stopScanning(){
		if(DEBUG) Log.d(TAG,"stop scanning");
		preview.mPreviewThreadRun.set(false);
	}

	private void freezePreview() {
		// on old device freezing preview only shows a black screen
		if(DEBUG) Log.d(TAG, "preview is freezed");
		preview.stopPreview();
		preview.PreviewCallbackScan();
	}

	private void unfreezePreview() {
		preview.startPreview();
		if(SEARCH_OBJECT_SCAN){
			if (!pd.isShowing()){
				startScanning();
			}
		}
	}

	private String lastPostedQid = null;
	
	private void createHistoryItem(String qid, String path, int callType) {

		Bitmap thumb = null;

		switch (callType) {
		
		case (IQE.scan):
			try {
				InputStream is = this.getAssets().open(
						"iqedata/" + path + "/" + path + ".jpg");				
				Bitmap origBmp = BitmapFactory.decodeStream(is);
				thumb = transformBitmapToThumb(origBmp);
			} catch (IOException e) {
				thumb = null;
			}
			break;
			
		case (IQE.snap):
			
			Bitmap origBmp = BitmapFactory.decodeFile(path);
			thumb = transformBitmapToThumb(origBmp);
			thumb = Utils.rotateBitmap(thumb, preview.getAngle());
			
		}

		HistoryItem item = new HistoryItem();
		item.id = lastPostedQid = qid;
		item.label = "Searching...";
		item.uri = null;
		item.thumb = thumb;

		if (history.size() > MAX_ITEM_HISTORY)
			history.remove(0);

		history.add(item);

		if (DEBUG) Log.d(TAG, "History item created for qid: " + qid);
	}

	private Runnable postponedToastAction;

	/**
	 * Method that starts a local and remote research on the phone.
	 * onResultCallback is called when result is ready.
	 * 
	 * @param bmp
	 *            A {@link Bitmap} image to process.
	 */

	private void processImageSnap(final YuvImage yuv) {

			postponedToastAction = new Runnable() {
				public void run() {
					if (SEARCH_OBJECT_REMOTE) {
					Toast.makeText(
							DemoActivity.this,
							"This may take a minute... We will notify you when your photo is recognized.",
							Toast.LENGTH_LONG).show();
					}
					pd.dismiss();
					pd.pdDismissed();
					unfreezePreview();
					iqe.goScan();
					snapButton
					.setImageResource(R.drawable.ic_camera);
				}
			};
			handler.postDelayed(postponedToastAction, REMOTE_MATCH_MAX_DURATION);

			if (DEBUG) Log.d(TAG," snap decode message");
		iqe.sendMessageAtFrontOfQueue(iqe.obtainMessage(IQE.CMD_DECODE, IQE.snap, 0, yuv));
	}

	/**
	 * Method that starts a local research on the phone. onResultCallback is
	 * called when result is ready.
	 * 
	 * @param yuv
	 *            A {@link YuvImage} to process.
	 * 
	 */

	private void processImageScan(final YuvImage yuv) {
		if (DEBUG) Log.d(TAG,"scan decode message");
		iqe.goScan();
		Message.obtain(iqe, IQE.CMD_DECODE, IQE.scan, 0, yuv).sendToTarget();
	}

	/**
	 * Checks if the Uri provided is good and displays it.
	 * 
	 * @param a
	 *            The current {@link Activity}.
	 * @param uri
	 *            The {@link Uri} to analyze.
	 * @return
	 */

	static boolean processMetaUri(Activity a, Uri uri) {

		if (uri != null && uri.toString().length() > 0) {
			try {
				a.startActivity(new Intent(Intent.ACTION_VIEW, uri));
				return true;
			} catch (ActivityNotFoundException e) {
				Log.w(TAG,
						"Unable to open view for this meta: " + uri.toString(),
						e);
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Processes the data from the match found and manage the UI.
	 * 
	 * @param searchId
	 *            A {@link String} that identifies the query.
	 * @param label
	 *            A {@link String} that gives the match found label.
	 * @param uri
	 *            A {@link Uri} representing the Metadata of the match found.
	 * @param continousSearch
	 *            A {@link Boolean} whether the continuous local search is
	 *            enable or not.
	 */

	private void processSearchResult(String searchId, String label, Uri uri,
			final int callType) {

		HistoryItem item = null;
		///////// find the linked history item /////////
		//********************************************//
		for (Iterator<HistoryItem> iter = history.iterator();;) {
			if (!iter.hasNext()) {
				break;
			}
			item = iter.next();
			if (searchId.equals(item.id)) {
				if (DEBUG) Log.d(TAG, "" + item.id);
				if (DEBUG) Log.d(TAG, "" + searchId);
				item.label = label;
				item.uri = uri;
				break;
			} else {
				item = null;
			}
		}

		if (item == null) {
			if (DEBUG) Log.w(TAG, "No entry found for qid: " + searchId);
			startScanning();
			return;
		}
		historyListAdapter.notifyDataSetChanged();
		//*********************************************//
		/////////////////////////////////////////////////

		if (!activityRunning.get()) {
			return;
		}

		
//		does not display the result if the query isn't the last posted
//		if (!searchId.equals(lastPostedQid)) {
//			return;
//		}
		
		
		Boolean validUri = false;
		// Try to display the resources from the Uri. //
		//********************************************//
		if (uri == null) {
			UrlValidator urlValidator = new UrlValidator();
			if (urlValidator.isValid(label)) {
				validUri = true;
				Uri Buri = Uri.parse(label);
				this.startActivity(new Intent(Intent.ACTION_VIEW, Buri));	
			}

		} else {
			validUri = processMetaUri(this, uri);
		}
		//*********************************************//
		/////////////////////////////////////////////////

		// If no Metadata available, just display the match found and the label.

		if (!validUri) {
			displayResult(item, callType, null);
		}
	}

	private void displayResult(final HistoryItem item, final int callType,
			File imgFile) {
		
		preview.mPreviewThreadRun.set(false);
		
		if(DEBUG) Log.d(TAG,"display results");
		
		// do no add results on top of others.
		if (ad !=null){
			if(ad.isShowing()){
				return;
			}
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View resultView = getLayoutInflater().inflate(R.layout.match_dialog,
				null);
		TextView tv = (TextView) resultView.findViewById(R.id.matchLabelTv);
		ImageView iv = (ImageView) resultView.findViewById(R.id.matchThumbIv);
		final Activity ac = this;

		/////// set a shop button for successful matches ///////
		//****************************************************//
 		if (item != null) {
			iv.setImageBitmap(item.thumb);
			tv.setText(item.label);
			if (item.label != IQE.NO_MATCH_FOUND_STR) {
				builder.setNeutralButton("Shop",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if(pd.isShowing()){
									iqe.goScan();
									pd.dismiss();
									pd.pdDismissed();
									unfreezePreview();
								}else{
									if(callType==IQE.scan) startScanning();
								}
								snapButton
										.setImageResource(R.drawable.ic_camera);
								if (!processMetaUri(ac, Uri.parse(item.label))) {
									Uri uriShop = Uri
											.parse("http://google.com//search?q="
													+ Uri.parse(item.label)
													+ "&tbm=shop");
									processMetaUri(ac, uriShop);
								}
							}
						});
			}
		//****************************************************//
		////////////////////////////////////////////////////////	
		} else {
			tv.setText(IQE.NO_MATCH_FOUND_STR);
			Bitmap origBmp = BitmapFactory.decodeFile(imgFile.getPath());
			Bitmap thumb = transformBitmapToThumb(origBmp);
			Bitmap rotThumb = Utils.rotateBitmap(thumb, preview.getAngle());
			iv.setImageBitmap(rotThumb);
		}
		builder.setView(resultView);
		builder.setTitle("Result");

		builder.setCancelable(true);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(pd.isShowing){
					iqe.goScan();
					pd.dismiss();
					pd.pdDismissed();
					unfreezePreview();
				}else{
					if(callType==IQE.scan) startScanning();
				}
				snapButton.setImageResource(R.drawable.ic_camera);
			}
		}); 
		ad = builder.create();
		ad.setCanceledOnTouchOutside(false);
		ad.setCancelable(false);
		ad.show();
	}

	class DemoFrameReceiver implements Preview.FrameReceiver {

		/**
		 * Starts the continuous local search with the displayed frames.
		 * 
		 * @param frameBuffer
		 *            A {@link Byte} array, the frame's data.
		 * @param framePreviewSize
		 *            A {@link Size}, the frame dimensions.
		 */
		
		
		@Override
		public void onFrameReceived(byte[] frameBuffer, Size framePreviewSize) {

			
			if (!preview.mPreviewThreadRun.get()) {
				return;
			}
			
			 if(frameBuffer == null){
				if (DEBUG) Log.d(TAG,"no picture");
				return;
			}
			
			YuvImage yuvImage = new YuvImage(frameBuffer, 17,
			framePreviewSize.width, framePreviewSize.height, null);
			// analyze the picture.
			processImageScan(yuvImage);
			
		}
		
		
	}

	/**
	 * Interface used to communicate with the iqe class
	 */

	private OnResultCallback onResultCallback = new OnResultCallback() {

		/**
		 * Called whenever a query is done (In the demo app, we call it on every
		 * snap queries and on Successful scan queries)
		 * 
		 * @param queryId
		 *            A {@link String}, the unique Id of the query.
		 * @param path
		 *            A {@link String}, the path of the picture associated with
		 *            the query.
		 * @param callType
		 *            An {@link Integer}, defines if it's a snap or a scan call.
		 */

		@Override
		public void onQueryIdAssigned(String queryId, String path, int callType) {

			switch (callType) {
			
			case (IQE.scan):
				createHistoryItem(queryId, path, IQE.scan);
				break;
			
			case (IQE.snap):
				createHistoryItem(queryId, path, IQE.snap);
				if (SEARCH_OBJECT_REMOTE) {
					handler.post(new Runnable() {

						@Override
						public void run() {
							pd.setMessage("Searching...");
						}
					});
				}
				break;
			}
		}

		/**
		 * Handle the results.
		 * 
		 * @param queryId
		 *            A {@link String}, the unique Id of the query.
		 * @param objId
		 *            A {@link String}, the unique Id identifying the object on
		 *            our server. * @param objId A {@link String}, the object
		 *            label. * @param objId A {@link String}, the object
		 *            metadata. * @param objId A {@link Integer}, determines
		 *            which engine made the match (barcode, local, remote).
		 * @param callType
		 *            An {@link Integer}, defines if it's a snap or a scan call.
		 */

		@Override
		public void onResult(String queryId, String objId, String objName,
				String objMeta, int engine, final int callType) {

			final String qId = queryId;
			final String oNm = objName;
			
			// if the it is a barcode
			if (engine == IQE.barcode) {
				handler.post(new Runnable() {
					public void run() {
						if (callType==IQE.snap) {
							handler.removeCallbacks(postponedToastAction);
						}
						processSearchResult(qId, oNm, null, IQE.scan);
					}
				});
				return;
			}
			
			//if it is a local match
			if (engine == IQE.local){
				handler.post(new Runnable() {
					public void run() {
						if (callType==IQE.snap) {
							handler.removeCallbacks(postponedToastAction);
						}
						processSearchResult(qId, oNm, null, IQE.scan);
					}
				});
				return;
			}
			
			//if it is a remote match
			else {
				if (queryId.equals(lastPostedQid)) {
					handler.removeCallbacks(postponedToastAction);
				}
				Uri uri = null;
				// match's Metadata set as URI.
				if (objMeta != null) {

					try {
						uri = Uri.parse(objMeta);
					} catch (Exception e1) {
						uri = null;
					}
				}
				// if no Metadata : match's name set as URI.
				if (uri == null) {

					if (objName != null) {

						try {
							uri = Uri.parse(objName);
						} catch (Exception e1) {
							uri = null;
						}
					}
				}
				final Uri fUri = uri;
				handler.post(new Runnable() {
					@Override
					public void run() {
						// process and display the results
						processSearchResult(qId, oNm, fUri, callType);
					}
				});
			}
		}

		/**
		 * When no match are found, or exception occurs.
		 * 
		 * 
		 */

		@Override
		public void onNoResult(int callType, Exception e, File imgFile) {
			
			// if an exception occured
			if (e != null) {
				if (e instanceof IOException) {
					Log.w(TAG, "Server call failed", e);
					handler.post(new Runnable() {
						@Override
						public void run() {
							handler.removeCallbacks(postponedToastAction);
							Toast.makeText(
									DemoActivity.this,
									"Unable to connect to the server. "
											+ "Check your intenet connection.",
									Toast.LENGTH_LONG).show();
							pd.dismiss();
							pd.pdDismissed();
							unfreezePreview();
						}
					});
				} else {
					Log.e(TAG, "Unable to complete search", e);
				}
				return;
			}
			// if just nothing found
			switch (callType) {
				
			case (IQE.scan):
				startScanning();
				break;

			case (IQE.snap):
				displayResult(null, IQE.snap, imgFile);
				break;
			}
		}
	};

	private Bitmap transformBitmapToThumb(Bitmap origBmp) {
		int thumbSize = getResources()
				.getDimensionPixelSize(R.dimen.thumb_size);
		return Utils.cropBitmap(origBmp, thumbSize);
	}

	private class QueryProgressDialog extends ProgressDialog {

		private boolean isShowing;
		
		public QueryProgressDialog(Activity activity) {
			super(activity);
			this.isShowing=false;
		}
		
		public void pdShowing(){
			this.isShowing=true;
		}
		
		public void pdDismissed(){
			this.isShowing=false;
		}
		
		public boolean isShowing(){
			return isShowing;
		}

	}
	
	public QueryProgressDialog showCenteredProgressDialog(final String msg) {
		
		pd.setCanceledOnTouchOutside(false);
		pd.setCancelable(false);
		pd.show();
		pd.pdShowing();
		handler.post(new Runnable() {
			@Override
			public void run() {
				pd.setMessage(msg);
			}
		});
		return pd;
		}
}
