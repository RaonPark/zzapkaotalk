package com.example.chatservice.entity

import jakarta.persistence.*
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.domain.Persistable
import kotlin.jvm.Transient

@Table
@Entity(name = "chatroom")
data class ChatRoom @PersistenceCreator constructor(
    @Id
    @Column(name = "chatroom_id")
    var id: Long,

    @Column(nullable = false)
    var roomName: String,

    @Column(nullable = false)
    var roomImage: String = "Default_IMG",

    var roomDescription: String = "",
): Persistable<Long> {
    @Transient
    private var _isNew = true

    override fun getId(): Long? {
        return id
    }

    override fun isNew(): Boolean {
        return _isNew
    }

    @PostPersist
    @PostLoad
    protected fun load() {
        _isNew = false
    }
}