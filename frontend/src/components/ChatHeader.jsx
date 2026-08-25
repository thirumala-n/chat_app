import { PanelLeftClose, PanelLeft } from 'lucide-react'

export default function ChatHeader({ title, onMenuToggle, sidebarCollapsed }) {
  return (
    <header className="chat-header">
      <button
        className="icon-btn sidebar-toggle-btn"
        onClick={onMenuToggle}
        aria-label={sidebarCollapsed ? 'Open sidebar' : 'Close sidebar'}
        title={sidebarCollapsed ? 'Open sidebar' : 'Close sidebar'}
      >
        {sidebarCollapsed ? <PanelLeft size={19} /> : <PanelLeftClose size={19} />}
      </button>

      <h2 className="chat-header-title">
        {title || 'AI Chat'}
      </h2>
    </header>
  )
}
