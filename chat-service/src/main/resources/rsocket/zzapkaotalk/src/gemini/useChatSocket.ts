// src/hooks/useChatSocket.ts
import { useState, useEffect, useRef } from 'react';
import {
    RSocketClient,
    BufferEncoders,
    JsonSerializer,
    IdentitySerializer,
    encodeCompositeMetadata,
    encodeRoute,
    MESSAGE_RSOCKET_AUTHENTICATION,
    MESSAGE_RSOCKET_ROUTING,
    MESSAGE_RSOCKET_COMPOSITE_METADATA,
} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import type {MessagePayload} from './types';
import type {ReactiveSocket} from "rsocket-types";

// 백엔드 주소와 라우팅 경로
const API_URL = 'ws://localhost:8084/rsocket';
// const MESSAGE_STREAM_ROUTE = 'chat.connect';
const MESSAGE_SEND_ROUTE = 'chat.connect';

export const useChatSocket = (username: string) => {
    const [messages, setMessages] = useState<MessagePayload[]>([]);
    const [connectionStatus, setConnectionStatus] = useState<'connecting' | 'connected' | 'error'>('connecting');
    const rsocketRef = useRef<ReactiveSocket<any, any> | null>(null);

    useEffect(() => {
        // RSocket 클라이언트 설정
        const client = new RSocketWebSocketClient({
            url: API_URL,
            wsCreator: url => new WebSocket(url) as any,
        }, BufferEncoders);

        // RSocket 커넥터 설정
        const connector = new RSocketClient({
            serializers: {
                data: JsonSerializer,
                metadata: IdentitySerializer
            },
            setup: {
                keepAlive: 60000,
                lifetime: 180000,
                dataMimeType: 'application/json',
                metadataMimeType: MESSAGE_RSOCKET_AUTHENTICATION.string,
                payload: {
                    metadata: encodeCompositeMetadata([
                        [MESSAGE_RSOCKET_ROUTING, encodeRoute('chat.connect')]
                    ])
                }
            },
            transport: client,
        });

        connector.connect().subscribe({
            onComplete: (socket) => {
                setConnectionStatus('connected');
                console.log('rsocket connected');
                console.log(socket.connectionStatus())
                rsocketRef.current = socket;

                // socket.requestStream({
                //     metadata: encodeCompositeMetadata([
                //         [MESSAGE_RSOCKET_ROUTING, encodeRoute(`chat.direct.${username}`)]
                //     ])
                // }).subscribe({
                //     onNext: (payload) => {
                //         const newMessage: Omit<MessagePayload, 'isOwnMessage'> = JSON.parse(
                //             payload.data.toString()
                //         );
                //         setMessages(prev => [...prev, {...newMessage, isOwnMessage: newMessage.sender == username}]);
                //     },
                //     onError: (e) => {
                //         console.log(e);
                //     }
                // })
            },
            onError: (e) => {
                console.log(e);
            }
        })
    }, []);

    // 메시지 전송 함수 (fireAndForget)
    const sendMessage = (text: string) => {
        if (!rsocketRef.current || text.trim() === '') return;

        const messageToSend = { sender: username, text };

        rsocketRef.current.fireAndForget({
            data: Buffer.from(JSON.stringify(messageToSend)),
            metadata: Buffer.from(String.fromCharCode(MESSAGE_SEND_ROUTE.length) + MESSAGE_SEND_ROUTE),
        });
    };

    return { messages, sendMessage, connectionStatus };
};