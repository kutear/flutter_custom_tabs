import 'package:flutter/material.dart';
import 'package:flutter_custom_tabs/flutter_custom_tabs.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return MaterialApp(
      title: 'Flutter Custom Tabs Example',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Flutter Custom Tabs Example'),
        ),
        body: Center(
          child: Column(
            children: [
              TextButton(
                child: Text(
                  'CustomTab',
                  style: TextStyle(
                    fontSize: 17,
                    color: theme.primaryColor,
                  ),
                ),
                onPressed: () => _launchURL(context, true),
              ),
              TextButton(
                child: Text(
                  'Native',
                  style: TextStyle(
                    fontSize: 17,
                    color: theme.primaryColor,
                  ),
                ),
                onPressed: () async => _launchURL(context, false),
              ),
              TextButton(
                child: Text(
                  'Force System',
                  style: TextStyle(
                    fontSize: 17,
                    color: theme.primaryColor,
                  ),
                ),
                onPressed: () async => launchUrl('https://www.zhihu.com/question/40492181/answer/1910855638?content_id=1381679307534778368&type=zvideo'),
              )
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _launchURL(BuildContext context, bool customTabs) async {
    try {
      final isSupport = await isSupportCustomTabs;
      print(isSupport);
      final option = CustomTabsOption(
        toolbarColor: Colors.white,
        enableDefaultShare: true,
        enableUrlBarHiding: true,
        showPageTitle: true,
        animation: CustomTabsAnimation.slideIn(),
      );
      const url =
          'https://www.zhihu.com/question/40492181/answer/1910855638?content_id=1381679307534778368&type=zvideo';
      if (customTabs) {
        await launch(
          url,
          option: option,
        );
      } else {
        await launchNative(url, title: 'Just Title', option: option);
      }
    } catch (e) {
      // An exception is thrown if browser app is not installed on Android device.
      debugPrint(e.toString());
    }
  }
}
