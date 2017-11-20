package com.scene.utils;

import android.content.Context;

import com.facebook.crypto.Entity;
import com.facebook.crypto.keychain.SimpleKeyChain;
import com.scene.file.crypt.util.AndroidCrypter;
import com.scene.file.crypt.util.Crypter;
import com.scene.file.crypt.util.JavaCrypter;

public class CryptCreater {
	public static String getPassword() {
		String pass = "informationscenecollection";
		return pass.substring(8, 14);
	}
	
	public static Crypter createJavaCrypter(Context context, String key) {
		String password = Keys.getKeys(context, key);
		return new JavaCrypter(password);
	}
	public static Crypter createAndroidCrypter(Context context, String key) {
		String password = Keys.getKeys(context, key);
		return new AndroidCrypter(new SimpleKeyChain(password, password), 
				new Entity(password));
	}
}
