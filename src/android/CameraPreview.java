package com.tuxpan.foregroundcameragalleryplugin;

import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private final String TAG = "Preview";
	private List<Camera.Size> mSupportedPreviewSizes;
	private Camera.Size mPreviewSize;

	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
		for (Camera.Size str : mSupportedPreviewSizes)
			Log.e(TAG, str.width + "/" + str.height);
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		// try {
		// mCamera.setPreviewDisplay(holder);
		// mCamera.startPreview();
		// } catch (IOException e) {
		// Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		// }
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// empty. Take care of releasing the Camera preview in your activity.
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.
		Log.e(TAG, "surfaceChanged => w=" + w + ", h=" + h);

		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		// start preview with new settings
		try {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			mCamera.setParameters(parameters);
			mCamera.setDisplayOrientation(90);
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();

		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, parentWidth, parentHeight);
		}

		float ratio;
		if (mPreviewSize.height >= mPreviewSize.width)
			ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
		else
			ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

		// One of these methods should be used, second method squishes preview
		// slightly
		setMeasuredDimension(parentWidth, (int) (parentWidth * ratio));
		// setMeasuredDimension((int) (width * ratio), height);
	}

	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) h / w;

		if (sizes == null)
			return null;

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Camera.Size size : sizes) {
			double ratio = (double) size.height / size.width;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;

			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		return optimalSize;
	}
}