import api from './api'

export const sendNotification = (data) =>
  api.post('/v1/notifications/send', data).then(r => r.data)

export const getNotificationsByUser = (userId) =>
  api.get(`/v1/notifications/user/${userId}`).then(r => r.data)

export const getNotificationById = (id) =>
  api.get(`/v1/notifications/${id}`).then(r => r.data)

export const markAsRead = (id) =>
  api.patch(`/v1/notifications/${id}/read`).then(r => r.data)

export const getPendingNotifications = () =>
  api.get('/v1/notifications/pending').then(r => r.data)
