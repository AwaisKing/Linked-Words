package awais.backworddictionary.custom;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;

import java.lang.reflect.Method;
import java.util.List;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;

class HuaweiFix {
    private final Context context;

    public HuaweiFix(@NonNull Context context) {
        this.context = context;
    }

    public void check() {
        if("huawei".equalsIgnoreCase(Build.MANUFACTURER) &&
                !Main.sharedPreferences.getBoolean("skipProtectedAppsMessage", false))
            ifHuaweiAlert();
    }

    private void ifHuaweiAlert() {
        final boolean skipMessage = Main.sharedPreferences.getBoolean("skipProtectedAppsMessage", false);

        if (skipMessage) return;

        final Intent intent = new Intent();
        intent.setClassName("com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity");
        if (isCallable(intent)) {
            final AppCompatCheckBox dontShowAgain = new AppCompatCheckBox(context);
            final String text = "Do not show again";
            dontShowAgain.setText(text);
            dontShowAgain.setOnCheckedChangeListener((buttonView, isChecked) ->
                    Main.sharedPreferences.edit().putBoolean("skipProtectedAppsMessage", isChecked).apply());

            new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Huawei Protected Apps").setView(dontShowAgain)
                    .setMessage(context.getResources().getString(R.string.app_name) + " requires to be enabled in 'Protected Apps' to function properly.")
                    .setPositiveButton("Protected Apps", (dialog, which) -> huaweiProtectedApps())
                    .setNegativeButton(android.R.string.cancel, null).show();
        } else Main.sharedPreferences.edit().putBoolean("skipProtectedAppsMessage", true).apply();
    }

    private boolean isCallable(Intent intent) {
        final List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void huaweiProtectedApps() {
        try {
            String cmd = "am start -n com.huawei.systemmanager/.optimize.process.ProtectActivity";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                cmd = cmd.concat(" --user ".concat(getUserSerial()));
            Runtime.getRuntime().exec(cmd);
        } catch (Exception ignored) { }
    }

    @NonNull
    private String getUserSerial() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) return "0";

        try {
            final Object userManager = context.getSystemService(Context.USER_SERVICE);
            if (userManager == null) return "0";

            final Method myUserHandleMethod = android.os.Process.class.getMethod("myUserHandle");
            final Object myUserHandle = myUserHandleMethod.invoke(android.os.Process.class, (Object[]) null);
            if (myUserHandle != null) {
                final Class<?> userHandleClass = myUserHandle.getClass();
                final Method getSerialNumberForUser = userManager.getClass().getMethod("getSerialNumberForUser", userHandleClass);
                final Long userSerial = (Long) getSerialNumberForUser.invoke(userManager, myUserHandle);
                return userSerial == null ? "0" : String.valueOf(userSerial);
            }
        } catch (Exception ignored) {}
        return "0";
    }
}
