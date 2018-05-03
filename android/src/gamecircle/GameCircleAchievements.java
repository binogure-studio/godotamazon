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

import com.amazon.ags.api.achievements.Achievement;
import com.amazon.ags.api.achievements.AchievementsClient;
import com.amazon.ags.api.achievements.GetAchievementResponse;
import com.amazon.ags.api.achievements.UpdateProgressResponse;
import com.amazon.ags.api.AGResponseCallback;
import com.amazon.ags.api.AGResponseHandle;
import com.amazon.ags.api.AmazonGamesClient;

public class GameCircleAchievements extends GodotAmazonCommon {
	private static GameCircleAchievements mInstance = null;
	private static final String TAG = "GameCircleAchievements";

	public static GameCircleAchievements getInstance(Activity activity) {
		synchronized (GameCircleAchievements.class) {
			if (mInstance == null) {
				mInstance = new GameCircleAchievements(activity);
			}
		}

		return mInstance;
	}

	public GameCircleAchievements(Activity activity) {
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}

	private PlayerClient getAchievementClient() {
		GameCircleClient gameCircleClient = GameCircleClient.getInstance(activity);
		AmazonGamesClient amazonGamesClient = gameCircleClient.getAmazonGamesClient();
		AchievementsClient achievementsClient = null;

		if (amazonGamesClient != null) {
			achievementsClient = amazonGamesClient.getPlayerClient();
		}

		return achievementsClient;
	}

	public void unlockAchievement(final String id) {
		AchievementsClient achievementsClient = getAchievementClient();

		if (achievementsClient != null) {
			AGResponseHandle<UpdateProgressResponse> updateProgressHandler = achievementsClient.updateProgress(id, 100.0f);

			updateProgressHandler.setCallback(new AGResponseCallback<UpdateProgressResponse>() {
				@Override
				public void onComplete(UpdateProgressResponse progressResponse) {
					if (progressResponse.isError()) {
						String message = "Unlock failed due to: " + progressResponse.toString();

						Log.w(TAG, message);
						GodotLib.calldeferred(instance_id, "amazon_achievement_unlock_failed", new Object[] { message });
					} else {
						GodotLib.calldeferred(instance_id, "amazon_achievement_unlocked", new Object[] { id });
					}
				}
			});
		} else {
			String message = "Amazon not connected";

			GodotLib.calldeferred(instance_id, "amazon_achievement_unlock_failed", new Object[] { message });
		}
	}

	public void increaseAchievement(final String id, final float percent) {
		final AchievementsClient achievementsClient = getAchievementClient();

		if (achievementsClient != null) {
			AGResponseHandle<GetAchievementResponse> getAchievementHandler = achievementsClient.getAchievement(id);

			getAchievementHandler.setCallback(new AGResponseCallback<GetAchievementResponse>() {
				@Override
				public void onComplete(GetAchievementResponse achievementResponse) {
					if (achievementResponse.isError()) {
						String message = "Increase failed due to: " + achievementResponse.toString();

						Log.w(TAG, message);
						GodotLib.calldeferred(instance_id, "amazon_achievement_increment_failed", new Object[] { message });
					} else {
						Achievement achievement = achievementResponse.getAchievement();
						final float progress = Math.max(0.0f, Math.min(100.0f, achievement.getProgress() + percent));

						AGResponseHandle<UpdateProgressResponse> updateProgressHandler = achievementsClient.updateProgress(id, progress);

						updateProgressHandler.setCallback(new AGResponseCallback<UpdateProgressResponse>() {
							@Override
							public void onComplete(UpdateProgressResponse progressResponse) {
								if (progressResponse.isError()) {
									String message = "Increase failed due to: " + progressResponse.toString();

									Log.w(TAG, message);
									GodotLib.calldeferred(instance_id, "amazon_achievement_increment_failed", new Object[] { message });
								} else {
									GodotLib.calldeferred(instance_id, "amazon_achievement_increased", new Object[] { id, progress });
								}
							}
						});
					}
				}
			});
		} else {
			String message = "Amazon not connected";

			GodotLib.calldeferred(instance_id, "amazon_achievement_increment_failed", new Object[] { message });
		}
	}

	public void showAchievements() {
		AchievementsClient achievementsClient = getAchievementClient();

		achievementsClient.showAchievementsOverlay();
	}
}
