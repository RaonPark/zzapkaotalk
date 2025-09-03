import openings from "../openings.ts";

// Returns a human-readable piece description in Korean based on piece type string like "wP", "bK"
export function getPieceData(pieceType: string): string {
  let whichPiece = pieceType[0] === "w" ? "백" : "흑";
  switch (pieceType[1]) {
    case "P":
    case "p":
      whichPiece += " 폰";
      break;
    case "N":
    case "n":
      whichPiece += " 나이트";
      break;
    case "B":
    case "b":
      whichPiece += " 비숍";
      break;
    case "R":
    case "r":
      whichPiece += " 룩";
      break;
    case "Q":
    case "q":
      whichPiece += " 퀸";
      break;
    case "K":
    case "k":
      whichPiece += " 킹";
      break;
  }
  return whichPiece;
}

// Detect opening name by matching the current history to known openings.
// Returns '초기 상태' for empty history and '커스텀 포지션' if no match found.
export function detectOpening(history: string[] | undefined | null): string {
  if (!history || history.length === 0) return "초기 상태";

  let foundOpening = "커스텀 포지션";
  let longestMatchLength = 0;

  const startsWith = (full: string[], prefix: string[]) => {
    if (prefix.length > full.length) return false;
    return prefix.every((move, idx) => full[idx] === move);
  };

  for (const opening of openings) {
    if (startsWith(history, opening.history)) {
      if (opening.history.length > longestMatchLength) {
        longestMatchLength = opening.history.length;
        foundOpening = opening.name;
      }
    }
  }

  return foundOpening;
}
