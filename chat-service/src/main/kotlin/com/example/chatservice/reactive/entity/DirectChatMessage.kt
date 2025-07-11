package com.example.chatservice.reactive.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("direct_chat_message")
data class DirectChatMessage(
    var id: Long,
    var message: String,
    var fromUserId: Long,
    var toUserId: Long,
    var checked: Boolean = false,

    @CreatedDate var createdAt: LocalDateTime,
    @LastModifiedDate var lastModifiedAt: LocalDateTime,
): Persistable<Long> {
    @Transient
    private var _isNew = true

    override fun getId(): Long = id

    override fun isNew() = _isNew

    fun load() {
        _isNew = false
    }
}