import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { getAccountsByCustomerId } from '../../services/accountService'
import { generateStatement, getStatementsByAccount } from '../../services/statementService'
import api from '../../services/api'
import LoadingSpinner from '../common/LoadingSpinner'

export default function MyStatements() {
  const { auth } = useAuth()

  // --- State Management ---
  const [accounts, setAccounts] = useState([])       // List of accounts the user owns
  const [accountId, setAccountId] = useState('')     // Currently selected account ID
  const [periodStart, setPeriodStart] = useState('') // Form: Start Date
  const [periodEnd, setPeriodEnd] = useState('')     // Form: End Date
  const [statements, setStatements] = useState(null) // List of generated statements
  const [loading, setLoading] = useState(true)       // Initial page load status
  const [genLoading, setGenLoading] = useState(false) // "Generating..." status
  const [msg, setMsg] = useState('')                 // Success feedback
  const [error, setError] = useState('')             // Error feedback
  const [expanded, setExpanded] = useState(null)     // Controls which statement is "open"

  // --- Logic: Initial Load ---
  useEffect(() => {
    const load = async () => {
      try {
        // STEP 1: Link User ID to a Banking Profile
        const customer = await api.get(`/v1/customer-reference/by-userid/${auth.userId}`).then(r => r.data)
        const accs = await getAccountsByCustomerId(customer.customerID)
        setAccounts(accs)

        // STEP 2: Auto-load history for the first account found
        if (accs.length > 0) {
          setAccountId(String(accs[0].accountId))
          const stmts = await getStatementsByAccount(accs[0].accountId).catch(() => [])
          setStatements(stmts)
        }
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load accounts')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [auth.userId])

  // --- Logic: Switching Accounts ---
  const handleAccountChange = async (id) => {
    setAccountId(id)
    setStatements(null) // Show spinner while switching
    setExpanded(null)   // Close any open statement
    try {
      setStatements(await getStatementsByAccount(id))
    } catch {
      setStatements([])
    }
  }

  // --- Logic: Generating a New Statement ---
  const handleGenerate = async (e) => {
    e.preventDefault()
    setGenLoading(true); setMsg(''); setError('')
    try {
      // Backend creates a snapshot of all transactions in this period
      const s = await generateStatement(accountId, periodStart, periodEnd)
      setMsg(`Statement #${s.statementId} generated successfully`)
      
      // Refresh the history to show the new statement at the top
      setStatements(await getStatementsByAccount(accountId))
    } catch (err) {
      setError('Access denied or failed to generate statement for this period.')
    } finally {
      setGenLoading(false)
    }
  }

  // --- Helper: JSON Parsing ---
  // The server stores the full transaction list as a JSON string in the database
  const parseSummary = (json) => {
    try { return JSON.parse(json) } catch { return null }
  }

  if (loading) return <div className="py-8"><LoadingSpinner /></div>

  return (
    <div>
      <h1 className="page-title mb-6">Statements</h1>

      {error && <div className="alert-error mb-4">{error}</div>}

      {/* Account Selector Tabs */}
      {accounts.length > 0 && (
        <div className="card mb-6">
          <h2 className="section-title">Select Account</h2>
          <div className="flex gap-2 flex-wrap">
            {accounts.map(a => (
              <button 
                key={a.accountId}
                onClick={() => handleAccountChange(String(a.accountId))}
                className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
                  accountId === String(a.accountId) ? 'bg-brand-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {a.category} — {a.accountNumber}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Generation Form */}
      <div className="card mb-6">
        <h2 className="section-title">Generate New Statement</h2>
        <form onSubmit={handleGenerate} className="grid grid-cols-1 sm:grid-cols-3 gap-3 items-end">
          <div>
            <label className="label">Period Start</label>
            <input type="date" className="input" value={periodStart} onChange={e => setPeriodStart(e.target.value)} required />
          </div>
          <div>
            <label className="label">Period End</label>
            <input type="date" className="input" value={periodEnd} onChange={e => setPeriodEnd(e.target.value)} required />
          </div>
          <button className="btn-primary" type="submit" disabled={genLoading || !accountId}>
            {genLoading ? 'Generating…' : 'Generate'}
          </button>
        </form>
        {msg && <div className="alert-success mt-3">{msg}</div>}
      </div>

      {/* Statement History Accordion */}
      <div className="card">
        <h2 className="section-title mb-4">Statement History</h2>
        {statements === null ? <LoadingSpinner /> : statements.length === 0 ? (
          <p className="text-center py-10 text-gray-400 italic">No statements found for this account</p>
        ) : (
          <div className="space-y-3">
            {statements.map(s => {
              const summary = parseSummary(s.summaryJson)
              const isOpen = expanded === s.statementId
              
              return (
                <div key={s.statementId} className="border border-gray-200 rounded-xl overflow-hidden">
                  {/* Accordion Header */}
                  <div 
                    className="flex items-center justify-between px-5 py-3 bg-gray-50 cursor-pointer hover:bg-gray-100"
                    onClick={() => setExpanded(isOpen ? null : s.statementId)}
                  >
                    <div className="text-sm">
                      <span className="font-semibold mr-4">#{s.statementId}</span>
                      <span className="text-gray-500">{s.periodStart} to {s.periodEnd}</span>
                    </div>
                    <span className="text-xs text-brand-600 font-medium">{isOpen ? 'Hide ▲' : 'View Details ▼'}</span>
                  </div>

                  {/* Accordion Content: Summary and Transaction Table */}
                  {isOpen && summary && (
                    <div className="px-5 py-4 animate-fade-in">
                      {/* Summary Totals */}
                      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
                        <div className="p-3 bg-gray-50 rounded-lg">
                          <div className="text-xs text-gray-500 uppercase mb-1">Opening</div>
                          <div className="font-bold">₹{parseFloat(summary.openingBalance).toFixed(2)}</div>
                        </div>
                        <div className="p-3 bg-green-50 rounded-lg">
                          <div className="text-xs text-green-600 uppercase mb-1">Credits</div>
                          <div className="font-bold text-green-700">+{parseFloat(summary.totalCredit).toFixed(2)}</div>
                        </div>
                        <div className="p-3 bg-red-50 rounded-lg">
                          <div className="text-xs text-red-600 uppercase mb-1">Debits</div>
                          <div className="font-bold text-red-700">-{parseFloat(summary.totalDebit).toFixed(2)}</div>
                        </div>
                        <div className="p-3 bg-brand-50 rounded-lg">
                          <div className="text-xs text-brand-600 uppercase mb-1">Closing</div>
                          <div className="font-bold text-brand-700">₹{parseFloat(summary.closingBalance).toFixed(2)}</div>
                        </div>
                      </div>

                      {/* Mini Transaction Table */}
                      <div className="table-wrap border-none">
                        <table className="table text-xs">
                          <thead>
                            <tr>{['Date', 'Type', 'Amount', 'Balance After'].map(h => <th key={h} className="th">{h}</th>)}</tr>
                          </thead>
                          <tbody>
                            {summary.transactions.map(t => (
                              <tr key={t.txnId}>
                                <td className="td">{t.txnDate?.substring(0, 10)}</td>
                                <td className="td"><span className={t.txnType === 'CREDIT' ? 'badge-green' : 'badge-red'}>{t.txnType}</span></td>
                                <td className="td font-medium">{parseFloat(t.amount).toFixed(2)}</td>
                                <td className="td font-mono">{parseFloat(t.balanceAfter).toFixed(2)}</td>
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
    </div>
  )
}