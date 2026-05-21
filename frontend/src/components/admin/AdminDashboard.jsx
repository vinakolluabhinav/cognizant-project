import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

// --- Configuration: Core Admin full system access cards ---
const cards = [
  { to: '/admin/users',         title: 'User Management', desc: 'Manage users and access roles',       icon: '👥', bg: 'bg-slate-50',   iconBg: 'bg-slate-600',   border: 'border-slate-100' },
  { to: '/admin/customers',     title: 'Customers',       desc: 'View all customer records',           icon: '👤', bg: 'bg-blue-50',    iconBg: 'bg-blue-600',    border: 'border-blue-100' },
  { to: '/admin/accounts',      title: 'Accounts',        desc: 'Account lookup and management',       icon: '🏦', bg: 'bg-indigo-50',  iconBg: 'bg-indigo-600',  border: 'border-indigo-100' },
  { to: '/admin/products',      title: 'Products',        desc: 'Configure deposit products',          icon: '🏷️', bg: 'bg-violet-50',  iconBg: 'bg-violet-600',  border: 'border-violet-100' },
  { to: '/admin/transactions',  title: 'Transactions',    desc: 'All transactions & reversals',        icon: '↔️', bg: 'bg-purple-50',  iconBg: 'bg-purple-600',  border: 'border-purple-100' },
  { to: '/admin/gl',            title: 'GL Postings',     desc: 'General ledger entries',              icon: '📒', bg: 'bg-pink-50',    iconBg: 'bg-pink-600',    border: 'border-pink-100' },
  { to: '/admin/interest',      title: 'Interest',        desc: 'Accrue and post interest',            icon: '📈', bg: 'bg-rose-50',    iconBg: 'bg-rose-600',    border: 'border-rose-100' },
  { to: '/admin/td',            title: 'TD Servicing',    desc: 'Maturity & premature closure',        icon: '⏰', bg: 'bg-orange-50',  iconBg: 'bg-orange-600',  border: 'border-orange-100' },
  { to: '/admin/holds',         title: 'Holds & SI',      desc: 'Holds and standing instructions',     icon: '🔒', bg: 'bg-amber-50',   iconBg: 'bg-amber-600',   border: 'border-amber-100' },
  { to: '/admin/statements',    title: 'Statements',      desc: 'Account statements',                  icon: '📄', bg: 'bg-yellow-50',  iconBg: 'bg-yellow-600',  border: 'border-yellow-100' },
  { to: '/admin/reports',       title: 'Reports',         desc: 'Deposit portfolio reports',           icon: '📊', bg: 'bg-lime-50',    iconBg: 'bg-lime-600',    border: 'border-lime-100' },
  { to: '/admin/notifications', title: 'Notifications',   desc: 'Send & manage notifications',         icon: '🔔', bg: 'bg-green-50',   iconBg: 'bg-green-600',   border: 'border-green-100' },
]

export default function AdminDashboard() {
  const { auth } = useAuth()

  return (
    <div>
      <div className="mb-8">
        <h1 className="page-title">Core Admin Dashboard 🛡️</h1>
        <p className="text-gray-500 mt-1">Full system access — Welcome, {auth.name}</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map(c => (
          <Link key={c.to} to={c.to}
            className={`card hover:shadow-md transition-all group border ${c.border} ${c.bg}`}>
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
