package com.way.launcher;

import com.way.view.PagedView;

import android.content.Context;
import android.util.AttributeSet;

public class SceneWorkspace extends PagedView {
	public SceneWorkspace(Context context) {
		this(context, null);
	}

	public SceneWorkspace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SceneWorkspace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContentIsRefreshable = false;
		setDataIsReady();
		mFadeInAdjacentScreens = false;
	}

	@Override
	public void syncPages() {

	}

	@Override
	public void syncPageItems(int page, boolean immediate) {

	}

}
