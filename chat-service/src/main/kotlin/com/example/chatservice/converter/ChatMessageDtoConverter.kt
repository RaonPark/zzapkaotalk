package com.example.chatservice.converter

import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.dto.ChatMessageResponse
import com.example.chatservice.dto.GetAllChatMessagesResponse
import com.example.chatservice.reactive.entity.ChatMessage
import org.mapstruct.*
import org.mapstruct.MappingConstants.ComponentModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Mapper(
    componentModel = ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    injectionStrategy = InjectionStrategy.FIELD,
    imports = [LocalDateTime::class, DateTimeFormatter::class]
)
interface ChatMessageDtoConverter {
    companion object {
        @Named("formatDateTime")
        @JvmStatic
        fun formatDateTime(createdDate: LocalDateTime): String {
            return createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))
        }
    }

    @Mappings(
        value = [
            Mapping(target = "id", ignore = true),
            Mapping(source = "chatRoomId", target = "chatRoomId"),
            Mapping(source = "fromUserId", target = "fromUserId"),
            Mapping(target = "checked", ignore = true),
            Mapping(target = "createdDate", expression = "java(LocalDateTime.now())"),
            Mapping(target = "modifiedDate", expression = "java(LocalDateTime.now())"),
        ]
    )
    fun convertRequestToModel(chatMessageRequest: ChatMessageRequest): ChatMessage

    @Mappings(
        value = [
            Mapping(source = "content", target = "content"),
            Mapping(target = "nickname", ignore = true),
            Mapping(target = "profileImage", ignore = true),
            Mapping(source = "createdDate", target = "createdTime", qualifiedByName = ["formatDateTime"])
        ]
    )
    fun convertModelToGetAllChatMessagesResponse(chatMessage: ChatMessage): GetAllChatMessagesResponse

    @Mappings(
        value = [
            Mapping(source = "content", target = "content"),
            Mapping(target = "nickname", ignore = true),
            Mapping(source = "createdDate", target = "createdTime", qualifiedByName = ["formatDateTime"]),
            Mapping(source = "fromUserId", target = "userId"),
            Mapping(source = "chatRoomId", target = "chatRoomId"),
            Mapping(target = "profileImage", ignore = true),
        ]
    )
    fun convertModelToResponse(chatMessage: ChatMessage): ChatMessageResponse
}