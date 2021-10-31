package com.hlj.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.hlj.common.CONST
import com.hlj.utils.CommonUtil

open class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val columnId = arguments!!.getString(CONST.COLUMN_ID)
        val title = arguments!!.getString(CONST.ACTIVITY_NAME)
        CommonUtil.submitClickCount(columnId, title)
    }

}
