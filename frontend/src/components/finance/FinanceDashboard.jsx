import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

// --- Configuration: Finance Analyst feature cards with icons ---
const cards = [
  { to: '/finance/transactions', title: 'Transactions', desc: 'View all transaction records',    icon: '↔️', bg: 'bg-blue-50',   iconBg: 'bg-blue-500',   border: 'border-blue-100' },
  { to: '/finance/gl',           title: 'GL Postings',  desc: 'General ledger entries',          icon: '📒', bg: 'bg-indigo-50', iconBg: 'bg-indigo-500', border: 'border-indigo-100' },
  { to: '/finance/interest',     title: 'Interest',     desc: 'Accrue and review interest',      icon: '📈', bg: 'bg-violet-50', iconBg: 'bg-violet-500', border: 'border-violet-100' },
  { to: '/finance/statements',   title: 'Statements',   desc: 'Account statement generation',    icon: '📄', bg: 'bg-purple-50', iconBg: 'bg-purple-500', border: 'border-purple-100' },
  { to: '/finance/reports',      title: 'Reports',      desc: 'Deposit portfolio reports',       icon: '📊', bg: 'bg-pink-50',   iconBg: 'bg-pink-500',   border: 'border-pink-100' },
  { to: '/finance/products',     title: 'Products',     desc: 'Configure deposit products',      icon: '🏷️', bg: 'bg-rose-50',   iconBg: 'bg-rose-500',   border: 'border-rose-100' },
]

export default function FinanceDashboard() {
  const { auth } = useAuth()

  return (
    <div>
      <div className="mb-8">
        <h1 className="page-title">Finance Analyst Dashboard 📊</h1>
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
