package com.example.chatservice.callbacks

import com.example.chatservice.reactive.entity.ChatMessage
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import org.springframework.data.r2dbc.mapping.OutboundRow
import org.springframework.data.r2dbc.mapping.event.AfterSaveCallback
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.stereotype.Component

@Component
class ChatMessageEntityCallback: AfterSaveCallback<ChatMessage> {
    override fun onAfterSave(
        entity: ChatMessage,
        outboundRow: OutboundRow,
        table: SqlIdentifier
    ): Publisher<ChatMessage> {
        entity.load()

        return mono {
            entity
        }
    }
}