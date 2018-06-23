package com.android.Aage;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Camera;
//import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
//import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;

//import com.android.age.R;
//import android.os.IHardwareService;

//import android.os.IHardwareService;

public class PressureTest extends Activity implements OnClickListener {

	private MediaPlayer mediaPlayer = null;
	public String TAG = "AdvancedAgeTest";

	private Timer vibratorTimer;//马达老化计时
	private Timer flashLightTimer;//闪光灯老化计时
	private Timer allFunctionTimer;//全功能测试计时
	private Timer allFunctionPreviewTimer;//全功能预览测试计时
	private Timer oneItemTimer;//老化单项计时
	
	private TimerTask vibratorTimerTask;//马达老化任务
	private TimerTask flashLightTimerTask;//闪光灯老化任务
	private TimerTask allFunctionTimerTask;//全功能测试任务
	private TimerTask allFunctionPreviewTimerTask;//全功能预览测试任务
	private TimerTask oneItemTimerTask;//老化单项任务
	private TimerTask mpTimerTask;//音频+图片轮播任务
	private TimerTask fbcTimerTask;//前+后摄像头切换任务
	private TimerTask vpTimerTask;//高清视频循环播放

	public static final int MUSIC_PHOTO = 0x0010;// 音乐图片轮播开启
	public static final int MP_CLOSE = 0x0020;// 音乐图片轮播关闭
	public static final int CAMERA_OPEN = 0x0030;// 前置摄像头开启
	public static final int FLASH_VIBRATE = 0x0040;// 闪光灯马达开启
	public static final int FV_CLOSE = 0x0050;// 闪光灯马达关闭
	public static final int ALL_CLOSE = 0x0060;// 提示框弹出
	public static final int CAMERA_CLOSE = 0x0070;// 前置摄像头关闭
	public static final int CAMERA_OPEN1 = 0x0080;// 后置摄像头开启
	public static final int CAMERA_CLOSE1 = 0x0090;// 后知摄像头关闭
	public static final int VEDIO_OPEN = 0x0011;// 视频开启
	public static final int VEDIO_CLOSE = 0x0012;// 视频关闭
	public static final int ONE_CLOSE = 0x0013;// 单项计时停止

	private Chronometer time;// 计时器
	private long recordingTime = 0;// 记录下来的总时间
	private long mExitTime = 0;//记录结束时间
	private AudioManager audioManager;//音频管理
	private EditText timelength;//测试时长
	private Handler allFunctionHandler;//全功能
	private Handler allFunctionPreviewHandler;//全功能预览
	private Handler vfHandler;//马达+闪光灯
	private Handler mpHandler;//音乐+图片
	private Handler fbcHandler;//前后摄像头
	private Handler vpHandler;//视频播放
	private Vibrator vibrator;//马达对象
	public Button playbutton;//开始测试按钮
	public Button previewbutton;//开始预览测试按钮
	public Button stopbutton;//停止测试按钮
	public Button vfButton;//马达闪光灯单项测试按钮
	public Button mpButton;//音乐图片轮播单项测试按钮
	public Button c01Button;//前后摄像头切换按钮
	public Button vedioButton;//高清视频播放按钮
	//int ScreenBrightness;
	//private Context PackageContext;
	//int maxVolume;
	int volume0;// 当前音量
	private Camera mCamera;
	private int mNumberOfCameras;
	public boolean LightFlag, VibratorFlag, MediaPlayerFlag, FlipperFlag, CameraFlag,
			CameraFlag1, VedioFlag, TimeFlag, allFunctionTimerFlag, allFunctionPreviewTimerFlag,
			oneItemTimerFlag = false;//各种测试标志位
	//private View v1, v2;
	private Camera.Parameters parameters;
	// private static int FLASH_TIME;
	//private PowerManager.WakeLock wl;
	//SurfaceView sView;
	//SurfaceHolder surfaceHolder;
	//int screenWidth, screenHeight;
	//private double t, s;
	// 定义系统所用的照相机

	// 是否在浏览中
	boolean isPreview = false;

	public TextView TV, vfView, vfTestView, mpView, mpTestView, c01View,
			c01TestView, vedioView, vedioTestView;

	private double BatteryT; // 电池温度
	private String BatteryStatus; // 电池状态
	private String BatteryTemp; // 电池使用情况

	public void onRecordStart() {
		time.setBase(SystemClock.elapsedRealtime() - recordingTime);// 跳过已经记录了的时间，起到继续计时的作用
		time.start();
	}
	public void onRecordPause() {
		time.stop();
		recordingTime = SystemClock.elapsedRealtime() - time.getBase();// 保存这次记录了的时间
	}
	public void onRecordStop() {
		recordingTime = 0;
		time.setBase(SystemClock.elapsedRealtime());
	}

	//自动调整按键内文字大小
	public static void setTextInButton(final String text, final Button btn) {
		ViewTreeObserver.OnGlobalLayoutListener ll = new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				btn.getViewTreeObserver().removeGlobalOnLayoutListener(this);//取消监听，很重要，要不然会很卡
				int width = btn.getWidth() - btn.getPaddingLeft() - btn.getPaddingRight();
				int len = text.length();
				btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (width / (len)) > 22 ? 22 : (float) (width / (len)));
			}
		};
		btn.getViewTreeObserver().addOnGlobalLayoutListener(ll);
	}
	//自动调整TextView内文字大小
	public static void setTextInTextView(final String text, final TextView tv) {
		ViewTreeObserver.OnGlobalLayoutListener ll = new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				tv.getViewTreeObserver().removeGlobalOnLayoutListener(this);//取消监听，很重要，要不然会很卡的
				int width = tv.getWidth() - tv.getPaddingLeft() - tv.getPaddingRight();
				int len = text.length();
				tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (width / (len)) > 22 ? 22 : (float) (width / (len)));
			}
		};
		tv.getViewTreeObserver().addOnGlobalLayoutListener(ll);
	}
	//自动调整EditExt内文字大小
	public static void setTextInEditText(final String text, final EditText et) {
		ViewTreeObserver.OnGlobalLayoutListener ll = new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				et.getViewTreeObserver().removeGlobalOnLayoutListener(this);//取消监听，很重要，要不然会很卡的
				int width = et.getWidth() - et.getPaddingLeft() - et.getPaddingRight();
				int len = text.length();
				et.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (width / (len)) > 20 ? 20 : (float) (width / (len)));
			}
		};
		et.getViewTreeObserver().addOnGlobalLayoutListener(ll);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main);
		// 初始化各UI控件
		synchronized (this) {
			timelength = (EditText) this.findViewById(R.id.timelength);
			timelength.setText("3");// 默认设置3小时
			setTextInEditText(timelength.getText().toString(), timelength);
			Log.i(TAG, "timelength.getTextSize()=" + timelength.getTextSize());

			vfView = (TextView) this.findViewById(R.id.vfView);
			mpView = (TextView) this.findViewById(R.id.mpView);
			setTextInTextView("音频循环+图片轮播", mpView);
			float adaptSize = mpView.getTextSize();
			Log.i(TAG, "adaptSize=" + adaptSize);
			c01View = (TextView) this.findViewById(R.id.c01View);
			vedioView = (TextView) this.findViewById(R.id.vedioView);

			vfView.setTextSize(TypedValue.COMPLEX_UNIT_PX, adaptSize);
			Log.i(TAG, "vfView.getTextSize()=" + vfView.getTextSize());
			c01View.setTextSize(TypedValue.COMPLEX_UNIT_PX, adaptSize);
			vedioView.setTextSize(TypedValue.COMPLEX_UNIT_PX, adaptSize);

			vfTestView = (TextView) this.findViewById(R.id.vfTestView);
			mpTestView = (TextView) this.findViewById(R.id.mpTestView);
			c01TestView = (TextView) this.findViewById(R.id.c01TestView);
			vedioTestView = (TextView) this.findViewById(R.id.vedioTestView);

			// vfTestView.setText("待测试");// 默认显示值
			vfTestView.setTextColor(Color.BLUE);
			// mpTestView.setText("待测试");// 默认显示值
			mpTestView.setTextColor(Color.BLUE);
			// c01TestView.setText("待测试");// 默认显示值
			c01TestView.setTextColor(Color.BLUE);
			// vedioTestView.setText("待测试");// 默认显示值
			vedioTestView.setTextColor(Color.BLUE);

			playbutton = (Button) findViewById(R.id.playbutton);// 开始按钮
			setTextInButton("开始", playbutton);
			float adaptBtnSize = playbutton.getTextSize();
			Log.i(TAG, "adaptBtnSize=" + adaptBtnSize);
			previewbutton = (Button) findViewById(R.id.previewbutton);// 预览按钮
			previewbutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, adaptBtnSize);
			Log.i(TAG, "previewbutton.getTextSize()=" + previewbutton.getTextSize());
			stopbutton = (Button) findViewById(R.id.stopbutton);// 停止按钮
			stopbutton.setTextSize(TypedValue.COMPLEX_UNIT_PX, adaptBtnSize);
			playbutton.setOnClickListener(this);
			previewbutton.setOnClickListener(this);
			stopbutton.setOnClickListener(this);

			vfButton = (Button) findViewById(R.id.vfButton);// 单项1按钮
			setTextInButton("开始", vfButton);
			float adaptSmallBtnSize = vfButton.getTextSize();
			Log.i(TAG, "adaptSmallBtnSize=" + adaptSmallBtnSize);
			vfButton.setOnClickListener(this);
			mpButton = (Button) findViewById(R.id.mpButton);// 单项2按钮
			mpButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, adaptSmallBtnSize);
			Log.i(TAG, "mpButton.getTextSize()=" + mpButton.getTextSize());
			mpButton.setOnClickListener(this);
			c01Button = (Button) findViewById(R.id.c01Button);// 单项3按钮
			c01Button.setTextSize(TypedValue.COMPLEX_UNIT_PX, adaptSmallBtnSize);
			c01Button.setOnClickListener(this);
			vedioButton = (Button) findViewById(R.id.vedioButton);// 单项4按钮
			vedioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, adaptSmallBtnSize);
			vedioButton.setOnClickListener(this);
		}
		//计时器控件
		time = (Chronometer) this.findViewById(R.id.chronometer);

		audioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);// 调用媒体服务
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);// 调用马达服务

		// MyTag可以随便写,可以写应用名称等
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// 换成PowerManager.SCREEN_DIM_WAKE_LOCK会变暗）
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyAgeTest");
		wl.acquire();// 开启屏幕常亮

		// 注册一个系统 BroadcastReceiver，作为访问电池计量之用，这个不能直接在AndroidManifest.xml中注册
		registerReceiver(mBatInfoReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		TV = (TextView) findViewById(R.id.TV);// 电池信息打印控件定义
		volume0 = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 获取当前音量
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		//开始测试后
		case R.id.playbutton:
			//除停止按钮外其他不可用
			vfButton.setEnabled(false);
			mpButton.setEnabled(false);
			c01Button.setEnabled(false);
			vedioButton.setEnabled(false);
			playbutton.setEnabled(false);
			previewbutton.setEnabled(false);

			mediaPlayer = new MediaPlayer();
			//获取设置的测试时长
			double t = Double.parseDouble("" + timelength.getText());
			if (t == 0 || timelength.getText() == null) {
				timelength.setText("3");
				Toast.makeText(getApplicationContext(), R.string.timeset,Toast.LENGTH_SHORT)
						.show();// 未设置测试时间提示
			}
			final double s = 1000 * 3600 * t + 1000;
			if (t != 0.0 && LightFlag == false && VibratorFlag == false
					&& MediaPlayerFlag == false && FlipperFlag == false
					&& CameraFlag == false && CameraFlag1 == false
					&& VedioFlag == false) {

				audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 按当前喇叭最大音量等级
				int maxVolume = audioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 获取最大音量
				if (volume0 != maxVolume) {
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							maxVolume, 0);// 设置为最大音量
				}
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置音频流模式
				//screenBrightness_check();// 关闭亮度自动调节
				//setScreenBritness(255);// 设置最大亮度
				vfTestView.setText("待测试");// 默认显示值
				vfTestView.setTextColor(Color.BLUE);
				mpTestView.setText("待测试");// 默认显示值
				mpTestView.setTextColor(Color.BLUE);
				c01TestView.setText("待测试");// 默认显示值
				c01TestView.setTextColor(Color.BLUE);
				vedioTestView.setText("待测试");// 默认显示值
				vedioTestView.setTextColor(Color.BLUE);
				// 全功能测试
				allFunctionHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						switch (msg.what) {
						//开启马达闪光灯
						case FLASH_VIBRATE:
							onRecordStop();// 停止计时
							time.stop();
							try {
								Thread.sleep(10);// 状态间歇10ms，可修改
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							onRecordStart();// 计时开始
							TimeFlag = true;
							vibratorTimer = new Timer(true);
							vibratorTimerTask = new TimerTask()// 马达启动计时任务设置
							{
								@Override
								public void run() {
									try {
										vibrator.vibrate(2000);//马达持续震动2000ms
										VibratorFlag = true;
									} catch (Exception e) {
										vfTestView.setText("Fail");// 显示失败
										vfTestView.setTextColor(Color.RED);
									}
								}
							};
							vibratorTimer.schedule(vibratorTimerTask, 0, 3000);//马达振动循环3000ms
							flashLightTimer = new Timer(true);
							flashLightTimerTask = new TimerTask()// 闪光灯启动计时任务设置
							{
								@Override
								public void run() {
									try {
										openLight();
										try {
											Thread.sleep(400);// 状态间歇400ms，可修改
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										closeLight();
										try {
											Thread.sleep(100);// 状态间歇100ms，可修改
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									} catch (Exception e) {
										vfTestView.setText("Fail");// 显示失败
										vfTestView.setTextColor(Color.RED);
									}
								}
							};
							flashLightTimer.schedule(flashLightTimerTask, 0, 4500);//闪光灯振动循环4500ms
							break;
						//关闭马达闪光灯测试
						case FV_CLOSE:
							if (VibratorFlag == true && LightFlag == true) {
								closeLight();
								mCamera.release();// MTK
								mCamera = null;// MTK
								LightFlag = false;// 闪光灯关闭标志位
								vibrator.cancel();
								VibratorFlag = false;// 马达关闭标志位
								vfTestView.setText("Pass");
								vfTestView.setTextColor(Color.GREEN);
							} else {
								vfTestView.setText("Fail");// 显示失败
								vfTestView.setTextColor(Color.RED);
							}
							vibratorTimer.cancel();
							flashLightTimer.cancel();
							break;
						//开启音乐+图片轮播测试
						case MUSIC_PHOTO:
							try {
								play(0);
								mediaPlayer.setLooping(true);// 设置音频循环播放
								MediaPlayerFlag = true;
								getFlipper();// 图片轮播函数
								FlipperFlag = true;// 图片轮播标志位
							} catch (Exception e) {
								mpTestView.setText("Fail");// 显示失败
								mpTestView.setTextColor(Color.RED);
							}
							break;
						//关闭音乐+图片轮播测试
						case MP_CLOSE:
							if (MediaPlayerFlag == true && FlipperFlag == true) {
								mediaPlayer.stop();// 停止音频播放
								mediaPlayer.release();
								mediaPlayer = null;
								MediaPlayerFlag = false;
								FlipperDialog.flipper.stopFlipping();
								FlipperDialog.instance.finish(); // 关闭轮播视图
								FlipperFlag = false;// 图片轮播停止标志位
								mpTestView.setText("Pass");
								mpTestView.setTextColor(Color.GREEN);
							} else {
								mpTestView.setText("Fail");// 显示失败
								mpTestView.setTextColor(Color.RED);
								//FlipperDialog.instance.finish(); // 关闭轮播视图
							}
							break;
						//开启前置摄像头
						case CAMERA_OPEN:
							try {
								getPhoto();// 前置摄像头开启
								CameraFlag = true;// 前置摄像头开启标志位
							} catch (Exception e) {
								c01TestView.setText("Fail");// 显示失败
								c01TestView.setTextColor(Color.RED);
								//CameraDialog.instance.finish();//
							}
							break;
						//关闭前置摄像头
						case CAMERA_CLOSE:
							if (CameraFlag == true) {
								CameraDialog.instance.finish();// 关闭相机视图
								if (CameraDialog.camera != null) {
									CameraDialog.camera.release();
									CameraDialog.camera = null;
								}
								CameraFlag = false;// 摄像头关闭标志位
								c01TestView.setText("Pass");
								c01TestView.setTextColor(Color.GREEN);
							} else {
								c01TestView.setText("Fail");// 显示失败
								c01TestView.setTextColor(Color.RED);
								// CameraDialog.instance.finish();
							}
							break;
						//开启后置摄像头
						case CAMERA_OPEN1:
							try {
								getPhoto1();
								CameraFlag1 = true;// 后置摄像头开启标志位
							} catch (Exception e) {
								c01TestView.setText("Fail");// 显示失败
								c01TestView.setTextColor(Color.RED);
								// CameraDialog1.instance.finish();
							}
							break;
						//关闭后置摄像头
						case CAMERA_CLOSE1:
							if (CameraFlag1 = true) {
								CameraDialog1.instance.finish();// 关闭相机视图
								if (CameraDialog1.camera != null) {
									CameraDialog1.camera.release();
									CameraDialog1.camera = null;
								}
								CameraFlag1 = false;// 摄像头关闭标志位
								c01TestView.setText("Pass");
								c01TestView.setTextColor(Color.GREEN);
							} else {
								c01TestView.setText("Fail");// 显示失败
								c01TestView.setTextColor(Color.RED);
								// CameraDialog1.instance.finish();
							}
							break;
						//开启高清视频循环播放
						case VEDIO_OPEN:
							try {
								getVedio();
								VedioFlag = true;// 视频播放开启标志位
							} catch (Exception e) {
								vedioTestView.setText("Fail");// 显示失败
								vedioTestView.setTextColor(Color.RED);
								// VedioPlayDialog.instance.finish();
							}
							break;
						//关闭高清视频循环播放
						case VEDIO_CLOSE:
							if (VedioFlag = true) {
								VedioPlayDialog.instance.finish();// 关闭视频视图
								VedioFlag = false;// 视频关闭标志位
								vedioTestView.setText("Pass");
								vedioTestView.setTextColor(Color.GREEN);
							} else {
								vedioTestView.setText("Fail");// 显示失败
								vedioTestView.setTextColor(Color.RED);
								// VedioPlayDialog.instance.finish();
							}
							break;
						//结束测试，弹出提示窗口，重置按钮状态
						case ALL_CLOSE:
							onRecordPause();// 停止计时
							TimeFlag = false;
							showDialog();// 弹出提示窗口
							vfButton.setEnabled(true);
							mpButton.setEnabled(true);
							c01Button.setEnabled(true);
							vedioButton.setEnabled(true);
							playbutton.setEnabled(true);
							previewbutton.setEnabled(true);
							break;
						//停止计时
						case ONE_CLOSE:
							onRecordPause();// 停止计时
							TimeFlag = false;
							// showDialog();// 弹出提示窗口
							break;
						}
						super.handleMessage(msg);

					}
				};

				allFunctionTimer = new Timer(true);// 全功能测试
				allFunctionTimerTask = new TimerTask()
				{
					@Override
					public void run() {
						allFunctionTimerFlag = true;
						//马达闪光灯循环任务
						allFunctionHandler.obtainMessage(FLASH_VIBRATE).sendToTarget();// 默认闪光灯马达开启45分钟(180/4)
						allFunctionHandler.sendEmptyMessageDelayed(FV_CLOSE,
								(long) (s / 4.0));
						//音乐图片轮播任务
						allFunctionHandler.sendEmptyMessageDelayed(MUSIC_PHOTO,
								(long) (s / 4.0) + 1000);// 音乐图片轮播开启45分钟
						allFunctionHandler.sendEmptyMessageDelayed(MP_CLOSE,
								(long) (s / 2.0));
						//前置摄像头任务
						allFunctionHandler.sendEmptyMessageDelayed(CAMERA_OPEN,
								(long) (s / 2.0) + 1000);// 前置摄像头开启22.5分钟
						allFunctionHandler.sendEmptyMessageDelayed(CAMERA_CLOSE,
								(long) (s / 1.6));
						//后置摄像头任务
						allFunctionHandler.sendEmptyMessageDelayed(CAMERA_OPEN1,
								(long) (s / 1.6) + 1000);// 后置摄像头开启22.5分钟
						allFunctionHandler.sendEmptyMessageDelayed(CAMERA_CLOSE1,
								(long) (s * 0.75));
						//高清视频循环任务
						allFunctionHandler.sendEmptyMessageDelayed(VEDIO_OPEN,
								(long) (s * 0.75) + 1000);// 高清视频开启45分钟
						allFunctionHandler.sendEmptyMessageDelayed(VEDIO_CLOSE,
								(long) (s) - 1000);
						//结束测试
						allFunctionHandler.sendEmptyMessageDelayed(ALL_CLOSE, (long) (s));
					}
				};
				allFunctionTimer.schedule(allFunctionTimerTask, 0);
			}
			break;
		//全功能预览测试，原理同上
		case R.id.previewbutton:
			vfButton.setEnabled(false);
			mpButton.setEnabled(false);
			c01Button.setEnabled(false);
			vedioButton.setEnabled(false);
			playbutton.setEnabled(false);
			previewbutton.setEnabled(false);

			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置音频流模式
			audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 按当前喇叭最大音量等级
			int maxVolume = audioManager
					.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 获取最大音量
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume,
					0);// 设置为最大音量
			//screenBrightness_check();// 关闭亮度自动调节
			//setScreenBritness(255);// 设置最大亮度

			vfTestView.setText("待测试");// 默认显示值
			vfTestView.setTextColor(Color.BLUE);
			mpTestView.setText("待测试");// 默认显示值
			mpTestView.setTextColor(Color.BLUE);
			c01TestView.setText("待测试");// 默认显示值
			c01TestView.setTextColor(Color.BLUE);
			vedioTestView.setText("待测试");// 默认显示值
			vedioTestView.setTextColor(Color.BLUE);

			allFunctionPreviewHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case FLASH_VIBRATE:
						vibratorTimer = new Timer(true);
						vibratorTimerTask = new TimerTask()// 马达启动计时任务设置
						{
							@Override
							public void run() {
								try {
									vibrator.vibrate(2000);
									VibratorFlag = true;
								} catch (Exception e) {
									vfTestView.setText("Fail");// 显示失败
									vfTestView.setTextColor(Color.RED);
								}
							}
						};
						vibratorTimer.schedule(vibratorTimerTask, 0, 3000);
						flashLightTimer = new Timer(true);
						flashLightTimerTask = new TimerTask()// 闪光灯启动计时任务设置
						{
							@Override
							public void run() {
								try {
									openLight();

									try {
										Thread.sleep(400);// 状态间歇1S，可修改
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									closeLight();
									try {
										Thread.sleep(100);// 状态间歇1S，可修改
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} catch (Exception e) {
									vfTestView.setText("Fail");// 显示失败
									vfTestView.setTextColor(Color.RED);
								}

							}
						};
						flashLightTimer.schedule(flashLightTimerTask, 0, 4500);
						break;
					case FV_CLOSE:
						if (VibratorFlag == true && LightFlag == true) {
							closeLight();
							mCamera.release();// MTK
							mCamera = null;// MTK
							LightFlag = false;// 闪光灯关闭标志位
							vibrator.cancel();
							VibratorFlag = false;// 马达关闭标志位
							vfTestView.setText("Pass");
							vfTestView.setTextColor(Color.GREEN);
						} else {
							vfTestView.setText("Fail");// 显示失败
							vfTestView.setTextColor(Color.RED);
						}
						vibratorTimer.cancel();
						flashLightTimer.cancel();
						break;
					case MUSIC_PHOTO:
						try {
							play(0);
							mediaPlayer.setLooping(true);// 设置音频循环播放
							MediaPlayerFlag = true;
							getFlipper();// 图片轮播函数
							FlipperFlag = true;// 图片轮播标志位
						} catch (Exception e) {
							mpTestView.setText("Fail");// 显示失败
							mpTestView.setTextColor(Color.RED);
						}
						break;
					case MP_CLOSE:
						if (MediaPlayerFlag == true && FlipperFlag == true) {
							mediaPlayer.stop();// 停止音频播放
							mediaPlayer.release();
							mediaPlayer = null;
							MediaPlayerFlag = false;
							FlipperDialog.flipper.stopFlipping();
							FlipperDialog.instance.finish(); // 关闭轮播视图
							FlipperFlag = false;// 图片轮播停止标志位
							mpTestView.setText("Pass");
							mpTestView.setTextColor(Color.GREEN);
						} else {
							mpTestView.setText("Fail");// 显示失败
							mpTestView.setTextColor(Color.RED);
							// FlipperDialog.instance.finish(); // 关闭轮播视图
						}
						break;
					case CAMERA_OPEN:
						try {
							getPhoto();// 前置摄像头开启
							CameraFlag = true;// 前置摄像头开启标志位
						} catch (Exception e) {
							c01TestView.setText("Fail");// 显示失败
							c01TestView.setTextColor(Color.RED);
							// CameraDialog.instance.finish();//
						}
						break;
					case CAMERA_CLOSE:
						if (CameraFlag == true) {
							CameraDialog.instance.finish();// 关闭相机视图
							if (CameraDialog.camera != null) {
								CameraDialog.camera.release();
								CameraDialog.camera = null;
							}
							CameraFlag = false;// 摄像头关闭标志位
							c01TestView.setText("Pass");
							c01TestView.setTextColor(Color.GREEN);
						} else {
							c01TestView.setText("Fail");// 显示失败
							c01TestView.setTextColor(Color.RED);
							// CameraDialog.instance.finish();
						}
						break;
					case CAMERA_OPEN1:
						try {
							getPhoto1();
							CameraFlag1 = true;// 后置摄像头开启标志位
						} catch (Exception e) {
							c01TestView.setText("Fail");// 显示失败
							c01TestView.setTextColor(Color.RED);
							// CameraDialog1.instance.finish();
						}
						break;
					case CAMERA_CLOSE1:
						if (CameraFlag1 = true) {
							CameraDialog1.instance.finish();// 关闭相机视图
							if (CameraDialog1.camera != null) {
								CameraDialog1.camera.release();
								CameraDialog1.camera = null;
							}
							CameraFlag1 = false;// 摄像头关闭标志位
							c01TestView.setText("Pass");
							c01TestView.setTextColor(Color.GREEN);
						} else {
							c01TestView.setText("Fail");// 显示失败
							c01TestView.setTextColor(Color.RED);
							// CameraDialog1.instance.finish();
						}
						break;

					case VEDIO_OPEN:
						try {
							getVedio();
							VedioFlag = true;// 视频播放开启标志位
						} catch (Exception e) {
							vedioTestView.setText("Fail");// 显示失败
							vedioTestView.setTextColor(Color.RED);
							// VedioPlayDialog.instance.finish();
						}
						break;
					case VEDIO_CLOSE:
						if (VedioFlag = true) {
							VedioPlayDialog.instance.finish();// 关闭视频视图
							VedioFlag = false;// 视频关闭标志位
							vedioTestView.setText("Pass");
							vedioTestView.setTextColor(Color.GREEN);
						} else {
							vedioTestView.setText("Fail");// 显示失败
							vedioTestView.setTextColor(Color.RED);
							// VedioPlayDialog.instance.finish();
						}
						break;
					case ALL_CLOSE:
						showDialog();
						vfButton.setEnabled(true);
						mpButton.setEnabled(true);
						c01Button.setEnabled(true);
						vedioButton.setEnabled(true);
						playbutton.setEnabled(true);
						previewbutton.setEnabled(true);
						break;
					}
					super.handleMessage(msg);
				}
			};

			if (recordingTime == 0) {
				allFunctionPreviewTimer = new Timer(true);
				allFunctionPreviewTimerTask = new TimerTask()// 预览测试任务
				{
					@Override
					public void run() {
						allFunctionPreviewTimerFlag = true;
						allFunctionPreviewHandler.obtainMessage(FLASH_VIBRATE).sendToTarget();
						allFunctionPreviewHandler.sendEmptyMessageDelayed(FV_CLOSE, 2000);
						allFunctionPreviewHandler.sendEmptyMessageDelayed(MUSIC_PHOTO, 2500);
						allFunctionPreviewHandler.sendEmptyMessageDelayed(MP_CLOSE, 7500);
						allFunctionPreviewHandler.sendEmptyMessageDelayed(CAMERA_OPEN, 8500);//
						allFunctionPreviewHandler.sendEmptyMessageDelayed(CAMERA_CLOSE, 11000);
						allFunctionPreviewHandler.sendEmptyMessageDelayed(CAMERA_OPEN1, 11500);
						allFunctionPreviewHandler.sendEmptyMessageDelayed(CAMERA_CLOSE1, 15000);
						allFunctionPreviewHandler.sendEmptyMessageDelayed(VEDIO_OPEN, 15500);
						allFunctionPreviewHandler.sendEmptyMessageDelayed(VEDIO_CLOSE, 20000);
						allFunctionPreviewHandler.sendEmptyMessageDelayed(ALL_CLOSE, 21000);
					}
				};
				allFunctionPreviewTimer.schedule(allFunctionPreviewTimerTask, 0);
			}
			break;
		// 马达闪光灯单项测试开关
		case R.id.vfButton:
			vfButton.setEnabled(false);
			mpButton.setEnabled(false);
			c01Button.setEnabled(false);
			vedioButton.setEnabled(false);
			playbutton.setEnabled(false);
			previewbutton.setEnabled(false);
			vfTestView.setText("待测试");// 默认显示值
			vfTestView.setTextColor(Color.BLUE);
			mpTestView.setText("待测试");// 默认显示值
			mpTestView.setTextColor(Color.BLUE);
			c01TestView.setText("待测试");// 默认显示值
			c01TestView.setTextColor(Color.BLUE);
			vedioTestView.setText("待测试");// 默认显示值
			vedioTestView.setTextColor(Color.BLUE);
			double t01 = Double.parseDouble("" + timelength.getText());
			if (t01 == 0 || timelength.getText() == null) {
				timelength.setText("3");
				Toast.makeText(getApplicationContext(), R.string.timeset,Toast.LENGTH_SHORT)
						.show();// 未设置测试时间提示
			}
			final double s01 = 1000 * 3600 * t01 + 1000;
			vfHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case FLASH_VIBRATE:
						// onRecordStart();// 计时开始
						// TimeFlag = true;

						vibratorTimer = new Timer(true);

						vibratorTimerTask = new TimerTask()// 马达启动计时任务设置
						{
							@Override
							public void run() {
								try {
									vibrator.vibrate(2000);
									VibratorFlag = true;
								} catch (Exception e) {
									vfTestView.setText("Fail");// 显示失败
									vfTestView.setTextColor(Color.RED);
								}
							}
						};
						vibratorTimer.schedule(vibratorTimerTask, 0, 3000);
						flashLightTimer = new Timer(true);
						flashLightTimerTask = new TimerTask()// 闪光灯启动计时任务设置
						{
							@Override
							public void run() {
								try {
									openLight();
									try {
										Thread.sleep(400);// 状态间歇1S，可修改
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									closeLight();
									try {
										Thread.sleep(100);// 状态间歇1S，可修改
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} catch (Exception e) {
									vfTestView.setText("Fail");// 显示失败
									vfTestView.setTextColor(Color.RED);
								}
							}
						};
						flashLightTimer.schedule(flashLightTimerTask, 0, 4500);
						break;
					case FV_CLOSE:
						if (VibratorFlag == true && LightFlag == true) {
							closeLight();
							mCamera.release();// MTK
							mCamera = null;// MTK
							LightFlag = false;// 闪光灯关闭标志位
							vibrator.cancel();
							VibratorFlag = false;// 马达关闭标志位
							vfTestView.setText("Pass");
							vfTestView.setTextColor(Color.GREEN);
						} else {
							vfTestView.setText("Fail");// 显示失败
							vfTestView.setTextColor(Color.RED);
						}
						vibratorTimer.cancel();
						flashLightTimer.cancel();
						break;
					case ONE_CLOSE:
						onRecordPause();// 停止计时
						TimeFlag = false;
						vfButton.setEnabled(true);
						mpButton.setEnabled(true);
						c01Button.setEnabled(true);
						vedioButton.setEnabled(true);
						playbutton.setEnabled(true);
						previewbutton.setEnabled(true);
						break;
					}
					super.handleMessage(msg);
				}
			};
			onRecordStop();// 停止计时
			time.stop();
			try {
				Thread.sleep(10);// 状态间歇1S，可修改
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			onRecordStart();// 计时开始
			TimeFlag = true;
			oneItemTimer = new Timer(true);// 单项测试
			oneItemTimerTask = new TimerTask()// 音频+图片轮播延长3600秒播放
			{
				@Override
				public void run() {
					oneItemTimerFlag = true;
					vfHandler.obtainMessage(FLASH_VIBRATE).sendToTarget();// 闪光灯马达开启
					vfHandler.sendEmptyMessageDelayed(FV_CLOSE,
							(long) (s01) - 500);
					vfHandler.sendEmptyMessageDelayed(ONE_CLOSE, (long) (s01));
				}
			};
			oneItemTimer.schedule(oneItemTimerTask, 0);
			break;
		// 音频+图片轮播单项测试开关
		case R.id.mpButton:
			vfButton.setEnabled(false);
			mpButton.setEnabled(false);
			c01Button.setEnabled(false);
			vedioButton.setEnabled(false);
			playbutton.setEnabled(false);
			previewbutton.setEnabled(false);
			vfTestView.setText("待测试");// 默认显示值
			vfTestView.setTextColor(Color.BLUE);
			mpTestView.setText("待测试");// 默认显示值
			mpTestView.setTextColor(Color.BLUE);
			c01TestView.setText("待测试");// 默认显示值
			c01TestView.setTextColor(Color.BLUE);
			vedioTestView.setText("待测试");// 默认显示值
			vedioTestView.setTextColor(Color.BLUE);
			double t02 = Double.parseDouble("" + timelength.getText());
			if (t02 == 0 || timelength.getText() == null) {
				timelength.setText("3");
				Toast.makeText(getApplicationContext(), R.string.timeset,Toast.LENGTH_SHORT)
						.show();// 未设置测试时间提示
			}
			final double s02 = 1000 * 3600 * t02 + 1000;
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置音频流模式
			audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 按当前喇叭最大音量等级
			int maxVolume02 = audioManager
					.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 获取最大音量
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					maxVolume02, 0);// 设置为最大音量
			mpHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case MUSIC_PHOTO:
						try {
							play(0);
							mediaPlayer.setLooping(true);// 设置音频循环播放
							MediaPlayerFlag = true;
							getFlipper();// 图片轮播函数
							FlipperFlag = true;// 图片轮播标志位
						} catch (Exception e) {
							mpTestView.setText("Fail");// 显示失败
							mpTestView.setTextColor(Color.RED);
						}
						break;
					case MP_CLOSE:
						if (MediaPlayerFlag == true && FlipperFlag == true) {
							mediaPlayer.stop();// 停止音频播放
							mediaPlayer.release();
							mediaPlayer = null;
							MediaPlayerFlag = false;
							FlipperDialog.flipper.stopFlipping();
							FlipperDialog.instance.finish(); // 关闭轮播视图
							FlipperFlag = false;// 图片轮播停止标志位
							mpTestView.setText("Pass");
							mpTestView.setTextColor(Color.GREEN);
						} else {
							mpTestView.setText("Fail");// 显示失败
							mpTestView.setTextColor(Color.RED);
							// FlipperDialog.instance.finish(); // 关闭轮播视图
						}
						break;
					case ONE_CLOSE:
						onRecordPause();// 停止计时
						TimeFlag = false;
						vfButton.setEnabled(true);
						mpButton.setEnabled(true);
						c01Button.setEnabled(true);
						vedioButton.setEnabled(true);
						playbutton.setEnabled(true);
						previewbutton.setEnabled(true);
						break;
					}
					super.handleMessage(msg);
				}
			};
			onRecordStop();// 停止计时
			time.stop();
			try {
				Thread.sleep(10);// 状态间歇1S，可修改
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			onRecordStart();// 计时开始
			TimeFlag = true;
			oneItemTimer = new Timer(true);// 单项测试
			mpTimerTask = new TimerTask()// 音频+图片轮播延长3600秒播放
			{
				@Override
				public void run() {
					oneItemTimerFlag = true;
					mpHandler.sendEmptyMessageDelayed(MUSIC_PHOTO, 0);// 音频+图片轮播开启
					mpHandler.sendEmptyMessageDelayed(MP_CLOSE,
							(long) (s02) - 500);
					mpHandler.sendEmptyMessageDelayed(ONE_CLOSE, (long) (s02));
				}
			};
			oneItemTimer.schedule(mpTimerTask, 0);
			break;
		// 前后摄像头单项测试开关
		case R.id.c01Button:
			vfButton.setEnabled(false);
			mpButton.setEnabled(false);
			c01Button.setEnabled(false);
			vedioButton.setEnabled(false);
			playbutton.setEnabled(false);
			previewbutton.setEnabled(false);
			vfTestView.setText("待测试");// 默认显示值
			vfTestView.setTextColor(Color.BLUE);
			mpTestView.setText("待测试");// 默认显示值
			mpTestView.setTextColor(Color.BLUE);
			c01TestView.setText("待测试");// 默认显示值
			c01TestView.setTextColor(Color.BLUE);
			vedioTestView.setText("待测试");// 默认显示值
			vedioTestView.setTextColor(Color.BLUE);
			double t03 = Double.parseDouble("" + timelength.getText());
			if (t03 == 0 || timelength.getText() == null) {
				timelength.setText("3");
				Toast.makeText(getApplicationContext(), R.string.timeset,Toast.LENGTH_SHORT)
						.show();// 未设置测试时间提示
			}
			final double s03 = 1000 * 3600 * t03 + 1000;
			fbcHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case CAMERA_OPEN:
						try {
							getPhoto();// 前置摄像头开启
							CameraFlag = true;// 前置摄像头开启标志位
						} catch (Exception e) {
							c01TestView.setText("Fail");// 显示失败
							c01TestView.setTextColor(Color.RED);
							// CameraDialog.instance.finish();//
						}
						break;
					case CAMERA_CLOSE:
						if (CameraFlag == true) {
							CameraDialog.instance.finish();// 关闭相机视图
							CameraDialog.camera.release();
							CameraDialog.camera = null;
							CameraFlag = false;// 摄像头关闭标志位
							c01TestView.setText("Pass");
							c01TestView.setTextColor(Color.GREEN);
						} else {
							c01TestView.setText("Fail");// 显示失败
							c01TestView.setTextColor(Color.RED);
							// CameraDialog.instance.finish();
						}
						break;
					case CAMERA_OPEN1:
						try {
							getPhoto1();
							CameraFlag1 = true;// 后置摄像头开启标志位
						} catch (Exception e) {
							c01TestView.setText("Fail");// 显示失败
							c01TestView.setTextColor(Color.RED);
							// CameraDialog1.instance.finish();
						}
						break;
					case CAMERA_CLOSE1:
						if (CameraFlag1 = true) {
							CameraDialog1.instance.finish();// 关闭相机视图
							CameraDialog1.camera.release();
							CameraDialog1.camera = null;
							CameraFlag1 = false;// 摄像头关闭标志位
							c01TestView.setText("Pass");
							c01TestView.setTextColor(Color.GREEN);
						} else {
							c01TestView.setText("Fail");// 显示失败
							c01TestView.setTextColor(Color.RED);
							// CameraDialog1.instance.finish();
						}
						break;

					case ONE_CLOSE:
						onRecordPause();// 停止计时
						// TimeFlag = false;
						vfButton.setEnabled(true);
						mpButton.setEnabled(true);
						c01Button.setEnabled(true);
						vedioButton.setEnabled(true);
						playbutton.setEnabled(true);
						previewbutton.setEnabled(true);
						break;
					}
					super.handleMessage(msg);
				}
			};
			onRecordStop();// 停止计时
			time.stop();
			try {
				Thread.sleep(10);// 状态间歇1S，可修改
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			onRecordStart();// 计时开始
			TimeFlag = true;
			oneItemTimer = new Timer(true);// 单项测试
			fbcTimerTask = new TimerTask()
			{
				@Override
				public void run() {
					oneItemTimerFlag = true;
					fbcHandler.obtainMessage(CAMERA_OPEN).sendToTarget();// 前+后摄像头切换开启
					fbcHandler.sendEmptyMessageDelayed(CAMERA_CLOSE,
							(long) (s03 / 2.0));
					fbcHandler.sendEmptyMessageDelayed(CAMERA_OPEN1,
							(long) (s03 / 2.0) + 1000);
					fbcHandler.sendEmptyMessageDelayed(CAMERA_CLOSE1,
							(long) (s03) - 500);
					fbcHandler.sendEmptyMessageDelayed(ONE_CLOSE, (long) (s03));
				}
			};
			oneItemTimer.schedule(fbcTimerTask, 0);
			break;
		// 高清视频循环单项测试开关
		case R.id.vedioButton:
			vfButton.setEnabled(false);
			mpButton.setEnabled(false);
			c01Button.setEnabled(false);
			vedioButton.setEnabled(false);
			playbutton.setEnabled(false);
			previewbutton.setEnabled(false);
			vfTestView.setText("待测试");// 默认显示值
			vfTestView.setTextColor(Color.BLUE);
			mpTestView.setText("待测试");// 默认显示值
			mpTestView.setTextColor(Color.BLUE);
			c01TestView.setText("待测试");// 默认显示值
			c01TestView.setTextColor(Color.BLUE);
			vedioTestView.setText("待测试");// 默认显示值
			vedioTestView.setTextColor(Color.BLUE);
			double t04 = Double.parseDouble("" + timelength.getText());
			if (t04 == 0 || timelength.getText() == null) {
				timelength.setText("3");
				Toast.makeText(getApplicationContext(), R.string.timeset,Toast.LENGTH_SHORT)
						.show();// 未设置测试时间提示
			}
			final double s04 = 1000 * 3600 * t04 + 1000;
			vpHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case VEDIO_OPEN:
						try {
							getVedio();
							VedioFlag = true;// 视频播放开启标志位
						} catch (Exception e) {
							vedioTestView.setText("Fail");// 显示失败
							vedioTestView.setTextColor(Color.RED);
							// VedioPlayDialog.instance.finish();
						}
						break;
					case VEDIO_CLOSE:
						if (VedioFlag = true) {
							VedioPlayDialog.instance.finish();// 关闭视频视图
							VedioFlag = false;// 视频关闭标志位
							vedioTestView.setText("Pass");
							vedioTestView.setTextColor(Color.GREEN);
						} else {
							vedioTestView.setText("Fail");// 显示失败
							vedioTestView.setTextColor(Color.RED);
							// VedioPlayDialog.instance.finish();
						}
						break;
					case ONE_CLOSE:
						onRecordPause();// 停止计时
						// TimeFlag = false;
						vfButton.setEnabled(true);
						mpButton.setEnabled(true);
						c01Button.setEnabled(true);
						vedioButton.setEnabled(true);
						playbutton.setEnabled(true);
						previewbutton.setEnabled(true);
						break;
					}
					super.handleMessage(msg);
				}
			};
			onRecordStop();// 停止计时
			time.stop();
			try {
				Thread.sleep(10);// 状态间歇1S，可修改
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			onRecordStart();// 计时开始
			TimeFlag = true;
			oneItemTimer = new Timer(true);// 单项测试
			vpTimerTask = new TimerTask()
			{
				@Override
				public void run() {
					oneItemTimerFlag = true;
					vpHandler.obtainMessage(VEDIO_OPEN).sendToTarget();// 视频循环播放开启
					vpHandler.sendEmptyMessageDelayed(VEDIO_CLOSE,
							(long) (s04) - 500);
					vpHandler.sendEmptyMessageDelayed(ONE_CLOSE, (long) (s04));
				}
			};
			oneItemTimer.schedule(vpTimerTask, 0);
			break;
		//停止测试按钮，关闭各测试模块，初始化按钮
		case R.id.stopbutton:
			vfTestView.setText("待测试");// 默认显示值
			vfTestView.setTextColor(Color.BLUE);
			mpTestView.setText("待测试");// 默认显示值
			mpTestView.setTextColor(Color.BLUE);
			c01TestView.setText("待测试");// 默认显示值
			c01TestView.setTextColor(Color.BLUE);
			vedioTestView.setText("待测试");// 默认显示值
			vedioTestView.setTextColor(Color.BLUE);
			if (allFunctionTimerFlag == true) {
				allFunctionTimer.cancel();
				allFunctionTimerTask.cancel();
				allFunctionTimer = null;
				allFunctionTimerTask =null;
				onRecordStop();// 停止计时
				time.stop();
				if (MediaPlayerFlag == true) {
					mediaPlayer.stop();// 停止播放
					mediaPlayer.release();
					mediaPlayer = null;
					MediaPlayerFlag = false;
				}
				if (VibratorFlag == true) {
					vibrator.cancel();// 停止马达振动
					VibratorFlag = false;
					vibratorTimer.cancel();
					vibratorTimerTask.cancel();
					vibratorTimer = null;
					vibratorTimerTask = null;
				}
				if (LightFlag == true) {
					// closeLight();//闪光灯关闭
					if (mCamera != null) {
						parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
						mCamera.setParameters(parameters);
						mCamera.release();
						mCamera = null;
					}
					LightFlag = false;
					flashLightTimer.cancel();
					flashLightTimerTask.cancel();
					flashLightTimer = null;
					flashLightTimerTask = null;
				}
				if (FlipperFlag == true) {
					FlipperDialog.flipper.stopFlipping();
					FlipperDialog.instance.finish(); // 关闭轮播视图
					FlipperFlag = false;// 图片轮播停止标志位
				}
				if (CameraFlag == true) {
					CameraDialog.instance.finish();// 关闭相机视图
					if (CameraDialog.camera != null) {
						CameraDialog.camera.release();
						CameraDialog.camera = null;
					}
					CameraFlag = false;// 摄像头关闭标志位
				}
				if (CameraFlag1 == true) {
					CameraDialog1.instance.finish();// 关闭相机视图
					if (CameraDialog1.camera != null) {
						CameraDialog1.camera.release();
						CameraDialog1.camera = null;
					}
					CameraFlag1 = false;// 摄像头关闭标志位
				}
				if (VedioFlag == true) {
					VedioPlayDialog.instance.finish();// 关闭视频视图
					VedioFlag = false;// 视频关闭标志位
				}
				
				allFunctionTimerFlag = false;
			}

			if (allFunctionPreviewTimerFlag == true) {
				allFunctionPreviewTimer.cancel();
				allFunctionPreviewTimerTask.cancel();
				allFunctionPreviewTimer = null;
				allFunctionPreviewTimerTask = null;
				if (MediaPlayerFlag == true) {
					mediaPlayer.stop();// 停止播放
					mediaPlayer.release();
					mediaPlayer = null;
					MediaPlayerFlag = false;
				}
				if (VibratorFlag == true) {
					vibrator.cancel();// 停止马达振动
					VibratorFlag = false;
					vibratorTimer.cancel();
					vibratorTimerTask.cancel();
					vibratorTimer = null;
					vibratorTimerTask = null;
				}
				if (LightFlag == true) {
					if (mCamera != null) {
						parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
						mCamera.setParameters(parameters);
						mCamera.release();
						mCamera = null;
					}
					LightFlag = false;
					flashLightTimer.cancel();
					flashLightTimerTask.cancel();
					flashLightTimer = null;
					flashLightTimerTask = null;
				}
				if (FlipperFlag == true) {
					FlipperDialog.flipper.stopFlipping();
					FlipperDialog.instance.finish(); // 关闭轮播视图
					FlipperFlag = false;// 图片轮播停止标志位
				}
				if (CameraFlag == true) {
					CameraDialog.instance.finish();// 关闭相机视图
					if (CameraDialog.camera != null) {
						CameraDialog.camera.release();
						CameraDialog.camera = null;
					}
					CameraFlag = false;// 摄像头关闭标志位
				}
				if (CameraFlag1 == true) {
					CameraDialog1.instance.finish();// 关闭相机视图
					if (CameraDialog1.camera != null) {
						CameraDialog1.camera.release();
						CameraDialog1.camera = null;
					}
					CameraFlag1 = false;// 摄像头关闭标志位
				}
				if (VedioFlag == true) {
					VedioPlayDialog.instance.finish();// 关闭视频视图
					VedioFlag = false;// 视频关闭标志位
				}
				
				allFunctionPreviewTimerFlag = false;
			}
			//单项测试停止
			if (oneItemTimerFlag == true) {
				if (MediaPlayerFlag == true) {
					mediaPlayer.stop();// 停止播放
					mediaPlayer.release();
					mediaPlayer = null;
					MediaPlayerFlag = false;
				}
				if (VibratorFlag == true) {
					vibrator.cancel();// 停止马达振动
					VibratorFlag = false;
					vibratorTimer.cancel();
					vibratorTimerTask.cancel();
					vibratorTimer = null;
					vibratorTimerTask = null;
				}
				if (LightFlag == true) {
					if (mCamera != null) {
						parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
						mCamera.setParameters(parameters);
						mCamera.release();
						mCamera = null;
					}
					LightFlag = false;
					flashLightTimer.cancel();
					flashLightTimerTask.cancel();
				}
				if (FlipperFlag == true) {
					FlipperDialog.flipper.stopFlipping();
					FlipperDialog.instance.finish(); // 关闭轮播视图
					FlipperFlag = false;// 图片轮播停止标志位
				}
				if (CameraFlag == true) {
					CameraDialog.instance.finish();// 关闭相机视图
					if (CameraDialog.camera != null) {
						CameraDialog.camera.release();
						CameraDialog.camera = null;
					}
					CameraFlag = false;// 摄像头关闭标志位
				}
				if (CameraFlag1 == true) {
					CameraDialog1.instance.finish();// 关闭相机视图
					if (CameraDialog1.camera != null) {
						CameraDialog1.camera.release();
						CameraDialog1.camera = null;
					}
					CameraFlag1 = false;// 摄像头关闭标志位
				}
				if (VedioFlag == true) {
					VedioPlayDialog.instance.finish();// 关闭视频视图
					VedioFlag = false;// 视频关闭标志位
				}
				onRecordStop();// 停止计时
				time.stop();
				oneItemTimer.cancel();
				oneItemTimer = null;
				try{
					oneItemTimerTask.cancel();
					oneItemTimerTask = null;
				}catch(Exception e){}
				try{
					mpTimerTask.cancel();
					mpTimerTask = null;
				}catch(Exception e){}
				try{
					fbcTimerTask.cancel();
					fbcTimerTask = null;
				}catch(Exception e){}
				try{
					vpTimerTask.cancel();
					vpTimerTask = null;
				}catch(Exception e){}
				oneItemTimerFlag = false;
			}
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume0, 0);// 恢复之前的音量
			//autoBrightness();// 恢复自动亮度调节
			if (TimeFlag == true) {
				onRecordStop();// 停止计时
				time.stop();
				timelength.setText("3");
				TimeFlag = false;
			}
			if (TimeFlag == false) {
				onRecordStop();// 停止计时
				time.stop();
				timelength.setText("3");

			}
			vfButton.setEnabled(true);
			mpButton.setEnabled(true);
			c01Button.setEnabled(true);
			vedioButton.setEnabled(true);
			playbutton.setEnabled(true);
			previewbutton.setEnabled(true);
			break;
		}
	}
	//音频播放
	private void play(int position) {
		try {
			// mediaPlayer.reset();// 把各项参数恢复到初始状态
			mediaPlayer = MediaPlayer.create(this, R.raw.chimes);
			// mediaPlayer.prepare();// 进行缓冲
			mediaPlayer.setOnPreparedListener(new PrepareListener(position));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private final class PrepareListener implements OnPreparedListener {
		private int position;
		public PrepareListener(int position) {
			this.position = position;
		}
		public void onPrepared(MediaPlayer mediaPlayer) {
			mediaPlayer.start();// 开始播放
			if (position > 0)
				mediaPlayer.seekTo(position);

		}
	}
	// 亮度模式检测，如果为自动，则关闭该模式
	/*private void screenBrightness_check()
	{
		// 先关闭系统的亮度自动调节
		try {
			if (android.provider.Settings.System.getInt(getContentResolver(),
					android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE) == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
				android.provider.Settings.System
						.putInt(getContentResolver(),
								android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
								android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			}
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// 设置亮度自动调节
	private void autoBrightness()
	{
		android.provider.Settings.System
				.putInt(getContentResolver(),
						android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
						android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
	}
	// 亮度调节
	private void setScreenBritness(int brightness)
	{
		// 不让屏幕全暗
		if (brightness <= 1) {
			brightness = 1;
		}
		// 设置当前activity的屏幕亮度
		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		// 0到1,调整亮度暗到全亮
		lp.screenBrightness = Float.valueOf(brightness / 255f);
		this.getWindow().setAttributes(lp);

		// 保存为系统亮度
		android.provider.Settings.System.putInt(getContentResolver(),
				android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);

	}*/
	//获取默认摄像头ID
	/*private int getDefaultCameraId() {
		int defaultId = -1;
		// Find the total number of cameras available
		mNumberOfCameras = Camera.getNumberOfCameras();
		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < mNumberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CAMERA_FACING_BACK) {
				defaultId = i;
			}
		}
		if (-1 == defaultId) {
			if (mNumberOfCameras > 0) {
				// 如果没有后向摄像头
				defaultId = 0;
			}
			else {
			    // 没有摄像头 Toast.makeText(getApplicationContext(),
			    R.string.no_camera, Toast.LENGTH_LONG).show();
			    }
		}
		return defaultId;
	}*/
	// 打开闪光灯
	private synchronized void openLight()
	{
		try {
			if (mCamera == null) {
				mCamera = Camera.open(CAMERA_FACING_BACK);
			}
			//mCamera.setPreviewTexture(new SurfaceTexture(0));//SP平台需要调用
			parameters = mCamera.getParameters();
			parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(parameters);
			mCamera.startPreview();
			LightFlag = true;
			Log.i(TAG, "openFlashLight success !");
		} catch (Exception e) {
			Log.d(TAG, "openFlashLight failed !");
			// System.exit(0);
		}
	}
	//关闭手电synchronized
	private synchronized void closeLight()
	{
		try {
			if (mCamera != null && LightFlag == true) {
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				mCamera.setParameters(parameters);
				//mCamera.release();//SP
				//mCamera=null;//SP

				Log.i(TAG, "closeFlashLight success !");
			}
		} catch (Exception e) {
			Log.d(TAG, "closeFlashLight failed !");
			// System.exit(0);

		}

	}
	// 开启图片轮播activity
	public void getFlipper()
	{
		Intent intent1 = new Intent(PressureTest.this, FlipperDialog.class);
		startActivity(intent1);
		// startActivityForResult(intent1, 0);
	}
	// 开启前置摄像头activity
	public void getPhoto()
	{
		Intent intent = new Intent(PressureTest.this, CameraDialog.class);
		startActivity(intent);
	}
	// 开启后置摄像头acitivty
	public void getPhoto1()
	{
		Intent intent1 = new Intent(PressureTest.this, CameraDialog1.class);
		startActivity(intent1);
	}
	// 开启视频播放acitivty
	public void getVedio()
	{
		Intent intent2 = new Intent(PressureTest.this, VedioPlayDialog.class);
		startActivity(intent2);
	}
	// 显示弹出窗口
	public void showDialog()
	{
		// 这里用到了返回试Activity的基本用法
		Intent i = new Intent(PressureTest.this, DialogActivity.class);
		startActivity(i);
	}
	// 程序按返回键退出处理
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			// 双击退出
			if (allFunctionTimerFlag == true || allFunctionPreviewTimerFlag == true)// 关闭前确认是否已停止测试
			{
				Toast.makeText(getApplicationContext(), R.string.stopfirst, Toast.LENGTH_SHORT)
						.show();// 未停止测试提示
			} else {
				if ((System.currentTimeMillis() - mExitTime) > 2000) {
					Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
					mExitTime = System.currentTimeMillis();
				} else {
					// isStart = false;
					finish();
				}
			}
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	/* 创建电池状态广播接收器 */
	public BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// 如果捕捉到的action是ACTION_BATTERY_CHANGED， 就运行onBatteryInfoReceiver()
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				// BatteryN = intent.getIntExtra("level", 0); //目前电量
				// BatteryV = intent.getIntExtra("voltage", 0); //电池电压
				try {
					BatteryT = intent.getIntExtra("temperature", 0); // 电池温度
				} catch (Exception e) {
					Log.e(TAG, e.toString());
					e.printStackTrace();
				}
				double T = BatteryT / 10.0; // 电池摄氏温度
				switch (intent.getIntExtra("status",
						BatteryManager.BATTERY_STATUS_UNKNOWN)) {
				case BatteryManager.BATTERY_STATUS_CHARGING:
					BatteryStatus = "充电状态";
					break;
				case BatteryManager.BATTERY_STATUS_DISCHARGING:
					BatteryStatus = "放电状态";
					break;
				case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
					BatteryStatus = "未充电";
					break;
				case BatteryManager.BATTERY_STATUS_FULL:
					BatteryStatus = "充满电";

					break;
				case BatteryManager.BATTERY_STATUS_UNKNOWN:
					BatteryStatus = "未知道状态";
					break;
				}
				switch (intent.getIntExtra("health",
						BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
				case BatteryManager.BATTERY_HEALTH_UNKNOWN:
					BatteryTemp = "未知错误";
					break;
				case BatteryManager.BATTERY_HEALTH_GOOD:
					BatteryTemp = "状态良好";
					break;
				case BatteryManager.BATTERY_HEALTH_DEAD:
					BatteryTemp = "电池没有电";

					break;
				case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
					BatteryTemp = "电池电压过高";
					break;
				case BatteryManager.BATTERY_HEALTH_OVERHEAT:
					BatteryTemp = "电池过热";
					break;
				}
				TV.setText("Temperature---" + T + "℃" + "     " + "Status:    " + BatteryTemp + "---" + BatteryStatus);
				setTextInTextView(TV.getText().toString(),TV);
			}
		}

	};

}
