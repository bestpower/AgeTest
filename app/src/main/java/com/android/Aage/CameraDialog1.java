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
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.List;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;

//import com.android.age.R;

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
public class CameraDialog1 extends Activity {
	SurfaceView sView;
	SurfaceHolder surfaceHolder;
	int screenWidth, screenHeight;
	// 定义系统所用的照相机
	public static Camera camera = null;
	public static CameraDialog1 instance = null;
	// 是否在浏览中
	boolean isPreview = false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// 设置全屏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera_dialog1);
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
		// MyTag可以随便写,可以写应用名称等
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// 换成PowerManager.SCREEN_DIM_WAKE_LOCK会变暗）
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyCamera1Test");
		wl.acquire();// 开启屏幕常亮
	}

	private void initCamera() {
		if (!isPreview) {
			camera = Camera.open(CAMERA_FACING_BACK);
		}
		if (camera != null && !isPreview) {
			try {
				Camera.Parameters parameters = camera.getParameters();
				// 设置预览照片的大小
				// 设置显示图像旋转90度
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
				// 设置图片格式
				parameters.setPictureFormat(PixelFormat.JPEG);
				camera.setParameters(parameters);
				// 通过SurfaceView显示取景画面
				camera.setPreviewDisplay(surfaceHolder);
				// 开始预览
				camera.startPreview();
				// 自动对焦
				camera.autoFocus(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			isPreview = true;
		}
	}

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
			
			camera.stopPreview();
			camera.startPreview();
			isPreview = true;
		}
	};

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
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