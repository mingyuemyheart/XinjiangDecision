package com.hlj.fragment

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hlj.common.CONST
import com.hlj.dto.WarningDto
import com.hlj.manager.DBManager
import com.hlj.utils.CommonUtil
import com.hlj.utils.OkHttpUtil
import kotlinx.android.synthetic.main.fragment_warning_detail.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import shawn.cxwl.com.hlj.R
import java.io.IOException

/**
 * 预警详情
 */
class WarningDetailFragment : BaseFragment() {

    private var data: WarningDto? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_warning_detail, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWidget()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        data = arguments!!.getParcelable("data")
        okHttpWarningDetail()
    }
    
    private fun okHttpWarningDetail() {
        if (data == null || TextUtils.isEmpty(data!!.html)) {
            return
        }
        Thread(Runnable {
            val url = "http://decision.tianqi.cn/alarm12379/content2/${data!!.html}"
            OkHttpUtil.enqueue(Request.Builder().url(url).build(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {}

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        return
                    }
                    val result = response.body!!.string()
                    activity!!.runOnUiThread {
                        if (isAdded) { //判断fragment是否已经添加
                            if (!TextUtils.isEmpty(result)) {
                                try {
                                    val obj = JSONObject(result)
                                    if (!obj.isNull("sendTime")) {
                                        tvTime!!.text = obj.getString("sendTime")
                                    }
                                    if (!obj.isNull("description")) {
                                        tvIntro!!.text = obj.getString("description")
                                    }
                                    if (!obj.isNull("headline")) {
                                        tvName!!.text = obj.getString("headline")
                                    }
                                    var bitmap: Bitmap? = null
                                    if (obj.getString("severityCode") == CONST.blue[0]) {
                                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + obj.getString("eventType") + CONST.blue[1] + CONST.imageSuffix)
                                        if (bitmap == null) {
                                            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + "default" + CONST.blue[1] + CONST.imageSuffix)
                                        }
                                    } else if (obj.getString("severityCode") == CONST.yellow[0]) {
                                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + obj.getString("eventType") + CONST.yellow[1] + CONST.imageSuffix)
                                        if (bitmap == null) {
                                            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + "default" + CONST.yellow[1] + CONST.imageSuffix)
                                        }
                                    } else if (obj.getString("severityCode") == CONST.orange[0]) {
                                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + obj.getString("eventType") + CONST.orange[1] + CONST.imageSuffix)
                                        if (bitmap == null) {
                                            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + "default" + CONST.orange[1] + CONST.imageSuffix)
                                        }
                                    } else if (obj.getString("severityCode") == CONST.red[0]) {
                                        bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + obj.getString("eventType") + CONST.red[1] + CONST.imageSuffix)
                                        if (bitmap == null) {
                                            bitmap = CommonUtil.getImageFromAssetsFile(activity, "warning/" + "default" + CONST.red[1] + CONST.imageSuffix)
                                        }
                                    }
                                    imageView!!.setImageBitmap(bitmap)
                                    initDBManager()
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            })
        }).start()
    }

    /**
     * 初始化数据库
     */
    private fun initDBManager() {
        val dbManager = DBManager(activity)
        dbManager.openDateBase()
        dbManager.closeDatabase()
        val database = SQLiteDatabase.openOrCreateDatabase(DBManager.DB_PATH + "/" + DBManager.DB_NAME, null)
        var cursor: Cursor? = null
        cursor = database.rawQuery("select * from " + DBManager.TABLE_NAME2 + " where WarningId = " + "\"" + data!!.type + data!!.color + "\"", null)
        var content: String? = null
        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            content = cursor.getString(cursor.getColumnIndex("WarningGuide"))
        }
        if (!TextUtils.isEmpty(content)) {
            tvGuide!!.text = getString(R.string.warning_guide).toString() + content
            tvGuide!!.visibility = View.VISIBLE
        } else {
            tvGuide!!.visibility = View.GONE
        }
    }
	
}
