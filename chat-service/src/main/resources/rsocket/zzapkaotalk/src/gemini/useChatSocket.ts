// src/hooks/useChatSocket.ts
import {useState, useEffect, useRef, useReducer} from 'react';
import {
    RSocketClient,
    BufferEncoders,
    IdentitySerializer,
    encodeCompositeMetadata,
    encodeRoute,
    MESSAGE_RSOCKET_AUTHENTICATION,
    MESSAGE_RSOCKET_ROUTING,
    MESSAGE_RSOCKET_COMPOSITE_METADATA, encodeBearerAuthMetadata,
} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import type {
    DirectMessageResponse, DirectMessageRequest, DirectMessagePayload, DirectChatStreamRequest,
    DirectChatSendResponse
} from './types';
import type {ReactiveSocket} from "rsocket-types";
import axios from "axios";
import {Buffer} from "buffer";

// 백엔드 주소와 라우팅 경로
const API_URL = 'ws://localhost:8084/rsocket';
const MESSAGE_SEND_ROUTE = 'chat.direct.send';

type ConnectionStatus = 'connecting' | 'connected' | 'disconnected' | 'buddyConnected' | 'error';

type Action =
    | { type: 'CONNECT' }
    | { type: 'DISCONNECT' }
    | { type: 'ERROR' }
    | { type: 'CONNECTED' }
    | { type: 'BUDDY_CONNECTED' }

function reducer(state: ConnectionStatus, action: Action): ConnectionStatus {
    switch (action.type) {
        case 'CONNECT':
            return 'connecting';
        case 'DISCONNECT':
            return 'disconnected';
        case 'ERROR':
            return 'error';
        case 'CONNECTED':
            return 'connected';
        case 'BUDDY_CONNECTED':
            return 'buddyConnected';
        default:
            return state;
    }
}

export const useChatSocket = (username: string, buddyEmail?: string) => {
    const [messages, setMessages] = useState<DirectMessagePayload[]>([]);
    const [connectionStatus, setConnectionStatus] = useReducer(reducer, 'connecting')
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
                setConnectionStatus({ type: 'CONNECTED' });
                console.log('rsocket connected');
                rsocketRef.current = socket;

                socket.requestStream({
                    data: Buffer.from(buddyEmail!),
                    metadata: encodeCompositeMetadata([
                        [MESSAGE_RSOCKET_ROUTING, encodeRoute('chat.direct.previous')],
                        [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(jwt)]
                    ])
                }).subscribe({
                    onNext: (payload) => {
                        setConnectionStatus({ type: 'BUDDY_CONNECTED' });
                        console.log(payload.data?.toString('utf-8'));
                        const newMessage: Omit<DirectMessageResponse, 'isOwnMessage'> = JSON.parse(
                            payload.data?.toString('utf-8')!!
                        )

                        console.log("new Message : " + newMessage.fromUserEmail + `${username}`);
                        setMessages(prev => [...prev, {...newMessage, isOwnMessage: newMessage.fromUserEmail == username}]);
                    },
                    onError: (e) => {
                        if(e.message.includes("expired")) {
                            checkLoginStatus();
                            console.log('jwt expired');
                            window.location.href = 'http://localhost:8084/login'
                        }
                    },
                    onSubscribe: (subscription) => {
                        subscription.request(1000);
                    }
                });

                const streamRequest: DirectChatStreamRequest = {
                    fromUserEmail: username,
                    toUserEmail: buddyEmail!,
                }

                socket.requestStream({
                    data: Buffer.from(JSON.stringify(streamRequest)),
                    metadata: encodeCompositeMetadata([
                        [MESSAGE_RSOCKET_ROUTING, encodeRoute('chat.direct.stream')],
                        [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(jwt)]
                    ])
                }).subscribe({
                    onNext: (payload) => {
                        console.log("request stream: " + payload.data);
                        const newMessage: Omit<DirectMessageResponse, 'isOwnMessage'> = JSON.parse(
                            payload.data?.toString('utf-8')!!
                        )
                        setMessages(prev => [...prev, {...newMessage, isOwnMessage: newMessage.fromUserEmail == username}]);
                    }
                })
            },
            onError: (e) => {
                console.log(e);
            }
        })
    }, [jwt, buddyEmail]);

    const sendMessage = (text: string) => {
        if (!rsocketRef.current || text.trim() === '' || buddyEmail === '' || buddyEmail === undefined) return;

        const messageToSend: DirectMessageRequest = {
            fromUserEmail: username,
            toUserEmail: buddyEmail,
            message: text,
            timestamp: new Date().toISOString(),
        }

        console.log(messageToSend)

        rsocketRef.current.requestResponse({
            data: Buffer.from(JSON.stringify(messageToSend)),
            metadata: encodeCompositeMetadata([
                [MESSAGE_RSOCKET_AUTHENTICATION, encodeBearerAuthMetadata(jwt)],
                [MESSAGE_RSOCKET_ROUTING, encodeRoute(MESSAGE_SEND_ROUTE)]
            ])
        }).subscribe({
            onComplete: (payload) => {
                const response = JSON.parse(payload.data?.toString('utf-8')) as DirectChatSendResponse;

                if(response.status == 'OK') {
                    const newMessage: DirectMessagePayload = {
                        fromUserEmail: username,
                        toUserEmail: buddyEmail,
                        message: text,
                        timestamp: new Date(response.timestamp).toISOString(),
                        isOwnMessage: true,
                    };

                    setMessages(prev => [...prev, newMessage]);
                }
                else {
                    alert("메세지가 전송되지 못했습니다.");
                }

            },
            onError: (e) => {
                console.log(e)
            }
        })
    };

    return { messages, sendMessage, connectionStatus };
};