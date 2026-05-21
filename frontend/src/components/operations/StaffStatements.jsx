import { useState } from 'react'
import { generateStatement, getStatementsByAccount } from '../../services/statementService'
import LoadingSpinner from '../common/LoadingSpinner'

export default function StaffStatements() {
  // --- State Management ---
  const [accountId, setAccountId] = useState('')
  const [periodStart, setPeriodStart] = useState('')
  const [periodEnd, setPeriodEnd] = useState('')
  const [statements, setStatements] = useState(null)
  const [loadingList, setLoadingList] = useState(false) // Spinner for history lookup
  const [genLoading, setGenLoading] = useState(false)  // Spinner for generation
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')
  const [expanded, setExpanded] = useState(null)    // Controls which statement is open

  // --- Logic: Load Existing Statements ---
  const handleLoad = async (e) => {
    e.preventDefault()
    if (!accountId) return
    setLoadingList(true); setError(''); setStatements(null); setExpanded(null)
    try {
      // Staff can query any Account ID directly
      setStatements(await getStatementsByAccount(accountId))
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load statements')
    } finally { setLoadingList(false) }
  }

  // --- Logic: Generate New Audit Statement ---
  const handleGenerate = async (e) => {
    e.preventDefault()
    setGenLoading(true); setMsg(''); setError('')
    try {
      const s = await generateStatement(accountId, periodStart, periodEnd)
      setMsg(`Statement #${s.statementId} generated successfully`)

      // Refresh the list immediately so staff see the new record
      const updated = await getStatementsByAccount(accountId)
      setStatements(updated)
    } catch (err) {
      setError('Failed to generate statement. Check account ID and date range.')
    } finally { setGenLoading(false) }
  }

  // Helper: Convert stored JSON string into a usable JS Object
  const parseSummary = (json) => {
    try { return JSON.parse(json) } catch { return null }
  }

  return (
    <div>
      <h1 className="page-title mb-6">Account Statements (Staff View)</h1>

      {/* MODULE 1: Target Account Lookup */}
      <div className="card mb-6">
        <h2 className="section-title">Look Up Account</h2>
        <form onSubmit={handleLoad} className="flex gap-3 items-end flex-wrap">
          <div className="flex-1 max-w-xs">
            <label className="label">Enter Account ID</label>
            <input className="input" value={accountId}
              onChange={e => { setAccountId(e.target.value); setStatements(null); setMsg('') }}
              placeholder="e.g. 101" required />
          </div>
          <button className="btn-primary" type="submit" disabled={loadingList}>
            {loadingList ? 'Searching…' : 'Load History'}
          </button>
        </form>
      </div>

      {/* MODULE 2: Generation Form (Only visible once an account is selected) */}
      {accountId && (
        <div className="card mb-6">
          <h2 className="section-title">Generate New Statement</h2>
          <form onSubmit={handleGenerate} className="grid grid-cols-1 sm:grid-cols-3 gap-3 items-end">
            <div><label className="label">Start Date</label>
              <input type="date" className="input" value={periodStart} onChange={e => setPeriodStart(e.target.value)} required /></div>
            <div><label className="label">End Date</label>
              <input type="date" className="input" value={periodEnd} onChange={e => setPeriodEnd(e.target.value)} required /></div>
            <button className="btn-primary" type="submit" disabled={genLoading}>
              {genLoading ? 'Processing…' : 'Generate'}
            </button>
          </form>
          {msg && <div className="alert-success mt-3">{msg}</div>}
          {error && <div className="alert-error mt-3">{error}</div>}
        </div>
      )}

      {/* MODULE 3: Results Display */}
      {loadingList && <LoadingSpinner />}

      {statements !== null && (
        <div className="card">
          <h2 className="section-title mb-4">History — Account #{accountId}</h2>

          {statements.length === 0 ? (
            <p className="text-center py-10 text-gray-400 italic">No statement records found for this account.</p>
          ) : (
            <div className="space-y-3">
              {statements.map(s => {
                const summary = parseSummary(s.summaryJson)
                const isOpen = expanded === s.statementId
                return (
                  <div key={s.statementId} className="border border-gray-200 rounded-xl overflow-hidden">
                    {/* Header Clickable Row */}
                    <div className="flex items-center justify-between px-5 py-3 bg-gray-50 cursor-pointer hover:bg-gray-100"
                      onClick={() => setExpanded(isOpen ? null : s.statementId)}>
                      <div className="text-sm">
                        <span className="font-bold mr-4">#{s.statementId}</span>
                        <span className="text-gray-600">{s.periodStart} to {s.periodEnd}</span>
                      </div>
                      <div className="flex items-center gap-3">
                        {summary && <span className="text-xs text-gray-500">{summary.transactionCount} txns</span>}
                        <span className="text-xs text-brand-600 font-semibold">{isOpen ? 'Close' : 'Review'}</span>
                      </div>
                    </div>

                    {/* Detailed Accordion Content */}
                    {isOpen && summary && (
                      <div className="px-5 py-4 border-t">
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
                          <div className="bg-gray-50 p-2 rounded">
                            <div className="text-[10px] text-gray-400 uppercase">Opening</div>
                            <div className="text-sm font-semibold">₹{parseFloat(summary.openingBalance).toLocaleString('en-IN')}</div>
                          </div>
                          <div className="bg-green-50 p-2 rounded">
                            <div className="text-[10px] text-green-600 uppercase">Credits</div>
                            <div className="text-sm font-semibold text-green-700">+{parseFloat(summary.totalCredit).toLocaleString('en-IN')}</div>
                          </div>
                          <div className="bg-red-50 p-2 rounded">
                            <div className="text-[10px] text-red-600 uppercase">Debits</div>
                            <div className="text-sm font-semibold text-red-700">-{parseFloat(summary.totalDebit).toLocaleString('en-IN')}</div>
                          </div>
                          <div className="bg-brand-50 p-2 rounded">
                            <div className="text-[10px] text-brand-600 uppercase">Closing</div>
                            <div className="text-sm font-semibold text-brand-700">₹{parseFloat(summary.closingBalance).toLocaleString('en-IN')}</div>
                          </div>
                        </div>
                        {/* Transaction Table Snippet */}
                        <div className="table-wrap border-none">
                          <table className="table text-xs">
                            <thead><tr><th>Date</th><th>Type</th><th>Amount</th><th>Narrative</th></tr></thead>
                            <tbody>
                              {summary.transactions.map(t => (
                                <tr key={t.txnId}>
                                  <td>{t.txnDate?.substring(0, 10)}</td>
                                  <td><span className={t.txnType === 'CREDIT' ? 'text-green-600' : 'text-red-600'}>{t.txnType}</span></td>
                                  <td className="font-mono">₹{parseFloat(t.amount).toFixed(2)}</td>
                                  <td className="text-gray-400 italic">{t.narrative || '—'}</td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    )}
                  </div>
                )
              })}
            </div>
          )}
        </div>
      )}
    </div>
  )
}