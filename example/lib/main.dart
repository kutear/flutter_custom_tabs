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
          child: TextButton(
            child: Text(
              'Show Flutter homepage',
              style: TextStyle(
                fontSize: 17,
                color: theme.primaryColor,
              ),
            ),
            onPressed: () => _launchURL(context),
          ),
        ),
      ),
    );
  }

  Future<void> _launchURL(BuildContext context) async {
    try {
      final isSupport = await isSupportCustomTabs;
      print(isSupport);
      await launch(
        'https://www.zhihu.com/question/40492181/answer/1910855638?content_id=1381679307534778368&type=zvideo',
        // handler: (url) async {
        //   print(url);
        // },
        option: CustomTabsOption(
          toolbarColor: Colors.white,
          enableDefaultShare: true,
          enableUrlBarHiding: true,
          showPageTitle: true,
          animation: CustomTabsAnimation.slideIn(),
          extraCustomTabs: const <String>[
            // ref. https://play.google.com/store/apps/details?id=org.mozilla.firefox
            'org.mozilla.firefox',
            // ref. https://play.google.com/store/apps/details?id=com.microsoft.emmx
            'com.microsoft.emmx',
          ],
        ),
      );
    } catch (e) {
      // An exception is thrown if browser app is not installed on Android device.
      debugPrint(e.toString());
    }
  }
}
