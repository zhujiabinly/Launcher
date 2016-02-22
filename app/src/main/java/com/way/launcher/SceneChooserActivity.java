package com.way.launcher;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.way.util.SceneXmlParser;
import com.way.view.SceneImageView;
import com.way.view.TitlePageIndicator;

public class SceneChooserActivity extends Activity {
	public static final String SCENE_PROPERTIE_NAME = "properties.xml";
	static final String SCENE_PREVIEW_NAME = "scene_preview.jpg";
	static final String OEM_ROOT_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath();
	static final String SCENE_DIR_PATH = OEM_ROOT_PATH + "/scene/";

	private ViewPager mViewPager;
	private List<String> mScenePathList; // 保存场景资源的路径

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scene_chooser);
		mViewPager = (ViewPager) findViewById(R.id.scene_choose_viewpager);

		initScenePathList();
		List<String> scenePathList = mScenePathList;
		if (scenePathList != null && scenePathList.size() > 0) {
			List<ItemInfo> itemInfoList = new ArrayList<ItemInfo>();
			for (String path : scenePathList) {
				Log.i("waylog", "scenePath = " + path);
				String imagePath = path + File.separator + SCENE_PREVIEW_NAME;
				String namePath = path + File.separator + SCENE_PROPERTIE_NAME;
				String name = SceneXmlParser.getName(namePath);
				ItemInfo itemInfo = new ItemInfo();
				itemInfo.bm = BitmapFactory.decodeFile(imagePath);
				itemInfo.text = path
						.substring(path.lastIndexOf(File.separator) + 1);
				if(!TextUtils.isEmpty(name))
					itemInfo.text = name;
				Log.i("waylog", "itemInfo.text = " + itemInfo.text);
				itemInfoList.add(itemInfo);
			}
			ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this,
					itemInfoList);
			mViewPager.setAdapter(pagerAdapter);
		} else {
			Toast.makeText(this, R.string.scene_not_found, Toast.LENGTH_SHORT)
					.show();
			finish();
		}
		((TitlePageIndicator) findViewById(R.id.titles))
				.setViewPager(mViewPager);
	}

	// 获取scene目录下的子目录，并保存起来
	private void initScenePathList() {
		File sceneDir = new File(SCENE_DIR_PATH);
		if (!sceneDir.isDirectory()) {
			return;
		}
		File[] sceneSubDirs = sceneDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		if (sceneSubDirs == null)
			return;
		if (mScenePathList == null)
			mScenePathList = new ArrayList<String>();
		mScenePathList.clear();
		for (File dir : sceneSubDirs) {
			mScenePathList.add(dir.getAbsolutePath());
		}
	}

	static final class ItemInfo {
		Bitmap bm;
		String text;
	}

	class ViewPagerAdapter extends PagerAdapter {

		private Context mContext;
		private List<ItemInfo> mItemList;
		private LayoutInflater mInflater;

		public ViewPagerAdapter(Context context, List<ItemInfo> itemList) {
			mContext = context;
			mItemList = itemList;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			SceneImageView imageView = (SceneImageView) mInflater.inflate(
					R.layout.viewpager_item, null);
			ItemInfo itemInfo = mItemList.get(position);
			imageView.setImageBitmap(itemInfo.bm);
			container.addView(imageView);
			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.i("waylog", "pagerItem = " + position);
					startSceneLauncher(position);
				}
			});
			return imageView;
		}

		private void startSceneLauncher(int position) {
			Intent intent = new Intent(mContext, SceneLauncherActivity.class);
			String scenePath = mScenePathList.get(position);
			intent.putExtra("scene_path", scenePath);
			mContext.startActivity(intent);
			finish();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return mItemList.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mItemList.get(position).text;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

	}
}