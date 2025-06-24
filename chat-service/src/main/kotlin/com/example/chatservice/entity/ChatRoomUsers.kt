package com.example.chatservice.entity

import jakarta.persistence.*
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.domain.Persistable
import kotlin.jvm.Transient

@Entity
@Table(name = "chatroom_users")
data class ChatRoomUsers @PersistenceCreator constructor(
    @Id
    var id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: Users,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    var chatRoom: ChatRoom,

    @Column(nullable = false)
    var role: String,
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