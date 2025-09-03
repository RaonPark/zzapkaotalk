// src/components/Sidebar.tsx
import React from 'react';
import styles from './Sidebar.module.css';
import {useChatBuddy} from "./useChatBuddy.ts";
import type {ChatUserResponse} from "./types.ts";

interface SidebarProps {
    onUserSelect: (user: ChatUserResponse) => void;
    selectedUserEmail: string | undefined;
}

const Sidebar: React.FC<SidebarProps> = ({ onUserSelect, selectedUserEmail }) => {
    const { chatUsers } = useChatBuddy();

    return (
        <div className={styles.sidebar}>
            <h2 className={styles.title}>Users</h2>
            <ul className={styles.userList}>
                {chatUsers.map(user => (
                    <li
                        key={user.userEmail}
                        className={`${styles.userItem} ${selectedUserEmail == user.userEmail ? styles.selected : ''}`}
                        onClick={() => onUserSelect(user)}
                    >
                        <img src={'https://i.pravatar.cc/40?u=alice'} alt={user.nickname} className={styles.avatar} />
                        <span>{user.nickname}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Sidebar;