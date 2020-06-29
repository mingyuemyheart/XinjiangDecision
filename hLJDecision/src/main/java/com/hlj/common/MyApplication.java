package com.hlj.common;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hlj.activity.HWarningDetailActivity;
import com.hlj.dto.WarningDto;
import com.hlj.utils.CrashHandler;
import com.iflytek.cloud.SpeechUtility;
import com.umeng.message.IUmengCallback;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.common.inter.ITagManager.Result;
import com.umeng.message.entity.UMessage;
import com.umeng.message.tag.TagManager;
import com.umeng.message.tag.TagManager.TCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyApplication extends Application{
	
	public static final String UPDATE_STATUS_ACTION = "shawn.cxwl.com.hlj.action.UPDATE_STATUS";
    private static PushAgent mPushAgent = null;
    private static TagManager tagManager = null;

	private static String appTheme = "0";

	public static String getAppTheme() {
		return appTheme;
	}
	public static void setTheme(String theme) {
		appTheme = theme;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());

		//科大讯飞
		SpeechUtility.createUtility(this, "appid=" + "5983c375");

		//蒲公英崩溃日志搜集
		registerUmengPush();
	}
	
    /**
	 * 注册umeng推送
	 */
	private void registerUmengPush() {
		mPushAgent = PushAgent.getInstance(this);
		tagManager = mPushAgent.getTagManager();
        mPushAgent.setDebugMode(false);
        
        //参数number可以设置为0~10之间任意整数。当参数为0时，表示不合并通知
        mPushAgent.setDisplayNotificationNumber(0);
        
//        //sdk开启通知声音
//        mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);
//        // sdk关闭通知声音
//		mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
//        // 通知声音由服务端控制
//		mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SERVER);
//		mPushAgent.setNotificationPlayLights(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
//		mPushAgent.setNotificationPlayVibrate(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
//        
//        //此处是完全自定义处理设置
//        mPushAgent.setPushIntentServiceClass(MyPushIntentService.class);
        
        //注册推送服务 每次调用register都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
            	Intent intent = new Intent();
            	intent.setAction(UPDATE_STATUS_ACTION);
            	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendBroadcast(intent);
                Log.e("deviceToken", deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                sendBroadcast(new Intent(UPDATE_STATUS_ACTION));
            }
        });


        /**
         * 自定义行为的回调处理
         * UmengNotificationClickHandler是在BroadcastReceiver中被调用，故
         * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
         * */
        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {
            @Override
            public void dealWithCustomAction(Context context, UMessage msg) {
//            	setSound(getApplicationContext(), R.raw.umeng_push_notification_default_sound);
            	if (msg.extra != null) {
            		JSONObject obj = new JSONObject(msg.extra);
            		try {
						String url = obj.getString("url");
						if (!TextUtils.isEmpty(url)) {
							WarningDto data = new WarningDto();
							data.html = url;
							Intent intentDetail = new Intent(getApplicationContext(), HWarningDetailActivity.class);
							intentDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							Bundle bundle = new Bundle();
							bundle.putParcelable("data", data);
							intentDetail.putExtras(bundle);
							startActivity(intentDetail);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
            	
            }
        };
        mPushAgent.setNotificationClickHandler(notificationClickHandler);
        
        //使用自定义的NotificationHandler，来结合友盟统计处理消息通知
        //参考http://bbs.umeng.com/thread-11112-1-1.html
//        CustomNotificationHandler notificationClickHandler = new CustomNotificationHandler();
//        mPushAgent.setNotificationClickHandler(notificationClickHandler);
        
//        /**
//         * 自定义通知栏样式回调
//         */
//        UmengMessageHandler messageHandler = new UmengMessageHandler() {
//            @Override
//            public void dealWithCustomMessage(final Context context, final UMessage msg) {
//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        // 对自定义消息的处理方式，点击或者忽略
//                        boolean isClickOrDismissed = true;
//                        if (isClickOrDismissed) {
//                            //自定义消息的点击统计
//                            UTrack.getInstance(getApplicationContext()).trackMsgClick(msg);
//                        } else {
//                            //自定义消息的忽略统计
//                            UTrack.getInstance(getApplicationContext()).trackMsgDismissed(msg);
//                        }
//                        Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
//
//            //自定义通知栏样式
//            @SuppressWarnings("deprecation")
//			@Override
//            public Notification getNotification(Context context, UMessage msg) {
//                switch (msg.builder_id) {
//                    case 1:
//                        Notification.Builder builder = new Notification.Builder(context);
//                        RemoteViews myNotificationView = new RemoteViews(context.getPackageName(), R.layout.notification_view);
//                        myNotificationView.setTextViewText(R.id.notification_title, msg.title);
//                        myNotificationView.setTextViewText(R.id.notification_text, msg.text);
//                        myNotificationView.setImageViewBitmap(R.id.notification_large_icon, getLargeIcon(context, msg));
//                        myNotificationView.setImageViewResource(R.id.notification_small_icon, getSmallIconId(context, msg));
//                        builder.setContent(myNotificationView)
//                                .setSmallIcon(getSmallIconId(context, msg))
//                                .setTicker(msg.ticker)
//                                .setAutoCancel(true);
//                        return builder.getNotification();
//                    default:
//                        //默认为0，若填写的builder_id并不存在，也使用默认。
//                        return super.getNotification(context, msg);
//                }
//            }
//        };
//        mPushAgent.setMessageHandler(messageHandler);
    }
	
	private class CustomNotificationHandler extends UmengNotificationClickHandler {
        @Override
        public void dismissNotification(Context context, UMessage msg) {
            super.dismissNotification(context, msg);
        }
         
        @Override
        public void launchApp(Context context, UMessage msg) {
            super.launchApp(context, msg);
            Map<String, String> map = new HashMap<String, String>();
            map.put("action", "launch_app");
            Toast.makeText(context, "launch_app", Toast.LENGTH_SHORT).show();
        }
         
        @Override
        public void openActivity(Context context, UMessage msg) {
            super.openActivity(context, msg);
            Map<String, String> map = new HashMap<String, String>();
            map.put("action", "open_activity");
            Toast.makeText(context, "open_activity", Toast.LENGTH_SHORT).show();
        }
         
        @Override
        public void openUrl(Context context, UMessage msg) {
            super.openUrl(context, msg);
            Map<String, String> map = new HashMap<String, String>();
            map.put("action", "open_url");
            Toast.makeText(context, "open_url", Toast.LENGTH_SHORT).show();
        }
         
        @Override
        public void dealWithCustomAction(Context context, UMessage msg) {
            super.dealWithCustomAction(context, msg);
            Map<String, String> map = new HashMap<String, String>();
            map.put("action", "custom_action");
            Toast.makeText(context, "custom_action", Toast.LENGTH_SHORT).show();
        }
         
        @Override
        public void autoUpdate(Context context, UMessage msg) {
            super.autoUpdate(context, msg);
            Map<String, String> map = new HashMap<String, String>();
            map.put("action", "auto_update");
            Toast.makeText(context, "auto_update", Toast.LENGTH_SHORT).show();
        }
	}

	/**
	 * 添加tags，添加多个tag以","隔开
	 * @param tags
	 */
	public static void resetTags(final String tags) {
		if (tagManager != null) {
			tagManager.update(new TCallBack() {
				@Override
				public void onMessage(boolean arg0, Result arg1) {
					Log.d("", "");
					tagManager.list(new TagManager.TagListCallBack() {
						@Override
						public void onMessage(boolean arg0, List<String> arg1) {
							Log.d("", "");
						}
					});
				}
			}, tags);
//			tagManager.reset(new TCallBack() {
//				@Override
//				public void onMessage(boolean arg0, Result arg1) {
//					Log.d("", "");
//					tagManager.add(new TCallBack() {
//						@Override
//						public void onMessage(boolean arg0, Result arg1) {
//							Log.d("", "");
////							File file = new File(Environment.getExternalStorageDirectory()+ "/"+ "111.txt");
////							FileOutputStream fout;
////							try {
////								fout = new FileOutputStream(file);
////								byte[] bytes = tags.getBytes();
////								fout.write(bytes);
////								fout.close();
////							} catch (FileNotFoundException e) {
////								e.printStackTrace();
////							} catch (IOException e) {
////								// TODO Auto-generated catch block
////								e.printStackTrace();
////							}
//
//							tagManager.list(new TagListCallBack() {
//								@Override
//								public void onMessage(boolean arg0, List<String> arg1) {
//									// TODO Auto-generated method stub
//									Log.d("", "");
////									String aaa = "";
////									for (int i = 0; i < arg1.size(); i++) {
////										aaa = aaa+arg1.get(i)+",";
////									}
////									File file2 = new File(Environment.getExternalStorageDirectory()+ "/"+ "222.txt");
////									FileOutputStream fout;
////									try {
////										fout = new FileOutputStream(file2);
////										byte[] bytes = tags.getBytes();
////										fout.write(bytes);
////										fout.close();
////									} catch (FileNotFoundException e) {
////										e.printStackTrace();
////									} catch (IOException e) {
////										// TODO Auto-generated catch block
////										e.printStackTrace();
////									}
//								}
//							});
//						}
//					}, tags);
//				}
//			});
		}
	}
	
	/**
	 * 打开推送
	 */
	public static void enablePush() {
		if (mPushAgent != null) {
			mPushAgent.enable(new IUmengCallback() {
				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onFailure(String arg0, String arg1) {
					// TODO Auto-generated method stub
					
				}
			});
		}
	}
	
	/**
	 * 关闭推送
	 */
	public static void disablePush() {
		if (mPushAgent != null) {
			mPushAgent.disable(new IUmengCallback() {
				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onFailure(String arg0, String arg1) {
					// TODO Auto-generated method stub
					
				}
			});
		}
	}
	
	/**
	 * 设置推送消息声音
	 * @param context
	 * @param id R.raw.id
	 */
	public static void setSound(Context context, int id) {
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification mNotification = new Notification();
		mNotification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + id);
		// mNotification.defaults = Notification.DEFAULT_SOUND;
		manager.notify(mNotification.icon, mNotification);
	}

	private static Map<String,Activity> destoryMap = new HashMap<>();
	/**
	 * 添加到销毁队列
	 * @param activity 要销毁的activity
	 */
	public static void addDestoryActivity(Activity activity, String activityName) {
		destoryMap.put(activityName,activity);
	}

	/**
	 *销毁指定Activity
	 */
	public static void destoryActivity() {
		Set<String> keySet=destoryMap.keySet();
		for (String key:keySet){
			destoryMap.get(key).finish();
		}
	}

}
