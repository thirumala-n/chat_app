import api from './client'

export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  resetPassword: (data) => api.post('/auth/reset-password', data),
  verifyEmail: (token) => api.get(`/auth/verify-email?token=${token}`),
}

export const userApi = {
  getProfile: () => api.get('/users/me'),
  updateProfile: (data) => api.put('/users/me', data),
  uploadAvatar: (file) => {
    const form = new FormData()
    form.append('file', file)
    return api.post('/users/me/avatar', form, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
  changePassword: (data) => api.post('/users/me/change-password', data),
  search: (q) => api.get(`/users/search?q=${encodeURIComponent(q)}`),
  getOnlineUsers: () => api.get('/users/online'),
}

export const conversationApi = {
  list: (archived = false) => api.get(`/conversations?archived=${archived}`),
  search: (q) => api.get(`/conversations/search?q=${encodeURIComponent(q)}`),
  direct: (userId) => api.post(`/conversations/direct/${userId}`),
  createGroup: (data) => api.post('/conversations/groups', data),
  addMembers: (id, memberIds) => api.post(`/conversations/${id}/members`, memberIds),
  removeMember: (id, memberId) => api.delete(`/conversations/${id}/members/${memberId}`),
  togglePin: (id) => api.put(`/conversations/${id}/pin`),
  toggleArchive: (id) => api.put(`/conversations/${id}/archive`),
  delete: (id) => api.delete(`/conversations/${id}`),
}

export const messageApi = {
  list: (conversationId, page = 0, size = 50) =>
    api.get(`/messages/conversation/${conversationId}?page=${page}&size=${size}`),
  send: (data) => api.post('/messages', data),
  sendWithFiles: (conversationId, content, files) => {
    const form = new FormData()
    form.append('conversationId', conversationId)
    if (content) form.append('content', content)
    files.forEach((f) => form.append('files', f))
    return api.post('/messages/with-attachments', form, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
  edit: (id, content) => api.put(`/messages/${id}`, { content }),
  delete: (id) => api.delete(`/messages/${id}`),
  react: (id, emoji) => api.post(`/messages/${id}/reactions`, { emoji }),
  forward: (id, targetConversationId) =>
    api.post(`/messages/${id}/forward?targetConversationId=${targetConversationId}`),
  markRead: (conversationId, messageId) =>
    api.post(`/messages/read?conversationId=${conversationId}&messageId=${messageId}`),
  suggestions: (conversationId, partial) =>
    api.get(`/messages/suggestions?conversationId=${conversationId}&partial=${encodeURIComponent(partial)}`),
}

export const notificationApi = {
  list: (page = 0) => api.get(`/notifications?page=${page}`),
  unreadCount: () => api.get('/notifications/unread-count'),
  markRead: (id) => api.put(`/notifications/${id}/read`),
  markAllRead: () => api.put('/notifications/read-all'),
}

export const aiApi = {
  conversations: () => api.get('/ai/conversations'),
  getConversation: (id) => api.get(`/ai/conversations/${id}`),
  chat: (data) => api.post('/ai/chat', data),
  deleteConversation: (id) => api.delete(`/ai/conversations/${id}`),
}
