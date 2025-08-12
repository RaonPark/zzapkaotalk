// src/components/ChatWindow.tsx
import React from 'react';
import Message from './Message';
import MessageInput from './MessageInput';
import styles from './ChatWindow.module.css';
import { useChatSocket } from './useChatSocket'; // 훅 임포트
import type {DirectMessageResponse} from './types';

const ChatWindow: React.FC = () => {
    // 훅을 사용하여 메시지, 전송 함수, 연결 상태를 가져옴
    const { messages, sendMessage, connectionStatus } = useChatSocket('You'); // 사용자 이름은 'You'로 고정

    const renderStatus = () => {
        switch(connectionStatus) {
            case 'connecting': return 'Connecting to server...';
            case 'error': return 'Connection failed.';
            case 'connected': return 'Chat with Alice'; // 연결 성공 시
        }
    }

    return (
        <div className={styles.chatWindow}>
            <div className={styles.header}>
                {/* 연결 상태 표시 */}
                <h3>{renderStatus()}</h3>
            </div>
            <div className={styles.messageList}>
                {/* 서버에서 받은 메시지 목록을 렌더링 */}
                {messages.map((msg: DirectMessageResponse) => (
                    <Message key={msg.timestamp} text={msg.message} isOwnMessage={false} />
                ))}
            </div>
            {/* sendMessage 함수를 MessageInput에 props로 전달 */}
            <MessageInput onSendMessage={sendMessage} disabled={connectionStatus !== 'connected'} />
        </div>
    );
};

export default ChatWindow;