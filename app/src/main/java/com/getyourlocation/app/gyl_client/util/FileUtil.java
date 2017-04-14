package com.getyourlocation.app.gyl_client.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.getyourlocation.app.gyl_client.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by lchad on 2016/11/1.
 * Github: https://github.com/lchad
 */
public class FileUtil {
    private static final String TAG = "FileUtil";

    /**
     * 检测SDCard是否存在
     *
     * @return 是否存在sd卡
     */
    public static boolean checkoutSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 检测Lua文件是否成功复制到sd卡内.
     */
    public static boolean copyLuaFinished() {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + Constant.LUR_NAME);
        Log.d("lua: ", file.getAbsolutePath() + " " + file.exists());
        return file.exists();
    }

    /**
     * 将asserts下一个指定文件夹中所有文件copy到SDCard中
     *
     * @param context     上下文对象
     * @param source      源文件夹
     * @param destination 目的文件夹
     */
    public static void copyDirToSDCardFromAsserts(Context context, String source, String destination) {

        try {
            AssetManager assetManager = context.getAssets();
            String[] fileList = assetManager.list(destination);
            outputStr(destination, fileList); // 输出destination中所有的文件名
            String dir = Environment.getExternalStorageDirectory() + File.separator + source;

            if (fileList != null && fileList.length > 0) {
                File file;

                // 创建文件夹
                file = new File(dir);
                if (!file.exists()) {
                    boolean success = file.mkdirs();
                    if (!success) {
                        Log.e("TAG", "make dir failed!");
                    }
                } else {
                    Log.d("FileUtil", dir + "已存在.");
                }

                // 创建文件
                InputStream inputStream = null;
                FileOutputStream outputStream = null;
                byte[] buffer = new byte[1024];
                int len;
                for (String aFileList : fileList) {
                    file = new File(dir, aFileList);
                    if (!file.exists()) {
                        boolean success = file.createNewFile();
                        if (!success) {
                            Log.e("TAG", "create new file failed!");
                        }
                    }
                    inputStream = assetManager.open(destination + File.separator + aFileList);
                    outputStream = new FileOutputStream(file);
                    while ((len = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, len);
                    }
                    outputStream.flush();
                }

                // 关流
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            Log.e("FileUtil", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 拷贝 lua 文件至 sdcard 目录
     */
    public static void copyLuaToStorage(Context context) {
        if (copyLuaFinished()) {
            Log.d(TAG, "Lua already copied");
            return;
        }
        if (checkoutSDCard()) {
            copyDirToSDCardFromAsserts(context, Constant.LUR_NAME, "font");
            copyDirToSDCardFromAsserts(context, Constant.LUR_NAME, Constant.LUR_NAME);
            Log.d(TAG, "Lua copied");
        } else {
            Log.d(TAG, "No sdcard");
        }
    }

    /**
     * 作用：输出文件夹中文件名
     *
     * @param path    文件夹路径
     * @param listStr 文件名数组
     */
    private static void outputStr(String path, String[] listStr) {
        if (listStr != null) {
            if (listStr.length <= 0) {
                Log.d("FileUtil", path + "文件为空");
            } else {
                Log.d("FileUtil", path + "文件中有以下文件：");
                for (String str : listStr) {
                    Log.d("FileUtil", str);
                }
            }
        }
    }

}
