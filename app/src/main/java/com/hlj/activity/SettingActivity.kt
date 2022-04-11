package com.hlj.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import android.widget.Toast
import com.hlj.common.CONST
import com.hlj.common.MyApplication
import com.hlj.manager.DataCleanManager
import com.hlj.utils.AutoUpdateUtil
import com.hlj.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.dialog_delete.view.tvContent
import kotlinx.android.synthetic.main.dialog_delete.view.tvMessage
import kotlinx.android.synthetic.main.dialog_delete.view.tvNegtive
import kotlinx.android.synthetic.main.dialog_delete.view.tvPositive
import kotlinx.android.synthetic.main.dialog_prompt.view.*
import kotlinx.android.synthetic.main.dialog_qr_code.view.*
import kotlinx.android.synthetic.main.layout_title.*
import shawn.cxwl.com.hlj.R
import java.io.File
import java.io.FileOutputStream

/**
 * 设置
 */
class SettingActivity : BaseActivity(), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        MyApplication.addDestoryActivity(this, "SettingActivity")
        initWidget()
    }

    /**
     * 初始化控件
     */
    private fun initWidget() {
        llBack!!.setOnClickListener(this)
        tvEmail!!.setOnClickListener(this)
        llVersion!!.setOnClickListener(this)
        llClearCache!!.setOnClickListener(this)
        llBuild!!.setOnClickListener(this)
        llCity!!.setOnClickListener(this)
        llProtocal!!.setOnClickListener(this)
        llPolicy!!.setOnClickListener(this)
        llQrCode!!.setOnClickListener(this)
        tvTitle!!.text = getString(R.string.setting)
        tvLogout!!.setOnClickListener(this)
        tvVersion!!.text = CommonUtil.getVersion(this)
        ivPortrait!!.setOnClickListener(this)
        tvUserName!!.setOnClickListener(this)

        if (TextUtils.equals(MyApplication.USERNAME, CONST.publicUser) || TextUtils.isEmpty(MyApplication.USERNAME)) { //公众用户或为空
            tvUserName!!.text = "点击登录"
            tvLogout!!.visibility = View.GONE
            clUnit.visibility = View.GONE
        } else {
            tvUserName!!.text = MyApplication.USERNAME
            tvNumber!!.text = MyApplication.MOBILE
            tvUnit!!.text = MyApplication.DEPARTMENT
            tvLogout!!.visibility = View.VISIBLE
            clUnit.visibility = View.VISIBLE
        }
        tvCache!!.text = DataCleanManager.getCacheSize(this)
    }

    /**
     * 删除对话框
     * @param message 标题
     * @param content 内容
     * @param flag 0删除本地存储，1删除缓存
     */
    private fun deleteDialog(flag: Boolean, message: String, content: String, textView: TextView?) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_delete, null)
        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()
        view.tvMessage.text = message
        view.tvContent.text = content
        view.tvContent.visibility = View.VISIBLE
        view.llNegative.setOnClickListener { dialog.dismiss() }
        view.llPositive.setOnClickListener {
            dialog.dismiss()
            if (flag) {
                DataCleanManager.clearCache(this)
                textView!!.text = DataCleanManager.getCacheSize(this)
            } else {
                DataCleanManager.clearLocalSave(this)
                textView!!.text = DataCleanManager.getLocalSaveSize(this)
            }
        }
    }

    /**
     * 删除对话框
     * @param message 标题
     * @param content 内容
     */
    private fun logout(message: String, content: String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_delete, null)
        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()
        view.tvMessage.text = message
        view.tvContent.text = content
        view.tvContent.visibility = View.VISIBLE
        view.llNegative.setOnClickListener { dialog.dismiss() }
        view.llPositive.setOnClickListener {
            dialog.dismiss()
            MyApplication.clearUserInfo(this)
            MyApplication.destoryActivity()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * 温馨提示对话框
     */
    private fun promptDialog() {
        if (!TextUtils.equals(CONST.publicUser, MyApplication.USERNAME)) {
            return
        }

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_prompt, null)
        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()
        view.tvProtocal.setOnClickListener {
            val intent = Intent(this, WebviewActivity::class.java)
            intent.putExtra(CONST.ACTIVITY_NAME, "用户协议")
            intent.putExtra(CONST.WEB_URL, "http://xinjiangdecision.tianqi.cn:81/Public/share/xj_htmls/yhxy.html")
            startActivity(intent)
        }
        view.tvPolicy.setOnClickListener {
            val intent = Intent(this, WebviewActivity::class.java)
            intent.putExtra(CONST.ACTIVITY_NAME, "隐私政策")
            intent.putExtra(CONST.WEB_URL, "http://xinjiangdecision.tianqi.cn:81/Public/share/xj_htmls/yscl.html ")
            startActivity(intent)
        }
        view.tvNegtive.setOnClickListener { dialog.dismiss() }
        view.tvPositive.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    /**
     * 二维码对话框
     * @param message 标题
     * @param content 内容
     */
    private fun qrCodeDialog() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_qr_code, null)
        val dialog = Dialog(this, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()
        view.ivClose.setOnClickListener { dialog.dismiss() }
        view.tvSave.setOnClickListener {
            dialog.dismiss()
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_qr_code_android)
            val files = File("${getExternalFilesDir(null)}/XinjiangDecision")
            if (!files.exists()) {
                files.mkdirs()
            }
            val fileName = "${files.absolutePath}/qr_code.jpg"
            val fos = FileOutputStream(fileName)
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
            CommonUtil.notifyAlbum(this, File(fileName))
            Toast.makeText(this, "已保存至相册", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.llBack -> finish()
            R.id.ivPortrait, R.id.tvUserName -> promptDialog()
            R.id.tvEmail -> {
                val intent = Intent(this, FeedbackActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_feedback))
                startActivity(intent)
            }
            R.id.llVersion -> {
                AutoUpdateUtil.checkUpdate(this@SettingActivity, this, "140", getString(R.string.app_name), false)
            }
            R.id.llClearCache -> deleteDialog(true, getString(R.string.delete_cache), getString(R.string.sure_delete_cache), tvCache)
            R.id.llBuild -> {
//                val intentBuild = Intent(this, WebviewActivity::class.java)
//                intentBuild.putExtra(CONST.ACTIVITY_NAME, getString(R.string.setting_build))
//                intentBuild.putExtra(CONST.WEB_URL, CONST.BUILD_URL)
//                startActivity(intentBuild)
            }
            R.id.tvLogout -> logout(getString(R.string.logout), getString(R.string.sure_logout))
            R.id.llCity -> startActivity(Intent(this, ReserveCityActivity::class.java))
            R.id.llProtocal -> {
                val intent = Intent(this, WebviewActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, "用户协议")
                intent.putExtra(CONST.WEB_URL, "http://xinjiangdecision.tianqi.cn:81/Public/share/xj_htmls/yhxy.html")
                startActivity(intent)
            }
            R.id.llPolicy -> {
                val intent = Intent(this, WebviewActivity::class.java)
                intent.putExtra(CONST.ACTIVITY_NAME, "隐私政策")
                intent.putExtra(CONST.WEB_URL, "http://xinjiangdecision.tianqi.cn:81/Public/share/xj_htmls/yscl.html ")
                startActivity(intent)
            }
            R.id.llQrCode -> qrCodeDialog()
        }
    }

}
