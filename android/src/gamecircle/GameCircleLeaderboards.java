package org.godotengine.godot.gamecircle;

import android.app.Activity;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.GodotAmazonCommon;
import org.godotengine.godot.GodotLib;

import org.json.JSONObject;
import org.json.JSONException;

import com.amazon.ags.api.leaderboards.GetPlayerScoreResponse;
import com.amazon.ags.api.leaderboards.LeaderboardsClient;
import com.amazon.ags.api.AGResponseHandle;
import com.amazon.ags.api.AGResponseCallback;
import com.amazon.ags.api.leaderboards.SubmitScoreResponse;
import com.amazon.ags.constants.LeaderboardFilter;
import com.amazon.ags.api.leaderboards.GetScoresResponse;
import com.amazon.ags.api.leaderboards.Score;
import com.amazon.ags.api.player.Player;

public class GameCircleLeaderboards extends GodotAmazonCommon {
	private static GameCircleLeaderboards mInstance = null;
	private static final String TAG = "GameCircleLeaderboards";

	public static GameCircleLeaderboards getInstance(Activity activity) {
		synchronized (GameCircleLeaderboards.class) {
			if (mInstance == null) {
				mInstance = new GameCircleLeaderboards(activity);
			}
		}

		return mInstance;
	}

	public GameCircleLeaderboards(Activity activity) {
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}

	private LeaderboardsClient getLeaderboardClient() {
		GameCircleClient gameCircleClient = GameCircleClient.getInstance(activity);
		AmazonGamesClient amazonGamesClient = gameCircleClient.getAmazonGamesClient();
		LeaderboardsClient leaderboardsClient = null;

		if (amazonGamesClient != null) {
			leaderboardsClient = leaderboardsClient.getPlayerClient();
		}

		return leaderboardsClient;
	}

	public void getTopScores(final String id, final LeaderboardFilter time_span, final int amount) {
		LeaderboardsClient leaderboardsClient = getLeaderboardClient();

		if (leaderboardsClient != null) {
			 AGResponseHandle<GetScoresResponse> scoresHandler = leaderboardsClient.getScores(id, time_span);

			 scoresHandler.setCallback(new AGResponseCallback<GetScoresResponse>() {
				@Override
				public void onComplete(GetScoresResponse scoresResponse) {
					if (scoresResponse.isError()) {
						String message = "Submit score failed due to: " + scoresResponse.toString();

						Log.w(TAG, message);
						GodotLib.calldeferred(instance_id, "amazon_leaderboard_load_top_scores_failed", new Object[] { message });
					} else {
						JSONObject leaderboard_result = new JSONObject();
						List<Score> leaderboardScores = scoresResponse.getScores();

						try {
							for (Score leaderboardScore : leaderboardScores) {
								Player player = leaderboardScore.getPlayer();
								String displayName = player.getAlias();
								String avatarUrl = player.getAvatarUrl();

								if (displayName == null) {
									displayName = "Unkown player";
									avatarUrl = "null";
								}

								leaderboard_result.put(String.valueOf(leaderboardScore.getRank()) + ":score", leaderboardScore.getScoreValue());
								leaderboard_result.put(String.valueOf(leaderboardScore.getRank()) + ":photo_uri", avatarUrl);
								leaderboard_result.put(String.valueOf(leaderboardScore.getRank()) + ":name", displayName);
							}
						} catch (JSONException e) {
							Log.w(TAG, "Failed to load player's game informations: " + e);
						}

						// Continue game flow.
						GodotLib.calldeferred(instance_id, "amazon_leaderboard_loaded_top_score", new Object[] { leaderboard_result.toString() });
					}
				}
			 });
		} else {
			String message = "Amazon not connected";

			GodotLib.calldeferred(instance_id, "amazon_leaderboard_load_top_scores_failed", new Object[] { message });
		}
	}

	public void getPlayerScore(final String id, final LeaderboardFilter time_span) {
		LeaderboardsClient leaderboardsClient = getLeaderboardClient();

		if (leaderboardsClient != null) {
			 AGResponseHandle<GetPlayerScoreResponse> playerScoreHandler = leaderboardsClient.getLocalPlayerScore(id, time_span);

			 playerScoreHandler.setCallback(new AGResponseCallback<GetPlayerScoreResponse>() {
				@Override
				public void onComplete(GetPlayerScoreResponse playerScoreResponse) {
					if (playerScoreResponse.isError()) {
						String message = "Submit score failed due to: " + playerScoreResponse.toString();

						Log.w(TAG, message);
						GodotLib.calldeferred(instance_id, "amazon_leaderboard_load_score_failed", new Object[] { message });
					} else {
						int rank = playerScoreResponse.getRank();
						long score = playerScoreResponse.getScoreValue();

						// Continue game flow.
						GodotLib.calldeferred(instance_id, "amazon_leaderboard_loaded_score", new Object[] { score, rank });
					}
				}
			 });
		} else {
			String message = "Amazon not connected";

			GodotLib.calldeferred(instance_id, "amazon_leaderboard_load_score_failed", new Object[] { message });
		}
	}

	public void submitScore(final String id, final long score) {
		// Replace YOUR_LEADERBOARD_ID with an actual leaderboard ID from your game.
		LeaderboardsClient leaderboardsClient = getLeaderboardClient();

		if (leaderboardsClient != null) {
			AGResponseHandle<SubmitScoreResponse> submitScoreHandler = leaderboardsClient.submitScore(id, score);

			submitScoreHandler.setCallback(new AGResponseCallback<SubmitScoreResponse>() {
				@Override
				public void onComplete(SubmitScoreResponse submitScoreResponse) {
					if (submitScoreResponse.isError()) {
						String message = "Submit score failed due to: " + submitScoreResponse.toString();

						Log.w(TAG, message);
						GodotLib.calldeferred(instance_id, "amazon_leaderboard_submit_failed", new Object[] { message });
					} else {
						// Continue game flow.
						GodotLib.calldeferred(instance_id, "amazon_leaderboard_submitted", new Object[] { });
					}
				}
			});
		} else {
			String message = "Amazon not connected";

			GodotLib.calldeferred(instance_id, "amazon_leaderboard_submit_failed", new Object[] { message });
		}
	}
}
