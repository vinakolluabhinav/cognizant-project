import { useState } from 'react'
import { accrueInterest, postInterest } from '../../services/interestService'

export default function InterestManagement() {
  // --- UI View State ---
  const [tab, setTab] = useState('accrue') // 'accrue' or 'post'

  // --- Form States ---
  // Accrual: Calculating interest for a specific date range
  const [accrueForm, setAccrueForm] = useState({ accountId: '', periodStart: '', periodEnd: '' })

  // Posting: Executing the transfer of "Pending" interest to the account balance
  const [postForm, setPostForm] = useState({ accountId: '', postingType: 'CASAInterest' })

  // --- Shared Feedback States ---
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  // Reset helper to clear the UI before a new action
  const reset = () => { setMsg(''); setError(''); setResult(null) }

  // --- Logic: Phase 1 - Accrue Interest ---
  const handleAccrue = async (e) => {
    e.preventDefault()
    setLoading(true); reset()
    try {
      // The backend calculates interest based on the average daily balance
      const r = await accrueInterest({ ...accrueForm, accountId: parseInt(accrueForm.accountId) })
      setResult(r)
      setMsg(`Accrual created — Interest: ${parseFloat(r.interestAmount).toFixed(2)}`)
    } catch (err) {
      setError(err.response?.data?.message || 'Accrual failed')
    } finally { setLoading(false) }
  }

  // --- Logic: Phase 2 - Post Interest ---
  const handlePost = async (e) => {
    e.preventDefault()
    setLoading(true); reset()
    try {
      // This converts "Accrued" amounts into an actual Credit Transaction
      const r = await postInterest(postForm.accountId, postForm.postingType)
      setResult(r)
      setMsg(`${r.postedCount} accrual(s) posted — Total: ${parseFloat(r.interestAmount).toFixed(2)}`)
    } catch (err) {
      setError(err.response?.data?.message || 'Posting failed')
    } finally { setLoading(false) }
  }

  return (
    <div>
      <h1 className="page-title mb-6">Interest Management</h1>

      {/* Tab Switcher: Accrual vs Posting */}
      <div className="flex gap-2 mb-6">
        {[['accrue', 'Accrue Interest'], ['post', 'Post Interest']].map(([k, l]) => (
          <button
            key={k}
            onClick={() => { setTab(k); reset() }}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${tab === k ? 'bg-brand-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
          >
            {l}
          </button>
        ))}
      </div>

      {msg && <div className="alert-success mb-4">{msg}</div>}
      {error && <div className="alert-error mb-4">{error}</div>}

      {/* --- VIEW: ACCRUAL --- */}
      {tab === 'accrue' && (
        <div className="card max-w-lg">
          <h2 className="section-title">Create Accrual</h2>
          <p className="text-sm text-gray-500 mb-4">
            Record interest expense for the period. Principal is fetched automatically.
          </p>
          <form onSubmit={handleAccrue} className="space-y-3">
            <div><label className="label">Account ID</label>
              <input className="input" type="number" value={accrueForm.accountId} onChange={e => setAccrueForm(f => ({ ...f, accountId: e.target.value }))} required /></div>
            <div className="grid grid-cols-2 gap-3">
              <div><label className="label">Period Start</label>
                <input type="date" className="input" value={accrueForm.periodStart} onChange={e => setAccrueForm(f => ({ ...f, periodStart: e.target.value }))} required /></div>
              <div><label className="label">Period End</label>
                <input type="date" className="input" value={accrueForm.periodEnd} onChange={e => setAccrueForm(f => ({ ...f, periodEnd: e.target.value }))} required /></div>
            </div>
            <button className="btn-primary w-full" type="submit" disabled={loading}>Accrue Interest</button>
          </form>
          {result && (
            <div className="mt-4 p-3 bg-green-50 rounded-lg text-sm">
              <p>Interest Amount: <strong>{parseFloat(result.interestAmount).toFixed(2)}</strong></p>
            </div>
          )}
        </div>
      )}

      {/* --- VIEW: POSTING --- */}
      {tab === 'post' && (
        <div className="card max-w-lg">
          <h2 className="section-title">Post Interest</h2>
          <p className="text-sm text-gray-500 mb-4">
            Convert PENDING accruals into a balance-impacting credit transaction.
          </p>
          <form onSubmit={handlePost} className="space-y-3">
            <div><label className="label">Account ID</label>
              <input className="input" value={postForm.accountId} onChange={e => setPostForm(f => ({ ...f, accountId: e.target.value }))} required /></div>
            <div><label className="label">Posting Type</label>
              <select className="input" value={postForm.postingType} onChange={e => setPostForm(f => ({ ...f, postingType: e.target.value }))}>
                <option value="CASAInterest">CASA Interest</option>
                <option value="FDMaturityInterest">FD Maturity Interest</option>
                <option value="RDMaturityInterest">RD Maturity Interest</option>
              </select></div>
            <button className="btn-primary w-full" type="submit" disabled={loading}>Post Interest</button>
          </form>
          {result && (
            <div className="mt-4 p-3 bg-green-50 rounded-lg text-sm">
              <p>Accruals Found: <strong>{result.postedCount}</strong></p>
              <p>Total Credited: <strong>{parseFloat(result.interestAmount).toFixed(2)}</strong></p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}