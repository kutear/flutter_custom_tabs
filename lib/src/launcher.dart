import 'dart:async';
import 'dart:io' show Platform;

import './custom_tabs_launcher.dart';
import './custom_tabs_option.dart';

/// Open the specified Web URL with Custom Tabs.
///
/// Custom Tab is only supported on the Android platform.
/// Therefore, this plugin uses [url_launcher](https://pub.dartlang.org/packages/url_launcher) on iOS to launch `SFSafariViewController`.
/// (The specified [option] is ignored on iOS.)
///
/// When Chrome is not installed on Android device, try to start other browsers.
/// If you want to launch a CustomTabs compatible browser on a device without Chrome, you can set its package name with `option.extraCustomTabs`.
/// e.g. Firefox(`org.mozilla.firefox`), Microsoft Edge(`com.microsoft.emmx`).
///
/// Example:
///
/// ```dart
/// await launch(
///   'https://flutter.io',
///   option: new CustomTabsOption(
///     toolbarColor: Theme.of(context).primaryColor,
///     enableUrlBarHiding: true,
///     showPageTitle: true,
///     animation: new CustomTabsAnimation.slideIn(),
///     extraCustomTabs: <String>[
///       'org.mozilla.firefox',
///       'com.microsoft.emmx'
///     ],
///   ),
/// );
/// ```
Future<bool> launch(
  String urlString, {
  required CustomTabsOption option,
}) {
  return _launcher(urlString, option);
}

Future<bool> launchNative(
  String urlString, {
  String? title,
  required CustomTabsOption option,
}) {
  return customLaunchNative(urlString, title, option);
}

Future<bool> launchUrl(String urlString) {
  return customLaunchUrl(urlString);
}

Future<bool> get isSupportCustomTabs => isSupportCustomTabsImpl;

typedef _PlatformLauncher = Future<bool> Function(
    String urlString, CustomTabsOption option);

typedef FallbackHandler = Future<void> Function(String urlString);

_PlatformLauncher get _launcher {
  if (!Platform.isAndroid) {
    throw Exception('not support this platform');
  }
  _platformLauncher = customTabsLauncher;
  return _platformLauncher;
}

late _PlatformLauncher _platformLauncher;
