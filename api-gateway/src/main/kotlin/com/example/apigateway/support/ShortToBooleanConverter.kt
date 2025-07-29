package com.example.apigateway.support

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.stereotype.Component

@Component
@ReadingConverter
class ShortToBooleanConverter: Converter<Short, Boolean> {
    override fun convert(source: Short): Boolean {
        return source != 0.toShort()
    }
}