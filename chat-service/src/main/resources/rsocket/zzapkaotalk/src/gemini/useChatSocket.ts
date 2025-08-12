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
    MESSAGE_RSOCKET_COMPOSITE_METADATA, encodeBearerAuthMetadata,
} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import type {DirectMessageResponse, DirectMessageRequest} from './types';
import type {ReactiveSocket} from "rsocket-types";
import axios from "axios";
import {Buffer} from "buffer";

// 백엔드 주소와 라우팅 경로
const API_URL = 'ws://localhost:8084/rsocket';
const MESSAGE_STREAM_ROUTE = 'chat.stream.direct';
const MESSAGE_SEND_ROUTE = 'chat.direct.send';

export const useChatSocket = (username: string) => {
    const [messages, setMessages] = useState<DirectMessageResponse[]>([]);
    const [connectionStatus, setConnectionStatus] = useState<'connecting' | 'connected' | 'error'>('connecting');
    const rsocketRef = useRef<ReactiveSocket<any, any> | null>(null);
    const [jwt, setJwt] = useState<string>('');

    const checkLoginStatus = async () => {
        try {
            const response = await axios.get('/checkLogin', {
                withCredentials: true, // 세션 쿠키를 보내기 위해 필수!
            });
            // 성공 시, 받아온 사용자 세션 정보를 state에 저장
            setJwt(response.data);

        } catch (err) {
            if (axios.isAxiosError(err) && err.response?.status === 401) {
                console.log('User is not logged in.');
                window.location.href = 'http://localhost:8084/login'
            } else {
                console.error("err" + err);
            }
        }
    };

    useEffect(() => {
        checkLoginStatus();
    }, []);

    useEffect(() => {
        if(jwt == '') {
            return;
        }
        console.log(`jwt = ${jwt}`);

        // RSocket 클라이언트 설정
        const client = new RSocketWebSocketClient({
            url: API_URL,
        }, BufferEncoders);

        // RSocket 커넥터 설정
        const connector = new RSocketClient({
            serializers: {
                data: IdentitySerializer,
                metadata: IdentitySerializer
            },
            setup: {
                keepAlive: 60000,
                lifetime: 180000,
                dataMimeType: 'application/json',
                metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
                payload: {
                    metadata: encodeCompositeMetadata([
                        [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(jwt)]
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

                socket.requestStream({
                    metadata: encodeCompositeMetadata([
                        [MESSAGE_RSOCKET_ROUTING, encodeRoute('chat.direct.stream')],
                        [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(jwt)]
                    ])
                }).subscribe({
                    onNext: (payload) => {
                        console.log(payload.data?.toString('utf-8')!!);
                        const newMessage: Omit<DirectMessageResponse, 'isOwnMessage'> = JSON.parse(
                            payload.data?.toString('utf-8')!!
                        )
                        setMessages(prev => [...prev, {...newMessage, isOwnMessage: newMessage.fromUserId == username}]);
                    },
                    onError: (e) => {
                        if(e.message.includes("expired")) {
                            checkLoginStatus();
                            console.log('jwt expired');
                            window.location.href = 'http://localhost:8084/login'
                        }
                    },
                    onSubscribe: (sub) => {
                        sub.request(100);
                    }
                })
            },
            onError: (e) => {
                console.log(e);
            }
        })
    }, [jwt]);

    // 메시지 전송 함수 (fireAndForget)
    const sendMessage = (text: string) => {
        if (!rsocketRef.current || text.trim() === '') return;

        const messageToSend: DirectMessageRequest = {
            fromUserId: 1,
            toUserId: 2,
            message: text,
            timestamp: new Date().toISOString()
        }

        console.log(messageToSend)

        rsocketRef.current.fireAndForget({
            data: Buffer.from(JSON.stringify(messageToSend)),
            metadata: encodeCompositeMetadata([
                [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(jwt)],
                [MESSAGE_RSOCKET_ROUTING, encodeRoute(MESSAGE_SEND_ROUTE)]
            ])
        });
    };

    return { messages, sendMessage, connectionStatus };
};