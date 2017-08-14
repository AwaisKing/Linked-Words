package awais.backworddictionary.customweb;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class WebViewFallback implements CustomTabActivityHelper.CustomTabFallback {
    @Override
    public void openUri(Activity activity, Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}