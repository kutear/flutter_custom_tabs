import 'dart:async';

import 'package:flutter/services.dart';

import '../flutter_custom_tabs.dart';
import 'custom_tabs_option.dart';

const MethodChannel _channel =
    MethodChannel('com.github.droibit.flutter.plugins.custom_tabs');

var _isInit = false;

void _init() {
}

Future<bool> customTabsLauncher(String urlString, CustomTabsOption option) {
  if (!_isInit) {
    _init();
    _isInit = true;
  }

  final Uri url = Uri.parse(urlString.trimLeft());
  if (url.scheme != 'http' && url.scheme != 'https') {
    throw PlatformException(
      code: 'NOT_A_WEB_SCHEME',
      message: 'Flutter Custom Tabs only supports URL of http or https scheme.',
    );
  }

  final args = <String, dynamic>{
    'url': urlString,
    'option': option.toMap(),
  };
  return _channel
      .invokeMethod<bool>('launchCustomTabs', args)
      .then((value) => value == true ? true : false);
}

Future<bool> customLaunchUrl(String url) {
  final args = <String, dynamic>{
    'url': url,
  };
  return _channel
      .invokeMethod<bool>('launchUrl', args)
      .then((value) => value == true ? true : false);
}

Future<bool> customLaunchNative(String urlString,String? title, CustomTabsOption option) {
  final args = <String, dynamic>{
    'url': urlString,
    'option': option.toMap()
  };
  if (title != null) {
    args['title'] = title;
  }
  return _channel
      .invokeMethod<bool>('launchNative', args)
      .then((value) => value == true ? true : false);
}

Future<bool> get isSupportCustomTabsImpl {
  return _channel
      .invokeMethod<bool>('isSupportCustomTabs')
      .then((value) => value == true);
}
