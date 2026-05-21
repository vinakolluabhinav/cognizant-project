import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

// --- Configuration: Navigation links with icons for each user role ---
const NAV = {
  CUSTOMER: [
    { to: '/customer',               label: 'Dashboard',      icon: '🏠' },
    { to: '/customer/accounts',      label: 'My Accounts',    icon: '💳' },
    { to: '/customer/transactions',  label: 'Transactions',   icon: '↔️' },
    { to: '/customer/statements',    label: 'Statements',     icon: '📄' },
    { to: '/customer/notifications', label: 'Notifications',  icon: '🔔' },
  ],
  BRANCH_OFFICER: [
    { to: '/branch',              label: 'Dashboard',           icon: '🏠' },
    { to: '/branch/onboard',      label: 'Onboard Customer',    icon: '👤' },
    { to: '/branch/open-account', label: 'Open Account',        icon: '🏦' },
    { to: '/branch/transaction',  label: 'Post Transaction',    icon: '💸' },
    { to: '/branch/holds',        label: 'Holds & SI',          icon: '🔒' },
    { to: '/branch/td-closure',   label: 'TD Premature Closure',icon: '📋' },
    { to: '/branch/statements',   label: 'Statements',          icon: '📄' },
  ],
  OPERATIONS_OFFICER: [
    { to: '/operations',                  label: 'Dashboard',      icon: '🏠' },
    { to: '/operations/transactions',     label: 'All Transactions',icon: '↔️' },
    { to: '/operations/gl',               label: 'GL Postings',    icon: '📒' },
    { to: '/operations/interest',         label: 'Interest',       icon: '📈' },
    { to: '/operations/td',               label: 'TD Maturity',    icon: '⏰' },
    { to: '/operations/holds',            label: 'Holds & SI',     icon: '🔒' },
    { to: '/operations/statements',       label: 'Statements',     icon: '📄' },
    { to: '/operations/reports',          label: 'Reports',        icon: '📊' },
    { to: '/operations/notifications',    label: 'Notifications',  icon: '🔔' },
  ],
  FINANCE_ANALYST: [
    { to: '/finance',              label: 'Dashboard',    icon: '🏠' },
    { to: '/finance/transactions', label: 'Transactions', icon: '↔️' },
    { to: '/finance/gl',           label: 'GL Postings',  icon: '📒' },
    { to: '/finance/interest',     label: 'Interest',     icon: '📈' },
    { to: '/finance/statements',   label: 'Statements',   icon: '📄' },
    { to: '/finance/reports',      label: 'Reports',      icon: '📊' },
    { to: '/finance/products',     label: 'Products',     icon: '🏷️' },
  ],
  CORE_ADMIN: [
    { to: '/admin',               label: 'Dashboard',       icon: '🏠' },
    { to: '/admin/users',         label: 'User Management', icon: '👥' },
    { to: '/admin/customers',     label: 'Customers',       icon: '👤' },
    { to: '/admin/accounts',      label: 'Accounts',        icon: '🏦' },
    { to: '/admin/products',      label: 'Products',        icon: '🏷️' },
    { to: '/admin/transactions',  label: 'Transactions',    icon: '↔️' },
    { to: '/admin/gl',            label: 'GL Postings',     icon: '📒' },
    { to: '/admin/interest',      label: 'Interest',        icon: '📈' },
    { to: '/admin/td',            label: 'TD Servicing',    icon: '⏰' },
    { to: '/admin/holds',         label: 'Holds & SI',      icon: '🔒' },
    { to: '/admin/statements',    label: 'Statements',      icon: '📄' },
    { to: '/admin/reports',       label: 'Reports',         icon: '📊' },
    { to: '/admin/notifications', label: 'Notifications',   icon: '🔔' },
  ],
}

// User-friendly labels for the roles
const ROLE_LABEL = {
  CUSTOMER:           'Customer',
  BRANCH_OFFICER:     'Branch Officer',
  OPERATIONS_OFFICER: 'Operations Officer',
  FINANCE_ANALYST:    'Finance Analyst',
  CORE_ADMIN:         'Core Admin',
}

// Role avatar icons shown in the profile section
const ROLE_ICON = {
  CUSTOMER:           '👤',
  BRANCH_OFFICER:     '🏢',
  OPERATIONS_OFFICER: '⚙️',
  FINANCE_ANALYST:    '📊',
  CORE_ADMIN:         '🛡️',
}

export default function Sidebar() {
  const { auth, logout } = useAuth()
  const navigate = useNavigate()

  // --- Logic: Pick the right link set based on user role ---
  const items = NAV[auth?.role] || []

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <aside className="w-64 h-screen sticky top-0 bg-brand-900 flex flex-col flex-shrink-0">
      {/* Platform Branding */}
      <div className="px-6 py-5 border-b border-white/10 flex items-center gap-3">
        <div className="w-9 h-9 bg-brand-600 rounded-xl flex items-center justify-center text-white font-bold text-base flex-shrink-0">
          D
        </div>
        <div>
          <h1 className="text-white font-bold text-base leading-tight">DepositCoreX</h1>
          <p className="text-brand-200 text-xs">Core Banking Platform</p>
        </div>
      </div>

      {/* Profile Section */}
      <div className="px-4 py-3 border-b border-white/10 flex items-center gap-3">
        <div className="w-9 h-9 bg-brand-700 rounded-full flex items-center justify-center text-lg flex-shrink-0">
          {ROLE_ICON[auth?.role] || '👤'}
        </div>
        <div className="min-w-0">
          <p className="text-white text-sm font-medium truncate">{auth?.name}</p>
          <span className="text-brand-200 text-xs">{ROLE_LABEL[auth?.role] || auth?.role}</span>
        </div>
      </div>

      {/* Navigation Menu */}
      <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto scrollbar-hide">
        {items.map(({ to, label, icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to.split('/').length === 2}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors ${
                isActive
                  ? 'bg-brand-600 text-white font-medium'
                  : 'text-brand-100 hover:bg-white/10 hover:text-white'
              }`
            }
          >
            <span className="text-base leading-none">{icon}</span>
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>

      {/* Logout Footer */}
      <div className="px-4 py-4 border-t border-white/10">
        <button
          onClick={handleLogout}
          className="w-full text-left text-sm text-red-400 hover:text-white hover:bg-red-600/30 transition-colors px-3 py-2 rounded-lg font-medium flex items-center gap-3"
        >
          <span className="text-base">⏻</span>
          <span>Sign out</span>
        </button>
      </div>
    </aside>
  )
}
