package com.example.chatservice.callbacks

import com.example.chatservice.reactive.entity.DirectChatMessage
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import org.springframework.data.r2dbc.mapping.OutboundRow
import org.springframework.data.r2dbc.mapping.event.AfterSaveCallback
import org.springframework.data.relational.core.sql.SqlIdentifier

    class DirectChatMessageEntityCallback: AfterSaveCallback<DirectChatMessage> {
        override fun onAfterSave(
            entity: DirectChatMessage,
            outboundRow: OutboundRow,
            table: SqlIdentifier
        ): Publisher<DirectChatMessage> {
            entity.load()

            return mono {
                entity
            }
        }
    }