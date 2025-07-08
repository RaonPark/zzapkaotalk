package com.example.chatservice.callbacks

import com.example.chatservice.reactive.entity.GroupChatMessage
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import org.springframework.data.r2dbc.mapping.OutboundRow
import org.springframework.data.r2dbc.mapping.event.AfterSaveCallback
import org.springframework.data.relational.core.sql.SqlIdentifier
import org.springframework.stereotype.Component

@Component
class GroupChatMessageEntityCallback: AfterSaveCallback<GroupChatMessage> {
    override fun onAfterSave(
        entity: GroupChatMessage,
        outboundRow: OutboundRow,
        table: SqlIdentifier
    ): Publisher<GroupChatMessage> {
        entity.load()

        return mono {
            entity
        }
    }
}