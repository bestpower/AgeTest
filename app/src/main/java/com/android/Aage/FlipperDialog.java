package com.android.Aage;

//import com.android.age.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ViewFlipper;

public class FlipperDialog extends Activity {
	public static ViewFlipper flipper = null;//引入轮播容器对象
	private int[] resId = { R.drawable.pc1, R.drawable.pc3, R.drawable.pc1,
			R.drawable.pc4 };
	public static FlipperDialog instance = null;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.imageview);
		instance = this;
		flipper = (ViewFlipper) findViewById(R.id.flipper);// 加入图片轮播视图
		// 动态导入的方式为ViewFlipper加入子View
		for (int i = 0; i < resId.length; i++) {
			flipper.addView(getImageView(resId[i]));
		}
		// 为ViewFlipper去添加动画效果
		flipper.setInAnimation(this, R.anim.left_in);
		flipper.setOutAnimation(this, R.anim.left_out);
		flipper.setFlipInterval(1000);
		flipper.startFlipping();
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// 换成PowerManager.SCREEN_DIM_WAKE_LOCK会变暗）
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyPhotoFlipperTest");
		wl.acquire();// 开启屏幕常亮
	}
	// 图片获取方法
	private ImageView getImageView(int resId)
	{
		ImageView image = new ImageView(this);
		image.setBackgroundResource(resId);
		return image;
	}
}
