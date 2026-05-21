import api from './api'

export const postTransaction = (data) =>
  api.post('/v1/transactions', data).then(r => r.data)

export const reverseTransaction = (id) =>
  api.post(`/v1/transactions/${id}/reverse`).then(r => r.data)

export const getTransactionById = (id) =>
  api.get(`/v1/transactions/${id}`).then(r => r.data)

export const getTransactionsByAccount = (accountId) =>
  api.get(`/v1/transactions/account/${accountId}`).then(r => r.data)

export const getAllTransactions = () =>
  api.get('/v1/transactions').then(r => r.data)

export const getAllGLPostings = () =>
  api.get('/v1/gl-postings').then(r => r.data)

export const getGLByTransaction = (txnId) =>
  api.get(`/v1/gl-postings/transaction/${txnId}`).then(r => r.data)
