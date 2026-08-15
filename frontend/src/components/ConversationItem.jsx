import { useState, useRef, useEffect, useCallback } from 'react'
import { MoreHorizontal, Pencil, Trash2 } from 'lucide-react'

function formatRelativeDate(iso) {
  if (!iso) return ''
  const date = new Date(iso)
  const now = new Date()
  const diffMs = now - date
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (diffDays === 0) return 'Today'
  if (diffDays === 1) return 'Yesterday'
  if (diffDays < 7) return `${diffDays} days ago`
  return date.toLocaleDateString([], { month: 'short', day: 'numeric' })
}

export default function ConversationItem({ conversation, active, onSelect, onDelete }) {
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef(null)

  // Close menu on outside click
  useEffect(() => {
    if (!menuOpen) return
    const handler = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [menuOpen])

  const handleMenuToggle = useCallback((e) => {
    e.stopPropagation()
    setMenuOpen((o) => !o)
  }, [])

  const handleDelete = useCallback((e) => {
    e.stopPropagation()
    setMenuOpen(false)
    onDelete?.(conversation.id)
  }, [conversation.id, onDelete])

  const displayTitle = conversation.title || 'New conversation'

  return (
    <div
      style={{ position: 'relative' }}
      ref={menuRef}
    >
      <button
        className={`conv-item ${active ? 'active' : ''} ${menuOpen ? 'menu-open' : ''}`}
        onClick={() => onSelect?.(conversation.id)}
        aria-current={active ? 'page' : undefined}
        aria-label={displayTitle}
      >
        <span className="conv-item-text" title={displayTitle}>
          {displayTitle}
        </span>

        <button
          className="conv-item-menu-btn"
          onClick={handleMenuToggle}
          aria-label="Conversation options"
          aria-haspopup="true"
          aria-expanded={menuOpen}
        >
          <MoreHorizontal size={14} />
        </button>
      </button>

      {menuOpen && (
        <div className="conv-dropdown" role="menu">
          <button
            className="conv-dropdown-item danger"
            role="menuitem"
            onClick={handleDelete}
          >
            <Trash2 size={14} />
            Delete
          </button>
        </div>
      )}
    </div>
  )
}
