import api from './api'

export const getAllProducts = () =>
  api.get('/v1/products').then(r => r.data)

export const getProductById = (id) =>
  api.get(`/v1/products/${id}`).then(r => r.data)

export const createProduct = (data) =>
  api.post('/v1/products', data).then(r => r.data)

export const updateProduct = (id, data) =>
  api.put(`/v1/products/${id}`, data).then(r => r.data)

export const deleteProduct = (id) =>
  api.delete(`/v1/products/${id}`).then(r => r.data)

export const simulateProduct = (id, amount, tenure) =>
  api.get(`/v1/products/${id}/simulate`, { params: { amount, tenure } }).then(r => r.data)
