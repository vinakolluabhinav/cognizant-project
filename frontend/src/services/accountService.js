import api from './api'

export const createCasaAccount = (data) =>
  api.post('/v1/accounts/casa', data).then(r => r.data)

export const createTermDeposit = (data) =>
  api.post('/v1/accounts/term-deposit', data).then(r => r.data)

export const getAccountsByCustomerId = (customerId) =>
  api.get(`/v1/accounts/customer/${customerId}`).then(r => r.data)

export const getAccountById = (id) =>
  api.get(`/v1/accounts/${id}`).then(r => r.data)

export const getTermDepositByAccountId = (accountId) =>
  api.get(`/v1/accounts/term-deposit/${accountId}`).then(r => r.data)

export const getTermDepositByTdId = (tdId) =>
  api.get(`/v1/accounts/term-deposit/by-tdid/${tdId}`).then(r => r.data)
