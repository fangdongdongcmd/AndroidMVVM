package com.example.android.study.commonres.ext

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ColorUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.android.study.commonres.R
import com.example.android.study.commonres.loadsir.EmptyCallback
import com.example.android.study.commonres.loadsir.ErrorCallback
import com.example.android.study.commonres.loadsir.LoadingCallback
import com.example.android.study.commonres.utils.onClick
import com.example.android.study.commonres.utils.setEndDrawableImage
import com.example.android.study.commonres.utils.setEndDrawableImageAndText
import com.example.android.study.commonres.utils.setEndText
import com.example.android.study.commonres.widget.dialog.LoadingViewDialog
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.tencent.mmkv.MMKV
import com.xiaomi.push.it
import me.hgj.jetpackmvvm.base.appContext
import me.hgj.jetpackmvvm.demo.app.weight.viewpager.ScaleTransitionPagerTitleView
import me.hgj.jetpackmvvm.ext.util.toHtml
import me.hgj.jetpackmvvm.ext.view.visibleOrGone
import me.hgj.jetpackmvvm.network.AppException
import me.hgj.jetpackmvvm.state.ResultState
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import java.text.SimpleDateFormat


/**
 * ????????????
 */
val YYYY_MM_DD = SimpleDateFormat("yyyy-MM-dd")

val YYYY_MM_DD_HH_MM = SimpleDateFormat("yyyy-MM-dd HH:mm")

val YYYY_MM_DD_HH_MM_SS = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

val MM_DD = SimpleDateFormat("MM???dd???")

val HH_MM = SimpleDateFormat("HH:mm")

val yyyyMMdd = SimpleDateFormat("yyyyMMdd")

val yyyy_MM = SimpleDateFormat("yyyy-MM")

val yyyy_X_MM_X = SimpleDateFormat("yyyy???MM????????????")


//loading???
private var loadingDialog: BasePopupView? = null


/**
 * ????????????????????????????????????????????????????????????????????????????????????????????????????????????
 * @param resultState ???????????????
 * @param onLoading ?????????
 * @param onSuccess ????????????
 * @param onError ????????????
 *
 */
fun <T> Context.parseContextState(
        resultState: ResultState<T>,
        onSuccess: (T) -> Unit,
        onError: ((AppException) -> Unit)? = null,
        onLoading: (() -> Unit)? = null
) {
    when (resultState) {
        is ResultState.Loading -> {
            this.showLoadingExt(resultState.loadingMessage)
            onLoading?.invoke()
        }
        is ResultState.Success -> {
            this.dismissLoadingExt()
            onSuccess(resultState.data)
        }
        is ResultState.Error -> {
            this.dismissLoadingExt()

            onError?.run { this(resultState.error) }
        }
    }
}


/**
 * ???????????????
 */
fun Context?.showLoadingExt(message: String? = null) {
    this?.let {
        if (loadingDialog == null) {
            loadingDialog = LoadingViewDialog(it)
            loadingDialog?.config(dismissOnTouchOutside = false, hasShadowBg = false)?.show()
        }
        loadingDialog?.show()
    }
}

/**
 * ???????????????
 */
fun Context?.dismissLoadingExt() {
    this?.let {
        loadingDialog?.dismiss()
        loadingDialog = null
    }
}


val mmkv: MMKV by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    appContext
    MMKV.defaultMMKV()
}


/**
 * ?????????
 * XpopVIew????????????
 *
 * return BasePopupView
 * @author {Wang Peng} by 12/25/20
 */
fun BasePopupView.config(
        dismissOnTouchOutside: Boolean = false,
        dismissOnBackPressed: Boolean = true,
        moveUpToKeyboard: Boolean = false,
        hasShadowBg: Boolean = true,
        isDestroyOnDismiss: Boolean = false,
        isDrag: Boolean = false,
        atView: View? = null,
): BasePopupView {
    val builder = XPopup.Builder(context)
    builder.dismissOnTouchOutside(dismissOnTouchOutside)
    builder.dismissOnBackPressed(dismissOnBackPressed)
    builder.hasShadowBg(hasShadowBg)
    builder.enableDrag(isDrag)
    builder.moveUpToKeyboard(moveUpToKeyboard)
    builder.isDestroyOnDismiss(isDestroyOnDismiss)
    atView?.let {
        builder.atView(it)
    }
    return builder.asCustom(this)
}


/**
 * ??????????????????
 * @param message ?????????????????????????????????
 */
fun LoadService<*>.showError(message: String = "??????????????????????????????", image: Int = R.drawable.ic_no_network) {
    this.setErrorText(message, image)
    this.showCallback(ErrorCallback::class.java)
}


private fun LoadService<*>.setErrorText(message: String, image: Int) {
    if (message.isNotEmpty()) {
        this.setCallBack(ErrorCallback::class.java) { _, view ->
            view.findViewById<TextView>(R.id.error_text).text = message
            view.findViewById<ImageView>(R.id.error_image).setImageResource(image)
        }
    }
}


/**
 * ???????????????
 */
fun LoadService<*>.showEmpty(message: String = "????????????", image: Int = R.drawable.empty_lost) {
    this.setEmptyText(message, image)
    this.showCallback(EmptyCallback::class.java)
}

/**
 *
 */
fun <T> LoadService<*>.showSuccess(list: ArrayList<T>?, onSuccess: ((ArrayList<T>) -> Unit)? = null, onEmpty: (() -> Unit)? = null) {
    this.showSuccess()
    if (list.isNullOrEmpty()) {
        onSuccess?.invoke(list!!)
    } else {
        if (onEmpty != null) {
            onEmpty.invoke()
        } else {
            this.showEmpty()
        }
    }
}

private fun LoadService<*>.setEmptyText(message: String, image: Int) {
    if (message.isNotEmpty()) {
        this.setCallBack(EmptyCallback::class.java) { _, view ->
            view.findViewById<TextView>(R.id.empty_message).text = message
            view.findViewById<ImageView>(R.id.empty_image).setImageResource(image)
        }
    }
}

/**
 * ???????????????
 */
fun LoadService<*>.showLoading() {
    this.showCallback(LoadingCallback::class.java)
}

fun loadServiceInit(view: View, callback: () -> Unit): LoadService<Any> {
    val service = LoadSir.getDefault().register(view) {
        it.findViewById<TextView>(R.id.tv_retry)?.onClick {
            //??????????????????????????????
            callback.invoke()
        }
    }
    service.showSuccess()
    return service
}

//???????????????Recyclerview
fun RecyclerView.init(
        layoutManger: RecyclerView.LayoutManager,
        bindAdapter: BaseQuickAdapter<*, *>,
        fixedSize: Boolean = true,
        isScroll: Boolean = true
): RecyclerView {
    layoutManager = layoutManger
    setHasFixedSize(fixedSize)
    adapter = bindAdapter
    isNestedScrollingEnabled = isScroll
    return this
}



//??????????????????????????????
fun BaseQuickAdapter<*, *>.setAdapterAnimation(mode: Int) {
    //??????0????????????????????? ????????????
    if (mode == 0) {
        this.animationEnable = false
    } else {
        this.animationEnable = true
        this.setAnimationWithDefault(BaseQuickAdapter.AnimationType.values()[mode - 1])
    }
}

fun MagicIndicator.bindViewPager2(
        viewPager: ViewPager2,
        mStringList: List<String> = arrayListOf(),
        action: (index: Int) -> Unit = {}) {
    val commonNavigator = CommonNavigator(appContext)
    commonNavigator.adapter = object : CommonNavigatorAdapter() {

        override fun getCount(): Int {
            return mStringList.size
        }

        override fun getTitleView(context: Context, index: Int): IPagerTitleView {
            return ScaleTransitionPagerTitleView(appContext).apply {
                //????????????
                text = mStringList[index].toHtml()
                //????????????
                textSize = 17f
                //???????????????
                normalColor = Color.WHITE
                //????????????
                selectedColor = Color.WHITE
                //????????????
                setOnClickListener {
                    viewPager.currentItem = index
                    action.invoke(index)
                }
            }
        }

        override fun getIndicator(context: Context): IPagerIndicator {
            return LinePagerIndicator(context).apply {
                mode = LinePagerIndicator.MODE_EXACTLY
                //??????????????????
                lineHeight = UIUtil.dip2px(appContext, 3.0).toFloat()
                lineWidth = UIUtil.dip2px(appContext, 30.0).toFloat()
                //???????????????
                roundRadius = UIUtil.dip2px(appContext, 6.0).toFloat()
                startInterpolator = AccelerateInterpolator()
                endInterpolator = DecelerateInterpolator(2.0f)
                //???????????????
                setColors(Color.WHITE)
            }
        }
    }
    this.navigator = commonNavigator

    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            this@bindViewPager2.onPageSelected(position)
            action.invoke(position)
        }

        override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            this@bindViewPager2.onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            this@bindViewPager2.onPageScrollStateChanged(state)
        }
    })
}

fun ViewPager2.init(
        fragment: Fragment,
        fragments: ArrayList<Fragment>,
        isUserInputEnabled: Boolean = true
): ViewPager2 {
    //???????????????
    this.isUserInputEnabled = isUserInputEnabled
    //???????????????
    adapter = object : FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int) = fragments[position]
        override fun getItemCount() = fragments.size
    }
    return this
}


/**
 * ???????????????
 */
fun hideSoftKeyboard(activity: Activity?) {
    activity?.let { act ->
        val view = act.currentFocus
        view?.let {
            val inputMethodManager =
                    act.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                    view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}


/**
 * ??????????????????
 *
 */
fun <T> loadListData(
        list: ArrayList<T>? = null,
        code: Int,
        msg: String,
        loadService: LoadService<*>,
        onFail: ((Int) -> Unit)? = null,
        onSuccess: ((ArrayList<T>) -> Unit)? = null,
        onRetry: (() -> Unit)? = null,
) {
    loadService.showSuccess()
    if (code == 100000) {
        if (list != null && list.size > 0) {
            onSuccess?.invoke(list)
        } else {
            loadService.showEmpty()
        }

    } else {
        onFail?.invoke(code)
        when (code) {
            303032 -> {
                loadService.showError()
                showMessage(msg)
                onRetry?.invoke()
            }
            303031 -> {
                loadService.showEmpty()
            }
            else -> {
                showMessage(msg)
                loadService.showError()
            }
        }


    }
}
