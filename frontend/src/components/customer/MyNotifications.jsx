import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { getNotificationsByUser, markAsRead } from '../../services/notificationService'
import LoadingSpinner from '../common/LoadingSpinner'

// --- UI Helpers ---
const categoryColor = (cat) => {
  const colors = { Balance: 'badge-blue', Interest: 'badge-green', Maturity: 'badge-yellow', SI: 'badge-red' }
  return colors[cat] || 'badge-gray'
}

const categoryIcon = (cat) => {
  const icons = { Balance: '💰', Interest: '📈', Maturity: '📅', SI: '🔄' }
  return icons[cat] || '🔔'
}

const FILTERS = [['ALL', 'All'], ['UNREAD', 'Unread'], ['READ', 'Read']]

export default function MyNotifications() {
  const { auth } = useAuth()

  // --- State Management ---
  const [notifications, setNotifications] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // --- Logic: Data Fetching ---
  const fetchAll = async () => {
    setLoading(true)
    try {
      setNotifications(await getNotificationsByUser(auth.userId))
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load notifications')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchAll() }, [auth.userId])

  // --- Logic: Update Status ---

  // Mark single: Uses "Optimistic UI" - we update the local list immediately 
  // so the user sees the change without waiting for the network.
  const handleMarkRead = async (id) => {
    try {
      await markAsRead(id)
      setNotifications(n => n.map(x => x.notificationId === id ? { ...x, status: 'READ' } : x))
    } catch { /* If API fails, we could revert state here */ }
  }

  const handleMarkAllRead = async () => {
    const unread = notifications.filter(n => n.status !== 'READ')
    // Batch update the UI
    setNotifications(all => all.map(n => ({ ...n, status: 'READ' })))
    // Sync with server in background
    for (const n of unread) {
      try { await markAsRead(n.notificationId) } catch { /* silent */ }
    }
  }

  // --- Derived State: Filtering ---
  const filtered = filter === 'ALL'
    ? notifications
    : filter === 'UNREAD'
      ? notifications.filter(n => n.status !== 'READ')
      : notifications.filter(n => n.status === 'READ')

  const unreadCount = notifications.filter(n => n.status !== 'READ').length

  if (loading) return <div className="py-8"><LoadingSpinner /></div>

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="page-title">Notifications</h1>
          {unreadCount > 0 && (
            <p className="text-sm text-brand-600 mt-1 font-medium">
              {unreadCount} unread message{unreadCount !== 1 ? 's' : ''}
            </p>
          )}
        </div>
        {unreadCount > 0 && (
          <button className="btn-secondary text-sm" onClick={handleMarkAllRead}>
            Mark all as read
          </button>
        )}
      </div>

      {error && <div className="alert-error mb-4">{error}</div>}

      {/* Filter Tabs */}
      <div className="flex gap-2 mb-6">
        {FILTERS.map(([val, label]) => (
          <button key={val} onClick={() => setFilter(val)}
            className={`px-4 py-1.5 rounded-lg text-sm font-medium transition-colors ${filter === val ? 'bg-brand-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}>
            {label}
            {val === 'UNREAD' && unreadCount > 0 && (
              <span className="ml-1.5 bg-white text-brand-600 text-xs font-bold px-1.5 py-0.5 rounded-full">
                {unreadCount}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Notification List */}
      <div className="space-y-3">
        {filtered.length === 0 ? (
          <div className="card text-center py-16 text-gray-400">
            <p className="text-4xl mb-3">🔔</p>
            <p>No notifications found</p>
          </div>
        ) : filtered.map(n => {
          const isUnread = n.status !== 'READ'
          return (
            <div key={n.notificationId}
              className={`card flex items-start justify-between gap-4 transition-colors ${isUnread ? 'border-l-4 border-l-brand-500 bg-brand-50' : ''
                }`}>
              <div className="flex items-start gap-3 flex-1 min-w-0">
                <div className={`w-9 h-9 rounded-lg flex items-center justify-center text-lg flex-shrink-0 ${isUnread ? 'bg-brand-100' : 'bg-gray-100'
                  }`}>
                  {categoryIcon(n.category)}
                </div>
                <div className="flex-1 min-w-0">
                  <p className={`text-sm ${isUnread ? 'font-semibold text-gray-900' : 'text-gray-700'}`}>
                    {n.message}
                  </p>
                  <div className="flex items-center gap-2 mt-1.5 flex-wrap">
                    <span className={categoryColor(n.category)}>{n.category}</span>
                    <span className="text-xs text-gray-400">{n.createdDate?.substring(0, 16).replace('T', ' ')}</span>
                  </div>
                </div>
              </div>
              {isUnread && (
                <button className="btn-secondary text-xs py-1 px-2 shrink-0"
                  onClick={() => handleMarkRead(n.notificationId)}>
                  Mark read
                </button>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}