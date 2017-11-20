package com.scene.file.crypt.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class JavaCrypter implements Crypter{
	private static final String CIPHER_ALGORITHM = "DES";
	private static final int KEY_LENGTH = 16;
	private final byte[] mKey; 	
	public JavaCrypter (String key) {
		ByteBuffer buffer = ByteBuffer.allocate(KEY_LENGTH);
		buffer.put(key.getBytes());
		mKey = buffer.array();
	}
	@Override
	public boolean isAvailable() {
		return true;
	}
	
	@Override
	public InputStream decrypt(File file) {
		if (file == null) {
			return null;
		}
		// Get the file to which ciphertext has been written.
		try {
			
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "Crypto");
	        DESKeySpec desKey = new DESKeySpec(mKey);
	        //创建一个密匙工厂，然后用它把DESKeySpec转换成
	        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(CIPHER_ALGORITHM);
	        SecretKey securekey = keyFactory.generateSecret(desKey);
	        //Cipher对象实际完成加密操作
	        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
	        //用密匙初始化Cipher对象
	        cipher.init(Cipher.DECRYPT_MODE, securekey, random);
			
			FileInputStream fileStream;
			fileStream = new FileInputStream(file);
			CipherInputStream inputStream = new CipherInputStream(fileStream, cipher);
			return inputStream;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return null;
	}
	
	@Override
	public OutputStream encrypt(File file) {
		if (file == null) {
			return null;
		}
		// Get the file to which ciphertext has been written.
		try {
			SecureRandom random = new SecureRandom();
	        DESKeySpec desKey = new DESKeySpec(mKey);
	        //创建一个密匙工厂，然后用它把DESKeySpec转换成
	        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(CIPHER_ALGORITHM);
	        SecretKey securekey = keyFactory.generateSecret(desKey);
	        //Cipher对象实际完成加密操作
	        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM	);
	        //用密匙初始化Cipher对象
	        cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
			OutputStream fileStream = new BufferedOutputStream(new FileOutputStream(file));
			// Creates an output stream which encrypts the data as
			// it is written to it and writes it out to the file.
			return new CipherOutputStream(fileStream, cipher);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}
