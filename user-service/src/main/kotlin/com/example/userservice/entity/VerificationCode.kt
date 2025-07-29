package com.example.userservice.entity

import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table

@Table("verification_code")
data class VerificationCode @PersistenceCreator constructor(
    val id: Long,
    val verificationCode: String,
    val email: String,
): Persistable<Long> {
    @Transient
    private var _isNew = true

    override fun getId(): Long {
        return id
    }

    override fun isNew(): Boolean {
        return _isNew
    }

    fun load() {
        _isNew = false
    }
}