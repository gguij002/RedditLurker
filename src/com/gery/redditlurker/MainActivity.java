package com.gery.redditlurker;

import com.gery.database.SubRedditsDataSource;
import com.gery.redditlurker.R.id;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar actionBar;
	// Tab titles
	private String[] tabs = { "SubReddits", "Favorite SubReddits" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initilization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
		}

		/**
		 * on swiping the viewpager make respective tab selected
		 * */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public boolean onSearchRequested() {

		Intent i = new Intent(MainActivity.this, SubRedditChannelActivity.class);
		startActivity(i);

		return false; // don't go ahead and show the search box
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);
		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search_widget).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		MenuItem item = menu.findItem(R.id.action_fav);
		item.setVisible(false);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * On selecting action bar icons
	 * */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case R.id.action_search_widget:
			item.getActionView().findViewById(id.action_search_widget);
			goToSubReddit();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void goToSubReddit() {
		Intent i = new Intent(MainActivity.this, SubRedditChannelActivity.class);
		startActivity(i);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		SubRedditsDataSource srDataSource = new SubRedditsDataSource(this);
		srDataSource.open();
		
		Fragment fragment = mAdapter.getItem(tab.getPosition());
		
		if (tab.getPosition() == 1) {
			EnteredSubRedditsFragment enteredFragment = (EnteredSubRedditsFragment) fragment;
			enteredFragment.UpdateSubRedditList(srDataSource.getAllSubReddit());
		} else if (tab.getPosition() == 0){
			AllSubRedditsFragment allFragment = (AllSubRedditsFragment) fragment;
			allFragment.UpdateFavs(srDataSource.getAllSubRedditsID());
		}
		srDataSource.close();
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}
}
