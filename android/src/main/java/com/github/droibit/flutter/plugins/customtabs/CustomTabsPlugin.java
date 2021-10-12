package com.github.droibit.flutter.plugins.customtabs;

import android.app.Activity;
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
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

import static android.content.Intent.ACTION_VIEW;

public class CustomTabsPlugin implements FlutterPlugin, ActivityAware, MethodChannel.MethodCallHandler {

    private void register(BinaryMessenger messenger, Context context, Activity activity) {
        final MethodChannel channel =
                new MethodChannel(messenger, "com.github.droibit.flutter.plugins.custom_tabs");
        channel.setMethodCallHandler(this);
        this.context = context;
        if (activity != null)
            this.activity = activity;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(PluginRegistry.Registrar registrar) {
        final CustomTabsPlugin plugin = new CustomTabsPlugin();
        plugin.register(registrar.messenger(), registrar.context(), registrar.activity());
    }

    private static final String KEY_OPTION = "option";

    private static final String KEY_URL = "url";

    private static final String KEY_TITLE = "title";

    private static final String KEY_ID = "id";

    private static final String KEY_EXTRA_CUSTOM_TABS = "extraCustomTabs";

    private static final String CODE_LAUNCH_ERROR = "LAUNCH_ERROR";

    private Launcher launcher;
    private Activity activity;
    private Context context;


    private Launcher getLauncher() {
        if (launcher == null) {
            launcher = new Launcher(activity);
        }
        return launcher;
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
            case "launchUrl":
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
        result.success(!getLauncher().getSupportCustomTabsPackages(context).isEmpty());
    }

    private void launchUrl(@NonNull final Map<String, Object> args, @NonNull MethodChannel.Result result) {
        final Context context;
        if (activity != null) {
            context = activity;
        } else {
            context = this.context;
        }
        final Uri uri = Uri.parse(args.get(KEY_URL).toString());
        Intent intent = new Intent(ACTION_VIEW, uri);
        String pkg = Launcher.getDefaultActionView(context);
        if (pkg != null) {
            intent.setPackage(pkg);
        }
        context.startActivity(intent);
        result.success(true);
    }

    private void launchNative(@NonNull final Map<String, Object> args, @NonNull MethodChannel.Result result) {
        final Context context;
        if (activity != null) {
            context = activity;
        } else {
            context = this.context;
        }
        final Uri uri = Uri.parse(args.get(KEY_URL).toString());
        final String title = String.valueOf(args.get(KEY_TITLE));
        final Map<String, Object> options = (Map<String, Object>) args.get(KEY_OPTION);
        final CustomTabsIntent customTabsIntent = getLauncher().buildIntent(options);

        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, uri.toString());
        intent.putExtra(WebViewActivity.EXTRA_TITLE, title);
        intent.putExtra(WebViewActivity.EXTRA_OPTION, new HashMap<>(options));
        context.startActivity(intent, customTabsIntent.startAnimationBundle);
        result.success(true);
    }

    @SuppressWarnings("unchecked")
    private void launch(@NonNull final Map<String, Object> args, @NonNull MethodChannel.Result result) {
        final Uri uri = Uri.parse(args.get(KEY_URL).toString());
        final Map<String, Object> options = (Map<String, Object>) args.get(KEY_OPTION);
        final CustomTabsIntent customTabsIntent = getLauncher().buildIntent(options);

        final Context context;
        if (activity != null) {
            context = activity;
        } else {
            context = this.context;
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            boolean launchResult = getLauncher().launch(context, uri, customTabsIntent);
            result.success(launchResult);
        } catch (ActivityNotFoundException e) {
            result.error(CODE_LAUNCH_ERROR, e.getMessage(), null);
        }
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        register(binding.getBinaryMessenger(), binding.getApplicationContext(), null);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }
}
