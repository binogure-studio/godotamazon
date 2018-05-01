
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

import com.amazon.ags.constants.LeaderboardFilter;

import org.godotengine.godot.inapp.InAppManager;
import org.godotengine.godot.gamecircle.GameCircleClient;
import org.godotengine.godot.gamecircle.GameCircleSnapshot;
import org.godotengine.godot.gamecircle.GameCircleAchievements;
import org.godotengine.godot.gamecircle.GameCircleLeaderboards;
import org.godotengine.godot.Dictionary;

public class GodotAmazon extends Godot.SingletonBase {

	private static final String TAG = "GodotAmazon";
	private static Context context;
	private static Activity activity;

	private InAppManager inAppManager;
	private GameCircleClient gameCircleClient;
	private GameCircleSnapshot gameCircleSnapshot;
	private GameCircleAchievements gameCircleAchievements;
	private GameCircleLeaderboards gameCircleLeaderboards;
	
	public static final Dictionary AMAZON_LEADERBOARD_TIMESPAN;

	static {
		AMAZON_LEADERBOARD_TIMESPAN = new Dictionary();

		AMAZON_LEADERBOARD_TIMESPAN.put("TIME_SPAN_WEEKLY", Integer.valueOf(LeaderboardFilter.GLOBAL_WEEK.ordinal()));
		AMAZON_LEADERBOARD_TIMESPAN.put("TIME_SPAN_ALL_TIME", Integer.valueOf(LeaderboardFilter.GLOBAL_ALL_TIME.ordinal()));
		AMAZON_LEADERBOARD_TIMESPAN.put("TIME_SPAN_DAILY", Integer.valueOf(LeaderboardFilter.GLOBAL_DAY.ordinal()));
	};

	static public Godot.SingletonBase initialize (Activity p_activity) {
		return new GodotAmazon(p_activity);
	}

	public GodotAmazon(Activity p_activity) {
		registerClass ("GodotAmazon", new String[] {
			"setPurchaseCallbackId", "querySkuDetails", "purchase", "isConnected", "requestPurchased",

			// GameCircleClient
			"amazon_initialize", "amazon_connect", "amazon_is_connected",

			// GameCircleSnapshot
			"amazon_snapshot_load", "amazon_amazon_snapshot_save",

			// GameCircleAchievement
			"amazon_achievement_show_list", "amazon_achievement_unlock", "amazon_achievement_increment",

			// GameCircleLeaderboard
			"amazon_leaderboard_load_player_score", "amazon_leaderboard_submit", "amazon_leaderboard_load_top_scores", "get_amazon_leaderboard_timespan"
		});

		activity = p_activity;
		context = activity.getApplicationContext();

		// Initiliaze singletons here
		inAppManager = InAppManager.getInstance(activity);

		// Order matters
		gameCircleClient = GameCircleClient.getInstance(activity);
		gameCircleSnapshot = GameCircleSnapshot.getInstance(activity);
		gameCircleAchievements = GameCircleAchievements.getInstance(activity);
		gameCircleLeaderboards = GameCircleLeaderboards.getInstance(activity);
	}

	public Dictionary get_amazon_leaderboard_timespan() {
		return AMAZON_LEADERBOARD_TIMESPAN;
	}

	public void amazon_initialize(final int instance_id) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				gameCircleClient.init(instance_id);
				gameCircleSnapshot.init(instance_id);
				gameCircleAchievements.init(instance_id);
				gameCircleLeaderboards.init(instance_id);
			}
		});
	}

	public void amazon_leaderboard_load_player_score(final String id, final int time_span) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				gameCircleLeaderboards.getPlayerScore(id, LeaderboardFilter.fromOrdinal(time_span));
			}
		});
	}
	public void amazon_leaderboard_submit(final String id, final int score) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				gameCircleLeaderboards.submitScore(id, score);
			}
		});
	}
	public void amazon_leaderboard_load_top_scores(final String id, final int time_span, final int amount, final boolean force) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				gameCircleLeaderboards.getTopScores(id, LeaderboardFilter.fromOrdinal(time_span), amount);
			}
		});
	}

	public void amazon_achievement_show_list() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				gameCircleAchievements.showAchievements();
			}
		});
	}

	public void amazon_achievement_unlock(final String id) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				gameCircleAchievements.unlockAchievement(id);
			}
		});
	}

	public void amazon_achievement_increment(final String id, final float percent) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				gameCircleAchievements.increaseAchievement(id, percent);
			}
		});
	}


	// Amazon snapshots
	public void amazon_snapshot_load(final String snapshotName, final int conflictResolutionPolicy) {
		gameCircleSnapshot.snapshot_load(snapshotName, conflictResolutionPolicy);
	}

	public void amazon_snapshot_save(final String snapshotName, final String data, final String description, final boolean flag_force) {
		gameCircleSnapshot.snapshot_save(snapshotName, data, description, flag_force);
	}

	public void amazon_connect() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				gameCircleClient.connect();
			}
		});
	}

	public boolean amazon_is_connected() {
		return gameCircleClient.isConnected();
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

	public void requestPurchased() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				inAppManager.requestPurchased();
			}
		});
	}

	public boolean isConnected() {
		return inAppManager.isConnected();
	}

	protected void onMainActivityResult (int requestCode, int resultCode, Intent data) {
		inAppManager.onActivityResult(requestCode, resultCode, data);

		gameCircleClient.onActivityResult(requestCode, resultCode, data);
		gameCircleSnapshot.onActivityResult(requestCode, resultCode, data);
		gameCircleAchievements.onActivityResult(requestCode, resultCode, data);
		gameCircleLeaderboards.onActivityResult(requestCode, resultCode, data);
	}

	protected void onMainPause () {
		inAppManager.onPause();

		gameCircleClient.onPause();
		gameCircleSnapshot.onPause();
		gameCircleAchievements.onPause();
		gameCircleLeaderboards.onPause();
	}

	protected void onMainResume () {
		inAppManager.onResume();

		gameCircleClient.onResume();
		gameCircleSnapshot.onResume();
		gameCircleAchievements.onResume();
		gameCircleLeaderboards.onResume();
	}

	protected void onMainDestroy () {
		inAppManager.onStop();

		gameCircleClient.onStop();
		gameCircleSnapshot.onStop();
		gameCircleAchievements.onStop();
		gameCircleLeaderboards.onStop();
	}
}
