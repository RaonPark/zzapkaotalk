// src/components/Sidebar.tsx
import React, {useEffect, useState} from 'react';
import styles from './Sidebar.module.css';
import axios from "axios";
import type {ChatUserResponse} from "./types.ts";

// 임시 사용자 데이터
const users = [
    { id: 1, name: 'Alice', avatar: 'https://i.pravatar.cc/40?u=alice' },
    { id: 2, name: 'Bob', avatar: 'https://i.pravatar.cc/40?u=bob' },
    { id: 3, name: 'Charlie', avatar: 'https://i.pravatar.cc/40?u=charlie' },
];

const Sidebar: React.FC = () => {
    const [chatUsers, setChatUsers] = useState<ChatUserResponse[]>([]);

    useEffect(() => {
        const getAllUsers = async () => {
            axios.get('/chat-service/chat/users', {
                withCredentials: true,
            }).
                then(response => {
                    console.log(response);
                    setChatUsers(response.data);
                }).
                catch(error => {
                    console.error('Error fetching chat users:', error);
                });
        }

        getAllUsers();
    }, []);

    return (
        <div className={styles.sidebar}>
            <h2 className={styles.title}>Users</h2>
            <ul className={styles.userList}>
                {chatUsers.map(user => (
                    <li key={user.userEmail} className={styles.userItem}>
                        <img src={'https://i.pravatar.cc/40?u=alice'} alt={user.nickname} className={styles.avatar} />
                        <span>{user.nickname}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Sidebar;