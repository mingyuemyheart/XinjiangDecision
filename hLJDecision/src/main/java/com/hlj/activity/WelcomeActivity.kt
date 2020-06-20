package com.hlj.activity;

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.hlj.common.CONST
import com.hlj.common.ColumnData
import com.hlj.common.MyApplication
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_welcome.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException

class WelcomeActivity : BaseActivity(), AMapLocationListener {

	private var lat = 0.0
	private var lng = 0.0
	private var dataList : ArrayList<ColumnData> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_welcome)
		okHttpTheme()
		startLocation()

		tvVersion.text = "V${CommonUtil.getVersion(this)}"
	}

	/**
	 * 获取背景
	 */
	private fun okHttpTheme() {
		var delayMillis : Long = 0
		val url = "https://decision-admin.tianqi.cn/Home/work2019/decision_theme_data?appid=${CONST.APPID}"
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {}

				@Throws(IOException::class)
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					runOnUiThread {
						if (!TextUtils.isEmpty(result)) {
							try {
								val obj = JSONObject(result)
								if (!obj.isNull("style")) {
									MyApplication.setTheme(obj.getString("style"))
								}
								if (!obj.isNull("launch_img")) {
									val imgUrl = obj.getString("launch_img")
									if (!TextUtils.isEmpty(imgUrl)) {
										Picasso.get().load(imgUrl).into(imageView)
									}
								}
								if (!obj.isNull("launch_time")) {
									delayMillis = obj.getLong("launch_time")
								}
							} catch (e: JSONException) {
								e.printStackTrace()
							}
						}
					}
				}
			})
		}).start()

		Handler().postDelayed({
			imageView.visibility = View.VISIBLE

			Handler().postDelayed({
				val sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE)
				val userName = sharedPreferences.getString(CONST.UserInfo.userName, null)
				val pwd = sharedPreferences.getString(CONST.UserInfo.passWord, null)
				val token = sharedPreferences.getString(CONST.UserInfo.token, null)
				CONST.TOKEN = token
				CONST.USERNAME = userName
				CONST.PASSWORD = pwd
				if (!TextUtils.isEmpty(token)) {//手机号登录
					okHttpTokenLogin()
				} else {//账号密码登录
					if (!TextUtils.isEmpty(userName) && !TextUtils.equals(userName, CONST.publicUser)) { //决策用户
						okHttpLogin(userName, pwd)
					} else {
						okHttpLogin(CONST.publicUser, CONST.publicPwd)
					}
				}
			}, delayMillis*1000)
		}, 1000)
	}

	/**
	 * 开始定位
	 */
	private fun startLocation() {
		val mLocationOption = AMapLocationClientOption() //初始化定位参数
		val mLocationClient = AMapLocationClient(this) //初始化定位
		mLocationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		mLocationOption.isNeedAddress = true //设置是否返回地址信息（默认返回地址信息）
		mLocationOption.isOnceLocation = true //设置是否只定位一次,默认为false
		mLocationOption.isMockEnable = false //设置是否允许模拟位置,默认为false，不允许模拟位置
		mLocationOption.interval = 2000 //设置定位间隔,单位毫秒,默认为2000ms
		mLocationClient.setLocationOption(mLocationOption) //给定位客户端对象设置定位参数
		mLocationClient.setLocationListener(this)
		mLocationClient.startLocation() //启动定位
	}

	override fun onLocationChanged(amapLocation: AMapLocation?) {
		if (amapLocation != null && amapLocation.errorCode == AMapLocation.LOCATION_SUCCESS) {
			lat = amapLocation.latitude
			lng = amapLocation.longitude
		}
	}

	/**
	 * 手机号刷新token登录
	 */
	private fun okHttpTokenLogin() {
		val url = "http://decision-admin.tianqi.cn/Home/work2019/hlgRefreshLogin"
		val builder = FormBody.Builder()
		builder.add("mobile", CONST.USERNAME)
		builder.add("token", CONST.TOKEN)
		builder.add("appid", CONST.APPID)
		builder.add("device_id", "")
		builder.add("platform", "android")
		builder.add("os_version", Build.VERSION.RELEASE)
		builder.add("software_version", CommonUtil.getVersion(this))
		builder.add("mobile_type", Build.MODEL)
		builder.add("address", "")
		builder.add("lat", lat.toString() + "")
		builder.add("lon", lng.toString() + "")
		val body: RequestBody = builder.build()
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {}

				@Throws(IOException::class)
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					parseData(result)
				}
			})
		}).start()
	}

	/**
	 * 账号密码登录
	 */
	private fun okHttpLogin(userName: String, pwd: String) {
		val url = "http://decision-admin.tianqi.cn/Home/Work/login"
		val builder = FormBody.Builder()
		builder.add("username", userName)
		builder.add("password", pwd)
		builder.add("appid", CONST.APPID)
		builder.add("device_id", "")
		builder.add("platform", "android")
		builder.add("os_version", Build.VERSION.RELEASE)
		builder.add("software_version", CommonUtil.getVersion(this))
		builder.add("mobile_type", Build.MODEL)
		builder.add("address", "")
		builder.add("lat", lat.toString() + "")
		builder.add("lon", lng.toString() + "")
		val body: RequestBody = builder.build()
		Thread(Runnable {
			OkHttpUtil.enqueue(Request.Builder().post(body).url(url).build(), object : Callback {
				override fun onFailure(call: Call, e: IOException) {}

				@Throws(IOException::class)
				override fun onResponse(call: Call, response: Response) {
					if (!response.isSuccessful) {
						return
					}
					val result = response.body!!.string()
					parseData(result)
				}
			})
		}).start()
	}

	private fun parseData(result: String) {
		runOnUiThread {
			cancelDialog()
			if (!TextUtils.isEmpty(result)) {
				try {
					val obje = JSONObject(result)
					if (!obje.isNull("status")) {
						val status = obje.getInt("status")
						if (status == 1) { //成功
							val array = JSONArray(obje.getString("column"))
							dataList.clear()
							for (i in 0 until array.length()) {
								val obj = array.getJSONObject(i)
								val data = ColumnData()
								if (!obj.isNull("id")) {
									data.columnId = obj.getString("id")
								}
								if (!obj.isNull("localviewid")) {
									data.id = obj.getString("localviewid")
								}
								if (!obj.isNull("name")) {
									data.name = obj.getString("name")
								}
								if (!obj.isNull("default")) {
									data.level = obj.getString("default")
								}
								if (!obj.isNull("icon")) {
									data.icon = obj.getString("icon")
								}
								if (!obj.isNull("icon2")) {
									data.icon2 = obj.getString("icon2")
								}
								if (!obj.isNull("desc")) {
									data.desc = obj.getString("desc")
								}
								if (!obj.isNull("dataurl")) {
									data.dataUrl = obj.getString("dataurl")
								}
								if (!obj.isNull("showtype")) {
									data.showType = obj.getString("showtype")
								}
								if (!obj.isNull("child")) {
									val childArray = JSONArray(obj.getString("child"))
									for (j in 0 until childArray.length()) {
										val childObj = childArray.getJSONObject(j)
										val dto = ColumnData()
										if (!childObj.isNull("localviewid")) {
											dto.id = childObj.getString("localviewid")
										}
										if (!childObj.isNull("name")) {
											dto.name = childObj.getString("name")
										}
										if (!childObj.isNull("desc")) {
											dto.desc = childObj.getString("desc")
										}
										if (!childObj.isNull("icon")) {
											dto.icon = childObj.getString("icon")
										}
										if (!childObj.isNull("icon2")) {
											dto.icon2 = childObj.getString("icon2")
										}
										if (!childObj.isNull("showtype")) {
											dto.showType = childObj.getString("showtype")
										}
										if (!childObj.isNull("dataurl")) {
											dto.dataUrl = childObj.getString("dataurl")
										}
										if (!childObj.isNull("child")) {
											val child2Array = JSONArray(childObj.getString("child"))
											for (k in 0 until child2Array.length()) {
												val child2Obj = child2Array.getJSONObject(k)
												val child2 = ColumnData()
												if (!child2Obj.isNull("localviewid")) {
													child2.id = child2Obj.getString("localviewid")
												}
												if (!child2Obj.isNull("name")) {
													child2.name = child2Obj.getString("name")
												}
												if (!child2Obj.isNull("desc")) {
													child2.desc = child2Obj.getString("desc")
												}
												if (!child2Obj.isNull("icon")) {
													child2.icon = child2Obj.getString("icon")
												}
												if (!child2Obj.isNull("icon2")) {
													child2.icon2 = child2Obj.getString("icon2")
												}
												if (!child2Obj.isNull("dataurl")) {
													child2.dataUrl = child2Obj.getString("dataurl")
												}
												if (!child2Obj.isNull("showtype")) {
													child2.showType = child2Obj.getString("showtype")
												}
												dto.child.add(child2)
											}
										}
										data.child.add(dto)
									}
								}
								dataList.add(data)
							}
							if (!obje.isNull("info")) {
								val obj = JSONObject(obje.getString("info"))
								if (!obj.isNull("id")) {
									val uid = obj.getString("id")
									if (uid != null) {
										//把用户信息保存在sharedPreferance里
										if (!TextUtils.equals(CONST.USERNAME, CONST.publicUser)) { //决策用户
											val sharedPreferences = getSharedPreferences(CONST.USERINFO, Context.MODE_PRIVATE)
											val editor = sharedPreferences.edit()
											editor.putString(CONST.UserInfo.uId, uid)
											editor.putString(CONST.UserInfo.userName, CONST.USERNAME)
											editor.putString(CONST.UserInfo.passWord, CONST.PASSWORD)
											editor.putString(CONST.UserInfo.token, CONST.TOKEN)
											editor.apply()
											CONST.UID = uid
										}
										val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
										intent.putParcelableArrayListExtra("dataList", dataList)
										startActivity(intent)
										finish()
									}
								}
							}
						} else {
							//失败
							if (!obje.isNull("msg")) {
								val msg = obje.getString("msg")
								if (msg != null) {
									Toast.makeText(this@WelcomeActivity, msg, Toast.LENGTH_SHORT).show()
								}
								okHttpLogin(CONST.publicUser, CONST.publicPwd)
							}
						}
					}
				} catch (e: JSONException) {
					e.printStackTrace()
				}
			}
		}
	}

	override fun onKeyDown(KeyCode: Int, event: KeyEvent?): Boolean {
		return if (KeyCode == KeyEvent.KEYCODE_BACK) {
			true
		} else super.onKeyDown(KeyCode, event)
	}
	
}
