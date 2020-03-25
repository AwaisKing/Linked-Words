package awais.backworddictionary.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import awais.backworddictionary.DictionaryFragment;

public class DictionaryFragmentsAdapter extends FragmentStatePagerAdapter {
    private final ArrayList<DictionaryFragment> fragmentList;
    private final ArrayList<String> titlesList;

    public DictionaryFragmentsAdapter(final FragmentManager manager, final int tabsSize) {
        super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragmentList = new ArrayList<>(tabsSize);
        titlesList = new ArrayList<>(tabsSize);
    }

    public void addFragment(final String title) {
        fragmentList.add(new DictionaryFragment());
        titlesList.add(title);
    }

    public void setFragments(@NonNull final String... titles) {
        fragmentList.clear();
        titlesList.clear();

        for (final String title : titles) {
            fragmentList.add(new DictionaryFragment());
            titlesList.add(title);
        }
    }

    public boolean isEmpty() {
        return titlesList.isEmpty() || fragmentList.isEmpty();
    }

    @NonNull
    @Override
    public DictionaryFragment getItem(final int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return titlesList.get(position);
    }

    public int fragmentIndex(@NonNull final CharSequence title) {
        for (int i = 0; i < titlesList.size(); i++)
            if (title.equals(titlesList.get(i))) return i;
        return -1;
    }
}