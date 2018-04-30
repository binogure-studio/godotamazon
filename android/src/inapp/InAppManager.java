package org.godotengine.godot.inapp;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.GodotAmazonCommon;
import org.godotengine.godot.GodotLib;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.RequestId;
import com.amazon.device.iap.model.UserData;

public class InAppManager extends GodotAmazonCommon {

	private static InAppManager mInstance = null;
	private static final String CONSUMED = "CONSUMED";
	private static final String REMAINING = "REMAINING";
	private static final String TAG = "InAppManager";
	private UserIapData userIapData;
	private Dictionary mSkuDetails = new Dictionary();
	private HashMap<Integer, String> hashCodes = new HashMap<Integer, String>();

	public static InAppManager getInstance(Activity activity) {
		synchronized (InAppManager.class) {
			if (mInstance == null) {
				mInstance = new InAppManager(activity);
			}
		}

		return mInstance;
	}

	public InAppManager(Activity activity) {
		this.activity = activity;
		this.context = activity.getApplicationContext();

		onStart();
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onCreate: registering PurchasingListener");
		final InAppPurchasingListener purchasingListener = new InAppPurchasingListener(this);

		PurchasingService.registerListener(context, purchasingListener);
		Log.d(TAG, "IS_SANDBOX_MODE: " + PurchasingService.IS_SANDBOX_MODE);
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume: call getUserData");
		PurchasingService.getUserData();
		Log.d(TAG, "onResume: getPurchaseUpdates");
		PurchasingService.getPurchaseUpdates(false);
	}

	// query in app item detail info
	public void querySkuDetails(final String[] list) {
		List<String> nKeys = Arrays.asList(list);
		List<String> cKeys = Arrays.asList(mSkuDetails.get_keys());
		ArrayList<String> fKeys = new ArrayList<String>();

		for (String key : nKeys) {
			if (!cKeys.contains(key)) {
				fKeys.add(key);
			}
		}

		if (fKeys.size() > 0) {
			Set<String> productSkus = new HashSet<String>(fKeys);

			PurchasingService.getProductData(productSkus);
		} else {
			completeSkuDetail();
		}
	}

	public void addSkuDetail(Map<String, Product> productDatas) {
		for (Map.Entry<String, Product> entry : productDatas.entrySet()) {
			Dictionary item = new Dictionary();
			Product product = entry.getValue();

			item.put("type", product.getProductType().toString());
			item.put("product_id", product.getSku());
			item.put("title", product.getTitle());
			item.put("description", product.getDescription());
			item.put("price", product.getPrice());

			// Those values are not valid
			item.put("price_currency_code", product.getPrice());
			item.put("price_amount", 0.000001d);

			mSkuDetails.put(item.get("product_id").toString(), item);
		}

	}

	public void completeSkuDetail() {
		GodotLib.calldeferred(instance_id, "sku_details_complete", new Object[] { mSkuDetails });
	}

	public void errorSkuDetail(String errorMessage) {
		GodotLib.calldeferred(instance_id, "sku_details_error", new Object[] { errorMessage });
	}

	public void setAmazonUserId(final String newAmazonUserId, final String newAmazonMarketplace) {
		// Reload everything if the Amazon user has changed.
		if (newAmazonUserId == null) {
			// A null user id typically means there is no registered Amazon
			// account.
			if (userIapData != null) {
				userIapData = null;
			}
		} else if (userIapData == null || !newAmazonUserId.equals(userIapData.getAmazonUserId())) {
			// If there was no existing Amazon user then either no customer was
			// previously registered or the application has just started.

			// If the user id does not match then another Amazon user has
			// registered.
			userIapData = reloadUserData(newAmazonUserId, newAmazonMarketplace);
		}
	}

	public void handleConsumablePurchase(final Receipt receipt, final UserData userData) {
		try {
			if (receipt.isCanceled()) {
				purchaseCancelled();
			} else {
				// 'hashCode' validation
				int hashCode = receipt.hashCode();
				String sku = hashCodes.get(hashCode);

				if (receipt.getSku().equals(sku)) {
					purchaseSuccess(receipt.getReceiptId(), String.valueOf(receipt.hashCode()), receipt.getSku());
				} else {
					Log.e(TAG, "Cannot validate the purchase (invalid hashCode " + String.valueOf(receipt.hashCode()) + ", " + receipt.getSku() + ")");

					purchaseFailed();
				}
			}
			return;
		} catch (final Throwable e) {
			Log.e(TAG, "Purchase failed: " + e.getMessage());

			purchaseFailed();
		}
	}

	public void handleReceipt(final Receipt receipt, final UserData userData) {
		switch (receipt.getProductType()) {
		case CONSUMABLE:
			handleConsumablePurchase(receipt, userData);
			break;
		case ENTITLED:
			Log.e(TAG, "Entitled are not implemented.");

			purchaseFailed();
			break;
		case SUBSCRIPTION:
			Log.e(TAG, "Subscriptions are not implemented.");

			purchaseFailed();
			break;
		}

	}

	public void purchaseCancelled() {
		GodotLib.calldeferred(instance_id, "purchase_cancel", new Object[] { });
	}

	public void purchaseFailed() {
		GodotLib.calldeferred(instance_id, "purchase_fail", new Object[] { });
	}

	public void purchaseSuccess(String ticket, String signature, String sku) {
		GodotLib.calldeferred(instance_id, "purchase_success", new Object[] { ticket, signature, sku });
	}

	public UserIapData getUserIapData() {
		return this.userIapData;
	}

	private UserIapData reloadUserData(final String amazonUserId, final String amazonMarketplace) {
		return new UserIapData(amazonUserId, amazonMarketplace);
	}

	public void purchase(final String sku) {
		RequestId requestId = PurchasingService.purchase(sku);

		Log.d(TAG, "Purchasing: " + sku + ", " + String.valueOf(requestId.hashCode()));

		hashCodes.put(requestId.hashCode(), sku);
	}
}
