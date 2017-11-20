package com.hlj.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

public class CustomHttpClient {
	private static final String CHARSET = HTTP.UTF_8;
	private static HttpClient customerHttpClient;
	private static final String TAG = "HttpClient";
	private static List<HttpClient> clientList = new ArrayList<HttpClient>();
	private static List<HttpRequest> requestList = new ArrayList<HttpRequest>();

	private static HttpClient httpClient;
	private static HttpRequest httpRequest;
	public static int TIME_OUT = 15000;

	public static synchronized HttpClient getHttpClient() {
		if (null == customerHttpClient) {
			HttpParams params = new BasicHttpParams();
			// 设置一些基本参数
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, CHARSET);
			HttpProtocolParams.setUseExpectContinue(params, true);
			HttpProtocolParams.setUserAgent(params, "Mozilla/5.0(Linux;U;Android 4.0;en-us;Nexus One Build.FRG83) "
					+ "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
			// 超时设置
			/* 从连接池中取连接的超时时间 */
			ConnManagerParams.setTimeout(params, TIME_OUT);
			/* 连接超时 */
			HttpConnectionParams.setConnectionTimeout(params, TIME_OUT);
			/* 请求超时 */
			HttpConnectionParams.setSoTimeout(params, TIME_OUT);

			// 设置我们的HttpClient支持HTTP和HTTPS两种模式
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

			// 使用线程安全的连接管理来创建HttpClient
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
			customerHttpClient = new DefaultHttpClient(conMgr, params);
		}
		httpClient = customerHttpClient;
		clientList.add(customerHttpClient);
		return customerHttpClient;
	}

	public static String get(String url) {
		HttpClient client = new DefaultHttpClient();
		if (!TextUtils.isEmpty(url) && url.contains("https://")) {
			client = getHttpClient();
		}
		HttpGet request = new HttpGet(url);
		if (url.contains("http://decision.tianqi.cn/") || url.contains("http://radar.tianqi.cn/radar/img_layer/")
				|| url.contains("http://www.welife100.com/Wap/Fengc/index") || url.contains("http://radar.tianqi.cn/typhoon/typhoon.html")) {
			request.setHeader("Host", "decision.tianqi.cn");
			request.addHeader("Referer", CustomHttpClient.getRequestHeader());//添加请求头
		}
		try {
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new RuntimeException("请求失败");
			}
			HttpEntity entity = response.getEntity();
			String result = entity == null ? null : EntityUtils.toString(entity, CHARSET);
			return result;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO: handle exception
			//请求失败所抛的异常
			e.printStackTrace();
		}
		httpRequest = request;
		requestList.add(request);
		return null;
	}

	public static String post(String url, List<NameValuePair> paramsList) {
		HttpClient client = new DefaultHttpClient();
		if (!TextUtils.isEmpty(url) && url.contains("https://")) {
			client = getHttpClient();
		}
		HttpPost request = new HttpPost(url);
		try {
			// 编码参数
			List<NameValuePair> formparams = new ArrayList<NameValuePair>(); // 请求参数
			for (NameValuePair p : paramsList) {
				formparams.add(p);
			}
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, CHARSET);
			// 创建POST请求
			request.setEntity(entity);
			// 发送请求
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new RuntimeException("请求失败");
			}
			HttpEntity resEntity = response.getEntity();
			String result = (resEntity == null) ? null : EntityUtils.toString(resEntity, CHARSET);
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		requestList.add(request);
		return null;
	}

	/**
	 * 关闭所有网络网络请求。
	 */
	public static void shuttdownAllReqeust() {
		for (HttpRequest request : requestList) {
			if (request != null) {
				if (request instanceof HttpGet) {
					((HttpGet) request).abort();
				} else if (request instanceof HttpPost) {
					((HttpPost) request).abort();
				}
			}
		}
		for (HttpClient client : clientList) {
			if (client != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static void shuttdownRequest() {
		if (httpRequest != null) {
			if (httpRequest instanceof HttpGet) {
				((HttpGet) httpRequest).abort();
			} else if (httpRequest instanceof HttpPost) {
				((HttpPost) httpRequest).abort();
			}
			
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}
	
	/**
	 * 获取http://decision.tianqi.cn域名的请求头
	 * @return
	 */
	public static String getRequestHeader() {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd00");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd06");
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd12");
		SimpleDateFormat sdf4 = new SimpleDateFormat("yyyyMMdd18");
		SimpleDateFormat sdf5 = new SimpleDateFormat("yyyyMMddHH");
		long time1 = 0, time2 = 0, time3 = 0, time4 = 0;
		long currentTime = 0;
		try {
			time1 = sdf5.parse(sdf1.format(new Date())).getTime();
			time2 = sdf5.parse(sdf2.format(new Date())).getTime();
			time3 = sdf5.parse(sdf3.format(new Date())).getTime();
			time4 = sdf5.parse(sdf4.format(new Date())).getTime();
			currentTime = new Date().getTime();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String date = null;
		if (currentTime >= time1 && currentTime < time2) {
			date = sdf1.format(new Date());
		}else if (currentTime >= time2 && currentTime < time3) {
			date = sdf2.format(new Date());
		}else if (currentTime >= time3 && currentTime < time4) {
			date = sdf3.format(new Date());
		}else if (currentTime >= time4) {
			date = sdf4.format(new Date());
		}
		String publicKey = "http://decision.tianqi.cn/?date="+date;//公钥
		String privateKye = "url_private_key_789";//私钥
		String result = "";
		try{
			byte[] rawHmac = null;
			byte[] keyBytes = privateKye.getBytes("UTF-8");
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);
			rawHmac = mac.doFinal(publicKey.getBytes("UTF-8"));
			result = Base64.encodeToString(rawHmac, Base64.DEFAULT);
//			result = URLEncoder.encode(result, "UTF-8");
			result = "http://decision.tianqi.cn/"+result;
		}catch(Exception e){
			Log.e("SceneException", e.getMessage(), e);
		}
		return result;
	}
	
}
