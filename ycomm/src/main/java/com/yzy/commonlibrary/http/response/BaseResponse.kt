package com.yzy.commonlibrary.http.response

import com.google.gson.annotations.SerializedName

/**
 *description: 网络请求返回的基类,接口文档没有不确定.
 *@date 2019/7/15
 *@author: yzy.
 */
data class BaseResponse<out T>(
        @SerializedName("errorCode")
        val code: Int,//正常接口使用的状态码
        @SerializedName("errorMsg")
        val message: String,//code异常对应的信息提示
        val data: T //正常返回的数据信息
)