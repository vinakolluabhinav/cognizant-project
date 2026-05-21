import { useEffect, useState } from 'react'
import { getAllGLPostings, getGLByTransaction } from '../../services/transactionService'
import LoadingSpinner from '../common/LoadingSpinner'

export default function GLPostings() {
  // --- State Management ---
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(true)
  const [txnId, setTxnId] = useState('')
  const [error, setError] = useState('')

  // Initial fetch: Load the entire ledger on mount
  useEffect(() => { loadAll() }, [])

  const loadAll = async () => {
    setLoading(true); setError(''); setTxnId('')
    try {
      setRows(await getAllGLPostings())
    } catch {
      setError('Failed to load GL postings')
    } finally {
      setLoading(false)
    }
  }

  // --- Logic: Targeted Audit Search ---
  const handleSearch = async (e) => {
    e.preventDefault()
    if (!txnId) return
    setLoading(true); setError('')
    try {
      // Fetch only the entries associated with a specific Transaction ID
      setRows(await getGLByTransaction(txnId))
    } catch {
      setError('No postings found for transaction #' + txnId)
    } finally {
      setLoading(false)
    }
  }

  // --- Logic: Derived State (Financial Aggregations) ---
  // These are calculated on every render to ensure the summary cards match the table
  const totalDebit = rows
    .filter(g => (g.entryType || '').toUpperCase() === 'DEBIT')
    .reduce((sum, g) => sum + parseFloat(g.amount || 0), 0)

  const totalCredit = rows
    .filter(g => (g.entryType || '').toUpperCase() === 'CREDIT')
    .reduce((sum, g) => sum + parseFloat(g.amount || 0), 0)

  const netBalance = totalCredit - totalDebit

  // Helper: Currency formatting (Indian Numbering System)
  const fmt = (n) => n.toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })

  return (
    <div>
      <h1 className="page-title mb-6">GL Postings</h1>

      {/* Search Bar: Audit Filter */}
      <div className="card mb-6">
        <form onSubmit={handleSearch} className="flex gap-3 items-end flex-wrap">
          <div className="flex-1">
            <label className="label">Filter by Transaction ID</label>
            <input
              className="input"
              value={txnId}
              onChange={e => setTxnId(e.target.value)}
              placeholder="e.g. 5"
            />
          </div>
          <button className="btn-primary" type="submit" disabled={!txnId}>Search</button>
          <button type="button" className="btn-secondary" onClick={loadAll}>Show All</button>
        </form>
      </div>

      {error && <div className="alert-error mb-4">{error}</div>}

      {loading ? <LoadingSpinner /> : (
        <>
          {/* --- MODULE 1: Financial Summary Cards --- */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            <div className="card border-t-4 border-t-green-500">
              <div className="text-xs text-gray-500 uppercase">Total Credits</div>
              <div className="text-xl font-bold text-green-600">+ {fmt(totalCredit)}</div>
              <div className="text-xs text-gray-400">{rows.filter(g => g.entryType === 'CREDIT').length} entries</div>
            </div>

            <div className="card border-t-4 border-t-red-500">
              <div className="text-xs text-gray-500 uppercase">Total Debits</div>
              <div className="text-xl font-bold text-red-600">− {fmt(totalDebit)}</div>
              <div className="text-xs text-gray-400">{rows.filter(g => g.entryType === 'DEBIT').length} entries</div>
            </div>

            <div className={`card border-t-4 ${netBalance >= 0 ? 'border-t-green-500' : 'border-t-red-500'}`}>
              <div className="text-xs text-gray-500 uppercase">Net Balance</div>
              <div className={`text-xl font-bold ${netBalance >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                {netBalance >= 0 ? '+' : ''}{fmt(netBalance)}
              </div>
              <div className="text-xs text-gray-400 italic">Trial Balance Status</div>
            </div>

            <div className="card border-t-4 border-t-blue-500">
              <div className="text-xs text-gray-500 uppercase">Total Entries</div>
              <div className="text-xl font-bold text-gray-800">{rows.length}</div>
              <div className="text-xs text-gray-400">{txnId ? `Audit: Txn #${txnId}` : 'Master Ledger'}</div>
            </div>
          </div>

          {/* --- MODULE 2: The General Ledger Table --- */}
          <div className="card">
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    {['GL ID', 'Txn ID', 'Account', 'GL Account', 'DR/CR', 'Amount', 'Date'].map(h => (
                      <th key={h} className="th">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {rows.map(g => (
                    <tr key={g.glId} className="tr-hover">
                      <td className="td font-mono text-xs">{g.glId}</td>
                      <td className="td font-mono text-xs">{g.transactionId}</td>
                      <td className="td font-mono text-xs">{g.accountId}</td>
                      <td className="td text-xs text-gray-500">{g.glAccount || 'Internal GL'}</td>
                      <td className="td">
                        <span className={g.entryType === 'CREDIT' ? 'badge-green' : 'badge-red'}>
                          {g.entryType}
                        </span>
                      </td>
                      <td className="td font-semibold">
                        <span className={g.entryType === 'CREDIT' ? 'text-green-700' : 'text-red-700'}>
                          {g.entryType === 'CREDIT' ? '+' : '−'} {fmt(parseFloat(g.amount || 0))}
                        </span>
                      </td>
                      <td className="td text-xs">{g.postingDate?.substring(0, 16).replace('T', ' ')}</td>
                    </tr>
                  ))}
                </tbody>
                {/* --- Logic: Ledger Totals Footer --- */}
                {rows.length > 0 && (
                  <tfoot>
                    <tr className="bg-gray-50 font-bold border-t-2 border-gray-200">
                      <td className="td" colSpan={4}>TOTALS</td>
                      <td className="td">
                        <div className="text-green-600 text-xs">CR: +{fmt(totalCredit)}</div>
                        <div className="text-red-600 text-xs">DR: −{fmt(totalDebit)}</div>
                      </td>
                      <td className="td" colSpan={2}>
                        <div className={netBalance === 0 ? 'text-gray-500' : 'text-red-600'}>
                          NET: {fmt(netBalance)}
                        </div>
                      </td>
                    </tr>
                  </tfoot>
                )}
              </table>
            </div>
          </div>
        </>
      )}
    </div>
  )
}