import api from './api'

export const getAllUsers = () =>
  api.get('/v1/users').then(r => r.data)

export const getUserById = (id) =>
  api.get(`/v1/users/${id}`).then(r => r.data)

export const deactivateUser = (id) =>
  api.patch(`/v1/users/${id}/deactivate`).then(r => r.data)

export const activateUser = (id) =>
  api.patch(`/v1/users/${id}/activate`).then(r => r.data)

export const getAuditLogs = (id) =>
  api.get(`/v1/users/${id}/audit-logs`).then(r => r.data)
