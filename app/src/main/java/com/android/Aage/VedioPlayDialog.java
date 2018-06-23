package com.android.Aage;

import java.io.IOException;

//import com.android.age.R;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;

/**
 * Description:
 * <br/>site: <a href="http://www.crazyit.org">crazyit.org</a> 
 * <br/>Copyright (C), 2001-2012, Yeeku.H.Lee
 * <br/>This program is protected by copyright laws.
 * <br/>Program Name:
 * <br/>Date:
 * @author  Yeeku.H.Lee kongyeeku@163.com
 * @version  1.0
 */
public class VedioPlayDialog extends Activity
	implements OnClickListener
{
	static SurfaceView surfaceView;
	public static ImageButton play;
	ImageButton pause;
	ImageButton stop;
	public static MediaPlayer mPlayer = null;
	//记录当前视频的播放位置
	int position;
	public static VedioPlayDialog instance = null;
	public AudioManager audioManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.vedio_play_dialog);
		instance = this;
		audioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);// 调用媒体服务
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置音频流模式
		audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 按当前喇叭最大音量等级
		int maxVolume = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 获取最大音量
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume,
				0);// 设置为最大音量
		
		// 获取界面中的3个按钮
		play = (ImageButton) findViewById(R.id.play);
		pause = (ImageButton) findViewById(R.id.pause);
		stop = (ImageButton) findViewById(R.id.stop);
		// 为3个按钮的单击事件绑定事件监听器
		play.setOnClickListener(this);
		pause.setOnClickListener(this);
		stop.setOnClickListener(this);		
		// 创建MediaPlayer
		//mPlayer = new MediaPlayer();
		surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
		// 设置SurfaceView自己不管理的缓冲区
		surfaceView.getHolder().setType(
			SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// 设置播放时打开屏幕
		surfaceView.getHolder().setKeepScreenOn(true);
		//设置视频播放SurfaceView监听
		surfaceView.getHolder().addCallback(new SurfaceListener(){
		public void surfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
		}
		public void surfaceCreated(SurfaceHolder holder) {
			// 打开视频播放
			try {
				play();
				mPlayer.setLooping(true);// 设置视频频循环播放
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void surfaceDestroyed(SurfaceHolder holder) {
			// 停止播放
			if (mPlayer.isPlaying())
				mPlayer.stop();
			// 释放资源
			mPlayer.release();
			mPlayer = null;
		}
	});
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// 换成PowerManager.SCREEN_DIM_WAKE_LOCK会变暗）
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyVideoTest");
		wl.acquire();// 开启屏幕常亮
		/*try {
			mPlayer.reset();
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			// 设置需要播放的视频
			mPlayer.setDataSource("/sdcard/movie.mp4");
			// 把视频画面输出到SurfaceView
			mPlayer.setDisplay(surfaceView.getHolder());
			mPlayer.prepare();
			mPlayer.start();
			mPlayer.setLooping(true);// 设置视频频循环播放
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	public void onClick(View source)
	{
		try
		{
			switch (source.getId())
			{
				// 播放按钮被单击
				case R.id.play:
					play();
					break;
				// 暂停按钮被单击
				case R.id.pause:
					if (mPlayer.isPlaying())
					{
						mPlayer.pause();
					}
					else
					{
						mPlayer.start();
					}
					break;
				// 停止按钮被单击
				case R.id.stop:
					if (mPlayer.isPlaying())
						mPlayer.stop();
					break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public synchronized void play() throws IOException
	{
		mPlayer = MediaPlayer.create(this, R.raw.movie);
		//mPlayer.reset();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// 设置需要播放的视频
		//mPlayer.setDataSource("/sdcard/movie.mp4");
		// 把视频画面输出到SurfaceView
		mPlayer.setDisplay(surfaceView.getHolder());
		//mPlayer.prepare();
		mPlayer.start();
	}
	private class SurfaceListener implements SurfaceHolder.Callback
	{
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height)
		{
		}
		public void surfaceCreated(SurfaceHolder holder)
		{
			if (position > 0)
			{
				try
				{
					// 开始播放
					play();
					// 并直接从指定位置开始播放
					mPlayer.seekTo(position);
					position = 0;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		public void surfaceDestroyed(SurfaceHolder holder)
		{
		}
	}
	// 当其他Activity被打开，暂停播放
	/*@Override
	protected void onPause()
	{
		if (mPlayer.isPlaying())
		{
			// 保存当前的播放位置
			position = mPlayer.getCurrentPosition();
			mPlayer.stop();
		}
		super.onPause();
	}
	@Override
	protected void onDestroy()
	{
		// 停止播放
		if (mPlayer.isPlaying())
			mPlayer.stop();
		// 释放资源
		mPlayer.release();
		mPlayer = null;
		super.onDestroy();
	}*/
}