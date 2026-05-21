import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'

// --- Shared Layout Components ---
import ProtectedRoute from './components/common/ProtectedRoute'
import Layout         from './components/common/Layout'

// --- Auth Pages ---
import LoginPage    from './components/auth/LoginPage'
import RegisterPage from './components/auth/RegisterPage'

// --- Customer ---
import CustomerDashboard from './components/customer/CustomerDashboard'
import MyAccounts        from './components/customer/MyAccounts'
import MyTransactions    from './components/customer/MyTransactions'
import MyStatements      from './components/customer/MyStatements'
import MyNotifications   from './components/customer/MyNotifications'

// --- Branch Officer ---
import BranchDashboard    from './components/branch/BranchDashboard'
import OnboardCustomer    from './components/branch/OnboardCustomer'
import OpenAccount        from './components/branch/OpenAccount'
import PostTransaction    from './components/branch/PostTransaction'
import HoldsAndSI         from './components/branch/HoldsAndSI'
import TDPrematureClosure from './components/branch/TDPrematureClosure'

// --- Operations Officer ---
import OperationsDashboard     from './components/operations/OperationsDashboard'
import AllTransactions         from './components/operations/AllTransactions'
import InterestManagement      from './components/operations/InterestManagement'
import TDMaturity              from './components/operations/TDMaturity'
import GLPostings              from './components/operations/GLPostings'
import ReportsPage             from './components/operations/ReportsPage'
import StaffStatements         from './components/operations/StaffStatements'
import NotificationsManagement from './components/operations/NotificationsManagement'

// --- Finance Analyst ---
import FinanceDashboard from './components/finance/FinanceDashboard'
import ProductsPage     from './components/finance/ProductsPage'

// --- Core Admin ---
import AdminDashboard from './components/admin/AdminDashboard'
import UserManagement from './components/admin/UserManagement'
import AccountsLookup from './components/admin/AccountsLookup'

// --- Configuration: Role → Home Route Mapping ---
const ROLE_HOME = {
  CUSTOMER:           '/customer',
  BRANCH_OFFICER:     '/branch',
  OPERATIONS_OFFICER: '/operations',
  FINANCE_ANALYST:    '/finance',
  CORE_ADMIN:         '/admin',
}

// Redirects "/" to the correct dashboard based on the user's role
function RootRedirect() {
  const { auth } = useAuth()
  if (!auth) return <Navigate to="/login" replace />
  return <Navigate to={ROLE_HOME[auth.role] || '/login'} replace />
}

// Higher-Order Component — enforces role-based access and wraps children in the sidebar Layout
function Wrap({ role, children }) {
  return (
    <ProtectedRoute allowedRoles={Array.isArray(role) ? role : [role]}>
      <Layout>{children}</Layout>
    </ProtectedRoute>
  )
}

export default function App() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/"         element={<RootRedirect />} />
      <Route path="/login"    element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* ── CUSTOMER ── */}
      <Route path="/customer"               element={<Wrap role="CUSTOMER"><CustomerDashboard /></Wrap>} />
      <Route path="/customer/accounts"      element={<Wrap role="CUSTOMER"><MyAccounts /></Wrap>} />
      <Route path="/customer/transactions"  element={<Wrap role="CUSTOMER"><MyTransactions /></Wrap>} />
      <Route path="/customer/statements"    element={<Wrap role="CUSTOMER"><MyStatements /></Wrap>} />
      <Route path="/customer/notifications" element={<Wrap role="CUSTOMER"><MyNotifications /></Wrap>} />

      {/* ── BRANCH OFFICER ── */}
      <Route path="/branch"              element={<Wrap role="BRANCH_OFFICER"><BranchDashboard /></Wrap>} />
      <Route path="/branch/onboard"      element={<Wrap role="BRANCH_OFFICER"><OnboardCustomer /></Wrap>} />
      <Route path="/branch/open-account" element={<Wrap role="BRANCH_OFFICER"><OpenAccount /></Wrap>} />
      <Route path="/branch/transaction"  element={<Wrap role="BRANCH_OFFICER"><PostTransaction /></Wrap>} />
      <Route path="/branch/holds"        element={<Wrap role="BRANCH_OFFICER"><HoldsAndSI /></Wrap>} />
      <Route path="/branch/td-closure"   element={<Wrap role="BRANCH_OFFICER"><TDPrematureClosure /></Wrap>} />
      <Route path="/branch/statements"   element={<Wrap role="BRANCH_OFFICER"><StaffStatements /></Wrap>} />

      {/* ── OPERATIONS OFFICER ── */}
      <Route path="/operations"                  element={<Wrap role="OPERATIONS_OFFICER"><OperationsDashboard /></Wrap>} />
      <Route path="/operations/transactions"     element={<Wrap role="OPERATIONS_OFFICER"><AllTransactions /></Wrap>} />
      <Route path="/operations/gl"               element={<Wrap role="OPERATIONS_OFFICER"><GLPostings /></Wrap>} />
      <Route path="/operations/interest"         element={<Wrap role="OPERATIONS_OFFICER"><InterestManagement /></Wrap>} />
      <Route path="/operations/td"               element={<Wrap role="OPERATIONS_OFFICER"><TDMaturity /></Wrap>} />
      <Route path="/operations/holds"            element={<Wrap role="OPERATIONS_OFFICER"><HoldsAndSI /></Wrap>} />
      <Route path="/operations/statements"       element={<Wrap role="OPERATIONS_OFFICER"><StaffStatements /></Wrap>} />
      <Route path="/operations/reports"          element={<Wrap role="OPERATIONS_OFFICER"><ReportsPage /></Wrap>} />
      <Route path="/operations/notifications"    element={<Wrap role="OPERATIONS_OFFICER"><NotificationsManagement /></Wrap>} />

      {/* ── FINANCE ANALYST ── */}
      <Route path="/finance"              element={<Wrap role="FINANCE_ANALYST"><FinanceDashboard /></Wrap>} />
      <Route path="/finance/transactions" element={<Wrap role="FINANCE_ANALYST"><AllTransactions /></Wrap>} />
      <Route path="/finance/gl"           element={<Wrap role="FINANCE_ANALYST"><GLPostings /></Wrap>} />
      <Route path="/finance/interest"     element={<Wrap role="FINANCE_ANALYST"><InterestManagement /></Wrap>} />
      <Route path="/finance/statements"   element={<Wrap role="FINANCE_ANALYST"><StaffStatements /></Wrap>} />
      <Route path="/finance/reports"      element={<Wrap role="FINANCE_ANALYST"><ReportsPage /></Wrap>} />
      <Route path="/finance/products"     element={<Wrap role="FINANCE_ANALYST"><ProductsPage /></Wrap>} />

      {/* ── CORE ADMIN ── */}
      <Route path="/admin"               element={<Wrap role="CORE_ADMIN"><AdminDashboard /></Wrap>} />
      <Route path="/admin/users"         element={<Wrap role="CORE_ADMIN"><UserManagement /></Wrap>} />
      <Route path="/admin/customers"     element={<Wrap role="CORE_ADMIN"><OnboardCustomer /></Wrap>} />
      <Route path="/admin/accounts"      element={<Wrap role="CORE_ADMIN"><AccountsLookup /></Wrap>} />
      <Route path="/admin/products"      element={<Wrap role="CORE_ADMIN"><ProductsPage /></Wrap>} />
      <Route path="/admin/transactions"  element={<Wrap role="CORE_ADMIN"><AllTransactions /></Wrap>} />
      <Route path="/admin/gl"            element={<Wrap role="CORE_ADMIN"><GLPostings /></Wrap>} />
      <Route path="/admin/interest"      element={<Wrap role="CORE_ADMIN"><InterestManagement /></Wrap>} />
      <Route path="/admin/td"            element={<Wrap role="CORE_ADMIN"><TDMaturity /></Wrap>} />
      <Route path="/admin/holds"         element={<Wrap role="CORE_ADMIN"><HoldsAndSI /></Wrap>} />
      <Route path="/admin/statements"    element={<Wrap role="CORE_ADMIN"><StaffStatements /></Wrap>} />
      <Route path="/admin/reports"       element={<Wrap role="CORE_ADMIN"><ReportsPage /></Wrap>} />
      <Route path="/admin/notifications" element={<Wrap role="CORE_ADMIN"><NotificationsManagement /></Wrap>} />

      {/* Fallback for undefined routes */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
