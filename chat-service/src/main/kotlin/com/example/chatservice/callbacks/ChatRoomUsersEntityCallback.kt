package com.example.chatservice.callbacks

import com.example.chatservice.reactive.entity.ChatroomUsers
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import org.springframework.data.r2dbc.mapping.OutboundRow
import org.springframework.data.r2dbc.mapping.event.AfterSaveCallback
import org.springframework.data.relational.core.sql.SqlIdentifier


class ChatRoomUsersEntityCallback: AfterSaveCallback<ChatroomUsers> {
    override fun onAfterSave(
        entity: ChatroomUsers,
        outboundRow: OutboundRow,
        table: SqlIdentifier
    ): Publisher<ChatroomUsers> {
        entity.load()

        return mono {
            entity
        }
    }
}