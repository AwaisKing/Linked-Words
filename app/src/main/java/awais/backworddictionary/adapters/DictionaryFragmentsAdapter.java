package awais.backworddictionary.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

import awais.backworddictionary.DictionaryFragment;
import awais.backworddictionary.models.Tab;

public final class DictionaryFragmentsAdapter extends FragmentStateAdapter {
    private final ArrayList<DictionaryFragment> fragmentList = new ArrayList<>();

    public DictionaryFragmentsAdapter(@NonNull final FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void addFragment(final Tab tab) {
        fragmentList.add(new DictionaryFragment().setTab(tab));
        fragmentList.trimToSize();
    }

    public void setFragments(@NonNull final Tab... tabs) {
        fragmentList.clear();
        for (final Tab tab : tabs) fragmentList.add(new DictionaryFragment().setTab(tab));
        fragmentList.trimToSize();
    }

    @Override
    public int getItemCount() {
        return fragmentList == null ? 0 : fragmentList.size();
    }

    @NonNull
    @Override
    public Fragment createFragment(final int position) {
        return fragmentList.get(position);
    }

    public DictionaryFragment getItem(final int position) {
        return fragmentList.get(position);
    }

    public int getPageTitle(final int position) {
        return getItem(position).getTab().getTabName();
    }

    public int fragmentIndex(final int title) {
        for (int i = 0; i < fragmentList.size(); i++) if (title == fragmentList.get(i).getTab().getTabName()) return i;
        return -1;
    }
}