import { useEffect, useState } from 'react'
import { sendNotification, getPendingNotifications } from '../../services/notificationService'
import LoadingSpinner from '../common/LoadingSpinner'

// --- Default Form State ---
const INIT = { userId: '', channel: 'EMAIL', message: '', category: 'Balance' }

export default function NotificationsManagement() {
  // --- Form States (The "Sender" Side) ---
  const [form, setForm] = useState(INIT)
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')

  // --- List States (The "Monitor" Side) ---
  const [pending, setPending] = useState([])
  const [listLoading, setListLoading] = useState(true)

  // Load the "Monitor" list as soon as the page opens
  useEffect(() => { fetchPending() }, [])

  // --- Logic: Fetch Unread Alerts ---
  const fetchPending = async () => {
    setListLoading(true)
    try {
      setPending(await getPendingNotifications())
    } catch {
      /* Silent catch: The list simply remains empty if the API is down */
    } finally {
      setListLoading(false)
    }
  }

  // --- Logic: Send Manual Notification ---
  const handleSend = async (e) => {
    e.preventDefault()
    setLoading(true); setMsg(''); setError('')
    try {
      // 1. Send the data to the notification service
      const r = await sendNotification({ ...form, userId: parseInt(form.userId) })

      // 2. Provide feedback to the staff member
      setMsg(`Notification #${r.notificationId} sent successfully to User ${form.userId}`)

      // 3. Reset the form and refresh the monitor list to show the new pending message
      setForm(INIT)
      fetchPending()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send notification')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1 className="page-title mb-6">Notifications</h1>

      {/* Two-Column Layout: Left for Sending, Right for Monitoring */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* --- PANEL: Send Notification --- */}
        <div className="card">
          <h2 className="section-title">Send Notification</h2>
          <form onSubmit={handleSend} className="space-y-3">
            <div>
              <label className="label">User ID</label>
              <input className="input" type="number" value={form.userId} onChange={e => setForm(f => ({ ...f, userId: e.target.value }))} required />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Category</label>
                <select className="input" value={form.category} onChange={e => setForm(f => ({ ...f, category: e.target.value }))}>
                  <option>Balance</option><option>Interest</option><option>Maturity</option><option>SI</option>
                </select>
              </div>
              <div>
                <label className="label">Channel</label>
                <select className="input" value={form.channel} onChange={e => setForm(f => ({ ...f, channel: e.target.value }))}>
                  <option>EMAIL</option><option>SMS</option><option>IN_APP</option>
                </select>
              </div>
            </div>
            <div>
              <label className="label">Message</label>
              <textarea className="input h-24 resize-none" value={form.message} onChange={e => setForm(f => ({ ...f, message: e.target.value }))} placeholder="Enter notification message…" required />
            </div>
            <button className="btn-primary w-full" type="submit" disabled={loading}>
              {loading ? 'Sending…' : 'Send Notification'}
            </button>
          </form>
          {msg && <div className="alert-success mt-3">{msg}</div>}
          {error && <div className="alert-error mt-3">{error}</div>}
        </div>

        {/* --- PANEL: Pending Notifications (Queue Monitor) --- */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h2 className="section-title mb-0">Unread Queue</h2>
            <button className="btn-secondary text-sm" onClick={fetchPending}>Refresh</button>
          </div>
          {listLoading ? <LoadingSpinner /> : (
            <div className="space-y-2 max-h-96 overflow-y-auto scrollbar-hide">
              {pending.length === 0 ? (
                <p className="text-gray-400 text-sm text-center py-8">No unread notifications in queue</p>
              ) : (
                pending.map(n => (
                  <div key={n.notificationId} className="p-3 rounded-lg bg-gray-50 border border-gray-200 text-sm">
                    <p className="font-medium text-gray-800">{n.message}</p>
                    <p className="text-xs text-gray-400 mt-1">
                      Target User: {n.userId} · via {n.channel} · {n.createdDate?.substring(0, 16).replace('T', ' ')}
                    </p>
                  </div>
                ))
              )}
            </div>
          )}
        </div>

      </div>
    </div>
  )
}