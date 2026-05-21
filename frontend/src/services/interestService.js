import api from './api'

export const accrueInterest = (data) =>
  api.post('/v1/interest/accrue', data).then(r => r.data)

export const postInterest = (accountId, postingType) =>
  api.post('/v1/interest/post', null, { params: { accountId, postingType } }).then(r => r.data)
