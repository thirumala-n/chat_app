import { useState, useMemo } from 'react'
import { Plus, Search, Sparkles } from 'lucide-react'
import ConversationItem from './ConversationItem'
import UserMenu from './UserMenu'
import ThemeToggle from './ThemeToggle'

function groupConversations(conversations) {
  const now = new Date()
  const today = [], yesterday = [], week = [], older = []

  for (const c of conversations) {
    const date = new Date(c.updatedAt || c.createdAt)
    const diffMs = now - date
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

    if (diffDays === 0) today.push(c)
    else if (diffDays === 1) yesterday.push(c)
    else if (diffDays < 7) week.push(c)
    else older.push(c)
  }

  return { today, yesterday, week, older }
}

function SectionHeader({ label }) {
  return <div className="sidebar-section-label">{label}</div>
}

export default function Sidebar({
  conversations,
  activeConvId,
  loading,
  onSelect,
  onNew,
  onDelete,
  open,           // mobile: is drawer open?
  onClose,        // mobile: close drawer
}) {
  const [search, setSearch] = useState('')

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return conversations
    return conversations.filter((c) =>
      (c.title || '').toLowerCase().includes(q)
    )
  }, [conversations, search])

  const groups = useMemo(() => groupConversations(filtered), [filtered])

  const renderGroup = (label, list) => {
    if (list.length === 0) return null
    return (
      <div key={label}>
        <SectionHeader label={label} />
        {list.map((c) => (
          <ConversationItem
            key={c.id}
            conversation={c}
            active={c.id === activeConvId}
            onSelect={(id) => { onSelect(id); onClose?.() }}
            onDelete={onDelete}
          />
        ))}
      </div>
    )
  }

  return (
    <>
      {/* Mobile overlay */}
      <div
        className={`sidebar-overlay ${open ? 'open' : ''}`}
        onClick={onClose}
        aria-hidden="true"
      />

      <aside className={`sidebar ${open ? 'open' : ''}`} aria-label="Sidebar navigation">
        {/* Header */}
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <div className="sidebar-logo-icon" aria-hidden="true">
              <Sparkles size={16} />
            </div>
            <span className="sidebar-logo-text">AI Chat</span>
          </div>
          <ThemeToggle />
        </div>

        {/* New chat button */}
        <button
          className="sidebar-new-chat-btn"
          onClick={() => { onNew(); onClose?.() }}
          aria-label="Start new conversation"
        >
          <Plus size={16} />
          New conversation
        </button>

        {/* Search */}
        <div className="sidebar-search">
          <Search size={14} className="sidebar-search-icon" />
          <input
            type="search"
            placeholder="Search conversations…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            aria-label="Search conversations"
          />
        </div>

        {/* Conversation list */}
        <div className="sidebar-list">
          {loading ? (
            <div style={{ padding: '12px 16px' }}>
              {[120, 90, 140, 80].map((w, i) => (
                <div key={i} style={{ marginBottom: 10 }}>
                  <div className="skeleton skeleton-line" style={{ width: w, marginBottom: 6 }} />
                </div>
              ))}
            </div>
          ) : filtered.length === 0 ? (
            <p style={{ padding: '16px', fontSize: 13, color: 'var(--text-tertiary)', textAlign: 'center' }}>
              {search ? 'No results found' : 'No conversations yet'}
            </p>
          ) : (
            <>
              {renderGroup('Today', groups.today)}
              {renderGroup('Yesterday', groups.yesterday)}
              {renderGroup('Previous 7 days', groups.week)}
              {renderGroup('Older', groups.older)}
            </>
          )}
        </div>

        {/* User menu at bottom */}
        <div className="sidebar-footer">
          <UserMenu />
        </div>
      </aside>
    </>
  )
}
