import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { login as loginAPI } from '../../services/authService'

// --- Configuration: Where to send users after they log in ---
const ROLE_HOME = {
  CUSTOMER: '/customer',
  BRANCH_OFFICER: '/branch',
  OPERATIONS_OFFICER: '/operations',
  FINANCE_ANALYST: '/finance',
  CORE_ADMIN: '/admin',
}

// --- Test Convenience: Demo logins for development ---
const DEMO_CREDENTIALS = {
  CUSTOMER: { email: 'customer1@bank.com', hint: 'Customer' },
  BRANCH_OFFICER: { email: 'branch1@bank.com', hint: 'Branch Officer' },
  OPERATIONS_OFFICER: { email: 'ops1@bank.com', hint: 'Operations Officer' },
  FINANCE_ANALYST: { email: 'finance1@bank.com', hint: 'Finance Analyst' },
  CORE_ADMIN: { email: 'admin@bank.com', hint: 'Core Admin' },
}

export default function LoginPage() {
  // --- State Management ---
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [selectedRole, setSelectedRole] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const { login } = useAuth() // Global login function from context
  const navigate = useNavigate()

  // --- Logic: Quick-select demo roles ---
  const handleRoleSelect = (role) => {
    setSelectedRole(role)
    setEmail(DEMO_CREDENTIALS[role].email)
    setPassword('')
    setError('')
  }

  // --- Logic: Submit Login Request ---
  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!email || !password) {
      setError('Email and password are required')
      return
    }

    setLoading(true)
    setError('')

    try {
      // 1. Call the Backend API
      const data = await loginAPI(email, password)

      // 2. Save the session globally (Context + LocalStorage)
      login(data)

      // 3. Redirect to the dashboard based on the user's role
      navigate(ROLE_HOME[data.role] || '/', { replace: true })

    } catch (err) {
      // Logic: Smart Error Parsing
      const msg = err.response?.data?.message || err.response?.data || ''
      if (typeof msg === 'string' && msg.toLowerCase().includes('inactive')) {
        setError('Your account has been deactivated. Please contact the administrator.')
      } else if (typeof msg === 'string' && msg.toLowerCase().includes('invalid')) {
        setError('Incorrect email or password. Please try again.')
      } else {
        setError(typeof msg === 'string' && msg ? msg : 'Login failed. Please try again.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-brand-900 via-brand-700 to-brand-500 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Branding Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-white">DepositCoreX</h1>
          <p className="text-brand-100 mt-1 text-sm">Core Banking Deposits Platform</p>
        </div>

        <div className="bg-white rounded-2xl shadow-2xl p-8">
          <h2 className="text-xl font-semibold text-gray-800 mb-6">Sign in to your account</h2>

          {/* Role quick-select buttons */}
          <div className="mb-6">
            <p className="label">Select role (test convenience)</p>
            <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
              {Object.entries(DEMO_CREDENTIALS).map(([role, { hint }]) => (
                <button
                  key={role}
                  type="button"
                  onClick={() => handleRoleSelect(role)}
                  className={`text-xs px-2 py-1.5 rounded-lg border font-medium transition-colors ${selectedRole === role
                      ? 'bg-brand-600 text-white border-brand-600'
                      : 'bg-white text-gray-600 border-gray-300 hover:border-brand-400'
                    }`}
                >
                  {hint}
                </button>
              ))}
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Email Field */}
            <div>
              <label className="label">Email</label>
              <input
                type="email"
                className="input"
                value={email}
                onChange={e => { setEmail(e.target.value); setError('') }}
                placeholder="Enter your email"
                autoComplete="email"
              />
            </div>

            {/* Password Field with Show/Hide Toggle */}
            <div>
              <label className="label">Password</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  className="input pr-11"
                  value={password}
                  onChange={e => { setPassword(e.target.value); setError('') }}
                  placeholder="Enter password"
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(s => !s)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
                  tabIndex={-1}
                >
                  {showPassword ? (
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </svg>
                  ) : (
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            {/* Specialized Error Messaging */}
            {error && (
              <div className={`rounded-lg px-4 py-3 text-sm flex items-start gap-2 ${error.includes('deactivated')
                  ? 'bg-amber-50 border border-amber-200 text-amber-800'
                  : 'alert-error'
                }`}>
                <span>{error.includes('deactivated') ? '🔒' : '⚠️'}</span>
                <span>{error}</span>
              </div>
            )}

            <button type="submit" disabled={loading} className="btn-primary w-full py-2.5 mt-2">
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>

          <p className="text-center text-sm text-gray-500 mt-6">
            New customer?{' '}
            <Link to="/register" className="text-brand-600 font-medium hover:underline">
              Create account
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}