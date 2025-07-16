import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import Chatting from "./chat/chat-main.tsx";

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Chatting />
  </StrictMode>,
)
