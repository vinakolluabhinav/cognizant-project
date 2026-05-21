import { useEffect, useState } from 'react'
import { getAllTransactions, reverseTransaction } from '../../services/transactionService'
import LoadingSpinner from '../common/LoadingSpinner'

export default function AllTransactions() {
  // --- State Management ---
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(true)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')

  // Initial fetch on component mount
  useEffect(() => { fetchAll() }, [])

  // --- Logic: Fetch Master Transaction List ---
  const fetchAll = async () => {
    setLoading(true)
    try {
      setRows(await getAllTransactions())
    } catch {
      setError('Failed to load transactions')
    } finally {
      setLoading(false)
    }
  }

  // --- Logic: Transaction Reversal ---
  // In banking, we rarely "delete" data. Instead, we "reverse" it by
  // creating an opposite entry to zero out the balance effect.
  const handleReverse = async (id) => {
    setMsg(''); setError('')
    try {
      await reverseTransaction(id)
      setMsg(`Transaction #${id} reversed successfully`)
      fetchAll() // Refresh list to show updated status
    } catch (err) {
      setError(err.response?.data?.message || 'Reversal failed')
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="page-title">All Transactions</h1>
        <button className="btn-secondary" onClick={fetchAll}>Refresh</button>
      </div>

      {msg && <div className="alert-success mb-4">{msg}</div>}
      {error && <div className="alert-error mb-4">{error}</div>}

      {loading ? <LoadingSpinner /> : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                {['ID', 'Account', 'Type', 'Amount', 'Narrative', 'Date', 'Status', 'Action'].map(h => (
                  <th key={h} className="th">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {rows.map(t => (
                <tr key={t.txnId} className="tr-hover">
                  <td className="td font-mono text-xs">{t.txnId}</td>
                  <td className="td font-mono text-xs">{t.accountId}</td>

                  {/* Semantic Color Coding: Green for Credit, Red for Debit */}
                  <td className="td">
                    <span className={t.txnType === 'CREDIT' ? 'badge-green' : 'badge-red'}>
                      {t.txnType}
                    </span>
                  </td>

                  <td className="td font-medium">{parseFloat(t.amount).toFixed(2)}</td>
                  <td className="td text-sm text-gray-500 max-w-xs truncate">{t.narrative || '—'}</td>
                  <td className="td text-xs">{t.txnDate?.substring(0, 10)}</td>

                  {/* Status Indicator */}
                  <td className="td">
                    <span className={t.status === 'REVERSED' ? 'badge-red' : 'badge-green'}>
                      {t.status}
                    </span>
                  </td>

                  <td className="td">
                    {/* Guard Rail: Transactions can only be reversed once */}
                    {t.status !== 'REVERSED' && (
                      <button
                        className="text-xs text-red-600 hover:underline"
                        onClick={() => {
                          if (window.confirm(`Reverse transaction #${t.txnId}?`)) handleReverse(t.txnId)
                        }}
                      >
                        Reverse
                      </button>
                    )}
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