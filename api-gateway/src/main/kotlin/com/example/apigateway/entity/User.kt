package com.example.apigateway.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@RedisHash("user")
@Table("users")
@JsonIgnoreProperties(ignoreUnknown = true)
data class User @JsonCreator constructor(
    @Id
    var id: Long,

    var nickname: String,

    var profileImage: String = "Default_IMG",

    var email: String,

    var password: String,

    var isVerified: Boolean = false,

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