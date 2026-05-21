import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { getNotificationsByUser } from '../../services/notificationService'

export default function CustomerDashboard() {
  const { auth } = useAuth()
  const [unread, setUnread] = useState(0)

  useEffect(() => {
    getNotificationsByUser(auth.userId)
      .then(n => setUnread(n.filter(x => x.status !== 'READ').length))
      .catch(() => {})
  }, [auth.userId])

  // --- Configuration: Dashboard cards with icons ---
  const cards = [
    {
      title: 'My Accounts',
      desc:  'View balances & account details',
      to:    '/customer/accounts',
      icon:  '💳',
      bg:    'bg-blue-50',
      iconBg:'bg-blue-500',
      border:'border-blue-100',
    },
    {
      title: 'Transactions',
      desc:  'View your transaction history',
      to:    '/customer/transactions',
      icon:  '↔️',
      bg:    'bg-indigo-50',
      iconBg:'bg-indigo-500',
      border:'border-indigo-100',
    },
    {
      title: 'Statements',
      desc:  'Generate & view statements',
      to:    '/customer/statements',
      icon:  '📄',
      bg:    'bg-violet-50',
      iconBg:'bg-violet-500',
      border:'border-violet-100',
    },
    {
      title: 'Notifications',
      desc:  unread > 0 ? `${unread} unread alert${unread > 1 ? 's' : ''}` : 'All caught up!',
      to:    '/customer/notifications',
      icon:  '🔔',
      bg:    'bg-pink-50',
      iconBg:'bg-pink-500',
      border:'border-pink-100',
      badge: unread > 0 ? unread : null,
    },
  ]

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1 className="page-title">Welcome back, {auth.name} 👋</h1>
        <p className="text-gray-500 mt-1">Here's your banking overview</p>
      </div>

      {/* Dashboard Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map(c => (
          <Link
            key={c.to}
            to={c.to}
            className={`card hover:shadow-md transition-all group border ${c.border} ${c.bg} relative overflow-hidden`}
          >
            {/* Unread badge */}
            {c.badge && (
              <span className="absolute top-3 right-3 bg-red-500 text-white text-xs font-bold w-5 h-5 rounded-full flex items-center justify-center">
                {c.badge}
              </span>
            )}
            {/* Icon */}
            <div className={`w-11 h-11 ${c.iconBg} rounded-xl flex items-center justify-center text-2xl mb-4 shadow-sm`}>
              {c.icon}
            </div>
            <h3 className="font-semibold text-gray-900 group-hover:text-brand-600">{c.title}</h3>
            <p className="text-sm text-gray-500 mt-1">{c.desc}</p>
            <div className="mt-3 text-xs text-brand-600 font-medium group-hover:translate-x-1 transition-transform inline-flex items-center gap-1">
              Open →
            </div>
          </Link>
        ))}
      </div>
    </div>
  )
}
