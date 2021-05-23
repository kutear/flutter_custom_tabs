import 'dart:async';
import 'package:url_launcher/url_launcher.dart' as pkg;
import '../flutter_custom_tabs.dart';
import 'custom_tabs_option.dart';

Future<void> urlLauncher(
    String urlString, CustomTabsOption option, FallbackHandler? handler) {
  return pkg.launch(urlString, forceSafariVC: true, forceWebView: false);
}
