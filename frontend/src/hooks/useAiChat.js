import { useState, useCallback, useRef } from 'react'
import { aiApi } from '../api/services'
import { getToken } from '../api/config'

const API_BASE = import.meta.env.VITE_API_URL || '/api'

/**
 * Custom hook encapsulating all AI chat state and logic.
 * Uses /ai/conversations + /ai/chat (and SSE streaming via /ai/chat/stream).
 */
export function useAiChat() {
  const [conversations, setConversations] = useState([])
  const [activeConvId, setActiveConvId] = useState(null)
  const [messages, setMessages] = useState([])
  const [loadingConvos, setLoadingConvos] = useState(false)
  const [loadingMessages, setLoadingMessages] = useState(false)
  const [sending, setSending] = useState(false)
  const [streaming, setStreaming] = useState(false)
  const [error, setError] = useState(null)
  const abortRef = useRef(null)

  // ── Load all AI conversations ────────────────────────────────────────────
  const loadConversations = useCallback(async () => {
    setLoadingConvos(true)
    setError(null)
    try {
      const { data } = await aiApi.conversations()
      const list = data.data || []
      setConversations(list)
      return list
    } catch (err) {
      console.error('Failed to load AI conversations', err)
      setError('Failed to load conversations')
      return []
    } finally {
      setLoadingConvos(false)
    }
  }, [])

  // ── Select and load a conversation ──────────────────────────────────────
  const selectConversation = useCallback(async (id) => {
    setActiveConvId(id)
    setMessages([])
    if (!id) return

    setLoadingMessages(true)
    setError(null)
    try {
      const { data } = await aiApi.getConversation(id)
      const conv = data.data
      setMessages(conv.messages || [])
    } catch (err) {
      console.error('Failed to load AI conversation messages', err)
      setError('Failed to load messages')
    } finally {
      setLoadingMessages(false)
    }
  }, [])

  // ── Send a message (with optional SSE streaming) ─────────────────────────
  const sendMessage = useCallback(async (content, conversationId = activeConvId) => {
    if (!content.trim() || sending) return

    // Optimistically add the user message
    const tempUserMsg = {
      id: `temp-user-${Date.now()}`,
      role: 'user',
      content: content.trim(),
      createdAt: new Date().toISOString(),
      _temp: true,
    }
    setMessages((prev) => [...prev, tempUserMsg])
    setSending(true)
    setError(null)

    const requestBody = {
      message: content.trim(),
      conversationId: conversationId || null,
    }

    // Try SSE streaming first
    const streamUrl = `${API_BASE}/ai/chat/stream`
    const token = getToken()

    try {
      abortRef.current = new AbortController()

      const response = await fetch(streamUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
          Accept: 'text/event-stream',
        },
        body: JSON.stringify(requestBody),
        signal: abortRef.current.signal,
      })

      if (!response.ok) {
        throw new Error(`Stream failed: ${response.status}`)
      }

      // Add a placeholder AI message
      const tempAiId = `temp-ai-${Date.now()}`
      const tempAiMsg = {
        id: tempAiId,
        role: 'assistant',
        content: '',
        createdAt: new Date().toISOString(),
        _temp: true,
        _streaming: true,
      }
      setMessages((prev) => [...prev, tempAiMsg])
      setStreaming(true)
      setSending(false)

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let aiContent = ''
      let newConvId = conversationId

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const text = decoder.decode(value, { stream: true })
        const lines = text.split('\n')

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const chunk = line.slice(5).trim()
            if (chunk && chunk !== '[DONE]') {
              aiContent += chunk
              setMessages((prev) =>
                prev.map((m) =>
                  m.id === tempAiId
                    ? { ...m, content: aiContent }
                    : m
                )
              )
            }
          }
        }
      }

      // Mark streaming complete
      setMessages((prev) =>
        prev.map((m) =>
          m.id === tempAiId ? { ...m, _streaming: false } : m
        )
      )

      // Reload conversations list to pick up new/updated title
      const list = await loadConversations()
      // If we started a new conversation, find it and set it active
      if (!conversationId && list.length > 0) {
        newConvId = list[0].id
        setActiveConvId(newConvId)
      }
    } catch (err) {
      if (err.name === 'AbortError') return

      // Streaming failed — fall back to regular POST
      try {
        const { data } = await aiApi.chat(requestBody)
        const aiMsg = data.data

        // Replace temp user message and add real AI message
        setMessages((prev) => {
          const withoutTemp = prev.filter((m) => m.id !== tempUserMsg.id && m._streaming !== true)
          const userMsg = { id: `u-${Date.now()}`, role: 'user', content: content.trim(), createdAt: new Date().toISOString() }
          return [...withoutTemp, userMsg, aiMsg]
        })

        // Refresh conversations to pick up new conversation
        const list = await loadConversations()
        if (!conversationId && list.length > 0) {
          setActiveConvId(list[0].id)
        }
      } catch (fallbackErr) {
        console.error('Failed to send AI message', fallbackErr)
        setError('Failed to send message. Please try again.')
        // Remove the temp user message on failure
        setMessages((prev) => prev.filter((m) => m.id !== tempUserMsg.id))
      }
    } finally {
      setSending(false)
      setStreaming(false)
      abortRef.current = null
    }
  }, [activeConvId, sending, loadConversations])

  // ── Start new conversation ───────────────────────────────────────────────
  const newConversation = useCallback(() => {
    setActiveConvId(null)
    setMessages([])
    setError(null)
  }, [])

  // ── Delete a conversation ────────────────────────────────────────────────
  const deleteConversation = useCallback(async (id) => {
    try {
      await aiApi.deleteConversation(id)
      setConversations((prev) => prev.filter((c) => c.id !== id))
      if (activeConvId === id) {
        setActiveConvId(null)
        setMessages([])
      }
    } catch (err) {
      console.error('Failed to delete conversation', err)
    }
  }, [activeConvId])

  // ── Stop streaming ───────────────────────────────────────────────────────
  const stopStreaming = useCallback(() => {
    abortRef.current?.abort()
  }, [])

  return {
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
  }
}
