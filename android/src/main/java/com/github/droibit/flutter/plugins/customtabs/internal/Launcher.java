package com.github.droibit.flutter.plugins.customtabs.internal;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;

import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.browser.customtabs.CustomTabsIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static android.content.Intent.ACTION_VIEW;

public class Launcher {

    public static final String KEY_OPTIONS_TOOLBAR_COLOR = "toolbarColor";
    private static final String KEY_OPTIONS_ENABLE_URL_BAR_HIDING = "enableUrlBarHiding";
    private static final String KEY_OPTIONS_SHOW_PAGE_TITLE = "showPageTitle";
    private static final String KEY_OPTIONS_DEFAULT_SHARE_MENU_ITEM = "enableDefaultShare";
    private static final String KEY_OPTIONS_ENABLE_INSTANT_APPS = "enableInstantApps";
    private static final String KEY_OPTIONS_ANIMATIONS = "animations";
    public static final String KEY_HEADERS = "headers";
    private static final String KEY_ANIMATION_START_ENTER = "startEnter";
    private static final String KEY_ANIMATION_START_EXIT = "startExit";
    private static final String KEY_ANIMATION_END_ENTER = "endEnter";
    private static final String KEY_ANIMATION_END_EXIT = "endExit";
    private static final Uri URI_ = Uri.parse("https://www.google.com");

    public interface LauncherErrorHandle {
        void handle(Throwable throwable);
    }

    // Note: The full resource qualifier is "package:type/entry".
    // https://developer.android.com/reference/android/content/res/Resources.html#getIdentifier(java.lang.String, java.lang.String, java.lang.String)
    private static final Pattern animationIdentifierPattern = Pattern.compile("^.+:.+/");

    private final Context context;

    public Launcher(@NonNull Context context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public CustomTabsIntent buildIntent(@NonNull Map<String, Object> options) {
        final CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        if (options.containsKey(KEY_OPTIONS_TOOLBAR_COLOR)) {
            final String colorString = (String) options.get(KEY_OPTIONS_TOOLBAR_COLOR);
            builder.setToolbarColor(Color.parseColor(colorString));
        }

        if (options.containsKey(KEY_OPTIONS_ENABLE_URL_BAR_HIDING) && ((Boolean) options.get(
                KEY_OPTIONS_ENABLE_URL_BAR_HIDING))) {
            builder.enableUrlBarHiding();
        }

        if (options.containsKey(KEY_OPTIONS_DEFAULT_SHARE_MENU_ITEM) && ((Boolean) options.get(
                KEY_OPTIONS_DEFAULT_SHARE_MENU_ITEM))) {
            builder.addDefaultShareMenuItem();
        }

        if (options.containsKey(KEY_OPTIONS_SHOW_PAGE_TITLE)) {
            builder.setShowTitle(((Boolean) options.get(KEY_OPTIONS_SHOW_PAGE_TITLE)));
        }

        if (options.containsKey(KEY_OPTIONS_ENABLE_INSTANT_APPS)) {
            builder.setInstantAppsEnabled(((Boolean) options.get(KEY_OPTIONS_ENABLE_INSTANT_APPS)));
        }

        if (options.containsKey(KEY_OPTIONS_ANIMATIONS)) {
            applyAnimations(builder, ((Map<String, String>) options.get(KEY_OPTIONS_ANIMATIONS)));
        }

        final CustomTabsIntent customTabsIntent = builder.build();
        onPostBuild(customTabsIntent.intent, options);
        return customTabsIntent;
    }

    private void onPostBuild(@NonNull Intent intent, @NonNull Map<String, Object> options) {
        if (options.containsKey(KEY_HEADERS)) {
            Map<String, String> headers = (Map<String, String>) options.get(KEY_HEADERS);
            Bundle bundleHeaders = new Bundle();
            for (Map.Entry<String, String> header : headers.entrySet()) {
                bundleHeaders.putString(header.getKey(), header.getValue());
            }
            intent.putExtra(Browser.EXTRA_HEADERS, bundleHeaders);
        }
    }

    private void applyAnimations(@NonNull CustomTabsIntent.Builder builder,
                                 @NonNull Map<String, String> animations) {
        final int startEnterAnimationId =
                animations.containsKey(KEY_ANIMATION_START_ENTER) ? resolveAnimationIdentifierIfNeeded(
                        animations.get(KEY_ANIMATION_START_ENTER)) : -1;
        final int startExitAnimationId =
                animations.containsKey(KEY_ANIMATION_START_EXIT) ? resolveAnimationIdentifierIfNeeded(
                        animations.get(KEY_ANIMATION_START_EXIT)) : -1;
        final int endEnterAnimationId =
                animations.containsKey(KEY_ANIMATION_END_ENTER) ? resolveAnimationIdentifierIfNeeded(
                        animations.get(KEY_ANIMATION_END_ENTER)) : -1;
        final int endExitAnimationId =
                animations.containsKey(KEY_ANIMATION_END_EXIT) ? resolveAnimationIdentifierIfNeeded(
                        animations.get(KEY_ANIMATION_END_EXIT)) : -1;

        if (startEnterAnimationId != -1 && startExitAnimationId != -1) {
            builder.setStartAnimations(context, startEnterAnimationId, startExitAnimationId);
        }

        if (endEnterAnimationId != -1 && endExitAnimationId != -1) {
            builder.setExitAnimations(context, endEnterAnimationId, endExitAnimationId);
        }
    }

    @AnimRes
    private int resolveAnimationIdentifierIfNeeded(@NonNull String identifier) {
        if (animationIdentifierPattern.matcher(identifier).find()) {
            return context.getResources().getIdentifier(identifier, null, null);
        } else {
            return context.getResources().getIdentifier(identifier, "anim", context.getPackageName());
        }
    }

    public boolean launch(@NonNull Context context, @NonNull Uri uri,
                       @NonNull CustomTabsIntent customTabsIntent) {
        List<String> supportCustomTabsPackages = getSupportCustomTabsPackages(context);
        if (supportCustomTabsPackages.isEmpty()) {
            return false;
        }


        String defaultCustomTabs = getDefaultCustomTabsPackage(context);
        if (defaultCustomTabs == null)
            defaultCustomTabs = findFirstPackage(supportCustomTabsPackages, pkg -> pkg.contains("chrome"));
        if (defaultCustomTabs == null)
            defaultCustomTabs = findFirstPackage(supportCustomTabsPackages, pkg -> pkg.contains("firefox"));
        if (defaultCustomTabs == null)
            defaultCustomTabs = supportCustomTabsPackages.get(0);

        customTabsIntent.intent.setPackage(defaultCustomTabs);
        customTabsIntent.launchUrl(context, uri);
        return true;
    }

    interface Condition {
        boolean find(String pkg);
    }

    private String findFirstPackage(@NonNull List<String> packages, @NonNull Condition condition) {
        for (String pkg : packages) {
            if (condition.find(pkg)) return pkg;
        }
        return null;
    }

    public static String getDefaultActionView(@NonNull Context context) {
        final PackageManager pm = context.getPackageManager();
        final Intent activityIntent = new Intent(ACTION_VIEW, URI_);
        final ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
        if (defaultViewHandlerInfo != null) {
            return defaultViewHandlerInfo.activityInfo.packageName;
        }
        return null;
    }

    private String getDefaultCustomTabsPackage(@NonNull Context context) {
        final PackageManager pm = context.getPackageManager();
        final Intent activityIntent = new Intent(ACTION_VIEW, URI_);
        final ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
        if (defaultViewHandlerInfo != null && supportedCustomTabs(pm, defaultViewHandlerInfo.activityInfo.packageName)) {
            return defaultViewHandlerInfo.activityInfo.packageName;
        }
        return null;
    }

    public @NonNull
    List<String> getSupportCustomTabsPackages(@NonNull Context context) {
        final PackageManager pm = context.getPackageManager();
        final int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flag = PackageManager.MATCH_ALL;
        } else {
            flag = PackageManager.MATCH_DEFAULT_ONLY;
        }
        final Intent activityIntent = new Intent(ACTION_VIEW, URI_);
        final List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(activityIntent, flag);

        final List<String> installedCustomTabs = new ArrayList<>();
        for (ResolveInfo resolveInfo : resolveInfoList) {
            final String packageName = resolveInfo.activityInfo.packageName;
            if (supportedCustomTabs(pm, packageName)) {
                installedCustomTabs.add(packageName);
            }
        }
        return installedCustomTabs;
    }

    private boolean supportedCustomTabs(
            @NonNull PackageManager pm,
            @NonNull String packageName) {
        // Whether support Chrome Custom Tabs.
        final Intent serviceIntent =
                new Intent("android.support.customtabs.action.CustomTabsService").setPackage(packageName);
        return pm.resolveService(serviceIntent, 0) != null;
    }
}
