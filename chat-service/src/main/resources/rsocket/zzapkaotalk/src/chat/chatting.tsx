// src/App.tsx
import React, { useState, useEffect, useRef } from 'react';
import { RSocketClient, JsonSerializer, IdentitySerializer } from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import './Chatting.css'; // 스타일링 파일

interface ChatMessage {
    fromUserId: number;
    toUserId: number;
    message: string;
    timestamp: string;
}

// RSocket 소켓 인스턴스를 저장하기 위한 타입
// type RSocket = ReturnType<RSocketClient<ChatMessage, string>['connect']>['subscribe'];

function RSocketChat() {
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [fromUserId, setFromUserId] = useState('');
    const [toUserId, setToUserId] = useState('');
    const [messageInput, setMessageInput] = useState('');
    const [isConnected, setIsConnected] = useState(false);

    // RSocket 인스턴스를 컴포넌트의 생명주기 동안 유지하기 위해 useRef 사용
    const rsocketRef = useRef<any>(null);

    useEffect(() => {

        // RSocket 클라이언트 설정
        const client = new RSocketClient({
            serializers: {
                data: JsonSerializer,
                metadata: IdentitySerializer,
            },
            setup: {
                keepAlive: 60000,
                lifetime: 180000,
                dataMimeType: 'application/json',
                metadataMimeType: 'message/x.rsocket.routing.v0',
            },
            transport: new RSocketWebSocketClient({
                url: 'ws://localhost:28079/rsocket', // Spring Boot RSocket 엔드포인트
            }),
        });

        // RSocket 연결
        client.connect().subscribe({
            onComplete: (socket) => {
                rsocketRef.current = socket;
                setIsConnected(true);
                console.log('RSocket connection established.');

                // 채팅 메시지 스트림 구독 (Request-Stream)
                socket
                    .requestStream({
                        metadata: String.fromCharCode('chat.stream'.length) + 'chat.stream',
                    })
                    .subscribe({
                        onNext: (payload) => {
                            // 새로운 메시지가 도착하면 messages 상태에 추가
                            setMessages((prevMessages) => [...prevMessages, payload.data]);
                        },
                        onError: (error) => console.error('Stream error:', error),
                        onSubscribe: (subscription) => {
                            subscription.request(1000); // 받을 메시지 개수 요청
                        },
                    });
            },
            onError: (error) => {
                console.error('Connection error:', error);
                setIsConnected(false);
            },
        });

        // 컴포넌트가 언마운트될 때 RSocket 연결 종료
        return () => {
            if (rsocketRef.current) {
                rsocketRef.current.close();
                console.log('RSocket connection closed.');
            }
        };
    }, []);

    const handleSendMessage = () => {
        if (!rsocketRef.current || !isConnected) {
            alert('RSocket is not connected.');
            return;
        }

        const fromId = parseInt(fromUserId, 10);
        const toId = parseInt(toUserId, 10);

        if (isNaN(fromId) || isNaN(toId) || !messageInput) {
            alert('모든 필드를 올바르게 입력해주세요.');
            return;
        }

        const chatMessage: ChatMessage = {
            fromUserId: fromId,
            toUserId: toId,
            message: messageInput,
            timestamp: new Date().toISOString(),
        };

        // 메시지 전송 (Fire-and-Forget)
        rsocketRef.current.fireAndForget({
            data: chatMessage,
            metadata: String.fromCharCode(`chat.direct.${fromId}`.length) + 'chat.direct.' + fromId,
        });

        setMessageInput(''); // 입력창 초기화
    };

    const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            handleSendMessage();
        }
    };

    return (
        <div className="chat-container">
            <div className="chat-header">
                <h2>RSocket React Chat</h2>
                <div className={`status ${isConnected ? 'connected' : 'disconnected'}`}>
                    {isConnected ? 'Connected' : 'Disconnected'}
                </div>
            </div>
            <div className="chat-messages">
                {messages.map((msg, index) => (
                    <div key={index} className="message">
                        <div className="info">
                            <strong>From:</strong> {msg.fromUserId} | <strong>To:</strong> {msg.toUserId} - <em>{new Date(msg.timestamp).toLocaleTimeString()}</em>
                        </div>
                        <div className="text">{msg.message}</div>
                    </div>
                ))}
            </div>
            <div className="chat-input-area">
                <input
                    type="number"
                    value={fromUserId}
                    onChange={(e) => setFromUserId(e.target.value)}
                    placeholder="보내는 사람 ID"
                />
                <input
                    type="number"
                    value={toUserId}
                    onChange={(e) => setToUserId(e.target.value)}
                    placeholder="받는 사람 ID"
                />
                <input
                    className="message-input"
                    type="text"
                    value={messageInput}
                    onChange={(e) => setMessageInput(e.target.value)}
                    onKeyPress={handleKeyPress}
                    placeholder="메시지를 입력하세요..."
                />
                <button onClick={handleSendMessage}>전송</button>
            </div>
        </div>
    );
}

export default RSocketChat;