package com.scene.file.crypt.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface Crypter {
	
	boolean isAvailable();
	
	/**
	 * 
	 * 获取文件输入流
	 * @param file
	 * @return
	 */
	InputStream decrypt(File file);
	/**
	 * 
	 * 获取文件输出流
	 * @param file
	 * @return
	 */
	OutputStream encrypt(File file);
	
}
