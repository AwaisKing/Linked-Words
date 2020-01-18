package awais.lapism;

import android.content.Context;
import android.os.Build;

import androidx.core.view.ViewCompat;

final class SearchUtils {
//    public static boolean isRTL() {
//        return isRTL(Locale.getDefault());
//    }

    /*if (isRTL()) {
        // The view has RTL layout
        mSearchArrow.setDirection(SearchArrowDrawable.ARROW_DIRECTION_END);
    } else {
        // The view has LTR layout
        mSearchArrow.setDirection(SearchArrowDrawable.ARROW_DIRECTION_START);
    }*/

//    private static boolean isRTL(Locale locale) {
//        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
//        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
//                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
//    }

    static boolean isRtlLayout(final Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                context.getResources().getConfiguration().getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

//    public static boolean isLandscapeMode(Context context) {
//        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
//    }
}