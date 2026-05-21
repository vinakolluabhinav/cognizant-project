import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

// --- Configuration: Branch Officer feature cards with icons ---
const cards = [
  { to: '/branch/onboard',      title: 'Onboard Customer',     desc: 'Sync new customer from core system',    icon: '👤', bg: 'bg-blue-50',    iconBg: 'bg-blue-500',    border: 'border-blue-100' },
  { to: '/branch/open-account', title: 'Open Account',         desc: 'Open CASA or Term Deposit account',     icon: '🏦', bg: 'bg-indigo-50',  iconBg: 'bg-indigo-500',  border: 'border-indigo-100' },
  { to: '/branch/transaction',  title: 'Post Transaction',     desc: 'Debit / credit a customer account',     icon: '💸', bg: 'bg-violet-50',  iconBg: 'bg-violet-500',  border: 'border-violet-100' },
  { to: '/branch/holds',        title: 'Holds & SI',           desc: 'Place holds or set standing instructions', icon: '🔒', bg: 'bg-pink-50', iconBg: 'bg-pink-500',    border: 'border-pink-100' },
  { to: '/branch/td-closure',   title: 'TD Premature Closure', desc: 'Process early term deposit closure',    icon: '📋', bg: 'bg-rose-50',   iconBg: 'bg-rose-500',    border: 'border-rose-100' },
  { to: '/branch/statements',   title: 'Statements',           desc: 'Generate account statements',           icon: '📄', bg: 'bg-orange-50', iconBg: 'bg-orange-500',  border: 'border-orange-100' },
]

export default function BranchDashboard() {
  const { auth } = useAuth()

  return (
    <div>
      <div className="mb-8">
        <h1 className="page-title">Branch Officer Dashboard 🏢</h1>
        <p className="text-gray-500 mt-1">Welcome, {auth.name}</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
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
