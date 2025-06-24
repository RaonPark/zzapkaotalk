package com.example.chatservice.converter

import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.entity.ChatMessage
import org.mapstruct.*
import org.mapstruct.MappingConstants.ComponentModel

@Mapper(
    componentModel = ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    injectionStrategy = InjectionStrategy.FIELD,
    uses = [IdMapper::class]
)
interface ChatMessageDtoConverter {
    @Mappings(
        value = [
            Mapping(target = "id", ignore = true),
            Mapping(source = "chatRoomId", target = "chatRoom"),
            Mapping(source = "fromUserId", target = "from"),
            Mapping(target = "checked", ignore = true),
            Mapping(target = "createdDate", ignore = true),
            Mapping(target = "lastModifiedDate", ignore = true),
        ]
    )
    fun convertRequestToModel(chatMessageRequest: ChatMessageRequest): ChatMessage

}