package com.android.Aage;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

//import com.android.age.R;
//import com.cfzz.wy.AfCameraTestActivity;

/**
 * Description: <br/>
 * site: <a href="http://www.crazyit.org">crazyit.org</a> <br/>
 * Copyright (C), 2001-2012, Yeeku.H.Lee <br/>
 * This program is protected by copyright laws. <br/>
 * Program Name: <br/>
 * Date:
 * 
 * @author Yeeku.H.Lee kongyeeku@163.com
 * @version 1.0
 */
public class CameraDialog extends Activity {
	SurfaceView sView;
	SurfaceHolder surfaceHolder;
	int screenWidth, screenHeight;
	//int w, h;
	// 定义系统所用的照相机
	public static Camera camera = null;
	public static CameraDialog instance = null;
	// 是否在浏览中
	boolean isPreview = false;
	//private static final int BACK_CAMERA = 0;
	//private static final int FRONT_CAMERA = 1;
	private static String TAG = "HIPPO_DEBUG";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// 设置全屏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera_dialog);
		instance = this;
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		// 获取屏幕的宽和高
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		// 获取界面中SurfaceView组件
		sView = (SurfaceView) findViewById(R.id.sView);
		// 获得SurfaceView的SurfaceHolder
		surfaceHolder = sView.getHolder();
		// 为surfaceHolder添加一个回调监听器
		surfaceHolder.addCallback(new Callback() {
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}
			public void surfaceCreated(SurfaceHolder holder) {
				// 打开摄像头
				initCamera();
			}
			public void surfaceDestroyed(SurfaceHolder holder) {
				// 如果camera不为null ,释放摄像头
				if (camera != null) {
					if (isPreview)
						camera.stopPreview();
					camera.release();
					camera = null;
				}
			}
		});
		// 设置该SurfaceView自己不维护缓冲
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// 换成PowerManager.SCREEN_DIM_WAKE_LOCK会变暗）
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyCameraTest");
		wl.acquire();// 开启屏幕常亮
	}
	// 相机初始化方法
	public void initCamera()
	{
		if (!isPreview) {
			/* 若相机非?预览模式，则开启相机 */
			try {
				camera = Camera.open(CAMERA_FACING_FRONT);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
		if (camera != null && !isPreview) {
			try {
				// Log.i(TAG, "inside the camera");
				camera.setPreviewDisplay(surfaceHolder);
				/* 建立Camera.Parameters对象 */
				Camera.Parameters parameters = camera.getParameters();
				// SDK版本选择，兼容
				if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
					setDisplayOrientation(camera, 90);
				} else {
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
						parameters.set("orientation", "portrait");
						parameters.set("rotation", 90);
					}
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						parameters.set("orientation", "landscape");
						parameters.set("rotation", 90);
					}
				}
				/* 设定相片格式为JPEG */
				parameters.setPictureFormat(PixelFormat.JPEG);
				camera.setParameters(parameters);
				/* setPreviewDisplay唯几的参数为SurfaceHolder */
				camera.setPreviewDisplay(surfaceHolder);
				/* 立即运行预览*/
				camera.startPreview();
				isPreview = true;
				Log.i(TAG, "startPreview");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				camera.release();
				camera = null;
				Log.i(TAG, e.toString());
				e.printStackTrace();
			}
		}

	}
    //设置显示方向
	public void setDisplayOrientation(Camera camera, int angle) {
		Method downPolymorphic;
		try {
			downPolymorphic = camera.getClass().getMethod(
					"setDisplayOrientation", new Class[] { int.class });
			if (downPolymorphic != null)
				downPolymorphic.invoke(camera, new Object[] { angle });
		} catch (Exception e1) {
		}
	}

	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		// 当用户单击照相键、中央键时执行拍照
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_CAMERA:
			if (camera != null && event.getRepeatCount() == 0) {
				// 拍照
				camera.takePicture(null, null, myjpegCallback);
				return true;
			}
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	PictureCallback myjpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			
			// 重新浏览
			camera.stopPreview();
			camera.startPreview();
			isPreview = true;
		}
	};

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h)// 获取最优预览分辨率方法
	{
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;
		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;
		int targetHeight = h;
		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}
		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}*/
}