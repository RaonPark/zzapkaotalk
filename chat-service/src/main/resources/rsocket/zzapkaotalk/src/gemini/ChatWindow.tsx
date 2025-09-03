// src/components/ChatWindow.tsx
import React, {useEffect, useRef} from 'react';
import Message from './Message';
import MessageInput from './MessageInput';
import styles from './ChatWindow.module.css';
import { useChatSocket } from './useChatSocket'; // 훅 임포트
import type {ChatUserResponse, DirectMessagePayload, Me} from './types';

interface ChatWindowProps {
    me: Me;
    buddy: ChatUserResponse | null;
}

const ChatWindow: React.FC<ChatWindowProps> = ({ me, buddy }) => {
    // 훅을 사용하여 메시지, 전송 함수, 연결 상태를 가져옴
    const { messages, sendMessage, connectionStatus } = useChatSocket(me.email, buddy?.userEmail);
    const messageListRef = useRef<HTMLDivElement>(null);

    const renderStatus = () => {
        switch(connectionStatus) {
            case 'connecting': return 'Connecting to server...';
            case 'error': return 'Connection failed.';
            case 'connected': return 'Connected to server.';
            case 'buddyConnected': return `Chat with ${buddy?.nickname}`; // 연결 성공 시
        }
    }

    useEffect(() => {
        if(messageListRef.current) {
            messageListRef.current.scrollTop = messageListRef.current.scrollHeight;
        }
    }, [messages]);

    return (
        <div className={styles.chatWindow}>
            <div className={styles.header}>
                {/* 연결 상태 표시 */}
                <h3>{renderStatus()}</h3>
            </div>
            <div className={styles.messageList} ref={messageListRef}>
                {/* 서버에서 받은 메시지 목록을 렌더링 */}
                {messages.map((msg: DirectMessagePayload) => (
                    <Message key={msg.timestamp} text={msg.message} isOwnMessage={msg.isOwnMessage} />
                ))}
            </div>
            {/* sendMessage 함수를 MessageInput에 props로 전달 */}
            <MessageInput onSendMessage={sendMessage} disabled={connectionStatus !== 'buddyConnected'} />
        </div>
    );
};

export default ChatWindow;