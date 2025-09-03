import {useEffect, useState} from "react";
import type {ChatUserResponse} from "./types.ts";
import axios from "axios";

export const useChatBuddy = () => {
    const [chatUsers, setChatUsers] = useState<ChatUserResponse[]>([]);

    useEffect(() => {
        const getAllUsers = async () => {
            axios.get('/chat-service/chat/users', {
                withCredentials: true,
            }).
            then(response => {
                console.log(response.data);
                setChatUsers(response.data);
            }).
            catch(error => {
                console.error('Error fetching chat users:', error);
            });
        }

        getAllUsers();
    }, []);

    return { chatUsers }
}