package awais.backworddictionary.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.R;
import awais.backworddictionary.helpers.ScrollingMovement;

public final class TTSItemsAdapter<T> extends RecyclerView.Adapter<TTSItemsAdapter.TTSViewHolder> {
    private final List<TTSItemHolder<T>> list;
    private final View.OnClickListener onClickListener;
    private Context context;
    private Resources resources;
    private LayoutInflater inflater;

    public TTSItemsAdapter(final List<TTSItemHolder<T>> list, final View.OnClickListener onClickListener) {
        this.list = list;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public TTSViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        if (context == null) context = parent.getContext();
        if (resources == null) resources = context.getResources();
        if (inflater == null) inflater = LayoutInflater.from(context);
        return new TTSViewHolder(inflater.inflate(R.layout.layout_tts_item, parent, false),
                onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final TTSViewHolder holder, final int position) {
        if (context == null) context = holder.itemView.getContext();
        if (resources == null) resources = context.getResources();

        final TTSItemHolder<T> obj = list.get(position);
        holder.itemView.setTag(obj);

        holder.ivIcon.setImageDrawable(null);
        holder.ivSelected.setImageResource(obj.selected ? R.drawable.ic_checked : 0);

        if (obj.object instanceof TextToSpeech.EngineInfo) {
            final TextToSpeech.EngineInfo engineInfo = (TextToSpeech.EngineInfo) obj.object;
            holder.textView1.setText(engineInfo.label);
            holder.textView2.setText(engineInfo.name);

            Drawable drawable = null;

            final Object tag = holder.ivIcon.getTag();
            if (tag instanceof Drawable) drawable = (Drawable) tag;
            else if (engineInfo.icon != 0) {
                final String resourceName = resources.getResourceName(engineInfo.icon);
                final String resourcePackageName = resources.getResourcePackageName(engineInfo.icon);
                if (!BuildConfig.APPLICATION_ID.equalsIgnoreCase(resourcePackageName)
                        && (resourceName == null || !resourceName.contains("ic_launcher"))) {
                    try {
                        drawable = ContextCompat.getDrawable(context, engineInfo.icon);
                    } catch (final Exception e) {
                        // ignore
                    }

                    if (drawable == null) {
                        try {
                            drawable = ResourcesCompat.getDrawable(resources, engineInfo.icon, context.getTheme());
                        } catch (final Exception e) {
                            // ignore
                        }
                    }
                }
                holder.ivIcon.setTag(drawable);
            }

            holder.ivIcon.setImageDrawable(drawable);
            holder.ivIcon.setVisibility(drawable != null ? View.VISIBLE : View.GONE);


        } else if (obj.object instanceof Locale) {
            final Locale locale = (Locale) obj.object;
            holder.textView1.setText(locale.getDisplayName());
            holder.ivIcon.setVisibility(View.GONE);


        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && obj.object instanceof Voice) {
            final Voice voice = (Voice) obj.object;

            final String name = voice.getName();
            final boolean isNetworkLang = (name.endsWith("-internet") || name.endsWith("-network"))
                    || (name.contains("-internet") || name.contains("-network"));

            holder.ivIcon.setImageResource(isNetworkLang ? R.drawable.ic_wifi : 0);
            holder.ivIcon.setVisibility(isNetworkLang ? View.VISIBLE : View.INVISIBLE);

            final String info = "[n:" + name + ',' +
                    " q:" + voice.getQuality() + ',' +
                    " l:" + voice.getLatency() + ',' +
                    " f:" + voice.getFeatures() + ']';
            holder.textView2.setText(info);
            holder.textView2.setVisibility(View.VISIBLE);
            holder.textView1.setText(voice.getLocale().getDisplayName());
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class TTSViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView1, textView2;
        private final ImageView ivIcon, ivSelected;

        public TTSViewHolder(@NonNull final View itemView, final View.OnClickListener onClickListener) {
            super(itemView);
            itemView.setOnClickListener(onClickListener);
            this.ivIcon = itemView.findViewById(android.R.id.icon);
            this.textView1 = itemView.findViewById(android.R.id.text1);
            this.textView2 = itemView.findViewById(android.R.id.text2);
            this.ivSelected = itemView.findViewById(android.R.id.button1);
            this.textView2.setMovementMethod(ScrollingMovement.getInstance());
            this.textView2.setSelected(true);
        }
    }

    public static class TTSItemHolder<T> {
        public final T object;
        public boolean selected;

        public TTSItemHolder(final T object, final boolean selected) {
            this.object = object;
            this.selected = selected;
        }
    }
}
