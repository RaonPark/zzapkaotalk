package com.example.chatservice.reactive.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.*
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("chat_message")
@JsonIgnoreProperties(ignoreUnknown = true)
data class GroupChatMessage @PersistenceCreator @JsonCreator constructor(
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

    fun load() {
        _isNew = false
    }
}