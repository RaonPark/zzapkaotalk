package com.example.chatservice.exception

open class EntityNotFoundException(message: String) : RuntimeException(message)

class UserNotFoundException(userId: Long): EntityNotFoundException("userId $userId not found") {}

class ChatRoomNotFoundException(chatRoomId: Long): EntityNotFoundException("chatRoomId $chatRoomId not found") {}