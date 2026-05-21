import { useState } from 'react'
import { placeHold, releaseHold, getAvailableBalance, createStandingInstruction } from '../../services/holdService'

// --- Default Initial States ---
const HOLD_INIT = { accountId: '', amount: '', reason: '', type: 'HOLD' }
const SI_INIT = { fromAccId: '', toAccId: '', amount: '', frequency: 'MONTHLY' }

export default function HoldsAndSI() {
  // --- Main View State ---
  const [tab, setTab] = useState('hold') // Switches between 'hold' or 'si' view

  // --- Hold Logic States ---
  const [holdForm, setHoldForm] = useState(HOLD_INIT) // For placing new holds
  const [releaseId, setReleaseId] = useState('')      // For releasing a specific hold
  const [balanceId, setBalanceId] = useState('')      // For checking post-hold balance
  const [balance, setBalance] = useState(null)

  // --- Standing Instruction States ---
  const [siForm, setSiForm] = useState(SI_INIT)

  // --- Shared UI States ---
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')

  // Helper to clear alerts
  const reset = () => { setMsg(''); setError('') }

  // --- Logic: Place a Hold (Lien) ---
  const handlePlaceHold = async (e) => {
    e.preventDefault()
    setLoading(true); 
    reset();
    try {
      const r = await placeHold({ ...holdForm, amount: parseFloat(holdForm.amount) })
      setMsg(`Hold #${r.holdId} placed on account ${r.accountId}`)
      setHoldForm(HOLD_INIT)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to place hold')
    } finally { setLoading(false) }
  }

  // --- Logic: Release a Hold ---
  const handleRelease = async (e) => {
    e.preventDefault()
    setLoading(true); reset()
    try {
      await releaseHold(releaseId)
      setMsg(`Hold #${releaseId} released`)
      setReleaseId('')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to release hold')
    } finally { setLoading(false) }
  }

  // --- Logic: Check "Available" vs "Current" Balance ---
  const handleBalance = async (e) => {
    e.preventDefault()
    reset()
    try {
      // Fetches balance minus any active holds
      setBalance(await getAvailableBalance(balanceId))
    } catch (err) {
      setError(err.response?.data?.message || 'Account not found')
    }
  }

  // --- Logic: Create Automated Recurring Transfer (SI) ---
  const handleSI = async (e) => {
    e.preventDefault()
    setLoading(true); reset()
    try {
      const r = await createStandingInstruction({ ...siForm, amount: parseFloat(siForm.amount) })
      setMsg(`Standing instruction #${r.siId} created`)
      setSiForm(SI_INIT)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create SI')
    } finally { setLoading(false) }
  }

  return (
    <div>
      <h1 className="page-title mb-6">Holds &amp; Standing Instructions</h1>

      {/* Tab Selector */}
      <div className="flex gap-2 mb-6">
        {[['hold', 'Holds'], ['si', 'Standing Instructions']].map(([k, l]) => (
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

      {/* --- VIEW: HOLDS (3-column Layout) --- */}
      {tab === 'hold' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          {/* Card 1: Form to freeze funds */}
          <div className="card">
            <h2 className="section-title">Place Hold</h2>
            <form onSubmit={handlePlaceHold} className="space-y-3">
              <div><label className="label">Account ID</label>
                <input className="input" value={holdForm.accountId} onChange={e => setHoldForm(f => ({ ...f, accountId: e.target.value }))} required /></div>
              <div><label className="label">Amount</label>
                <input className="input" type="number" step="0.01" value={holdForm.amount} onChange={e => setHoldForm(f => ({ ...f, amount: e.target.value }))} required /></div>
              <div><label className="label">Reason</label>
                <input className="input" value={holdForm.reason} onChange={e => setHoldForm(f => ({ ...f, reason: e.target.value }))} /></div>
              <div><label className="label">Type</label>
                <select className="input" value={holdForm.type} onChange={e => setHoldForm(f => ({ ...f, type: e.target.value }))}>
                  <option>HOLD</option><option>LIEN</option>
                </select></div>
              <button className="btn-primary w-full" type="submit" disabled={loading}>Place Hold</button>
            </form>
          </div>

          {/* Card 2: Form to unfreeze funds */}
          <div className="card">
            <h2 className="section-title">Release Hold</h2>
            <form onSubmit={handleRelease} className="space-y-3">
              <div><label className="label">Hold ID</label>
                <input className="input" value={releaseId} onChange={e => setReleaseId(e.target.value)} required /></div>
              <button className="btn-danger w-full" type="submit" disabled={loading}>Release Hold</button>
            </form>
          </div>

          {/* Card 3: Form to check funds available to spend */}
          <div className="card">
            <h2 className="section-title">Available Balance</h2>
            <form onSubmit={handleBalance} className="space-y-3">
              <div><label className="label">Account ID</label>
                <input className="input" value={balanceId} onChange={e => setBalanceId(e.target.value)} required /></div>
              <button className="btn-secondary w-full" type="submit">Check Balance</button>
            </form>
            {balance && (
              <p className="mt-4 text-2xl font-bold text-brand-600">
                ₹ {parseFloat(balance.availableBalance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
              </p>
            )}
          </div>
        </div>
      )}

      {/* --- VIEW: STANDING INSTRUCTIONS --- */}
      {tab === 'si' && (
        <div className="card max-w-lg mx-auto">
          <h2 className="section-title">Create Standing Instruction</h2>
          <form onSubmit={handleSI} className="space-y-3">
            <div><label className="label">From Account ID</label>
              <input className="input" value={siForm.fromAccId} onChange={e => setSiForm(f => ({ ...f, fromAccId: e.target.value }))} required /></div>
            <div><label className="label">To Account ID</label>
              <input className="input" value={siForm.toAccId} onChange={e => setSiForm(f => ({ ...f, toAccId: e.target.value }))} required /></div>
            <div><label className="label">Amount</label>
              <input className="input" type="number" step="0.01" value={siForm.amount} onChange={e => setSiForm(f => ({ ...f, amount: e.target.value }))} required /></div>
            <div><label className="label">Frequency</label>
              <select className="input" value={siForm.frequency} onChange={e => setSiForm(f => ({ ...f, frequency: e.target.value }))}>
                <option>DAILY</option><option>WEEKLY</option><option>MONTHLY</option>
              </select></div>
            <button className="btn-primary w-full" type="submit" disabled={loading}>Create SI</button>
          </form>
        </div>
      )}
    </div>
  )
}