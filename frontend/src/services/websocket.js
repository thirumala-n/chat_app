import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getToken } from '../api/config'

const WS_URL = import.meta.env.VITE_WS_URL || '/api/ws'

class WebSocketService {
  constructor() {
    this.client = null
    this.subscriptions = new Map()
  }

  connect(onConnect) {
    if (this.client?.connected) return

    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: { Authorization: `Bearer ${getToken()}` },
      reconnectDelay: 3000,
      onConnect: () => {
        onConnect?.()
      },
      onStompError: (frame) => console.error('STOMP error:', frame),
    })

    this.client.activate()
  }

  disconnect() {
    this.subscriptions.forEach((sub) => sub.unsubscribe())
    this.subscriptions.clear()
    this.client?.deactivate()
  }

  subscribe(destination, callback) {
    if (!this.client?.connected) return null
    const sub = this.client.subscribe(destination, (msg) => {
      callback(JSON.parse(msg.body))
    })
    this.subscriptions.set(destination, sub)
    return sub
  }

  unsubscribe(destination) {
    const sub = this.subscriptions.get(destination)
    if (sub) {
      sub.unsubscribe()
      this.subscriptions.delete(destination)
    }
  }

  sendTyping(conversationId, username, typing) {
    this.client?.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify({ conversationId, username, typing }),
    })
  }
}

export const wsService = new WebSocketService()
