import { useEffect, useState } from 'react'
import { getAllUsers, deactivateUser, activateUser, getAuditLogs } from '../../services/userService'
import { registerUser } from '../../services/authService'
import LoadingSpinner from '../common/LoadingSpinner'

// Default values for the registration form
const INIT = { name: '', email: '', phone: '', password: '', role: 'CUSTOMER' }

export default function UserManagement() {
  // --- State Management ---
  const [form, setForm] = useState(INIT)
  const [saving, setSaving] = useState(false)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')

  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)

  const [logs, setLogs] = useState(null)
  const [selectedUserId, setSelectedUserId] = useState(null)

  // Load all users immediately on page load
  useEffect(() => { fetchAll() }, [])

  // --- Logic: Data Fetching ---
  const fetchAll = async () => {
    setLoading(true)
    try {
      setUsers(await getAllUsers())
    } catch {
      setError('Failed to load users')
    } finally {
      setLoading(false)
    }
  }

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  // --- Logic: Admin Actions ---

  // 1. Create User
  const handleCreate = async (e) => {
    e.preventDefault()
    setSaving(true); setMsg(''); setError('')
    try {
      await registerUser(form)
      setMsg(`User "${form.name}" registered successfully.`)
      setForm(INIT)
      fetchAll() // Refresh list to show new user
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed')
    } finally {
      setSaving(false)
    }
  }

  // 2. Block Login (Deactivate)
  const handleDeactivate = async (id) => {
    if (!window.confirm('Deactivate this user? They will be unable to log in.')) return
    try {
      await deactivateUser(id)
      setMsg(`User #${id} deactivated`)
      fetchAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to deactivate')
    }
  }

  // 3. Unblock Login (Activate)
  const handleActivate = async (id) => {
    if (!window.confirm('Activate this user?')) return
    try {
      await activateUser(id)
      setMsg(`User #${id} activated`)
      fetchAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to activate')
    }
  }

  // 4. View Security Audit Logs
  const handleLogs = async (id) => {
    setSelectedUserId(id)
    try {
      setLogs(await getAuditLogs(id))
    } catch {
      setError('Failed to load audit logs')
    }
  }

  return (
    <div>
      <h1 className="page-title mb-6">User Management</h1>

      {/* MODULE 1: Registration Form */}
      <div className="card mb-6">
        <h2 className="section-title">Register New User</h2>
        <form onSubmit={handleCreate} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <div><label className="label">Full Name *</label>
            <input className="input" value={form.name} onChange={e => set('name', e.target.value)} required /></div>
          <div><label className="label">Email *</label>
            <input className="input" type="email" value={form.email} onChange={e => set('email', e.target.value)} required /></div>
          <div><label className="label">Phone</label>
            <input className="input" value={form.phone} onChange={e => set('phone', e.target.value)} /></div>
          <div><label className="label">Password *</label>
            <input className="input" type="password" value={form.password} onChange={e => set('password', e.target.value)} required /></div>
          <div><label className="label">Role</label>
            <select className="input" value={form.role} onChange={e => set('role', e.target.value)}>
              <option>CUSTOMER</option>
              <option>BRANCH_OFFICER</option>
              <option>OPERATIONS_OFFICER</option>
              <option>FINANCE_ANALYST</option>
              <option>CORE_ADMIN</option>
            </select></div>
          <div className="lg:col-span-3 sm:col-span-2">
            <button className="btn-primary" type="submit" disabled={saving}>{saving ? 'Registering…' : 'Register User'}</button>
          </div>
        </form>
        {msg && <div className="alert-success mt-3">{msg}</div>}
        {error && <div className="alert-error mt-3">{error}</div>}
      </div>

      {/* MODULE 2: User Directory */}
      <div className="card mb-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="section-title mb-0">All Users</h2>
          <button className="btn-secondary text-sm" onClick={fetchAll}>Refresh</button>
        </div>
        {loading ? <LoadingSpinner /> : (
          <div className="table-wrap">
            <table className="table">
              <thead><tr>{['ID', 'Name', 'Email', 'Phone', 'Role', 'Status', 'Actions'].map(h => <th key={h} className="th">{h}</th>)}</tr></thead>
              <tbody className="divide-y divide-gray-100">
                {users.map(u => (
                  <tr key={u.userId} className="tr-hover">
                    <td className="td text-xs">{u.userId}</td>
                    <td className="td font-medium">{u.name}</td>
                    <td className="td text-sm">{u.email}</td>
                    <td className="td text-sm">{u.phone || '—'}</td>
                    <td className="td"><span className="badge-blue">{u.role}</span></td>
                    <td className="td">
                      <span className={u.active ? 'badge-green' : 'badge-red'}>{u.active ? 'Active' : 'Inactive'}</span>
                    </td>
                    <td className="td space-x-2">
                      <button className="text-xs text-brand-600 hover:underline" onClick={() => handleLogs(u.userId)}>Logs</button>
                      {u.active
                        ? <button className="text-xs text-red-600 hover:underline" onClick={() => handleDeactivate(u.userId)}>Deactivate</button>
                        : <button className="text-xs text-green-600 hover:underline" onClick={() => handleActivate(u.userId)}>Activate</button>
                      }
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* MODULE 3: Audit Trails (Conditional) */}
      {logs && (
        <div className="card">
          <h2 className="section-title">Audit Logs — User #{selectedUserId}</h2>
          <div className="table-wrap">
            <table className="table">
              <thead><tr>{['ID', 'Action', 'Description', 'Timestamp'].map(h => <th key={h} className="th">{h}</th>)}</tr></thead>
              <tbody className="divide-y divide-gray-100">
                {logs.map(l => (
                  <tr key={l.logId} className="tr-hover">
                    <td className="td font-mono text-xs">{l.logId}</td>
                    <td className="td font-medium">{l.action}</td>
                    <td className="td text-sm">{l.description}</td>
                    <td className="td text-xs">{l.timestamp?.substring(0, 16).replace('T', ' ')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}