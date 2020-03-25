package awais.backworddictionary.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.Random;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.R;

public class MyApps {
    public final static Icons[] iconsList;
    static {
        final Icons[] values = Icons.values();
        iconsList = new Icons[values.length - 1];
        int i = 0;
        for (final Icons value : values) if (!value.pkg.equals(BuildConfig.APPLICATION_ID)) iconsList[i++] = value;
    }

    public enum Icons {
        MEDIASCAN("awais.media.scanner", "mediaScanner", R.drawable.ms),
        ADDME("awais.addme", "AddMe", R.drawable.adm),
        LINKEDWORDS("awais.backworddictionary", "Linked Words", R.drawable.lw),
        QUODB("awais.quodb", "QuoDB", R.drawable.qdb),
        REVERSIFY("awais.reversify", "Reversify", R.drawable.rev),
        REVERSIFY_LITE("awais.reversify.lite", "Reversify Lite", R.drawable.revl),
        TESV("awais.skyrimconsole", "Skyrim Cheats", R.drawable.tesv),
        VIDEEZE("awais.videobar.play", "Videeze", R.drawable.vdz);
        @DrawableRes
        private final int icon;
        private final String name, pkg;

        Icons(final String pkg, final String name, @DrawableRes final int icon) {
            this.name = name;
            this.pkg = pkg;
            this.icon = icon;
        }
    }

    public static void openAppStore(@NonNull final Context context, final int position) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + MyApps.iconsList[position].pkg)));
    }

    public static void showAlertDialog(final Activity activity, final AdapterView.OnItemClickListener clickListener) {
        final DialogInterface.OnCancelListener cancelListener = d -> {
            if (clickListener != null) clickListener.onItemClick(null, null, -1, -1);
            else Process.killProcess(Process.myPid());
        };
        if (new Random().nextDouble() < (BuildConfig.DEBUG ? 0.117D : 0.617D)) {
            cancelListener.onCancel(null);
            return;
        }
        final GridView gridView = new GridView(activity);
        gridView.setAdapter(new ImageAdapter(activity));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(clickListener);
        final AlertDialog dialog = new AlertDialog.Builder(activity).setView(gridView).setTitle("Support my apps").create();
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
    }

    public static class ImageAdapter extends BaseAdapter {
        private final Context context;
        private final int size;

        public ImageAdapter(@NonNull final Context context) {
            this.context = context;
            this.size = (int) (80 * Resources.getSystem().getDisplayMetrics().density);
        }

        @Override
        public int getCount() {
            return iconsList != null ? iconsList.length : 0;
        }

        @Override
        public Object getItem(final int position) {
            return iconsList != null ? iconsList[position] : null;
        }

        @Override
        public long getItemId(final int position) {
            return 0;
        }

        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                final int sdkInt = Build.VERSION.SDK_INT;
                final LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final AppCompatImageView imageView = new AppCompatImageView(context);
                final AppCompatTextView textView = new AppCompatTextView(context);
                if (sdkInt >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                imageView.setAdjustViewBounds(true);
                linearLayout.addView(imageView, LinearLayout.LayoutParams.MATCH_PARENT, size);
                linearLayout.addView(textView);
                final int padding = size >> 2;
                if (sdkInt >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    linearLayout.setPaddingRelative(padding, padding, padding, padding);
                else linearLayout.setPadding(padding, padding, padding, padding);
                convertView = linearLayout;
                convertView.setTag(holder = new ViewHolder(textView, imageView));
            } else
                holder = (ViewHolder) convertView.getTag();

            final Object item = getItem(position);
            if (item instanceof Icons) {
                final Icons icons = (Icons) item;
                holder.title.setText(icons.name);
                holder.icon.setImageResource(icons.icon);
            }
            return convertView;
        }

        private final static class ViewHolder {
            private final TextView title;
            private final ImageView icon;

            private ViewHolder(final TextView title, final ImageView icon) {
                this.title = title;
                this.icon = icon;
            }
        }
    }
}