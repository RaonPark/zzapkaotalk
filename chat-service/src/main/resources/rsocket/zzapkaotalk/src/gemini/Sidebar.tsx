// src/components/Sidebar.tsx
import React from 'react';
import styles from './Sidebar.module.css';

// 임시 사용자 데이터
const users = [
    { id: 1, name: 'Alice', avatar: 'https://i.pravatar.cc/40?u=alice' },
    { id: 2, name: 'Bob', avatar: 'https://i.pravatar.cc/40?u=bob' },
    { id: 3, name: 'Charlie', avatar: 'https://i.pravatar.cc/40?u=charlie' },
];

const Sidebar: React.FC = () => {
    return (
        <div className={styles.sidebar}>
            <h2 className={styles.title}>Users</h2>
            <ul className={styles.userList}>
                {users.map(user => (
                    <li key={user.id} className={styles.userItem}>
                        <img src={user.avatar} alt={user.name} className={styles.avatar} />
                        <span>{user.name}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Sidebar;