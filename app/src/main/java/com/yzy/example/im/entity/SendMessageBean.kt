package com.yzy.example.im.entity

import io.rong.imlib.model.Conversation
import io.rong.imlib.model.Message
import io.rong.imlib.model.MessageContent

/**
 *description: 发送消息的Bean.
 *@date 2019/3/11 16:40.
 *@author: yzy.
 */
data class SendMessageBean(
        var conversationType: Conversation.ConversationType? = null,
        var targetId: String? = null,
        var content: MessageContent? = null,
        var pushContent: String? = null,
        var pushData: String? = null,
        var messageType: IMMessageType? = null,
        var success: ((message: Message?, type: IMMessageType) -> Unit)? = null,
        var error: ((message: Message?, type: IMMessageType, errorCode: Int?) -> Unit)? = null,
        var attached: ((message: Message?, type: IMMessageType) -> Unit)? = null,
        var canceled: ((message: Message?, type: IMMessageType) -> Unit)? = null,
        var progress: ((message: Message?, type: IMMessageType, progress: Int) -> Unit)? = null
)