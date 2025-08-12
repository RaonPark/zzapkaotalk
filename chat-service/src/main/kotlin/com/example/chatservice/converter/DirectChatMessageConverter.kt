package com.example.chatservice.converter

import com.example.chatservice.dto.DirectChatMessageResponse
import com.example.chatservice.reactive.entity.DirectChatMessage
import org.mapstruct.InjectionStrategy
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    injectionStrategy = InjectionStrategy.FIELD,
)
interface DirectChatMessageConverter {
    @Mappings(
        Mapping(target = "createdTime", source = "createdAt")
    )
    fun modelToResponse(directChatMessage: DirectChatMessage): DirectChatMessageResponse
}