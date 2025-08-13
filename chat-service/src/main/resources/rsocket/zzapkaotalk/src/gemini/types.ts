// src/types.ts
export interface MessagePayload {
    id: string; // 메시지 고유 ID (서버에서 생성)
    sender: string;
    text: string;
    isOwnMessage: boolean; // UI에서 내가 보낸 메시지인지 구분하기 위한 플래그
}

export interface DirectMessageRequest {
    message: string;
    fromUserEmail: string;
    toUserEmail: string;
    timestamp: string;
}

export interface DirectMessageResponse {
    message: string;
    fromUserId: string;
    toUserId: string;
    timestamp: string;
}

export interface ChatUserResponse {
    userEmail: string;
    nickname: string;
    profileImage: string;
}