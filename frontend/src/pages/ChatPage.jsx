import { useState, useEffect, useRef, useCallback } from 'react'
import { Send, LogOut, MessageSquare } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { conversationApi, messageApi } from '../api/services'
import { wsService } from '../services/websocket'

export default function ChatPage() {
  const { user, logout } = useAuth()
  const [conversations, setConversations] = useState([])
  const [activeId, setActiveId] = useState(null)
  const [messages, setMessages] = useState([])
  const [draft, setDraft] = useState('')
  const [loadingConvos, setLoadingConvos] = useState(true)
  const [loadingMessages, setLoadingMessages] = useState(false)
  const bottomRef = useRef(null)
  const activeIdRef = useRef(null)

  useEffect(() => {
    activeIdRef.current = activeId
  }, [activeId])

  // Load conversation list
  useEffect(() => {
    conversationApi.list().then(({ data }) => {
      const list = data.data || []
      setConversations(list)
      if (list.length > 0) setActiveId(list[0].id)
    }).finally(() => setLoadingConvos(false))
  }, [])

  // Load messages when active conversation changes
  useEffect(() => {
    if (!activeId) return
    setLoadingMessages(true)
    messageApi.list(activeId).then(({ data }) => {
      const page = data.data
      const list = (page.content || page || []).slice().reverse()
      setMessages(list)
    }).finally(() => setLoadingMessages(false))
  }, [activeId])

  // WebSocket: connect once
  useEffect(() => {
    wsService.connect()
    return () => wsService.disconnect()
  }, [])

  // Re-subscribe whenever the active conversation changes
  useEffect(() => {
    if (!activeId) return
    const topic = `/topic/conversation/${activeId}`
    wsService.subscribe(topic, (msg) => {
      setMessages((prev) => (prev.some((m) => m.id === msg.id) ? prev : [...prev, msg]))
    })
    return () => wsService.unsubscribe(topic)
  }, [activeId])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = useCallback(async (e) => {
    e.preventDefault()
    const content = draft.trim()
    if (!content || !activeId) return
    setDraft('')
    try {
      const { data } = await messageApi.send({ conversationId: activeId, content })
      setMessages((prev) => (prev.some((m) => m.id === data.data.id) ? prev : [...prev, data.data]))
    } catch (err) {
      console.error('Failed to send message', err)
      setDraft(content)
    }
  }, [draft, activeId])

  const activeConversation = conversations.find((c) => c.id === activeId)

  return (
    <div className="flex h-screen bg-surface text-white">
      {/* Sidebar */}
      <aside className="w-80 shrink-0 border-r border-slate-700 flex flex-col">
        <div className="flex items-center justify-between px-4 py-4 border-b border-slate-700">
          <div className="flex items-center gap-2">
            <MessageSquare className="h-6 w-6 text-primary-500" />
            <span className="font-semibold">Chats</span>
          </div>
          <button onClick={logout} title="Log out" className="text-slate-400 hover:text-white transition-colors">
            <LogOut className="h-5 w-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto">
          {loadingConvos ? (
            <div className="p-4 text-sm text-slate-500">Loading conversations…</div>
          ) : conversations.length === 0 ? (
            <div className="p-4 text-sm text-slate-500">No conversations yet.</div>
          ) : (
            conversations.map((c) => (
              <button
                key={c.id}
                onClick={() => setActiveId(c.id)}
                className={`w-full text-left px-4 py-3 border-b border-slate-800 hover:bg-surface-light transition-colors ${
                  c.id === activeId ? 'bg-surface-light' : ''
                }`}
              >
                <div className="font-medium truncate">{c.name || c.title || 'Untitled conversation'}</div>
                <div className="text-sm text-slate-400 truncate">
                  {c.lastMessage?.content || 'No messages yet'}
                </div>
              </button>
            ))
          )}
        </div>

        <div className="px-4 py-3 border-t border-slate-700 text-sm text-slate-400 truncate">
          Signed in as {user?.username || user?.email}
        </div>
      </aside>

      {/* Main chat panel */}
      <main className="flex-1 flex flex-col min-w-0">
        {!activeId ? (
          <div className="flex-1 flex items-center justify-center text-slate-500">
            Select a conversation to start chatting
          </div>
        ) : (
          <>
            <div className="px-6 py-4 border-b border-slate-700 font-semibold">
              {activeConversation?.name || activeConversation?.title || 'Conversation'}
            </div>

            <div className="flex-1 overflow-y-auto px-6 py-4 space-y-3">
              {loadingMessages ? (
                <div className="text-sm text-slate-500">Loading messages…</div>
              ) : messages.length === 0 ? (
                <div className="text-sm text-slate-500">No messages yet. Say hello!</div>
              ) : (
                messages.map((m) => {
                  const mine = m.senderId === user?.id || m.sender?.id === user?.id
                  return (
                    <div key={m.id} className={`flex ${mine ? 'justify-end' : 'justify-start'}`}>
                      <div
                        className={`max-w-[70%] rounded-lg px-4 py-2 ${
                          mine ? 'bg-primary-600 text-white' : 'bg-surface-light text-slate-100'
                        }`}
                      >
                        {!mine && (
                          <div className="text-xs text-slate-400 mb-1">
                            {m.sender?.username || m.senderName || 'Someone'}
                          </div>
                        )}
                        <div className="whitespace-pre-wrap break-words">{m.content}</div>
                      </div>
                    </div>
                  )
                })
              )}
              <div ref={bottomRef} />
            </div>

            <form onSubmit={handleSend} className="flex items-center gap-2 px-6 py-4 border-t border-slate-700">
              <input
                type="text"
                value={draft}
                onChange={(e) => setDraft(e.target.value)}
                placeholder="Type a message…"
                className="flex-1 rounded-lg bg-surface-light border border-slate-600 px-4 py-2.5 text-white placeholder-slate-500 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
              />
              <button
                type="submit"
                disabled={!draft.trim()}
                className="rounded-lg bg-primary-600 p-2.5 hover:bg-primary-500 disabled:opacity-50 transition-colors"
              >
                <Send className="h-5 w-5" />
              </button>
            </form>
          </>
        )}
      </main>
    </div>
  )
}