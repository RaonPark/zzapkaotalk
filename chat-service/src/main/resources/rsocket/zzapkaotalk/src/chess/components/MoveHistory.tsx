interface MoveHistoryProps {
  moves: string[];
}

export default function MoveHistory({ moves }: MoveHistoryProps) {
  const formatted = [];
  for (let i = 0; i < moves.length; i += 2) {
    const moveNumber = i / 2 + 1;
    const whiteMove = moves[i];
    const blackMove = moves[i + 1] ? moves[i + 1] : "";
    formatted.push(
      <li key={moveNumber}>
        <span>{moveNumber}.</span>
        <span>{whiteMove}</span>
        <span>{blackMove}</span>
      </li>
    );
  }

  return <ol>{formatted}</ol>;
}
