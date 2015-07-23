package moe.feng.bilinyan.ui.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.gson.Gson;

import moe.feng.bilinyan.R;
import moe.feng.bilinyan.api.IndexApi;
import moe.feng.bilinyan.model.BasicMessage;
import moe.feng.bilinyan.model.Index;
import moe.feng.bilinyan.ui.adapter.pager.BannerPagerAdapter;
import moe.feng.bilinyan.ui.adapter.pager.HomePagerAdapter;
import moe.feng.bilinyan.ui.common.LazyFragment;
import moe.feng.bilinyan.util.AsyncTask;
import moe.feng.bilinyan.util.Utility;
import moe.feng.bilinyan.view.CircleIndicator;
import moe.feng.bilinyan.view.SlidingTabLayout;
import ru.noties.scrollable.CanScrollVerticallyDelegate;
import ru.noties.scrollable.OnScrollChangedListener;
import ru.noties.scrollable.ScrollableLayout;

public class SectionHomeFragment extends LazyFragment {

	private ViewPager mBannerPager, mTabPager;
	private BannerPagerAdapter mBannerAdapter;
	private HomePagerAdapter mHomeAdapter;
	private CircleIndicator mBannerIndicator;
	private SlidingTabLayout mSlidingTab;
	private ScrollableLayout mScrollableLayout;

	private View mAppBarLayout, mAppBarBackground;

	private Index mIndexData;

	private int APP_BAR_HEIGHT, TOOLBAR_HEIGHT, TAB_HEIGHT, STATUS_BAR_HEIGHT = 0;

	@Override
	public int getLayoutResId() {
		return R.layout.fragment_section_home;
	}

	@Override
	public void finishCreateView(Bundle state) {
		APP_BAR_HEIGHT = getResources().getDimensionPixelSize(R.dimen.appbar_parallax_max_height);
		TAB_HEIGHT = getResources().getDimensionPixelSize(R.dimen.appbar_tab_height);
		TOOLBAR_HEIGHT = getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);
		if (Build.VERSION.SDK_INT >= 19) {
			STATUS_BAR_HEIGHT = Utility.getStatusBarHeight(getApplicationContext());
		}

		mBannerPager = $(R.id.banner_pager);
		mTabPager = $(R.id.tab_pager);
		mBannerIndicator = $(R.id.pager_indicator);
		mSlidingTab = $(R.id.sliding_tabs);
		mAppBarLayout = $(R.id.appbar_layout);
		mAppBarBackground = $(R.id.appbar_background);
		mScrollableLayout = $(R.id.scrollable);

		ViewCompat.setElevation(mAppBarLayout, getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));

		mBannerAdapter = new BannerPagerAdapter(getChildFragmentManager(), 3);
		mBannerPager.setAdapter(mBannerAdapter);
		mBannerIndicator.setViewPager(mBannerPager);

		mHomeAdapter = new HomePagerAdapter(getChildFragmentManager(), getApplicationContext());
		mTabPager.setAdapter(mHomeAdapter);
		mSlidingTab.setViewPager(mTabPager);
		mSlidingTab.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
		mSlidingTab.setSelectedIndicatorColors(getResources().getColor(android.R.color.white));
		mSlidingTab.setDistributeEvenly(true);
		mSlidingTab.setOnTabItemClickListener(new SlidingTabLayout.OnTabItemClickListener() {
			@Override
			public void onTabItemClick(int pos) {
				if (pos != mTabPager.getCurrentItem()) return;
				mHomeAdapter.scrollToTop(pos);
			}
		});

		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT
		);
		lp.setMargins(0, STATUS_BAR_HEIGHT + TOOLBAR_HEIGHT + TAB_HEIGHT, 0, 0);
		mTabPager.setLayoutParams(lp);
		mScrollableLayout.setMaxScrollY(APP_BAR_HEIGHT - STATUS_BAR_HEIGHT - TOOLBAR_HEIGHT - TAB_HEIGHT);
		mScrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
			@Override
			public boolean canScrollVertically(int i) {
				return mHomeAdapter.canScrollVertically(mTabPager.getCurrentItem(), i);
			}
		});
		mScrollableLayout.setOnScrollChangedListener(new OnScrollChangedListener() {
			@Override
			public void onScrollChanged(int y, int oldY, int maxY) {
				float tabsTransitionY = y < maxY ? 0 : y - maxY;
				mSlidingTab.setTranslationY(tabsTransitionY);
				mBannerAdapter.setBannerImageTransitionY(tabsTransitionY * 0.7f);

				float alpha = ((float) y) / (float) maxY;
				mAppBarBackground.setAlpha(alpha);
			}
		});

		new IndexGetApi().execute();
	}

	public Index getIndexData() {
		return this.mIndexData;
	}

	private class IndexGetApi extends AsyncTask<Void, Void, BasicMessage<Index>> {

		@Override
		protected BasicMessage<Index> doInBackground(Void... params) {
			return IndexApi.getIndex();
		}

		@Override
		protected void onPostExecute(BasicMessage<Index> msg) {
			if (msg != null && msg.getCode() == BasicMessage.CODE_SUCCEED) {
				mIndexData = msg.getObject();
				mHomeAdapter.notifyIndexDataUpdateAll(mIndexData);
			} else {

			}
		}

	}

}
