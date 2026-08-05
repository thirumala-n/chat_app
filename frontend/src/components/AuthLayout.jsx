import { Link } from 'react-router-dom'
import { MessageSquare } from 'lucide-react'

export default function AuthLayout({ children, title, subtitle }) {
  return (
    <div className="flex min-h-screen">
      <div className="hidden lg:flex lg:w-1/2 flex-col justify-center bg-gradient-to-br from-primary-700 to-primary-900 p-12">
        <div className="flex items-center gap-3 mb-8">
          <MessageSquare className="h-10 w-10 text-white" />
          <span className="text-2xl font-bold text-white">AI Chat Platform</span>
        </div>
        <h1 className="text-4xl font-bold text-white mb-4">Connect. Chat. Collaborate.</h1>
        <p className="text-primary-200 text-lg max-w-md">
          Enterprise-grade real-time messaging with AI-powered assistance.
          Built for teams that move fast.
        </p>
      </div>
      <div className="flex w-full lg:w-1/2 flex-col justify-center px-8 py-12 bg-surface">
        <div className="mx-auto w-full max-w-md">
          <div className="lg:hidden flex items-center gap-2 mb-8">
            <MessageSquare className="h-8 w-8 text-primary-500" />
            <span className="text-xl font-bold">AI Chat Platform</span>
          </div>
          <h2 className="text-2xl font-bold mb-1">{title}</h2>
          <p className="text-slate-400 mb-8">{subtitle}</p>
          {children}
        </div>
      </div>
    </div>
  )
}

export function AuthLink({ to, children }) {
  return <Link to={to} className="text-primary-400 hover:text-primary-300 transition-colors">{children}</Link>
}
