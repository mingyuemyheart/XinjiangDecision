package com.scene.file.crypt.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

public class AndroidCrypter implements Crypter{
	private final Crypto crypto;
	private final Entity entity;
	public AndroidCrypter (KeyChain chain, Entity entity) {
		crypto = new Crypto(chain,
				new SystemNativeCryptoLibrary());
		this.entity = entity;
	}
	
	@Override
	public boolean isAvailable() {
		return crypto.isAvailable();
	}
	
	@Override
	public InputStream decrypt(File file) {
		if (file == null) {
			return null;
		}
		// Get the file to which ciphertext has been written.
		try {
			FileInputStream fileStream;
			fileStream = new FileInputStream(file);
			// Creates an input stream which decrypts the data as
			// it is read from it.
			InputStream inputStream = crypto.getCipherInputStream(fileStream, entity);
			return inputStream;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CryptoInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyChainException e) {
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
			OutputStream fileStream = new BufferedOutputStream(new FileOutputStream(file));
			// Creates an output stream which encrypts the data as
			// it is written to it and writes it out to the file.
			OutputStream outputStream = crypto.getCipherOutputStream(fileStream, entity);
			return outputStream;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CryptoInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyChainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
