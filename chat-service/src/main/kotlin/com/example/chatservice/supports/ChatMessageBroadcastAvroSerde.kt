package com.example.chatservice.supports

import org.apache.avro.util.ByteBufferOutputStream
import java.nio.ByteBuffer

class ChatMessageBroadcastAvroSerde {
    private fun convertByteBuffer2ByteArray(outputStream: ByteBufferOutputStream): ByteArray {
        val totalSize: Int = outputStream.bufferList.stream().mapToInt(ByteBuffer::remaining).sum()
        val byteArray = ByteArray(totalSize)

        var offset = 0
        for (buffer in outputStream.bufferList) {
            val bufferSize: Int = buffer.remaining()
            buffer.get(byteArray, offset, bufferSize)
            offset += bufferSize
        }

        return byteArray
    }
}