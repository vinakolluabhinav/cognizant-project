import { useState } from 'react'
import { closePremature, getClosure } from '../../services/tdService'

export default function TDPrematureClosure() {
  // --- Form States ---
  const [tdId, setTdId] = useState('')
  const [penalRate, setPenalRate] = useState('1.0') // Default 1% penalty
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')

  // --- Logic: Process Premature Closure ---
  const handleClose = async (e) => {
    e.preventDefault()
    setLoading(true); setMsg(''); setError('')
    try {
      // Calls service to calculate interest, apply penalty, and move funds to CASA
      const r = await closePremature(tdId, parseFloat(penalRate))
      setResult(r)
      setMsg(`TD #${tdId} closed successfully. Penal rate applied: ${penalRate}%`)
    } catch (err) {
      setError(err.response?.data?.message || 'Closure failed')
    } finally {
      setLoading(false)
    }
  }

  // --- Logic: View Existing Closure Record ---
  const handleView = async () => {
    setError('')
    try {
      setResult(await getClosure(tdId))
    } catch (err) {
      setError(err.response?.data?.message || 'Closure record not found')
    }
  }

  return (
    <div>
      <h1 className="page-title mb-6">TD Premature Closure</h1>

      {/* --- MODULE 1: Closure Execution Form --- */}
      <div className="card max-w-lg mb-6">
        <h2 className="section-title">Process Premature Closure</h2>
        <form onSubmit={handleClose} className="space-y-4">
          <div>
            <label className="label">TD ID</label>
            <input className="input" value={tdId} onChange={e => setTdId(e.target.value)} placeholder="e.g. 1" required />
          </div>
          <div>
            <label className="label">
              Penal Rate (%)
              <span className="text-gray-400 font-normal text-xs ml-1">deducted from original interest rate</span>
            </label>
            <input className="input" type="number" step="0.1" min="0" value={penalRate} onChange={e => setPenalRate(e.target.value)} />
          </div>
          <div className="flex gap-3">
            <button className="btn-danger" type="submit" disabled={loading}>
              {loading ? 'Processing…' : 'Close TD'}
            </button>
            <button type="button" className="btn-secondary" onClick={handleView}>
              View Record
            </button>
          </div>
        </form>
        {msg && <div className="alert-success mt-3">{msg}</div>}
        {error && <div className="alert-error mt-3">{error}</div>}
      </div>

      {/* --- MODULE 2: Financial Breakdown (Results) --- */}
      {result && (
        <div className="card">
          <h2 className="section-title">Closure Details</h2>
          <dl className="grid grid-cols-2 md:grid-cols-3 gap-4">
            {[
              ['TD ID', result.tdId],
              ['Principal', result.principalAmount],
              ['Earned Interest', result.earnedInterest],
              ['Penal Deduction', result.penalDeduction],
              ['Net Payout', result.netPayout],
              ['Original Rate', result.originalRate ? `${result.originalRate}%` : '—'],
              ['Penal Rate', result.penalRate ? `${result.penalRate}%` : '—'],
              ['Effective Rate', result.effectiveRate ? `${result.effectiveRate}%` : '—'],
              ['Status', result.status],
              ['Closure Date', result.closureDate],
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