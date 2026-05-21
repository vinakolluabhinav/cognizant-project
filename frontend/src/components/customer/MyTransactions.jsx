import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { getAccountsByCustomerId } from '../../services/accountService'
import { getTransactionsByAccount } from '../../services/transactionService'
import api from '../../services/api'
import LoadingSpinner from '../common/LoadingSpinner'

export default function MyTransactions() {
  const { auth } = useAuth()

  // --- State Management ---
  const [accounts, setAccounts] = useState([])     // For the filter tabs
  const [rows, setRows] = useState([])     // The master list of all transactions
  const [selectedAcc, setSelectedAcc] = useState('all') // Current filter selection
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // --- Logic: Data Orchestration ---
  useEffect(() => {
    const load = async () => {
      setLoading(true); setError('')
      try {
        // STEP 1: Link the User to their Customer Profile
        const customer = await api.get(`/v1/customer-reference/by-userid/${auth.userId}`).then(r => r.data)

        // STEP 2: Find every account owned by this customer
        const accs = await getAccountsByCustomerId(customer.customerID)
        setAccounts(accs)

        // STEP 3: Parallel Transaction Fetching
        // Instead of waiting for Account A, then Account B, we trigger all requests at once.
        const results = await Promise.all(
          accs.map(a => getTransactionsByAccount(a.accountId).catch(() => []))
        )

        // STEP 4: Data Flattening & Sorting
        // results is an array of arrays [[txns], [txns]]. We flatten it into one list
        // and sort by date (newest first) so the user sees a unified timeline.
        const flattened = results.flat().sort((a, b) => new Date(b.txnDate) - new Date(a.txnDate))
        setRows(flattened)

      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load transactions')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [auth.userId])

  // --- Logic: Dynamic Filtering ---
  // We perform the filter in-memory to make the tab switching instant for the user.
  const filtered = selectedAcc === 'all'
    ? rows
    : rows.filter(t => String(t.accountId) === String(selectedAcc))

  if (loading) return <div className="py-8"><LoadingSpinner /></div>

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">My Transactions</h1>
        <span className="text-sm text-gray-500">
          {filtered.length} transaction{filtered.length !== 1 ? 's' : ''}
        </span>
      </div>

      {error && <div className="alert-error mb-4">{error}</div>}

      {/* Account Filter Tabs */}
      {accounts.length > 0 && (
        <div className="card mb-6">
          <div className="flex items-center gap-3 flex-wrap text-sm">
            <span className="font-medium text-gray-600">Filter by account:</span>
            <button
              onClick={() => setSelectedAcc('all')}
              className={`px-3 py-1.5 rounded-lg transition-colors ${selectedAcc === 'all' ? 'bg-brand-600 text-white' : 'bg-gray-100 text-gray-600'
                }`}
            >
              All Accounts
            </button>
            {accounts.map(a => (
              <button
                key={a.accountId}
                onClick={() => setSelectedAcc(String(a.accountId))}
                className={`px-3 py-1.5 rounded-lg transition-colors ${selectedAcc === String(a.accountId) ? 'bg-brand-600 text-white' : 'bg-gray-100 text-gray-600'
                  }`}
              >
                {a.category} — {a.accountNumber}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Data Table */}
      {filtered.length === 0 ? (
        <div className="card text-center py-16 text-gray-400">
          <p className="text-4xl mb-3">💳</p>
          <p>No transactions found</p>
        </div>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>{['ID', 'Acc ID', 'Type', 'Amount', 'Narrative', 'Date', 'Status'].map(h => <th key={h} className="th">{h}</th>)}</tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filtered.map(t => (
                <tr key={t.txnId} className="tr-hover">
                  <td className="td text-xs font-mono">{t.txnId}</td>
                  <td className="td text-xs font-mono">{t.accountId}</td>
                  <td className="td">
                    <span className={t.txnType === 'CREDIT' ? 'badge-green' : 'badge-red'}>{t.txnType}</span>
                  </td>
                  <td className="td font-semibold">
                    <span className={t.txnType === 'CREDIT' ? 'text-green-700' : 'text-red-700'}>
                      {t.txnType === 'CREDIT' ? '+' : '−'}
                      {parseFloat(t.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </span>
                  </td>
                  <td className="td text-xs text-gray-500 max-w-xs truncate">{t.narrative || '—'}</td>
                  <td className="td text-xs">{t.txnDate?.substring(0, 16).replace('T', ' ')}</td>
                  <td className="td">
                    <span className={t.status === 'REVERSED' ? 'badge-red' : 'badge-green'}>{t.status}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}