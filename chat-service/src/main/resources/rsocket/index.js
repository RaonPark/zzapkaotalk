// 1. CDN에서 RSocket 라이브러리를 직접 가져옵니다.
import { RSocketClient, JsonSerializer, IdentitySerializer } from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';

// 2. HTML 요소들을 가져옵니다.
const connectBtn = document.getElementById('connectBtn');
const logContainer = document.getElementById('log');

// 로그를 화면에 출력하는 헬퍼 함수
function log(message) {
    const p = document.createElement('pre');
    p.innerText = `[${new Date().toLocaleTimeString()}] ${message}`;
    logContainer.appendChild(p);
    logContainer.scrollTop = logContainer.scrollHeight; // 자동 스크롤
    console.log(message);
}

// 3. 메인 로직: RSocket 클라이언트 생성 및 연결
async function main() {
    log('Client starting...');

    const transport = new RSocketWebSocketClient({
        // 서버 설정과 동일한 주소를 사용합니다.
        url: 'ws://localhost:28079/rsocket'
    });

    const client = new RSocketClient({
        setup: {
            keepAlive: 60000,
            lifetime: 180000,
            dataMimeType: 'application/json',
            metadataMimeType: 'message/x.rsocket.routing.v0', // 라우팅 메타데이터 타입 명시
        },
        transport,
    });

    try {
        // 4. 서버에 연결합니다.
        const socket = await client.connect();
        log('✅ RSocket connection established!');

        // 5. Request-Response 모델로 요청을 보냅니다.
        log('Sending a request...');
        socket.requestResponse({
            data: {
                message: "Hello from Browser!",
                fromUserId: 1,
                toUserId: 1,
                timestamp: "2025-07-14T16:36:00"
            }, // @Payload 에 전달될 데이터
            metadata: String.fromCharCode('chat.direct.1'.length) + 'chat.direct.1' // @MessageMapping 경로
        }).subscribe({
            onComplete: (response) => {
                log(`🔔 Response received: ${JSON.stringify(response.data)}`);
            },
            onError: (error) => {
                log(`❌ Error in request: ${error.message}`);
            },
        });

    } catch (error) {
        log(`❌ Connection error: ${error.message}`);
    }
}

// 버튼 클릭 시 main 함수를 실행합니다.
connectBtn.onclick = main;