import 'dart:async';

import 'package:flutter/services.dart';

import '../flutter_custom_tabs.dart';
import 'custom_tabs_option.dart';

const MethodChannel _channel =
    MethodChannel('com.github.droibit.flutter.plugins.custom_tabs');

final handles = <int, FallbackHandler>{};
var _isInit = false;
var _index = 0;

void _init() {
  _channel.setMethodCallHandler((methodCall) async {
    switch (methodCall.method) {
      case 'handle':
        final args = Map<String, dynamic>.from(methodCall.arguments);
        final id = args["id"] as int;
        final url = args["url"] as String;
        final handler = handles[id];
        if (handler != null) {
          handler(url);
          handles.remove(id);
        }
    }
  });
}

Future<bool> customTabsLauncher(
    String urlString, CustomTabsOption option, FallbackHandler? handler) {
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

  final id = _index++;
  if (handler != null) {
    handles[id] = handler;
  }

  final args = <String, dynamic>{
    'url': urlString,
    'option': option.toMap(),
    'id': handler == null ? -1 : id
  };
  return _channel
      .invokeMethod<bool>('launch', args)
      .then((value) => value == true ? true : false);
}

Future<bool> get isSupportCustomTabsImpl {
  return _channel
      .invokeMethod<bool>('isSupportCustomTabs')
      .then((value) => value == true);
}
