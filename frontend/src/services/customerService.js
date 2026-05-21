import api from './api'

export const syncCustomer = (data) =>
  api.post('/v1/customer-reference/sync', data).then(r => r.data)

export const getAllCustomers = () =>
  api.get('/v1/customer-reference/view').then(r => r.data)

export const getCustomerByCif = (cif) =>
  api.get(`/v1/customer-reference/${cif}`).then(r => r.data)

export const updateCustomer = (cif, kycStatus, status) =>
  api.patch(`/v1/customer-reference/${cif}`, { kycStatus, status }).then(r => r.data)
