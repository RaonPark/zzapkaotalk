import { defaultPieces, type PieceRenderObject } from "react-chessboard";
import type { PieceSymbol } from "chess.js";

interface PromotionOverlayProps {
  visible: boolean;
  left: number;
  squareWidth: number;
  onSelect: (piece: PieceSymbol) => void;
  onClose: () => void;
}

export default function PromotionOverlay({ visible, left, squareWidth, onSelect, onClose }: PromotionOverlayProps) {
  if (!visible) return null;

  return (
    <>
      <div
        onClick={onClose}
        onContextMenu={(e) => {
          e.preventDefault();
          onClose();
        }}
        style={{
          position: "absolute",
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: "rgba(0, 0, 0, 0.1)",
          zIndex: 1000,
        }}
      />

      <div
        style={{
          position: "absolute",
          top: 0,
          left,
          backgroundColor: "white",
          width: squareWidth,
          zIndex: 1001,
          display: "flex",
          flexDirection: "column",
          boxShadow: "0 0 10px 0 rgba(0, 0, 0, 0.5)",
        }}
      >
        {(["q", "r", "n", "b"] as PieceSymbol[]).map((piece) => (
          <button
            key={piece}
            onClick={() => onSelect(piece)}
            onContextMenu={(e) => {
              e.preventDefault();
            }}
            style={{
              width: "100%",
              aspectRatio: "1",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              padding: 0,
              border: "none",
              cursor: "pointer",
            }}
          >
            {defaultPieces[`w${piece.toUpperCase()}` as keyof PieceRenderObject]()}
          </button>
        ))}
      </div>
    </>
  );
}
