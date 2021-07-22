package awais.backworddictionary.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

import awais.backworddictionary.DictionaryFragment;

public final class DictionaryFragmentsAdapter extends FragmentStateAdapter {
    private final ArrayList<DictionaryFragment> fragmentList;
    private final ArrayList<String> titlesList;

    public DictionaryFragmentsAdapter(@NonNull final FragmentActivity fragmentActivity, final int tabsSize) {
        super(fragmentActivity);
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
    public Fragment createFragment(final int position) {
        return fragmentList.get(position);
    }

    public DictionaryFragment getItem(final int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    public String getPageTitle(final int position) {
        return titlesList.get(position);
    }

    public int fragmentIndex(@NonNull final CharSequence title) {
        for (int i = 0; i < titlesList.size(); i++)
            if (title.equals(titlesList.get(i))) return i;
        return -1;
    }
}