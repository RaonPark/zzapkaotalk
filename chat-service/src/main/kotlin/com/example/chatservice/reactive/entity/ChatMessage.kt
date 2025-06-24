package com.example.chatservice.reactive.entity

import jakarta.persistence.Id
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.domain.Persistable
import java.time.LocalDateTime

data class ChatMessage @PersistenceCreator constructor(
    @Id
    var id: Long,

    var content: String,

    var fromUserId: Long,

    var chatRoomId: Long,

    var checked: Int,

    @CreatedDate
    var createdDate: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    var modifiedDate: LocalDateTime = LocalDateTime.now()
): Persistable<Long> {
    @Transient
    private var _isNew = true

    override fun getId() = id

    override fun isNew() = _isNew

    @PostLoad
    @PostPersist
    protected fun load() {
        _isNew = false
    }
}