package org.godotengine.godot.gamecircle;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.GodotAmazonCommon;
import org.godotengine.godot.GodotLib;

import com.amazon.ags.api.player.Player;
import com.amazon.ags.api.player.RequestPlayerResponse;
import com.amazon.ags.api.player.AGSignedInListener;
import com.amazon.ags.api.player.PlayerClient;
import com.amazon.ags.api.AGResponseHandle;
import com.amazon.ags.api.AmazonGamesClient;
import com.amazon.ags.api.AGResponseCallback;

public class GameCircleAuthentication extends GodotAmazonCommon {
	private static GameCircleAuthentication mInstance = null;
	private static final String TAG = "GameCircleAuthentication";
	private Player currentPlayer = null;

	public static GameCircleAuthentication getInstance(Activity activity) {
		synchronized (GameCircleAuthentication.class) {
			if (mInstance == null) {
				mInstance = new GameCircleAuthentication(activity);
			}
		}

		return mInstance;
	}

	public GameCircleAuthentication(Activity activity) {
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}

	private AmazonGamesClient getAmazonGamesClient() {
		GameCircleClient gameCircleClient = GameCircleClient.getInstance(activity);

		return gameCircleClient.getAmazonGamesClient();
	}

	private PlayerClient getPlayerClient() {
		GameCircleClient gameCircleClient = GameCircleClient.getInstance(activity);

		return gameCircleClient.getAmazonGamesClient().getPlayerClient(); 
	}

	public boolean isSignedIn() {
		boolean signedIn = false;
		PlayerClient playerClient = getPlayerClient();

		if (playerClient != null) {
			signedIn = playerClient.isSignedIn();
		}

		return signedIn;
	}

	public void onConnected() {
		PlayerClient playerClient = getPlayerClient();

		AGResponseHandle<RequestPlayerResponse> requestPlayerHandler = playerClient.getLocalPlayer();

		requestPlayerHandler.setCallback(new AGResponseCallback<RequestPlayerResponse>() {
			@Override
			public void onComplete(RequestPlayerResponse playerResponse) {
				String playerName = "";

				if (playerResponse.isError()) {
					String message = "Unlock failed due to: " + playerResponse.toString();

					Log.w(TAG, message);
					GodotLib.calldeferred(instance_id, "amazon_achievement_unlock_failed", new Object[] { message });
				} else {
					currentPlayer = playerResponse.getPlayer();
					playerName = currentPlayer.getAlias();
				}

				GodotLib.calldeferred(instance_id, "amazon_auth_connected", new Object[] { playerName });
			}
		});
	}

	public JSONObject getCurrentPlayer() {
		JSONObject userDetails = new JSONObject();

		if (currentPlayer != null) {
			try {
				userDetails.put("amazon.com:name", currentPlayer.getAlias());
				userDetails.put("amazon.com:uid", currentPlayer.getPlayerId());

				String avatarUrl = currentPlayer.getAvatarUrl();

				if (avatarUrl != null) {
					userDetails.put("amazon.com:photo_uri", avatarUrl);
				}

			} catch (JSONException e) {
				Log.w(TAG, "Failed to load player's game informations: " + e);
			}
		}

		return userDetails;
	}

	public void onDisconnected() {
		currentPlayer = null;

		GodotLib.calldeferred(instance_id, "amazon_auth_disconnected", new Object[] { });
	}

	public void signIn() {
		if (!isSignedIn()) {
			AmazonGamesClient amazonGamesClient = getAmazonGamesClient();

			if (amazonGamesClient != null) {
				amazonGamesClient.showSignInPage();
			} else {
				String message = "Cannot sign in since AmazonGamesClient is null.";

				Log.w(TAG, message);
				GodotLib.calldeferred(instance_id, "amazon_auth_connect_failed", new Object[] { message });
			}
		} else {
			onConnected();
		}
	}

	public void signOut() {
		if (isSignedIn()) {
			AmazonGamesClient amazonGamesClient = getAmazonGamesClient();

			if (amazonGamesClient != null) {
				amazonGamesClient.showGameCircle();
			} else {
				Log.i(TAG, "Already disconnected since amazonGamesClient is null.");

				onDisconnected();
			}
		} else {
			onDisconnected();
		}
	}
}
