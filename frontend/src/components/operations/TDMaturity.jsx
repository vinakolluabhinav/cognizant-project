import { useState } from 'react'
import { processMaturity, getMaturity } from '../../services/tdService'

export default function TDMaturity() {
  // --- Form States ---
  const [tdId, setTdId] = useState('')
  const [action, setAction] = useState('PAYOUT') // 'PAYOUT' (Credit CASA) or 'RENEW' (Rollover)
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')

  // --- Logic: Process TD Maturity ---
  const handleProcess = async (e) => {
    e.preventDefault()
    setLoading(true); setMsg(''); setError('')
    try {
      // Backend handles the heavy lifting: Interest calculation + GL postings
      const r = await processMaturity(tdId, action)
      setResult(r)
      setMsg(`Maturity processed for TD #${tdId} — Action: ${action}`)
    } catch (err) {
      setError(err.response?.data?.message || 'Processing failed')
    } finally {
      setLoading(false)
    }
  }

  // --- Logic: View Existing Maturity Record ---
  const handleView = async () => {
    setError('')
    try {
      setResult(await getMaturity(tdId))
    } catch (err) {
      setError(err.response?.data?.message || 'Maturity record not found')
    }
  }

  return (
    <div>
      <h1 className="page-title mb-6">TD Maturity Processing</h1>

      {/* --- MODULE 1: Process Action Form --- */}
      <div className="card max-w-lg mb-6">
        <h2 className="section-title">Finalize Deposit</h2>
        <form onSubmit={handleProcess} className="space-y-4">
          <div>
            <label className="label">TD Account ID</label>
            <input className="input" value={tdId} onChange={e => setTdId(e.target.value)} placeholder="e.g. 1" required />
          </div>
          <div>
            <label className="label">Maturity Action</label>
            <select className="input" value={action} onChange={e => setAction(e.target.value)}>
              <option value="PAYOUT">Payout — Credit to Savings/Current</option>
              <option value="RENEW">Renew — Automatic Rollover</option>
            </select>
          </div>
          <div className="flex gap-3">
            <button className="btn-primary" type="submit" disabled={loading}>
              {loading ? 'Processing…' : 'Process Maturity'}
            </button>
            <button type="button" className="btn-secondary" onClick={handleView}>
              View Record
            </button>
          </div>
        </form>
        {msg && <div className="alert-success mt-3">{msg}</div>}
        {error && <div className="alert-error mt-3">{error}</div>}
      </div>

      {/* --- MODULE 2: Financial Breakdown --- */}
      {result && (
        <div className="card">
          <h2 className="section-title">Maturity Summary</h2>
          <dl className="grid grid-cols-2 md:grid-cols-3 gap-4">
            {[
              ['Maturity ID', result.maturityId],
              ['TD ID', result.tdId],
              ['Principal (₹)', result.principalAmount],
              ['Interest (₹)', result.interestAmount],
              ['Total Payout', result.maturityAmount],
              ['Status', result.status],
              ['Payout Txn ID', result.payoutTxnId],
              ['Payout Date', result.payoutDate],
              ['Staff ID', result.initiatedByUserId],
            ].map(([k, v]) => (
              <div key={k}>
                <dt className="text-xs text-gray-500 uppercase tracking-wide">{k}</dt>
                <dd className="mt-1 text-sm font-medium">{String(v ?? '—')}</dd>
              </div>
            ))}
          </dl>
        </div>
      )}
    </div>
  )
}