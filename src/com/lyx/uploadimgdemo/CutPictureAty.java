package com.lyx.uploadimgdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.lyx.uploadimgdemo.R;
import com.lyx.uploadimgdemo.CutView.OnDrawListenerComplete;

/**
 * 图片裁剪
 */
public class CutPictureAty extends Activity implements OnTouchListener,
		OnClickListener {
	private ImageView img;
	/** 保存按钮 */
	private TextView save;
	/** 返回 */
	// private LinearLayout layout_finish;
	/** 取消 */
	private TextView cancle;
	private CutView clipview;

	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();

	/** 动作标志：无 */
	private static final int NONE = 0;
	/** 动作标志：拖动 */
	private static final int DRAG = 1;
	/** 动作标志：缩放 */
	private static final int ZOOM = 2;
	/** 初始化动作标志 */
	private int mode = NONE;

	/** 记录起始坐标 */
	private PointF start = new PointF();
	/** 记录缩放时两指中间点坐标 */
	private PointF mid = new PointF();
	private float oldDist = 1f;

	private Bitmap bitmap;
	private String mPath;
	public int screenWidth = 0;
	public int screenHeight = 0;
	/** 裁剪图片地址 */
	public String filename = Environment.getExternalStorageDirectory()
			.getPath() + "/clip.png";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cut_image);
		getWindowWH();
		mPath = getIntent().getStringExtra("path");
		bitmap = createBitmap(mPath, screenWidth, screenHeight);

		int degreee = readBitmapDegree(mPath);
		if (bitmap != null) {
			if (degreee == 0) {
				// mClipImageLayout.setImageBitmap(bitmap);
			} else {
				bitmap = rotateBitmap(degreee, bitmap);
			}
		} else {
			finish();
		}

		img = (ImageView) this.findViewById(R.id.img);
		img.setOnTouchListener(this);

		ViewTreeObserver observer = img.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@SuppressWarnings("deprecation")
			public void onGlobalLayout() {
				img.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				initClipView(img.getTop(), bitmap);
			}
		});
		save = (TextView) findViewById(R.id.save);
		cancle = (TextView) findViewById(R.id.cancle);
		cancle.setOnClickListener(this);
		save.setOnClickListener(this);
	}

	/**
	 * 获取屏幕的高和宽
	 */
	private void getWindowWH() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
	}

	public Bitmap createBitmap(String path, int w, int h) {
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			// 这里是整个方法的关键，inJustDecodeBounds设为true时将不为图片分配内存。
			BitmapFactory.decodeFile(path, opts);
			int srcWidth = opts.outWidth;// 获取图片的原始宽度
			int srcHeight = opts.outHeight;// 获取图片原始高度
			int destWidth = 0;
			int destHeight = 0;
			// 缩放的比例
			double ratio = 0.0;
			if (srcWidth < w || srcHeight < h) {
				ratio = 0.0;
				destWidth = srcWidth;
				destHeight = srcHeight;
			} else if (srcWidth > srcHeight) {// 按比例计算缩放后的图片大小，maxLength是长或宽允许的最大长度
				ratio = (double) srcWidth / w;
				destWidth = w;
				destHeight = (int) (srcHeight / ratio);
			} else {
				ratio = (double) srcHeight / h;
				destHeight = h;
				destWidth = (int) (srcWidth / ratio);
			}
			BitmapFactory.Options newOpts = new BitmapFactory.Options();
			// 缩放的比例，缩放是很难按准备的比例进行缩放的，目前我只发现只能通过inSampleSize来进行缩放，其值表明缩放的倍数，SDK中建议其值是2的指数值
			newOpts.inSampleSize = (int) ratio + 1;
			// inJustDecodeBounds设为false表示把图片读进内存中
			newOpts.inJustDecodeBounds = false;
			// 设置大小，这个一般是不准确的，是以inSampleSize的为准，但是如果不设置却不能缩放
			newOpts.outHeight = destHeight;
			newOpts.outWidth = destWidth;
			// 获取缩放后图片
			return BitmapFactory.decodeFile(path, newOpts);
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

	/**
	 * 初始化截图区域，并将源图按裁剪框比例缩放
	 * 
	 * @param top
	 * @param bitmap
	 */
	private void initClipView(int top, final Bitmap bitmap) {
		// bitmap = BitmapFactory.decodeResource(this.getResources(),
		// R.drawable.a1111111);
		clipview = new CutView(CutPictureAty.this);
		clipview.setCustomTopBarHeight(top);
		clipview.addOnDrawCompleteListener(new OnDrawListenerComplete() {

			public void onDrawCompelete() {
				clipview.removeOnDrawCompleteListener();
				int clipHeight = clipview.getClipHeight();
				int clipWidth = clipview.getClipWidth() + 50;
//				int midX = clipview.getClipLeftMargin() + (clipWidth / 2);
				int midY = clipview.getClipTopMargin() + (clipHeight / 2);

				int imageWidth = bitmap.getWidth();
				int imageHeight = bitmap.getHeight();

				// 按裁剪框求缩放比例
				float scale = (clipWidth * 1.0f) / imageWidth;
				if (imageWidth > imageHeight) {
					scale = (clipHeight * 1.0f) / imageHeight;
				}
				// 起始中心点
//				float imageMidX = imageWidth / 2;
				float imageMidY = clipview.getCustomTopBarHeight()
						+ imageHeight * scale / 2;
				img.setScaleType(ScaleType.MATRIX);

				// 缩放
				matrix.postScale(scale, scale);
				// 平移
				matrix.postTranslate(0, midY - imageMidY);

				img.setImageMatrix(matrix);
				img.setImageBitmap(bitmap);
			}
		});

		this.addContentView(clipview, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			// 设置开始点位置
			start.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY()
						- start.y);
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}
		view.setImageMatrix(matrix);
		return true;
	}

	/**
	 * 多点触控时，计算最先放下的两指距离
	 * 
	 * @param event
	 * @return
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * 多点触控时，计算最先放下的两指中心坐标
	 * 
	 * @param point
	 * @param event
	 */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.save:
			getBitmap();
			Intent intent = new Intent();
			intent.putExtra("path", filename);
			setResult(Activity.RESULT_OK, intent);
			finish();
			break;
		case R.id.cancle:
			finish();
			break;
		}

	}

	// 旋转图片
	private Bitmap rotateBitmap(int angle, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		return resizedBitmap;
	}

	// 读取图像的旋转度
	private int readBitmapDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 获取裁剪框内截图
	 * 
	 * @return
	 */
	private Bitmap getBitmap() {

		// 获取截屏
		View view = this.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();

		int contentTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT)
				.getTop();
		Bitmap finalBitmap = Bitmap.createBitmap(view.getDrawingCache(),
				clipview.getClipLeftMargin(), clipview.getClipTopMargin()
						+ contentTop, clipview.getClipWidth(),
				clipview.getClipHeight());

		savaBitmap(finalBitmap);
		// 释放资源
		view.destroyDrawingCache();

		return finalBitmap;
	}

	/**
	 * 保存bitmap对象到本地
	 * 
	 * @param bitmap
	 */
	public void savaBitmap(Bitmap bitmap) {
		File f = new File(filename);
		FileOutputStream fOut = null;
		try {
			f.createNewFile();
			fOut = new FileOutputStream(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);// 把Bitmap对象解析成流
		try {
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
