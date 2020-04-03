package com.yzy.baselibrary.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yzy.baselibrary.http.ExceptionHandle
import com.yzy.baselibrary.http.ResponseThrowable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


open class BaseViewModel : ViewModel() {

//    val defUI: BaseUiModel<T> by lazy { BaseUiModel() }

    /**
     * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动
     * 调用ViewModel的  #onCleared 方法取消所有协程
     */
    fun launchUI(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch { block() }

    /**
     * 用流的方式进行网络请求
     */
    fun <T> launchFlow(block: suspend () -> T): Flow<T> {
        return flow {
            emit(block())
        }
    }

    /**
     *  不过滤请求结果
     * @param block 请求体
     * @param error 失败回调
     * @param complete  完成回调（无论成功失败都会调用）
     */
    fun launchGo(
        block: suspend CoroutineScope.() -> Unit,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit,
        complete: suspend CoroutineScope.() -> Unit = {}
    ) {
        launchUI {
            handleException(
                withContext(Dispatchers.IO) { block },
                { error(it) },
                {
                    complete()
                }
            )
        }
    }

    /**
     * 过滤请求结果，其他全抛异常
     * @param block 请求体
     * @param success 成功回调
     * @param error 失败回调
     * @param complete  完成回调（无论成功失败都会调用）
     */
    fun <T> launchOnlyresult(
        block: suspend CoroutineScope.() -> IBaseResponse<T>,
        success: (T) -> Unit,
        error: (ResponseThrowable) -> Unit= {},
        complete: () -> Unit = {}
    ) {
        launchUI {
            handleException(
                { withContext(Dispatchers.IO) { block() } },
                { res ->
                    executeResponse(res) { success(it) }
                },
                {
                    error(it)
                },
                {
                    complete()
                }
            )
        }
    }

    /**
     * 请求结果过滤
     */
    private suspend fun <T> executeResponse(
        response: IBaseResponse<T>,
        success: suspend CoroutineScope.(T) -> Unit
    ) {
        coroutineScope {
            if (response.isSuccess()) success(response.data())
            else throw ResponseThrowable(response.code(), response.msg())
        }
    }



    fun <T> launchOnlyresult1(
        block: suspend CoroutineScope.() -> IBaseResponse1<T>,
        success: (T) -> Unit,
        complete: () -> Unit = {}) {
        launchUI {
            handleException1({
                withContext(Dispatchers.IO) { block() } },
                { res ->
                    executeResponse1(res) { success(it) }
                },
                {
                    complete()
                }
            )
        }
    }

    /**
     * 请求结果过滤
     */
    private suspend fun <T> executeResponse1(
        response: IBaseResponse1<T>,
        success: suspend CoroutineScope.(T) -> Unit
    ) {
        coroutineScope {
            if (response.data() !=null){
                success(response.data())
            }else{

            }


        }
    }

    private suspend fun <T> handleException1(
        block: suspend CoroutineScope.() -> IBaseResponse1<T>,
        success: suspend CoroutineScope.(IBaseResponse1<T>) -> Unit,
        complete: suspend CoroutineScope.() -> Unit
    ) {
        coroutineScope {
            try {
                success(block())
            } catch (e: Throwable) {
                error(e)
            } finally {
                complete()
            }
        }
    }
    /**
     * 异常统一处理
     */
    private suspend fun <T> handleException(
        block: suspend CoroutineScope.() -> IBaseResponse<T>,
        success: suspend CoroutineScope.(IBaseResponse<T>) -> Unit,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit,
        complete: suspend CoroutineScope.() -> Unit
    ) {
        coroutineScope {
            try {
                success(block())
            } catch (e: Throwable) {
                error(ExceptionHandle.handleException(e))
            } finally {
                complete()
            }
        }
    }


    /**
     * 异常统一处理
     */
    private suspend fun handleException(
        block: suspend CoroutineScope.() -> Unit,
        error: suspend CoroutineScope.(ResponseThrowable) -> Unit,
        complete: suspend CoroutineScope.() -> Unit
    ) {
        coroutineScope {
            try {
                block()
            } catch (e: Throwable) {
                error(ExceptionHandle.handleException(e))
            } finally {
                complete()
            }
        }
    }


    /**
     * UI事件
     */
//    inner class UIChange {
//        val showDialog by lazy { SingleLiveEvent<String>() }
//        val dismissDialog by lazy { SingleLiveEvent<Void>() }
//        val toastEvent by lazy { SingleLiveEvent<String>() }
//        val msgEvent by lazy { SingleLiveEvent<Message>() }
//    }

    open class BaseUiModel<T>(
        var showEnd: Boolean = false, // 加载更多
        var success: T? = null, // 加载更多
        var showLoading: Boolean = false,
        var isRefresh: Boolean = false // 刷新

    )
//    {
//        fun {}
//        fun success(bean: T?) {}
//        fun error() {}
//
//    }
}