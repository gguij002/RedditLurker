package com.gery.redditlurker;

import java.util.Calendar;

import com.gery.database.SubRedditsDataSource;
import com.gery.database.Utils;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SearchView;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener, OnMenuItemClickListener {

	private ViewPager viewPager;
	private TabsPagerAdapter mAdapter;
	private ActionBar actionBar;
	// Tab titles
	private String[] tabs = { "Front Page", "All", "Favorite" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.setPrefTheme(this);
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
		
		MenuItem changeTheme = menu.findItem(R.id.action_theme_question);
		changeTheme.setVisible(true);
		
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
			item.getActionView().findViewById(R.id.action_search_widget);
			goToSubReddit();
			return true;
		case R.id.action_theme_question:
		{
			final View anchor = findViewById(R.id.action_search_widget);
			PopupMenu popupMenu = new PopupMenu(this, anchor);
			popupMenu.setOnMenuItemClickListener(this);
			popupMenu.getMenuInflater().inflate(R.menu.themes_menu, popupMenu.getMenu());
			popupMenu.show();
			return true;
		}
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	private void restartSelf() {
	    AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
	    am.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 1000, // one second
	            PendingIntent.getActivity(this, 0, getIntent(), PendingIntent.FLAG_ONE_SHOT
	                    | PendingIntent.FLAG_CANCEL_CURRENT));
	    finish();
	}
	
	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	 switch (item.getItemId())
		             {
		     	        case R.id.action_theme_dark:
		     	        	Utils.savePrefTheme(MainActivity.this, Utils.THEME_DARK);
		     	        break;
		     	        case R.id.action_theme_light:
		     	        	Utils.savePrefTheme(MainActivity.this, Utils.THEME_LIGHT);
		     	        break;
		     	        case R.id.action_theme_mixed:
		     	        	Utils.savePrefTheme(MainActivity.this, Utils.THEME_BASE);
		     	        break;
		             }
		        	dialog.dismiss();
		         	restartSelf();
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		        	 dialog.dismiss();
		            break;
		        }
		    }
		};
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Restart Alert");
		builder.setMessage("Need to restart application to change theme\nContinue?").setPositiveButton("Yes Restart", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
		
		return false;
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
