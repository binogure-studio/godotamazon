package org.godotengine.godot.inapp;

import java.util.HashSet;
import java.util.Set;

import android.util.Log;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.RequestId;
import com.amazon.device.iap.model.UserDataResponse;

public class InAppPurchasingListener implements PurchasingListener {

	private static final String TAG = "InAppPurchasingListener";

	private final InAppManager inAppManager;

	public InAppPurchasingListener(final InAppManager inAppManager) {
		this.inAppManager = inAppManager;
	}

	@Override
	public void onUserDataResponse(final UserDataResponse response) {
		Log.d(TAG, "onGetUserDataResponse: requestId (" + response.getRequestId()
					 + ") userIdRequestStatus: "
					 + response.getRequestStatus()
					 + ")");

		final UserDataResponse.RequestStatus status = response.getRequestStatus();
		switch (status) {
		case SUCCESSFUL:
			Log.d(TAG, "onUserDataResponse: get user id (" + response.getUserData().getUserId()
						 + ", marketplace ("
						 + response.getUserData().getMarketplace()
						 + ") ");
			inAppManager.setAmazonUserId(response.getUserData().getUserId(), response.getUserData().getMarketplace());
			break;

		case FAILED:
		case NOT_SUPPORTED:
			Log.d(TAG, "onUserDataResponse failed, status code is " + status);
			inAppManager.setAmazonUserId(null, null);
			break;
		}
	}

	@Override
	public void onProductDataResponse(final ProductDataResponse response) {
		final ProductDataResponse.RequestStatus status = response.getRequestStatus();
		Log.d(TAG, "onProductDataResponse: RequestStatus (" + status + ")");

		switch (status) {
		case SUCCESSFUL:
			Log.d(TAG, "onProductDataResponse: successful.	The item data map in this response includes the valid SKUs");
			final Set<String> unavailableSkus = response.getUnavailableSkus();
			Log.d(TAG, "onProductDataResponse: " + unavailableSkus.size() + " unavailable skus");

			inAppManager.addSkuDetail(response.getProductData());
			inAppManager.completeSkuDetail();
			break;
		case FAILED:
		case NOT_SUPPORTED:
			Log.d(TAG, "onProductDataResponse: failed, should retry request");
			inAppManager.errorSkuDetail("RequestStatus (" + status + ")");
			break;
		}
	}

	@Override
	public void onPurchaseUpdatesResponse(final PurchaseUpdatesResponse response) {
		final RequestId requestId = response.getRequestId();
		Log.d(TAG, "onPurchaseUpdatesResponse: requestId (" + requestId
					 + ") purchaseUpdatesResponseStatus ("
					 + response.getRequestStatus()
					 + ") userId ("
					 + response.getUserData().getUserId()
					 + ")");
		final PurchaseUpdatesResponse.RequestStatus status = response.getRequestStatus();
		switch (status) {
		case SUCCESSFUL:
			inAppManager.setAmazonUserId(response.getUserData().getUserId(), response.getUserData().getMarketplace());
			for (final Receipt receipt : response.getReceipts()) {
				inAppManager.handleReceipt(receipt, requestId, response.getUserData());
			}
			if (response.hasMore()) {
				PurchasingService.getPurchaseUpdates(false);
			}

			break;
		case FAILED:
		case NOT_SUPPORTED:
			Log.d(TAG, "onProductDataResponse: failed, should retry request");
			inAppManager.purchaseFailed();
			break;
		}

	}

	@Override
	public void onPurchaseResponse(final PurchaseResponse response) {
		final RequestId requestId = response.getRequestId();
		final String userId = response.getUserData().getUserId();
		final PurchaseResponse.RequestStatus status = response.getRequestStatus();
		Log.d(TAG, "onPurchaseResponse: requestId (" + requestId
					 + ") userId ("
					 + userId
					 + ") purchaseRequestStatus ("
					 + status
					 + ")");

		switch (status) {
		case SUCCESSFUL:
			final Receipt receipt = response.getReceipt();

			inAppManager.setAmazonUserId(response.getUserData().getUserId(), response.getUserData().getMarketplace());
			Log.d(TAG, "onPurchaseResponse: receipt json:" + receipt.toJSON());
			inAppManager.handleReceipt(receipt, requestId, response.getUserData());
			break;
		case ALREADY_PURCHASED:
			Log.d(TAG, "onPurchaseResponse: already purchased.");
			// This is not applicable for consumable item. It is only
			// application for entitlement and subscription.
			// check related samples for more details.
			inAppManager.purchaseFailed();
			break;
		case INVALID_SKU:
			Log.d(TAG, "onPurchaseResponse: invalid SKU: " + response.getReceipt().getSku());
			inAppManager.purchaseFailed();
			break;
		case FAILED:
		case NOT_SUPPORTED:
			Log.d(TAG, "onPurchaseResponse: failed to purchase " + response.getReceipt().getSku());

			inAppManager.purchaseFailed();
			break;
		}

	}

}
