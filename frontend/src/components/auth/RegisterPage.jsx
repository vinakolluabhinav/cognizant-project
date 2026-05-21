import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { signup } from '../../services/authService'

// Default values for a blank registration form
const INIT = { name: '', email: '', phone: '', password: '', confirmPassword: '' }

// UI Helper: Reusable component for the show/hide password eye icon
const EyeIcon = ({ open }) => open ? (
  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
  </svg>
) : (
  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
  </svg>
)

export default function RegisterPage() {
  // --- State Management ---
  const [form, setForm] = useState(INIT)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const navigate = useNavigate()

  // Helper to update specific form fields
  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  // --- Logic: Handle Registration Submission ---
  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    // 1. Client-Side Validation
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match')
      return
    }
    if (form.password.length < 6) {
      setError('Password must be at least 6 characters')
      return
    }

    setLoading(true)

    try {
      // 2. Call the Signup Service
      // (Note: confirmPassword is excluded as the backend doesn't need it)
      await signup({
        name: form.name,
        email: form.email,
        phone: form.phone,
        password: form.password
      })

      // 3. Success Feedback and Automatic Redirect
      setSuccess('Account created successfully! Redirecting to login…')
      setTimeout(() => navigate('/login'), 2000)

    } catch (err) {
      // 4. Smart Error Parsing
      const msg = err.response?.data?.message || err.response?.data || ''
      if (typeof msg === 'string' && msg.toLowerCase().includes('already')) {
        setError('This email is already registered. Please sign in instead.')
      } else {
        setError(typeof msg === 'string' && msg ? msg : 'Registration failed. Please try again.')
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
          <h2 className="text-xl font-semibold text-gray-800 mb-2">Create your account</h2>
          <p className="text-sm text-gray-500 mb-6">
            Self-registration creates a <span className="font-medium text-brand-600">Customer</span> account.
          </p>

          {/* Conditional View: Show Success Message OR Registration Form */}
          {success ? (
            <div className="alert-success text-center py-4">
              <p className="text-xl mb-1">✓</p>
              <p className="font-medium">{success}</p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Personal Info Inputs */}
              <div>
                <label className="label">Full Name</label>
                <input className="input" value={form.name} onChange={e => set('name', e.target.value)} placeholder="John Doe" required />
              </div>
              <div>
                <label className="label">Email</label>
                <input type="email" className="input" value={form.email} onChange={e => set('email', e.target.value)} placeholder="john@example.com" required />
              </div>
              <div>
                <label className="label">Phone <span className="text-gray-400 font-normal">(optional)</span></label>
                <input className="input" value={form.phone} onChange={e => set('phone', e.target.value)} placeholder="+91 98765 43210" />
              </div>

              {/* Password Input with Toggle */}
              <div>
                <label className="label">Password</label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    className="input pr-11"
                    value={form.password}
                    onChange={e => set('password', e.target.value)}
                    placeholder="Min. 6 characters" required
                  />
                  <button type="button" tabIndex={-1} onClick={() => setShowPassword(s => !s)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                    <EyeIcon open={showPassword} />
                  </button>
                </div>
              </div>

              {/* Confirm Password with Live Match Indicator */}
              <div>
                <label className="label">Confirm Password</label>
                <div className="relative">
                  <input
                    type={showConfirm ? 'text' : 'password'}
                    className="input pr-11"
                    value={form.confirmPassword}
                    onChange={e => set('confirmPassword', e.target.value)}
                    placeholder="Re-enter password" required
                  />
                  <button type="button" tabIndex={-1} onClick={() => setShowConfirm(s => !s)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                    <EyeIcon open={showConfirm} />
                  </button>
                </div>
                {/* Visual Cue: Do passwords match? */}
                {form.confirmPassword && (
                  <p className={`text-xs mt-1 ${form.password === form.confirmPassword ? 'text-green-600' : 'text-red-500'}`}>
                    {form.password === form.confirmPassword ? '✓ Passwords match' : '✗ Passwords do not match'}
                  </p>
                )}
              </div>

              {/* Non-editable role badge for clarity */}
              <div className="flex items-center gap-2 p-3 bg-brand-50 rounded-lg border border-brand-200">
                <span className="text-sm text-gray-600">Account type:</span>
                <span className="badge-blue">CUSTOMER</span>
              </div>

              {error && <div className="alert-error">{error}</div>}

              <button type="submit" disabled={loading} className="btn-primary w-full py-2.5">
                {loading ? 'Creating account…' : 'Create account'}
              </button>
            </form>
          )}

          <p className="text-center text-sm text-gray-500 mt-6">
            Already have an account?{' '}
            <Link to="/login" className="text-brand-600 font-medium hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}