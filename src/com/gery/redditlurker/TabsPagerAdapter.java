package com.gery.redditlurker;

import com.gery.database.SmartFragmentStatePagerAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends SmartFragmentStatePagerAdapter {
	 
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            // List Subreddits
            return new AllSubRedditsFragment();
        case 1:
            //Entered Subreddits
            return new EnteredSubRedditsFragment();
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }
 
}