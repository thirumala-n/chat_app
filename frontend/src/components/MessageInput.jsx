import { useRef, useCallback, useEffect } from 'react'
import { ArrowUp, Square } from 'lucide-react'

export default function MessageInput({ value, onChange, onSend, disabled, streaming, onStop }) {
  const textareaRef = useRef(null)

  // Auto-resize textarea
  useEffect(() => {
    const el = textareaRef.current
    if (!el) return
    el.style.height = 'auto'
    el.style.height = Math.min(el.scrollHeight, 200) + 'px'
  }, [value])

  const handleKeyDown = useCallback(
    (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        if (!disabled && value.trim()) {
          onSend(value)
        }
      }
    },
    [disabled, value, onSend]
  )

  const handleSendClick = useCallback(() => {
    if (value.trim() && !disabled) {
      onSend(value)
    }
  }, [value, disabled, onSend])

  const canSend = value.trim().length > 0 && !disabled

  return (
    <div className="input-area">
      <div className="input-area-inner">
        <div className="input-box" role="form" aria-label="Message composer">
          <textarea
            ref={textareaRef}
            id="message-input"
            className="input-textarea"
            placeholder="Message the assistant…"
            value={value}
            onChange={(e) => onChange(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={disabled && !streaming}
            rows={1}
            aria-label="Type your message"
            aria-multiline="true"
          />

          {streaming ? (
            <button
              onClick={onStop}
              className="input-send-btn"
              aria-label="Stop generation"
              title="Stop generation"
              style={{ background: 'var(--text-secondary)' }}
            >
              <Square size={14} />
            </button>
          ) : (
            <button
              onClick={handleSendClick}
              disabled={!canSend}
              className="input-send-btn"
              aria-label="Send message"
              title="Send (Enter)"
            >
              <ArrowUp size={16} />
            </button>
          )}
        </div>
        <p className="input-hint">
          Press <kbd style={{ fontFamily: 'inherit' }}>Enter</kbd> to send,{' '}
          <kbd style={{ fontFamily: 'inherit' }}>Shift + Enter</kbd> for a new line
        </p>
      </div>
    </div>
  )
}
