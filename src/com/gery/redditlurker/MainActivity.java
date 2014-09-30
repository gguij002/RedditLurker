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
	private String[] tabs = { "Front Page", "All", "Favorite" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initilization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		setTitle("Reddit Lurker");

		mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);

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
		goToSubReddit();

		return false; // don't go ahead and show the search box
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);
		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		MenuItem var = menu.findItem(R.id.action_search_widget);
		var.setVisible(true);
		SearchView searchView = (SearchView) var.getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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
			// case android.R.id.home:
			// Intent intent = new Intent(this, ActivityFrontPage.class);
			// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// startActivity(intent);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void goToSubReddit() {
		Intent i = new Intent(MainActivity.this, ActivitySubRedditChannel.class);
		startActivity(i);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

		Fragment fragment = mAdapter.getItem(tab.getPosition());

		if (tab.getPosition() == 2) {
			SubRedditsDataSource srDataSource = new SubRedditsDataSource(this);
			srDataSource.open();
			EnteredSubRedditsFragment enteredFragment = (EnteredSubRedditsFragment) fragment;
			enteredFragment.UpdateSubRedditList(srDataSource.getAllSubReddit());
			srDataSource.close();
		} else if (tab.getPosition() == 1) {
			SubRedditsDataSource srDataSource = new SubRedditsDataSource(this);
			srDataSource.open();
			AllSubRedditsFragment allFragment = (AllSubRedditsFragment) fragment;
			allFragment.UpdateFavs(srDataSource.getAllSubRedditsID());
			srDataSource.close();
		} else if (tab.getPosition() == 0) {
			System.out.println("Front PaGE FRAgment");
		}

		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}
}
