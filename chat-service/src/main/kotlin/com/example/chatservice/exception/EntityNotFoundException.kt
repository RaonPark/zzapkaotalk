package com.example.chatservice.exception

import jakarta.persistence.EntityNotFoundException

class UserNotFoundException(userId: Long): EntityNotFoundException("userId $userId not found") {}

class ChatRoomNotFoundException(chatRoomId: Long): EntityNotFoundException("chatRoomId $chatRoomId not found") {}