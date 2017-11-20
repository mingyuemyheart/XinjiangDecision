package com.scene.file.crypt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.util.Log;

public class CryptUtil {
	private static final int BYTE_COUNT = 8 * 1024;
	/**
	 * 获取加密、解密实例
	 * @param userJava 是否用java标准加密、解密方式
	 * @return
	 */
	public static final Crypter getCrypter(Context context, boolean useJava, String key) {
		return KeysUtil.getCrypter(context, useJava, key);
	}
	
	public static final void asynEncrypt(final File file, final File destFile, final Crypter crypter) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				encrypt(file, destFile, crypter);
			}
		}).start();
	}
	/**
	 * 加密字符串，并写入文件中
	 * @param content 要加密的内容
	 * @param destFile 加密文件输出到此文件中
	 * @param chain 
	 * @param entity
	 * @return
	 */
	public static final boolean encrypt(String content, File destFile,
			Crypter crypter) {
		if (crypter == null || !crypter.isAvailable()) {
			return false;
		}
		try {
			OutputStream outputStream = crypter.encrypt(destFile);
			if (outputStream == null) {
				return false;
			}
			// Write plaintext to it.
			byte[] bytes = content.getBytes("utf-8");
			outputStream.write(bytes, 0, bytes.length);
			outputStream.flush();
			outputStream.close();
			return true;
		} catch (IOException e) {
			// TODO: handle exception
		}

		return false;}
	
	/**
	 * 加密文件
	 * @param file 要加密的原始文件
	 * @param destFile 加密文件输出到此文件中
	 * @param chain 
	 * @param entity
	 * @return
	 */
	public static final boolean encrypt( File file, File destFile, Crypter crypter) {
		if (crypter == null || !crypter.isAvailable()) {
			return false;
		}
		try {
			OutputStream outputStream = crypter.encrypt(destFile);
			if (outputStream == null) {
				return false;
			}
			// Write plaintext to it.
			FileInputStream input = new FileInputStream(file);
			byte[] bytes = new byte[BYTE_COUNT];
			int count = -1;
			while ((count = input.read(bytes)) != -1) {
				Log.d("CryptUtil", "count = " + count);
				outputStream.write(bytes, 0, count);
			}
			outputStream.flush();
			outputStream.close();
			input.close();
			return true;
		} catch (IOException e) {
			// TODO: handle exception
		} 

		return false;
	}
	
	/**
	 * 解密文件的内容
	 * @param file 要解密的原文件
	 * @param content 解密之后的内容
	 * @param chain
	 * @param entity
	 * @return
	 */
	public static String decrypt(File file, Crypter crypter) {
		if (crypter == null || !crypter.isAvailable()) {
			return null;
		}
		try {
			InputStream inputStream = crypter.decrypt(file);
			if (inputStream == null) return null;

			// Read into a byte array.
			int read;
			byte[] buffer = new byte[BYTE_COUNT];
			StringBuffer content = new StringBuffer();
			while ((read = inputStream.read(buffer)) != -1) {
				content.append(new String(buffer, 0, read, "utf-8"));
			}
			inputStream.close();
			inputStream = null;
			return content.toString();
		} catch (IOException e) {
			// TODO: handle exception
		} 
		return null;
	}
	/**
	 * 解密文件
	 * @param file 要解密的原文件
	 * @param destFile 解密之后新生成的文件。
	 * @param chain
	 * @param entity
	 * @return
	 */
	public static boolean decrypt(File file, File destFile, Crypter crypter) {
		if (crypter == null || !crypter.isAvailable()) {
			return false;
		}
		try {
			InputStream inputStream = crypter.decrypt(file);
			if (inputStream == null) return false;
			
			// Read into a byte array.
			int read;
			byte[] buffer = new byte[BYTE_COUNT];
			FileOutputStream out = new FileOutputStream(destFile);
			// You must read the entire stream to completion.
			// The verification is done at the end of the stream.
			// Thus not reading till the end of the stream will cause
			// a security bug. For safety, you should not
			// use any of the data until it's been fully read or throw
			// away the data if an exception occurs.
			while ((read = inputStream.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			out.flush();
			out.close();
			out = null;
			inputStream.close();
			inputStream = null;
			return true;
		} catch (IOException e) {
			// TODO: handle exception
		} 
		return false;
	}
}
