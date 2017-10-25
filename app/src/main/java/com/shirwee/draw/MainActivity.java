package com.shirwee.draw;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity implements OnClickListener,
		OnSeekBarChangeListener, OnTouchListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	private Paint paint;
	private ImageView iv;
	private Canvas canvas;
	private Bitmap bitmap;
	private SeekBar seekBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.v_red).setOnClickListener(this);
		findViewById(R.id.v_green).setOnClickListener(this);
		findViewById(R.id.v_blue).setOnClickListener(this);
		findViewById(R.id.v_yellow).setOnClickListener(this);
		findViewById(R.id.v_purple).setOnClickListener(this);
		findViewById(R.id.v_cyan).setOnClickListener(this);

		iv = (ImageView) findViewById(R.id.iv);
		iv.setOnTouchListener(this);

		seekBar = (SeekBar) findViewById(R.id.sb);
		seekBar.setOnSeekBarChangeListener(this);
	
		//布局尺寸大小,得到treeview对象，注册全局的布局监听器
		iv.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {

				iv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

				int width = iv.getMeasuredWidth();
				int height = iv.getMeasuredHeight();

				// 创建空白纸张
				bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				// 创建画板，将空白纸张放在画板上
				canvas = new Canvas(bitmap);
				// 给画板添加颜色
				canvas.drawColor(Color.rgb(0xff, 0xff, 0xe0));

				iv.setImageBitmap(bitmap);
			}
		});

		// 创建画笔
		paint = new Paint();
		// 创建画笔粗细的默认值
		paint.setStrokeWidth(seekBar.getProgress());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.v_red:
			paint.setColor(Color.RED);
			break;
		case R.id.v_green:
			paint.setColor(Color.GREEN);
			break;
		case R.id.v_blue:
			paint.setColor(Color.BLUE);
			break;
		case R.id.v_yellow:
			paint.setColor(Color.YELLOW);
			break;
		case R.id.v_purple:
			paint.setColor(0xffff00ff);
			break;
		case R.id.v_cyan:
			paint.setColor(Color.CYAN);
			break;
		default:
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// 停止触摸的时候，重新设置画笔的大小
		paint.setStrokeWidth(seekBar.getProgress());
		Toast.makeText(this, "画笔的大小：" + seekBar.getProgress(), Toast.LENGTH_SHORT).show();

	}

	float downX, downY, moveX, moveY;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = event.getX();
			downY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			moveX = event.getX();
			moveY = event.getY();
			// 开始画图,画的图在纸张上，就是这里的bitmap
			canvas.drawLine(downX, downY, moveX, moveY, paint);
			iv.setImageBitmap(bitmap);

			// 每动一点都要重新新的起点
			downX = moveX;
			downY = moveY;

			break;
		case MotionEvent.ACTION_UP:

			break;
		default:
			break;
		}

		// false代表的是我不是最终接受者，不是继续传递给下一个事件
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.save:
			// 存储图片，bitmap---->jpg
			File file =null;
			try {
				file = new File(Environment.getExternalStorageDirectory(),
						System.currentTimeMillis() + ".jpg");
				FileOutputStream fos = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				fos.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

			// 存储完毕之后，重新扫描sdcard
			//在4.4之后，系统不允许发送这个广播了，因为会全部重新扫面sdcard。。只能扫描单个文件
			/*Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
			intent.setData(Uri.parse("file://"));
			sendBroadcast(intent);*/
			
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			Uri uri = null;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				uri = FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);
			} else {
				uri = Uri.fromFile(file);
			}
			intent.setData(uri);
			sendBroadcast(intent);

			Toast.makeText(this, "保存图片成功", Toast.LENGTH_SHORT).show();

			break;
		case R.id.clear:
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			canvas.drawColor(Color.rgb(0xff, 0xff, 0xe0));
			iv.setImageBitmap(bitmap);
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	// 创建菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
