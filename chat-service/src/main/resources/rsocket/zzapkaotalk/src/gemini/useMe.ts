import {useEffect, useState} from "react";
import type {Me} from "./types.ts";
import axios from "axios";

export const useMe = () => {
    const [me, setMe] = useState<Me>({
        email: 'anonymous',
        nickname: 'anonymous',
        profileImage: 'X',
        lastSeen: 'X',
    });

    useEffect(() => {
        const getMe = async () => {
            axios.get('/auth/user/me', {
                withCredentials: true,
            }).then(response => {
               setMe(response.data);
               console.log(`me from auth ${response.data.email}`);
            });
        };

        getMe();
    }, []);

    return { me }
}