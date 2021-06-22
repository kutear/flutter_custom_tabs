package com.github.droibit.flutter.plugins.customtabs;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;

import com.github.droibit.flutter.plugins.customtabs.internal.Launcher;

import java.util.HashMap;
import java.util.Map;

import io.flutter.Log;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public class CustomTabsPlugin implements MethodChannel.MethodCallHandler {
    private MethodChannel globalChannel;

    /**
     * Plugin registration.
     */
    public static void registerWith(PluginRegistry.Registrar registrar) {
        final MethodChannel channel =
                new MethodChannel(registrar.messenger(), "com.github.droibit.flutter.plugins.custom_tabs");
        channel.setMethodCallHandler(new CustomTabsPlugin(channel, registrar));

    }

    private static final String KEY_OPTION = "option";

    private static final String KEY_URL = "url";

    private static final String KEY_ID = "id";

    private static final String KEY_EXTRA_CUSTOM_TABS = "extraCustomTabs";

    private static final String CODE_LAUNCH_ERROR = "LAUNCH_ERROR";

    private final PluginRegistry.Registrar registrar;

    private final Launcher launcher;

    private CustomTabsPlugin(@NonNull MethodChannel channel, @NonNull PluginRegistry.Registrar registrar) {
        this.registrar = registrar;
        this.globalChannel = channel;
        this.launcher = new Launcher(registrar.activeContext());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMethodCall(MethodCall call, final MethodChannel.Result result) {
        switch (call.method) {
            case "launchCustomTabs":
                launch(((Map<String, Object>) call.arguments), result);
                break;
            case "launchNative":
                launchNative(((Map<String, Object>) call.arguments), result);
                break;
            case "launchUrl" :
                launchUrl(((Map<String, Object>) call.arguments), result);
                break;
            case "isSupportCustomTabs":
                isSupportCustomTabs(result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void isSupportCustomTabs(@NonNull MethodChannel.Result result) {
        result.success(!launcher.getSupportCustomTabsPackages(registrar.context()).isEmpty());
    }

    private void launchUrl(@NonNull final Map<String, Object> args, @NonNull MethodChannel.Result result) {
        final Context context;
        if (registrar.activity() != null) {
            context = registrar.activity();
        } else {
            context = registrar.context();
        }
        Intent intent = new Intent();
        String pkg = Launcher.getDefaultActionView(context);
        if (pkg == null) {
            result.error("-1","can't find view", null);
            return;
        }
        final Uri uri = Uri.parse(args.get(KEY_URL).toString());
        intent.setPackage(pkg);
        intent.setData(uri);
        context.startActivity(intent);
        result.success(true);
    }

    private void launchNative(@NonNull final Map<String, Object> args, @NonNull MethodChannel.Result result) {
        final Context context;
        if (registrar.activity() != null) {
            context = registrar.activity();
        } else {
            context = registrar.context();
        }
        final Uri uri = Uri.parse(args.get(KEY_URL).toString());
        final Map<String, Object> options = (Map<String, Object>) args.get(KEY_OPTION);
        final CustomTabsIntent customTabsIntent = launcher.buildIntent(options);

        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, uri.toString());
        intent.putExtra(WebViewActivity.EXTRA_OPTION, new HashMap<>(options));
        context.startActivity(intent, customTabsIntent.startAnimationBundle);
        result.success(true);
    }

    @SuppressWarnings("unchecked")
    private void launch(@NonNull final Map<String, Object> args, @NonNull MethodChannel.Result result) {
        final Uri uri = Uri.parse(args.get(KEY_URL).toString());
        final Map<String, Object> options = (Map<String, Object>) args.get(KEY_OPTION);
        final CustomTabsIntent customTabsIntent = launcher.buildIntent(options);

        final Context context;
        if (registrar.activity() != null) {
            context = registrar.activity();
        } else {
            context = registrar.context();
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            boolean launchResult = launcher.launch(context, uri, customTabsIntent);
            result.success(launchResult);
        } catch (ActivityNotFoundException e) {
            result.error(CODE_LAUNCH_ERROR, e.getMessage(), null);
        }
    }
}
