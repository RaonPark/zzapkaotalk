import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import ChatPage from "./gemini/ChatPage.tsx";
import MyChess from "./chess/chess.tsx";

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MyChess />
  </StrictMode>,
)
