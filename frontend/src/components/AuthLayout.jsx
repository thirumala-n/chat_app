import { Link } from 'react-router-dom'
import { Sparkles } from 'lucide-react'

export default function AuthLayout({ children, title, subtitle }) {
  return (
    <div className="auth-root">
      {/* Left decorative panel */}
      <div className="auth-left">
        <div className="auth-left-bg" />
        <div className="auth-left-content">
          <div className="auth-brand">
            <div className="auth-brand-icon"><Sparkles size={18} /></div>
            <span className="auth-brand-name">AI Chat</span>
          </div>
          <h1 className="auth-tagline">
            Your intelligent<br />assistant, always<br />ready to help.
          </h1>
          <p className="auth-tagline-sub">
            Have natural conversations, get instant answers, write better code,
            and think through complex problems — all in one place.
          </p>
        </div>
        <div className="auth-testimonial">
          <p className="auth-testimonial-text">
            "The most natural AI interface I've ever used. It just gets what I'm asking."
          </p>
          <p className="auth-testimonial-author">— Early access user</p>
        </div>
      </div>

      {/* Right form panel */}
      <div className="auth-right">
        <div className="auth-form-card">
          {/* Mobile brand */}
          <div className="auth-mobile-brand">
            <div className="auth-brand-icon" style={{ width: 30, height: 30 }}><Sparkles size={15} /></div>
            <span className="auth-brand-name">AI Chat</span>
          </div>

          <h2 className="auth-form-title">{title}</h2>
          <p className="auth-form-sub">{subtitle}</p>
          {children}
        </div>
      </div>
    </div>
  )
}

export function AuthLink({ to, children }) {
  return (
    <Link to={to} className="auth-link">
      {children}
    </Link>
  )
}
