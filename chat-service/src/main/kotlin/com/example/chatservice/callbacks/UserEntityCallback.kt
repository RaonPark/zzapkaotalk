package com.example.chatservice.callbacks

import com.example.chatservice.reactive.entity.User
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import org.springframework.data.r2dbc.mapping.OutboundRow
import org.springframework.data.r2dbc.mapping.event.AfterSaveCallback
import org.springframework.data.relational.core.sql.SqlIdentifier

class UserEntityCallback: AfterSaveCallback<User> {
    override fun onAfterSave(entity: User, outboundRow: OutboundRow, table: SqlIdentifier): Publisher<User> {
        entity.load()

        return mono {
            entity
        }
    }
}