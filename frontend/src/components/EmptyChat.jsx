import { Sparkles } from 'lucide-react'

const SUGGESTIONS = [
  { icon: '💡', title: 'Explain a concept', desc: 'Break down complex topics simply' },
  { icon: '💻', title: 'Help me write code', desc: 'Generate or review code snippets' },
  { icon: '🐛', title: 'Debug my code', desc: 'Find and fix bugs in your app' },
  { icon: '📊', title: 'Analyze a problem', desc: 'Think through challenges together' },
]

export default function EmptyChat({ onSuggestion }) {
  return (
    <div className="empty-chat msg-appear">
      <div className="empty-chat-icon">
        <Sparkles size={26} />
      </div>

      <div style={{ textAlign: 'center' }}>
        <h1 className="empty-chat-heading">What can I help with?</h1>
        <p className="empty-chat-sub" style={{ marginTop: 8 }}>
          Start a conversation — I'm ready to assist you.
        </p>
      </div>

      <div className="suggestion-grid">
        {SUGGESTIONS.map((s) => (
          <button
            key={s.title}
            className="suggestion-card"
            onClick={() => onSuggestion?.(s.title)}
            aria-label={`Suggestion: ${s.title}`}
          >
            <div className="suggestion-card-icon">{s.icon}</div>
            <div className="suggestion-card-title">{s.title}</div>
            <div className="suggestion-card-desc">{s.desc}</div>
          </button>
        ))}
      </div>
    </div>
  )
}
