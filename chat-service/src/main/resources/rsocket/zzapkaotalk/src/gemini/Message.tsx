// src/components/Message.tsx
import React from 'react';
import styles from './Message.module.css';

interface MessageProps {
    text: string;
    isOwnMessage: boolean;
}

const Message: React.FC<MessageProps> = ({ text, isOwnMessage }) => {
    const messageClass = isOwnMessage ? `${styles.message} ${styles.own}` : styles.message;

    return (
        <div className={messageClass}>
            <div className={styles.bubble}>
                {text}
            </div>
        </div>
    );
};

export default Message;