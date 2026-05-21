import api from './api'

export const generateStatement = (accountId, periodStart, periodEnd) =>
  api.post('/v1/statements/generate', null, { params: { accountId, periodStart, periodEnd } }).then(r => r.data)

export const getStatementsByAccount = (accountId) =>
  api.get(`/v1/statements/account/${accountId}`).then(r => r.data)

export const getStatementById = (id) =>
  api.get(`/v1/statements/${id}`).then(r => r.data)

export const generateReport = (scope) =>
  api.post('/v1/statements/reports/generate', null, { params: { scope } }).then(r => r.data)

export const getAllReports = () =>
  api.get('/v1/statements/reports').then(r => r.data)

export const getReportById = (id) =>
  api.get(`/v1/statements/reports/${id}`).then(r => r.data)
