package in.delbird.delbirddriver.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import in.delbird.delbirddriver.fragment.HistoryPackageFragment;
import in.delbird.delbirddriver.fragment.HistoryUserFragment;


/**
 * Created by Dharmendra on 28/1/16.
 */
public class HistoryViewPagerAdapter extends FragmentPagerAdapter {

    CharSequence Titles[];
    int NumbOfTabs;

    public HistoryViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) // if the position is 0 we are returning the First tab
        {
            HistoryUserFragment fragment2 = new HistoryUserFragment();
            return fragment2;
        } else             // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
        {

            HistoryPackageFragment fragment1 = new HistoryPackageFragment();
            return fragment1;

        }


    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }


    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}
