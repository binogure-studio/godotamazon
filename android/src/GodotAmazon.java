
package org.godotengine.godot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.os.Bundle;

import com.godot.game.BuildConfig;
import org.godotengine.godot.GodotLib;

import org.godotengine.godot.inapp.InAppManager;
import org.godotengine.godot.Dictionary;

public class GodotAmazon extends Godot.SingletonBase {

	private static final String TAG = "GodotAmazon";
	private static Context context;
	private static Activity activity;

	private InAppManager inAppManager;

	static public Godot.SingletonBase initialize (Activity p_activity) {
		return new GodotAmazon(p_activity);
	}

	public GodotAmazon(Activity p_activity) {
		registerClass ("GodotAmazon", new String[] {
			"setPurchaseCallbackId", "querySkuDetails", "purchase", "isConnected"
		});

		activity = p_activity;
		context = activity.getApplicationContext();

		// Initiliaze singletons here
		inAppManager = InAppManager.getInstance(activity);
	}


	public void setPurchaseCallbackId(final int instance_id) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				inAppManager.init(instance_id);
			}
		});
	}

	public void querySkuDetails(final String[] list) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				inAppManager.querySkuDetails(list);
			}
		});
	}

	public void purchase(final String sku, final String transaction_id) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				inAppManager.purchase(sku);
			}
		});
	}

	public boolean isConnected() {
		return inAppManager.isConnected();
	}

	protected void onMainActivityResult (int requestCode, int resultCode, Intent data) {
		inAppManager.onActivityResult(requestCode, resultCode, data);
	}

	protected void onMainPause () {
		inAppManager.onPause();
	}

	protected void onMainResume () {
		inAppManager.onResume();
	}

	protected void onMainDestroy () {
		inAppManager.onStop();
	}
}
