import React, {useState} from 'react';
import Sidebar from './Sidebar';
import ChatWindow from './ChatWindow';
import styles from './ChatPage.module.css';
import type {ChatUserResponse} from "./types.ts";
import {useMe} from "./useMe.ts";

const ChatPage: React.FC = () => {
    const [selectedUser, setSelectedUser] = useState<ChatUserResponse | null>(null)

    const { me } = useMe();

    const handleUserSelect = (user: ChatUserResponse) => {
        setSelectedUser(user);
    }

    return (
        <div className={styles.chatContainer}>
            <Sidebar onUserSelect={handleUserSelect} selectedUserEmail={selectedUser?.userEmail}/>
            <ChatWindow me={me} buddy={selectedUser}/>
        </div>
    );
};

export default ChatPage;