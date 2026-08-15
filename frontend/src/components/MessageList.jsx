import { useEffect, useRef } from 'react'
import Message from './Message'
import EmptyChat from './EmptyChat'
import LoadingDots from './LoadingDots'

// Skeleton rows for loading state
function MessageSkeleton() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24, padding: '24px 0' }}>
      {[80, 55, 90, 65].map((w, i) => (
        <div key={i} style={{ display: 'flex', gap: 12, flexDirection: i % 2 === 1 ? 'row-reverse' : 'row' }}>
          <div className="skeleton" style={{ width: 32, height: 32, borderRadius: '50%', flexShrink: 0 }} />
          <div style={{ flex: 1, maxWidth: 480 }}>
            <div className="skeleton skeleton-line" style={{ width: '40%', marginBottom: 8 }} />
            <div className="skeleton" style={{ height: 60, borderRadius: 10, width: `${w}%` }} />
          </div>
        </div>
      ))}
    </div>
  )
}

export default function MessageList({ messages, loading, sending, streaming, currentUser, onSuggestion }) {
  const bottomRef = useRef(null)
  const containerRef = useRef(null)

  // Auto-scroll: only scroll when near the bottom, or when sending/streaming
  useEffect(() => {
    const el = containerRef.current
    if (!el) return
    const atBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 100
    if (atBottom || sending || streaming) {
      bottomRef.current?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
    }
  }, [messages, sending, streaming])

  return (
    <div className="message-list" ref={containerRef}>
      <div className="message-list-inner">
        {loading ? (
          <MessageSkeleton />
        ) : messages.length === 0 ? (
          <EmptyChat onSuggestion={onSuggestion} />
        ) : (
          <>
            {messages.map((msg) => (
              <Message key={msg.id} message={msg} currentUser={currentUser} />
            ))}
            {/* Show loading dots when waiting for AI to start responding */}
            {sending && !streaming && (
              <div className="msg-row ai">
                <div className="msg-avatar ai" aria-hidden="true">✦</div>
                <div className="msg-body">
                  <div className="msg-sender">Assistant</div>
                  <LoadingDots />
                </div>
              </div>
            )}
          </>
        )}
        <div ref={bottomRef} style={{ height: 1 }} />
      </div>
    </div>
  )
}
