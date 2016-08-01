# UploadImage
这是一个关于上传头像自定义裁剪图片的demo，里面解决了三星的旋转问题

public class MainAty extends Activity implements OnClickListener {
	private final int CAMERA_WITH_DATA = 1;
	/** 本地图片选取标志 */
	private static final int FLAG_CHOOSE_IMG = 2;
	/** 截取结束标志 */
	private static final int FLAG_MODIFY_FINISH = 3;
	public static final String TMP_PATH = "temp.jpg";
	private Context context;
	private ImageView img_pic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		img_pic = (ImageView) findViewById(R.id.img_pic);
		img_pic.setOnClickListener(this);
		context = MainAty.this;
	}

	/** 弹出选择照片菜单 */
	public void showSelectPictureMenu() {
		new SelectDialog(context)
				.builder()
				.setCancelable(true)
				.setTitle("请选择操作")
				.setCanceledOnTouchOutside(true)
				.addSelectItem("相机", SelectItemColor.Green,
						new OnSelectItemClickListener() {
							@Override
							public void onClick(int which) {
								startCamera();
							}
						})
				.addSelectItem("图库", SelectItemColor.Green,
						new OnSelectItemClickListener() {
							@Override
							public void onClick(int which) {
								startAlbum();
							}
						}).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FLAG_CHOOSE_IMG && resultCode == RESULT_OK) {
			if (data != null) {
				Uri uri = data.getData();
				if (!TextUtils.isEmpty(uri.getAuthority())) {
					Cursor cursor = getContentResolver().query(uri,
							new String[] { MediaStore.Images.Media.DATA },
							null, null, null);
					if (null == cursor) {
						Toast.makeText(getApplicationContext(), "图片没找到", 0)
								.show();
						return;
					}
					cursor.moveToFirst();
					String path = cursor.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					cursor.close();

					Intent intent = new Intent(this, CutPictureAty.class);
					intent.putExtra("path", path);
					startActivityForResult(intent, FLAG_MODIFY_FINISH);
				} else {
					Intent intent = new Intent(this, CutPictureAty.class);
					intent.putExtra("path", uri.getPath());
					startActivityForResult(intent, FLAG_MODIFY_FINISH);
				}
			}
		} else if (requestCode == FLAG_MODIFY_FINISH && resultCode == RESULT_OK) {
			if (data != null) {
				final String path = data.getStringExtra("path");
				Bitmap b = BitmapFactory.decodeFile(path);
				img_pic.setImageBitmap(b);
			}
		}
		switch (requestCode) {
		case CAMERA_WITH_DATA:
			// 照相机程序返回的,再次调用图片剪辑程序去修剪图片
			startCropImageActivity(Environment.getExternalStorageDirectory()
					+ "/" + TMP_PATH);
			break;
		}
	}

	// 裁剪图片的Activity
	private void startCropImageActivity(String path) {
		Intent intent = new Intent(this, CutPictureAty.class);
		intent.putExtra("path", path);
		startActivityForResult(intent, FLAG_MODIFY_FINISH);
	}

	private void startAlbum() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, FLAG_CHOOSE_IMG);
	}

	private void startCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
				Environment.getExternalStorageDirectory(), TMP_PATH)));
		startActivityForResult(intent, CAMERA_WITH_DATA);
	}

	/**
	 * 通过uri获取文件路径
	 * 
	 * @param mUri
	 * @return
	 */
	public String getFilePath(Uri mUri) {
		try {
			if (mUri.getScheme().equals("file")) {
				return mUri.getPath();
			} else {
				return getFilePathByUri(mUri);
			}
		} catch (FileNotFoundException ex) {
			return null;
		}
	}

	// 获取文件路径通过url
	private String getFilePathByUri(Uri mUri) throws FileNotFoundException {
		Cursor cursor = getContentResolver()
				.query(mUri, null, null, null, null);
		cursor.moveToFirst();
		return cursor.getString(1);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_pic:
			showSelectPictureMenu();
			break;

		default:
			break;
		}
	}

}

