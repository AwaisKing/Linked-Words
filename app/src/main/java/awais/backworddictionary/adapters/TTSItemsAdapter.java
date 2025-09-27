package awais.backworddictionary.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.speech.tts.Voice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.R;
import awais.backworddictionary.adapters.holders.TTSItemHolder;
import awais.backworddictionary.adapters.holders.TTSViewHolder;

public final class TTSItemsAdapter<T> extends RecyclerView.Adapter<TTSViewHolder> {
    private final List<TTSItemHolder<T>> list;
    private final View.OnClickListener onClickListener;
    private final Context context;
    private final Resources resources;
    private final LayoutInflater inflater;

    public TTSItemsAdapter(@NonNull final Context context, final List<TTSItemHolder<T>> list, final View.OnClickListener onClickListener) {
        this.resources = context.getResources();
        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public TTSViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new TTSViewHolder(inflater.inflate(R.layout.layout_tts_item, parent, false), onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final TTSViewHolder holder, final int position) {
        final TTSItemHolder<T> obj = list.get(position);
        holder.itemView.setTag(obj);

        holder.ivIcon.setImageDrawable(null);
        holder.ivSelected.setImageResource(obj.selected ? R.drawable.ic_checked : 0);

        if (obj.object instanceof final EngineInfo engineInfo) {
            holder.textView1.setText(engineInfo.label);
            holder.textView2.setText(engineInfo.name);

            Drawable drawable = null;

            final Object tag = holder.ivIcon.getTag();
            if (tag instanceof Drawable) drawable = (Drawable) tag;
            else if (engineInfo.icon != 0) {
                String resourceName, resourcePackageName;

                // getResourceName crash fix
                try {
                    resourceName = resources.getResourceName(engineInfo.icon);
                } catch (final Exception e) {
                    resourceName = null;
                }
                try {
                    resourcePackageName = resources.getResourcePackageName(engineInfo.icon);
                } catch (final Exception e) {
                    resourcePackageName = null;
                }

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

            return;
        }

        if (obj.object instanceof final Locale locale) {
            holder.textView1.setText(locale.getDisplayName());
            holder.ivIcon.setVisibility(View.GONE);

            return;
        }

        if (obj.object instanceof final Voice voice) {
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
}