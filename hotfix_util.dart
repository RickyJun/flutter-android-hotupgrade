import 'dart:async';
import 'dart:io';

import 'package:app/net/http_mannage.dart';
import 'package:app/net/repository/app_repository.dart';
import 'package:app/utils/ToastUtils.dart';
import 'package:app/utils/sputils.dart';
import 'package:path_provider/path_provider.dart';
import 'package:youxuan_im/util/dio_util.dart';

class HotfixUtil {
  //上一次更新的fix.so文件编号
  static bool _ifNeedReStart = false;
  static final String LAST_FIX_FILE_NAME = "LAST_FIX_FILE";
  static void hotUpgrade() async {
    await hotUpgradeSoFile();
    //await hotUpgradeAssetsFile();
  }

  static Future hotUpgradeSoFile() async {
    // bool needHotfix = await _needHotfix(null);
    // if (!needHotfix) {
    //   return;
    // }
    ToastUtils.show("开始热更新");
    String file =
        (await getExternalStorageDirectory()).path + "/" + "libapp_fix.so";
    Completer downFinish = Completer();
    DioUtil.getInstance().download("https:xxxxx/libapp_fix.so", file,
        onProgress: (current, max) {
      if (current == max) {
        downFinish.complete(true);
      }
    });
    downFinish.future.then((value) {
      if (value) {
        AppRepository.platform.invokeMethod("copyLibAndWrite").then((value) {
          ToastUtils.show("热更新完成，请杀掉进程");
          _ifNeedReStart = true;
          //await Sputils.save(LAST_FIX_FILE_NAME, newFixFileName);
        });
      }
    });
  }

  //资源文件暂无没找到办法热更新，替代方案是，热更新资源放在getExternalStorageDirectory下，使用image.file,或者直接用image.network使用
  static Future hotUpgradeAssetsFile() async {
    String url =
        "https://raw.githubusercontent.com/RickyJun/test/master/image.jpg";
    String filePath =
        (await getExternalStorageDirectory()).path + "/" + "image.jpg";
    DioUtil.getInstance().download(url, filePath).then((value) {
      if (value != null) {
        if (value.statusCode == 200) {}
      }
    });
  }

  static Future<bool> _needHotfix(String newFixFileName) async {
    String value = await Sputils.get(LAST_FIX_FILE_NAME);
    //没有更新过
    if (value == null) {
      return true;
    } else {
      //该文件已经更新过
      if (newFixFileName == value) {
        return false;
      } else {
        return true;
      }
    }
  }

  //进入后台，主动杀掉进程
  static void onPaused() {
    if (_ifNeedReStart) {
      exit(0);
    }
  }
}
