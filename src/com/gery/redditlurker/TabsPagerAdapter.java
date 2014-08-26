package com.gery.redditlurker;

import com.gery.database.SmartFragmentStatePagerAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {
	 
	private AllSubRedditsFragment allSubRedditsFragment;
	private EnteredSubRedditsFragment enteredSubRedditsFragment;
	
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            // List Subreddits
        	allSubRedditsFragment = new AllSubRedditsFragment();
            return allSubRedditsFragment;
        case 1:
            //Entered Subreddits
        	enteredSubRedditsFragment = new EnteredSubRedditsFragment();
            return enteredSubRedditsFragment;
        }
 
        return null;
    }
    
    public Fragment getFragmentAlreadyCreated(int index)
    {
    	 switch (index) {
         case 0:
             // List Subreddits
             return allSubRedditsFragment;
         case 1:
             //Entered Subreddits
             return enteredSubRedditsFragment;
         }
  
         return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }
 
}