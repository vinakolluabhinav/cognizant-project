import api from './api'

export const processMaturity = (tdId, action = 'PAYOUT') =>
  api.post(`/v1/td/${tdId}/maturity`, null, { params: { action } }).then(r => r.data)

export const closePremature = (tdId, penalRate = 1.0) =>
  api.post(`/v1/td/${tdId}/premature-closure`, null, { params: { penalRate } }).then(r => r.data)

export const getMaturity = (tdId) =>
  api.get(`/v1/td/${tdId}/maturity`).then(r => r.data)

export const getClosure = (tdId) =>
  api.get(`/v1/td/${tdId}/closure`).then(r => r.data)
