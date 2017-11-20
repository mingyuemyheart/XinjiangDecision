package com.scene.utils;

import android.content.Context;
import android.text.TextUtils;


class Keys {
	
	public static final String getKeys(Context context, String keys) {
		if (context == null || !TextUtils.equals(context.getApplicationInfo().packageName, "com.information.collection")) {
			return null;
		}
		KeysParser parser = new KeysParser(keys);
		StringBuffer buffer = new StringBuffer();
		buffer.append(parser.key(0));
		buffer.append(parser.key(16));
		buffer.append(parser.key(13));
		buffer.append(parser.key(18));
		buffer.append(parser.key(6));
		buffer.append(parser.key(3));
		buffer.append(parser.key(16));
		buffer.append(parser.key(17));
		return buffer.toString();
	}
}
