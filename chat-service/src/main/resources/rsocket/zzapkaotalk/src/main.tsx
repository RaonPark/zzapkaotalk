import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import RSocketChat from "./chat/chatting.tsx";

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RSocketChat />
  </StrictMode>,
)
