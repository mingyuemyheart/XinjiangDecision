package com.scene.file.crypt.util;

import android.content.Context;

import com.scene.utils.CryptCreater;
 
 class KeysUtil {
	public static final Crypter getCrypter(Context context, boolean useJava, String key) {
		Crypter crypter = null;
		if (useJava) {
			crypter = CryptCreater.createJavaCrypter(context, key);
		} else {
			crypter = CryptCreater.createAndroidCrypter(context, key);
		}
		return crypter;
	}
 }
