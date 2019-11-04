package com.yzy.baselibrary.base

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.MvRxViewId
import com.airbnb.mvrx.MvRxViewModelStore
import com.yzy.baselibrary.extention.dp2px
import com.yzy.baselibrary.extention.isDoubleClick

/**
 *description: Dialog的基类.
 *@date 2019/7/15
 *@author: yzy.
 */
abstract class BaseFragmentDialog : DialogFragment(), MvRxView {

    var mWidth = WRAP_CONTENT
    var mHeight = WRAP_CONTENT
    var mGravity = Gravity.CENTER
    var mOffsetX = 0
    var mOffsetY = 0
    var mAnimation: Int? = null
    var touchOutside: Boolean = true
    var mSoftInputMode: Int = SOFT_INPUT_STATE_ALWAYS_HIDDEN
    var lowerBackground = false // 是否降级背景，例如图片预览的时候不可以降级（设置Activity的透明度）
    lateinit var mContext: Context
    lateinit var mActivity: Activity

    /****** listener ******/
    private var viewLoadedListener: ((View) -> Unit)? = null
    private var showListener: (() -> Unit)? = null
    private var disListener: (() -> Unit)? = null
    override val mvrxViewModelStore by lazy { MvRxViewModelStore(viewModelStore) }

    private val mvrxViewIdProperty = MvRxViewId()
    final override val mvrxViewId: String by mvrxViewIdProperty

    override fun onCreate(savedInstanceState: Bundle?) {
        mvrxViewModelStore.restoreViewModels(this, savedInstanceState)
        mvrxViewIdProperty.restoreFrom(savedInstanceState)
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as Activity
    }

    private var contentView: View? = null
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setStyle()
        if (contentView == null) {
            val view = inflater.inflate(contentLayout(), container, false)
            contentView = view
            viewLoadedListener?.invoke(view)
        } else {
            contentView?.parent?.let { parent -> ((parent as ViewGroup).removeView(contentView)) }
        }
        initBeforeCreateView(savedInstanceState)
        return contentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val c = context
        if (lowerBackground && c != null) setBackgroundAlpha(c, 0.3F)
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    override fun onDestroyView() {
        val c = context
        if (lowerBackground && c != null) setBackgroundAlpha(c, 1F)
        super.onDestroyView()
    }

    // 黑暗 0.0F ~ 1.0F 透明
    protected open fun setBackgroundAlpha(
            context: Context,
            alpha: Float
    ) {
        val act = context as? Activity ?: return
        val attributes = act.window.attributes
        attributes.alpha = alpha
        act.window.attributes = attributes
    }


    //防止快速弹出多个
    private var showTime = 0L

    /**
     * 防止同时弹出两个dialog
     */
    override fun show(manager: FragmentManager, tag: String?) {
        if (System.currentTimeMillis() - showTime < 500 || activity?.isFinishing == true) {
            return
        }
        showListener?.invoke()
        setBooleanField("mDismissed", false)
        setBooleanField("mShownByMe", true)
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    private fun setBooleanField(fieldName: String, value: Boolean) {
        try {
            val field = DialogFragment::class.java.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(this, value)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        disListener?.invoke()
        super.onDismiss(dialog)
    }

    fun onShow(listener: () -> Unit) {
        showListener = listener
    }

    fun onDismiss(listener: () -> Unit) {
        disListener = listener
    }

    /**
     * 布局加载完成监听事件
     * 用于 获取布局中的 view
     */
    fun onViewLoaded(listener: (View) -> Unit) {
        viewLoadedListener = listener
    }

    /**
     * 设置统一样式
     */
    private fun setStyle() {
        //获取Window
        val window = dialog?.window
        //无标题
        dialog?.requestWindowFeature(STYLE_NO_TITLE)
        // 透明背景
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (lowerBackground) window?.setDimAmount(0F) else window?.setDimAmount(0.5F)// 去除 dialog 弹出的阴影
        dialog?.setCanceledOnTouchOutside(touchOutside)
        //设置宽高
        window!!.decorView.setPadding(0, 0, 0, 0)
        val wlp = window.attributes
        wlp.width = mWidth
        wlp.height = mHeight
        //设置对齐方式
        wlp.gravity = mGravity
        //设置偏移量
        wlp.x = dialog?.context?.dp2px(mOffsetX) ?: 0
        wlp.y = dialog?.context?.dp2px(mOffsetY) ?: 0
        wlp.softInputMode = mSoftInputMode
        //设置动画
        mAnimation?.also { window.setWindowAnimations(it) }
        window.attributes = wlp
    }

    override val subscriptionLifecycleOwner: LifecycleOwner
        get() = this.viewLifecycleOwnerLiveData.value ?: this

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mvrxViewModelStore.saveViewModels(outState)
        mvrxViewIdProperty.saveTo(outState)
    }

    override fun onStart() {
        super.onStart()
        postInvalidate()
    }

    override fun invalidate() {
    }

    //XML布局
    protected abstract fun contentLayout(): Int

    /**
     * 需要在onCreateView中调用的方法
     */
    protected open fun initBeforeCreateView(savedInstanceState: Bundle?) {}

    protected abstract fun initView(view: View)
}