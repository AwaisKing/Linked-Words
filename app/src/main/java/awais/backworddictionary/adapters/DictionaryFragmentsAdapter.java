package awais.backworddictionary.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import awais.backworddictionary.DictionaryFragment;

public class DictionaryFragmentsAdapter extends FragmentStatePagerAdapter {
    public final List<String> mFragmentTitleList = new ArrayList<>();
    public final List<DictionaryFragment> mFragmentList = new ArrayList<>();

    public DictionaryFragmentsAdapter(FragmentManager manager) {
        super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    public void addFragment(String title) {
        mFragmentList.add(new DictionaryFragment());
        mFragmentTitleList.add(title);
    }

    public void setFragments(@NonNull String... titles) {
        mFragmentList.clear();
        mFragmentTitleList.clear();

        for (String title : titles) {
            mFragmentList.add(new DictionaryFragment());
            mFragmentTitleList.add(title);
        }
    }

    public boolean isEmpty() {
        return mFragmentTitleList.isEmpty() || mFragmentList.isEmpty();
    }

    @NonNull
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