import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { getAccountsByCustomerId, getTermDepositByAccountId } from '../../services/accountService'
import api from '../../services/api'
import LoadingSpinner from '../common/LoadingSpinner'

// --- UI Helpers for Badges and Icons ---
const statusBadge = (s) => {
  if (s === 'ACTIVE') return <span className="badge-green">{s}</span>
  if (s === 'CLOSED') return <span className="badge-red">{s}</span>
  if (s === 'DORMANT') return <span className="badge-yellow">{s}</span>
  return <span className="badge-gray">{s}</span>
}

const categoryIcon = (cat) => {
  if (cat === 'SAVINGS') return '💰'
  if (cat === 'CURRENT') return '🏦'
  if (cat === 'FD') return '📈'
  if (cat === 'RD') return '📅'
  return '💳'
}

const categoryLabel = (cat) => {
  const labels = { SAVINGS: 'Savings Account', CURRENT: 'Current Account', FD: 'Fixed Deposit', RD: 'Recurring Deposit' }
  return labels[cat] || cat
}

export default function MyAccounts() {
  const { auth } = useAuth()
  const [accounts, setAccounts] = useState([])
  const [tdMap, setTdMap] = useState({}) // Stores extra details for FD/RD accounts
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [expanded, setExpanded] = useState(null) // Controls which card is "open"

  // --- Logic: Three-Step Data Loading ---
  useEffect(() => {
    const load = async () => {
      setLoading(true); setError('')
      try {
        // STEP 1: Resolve User ID to a Customer Reference
        // This links the "Login Account" to the "Banking Profile"
        const customer = await api.get(`/v1/customer-reference/by-userid/${auth.userId}`).then(r => r.data)

        // STEP 2: Fetch all accounts linked to that Customer ID
        const data = await getAccountsByCustomerId(customer.customerID)
        setAccounts(data)

        // STEP 3: Parallel Fetching for Term Deposits
        // We look for accounts that are FD or RD and fetch their maturity/principal details
        const tdFetches = data
          .filter(a => ['FD', 'RD'].includes(a.category))
          .map(a =>
            getTermDepositByAccountId(a.accountId)
              .then(td => ({ [a.accountId]: td }))
              .catch(() => ({}))
          )

        // Combine all individual TD results into a single map (Object.assign)
        const tdResults = await Promise.all(tdFetches)
        setTdMap(Object.assign({}, ...tdResults))

      } catch (err) {
        const msg = err.response?.data?.message || ''
        if (err.response?.status === 404 || msg.includes('No customer')) {
          setError('No customer profile is linked to your account yet. Please visit your branch to sync your CIF.')
        } else {
          setError(msg || 'Failed to load accounts')
        }
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [auth.userId])

  if (loading) return <div className="py-8"><LoadingSpinner /></div>

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">My Accounts</h1>
        <span className="text-sm text-gray-500">{accounts.length} account{accounts.length !== 1 ? 's' : ''}</span>
      </div>

      {error && <div className="alert-error mb-4">{error}</div>}

      {/* Empty State */}
      {accounts.length === 0 && !error && (
        <div className="card text-center py-16 text-gray-400">
          <p className="text-4xl mb-3">🏦</p>
          <p className="font-medium text-gray-600">No accounts found</p>
        </div>
      )}

      {/* Account List */}
      <div className="space-y-4">
        {accounts.map(acc => (
          <div key={acc.accountId} className="card">
            {/* Header: Always visible */}
            <div
              className="flex items-center justify-between cursor-pointer"
              onClick={() => setExpanded(expanded === acc.accountId ? null : acc.accountId)}
            >
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-xl bg-brand-50 border border-brand-200 flex items-center justify-center text-2xl">
                  {categoryIcon(acc.category)}
                </div>
                <div>
                  <div className="font-semibold text-gray-900">{categoryLabel(acc.category)}</div>
                  <div className="text-sm text-gray-500 font-mono">{acc.accountNumber}</div>
                </div>
              </div>
              <div className="text-right">
                <div className="text-xl font-bold text-gray-900">
                  {acc.currency} {parseFloat(acc.currentBalance || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                </div>
                <div className="mt-1">{statusBadge(acc.status)}</div>
              </div>
            </div>

            {/* Detailed View: Visible when clicked */}
            {expanded === acc.accountId && (
              <div className="mt-4 pt-4 border-t border-gray-100">
                <dl className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
                  <div><dt className="text-xs text-gray-500 uppercase">Account ID</dt><dd className="font-medium">{acc.accountId}</dd></div>
                  <div><dt className="text-xs text-gray-500 uppercase">Category</dt><dd className="font-medium">{acc.category}</dd></div>
                  <div><dt className="text-xs text-gray-500 uppercase">Currency</dt><dd className="font-medium">{acc.currency}</dd></div>
                  <div><dt className="text-xs text-gray-500 uppercase">Opened On</dt><dd className="font-medium">{acc.openDate}</dd></div>
                </dl>

                {/* Specific Details for Fixed Deposits / Recurring Deposits */}
                {tdMap[acc.accountId] && (
                  <div className="mt-4 p-4 bg-brand-50 rounded-lg border border-brand-200">
                    <div className="text-xs font-semibold text-brand-700 uppercase mb-3">Term Deposit Details</div>
                    <dl className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
                      <div><dt className="text-xs text-gray-500 uppercase">Principal</dt><dd className="font-medium">{acc.currency} {parseFloat(tdMap[acc.accountId].principalAmount).toFixed(2)}</dd></div>
                      <div><dt className="text-xs text-gray-500 uppercase">Rate</dt><dd className="font-medium">{tdMap[acc.accountId].rate}%</dd></div>
                      <div><dt className="text-xs text-gray-500 uppercase">Tenure</dt><dd className="font-medium">{tdMap[acc.accountId].tenureMonths} mo</dd></div>
                      <div><dt className="text-xs text-gray-500 uppercase">Maturity Date</dt><dd className="font-medium">{tdMap[acc.accountId].maturityDate}</dd></div>
                    </dl>
                  </div>
                )}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}