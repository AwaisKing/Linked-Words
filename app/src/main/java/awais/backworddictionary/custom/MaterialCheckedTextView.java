package awais.backworddictionary.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CheckedTextViewCompat;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

import awais.backworddictionary.R;

@SuppressLint({"PrivateResource", "RestrictedApi"})
public final class MaterialCheckedTextView extends AppCompatCheckedTextView {
    private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_CheckedTextView;
    private static final int[][] ENABLED_CHECKED_STATES = new int[][]{
            new int[]{android.R.attr.state_enabled, android.R.attr.state_checked},   // [0]
            new int[]{android.R.attr.state_enabled, -android.R.attr.state_checked},  // [1]
            new int[]{-android.R.attr.state_enabled, android.R.attr.state_checked},  // [2]
            new int[]{-android.R.attr.state_enabled, -android.R.attr.state_checked}, // [3]
    };
    @Nullable private ColorStateList materialThemeColorsTintList;
    private boolean useMaterialThemeColors;

    public MaterialCheckedTextView(@NonNull final Context context) {
        this(context, null);
    }

    public MaterialCheckedTextView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, R.attr.checkedTextViewStyle);
    }

    public MaterialCheckedTextView(@NonNull Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
        // Ensure we are using the correctly themed context rather than the context that was passed in.
        context = getContext();

        final TypedArray attributes = ThemeEnforcement.obtainStyledAttributes(context, attrs, R.styleable.MaterialCheckBox, defStyleAttr, DEF_STYLE_RES);

        final Resources.Theme theme = context.getTheme();
        final TypedValue outValue = new TypedValue();
        theme.resolveAttribute(R.attr.selectableItemBackground, outValue, true);
        final int resourceId = outValue.resourceId != 0 ? outValue.resourceId : outValue.data;


        try {
            final Drawable drawable = ContextCompat.getDrawable(context, resourceId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) setForeground(drawable);
            else setBackground(drawable);
        } catch (final Exception e) {
            setBackgroundResource(resourceId);
        }

        /// If buttonTint is specified, read it using MaterialResources to allow themeable attributes in all API levels.
        if (attributes.hasValue(R.styleable.MaterialCheckBox_buttonTint)) {
            final ColorStateList cslBtnTint = MaterialResources.getColorStateList(context, attributes, R.styleable.MaterialCheckBox_buttonTint);
            CheckedTextViewCompat.setCheckMarkTintList(this, cslBtnTint);
        }

        useMaterialThemeColors = attributes.getBoolean(R.styleable.MaterialCheckBox_useMaterialThemeColors, true);

        attributes.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (useMaterialThemeColors && CheckedTextViewCompat.getCheckMarkTintList(this) == null)
            setUseMaterialThemeColors(true);
    }

    /**
     * Forces the {@link MaterialCheckedTextView} to use colors from a Material Theme. Overrides any
     * specified ButtonTintList. If set to false, sets the tints to null.
     * Use {@link MaterialCheckedTextView#setSupportCheckMarkTintList(ColorStateList)} to change button tints.
     */
    public void setUseMaterialThemeColors(final boolean useMaterialThemeColors) {
        this.useMaterialThemeColors = useMaterialThemeColors;
        CheckedTextViewCompat.setCheckMarkTintList(this, useMaterialThemeColors ? getMaterialThemeColorsTintList() : null);
    }

    private ColorStateList getMaterialThemeColorsTintList() {
        if (materialThemeColorsTintList == null) {
            final int[] checkBoxColorsList = new int[ENABLED_CHECKED_STATES.length];
            final int colorControlActivated = MaterialColors.getColor(this, R.attr.colorControlActivated);
            final int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);
            final int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);

            checkBoxColorsList[0] = MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_FULL);
            checkBoxColorsList[1] = MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_MEDIUM);
            checkBoxColorsList[2] = MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED);
            checkBoxColorsList[3] = MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED);

            materialThemeColorsTintList = new ColorStateList(ENABLED_CHECKED_STATES, checkBoxColorsList);
        }
        return materialThemeColorsTintList;
    }
}