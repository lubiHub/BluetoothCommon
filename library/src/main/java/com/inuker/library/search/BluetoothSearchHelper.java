package com.inuker.library.search;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.inuker.library.utils.BluetoothLog;
import com.inuker.library.utils.BluetoothUtils;

/**
 * @author liwentian
 */
public class BluetoothSearchHelper {

	private static final int MSG_START_SEARCH = 0x10;
	private static final int MSG_CANCEL_SEARCH = 0x20;

	private HandlerThread mWorkerThread;
	private Handler mBluetoothSearchHandler;

	private BluetoothSearchRequest mCurrentRequest;

	public void startSearch(BluetoothSearchRequest request,
			BluetoothSearchResponse response) {
		if (request == null || response == null) {
			return;
		}

		BluetoothSearchResponseWrapper wrapper = new BluetoothSearchResponseWrapper(
				response);
		request.setSearchResponse(wrapper);

		if (BluetoothUtils.isBluetoothEnabled()) {
			mBluetoothSearchHandler.obtainMessage(MSG_START_SEARCH, request)
					.sendToTarget();
		} else {
			cancelSearch(request);
		}
	}

	public boolean isSearching() {
		return mCurrentRequest != null;
	}

	public void cancelSearch(BluetoothSearchRequest request) {
        mBluetoothSearchHandler.obtainMessage(MSG_CANCEL_SEARCH, request)
                .sendToTarget();
	}

	private void processStartSearch(BluetoothSearchRequest request) {
		if (mCurrentRequest != null) {
			mCurrentRequest.cancel();
			mCurrentRequest = request;
			mCurrentRequest.start();
		} else {
			mCurrentRequest = request;
			mCurrentRequest.start();
		}
	}

	private void processCancelSearch(BluetoothSearchRequest request) {
		if (mCurrentRequest != null) {
			if (mCurrentRequest == request || request == null) {
				mCurrentRequest.cancel();
				mCurrentRequest = null;
			}
		} else if (request != null) {
			request.cancel();
		}
	}

	private class BluetoothSearchResponseWrapper implements
			BluetoothSearchResponse {

		private BluetoothSearchResponse mResponse;

		private BluetoothSearchResponseWrapper(BluetoothSearchResponse response) {
			mResponse = response;
		}

		@Override
		public void onSearchStarted() {
			// TODO Auto-generated method stub
			BluetoothLog.d("Bluetooth search start");
			BluetoothSearchResponser.getInstance().notifySearchStarted(
					mResponse);
		}

		@Override
		public void onDeviceFounded(BluetoothSearchResult device) {
			// TODO Auto-generated method stub
			BluetoothDeviceHandler.getInstance().notifyDeviceFounded(device,
					mResponse);
		}

		@Override
		public void onSearchStopped() {
			// TODO Auto-generated method stub
			mCurrentRequest = null;
			BluetoothLog.d("Bluetooth search stop");
			BluetoothSearchResponser.getInstance().notifySearchStopped(
					mResponse);
		}

		@Override
		public void onSearchCanceled() {
			// TODO Auto-generated method stub
			mCurrentRequest = null;
			BluetoothLog.d("Bluetooth search cancel");
			BluetoothSearchResponser.getInstance().notifySearchCanceled(
					mResponse);
		}

	}

	private BluetoothSearchHelper() {
		mWorkerThread = new HandlerThread("BluetoothSearch");
		mWorkerThread.start();

		mBluetoothSearchHandler = new Handler(mWorkerThread.getLooper()) {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				BluetoothSearchRequest request = (BluetoothSearchRequest) msg.obj;

				switch (msg.what) {
				case MSG_START_SEARCH:
					processStartSearch(request);
					break;

				case MSG_CANCEL_SEARCH:
					processCancelSearch(request);
					break;

				default:
					break;
				}
			}

		};
	}

	public static BluetoothSearchHelper getInstance() {
		return BluetoothSearchManagerHolder.instance;
	}

	private static class BluetoothSearchManagerHolder {
		private static BluetoothSearchHelper instance = new BluetoothSearchHelper();
	}
}
