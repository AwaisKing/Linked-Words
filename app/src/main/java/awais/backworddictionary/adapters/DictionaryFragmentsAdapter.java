package awais.backworddictionary.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.DictionaryFragment;

public class DictionaryFragmentsAdapter extends FragmentPagerAdapter {
    private final List<DictionaryFragment> mFragmentList = new ArrayList<>();
    public final List<String> mFragmentTitleList = new ArrayList<>();

    public DictionaryFragmentsAdapter(FragmentManager manager) {
        super(manager);
    }

    public void addFragment(DictionaryFragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public DictionaryFragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}