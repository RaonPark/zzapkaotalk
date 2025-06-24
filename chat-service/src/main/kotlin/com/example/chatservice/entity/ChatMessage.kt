package com.example.chatservice.entity

import com.example.chatservice.supports.BaseTimeEntity
import jakarta.persistence.*
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.domain.Persistable
import kotlin.jvm.Transient

@Table(
    indexes = [
        Index(name = "IndexedByCreatedDate", columnList = "createdDate", unique = true),
    ]
)
@Entity(name = "chatMessage")
data class ChatMessage @PersistenceCreator constructor (
    @Id
    @Column(name = "chatMessage_id")
    var id: Long,

    @Column(nullable = false)
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var from: Users,

    @ManyToOne(fetch = FetchType.LAZY)
    var chatRoom: ChatRoom,

    var checked: Int
): Persistable<Long>, BaseTimeEntity() {
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