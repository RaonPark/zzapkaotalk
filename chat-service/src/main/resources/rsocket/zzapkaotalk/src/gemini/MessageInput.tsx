// src/components/MessageInput.tsx
import React, { useState } from 'react';
import styles from './MessageInput.module.css';

interface MessageInputProps {
    onSendMessage: (message: string) => void;
    disabled: boolean;
}

const MessageInput: React.FC<MessageInputProps> = ({ onSendMessage, disabled }) => {
    const [text, setText] = useState('');

    const handleSend = () => {
        if (text.trim()) {
            onSendMessage(text);
            setText(''); // 메시지 전송 후 입력창 비우기
        }
    };

    const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter' && !disabled) {
            handleSend();
        }
    };

    return (
        <div className={styles.inputContainer}>
            <input
                type="text"
                placeholder={disabled ? "Connecting..." : "Type a message..."}
                className={styles.input}
                value={text}
                onChange={(e) => setText(e.target.value)}
                onKeyPress={handleKeyPress}
                disabled={disabled}
            />
            <button className={styles.sendButton} onClick={handleSend} disabled={disabled}>
                Send
            </button>
        </div>
    );
};

export default MessageInput;