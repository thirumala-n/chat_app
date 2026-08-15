import { Menu } from 'lucide-react'

export default function ChatHeader({ title, onMenuToggle }) {
  return (
    <header className="chat-header">
      {/* Mobile hamburger */}
      <button
        className="icon-btn"
        onClick={onMenuToggle}
        aria-label="Open sidebar"
        style={{ display: 'none' }}
        id="sidebar-toggle-btn"
      >
        <Menu size={18} />
      </button>

      <h2 className="chat-header-title">
        {title || 'AI Chat'}
      </h2>
    </header>
  )
}
