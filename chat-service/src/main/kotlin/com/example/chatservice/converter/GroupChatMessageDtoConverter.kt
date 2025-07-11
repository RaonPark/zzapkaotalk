package com.example.chatservice.converter

import com.example.chatservice.dto.GroupChatMessageRequest
import com.example.chatservice.dto.GroupChatMessageResponse
import com.example.chatservice.dto.GetAllChatMessagesResponse
import com.example.chatservice.reactive.entity.GroupChatMessage
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
interface GroupChatMessageDtoConverter {
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
            Mapping(source = "createdTime", target = "createdDate"),
            Mapping(target = "modifiedDate", expression = "java(LocalDateTime.now())"),
        ]
    )
    fun convertRequestToModel(groupChatMessageRequest: GroupChatMessageRequest): GroupChatMessage

    @Mappings(
        value = [
            Mapping(source = "content", target = "content"),
            Mapping(source = "createdDate", target = "createdTime")
        ]
    )
    fun convertModelToGetAllChatMessagesResponse(groupChatMessage: GroupChatMessage): GetAllChatMessagesResponse

    @Mappings(
        value = [
            Mapping(source = "content", target = "content"),
            Mapping(source = "createdDate", target = "createdTime"),
            Mapping(source = "fromUserId", target = "userId"),
            Mapping(source = "chatRoomId", target = "chatRoomId"),
        ]
    )
    fun convertModelToResponse(groupChatMessage: GroupChatMessage): GroupChatMessageResponse
}