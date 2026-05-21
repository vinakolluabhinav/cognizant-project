import React, { createContext, useContext, useState, useCallback } from 'react'

// Create the Context object
const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  // --- State Initialization ---
  // We check LocalStorage on startup so the user stays logged in after a page refresh.
  const [auth, setAuth] = useState(() => {
    try {
      const stored = localStorage.getItem('dcx_auth')
      return stored ? JSON.parse(stored) : null
    } catch (err) {
      console.error("Auth hydration failed", err)
      return null
    }
  })

  // --- Logic: Log In ---
  // Stores the JWT token and user metadata (name, role, ID)
  const login = useCallback((data) => {
    // Expected data structure: { token, tokenType, userId, name, role }
    localStorage.setItem('dcx_auth', JSON.stringify(data))
    setAuth(data)
  }, [])

  // --- Logic: Log Out ---
  // Wipes the local storage and resets global state to null
  const logout = useCallback(() => {
    localStorage.removeItem('dcx_auth')
    setAuth(null)
  }, [])

  return (
    <AuthContext.Provider value={{ auth, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// Custom hook for easy access in functional components
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}