package com.example.chatservice.callbacks

import com.example.chatservice.reactive.entity.Chatroom
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import org.springframework.data.r2dbc.mapping.OutboundRow
import org.springframework.data.r2dbc.mapping.event.AfterSaveCallback
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.stereotype.Component

@Component
class ChatRoomEntityCallback: AfterSaveCallback<Chatroom> {
    override fun onAfterSave(entity: Chatroom, outboundRow: OutboundRow, table: SqlIdentifier): Publisher<Chatroom> {
        entity.load()

        return mono {
            entity
        }
    }
}