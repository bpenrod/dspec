package org.lucasr.dspec.overlay;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.lucasr.dspec.DesignSpec;

public class DisplayOverlayService extends Service {

	public static final String ASSET_FILE = "asset-file";

	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		DisplayOverlayService getService() {
			// Return this instance of LocalService so clients can call public methods
			return DisplayOverlayService.this;
		}
	}

	private final IBinder localBinder = new LocalBinder();
	private WindowManager windowManager;
	private View overlay;

	@Override
	public IBinder onBind(Intent intent) {
		return localBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		overlay = new View(this) {
			@Override
			public boolean onTouchEvent(MotionEvent event) {
				return false;
			}
		};
		overlay.setFitsSystemWindows(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (overlay != null)
			windowManager.removeView(overlay);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.hasExtra(ASSET_FILE)) {
			displaySpecFromAsset(intent.getStringExtra(ASSET_FILE));
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public void displaySpecFromAsset(final String assetFileName) {
		if (overlay == null) {
			overlay = new View(this);
		}
		final DesignSpec designSpec = DesignSpec.fromAsset(overlay, assetFileName);
		overlay.setBackground(designSpec);

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		if (overlay.getParent() != null) {
			windowManager.removeView(overlay);
		}

		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.START;
		params.x = 0;
		params.y = 0;

		windowManager.addView(overlay, params);
	}
}
