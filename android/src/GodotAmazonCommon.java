package org.godotengine.godot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class GodotAmazonCommon {
	protected int instance_id;
	protected Activity activity = null;
	protected Context context = null;

	public void init(final int p_instance_id) {
		this.instance_id = p_instance_id;

		onStart();
	}

	public void onStart() {
		// Nothing to do
	}

	public void onPause() {
		// Nothing to do
	}

	public void onResume() {
		// Nothing to do
	}

	public void onStop() {
		activity = null;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Nothing to do
	}
}