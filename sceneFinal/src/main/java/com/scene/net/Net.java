package com.scene.net;

import java.io.File;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import net.tsz.afinal.http.HttpHandler;

public class Net {
	private static FinalHttp client = new FinalHttp();

	  public static void get(String url, AjaxParams params, AjaxCallBack<?> callback) {
		  getClient().get(url, params, callback);
	  }

	  public static void post(String url, AjaxParams params, AjaxCallBack<?> callback) {
	      getClient().post(url, params, callback);
	  }
	  
	  public static HttpHandler<File> down(String url, String target, boolean isResume, AjaxCallBack<File> callback) {
		  return getClient().download(url, target, isResume, callback);
		  
	  }
	  public static HttpHandler<File> down(String url,  String target, boolean isResume, AjaxCallBack<File> callback, boolean isRedirect) {
		 getClient().getHttpClient().getParams().setBooleanParameter("http.protocol.allow-circular-redirects", isRedirect);
		  return getClient().download(url, target, isResume, callback);
		  
	  }
	  public static Object getSync(String url, AjaxParams params) {
		  return getClient().getSync(url, params);
	  }
	  
	  public static Object post(String url, AjaxParams params) {
		  return getClient().postSync(url, params);
	  }
	  
	  private static FinalHttp getClient() {
		  return client;
	  }

}
