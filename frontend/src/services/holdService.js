import api from './api'

export const placeHold = (data) =>
  api.post('/v1/holds/place', data).then(r => r.data)

export const releaseHold = (holdId) =>
  api.post(`/v1/holds/release/${holdId}`).then(r => r.data)

export const getAvailableBalance = (accountId) =>
  api.get(`/v1/holds/balance/${accountId}`).then(r => r.data)

export const createStandingInstruction = (data) =>
  api.post('/v1/standing-instructions', data).then(r => r.data)
