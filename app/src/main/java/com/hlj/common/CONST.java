package com.hlj.common;

import com.amap.api.maps.model.LatLng;

public class CONST {

	public static String noValue = "--";
	public static final String imageSuffix = ".png";//图标后缀名
	public static final String publicUser = "publicuser";//公众账号
	public static final String publicPwd = "publicuser@2021";//公众密码
	public static String APPID = "1";
	public static final LatLng guizhouLatLng = new LatLng(48.602915,128.121040);
	public static boolean isDelete = false;//判断是否可以删除
	public static String BUILD_URL = "http://decision-admin.tianqi.cn/infomes/data/heilongjiang/about_heilongjiang.html";//企业信息地址

	//showType类型，区分本地类或者图文
	public static final String LOCAL = "local";
	public static final String NEWS = "news";
	public static final String PRODUCT = "product";
	public static final String URL = "url";
	public static final String PDF = "pdf";
	public static final String MP4 = "mp4";

	//intent传值的标示
	public static final String LOCAL_ID = "local_id";//栏目id
	public static final String COLUMN_ID = "column_id";//栏目id
	public static final String WEB_URL = "web_Url";//网页地址的标示
	public static final String LAT = "lat";
	public static final String LNG = "lng";
	public static final String ACTIVITY_NAME = "activity_name";//界面名称
	public static final String INTENT_IMGURL = "intent_imgurl";//分享时分享的图片
	public static final String BROADCAST_ACTION = "broadcast_action";//不同fragment区分广播，解决性能问题

	//下拉刷新progresBar四种颜色
	public static final int color1 = android.R.color.holo_blue_bright;
	public static final int color2 = android.R.color.holo_blue_light;
	public static final int color3 = android.R.color.holo_blue_bright;
	public static final int color4 = android.R.color.holo_blue_light;

	//本地保存用户信息参数
	public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
	public static class UserInfo {
		public static final String uId = "uId";
		public static final String userName = "uName";
		public static final String passWord = "pwd";
		public static final String token = "token";
		public static final String groupId = "groupId";
		public static final String uGroupName = "uGroupName";
	}

	public static String UID = "2606";//用户id
	public static String USERNAME = publicUser;//用户名
	public static String PASSWORD = publicPwd;//用户密码
	public static String TOKEN = "";//token
	public static String GROUPID = "50";
	public static String UGROUPNAME = "";//uGroupName

	//预警颜色对应规则
	public static String[] blue = {"01", "_blue"};
	public static String[] yellow = {"02", "_yellow"};
	public static String[] orange = {"03", "_orange"};
	public static String[] red = {"04", "_red"};

}
