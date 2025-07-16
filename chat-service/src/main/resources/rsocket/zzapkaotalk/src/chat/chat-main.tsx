import { useState, useEffect } from 'react';
import {
    RSocketClient,
    JsonSerializer,
    IdentitySerializer,
} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import type {
    Payload,
    ReactiveSocket
} from "rsocket-types";

interface ChatMessage {
    fromUserId: number;
    toUserId: number;
    message: string;
    timestamp: string;
}

type RoutingMetadata = string;

class EchoResponder {
    private receiver: (payload: Payload<ChatMessage, RoutingMetadata>) => void;

    constructor(receiver: (payload: Payload<ChatMessage, RoutingMetadata>) => void) {
        this.receiver = receiver;
    }

    fireAndForget(payload: Payload<ChatMessage, RoutingMetadata>): void {
        console.log('Received message from server via fireAndForget');
        this.receiver(payload);
    }
}

const Chatting = () => {
    const [message, setMessage] = useState<string>('');
    const [socket, setSocket] = useState<ReactiveSocket<ChatMessage, RoutingMetadata> | null>(null);
    const [messages, setMessages] = useState<ChatMessage[]>([]);

    useEffect(() => {
        connect();

        return () => {
            if(socket) {
                socket.close();
            }
        }
    }, []);

    const messageReceiver = (payload: Payload<ChatMessage, RoutingMetadata>) => {
        setMessages((prevMessages) => [...prevMessages, payload.data!]);
    };
    const responder = new EchoResponder(messageReceiver);

    const send = () => {
        if(!socket) {
            console.error("Socket is not connected");
            return;
        }

        socket
            .fireAndForget({
                data: {
                    fromUserId: 1,
                    toUserId: 2,
                    message: message,
                    timestamp: "2025-07-16T15:45:00"
                },
                metadata: String.fromCharCode('chat.direct.1'.length) + 'chat.direct.1'
            });

        setMessage('');
    };

    const connect = () => {
        const client = new RSocketClient({
            serializers: {
                data: JsonSerializer,
                metadata: IdentitySerializer,
            },
            setup: {
                // ms btw sending keepalive to server
                keepAlive: 60000,
                // ms timeout if no keepalive response
                lifetime: 180000,
                // format of `data`
                dataMimeType: 'application/json',
                // format of `metadata`
                metadataMimeType: 'message/x.rsocket.routing.v0',
            },
            responder: responder,
            transport: new RSocketWebSocketClient({
                url: 'ws://localhost:28079/rsocket',
            }),
        });

        client.connect().subscribe({
            onComplete: (socket) => {
                // @ts-ignore
                setSocket(socket);
            },
            onError: (error) => {
                console.log("error: " + error);
            },
            onSubscribe: (cancel) => {
                console.log("canceled: " + cancel);
            },
        });
    };

    return (
        <div>
            <h1>Chatting</h1>
            <input
                type="text"
                value={message}
                onChange={(e) => setMessage(e.target.value)}
            />
            <button onClick={send}>전송</button>
            <ul>
                {messages.map((item, index) => (
                    <li key={index}>{item.fromUserId} : {item.message}</li>
                ))}
            </ul>
        </div>
    );
};

export default Chatting;