package awais.backworddictionary.custom;

import android.graphics.Typeface;
import android.util.Log;

import java.lang.reflect.Field;

@SuppressWarnings( "BooleanMethodIsAlwaysInverted" )
public class Utils {

    // thanks to weston
    // source: https://stackoverflow.com/questions/2711858/is-it-possible-to-set-a-custom-font-for-entire-of-application/16883281#16883281
    public static void setDefaultFont(String typefaceName, Typeface fontTypeface) {
        try {
            final Field staticField = Typeface.class.getDeclaredField(typefaceName);
            staticField.setAccessible(true);
            staticField.set(null, fontTypeface);
        } catch (Exception e) {
            Log.e("AWAISKING_APP", "", e);
        }
    }

    public static boolean isEmpty(String str) {
        if (str == null) return true;
        if (str.length() <= 0) return true;
        if (str.trim().isEmpty()) return true;
        return str.trim().equals("");
    }

    public static boolean isEmpty(CharSequence sequence) {
        if (sequence == null) return true;
        if (sequence.length() <= 0) return true;
        if (sequence == "") return true;
        return sequence.equals("");
    }
}