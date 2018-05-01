package org.godotengine.godot.gamecircle;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.GodotAmazonCommon;
import org.godotengine.godot.GodotLib;

import com.amazon.ags.api.AmazonGamesClient;

import com.amazon.ags.api.whispersync.model.SyncableDeveloperString;
import com.amazon.ags.api.whispersync.GameDataMap;
import com.amazon.ags.api.whispersync.WhispersyncClient;
import com.amazon.ags.api.whispersync.WhispersyncEventListener;


public class GameCircleSnapshot extends GodotAmazonCommon {
	private static GameCircleSnapshot mInstance = null;
	private static final String TAG = "GameCircleSnapshot";
	private WhispersyncClient whispersyncClient = null;
	private GameDataMap gameDataMap;
	private String snapshotNameToLoad;
	
	public static GameCircleSnapshot getInstance(Activity activity) {
		synchronized (GameCircleSnapshot.class) {
			if (mInstance == null) {
				mInstance = new GameCircleSnapshot(activity);
			}
		}

		return mInstance;
	}

	public GameCircleSnapshot(Activity activity) {
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}

	@Override
	public void onStart() {
		this.whispersyncClient = AmazonGamesClient.getWhispersyncClient();
		this.gameDataMap = whispersyncClient.getGameData();

		whispersyncClient.setWhispersyncEventListener(new WhispersyncEventListener() {
			public void onNewCloudData() {
				Log.d(TAG, "Data loaded from the cloud (" + snapshotNameToLoad + ")");

				if (snapshotNameToLoad != null) {
					SyncableDeveloperString savegame = gameDataMap.getDeveloperString(snapshotNameToLoad);

					if (savegame.inConflict()) {
						String data = savegame.getCloudValue();

						// If there is a conflict, send back the cloud value,
						// so the developper would be able to resolve conflicts
						Log.i(TAG, "Data loaded from the cloud are conflicting with local datas");

						GodotLib.calldeferred(instance_id, "amazon_snapshot_loaded", new Object[] { data });
					} else if (savegame.isSet()) {
						String data = savegame.getValue();

						GodotLib.calldeferred(instance_id, "amazon_snapshot_loaded", new Object[] { data });
					} else {
						GodotLib.calldeferred(instance_id, "amazon_snapshot_loaded", new Object[] { "" });
					}
				}
			}

			public void onDataUploadedToCloud() {
				Log.d(TAG, "Data uploaded to the cloud");

				GodotLib.calldeferred(instance_id, "amazon_snapshot_saved", new Object[] { });
			}

			public void onThrottled() {
			}

			public void onDiskWriteComplete() {
			}
		});
	}

	public void snapshot_load(final String snapshotName, final int conflictResolutionPolicy) {
		snapshotNameToLoad = snapshotName;

		SyncableDeveloperString savegame = gameDataMap.getDeveloperString(snapshotName);

		if (savegame.inConflict()) {
			String data = savegame.getCloudValue();

			// If there is a conflict, send back the cloud value,
			// so the developper would be able to resolve conflicts
			Log.i(TAG, "Data loaded from the cloud are conflicting with local datas");

			GodotLib.calldeferred(instance_id, "amazon_snapshot_loaded", new Object[] { data });
		} else if (savegame.isSet()) {
			String data = savegame.getValue();

			GodotLib.calldeferred(instance_id, "amazon_snapshot_loaded", new Object[] { data });
		}

		whispersyncClient.synchronize();
	}

	public void snapshot_save(final String snapshotName, final String data, final String description, final boolean force) {
		SyncableDeveloperString savegame = gameDataMap.getDeveloperString(snapshotName);

		savegame.setValue(data);

		// Resolve conflicts
		if (savegame.inConflict()) {
			if (force) {
				savegame.markAsResolved();

				whispersyncClient.synchronize();
			} else {
				String message = "Cannot save the game data due to a conflict";

				Log.w(TAG, message);
				GodotLib.calldeferred(instance_id, "amazon_snapshot_save_failed", new Object[] { message });
			}
		} else {
			whispersyncClient.synchronize();
		}
	}
}