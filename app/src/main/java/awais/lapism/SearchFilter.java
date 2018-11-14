package awais.lapism;

class SearchFilter {
    private final String mTitle;
    private boolean mIsChecked;

    public SearchFilter(String title, boolean checked) {
        this.mTitle = title;
        this.mIsChecked = checked;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public boolean isChecked() {
        return this.mIsChecked;
    }

    public void setChecked(boolean checked) {
        this.mIsChecked = checked;
    }
}