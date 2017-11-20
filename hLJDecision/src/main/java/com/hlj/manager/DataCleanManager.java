package com.hlj.manager;

import java.io.File;
import java.math.BigDecimal;

import android.content.Context;
import android.os.Environment;

public class DataCleanManager {
	
	/**
	 * 获取缓存文件大小
	 * @param context
	 * @throws Exception
	 */
	public static String getCacheSize(Context context) throws Exception {
		long cacheSize = getFolderSize(context.getCacheDir());//内部缓存
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			long sdcardCacheSize = getFolderSize(context.getExternalCacheDir());//sdcard缓存
			cacheSize += sdcardCacheSize;
		}
		return getFormatSize(cacheSize);
	}
	
	/**
	 * 获取存储文件大小
	 * @param context
	 * @throws Exception
	 */
	public static String getLocalSaveSize(Context context) throws Exception {
		long cacheSize = getFolderSize(context.getFilesDir());//内部存储
		return getFormatSize(cacheSize);
	}
	
	private static long getFolderSize(File file) throws Exception {
		long size = 0;
		try {
			File[] fileList = file.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				// 如果下面还有文件
				if (fileList[i].isDirectory()) {
					size = size + getFolderSize(fileList[i]);
				} else {
					size = size + fileList[i].length();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return size;
	}

	/**
	 * 格式化单位
	 * 
	 * @param size
	 * @return
	 */
	private static String getFormatSize(double size) {
		double kiloByte = size / 1024;
		if (kiloByte < 1) {
			// return size + "Byte";
			return "0K";
		}

		double megaByte = kiloByte / 1024;
		if (megaByte < 1) {
			BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
			return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
		}

		double gigaByte = megaByte / 1024;
		if (gigaByte < 1) {
			BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
			return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
		}

		double teraBytes = gigaByte / 1024;
		if (teraBytes < 1) {
			BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
			return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
		}
		BigDecimal result4 = new BigDecimal(teraBytes);
		return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()+ "TB";
	}
	
	/**
	 * 清除缓存
	 * @param context
	 */
	public static void clearCache(Context context) {
		deleteDir(context.getCacheDir());
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			deleteDir(context.getExternalCacheDir());
		}
	}
	
	/**
	 * 清除本次存储文件
	 */
	public static void clearLocalSave(Context context) {
		deleteDir(context.getFilesDir());
	}

	/**
	 * 删除指定目录下的文件
	 * @param dir
	 * @return
	 */
	private static boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

}