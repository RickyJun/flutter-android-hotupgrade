package cn.weeget.youxuan.hotfix.utils;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FlutterHotFixUtils {
    static final String TAG = "FlutterHotFixUtils";
    static String hotFixFileName = "libapp_fix.so";
    static String lastVersionCodeKey = "lastVersionCodeKey";
    //copy 自 shared_preferences
    static final String SHARED_PREFERENCES_NAME = "FlutterSharedPreferences";
    static android.content.SharedPreferences preferences;
    /**
     * 将fix.so文件拷贝到私有目录libs下
     */
    public static String copyLibAndWrite(Context context, String fileName){
        try {
            if(fileName != null){
                hotFixFileName = fileName;
            }

            File destFile = new File(getHotFixLibsDir(context) + File.separator + fileName);
            String path = context.getExternalFilesDir(null).getPath();
            File targetFile = new File(path + File.separator + fileName);
            if(!targetFile.exists()){
                return null;
            }
            if (destFile.exists()) {
                destFile.delete();
            }
            if (!destFile.exists()){


                boolean res = destFile.createNewFile();
                if (res){
                    FileInputStream is = new FileInputStream(targetFile);
                    FileOutputStream fos = new FileOutputStream(destFile);
                    byte[] buffer = new byte[is.available()];
                    int byteCount;
                    while ((byteCount = is.read(buffer)) != -1){
                        fos.write(buffer,0,byteCount);
                    }
                    fos.flush();
                    is.close();
                    fos.close();
                    //文件转移完成，删除源文件
                    targetFile.delete();
                    getPreferences(context).edit().putString(lastVersionCodeKey,getCurrentVersionCode(context)).commit();
                    return destFile.getAbsolutePath();
                }
            }
        }catch (Exception e){
            Log.e(TAG,"copyLibAndWrite failed",e);
            e.printStackTrace();
        }
        return "";
    }
    static String getCurrentVersionCode(Context context){
        Long versionCode = null;
        try {
            versionCode = context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).getLongVersionCode();
            return versionCode+"";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static void checkIfNeedToDeleteFixFile(Context context){
        String lastVersionCode = getPreferences(context).getString(lastVersionCodeKey,null);
        String currentVersionCode = getCurrentVersionCode(context);
        if(lastVersionCode != null && !lastVersionCode.equals(currentVersionCode)){
            String fixLibPath = getHotFixLibsDir(context)+File.separator+hotFixFileName;
            File file = new File(fixLibPath);
            if(file.exists()){
                file.delete();
            }
        }
    }
    static android.content.SharedPreferences getPreferences(Context context){
        if(preferences == null){
            preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
        return preferences;
    }
    static String getHotFixLibsDir(@NonNull Context applicationContext){
        File dir = applicationContext.getDir("libs", Activity.MODE_PRIVATE);
        return dir.getPath();
    }
    public static String getFixLibappFilePath(@NonNull Context applicationContext){
        String fixLibPath = getHotFixLibsDir(applicationContext)+File.separator+hotFixFileName;
        File file = new File(fixLibPath);
        if(file.exists()){
            return fixLibPath;
        }
        return null;
    }
    public static String getFixLibappFileName(){
        return hotFixFileName;
    }

}