export interface Opening {
    name: string;
    history: string[];
}

// 오프닝 데이터베이스 (PGN이 긴 순서대로 정렬하면 더 효율적일 수 있습니다)
const openings: Opening[] = [
    { name: '킹즈 폰 오프닝', history: ['e4', 'e5'] },
    { name: '이탈리안 게임', history: ['e4', 'e5', 'Nf3', 'Nc6', 'Bc4'] },
    { name: '이탈리안 게임, 지우코 피아노', history: ['e4', 'e5', 'Nf3', 'Nc6', 'Bc4', 'Bc5'] },
    { name: '이탈리안 게임, 지우코 피아니시모, 메인라인', history: ['e4', 'e5', 'Nf3', 'Nc6', 'Bc4', 'Bc5', 'c3', 'Nf6', 'd3'] },
    { name: '루이 로페즈', history: ['e4', 'e5', 'Nf3', 'Nc6', 'Bb5'] },
    { name: '스카치 게임', history: ['e4', 'e5', 'Nf3', 'Nc6', 'd4'] },
    { name: '스카치 갬빗', history: ['e4', 'e5', 'Nf3', 'Nc6', 'd4', 'exd4', 'Bc4'] },
    { name: '시실리안 디펜스', history: ['e4', 'c5'] },
    { name: '시실리안 디펜스, 엑셀러레이티드 드래곤', history: ['e4', 'c5', 'Nf3', 'Nc6', 'd4', 'cxd4', 'Nxd4', 'g6'] },
    { name: '시실리안 디펜스, 나이도프', history: ['e4', 'c5', 'Nf3', 'd6', 'd4', 'cxd4', 'Nxd4', 'Nf6', 'Nc3', 'a6'] },
    { name: '프렌치 디펜스', history: ['e4', 'e6'] },
    { name: '프렌치 디펜스, 어드밴스드 바리에이션', history: ['e4', 'e6', 'd4', 'd5', 'e5'] },
    { name: '프렌치 디펜스, 익스체인지 바리에이션', history: ['e4', 'e6', 'd4', 'd5', 'exd5'] },
    { name: '카로칸 디펜스', history: ['e4', 'c6', 'd4', 'd5'] },
    { name: '카로칸 디펜스, 어드밴스드 바리에이션', history: ['e4', 'c6', 'd4', 'd5', 'e5'] },
    { name: '카로칸 디펜스, 익스체인지 바리에이션', history: ['e4', 'c6', 'd4', 'd5', 'exd5'] },
    { name: '카로칸 디펜스, 판타지 바리에이션', history: ['e4', 'c6', 'd4', 'd5', 'f3'] },
    { name: '카로칸 디펜스, 판타지 바리에이션 메인 라인', history: ['e4', 'c6', 'd4', 'd5', 'f3', 'dxe4', 'fxe4', 'e5'] },
    { name: '스칸디나비안 디펜스', history: ['e4', 'd5'] },
    { name: '스칸디나비안 디펜스, 모던 라인', history: ['e4', 'd5', 'exd5', 'Nf6'] },
    { name: '알레킨 디펜스', history: ['e4', 'Nf6'] },

    // Queen's Pawn Openings
    { name: '퀸즈 폰 오프닝', history: ['d4', 'd5'] },
    { name: '퀸즈 갬빗', history: ['d4', 'd5', 'c4'] },
    { name: '슬라브 디펜스', history: ['d4', 'd5', 'c4', 'c6'] },
    { name: '인디언 디펜스', history: ['d4', 'Nf6'] },
    { name: '킹즈 인디언 디펜스', history: ['d4', 'Nf6', 'c4', 'g6'] },
    { name: '님조-인디언 디펜스', history: ['d4', 'Nf6', 'c4', 'e6', 'Nc3', 'Bb4'] },
    { name: '런던 시스템', history: ['d4', 'd5', 'Bf4'] },

    // Others
    { name: '레티 오프닝', history: ['Nf3'] },
    { name: '잉글리시 오프닝', history: ['c4'] },
    { name: '모던 디펜스', history: ['e4', 'g6'] },
];

export default openings;
