import api from './api'

export const login = (email, password) =>
  api.post('/v1/auth/login', { email, password }).then(r => r.data)

export const signup = (data) =>
  api.post('/v1/auth/signup', {
    name:     data.name,
    email:    data.email,
    phone:    data.phone || null,
    password: data.password,
  }).then(r => r.data)

export const registerUser = (userDTO) =>
  api.post('/v1/auth/register', userDTO).then(r => r.data)
