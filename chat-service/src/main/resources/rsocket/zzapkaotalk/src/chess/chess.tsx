import {useRef, useState} from 'react';
import { Chess, type PieceSymbol, type Square } from 'chess.js';
import {
    Chessboard,
    chessColumnToColumnIndex,
    type PieceDropHandlerArgs,
    type SquareHandlerArgs
} from 'react-chessboard';
import './chess.css';
import ReactConfetti from 'react-confetti';
import PromotionOverlay from './components/PromotionOverlay';
import MoveHistory from './components/MoveHistory';
import { detectOpening, getPieceData } from './utils/chessUtils';

function MyChess() {
    // chess.js 인스턴스를 생성하고, 게임 상태를 관리합니다.
    // 'new Chess()'는 초기 체스판 상태로 게임을 시작합니다.
    const [game, setGame] = useState(new Chess());

    // 현재 게임의 FEN(Forsyth-Edwards Notation) 문자열을 가져옵니다.
    // FEN은 체스판의 모든 기물 위치와 게임 상태를 하나의 문자열로 나타냅니다.
    // react-chessboard는 이 FEN 값을 사용해 체스판을 렌더링합니다.
    const [fen, setFen] = useState(game.fen());

    // 게임 상태 메시지 (예: 체크, 체크메이트)
    const [message, setMessage] = useState('');

    const [moveHistory, setMoveHistory] = useState<string[]>([]);

    const [moveFrom, setMoveFrom] = useState<string>('');

    const [optionSquares, setOptionSquares] = useState({});

    const [winner, setWinner] = useState<'white' | 'black' | null>(null);

    const [showConfetti, setShowConfetti] = useState<boolean>(false);

    const [promotionMove, setPromotionMove] = useState<Omit<PieceDropHandlerArgs, 'piece'> | null>(null);

    // 👈 2. 오프닝 이름을 저장할 새로운 상태 추가
    const [openingName, setOpeningName] = useState<string>('');

    const [boardOrientation, setBoardOrientation] = useState<'white' | 'black'>('white');

    const premovesRef = useRef<PieceDropHandlerArgs[]>([]);


    function onPromotionPieceSelect(piece: PieceSymbol) {
        if (promotionMove) {
            commitMoveAndUpdate({
                from: promotionMove.sourceSquare as Square,
                to: promotionMove.targetSquare as Square,
                promotion: piece
            });
        }
        setPromotionMove(null);
    }

    // calculate the left position of the promotion square
    const squareWidth = document.querySelector(`[data-column="a"][data-row="1"]`)?.getBoundingClientRect()?.width ?? 0;
    const promotionSquareLeft = promotionMove?.targetSquare ? squareWidth * chessColumnToColumnIndex(promotionMove.targetSquare.match(/^[a-z]+/)?.[0] ?? '', 8,
        // number of columns
        'white' // board orientation
    ) : 0;


    // 게임 상태를 확인하고 메시지를 업데이트하는 함수
    function updateGameStatus() {
        if (game.isCheckmate()) {
            setShowConfetti(true);
            setMessage(`체크메이트! ${game.turn() === 'w' ? '흑' : '백'}이 승리했습니다.`);
            setWinner(game.turn() === 'w' ? 'black' : 'white');
            setTimeout(() => {
                setShowConfetti(false);
            }, 10000);
        } else if (game.isDraw()) {
            setMessage('무승부입니다.');
        } else if (game.isCheck()) {

            setMessage('체크!');
        } else {
            setMessage('');
        }
    }

    // 공통 이동 커밋 로직: onDrop, onSquareClick, 승격 선택 시에 모두 사용
    function commitMoveAndUpdate(params: { from: Square, to: Square, promotion?: PieceSymbol, logPieceType?: string }): boolean {
        try {
            const move = game.move({
                from: params.from,
                to: params.to,
                promotion: params.promotion
            });

            // 이동 기록 (SAN)
            setMoveHistory(prev => [...prev, move.san]);
            // 오프닝 이름 업데이트
            setOpeningName(detectOpening(game.history()));
            // 로그 (선택)
            if (params.logPieceType) {
                console.log(`Move: ${getPieceData(params.logPieceType)} ${move.from} -> ${move.to}`);
            }
            // FEN, 게임 상태 반영
            setFen(game.fen());
            setGame(game);
            updateGameStatus();
            return true;
        } catch (e) {
            return false;
        }
    }

    function getMoveOptions(square: Square) {
        const moves = game.moves({
            square,
            verbose: true
        });

        if(moves.length === 0) {
            setOptionSquares({});
            return false;
        }

        const newSquares: Record<string, React.CSSProperties> = {};

        for (const move of moves) {
            newSquares[move.to] = {
                background: game.get(move.to) && game.get(move.to)?.color !== game.get(square)?.color ? 'radial-gradient(circle, rgba(0,0,0,.1) 85%, transparent 85%)' // larger circle for capturing
                    : 'radial-gradient(circle, rgba(0,0,0,.1) 25%, transparent 25%)',
                // smaller circle for moving
                borderRadius: '50%'
            };
        }

        newSquares[square] = {
            background: 'rgba(255, 255, 0, 0.4)'
        };

        setOptionSquares(newSquares);

        return true;
    }

    function onSquareClick({
                               square,
                               piece
                           }: SquareHandlerArgs) {
        // piece clicked to move
        if (!moveFrom && piece) {
            // get the move options for the square
            const hasMoveOptions = getMoveOptions(square as Square);

            // if move options, set the moveFrom to the square
            if (hasMoveOptions) {
                setMoveFrom(square);
            }

            // return early
            return;
        }

        // square clicked to move to, check if valid move
        const moves = game.moves({
            square: moveFrom as Square,
            verbose: true
        });
        const foundMove = moves.find(m => m.from === moveFrom && m.to === square);

        // not a valid move
        if (!foundMove) {
            // check if clicked on new piece
            const hasMoveOptions = getMoveOptions(square as Square);

            // if new piece, setMoveFrom, otherwise clear moveFrom
            setMoveFrom(hasMoveOptions ? square : '');

            // return early
            return;
        }

        // is normal move
        const ok = commitMoveAndUpdate({
            from: moveFrom as Square,
            to: square as Square,
            promotion: 'q'
        });
        if (!ok) {
            // if invalid, setMoveFrom and getMoveOptions
            const hasMoveOptions = getMoveOptions(square as Square);

            // if new piece, setMoveFrom, otherwise clear moveFrom
            if (hasMoveOptions) {
                setMoveFrom(square);
            }

            // return early
            return;
        }

        // clear moveFrom and optionSquares
        setMoveFrom('');
        setOptionSquares({});
    }

    // 기물을 옮겼을 때 실행되는 함수
    function onPieceDrop({piece, sourceSquare, targetSquare}: PieceDropHandlerArgs): boolean {
        if(targetSquare === null) return false;
        // chess.js의 move 메서드를 사용해 이동을 시도합니다.
        if(piece.pieceType[1] == 'P' && targetSquare.match(/\d+$/)?.[0] === '8') {
            const possibleMoves = game.moves({
                square: sourceSquare as Square
            });

            console.log(possibleMoves + " and " + `${targetSquare}=`);

            if(possibleMoves.some(move => move.includes(`${targetSquare}=`))) {
                console.log("promotion");
                setPromotionMove({
                    sourceSquare,
                    targetSquare
                });
            }

            return true;
        }

        const ok = commitMoveAndUpdate({
            from: sourceSquare as Square,
            to: targetSquare as Square,
            logPieceType: piece.pieceType
        });
        return ok;
    }


    // 게임을 초기 상태로 리셋하는 함수
    function resetGame() {
        const newGame = new Chess();
        setGame(newGame);
        setFen(newGame.fen());
        setMessage('');
        setMoveHistory([]);
        setOpeningName('');
        setShowConfetti(false);
        setWinner(null);
    }

    function changeOrientation() {
        if(boardOrientation === 'white') {
            setBoardOrientation('black');
        } else {
            setBoardOrientation('white');
        }
    }

    const chessboardOptions = {
        position: fen,
        boardWidth: 1280,
        showAnimation: true,
        onSquareClick,
        onPieceDrop,
        squareStyles: optionSquares,
        boardOrientation,
    }

    return (
        <div className="app-container">
            {showConfetti && <ReactConfetti/>}
            <h1>DO CHESS</h1>
            <div className="game-layout">
                <div className="chessboard-container">
                    <PromotionOverlay
                        visible={!!promotionMove}
                        left={promotionSquareLeft}
                        squareWidth={squareWidth}
                        onSelect={onPromotionPieceSelect}
                        onClose={() => setPromotionMove(null)}
                    />
                    <Chessboard options={chessboardOptions}/>
                </div>
                {winner && (
                    <div
                        className={`checkmate-effect ${winner === 'white' ? 'winner-white' : 'winner-black'}`}
                    >
                        체크메이트!
                    </div>
                )}
                <div className="game-info">
                    <h2>체스 기보</h2>
                    <h3>{openingName != '' && openingName}</h3>
                    <div className="history-container">
                        <MoveHistory moves={moveHistory} />
                    </div>
                    {message && <p className="game-message">{message}</p>}
                    <div className="button-container">
                        <button className="reset-button" onClick={resetGame}>
                            게임 리셋
                        </button>
                        <button className="reset-button" onClick={changeOrientation}>
                            판 돌리기
                        </button>
                    </div>

                </div>
            </div>
        </div>
    );
}

export default MyChess;