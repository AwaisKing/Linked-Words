package awais.backworddictionary.helpers.other;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.Random;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.R;

public final class MyApps {
    /**
     * copy paste this shit
     * <p>
     * MyApps.showAlertDialog(this, (parent, view, position, id) -> {
     * if (id == -1 && position == -1 && parent == null) super.onBackPressed();
     * else MyApps.openAppStore(this, position);
     * });
     */
    private final static Icons[] iconsList;

    static {
        final Icons[] currList = new Icons[]{
                new Icons("awais.addme", "AddMe", R.drawable.adm),
                new Icons("awais.media.scanner", "mediaScanner", R.drawable.ms),
                new Icons("awais.skyrimconsole", "Skyrim Cheats", R.drawable.tesv),
                new Icons("awais.reversify", "Reversify", R.drawable.rev),
                new Icons("awais.reversify.lite", "Reversify Lite", R.drawable.revl),
                new Icons("awais.backworddictionary", "Linked Words", R.drawable.lw),
                new Icons("awais.hostsmanager", "Hosts Manager Pro", R.drawable.hmp),
                new Icons("awais.hostsmanager.lite", "Hosts Manager Lite", R.drawable.hml),
                new Icons("awais.game.tictactoe", "Tic Tac Toe", R.drawable.ttt),
                new Icons("awais.game.jigsaw", "JigSaw", R.drawable.jsw),
                new Icons("awais.quodb", "QuoDB", R.drawable.qdb),
        };
        iconsList = new Icons[currList.length - 1];
        int i = 0;
        for (final Icons value : currList) if (!value.pkg.equals(BuildConfig.APPLICATION_ID)) iconsList[i++] = value;
    }

    public static void openAppStore(@NonNull final Context context, final int position) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + MyApps.iconsList[position].pkg)));
        } catch (final Exception e) {
            // activity not found? bruh
        }
    }

    public static void showAlertDialog(final Context context, final AdapterView.OnItemClickListener clickListener) {
        final DialogInterface.OnCancelListener cancelListener = d -> {
            if (clickListener != null) clickListener.onItemClick(null, null, -1, -1);
            else {
                if (context instanceof Activity) ((Activity) context).finish();
                else Process.killProcess(Process.myPid());
            }
        };
        if (new Random().nextDouble() < 0.6D) {
            cancelListener.onCancel(null);
            return;
        }
        final GridView gridView = new GridView(context);
        gridView.setAdapter(new ImageAdapter(context));
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(clickListener);
        final AlertDialog dialog = new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialogTheme)
                .setView(gridView).setTitle("Support my apps").create();
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
    }

    private static class Icons {
        private final int icon;
        private final String title, pkg;

        private Icons(final String pkg, final String title, final int icon) {
            this.title = title;
            this.pkg = pkg;
            this.icon = icon;
        }
    }

    private static class ImageAdapter extends BaseAdapter {
        private final Context context;
        private final int size;

        public ImageAdapter(@NonNull final Context context) {
            this.context = context;
            this.size = (int) (80 * context.getResources().getDisplayMetrics().density);
        }

        @Override
        public int getCount() {
            return iconsList.length;
        }

        @Override
        public Object getItem(final int position) {
            return iconsList[position];
        }

        @Override
        public long getItemId(final int position) {
            return 0;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                final LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                final AppCompatImageView imageView = new AppCompatImageView(context);
                final MaterialTextView textView = new MaterialTextView(context);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    textView.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                imageView.setAdjustViewBounds(true);
                linearLayout.addView(imageView, LinearLayout.LayoutParams.MATCH_PARENT, size);
                linearLayout.addView(textView);
                final int padding = size / 4;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    linearLayout.setPaddingRelative(padding, padding, padding, padding);
                else linearLayout.setPadding(padding, padding, padding, padding);
                convertView = linearLayout;
                convertView.setTag(holder = new ViewHolder(textView, imageView));
            } else
                holder = (ViewHolder) convertView.getTag();

            final Object item = getItem(position);
            if (item instanceof Icons) {
                final Icons icons = (Icons) item;
                holder.title.setText(icons.title);
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