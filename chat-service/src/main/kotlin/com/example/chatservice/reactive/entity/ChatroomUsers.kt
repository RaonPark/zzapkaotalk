package com.example.chatservice.reactive.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("chatroom_users")
@JsonIgnoreProperties(ignoreUnknown = true)
data class ChatroomUsers @PersistenceCreator @JsonCreator constructor(
    @Id
    var id: Long,

    var userId: Long,

    var chatroomId: Long,

    var role: String,

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