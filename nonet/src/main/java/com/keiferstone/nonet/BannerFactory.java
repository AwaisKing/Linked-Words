package com.keiferstone.nonet;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class BannerFactory {

    static BannerView getBanner(Context context) {
        return getBanner(context, R.string.no_server_connection_message);
    }

    static BannerView getBanner(Context context, String message) {
        ViewGroup parent = (ViewGroup) extractRootView(context);
        BannerView banner = inflateBanner(context, R.layout.default_banner, parent);
        if (banner != null) banner.setText(message);
        attachToParent(banner, parent);
        return banner;
    }

    static BannerView getBanner(Context context, @StringRes int messageRes) {
        ViewGroup parent = (ViewGroup) extractRootView(context);
        BannerView banner = inflateBanner(context, R.layout.default_banner, parent);
        if (banner != null) banner.setText(messageRes);
        attachToParent(banner, parent);
        return banner;
    }

    static BannerView getBanner(Context context, @LayoutRes int bannerRes, @Nullable ViewGroup parent) {
        BannerView banner = inflateBanner(context, bannerRes, parent);
        attachToParent(banner, parent);
        return banner;
    }

    private static BannerView inflateBanner(Context context, @LayoutRes int layoutRes, @Nullable ViewGroup parent) {
        if (context == null) return null;
        if (parent == null) parent = (ViewGroup) extractRootView(context);
        return (BannerView) LayoutInflater.from(context).inflate(layoutRes, parent, false);
    }

    private static void attachToParent(BannerView banner, ViewGroup parent) {
        if (banner != null && parent != null) parent.addView(banner);
    }

    @Nullable
    private static View extractRootView(Context context) {
        if (context instanceof Activity)
            return ((Activity) context).findViewById(android.R.id.content);
        return null;
    }
}
