import React from 'react';
import Sidebar from './Sidebar';
import ChatWindow from './ChatWindow';
import styles from './ChatPage.module.css';

const ChatPage: React.FC = () => {
    return (
        <div className={styles.chatContainer}>
            <Sidebar />
            <ChatWindow />
        </div>
    );
};

export default ChatPage;