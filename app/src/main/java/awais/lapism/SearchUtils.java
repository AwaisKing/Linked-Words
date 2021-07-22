package awais.lapism;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

final class SearchUtils {
    static boolean isRtlLayout(@Nullable final Context context) {
        final Resources res = context != null ? context.getResources() : Resources.getSystem();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                res.getConfiguration().getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    /*

    if (isRTL()) {
        // The view has RTL layout
        mSearchArrow.setDirection(SearchArrowDrawable.ARROW_DIRECTION_END);
    } else {
        // The view has LTR layout
        mSearchArrow.setDirection(SearchArrowDrawable.ARROW_DIRECTION_START);
    }

    public static boolean isRTL() {
        return isRTL(Utils.defaultLocale);
    }

    private static boolean isRTL(Locale locale) {
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    public static boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
   */
}