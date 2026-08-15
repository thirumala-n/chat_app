import { useState, useCallback } from 'react'
import { Copy, Check, Sparkles, User } from 'lucide-react'
import MarkdownRenderer from './MarkdownRenderer'
import LoadingDots from './LoadingDots'

function formatTime(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function getInitials(user) {
  if (!user) return 'U'
  if (user.firstName && user.lastName) return (user.firstName[0] + user.lastName[0]).toUpperCase()
  if (user.username) return user.username[0].toUpperCase()
  if (user.email) return user.email[0].toUpperCase()
  return 'U'
}

function CopyButton({ content }) {
  const [copied, setCopied] = useState(false)
  const handleCopy = useCallback(() => {
    navigator.clipboard.writeText(content).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }, [content])

  return (
    <button className="msg-action-btn" onClick={handleCopy} aria-label="Copy message">
      {copied ? <Check size={13} /> : <Copy size={13} />}
      {copied ? 'Copied' : 'Copy'}
    </button>
  )
}

/**
 * A single AI conversation message row.
 * role: 'user' | 'assistant'
 */
export default function Message({ message, currentUser }) {
  const isUser = message.role === 'user'
  const isStreaming = message._streaming

  return (
    <div className={`msg-row msg-appear ${isUser ? 'user' : 'ai'}`}>
      {/* Avatar */}
      <div className={`msg-avatar ${isUser ? 'user' : 'ai'}`} aria-hidden="true">
        {isUser
          ? (currentUser ? getInitials(currentUser) : <User size={15} />)
          : <Sparkles size={15} />
        }
      </div>

      {/* Body */}
      <div className="msg-body">
        <div className={`msg-sender`}>
          {isUser ? (currentUser?.firstName || currentUser?.username || 'You') : 'Assistant'}
        </div>

        <div className={`msg-bubble ${isUser ? 'user' : 'ai'}`}>
          {isStreaming && !message.content ? (
            <LoadingDots />
          ) : isUser ? (
            <span style={{ whiteSpace: 'pre-wrap' }}>{message.content}</span>
          ) : (
            <MarkdownRenderer content={message.content} />
          )}
          {isStreaming && message.content && (
            <span
              style={{
                display: 'inline-block',
                width: 2,
                height: '1em',
                background: 'var(--accent)',
                marginLeft: 2,
                verticalAlign: 'text-bottom',
                animation: 'cursorBlink 0.8s step-end infinite',
              }}
              aria-hidden="true"
            />
          )}
        </div>

        {/* Actions (shown on hover) */}
        {!isStreaming && message.content && (
          <div className={`msg-actions`}>
            <CopyButton content={message.content} />
            {message.createdAt && (
              <span className="msg-time">{formatTime(message.createdAt)}</span>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
