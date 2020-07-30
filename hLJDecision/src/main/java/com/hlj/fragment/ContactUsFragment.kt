package com.hlj.fragment

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hlj.activity.DataIntroActivity
import com.hlj.common.CONST
import com.hlj.common.MyApplication
import com.hlj.utils.CommonUtil
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.fragment_contact_us.*
import shawn.cxwl.com.hlj.R

/**
 * 联系我们
 * @author shawn_sun
 */
class ContactUsFragment : Fragment() {

    private var mReceiver: MyBroadCastReceiver? = null
    private var desc = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact_us, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDesc()
        initBroadCast()
    }

    private fun setDesc() {
        for (i in 0 until MyApplication.columnDataList.size) {
            val dto1 = MyApplication.columnDataList[i]
            var desc1 = "${(i+1)}.${dto1.name}：${dto1.desc}\n"
            for (j in 0 until dto1.child.size) {
                val dto2 = dto1.child[j]
                val desc2 = "   ${(i+1)}.${(j+1)}.${dto2.name}：${dto2.desc}\n"
                desc1 += desc2
            }
            desc+=desc1
        }
    }

    private fun initBroadCast() {
        mReceiver = MyBroadCastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(arguments!!.getString(CONST.BROADCAST_ACTION))
        activity!!.registerReceiver(mReceiver, intentFilter)
    }

    private inner class MyBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refresh()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mReceiver != null) {
            activity!!.unregisterReceiver(mReceiver)
        }
    }

    private fun refresh() {
        ivData!!.setOnClickListener {
            val intent = Intent(activity, DataIntroActivity::class.java)
            intent.putExtra(CONST.ACTIVITY_NAME, "客户端产品数据说明")
            intent.putExtra("desc", desc)
            startActivity(intent)
        }
//        tvPhone.setOnClickListener {
//            dialPhone("联系电话", "0451-55172953", "拨打");
//        }
        val columnId = arguments!!.getString(CONST.COLUMN_ID)
        val title = arguments!!.getString(CONST.ACTIVITY_NAME)
        CommonUtil.submitClickCount(columnId, title)
    }

    private fun dialPhone(message: String, content: String, positive: String) {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_delete, null)
        val dialog = Dialog(activity, R.style.CustomProgressDialog)
        dialog.setContentView(view)
        dialog.show()
        view.tvPositive.text = positive
        view.tvMessage.text = message
        view.tvContent.text = content
        view.tvContent.visibility = View.VISIBLE
        view.llNegative.setOnClickListener { dialog.dismiss() }
        view.llPositive.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$content")))
        }
    }
	
}
