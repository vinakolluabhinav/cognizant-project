import { useState } from 'react'
import { postTransaction, getTransactionsByAccount } from '../../services/transactionService'
import LoadingSpinner from '../common/LoadingSpinner'

// --- Default Form State ---
const INIT = { accountId: '', txnType: 'CREDIT', amount: '', channel: 'BRANCH', narrative: '' }

export default function PostTransaction() {
  // --- State Management ---
  const [form, setForm] = useState(INIT)
  const [history, setHistory] = useState(null)     // Stores the recent txn list
  const [loading, setLoading] = useState(false)     // Loading state for the POST button
  const [histLoading, setHistLoading] = useState(false)     // Loading state for the history table
  const [msg, setMsg] = useState('')        // Success feedback
  const [error, setError] = useState('')        // Error feedback

  // Helper: Update specific keys in the form object
  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  // --- Logic: Submit Transaction ---
  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setMsg('')
    setError('')

    try {
      // Note: form.amount is converted from String to Float before sending to API
      const r = await postTransaction({ ...form, amount: parseFloat(form.amount) })
      setMsg(`Transaction #${r.txnId} posted successfully`)
      setForm(INIT) // Reset form on success
    } catch (err) {
      setError(err.response?.data?.message || 'Transaction failed')
    } finally {
      setLoading(false)
    }
  }

  // --- Logic: Fetch Account History ---
  const handleHistory = async () => {
    if (!form.accountId) return
    setHistLoading(true)
    try {
      setHistory(await getTransactionsByAccount(form.accountId))
    } catch {
      /* Silent catch: if history fails, the panel simply won't appear */
    } finally {
      setHistLoading(false)
    }
  }

  return (
    <div>
      <h1 className="page-title mb-6">Post Transaction</h1>

      {/* --- MODULE 1: Entry Form --- */}
      <div className="card mb-6">
        <h2 className="section-title">New Transaction</h2>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="label">Account ID</label>
            <input className="input" value={form.accountId} onChange={e => set('accountId', e.target.value)} required />
          </div>
          <div>
            <label className="label">Type</label>
            <select className="input" value={form.txnType} onChange={e => set('txnType', e.target.value)}>
              <option value="CREDIT">CREDIT (+)</option>
              <option value="DEBIT">DEBIT (-)</option>
            </select>
          </div>
          <div>
            <label className="label">Amount</label>
            <input className="input" type="number" step="0.01" min="0.01" value={form.amount} onChange={e => set('amount', e.target.value)} required />
          </div>
          <div>
            <label className="label">Channel</label>
            <select className="input" value={form.channel} onChange={e => set('channel', e.target.value)}>
              <option>BRANCH</option><option>ATM</option><option>ONLINE</option><option>API</option>
            </select>
          </div>
          <div className="sm:col-span-2">
            <label className="label">Narrative (Comments)</label>
            <input className="input" value={form.narrative} onChange={e => set('narrative', e.target.value)} placeholder="e.g. Salary credit for May 2026" />
          </div>
          <div className="sm:col-span-2 flex gap-3">
            <button className="btn-primary" type="submit" disabled={loading}>
              {loading ? 'Posting…' : 'Post Transaction'}
            </button>
            <button type="button" className="btn-secondary" onClick={handleHistory}>
              View Account History
            </button>
          </div>
        </form>
        {msg && <div className="alert-success mt-3">{msg}</div>}
        {error && <div className="alert-error mt-3">{error}</div>}
      </div>

      {/* --- MODULE 2: Transaction History Table --- */}
      {histLoading && <LoadingSpinner />}

      {history && (
        <div className="card">
          <h2 className="section-title">History — Account {form.accountId}</h2>
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>{['ID', 'Type', 'Amount', 'Channel', 'Date', 'Status'].map(h => <th key={h} className="th">{h}</th>)}</tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {history.map(t => (
                  <tr key={t.txnId} className="tr-hover">
                    <td className="td text-xs">{t.txnId}</td>
                    <td className="td">
                      <span className={t.txnType === 'CREDIT' ? 'badge-green' : 'badge-red'}>{t.txnType}</span>
                    </td>
                    <td className="td font-medium">{parseFloat(t.amount).toFixed(2)}</td>
                    <td className="td text-sm">{t.channel}</td>
                    <td className="td text-xs">{t.txnDate?.substring(0, 10)}</td>
                    <td className="td">
                      <span className={t.status === 'REVERSED' ? 'badge-red' : 'badge-green'}>{t.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}