package com.android.Aage;

//import com.android.age.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.PowerManager;

import android.view.MotionEvent;
import android.view.WindowManager;

import android.view.View;

import android.view.View.OnClickListener;

import android.view.Window;

import android.widget.Button;
import android.widget.LinearLayout;

import android.widget.Toast;

public class DialogActivity extends Activity {
	private Button returnButton1;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_dialog);
		returnButton1 = (Button) findViewById(R.id.returnButton1);
		new AlertDialog.Builder(DialogActivity.this).setCancelable(false)
				.setPositiveButton("测试结束", null).show();
		// 和前面一样，只是用到了返回式Activity的基本方法，虽然这里已经是个Dialog了，但却和普通Activity无异
		returnButton1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(DialogActivity.this, PressureTest.class);
				// i.setAction(Intent.action.VIEW);
				// i.addCategory(Intent.CATEGORY_DEFAULT);
				DialogActivity.this.setResult(RESULT_OK, i);
				DialogActivity.this.finish();
			}
		});

		// MyTag可以随便写,可以写应用名称等
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// 换成PowerManager.SCREEN_DIM_WAKE_LOCK会变暗）
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyCloseTest");
		wl.acquire();// 开启屏幕常亮
	}

	/**
	 * 
	 * 触摸上面的LinearLayout之外的地方都会关闭本Activity
	 */

	/*
	 * @Override public boolean onTouchEvent(MotionEvent event) { // TODO
	 * Auto-generated method stub this.finish();// 结束本Activity return
	 * super.onTouchEvent(event);
	 * 
	 * }
	 */

}
