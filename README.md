#**VisionIQ Android SDK**



Intro
=====

IQ Engines provides VisionIQ, an image recognition platform that makes it possible to add visual search to your mobile application. Before diving into the details of how 	you can integrate the SDK into your Android application, we give a brief overview of the two engines at the core of VisionIQ: (1) visual search in the cloud, and (2) visual search on the mobile device. You can find more information about VisionIQ on the [system overview](http://www.iqengines.com/system/) page.

Vision IQ server : Visual search in the cloud
---------------------------------------------

When you send an image to VisionIQ server, it is first analyzed by VisionIQ's computer vision algorithms. The image is matched against IQ Engines' public image dataset, as well as your private image dataset if you have [trained](http://www.iqengines.com/dashboard/upload) the system.

When the computer vision algorithms are not able to recognize your image, then it is sent to VisionIQ's crowdsourcing engine, where a person will manually tag your image. By combining computer vision and crowdsourcing, we are able to provide accurate labels for 100% of images.

Vision IQ mobile : Visual search on the mobile device
-----------------------------------------------------

The VisionIQ cloud can scale to recognize millions of images. However, if your dataset is small (~100 objects), then the recognition can happen entirely on the mobile device. There is need for an internet connection as the image isn't sent to the VisionIQ cloud. This makes for a really snappy experience, and quasi-instant results. If you turn on the *continuous* mode, then frames are continuously grabbed from the camera and matched against the *local* image dataset, which means that objects can be recognized without the user having to take an action such as pressing a shutter button 

Contents 
--------

* **iqengines-sdk**: the VisionIQ SDK. It contains all the necessary functions to include VisionIQ functionality in your app.
* **iqengines-barcode**: our user-friendly integration of the [ZXing](http://code.google.com/p/zxing/) library.
* **iqengines-demo**: the VisionIQ demo application. It provides a good example of how the SDK can be included in a mobile app, along with best practices and a UI that follows Android guidelines.
* **prebuilt**: a set of prebuilt libraries for [OpenCV](http://opencv.willowgarage.com/) that are used for the *local* search.

Throughout this tutorial we use the Eclipse IDE. If you aren't developing on eclipse, please adapt the following instruction to your own IDE.

Building the demo app. 
===================

Before including the SDK in your existing app, we recommend that you first try building the demo app, as this is the best way to get familiar with both the SDK and VisionIQ's capabilities.

Installation
------------

* Install the Android [NDK](http://developer.android.com/sdk/ndk/index.html)
* Set the **`ANDROID_NDK_ROOT`** variable to the directory where you have installed the NDK in Eclipse->Preferences->Run/Debug->String substitution. This step should be done any time you change your workspace.

![center](http://img.skitch.com/20120511-f132g6p21ycs24mihdcyxy81dy.medium.jpg)

* Import an **existing project into Workspace** into your workspace and select the **iqe-android-sdk** folder.

![center](http://img.skitch.com/20120816-gyshwsxb6krsumecwdniwssnd9.png)

* For each project, in the package explorer right-click your android project and select **Properties**.
* In the **Properties** window, select the **Android** properties group at left and locate the **Project Build Target** window on the right.
* Pick a version above API level 11. Note that the SDK works for any API level 8 and above, but in order to follow Android's design guidelines, the demo app only works for any API level 11 and above.

![center](http://img.skitch.com/20120817-bwn3b8p8bw6tybky8cwpejuw7.png)

* When the dialog closes, click Apply in the **Properties** window.
* Click OK to close the **Properties** window.
* **Clean** your project.

Configure the VisionIQ SDK
--------------------------

When you import the VisionIQ SDK into an app, you can configure it by setting the following variables:

* **KEY**, **SECRET**: your API key and secret obtained after you've signed up for VisionIQ (it takes 30 seconds, at most). You can find both keys in the [developer center](http://www.iqengines.com/dashboard/settings/) 

![center](http://img.skitch.com/20120515-c7418drprn2papjpm3w9t4iq5s.png)

* **search engines** :
    * **`SEARCH_OBJECT_REMOTE`**: if set to **true**, *remote* search is enabled.
    * **`SEARCH_OBJECT_LOCAL`**: if set to **true**, *local* search is enabled.
    * **`SEARCH_OBJECT_BARCODE`**: if set to **true**, *barcode* search is enabled.
* **search modes** :
	* **`SEARCH_OBJECT_SCAN`**: if set to **true**, your app will be able to scan. Frames will be automatically grabbed and processed by either *local* or *barcode* search, according to your settings.
	* **`SEARCH_OBJECT_SNAP`**: if set to **true**, your app will be able to snap. When you push the button, the phone takes a picture and processes it. If the *remote* search is enable, it sends it to our server.
	
> With those options you can build an app using exclusively either *remote* search, *local* search or *barcode* search. Your app can also use any combination of the three.

![center](http://img.skitch.com/20120816-p83pdwnagtgy2x8mkxyk6trqpy.png)

You are now ready to try the demo-app. launch the application on your own Android device and try VisionIQ.

How to use the SDK
==================

This part explains how to get the best experience possible with VisionIQ.

Manage your datasets
--------------------

You will be dealing with 3 datasets, each developer may use 1 or more of them :

* Your private dataset on the VisionIQ cloud.
* The local dataset which is on the device. 
* IQ Engines' dataset.


### The private dataset

The easiest way to manage your own dataset is to use the website interface. Create your dataset by using the [training interface](http://www.iqengines.com/dashboard/upload/). 
With this interface uploading your data is fast and effortless. 

Your images are grouped into objects. There are three steps to build a new object.

1. Drag your images in the window.
2. Label the set of images you have selected with a name or a phrase
3. Set Metadata (optional).

![center](http://img.skitch.com/20120515-x9urp6ubcq5e6dqprerscdptk7.png)

> The more pictures you provide for an object, the more likely it will be recognized.

### The local dataset

The local dataset is hosted directly on the mobile device, and the recognition algorithms are also performed entirely locally. It is not possible at the moment for you to directly generate the image signatures that are necessary for recognition (this "local training" feature is coming soon!). To receive the iqedata folder that corresponds to your own set of images, first train VisionIQ server, and then contact us at <support@iqengines.com>. Once you have received the iqedata folder back, put it in the "assets" folder as shown in the example below.

![center](http://img.skitch.com/20120816-bp7shu4915iaghutu167yqg4rr.png)

### IQ Engines dataset and crowdsourcing

VisionIQ Also allows you to search in the IQ Engines' data base and to use the crowdsourcing module in case VisionIQ is not able to match your image. Images tagged by humans can also "train" the IQ Engines' dataset : the picture and the tag are both stored in the server.

Configure the barcode engine
----------------------------

Our *barcode* engine is an user-friendly integration of the laser-fast open library [ZXing](http://code.google.com/p/zxing/). It provides a large range of barcode formats, and is very flexible. 

The *iqengines-sdk-barcode* project contains the source code. All the settings are in the *iqengines-sdk* project, in the *com.iqengines.sdk.barcode* package. In there, you can change the barcode formats you want to recognize or the way your app handle the results

![center](http://img.skitch.com/20120816-gjyfncbsn8sngxh44fk84atawk.png)

Configure VisionIQ server
-------------------------

Crowdsourcing and *local* search are not required for every app. VisionIQ allows you to choose either *remote*, *local* or *barcode* search as well as any combination of the three. You may also choose in which dataset you want to perform the search. This flexibility is available through the [settings](http://www.iqengines.com/dashboard/settings/) on the developer dashboard.

![center](http://img.skitch.com/20120815-k7ei9jp88km7a6fmrennmaniec.png)

For example, an app performing only remote search in the private dataset would have :

* In the SDK settings :
	* **`SEARCH_OBJECT_REMOTE`**=true; 
	* **`SEARCH_OBJECT_LOCAL`**= false;
	* **`SEARCH_OBJECT_LOCAL_CONTINUOUS`**=false;
* In the dashboard settings

![center](http://img.skitch.com/20120515-dunphgqy2mfrcqqud7kxs4ibis.png)


Tips for successful and fast search
-----------------------------------

*Local* search and *barcode* search combined with *scan* mode are powerful tools that make image recognition performs very fast in your app. Time for processing an image locally is < 100 milliseconds for recent Android devices. It is important that you manage camera and picture formats carefully. Here are some tips to build the best app possible.


* *Remote* deal with every [image format](http://developer.android.com/reference/android/graphics/ImageFormat.html).
* *Barcode* search crops a square at the center of the screen. It's length is 80% of the phone width. It can be a good idea to insert a target zone.
* There is always a balance between accuracy and speed. Trials are the best way to find the best settings.
	* Compression and format conversion are time-consuming operations, avoid them whenever it's possible.
	* Data compression increases transfer speed and therefore the *remote* search speed.
	* Remember that compression involves (in some cases) data loss and lower picture quality.
	* See the [YuvImage](http://developer.android.com/reference/android/graphics/YuvImage.html) and try to manage [YUV format](http://developer.android.com/reference/android/graphics/ImageFormat.html) which is the standard output format of Android cameras.


Include the VisionIQ SDK in your own project 
============================================

To start using IQEngines SDK in your Android project, follow these steps:

* If the **NDK** isn't set up, install the Android [NDK](http://developer.android.com/sdk/ndk/index.html) Set the android **`ANDROID_SDK_ROOT`** as explained earlier.
* Import both **iqengines-sdk** and **iqengines-sdk-barcode** projects into your workspace.
* Pick a version above API Level 8 in the **Properties** as explained earlier.
* In the Package Explorer, right-click your **Android project** and select **Properties**.
* In the **Properties** window, select the **Android** properties group at left and locate the **Library** window on the right.
* Click Add to open the Project Selection dialog.

![center](http://img.skitch.com/20120511-dwc7rj7rjy6ajjjpntbayyrghi.png)

* From the list of available library projects, select the **iqengines-sdk** project and click OK.
* When the dialog closes, click Apply in the **Properties** window.
* Click OK to close the **Properties** window.
* **Clean** your project.

You can now use the VisionIQ features on your app.

For example :

	private void startLocalContinuousCapture() {
    	Preview.FrameReceiver receiver = new DemoFrameReceiver();
    	capturing.set(true);
    	preview.setFrameReceiver(receiver);
	}


	class DemoFrameReceiver implements Preview.FrameReceiver {
		@Override
		public void onFrameReceived(byte[] frameBuffer, Size framePreviewSize) {
			if (!iqe.isIndexInitialized()) {
				// local index is not initialized yet
				return;
			}
			if (!capturing.get()) {
				return;
			}
			if (remoteMatchInProgress.get()) {
			} else {
				if (!SEARCH_OBJECT_LOCAL_CONTINUOUS) {
					return;
				}
				if (localMatchInProgress.get()) {
					return;
				}
				localMatchInProgress.set(true);
				Bitmap bmp = Preview.convertFrameToBmp(frameBuffer, framePreviewSize);
				processImageNative(bmp);
			}
		}
	};


Questions
=========

* [FAQ](http://support.iqengines.com/knowledgebase)
* ask your questions at support@iqengines.com