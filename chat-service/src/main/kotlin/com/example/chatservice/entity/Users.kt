package com.example.chatservice.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Table
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.domain.Persistable

@Table
@Entity(name = "users")
data class Users @PersistenceCreator constructor(
    @Id
    var userId: Long,

    @Column(nullable = false)
    var nickname: String,

    @Column
    var profileImage: String = "Default_IMG",

): Persistable<Long> {
    private var _isNew = true

    override fun getId(): Long? = userId

    override fun isNew(): Boolean {
        return _isNew
    }

    @PostPersist
    @PostLoad
    protected fun load() {
        _isNew = false
    }
}