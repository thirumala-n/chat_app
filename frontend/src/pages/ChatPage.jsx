import { useState, useEffect, useCallback } from 'react'
import { useAuth } from '../context/AuthContext'
import { wsService } from '../services/websocket'
import { useAiChat } from '../hooks/useAiChat'
import Sidebar from '../components/Sidebar'
import ChatHeader from '../components/ChatHeader'
import MessageList from '../components/MessageList'
import MessageInput from '../components/MessageInput'

export default function ChatPage() {
  const { user } = useAuth()
  const [draft, setDraft] = useState('')
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const {
    conversations,
    activeConvId,
    messages,
    loadingConvos,
    loadingMessages,
    sending,
    streaming,
    error,
    loadConversations,
    selectConversation,
    sendMessage,
    newConversation,
    deleteConversation,
    stopStreaming,
    setError,
  } = useAiChat()

  // Load conversation list on mount
  useEffect(() => {
    loadConversations().then((list) => {
      // Auto-select most recent conversation if any
      if (list.length > 0 && !activeConvId) {
        selectConversation(list[0].id)
      }
    })
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  // Keep WebSocket alive (for regular conversations, preserved for future use)
  useEffect(() => {
    wsService.connect()
    return () => wsService.disconnect()
  }, [])

  const handleSend = useCallback(
    async (content) => {
      if (!content.trim() || sending) return
      setDraft('')
      await sendMessage(content)
    },
    [sending, sendMessage]
  )

  const handleSuggestion = useCallback(
    (text) => {
      setDraft(text)
    },
    []
  )

  // Active conversation title
  const activeConv = conversations.find((c) => c.id === activeConvId)
  const headerTitle = activeConv?.title || (activeConvId ? 'Conversation' : 'New conversation')

  return (
    <div className="app-layout">
      {/* Sidebar */}
      <Sidebar
        conversations={conversations}
        activeConvId={activeConvId}
        loading={loadingConvos}
        onSelect={selectConversation}
        onNew={newConversation}
        onDelete={deleteConversation}
        open={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
      />

      {/* Main content */}
      <main className="main-content">
        <ChatHeader
          title={headerTitle}
          onMenuToggle={() => setSidebarOpen((o) => !o)}
        />

        {/* Error banner */}
        {error && (
          <div className="toast-error" role="alert">
            <span style={{ flex: 1 }}>{error}</span>
            <button
              onClick={() => setError(null)}
              style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'inherit', fontSize: 16, lineHeight: 1 }}
              aria-label="Dismiss error"
            >
              ×
            </button>
          </div>
        )}

        {/* Messages */}
        <MessageList
          messages={messages}
          loading={loadingMessages}
          sending={sending}
          streaming={streaming}
          currentUser={user}
          onSuggestion={handleSuggestion}
        />

        {/* Input composer */}
        <MessageInput
          value={draft}
          onChange={setDraft}
          onSend={handleSend}
          disabled={sending}
          streaming={streaming}
          onStop={stopStreaming}
        />
      </main>

      {/* Inline style for mobile sidebar toggle button visibility */}
      <style>{`
        @media (max-width: 768px) {
          #sidebar-toggle-btn { display: flex !important; }
        }
        @keyframes cursorBlink {
          0%, 100% { opacity: 1; }
          50% { opacity: 0; }
        }
      `}</style>
    </div>
  )
}