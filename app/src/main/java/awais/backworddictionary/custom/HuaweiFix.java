package awais.backworddictionary.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;

import java.lang.reflect.Method;
import java.util.List;

import awais.backworddictionary.Main;
import awais.backworddictionary.R;

@SuppressWarnings( {"unused", "WeakerAccess"} )
public class HuaweiFix {
    private final Context context;

    public HuaweiFix(Context context) {
        this.context = context;
    }

    public void check() {
        if("huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER) &&
                !Main.sharedPreferences.getBoolean("skipProtectedAppsMessage", false)) {
            ifHuaweiAlert();
        }
    }

    private void ifHuaweiAlert() {
        final String saveIfSkip = "skipProtectedAppsMessage";
        boolean skipMessage = Main.sharedPreferences.getBoolean(saveIfSkip, false);
        if (!skipMessage) {
            Intent intent = new Intent();
            intent.setClassName("com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity");
            if (isCallable(intent)) {
                final AppCompatCheckBox dontShowAgain = new AppCompatCheckBox(context);
                String text = "Do not show again";
                dontShowAgain.setText(text);
                dontShowAgain.setOnCheckedChangeListener((buttonView, isChecked) ->
                        Main.sharedPreferences.edit().putBoolean(saveIfSkip, isChecked).apply());

                new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Huawei Protected Apps").setView(dontShowAgain)
                        .setMessage(String.format("%s requires to be enabled in 'Protected Apps' to function properly.%n",
                                context.getResources().getString(R.string.app_name)))
                        .setPositiveButton("Protected Apps", (dialog, which) -> huaweiProtectedApps())
                        .setNegativeButton(android.R.string.cancel, null).show();
            } else Main.sharedPreferences.edit().putBoolean(saveIfSkip, true).apply();
        }
    }

    private boolean isCallable(Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
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

    private String getUserSerial() {
        try {
            @SuppressLint("WrongConstant") Object userManager = context.getSystemService("user");
            if (userManager == null) return "";
            //noinspection JavaReflectionMemberAccess
            Method myUserHandleMethod = android.os.Process.class.getMethod("myUserHandle", (Class<?>[]) null);
            Object myUserHandle = myUserHandleMethod.invoke(android.os.Process.class, (Object[]) null);
            Method getSerialNumberForUser = userManager.getClass().getMethod("getSerialNumberForUser", myUserHandle.getClass());
            Long userSerial = (Long) getSerialNumberForUser.invoke(userManager, myUserHandle);
            if (userSerial != null) return String.valueOf(userSerial);
            else return "";
        } catch (Exception ignored) {}
        return "";
    }
}
