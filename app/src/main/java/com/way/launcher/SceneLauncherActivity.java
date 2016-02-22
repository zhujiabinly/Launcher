package com.way.launcher;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.way.bean.ApplicationInfo;
import com.way.view.AnalogClock;
import com.way.view.Calendar;
import com.way.view.NumberClock;
import com.way.view.SceneAllAppsPagedView;

public class SceneLauncherActivity extends Activity {
	private static final String TAG = "way";

	private final static int MENU_MAINMENU = Menu.FIRST + 1;
	private final static int MENU_SETTING = MENU_MAINMENU + 1;
	private final static int MENU_CLASSIC_LAUNCHER = MENU_SETTING + 1;
	private final static int MENU_SCENE_CHOOSE = MENU_CLASSIC_LAUNCHER + 1;

	private final static String TYPE_IMAGEBUTTON = "ImageButton";
	private final static String TYPE_MAINMENUBUTTON = "MainmenuButton";
	private final static String TYPE_ANALOGCLOCK = "AnalogClock";
	private final static String TYPE_ANIMVIEW = "AnimView";
	private final static String TYPE_CALENDAR = "Calendar";
	private final static String TYPE_NUMBERCLOCK = "NumberClock";

	private ViewGroup mSceneRoot;
	private ViewGroup mSceneAllAppsLayout;
	private SceneAllAppsPagedView mSceneAllApps;
	private SceneLauncherApplication mSceneLauncherApplication;

	private List<ImageView> mAnimViews;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scene_launcher);
		Log.d(TAG, "SceneLauncherActivity onCreate");
		mSceneLauncherApplication = (SceneLauncherApplication) getApplication();
		mSceneLauncherApplication.setLauncher(this);
		String scenePath = getIntent().getStringExtra("scene_path");
		if (scenePath != null) {
			mSceneLauncherApplication.setSelectScenePath(scenePath);
		}
		initAllViews();
		loadAllViews();
		bindAllapps();

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "SceneLauncherActivity onNewIntent intent = " + intent);
		hideAllapps();
		String scenePath = intent.getStringExtra("scene_path");
		if (scenePath != null
				&& !scenePath.equals(mSceneLauncherApplication
						.getSelectScenePath())) {
			mSceneLauncherApplication.setSelectScenePath(scenePath);
			mSceneLauncherApplication.resetScenePageInfoList();
			mSceneLauncherApplication.resetHotseatInfo();
			mSceneRoot.removeAllViews();
			loadAllViews();
			bindAllapps();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
		settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		menu.add(0, MENU_MAINMENU, 0, R.string.menu_mainmenu);
		menu.add(0, MENU_SETTING, 0, R.string.menu_settings)
				.setIntent(settings);
		if (mSceneLauncherApplication.getScenePathList() != null
				&& mSceneLauncherApplication.getScenePathList().size() > 1) {
			menu.add(0, MENU_SCENE_CHOOSE, 0, R.string.menu_scene_choose);
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		if (isAllappsVisible())
			return false;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_MAINMENU:
			showAllapps();
			return true;

		case MENU_SCENE_CHOOSE:
			startSceneChoose();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Log.d(TAG, "onWindowFocusChanged hasFocus = " + hasFocus);
		if (hasFocus) {
			animViewStart();
		} else {
			animViewStop();
		}
	}

	@Override
	public void onBackPressed() {
		if (isAllappsVisible()) {
			hideAllapps();
		}
	}

	// 加载主菜单
	public void bindAllapps() {
		mSceneAllApps.setApps(mSceneLauncherApplication.getAllApps());
	}

	public void onAppsItemClick(View v) {
		final ApplicationInfo info = (ApplicationInfo) v.getTag();
		if (info != null) {
			mSceneLauncherApplication.startActivitySafely(info.intent);
		}
	}

	private void initAllViews() {
		mSceneRoot = (ViewGroup) findViewById(R.id.scene_root);
	}

	// 加载应用快捷图标
	private void loadAllViews() {
		loadScenePages();
		loadSceneHotseat();
		loadMainmenu();
	}

	// 加载场景页面
	private void loadScenePages() {
		if (mAnimViews == null) {
			mAnimViews = new ArrayList<ImageView>();
		} else {
			mAnimViews.clear();
		}
		List<ScenePageInfo> scenePages = mSceneLauncherApplication
				.getSenePageInfoList();
		if (scenePages != null && !scenePages.isEmpty()) {
			SceneWorkspace sceneWorkspace = new SceneWorkspace(this);
			mSceneRoot.addView(sceneWorkspace);
			for (ScenePageInfo page : scenePages) {
				FrameLayout layout = new FrameLayout(this);
				FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);
				layout.setBackgroundDrawable(getOrignShorcutDrawable(page.backgroundPath));
				sceneWorkspace.addView(layout, layoutParams);
				loadSceneShortcut(layout, page);
			}
		}
	}

	// 加载场景桌面hotseat
	private void loadSceneHotseat() {
		SceneHotseatInfo hotseat = mSceneLauncherApplication.getHotseatInfo();
		if (hotseat != null) {
			ImageView hotseatView = new ImageView(this);
			hotseatView
					.setBackgroundDrawable(getShortDrawable(hotseat.backgroundPath));
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(getAbsolutePosX(hotseat.x),
					getAbsolutePosY(hotseat.y), 0, 0);
			mSceneRoot.addView(hotseatView, layoutParams);
			if (hotseat.hotseats != null) {
				for (ShortcutInfo seatInfo : hotseat.hotseats) {
					View v = createView(seatInfo);
					layoutParams = new FrameLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					layoutParams.setMargins(getAbsolutePosX(seatInfo.x),
							getAbsolutePosY(seatInfo.y), 0, 0);
					mSceneRoot.addView(v, layoutParams);
				}
			}
		}
	}

	// 加载场景上的shortcut
	private void loadSceneShortcut(ViewGroup layout, ScenePageInfo info) {
		if (layout == null || info == null || info.children == null)
			return;
		for (ShortcutInfo shortcut : info.children) {
			View v = createView(shortcut);
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(getAbsolutePosX(shortcut.x),
					getAbsolutePosY(shortcut.y), 0, 0);
			layout.addView(v, layoutParams);
		}
	}

	private View createView(final ShortcutInfo info) {
		View view = null;
		if (info.type.equals(TYPE_IMAGEBUTTON)
				|| info.type.equals(TYPE_MAINMENUBUTTON)) {
			view = new ImageButton(this);
			view.setBackgroundDrawable(getShortDrawable(info.backgroundPath));
		} else if (info.type.equals(TYPE_ANALOGCLOCK)) {
			AnalogClockInfo analogClockInfo = (AnalogClockInfo) info;
			Drawable dial = getShortDrawable(analogClockInfo.backgroundPath);
			Drawable hourHand = getShortDrawable(analogClockInfo.handHour);
			Drawable minuteHand = getShortDrawable(analogClockInfo.handMinute);
			view = new AnalogClock(this, dial, hourHand, minuteHand);
		} else if (info.type.equals(TYPE_NUMBERCLOCK)) {
			NumberClockInfo numberClockInfo = (NumberClockInfo) info;
			Drawable timeBg = getShortDrawable(numberClockInfo.backgroundPath);
			Drawable timeColon = getShortDrawable(numberClockInfo.timeColon);
			List<Drawable> dTimeArray = new ArrayList<Drawable>();
			if (numberClockInfo.timePic != null
					&& numberClockInfo.timePic.length != 0) {
				for (String imageName : numberClockInfo.timePic) {
					dTimeArray.add(getShortDrawable(imageName));
				}
			}
			List<Drawable> dApmArray = new ArrayList<Drawable>();
			if (numberClockInfo.timeApm != null
					&& numberClockInfo.timeApm.length != 0) {
				for (String imageName : numberClockInfo.timeApm) {
					dApmArray.add(getShortDrawable(imageName));
				}
			}
			view = new NumberClock(this, timeBg, timeColon, dTimeArray,
					dApmArray);
		} else if (info.type.equals(TYPE_ANIMVIEW)) {
			FrameAnimInfo animInfo = (FrameAnimInfo) info;
			if (animInfo.anim != null && animInfo.anim.length != 0) {
				AnimationDrawable animDrawable = new AnimationDrawable();
				animDrawable.setOneShot(false);
				for (String imageName : animInfo.anim) {
					// start by way for getShortDrawable == null 20130831
					Drawable d = getShortDrawable(imageName);
					if (d != null)
						animDrawable.addFrame(d, 300);
					else
						Log.i("waylog", "getShortDrawable == null" + imageName);
					// end by way 20130831
				}
				view = new ImageView(this);
				view.setBackgroundDrawable(animDrawable);
				mAnimViews.add((ImageView) view);
			}
		} else if (info.type.equals(TYPE_CALENDAR)) {
			CalendarInfo calendarInfo = (CalendarInfo) info;
			Drawable caleBg = getShortDrawable(calendarInfo.backgroundPath);

			Drawable caleDot = getShortDrawable(calendarInfo.bmp_dot);
			Point posYear = new Point();
			posYear = calendarInfo.point_year;
			List<Drawable> dYearArray = new ArrayList<Drawable>();
			if (calendarInfo.bmp_year != null
					&& calendarInfo.bmp_year.length != 0) {
				for (String imageName : calendarInfo.bmp_year) {
					dYearArray.add(getShortDrawable(imageName));
				}
			}
			Point posMonth = new Point();
			posMonth = calendarInfo.point_month;
			List<Drawable> dMonthArray = new ArrayList<Drawable>();
			if (calendarInfo.bmp_month != null
					&& calendarInfo.bmp_month.length != 0) {
				for (String imageName : calendarInfo.bmp_month) {
					dMonthArray.add(getShortDrawable(imageName));
				}
			}
			Point posDate = new Point();
			posDate = calendarInfo.point_date;
			List<Drawable> dDateArray = new ArrayList<Drawable>();
			if (calendarInfo.bmp_date != null
					&& calendarInfo.bmp_date.length != 0) {
				for (String imageName : calendarInfo.bmp_date) {
					dDateArray.add(getShortDrawable(imageName));
				}
			}
			Point posWeek = new Point();
			posWeek = calendarInfo.point_week;
			List<Drawable> dWeekArray = new ArrayList<Drawable>();
			if (calendarInfo.bmp_week != null
					&& calendarInfo.bmp_week.length != 0) {
				for (String imageName : calendarInfo.bmp_week) {
					dWeekArray.add(getShortDrawable(imageName));
				}
			}
			view = new Calendar(this, caleBg, caleDot, posYear, dYearArray,
					posMonth, dMonthArray, posDate, dDateArray, posWeek,
					dWeekArray);
		}

		if (info.uri != null && !info.uri.isEmpty()) {
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						Intent intent = Intent.parseUri(info.uri, 0);
						mSceneLauncherApplication
								.startActivitySafelyForShortcut(intent);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			});
		} else if (info.type.equals("MainmenuButton")) {
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showAllapps();
				}
			});
		}
		return view;
	}

	private void loadMainmenu() {
		LayoutInflater flater = LayoutInflater.from(this);
		mSceneAllAppsLayout = (ViewGroup) flater.inflate(
				R.layout.scene_allapps, null);
		mSceneAllAppsLayout.setVisibility(View.INVISIBLE);
		mSceneRoot.addView(mSceneAllAppsLayout);
		mSceneAllApps = (SceneAllAppsPagedView) findViewById(R.id.scene_allapps);
	}

	private void showAllapps() {
		mSceneAllAppsLayout.setVisibility(View.VISIBLE);
		mSceneAllApps.flashScrollingIndicator(true);
		// mSceneAllAppsLayout.setFocusable(true);
		// mSceneAllAppsLayout.requestFocus();
	}

	private void hideAllapps() {
		mSceneAllAppsLayout.setVisibility(View.INVISIBLE);
		// mSceneAllAppsLayout.setFocusable(false);
	}

	private boolean isAllappsVisible() {
		return mSceneAllAppsLayout.getVisibility() == View.VISIBLE;
	}

	private void animViewStart() {
		if (mAnimViews != null && !mAnimViews.isEmpty()) {
			for (ImageView animView : mAnimViews) {
				AnimationDrawable animDrawable = (AnimationDrawable) animView
						.getBackground();
				animDrawable.start();
			}
		}
	}

	private void animViewStop() {
		if (mAnimViews != null && !mAnimViews.isEmpty()) {
			for (ImageView animView : mAnimViews) {
				AnimationDrawable animDrawable = (AnimationDrawable) animView
						.getBackground();
				animDrawable.stop();
			}
		}
	}

	private Drawable getOrignShorcutDrawable(String fileName) {
		Drawable d = null;
		Bitmap b = getShortcutBitmap(fileName);
		if (b != null) {
			d = new BitmapDrawable(getResources(), b);
		}
		return d;
	}

	private Drawable getShortDrawable(String fileName) {
		Drawable d = null;
		Bitmap b = getShortcutBitmap(fileName);
		if (b != null) {
			b = scaleBitmap(b);
		}
		if (b != null) {
			d = new BitmapDrawable(getResources(), b);
		}
		return d;
	}

	private Bitmap getShortcutBitmap(String fileName) {
		// get bm from customer path
		InputStream is = null;
		Bitmap bmp = null;
		try {
			is = new FileInputStream(
					SceneLayoutXmlParser.getCustomDrawablePath() + fileName);
			bmp = BitmapFactory.decodeStream(is);
			is.close();
		} catch (FileNotFoundException e) {
			try {
				// get bm from assets
				is = getAssets().open(
						SceneLayoutXmlParser.BACKGROUND_PATH + fileName);
				bmp = BitmapFactory.decodeStream(is);
				is.close();
			} catch (IOException e1) {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bmp;
	}

	private Bitmap scaleBitmap(Bitmap b) {
		Bitmap bmp = b;
		Matrix matrix = new Matrix();
		matrix.postScale(getWidthScale(), getHeightScale());
		bmp = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix,
				true);
		Log.d(TAG, "scaleBitmap orig width and height = (" + b.getWidth()
				+ ", " + b.getHeight() + ")");
		Log.d(TAG,
				"scaleBitmap after scale width and height = (" + bmp.getWidth()
						+ ", " + bmp.getHeight() + ")");
		return bmp;
	}

	// 获取宽度缩放比例
	private float getWidthScale() {
		final int contrastWidth = 480;
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		float scale = (float) metrics.widthPixels / contrastWidth;
		Log.d(TAG, "width scale = " + scale);
		return scale;
	}

	// 获取高度缩放比例
	private float getHeightScale() {
		final int contrastHeight = 800;
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		// unit pix, 25dp为状态栏的高度
		final int statusbarHeight = (int) (25 * metrics.density + 0.5f); 
		float scale = (float) (metrics.heightPixels - statusbarHeight)
				/ contrastHeight;
		Log.d(TAG, "height scale = " + scale);
		return scale;
	}

	// 从480x800分辨率下的坐标，转化为其他分辨率下的坐标
	private int getAbsolutePosX(int contrastX) {
		return (int) (getWidthScale() * contrastX);
	}

	private int getAbsolutePosY(int constrastY) {
		return (int) (getHeightScale() * constrastY);
	}
	
	//选择主题
	private void startSceneChoose() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClass(SceneLauncherActivity.this, SceneChooserActivity.class);
		startActivity(intent);
	}

}
