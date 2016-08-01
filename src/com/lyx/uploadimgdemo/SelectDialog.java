package com.lyx.uploadimgdemo;

import java.util.ArrayList;
import java.util.List;

import com.lyx.uploadimgdemo.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

public class SelectDialog {
	private Context context;
	private Dialog dialog;
	private TextView txt_title;
	private TextView txt_cancel;
	private LinearLayout lLayout_content;
	private ScrollView sLayout_content;
	private boolean showTitle = false;
	private List<SelectItem> SelectItemList;
	private Display display;

	public SelectDialog(Context context) {
		this.context = context;
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		display = windowManager.getDefaultDisplay();
	}

	@SuppressWarnings("deprecation")
	public SelectDialog builder() {
		// 获取Dialog布局
		View view = LayoutInflater.from(context).inflate(
				R.layout.toast_select, null);

		// 设置Dialog最小宽度为屏幕宽度
		view.setMinimumWidth(display.getWidth());

		// 获取自定义Dialog布局中的控件
		sLayout_content = (ScrollView) view.findViewById(R.id.sLayout_content);
		lLayout_content = (LinearLayout) view
				.findViewById(R.id.lLayout_content);
		txt_title = (TextView) view.findViewById(R.id.txt_title);
		txt_cancel = (TextView) view.findViewById(R.id.txt_cancel);
		txt_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		// 定义Dialog布局和参数
		dialog = new Dialog(context, R.style.DialogStyle);
		dialog.setContentView(view);
		Window dialogWindow = dialog.getWindow();
		dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.x = 0;
		lp.y = 0;
		dialogWindow.setAttributes(lp);

		return this;
	}

	public SelectDialog setTitle(String title) {
		showTitle = true;
		txt_title.setVisibility(View.VISIBLE);
		txt_title.setText(title);
		return this;
	}

	public SelectDialog setCancelable(boolean cancel) {
		dialog.setCancelable(cancel);
		return this;
	}

	public SelectDialog setCanceledOnTouchOutside(boolean cancel) {
		dialog.setCanceledOnTouchOutside(cancel);
		return this;
	}

	/**
	 * 添加条目布局
	 * 
	 * @param itemName
	 *            条目名称
	 * @param color
	 *            条目字体颜色，设置null则默认蓝色
	 * @param listener
	 * @return
	 */
	public SelectDialog addSelectItem(String itemName,
			SelectItemColor color, OnSelectItemClickListener listener) {
		if (SelectItemList == null) {
			SelectItemList = new ArrayList<SelectItem>();
		}
		SelectItemList.add(new SelectItem(itemName, color, listener));
		return this;
	}

	/** 设置条目布局 */
	@SuppressWarnings("deprecation")
	private void setSelectItems() {
		if (SelectItemList == null || SelectItemList.size() <= 0) {
			return;
		}

		int size = SelectItemList.size();

		// 高度控制，非最佳解决办法
		// 添加条目过多的时候控制高度
		if (size >= 7) {
			LinearLayout.LayoutParams params = (LayoutParams) sLayout_content
					.getLayoutParams();
			params.height = display.getHeight() / 2;
			sLayout_content.setLayoutParams(params);
		}

		// 循环添加条目
		for (int i = 1; i <= size; i++) {
			final int index = i;
			SelectItem SelectItem = SelectItemList.get(i - 1);
			String strItem = SelectItem.name;
			SelectItemColor color = SelectItem.color;
			final OnSelectItemClickListener listener = (OnSelectItemClickListener) SelectItem.itemClickListener;

			TextView textView = new TextView(context);
			// 设置文本内容
			textView.setText(strItem);
			// 设置文本字号
			textView.setTextSize(18);
			// 设置文本居中
			textView.setGravity(Gravity.CENTER);
			// 设置文本加粗
			// textView.setTypeface(Typeface.DEFAULT_BOLD);

			// 设置背景图片
			if (size == 1) {
				if (showTitle) {
					textView.setBackgroundResource(R.drawable.photo_bottom_selector);
				} else {
					textView.setBackgroundResource(R.drawable.photo_single_selector);
				}
			} else {
				if (showTitle) {
					if (i >= 1 && i < size) {
						textView.setBackgroundResource(R.drawable.photo_middle_selector);
					} else {
						textView.setBackgroundResource(R.drawable.photo_bottom_selector);
					}
				} else {
					if (i == 1) {
						textView.setBackgroundResource(R.drawable.photo_top_selector);
					} else if (i < size) {
						textView.setBackgroundResource(R.drawable.photo_middle_selector);
					} else {
						textView.setBackgroundResource(R.drawable.photo_bottom_selector);
					}
				}
			}

			// 设置文本颜色
			if (color == null) {
				// 设置文本默认为蓝色
				textView.setTextColor(Color.parseColor(SelectItemColor.Blue
						.getName()));
			} else {
				textView.setTextColor(Color.parseColor(color.getName()));
			}

			// 高度
			float scale = context.getResources().getDisplayMetrics().density;
			int height = (int) (45 * scale + 0.5f);
			// 设置文本参数
			textView.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, height));

			// 点击事件
			textView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.onClick(index);
					dialog.dismiss();
				}
			});
			lLayout_content.addView(textView);
		}
	}

	// android.app.Dialog@2f422fe9
	/** 显示条目布局 */
	public void show() {
		setSelectItems();
		dialog.show();

	}

	public void setfinish() {
		if (((Activity) context).isFinishing()) {
			((Activity) context).finish();
		}
	}

	/** 条目点击监听 */
	public interface OnSelectItemClickListener {
		void onClick(int which);
	}

	/** 条目 */
	public class SelectItem {
		String name;
		OnSelectItemClickListener itemClickListener;
		SelectItemColor color;

		public SelectItem(String name, SelectItemColor color,
				OnSelectItemClickListener itemClickListener) {
			this.name = name;
			this.color = color;
			this.itemClickListener = itemClickListener;
		}
	}

	/** 条目颜色 */
	public enum SelectItemColor {
		Blue("#037BFF"), Red("#FD4A2E"), Black("#000000"), Green("#34c2a6"), Gray(
				"#8F8F8F");

		private String name;

		private SelectItemColor(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
