import { useEffect, useState } from 'react'
import { generateReport, getAllReports } from '../../services/statementService'
import LoadingSpinner from '../common/LoadingSpinner'

// --- Configuration: Different types of analytical reports ---
const SCOPES = [
  { value: 'ALL', label: 'All Deposits' },
  { value: 'CASA', label: 'CASA Accounts' },
  { value: 'FD', label: 'Fixed Deposits' },
  { value: 'MONTHLY', label: 'Monthly Activity' },
]

export default function ReportsPage() {
  const [reports, setReports] = useState([])
  const [scope, setScope] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [genLoading, setGenLoading] = useState(false)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')
  const [expanded, setExpanded] = useState(null)

  // Fetch report history on mount
  useEffect(() => { fetchAll() }, [])

  const fetchAll = async () => {
    setLoading(true)
    try { setReports(await getAllReports()) }
    catch { setError('Failed to load reports') }
    finally { setLoading(false) }
  }

  // --- Logic: Generate a new Portfolio Snapshot ---
  const handleGenerate = async (e) => {
    e.preventDefault(); setGenLoading(true); setMsg(''); setError('')
    try {
      const r = await generateReport(scope)
      setMsg(`Report #${r.reportId} generated for scope: ${scope}`)
      fetchAll() // Refresh list to show the new report
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate')
    } finally { setGenLoading(false) }
  }

  // --- Helper: The JSON Unpacker ---
  // The backend sends a "metrics" string. We convert it back to an object.
  const parseMetrics = (json) => {
    try { return JSON.parse(json) } catch { return {} }
  }

  return (
    <div className="space-y-6">
      <h1 className="page-title">Deposit Reports</h1>

      {/* --- MODULE 1: Report Generator --- */}
      <div className="card">
        <h2 className="section-title">Generate New Report</h2>
        <form onSubmit={handleGenerate} className="flex gap-3 items-end flex-wrap">
          <div className="flex-1 min-w-[200px]">
            <label className="label">Report Scope</label>
            <select className="input" value={scope} onChange={e => setScope(e.target.value)}>
              {SCOPES.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
            </select>
          </div>
          <button className="btn-primary" type="submit" disabled={genLoading}>
            {genLoading ? 'Generating…' : 'Generate Portfolio Report'}
          </button>
        </form>
        {msg && <div className="alert-success mt-3">{msg}</div>}
      </div>

      {/* --- MODULE 2: Report History & Analytics --- */}
      <div className="card">
        <h2 className="section-title">History</h2>
        {loading ? <LoadingSpinner /> : (
          <div className="space-y-3">
            {reports.map(r => {
              const metrics = parseMetrics(r.metrics)
              const isOpen = expanded === r.reportId

              return (
                <div key={r.reportId} className="border border-gray-200 rounded-xl overflow-hidden">
                  {/* Report Header Bar */}
                  <div
                    className="flex items-center justify-between px-5 py-3 bg-gray-50 cursor-pointer hover:bg-gray-100"
                    onClick={() => setExpanded(isOpen ? null : r.reportId)}
                  >
                    <div className="flex items-center gap-4 text-sm">
                      <span className="font-bold text-gray-800">#{r.reportId}</span>
                      <span className="badge-blue">{r.scope}</span>
                      <span className="text-gray-400 text-xs">{r.generatedDate?.substring(0, 16).replace('T', ' ')}</span>
                    </div>

                    {/* Quick Stats: Always Visible */}
                    <div className="hidden sm:flex items-center gap-6 text-sm">
                      <span className="text-gray-600">Accounts: <strong>{metrics.totalAccounts || 0}</strong></span>
                      <span className="text-gray-600">Balance: <strong>₹{parseFloat(metrics.totalBalance || 0).toLocaleString('en-IN')}</strong></span>
                      <span>{isOpen ? '▲' : '▼'}</span>
                    </div>
                  </div>

                  {/* Expanded Metrics Grid: Dynamic Rendering */}
                  {isOpen && (
                    <div className="px-5 py-4 bg-white border-t animate-fade-in">
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        {Object.entries(metrics)
                          .filter(([k]) => !['scope', 'generatedAt'].includes(k)) // Hide metadata
                          .map(([key, value]) => (
                            <div key={key} className="bg-gray-50 rounded-lg p-3 border border-gray-100">
                              <div className="text-xs text-gray-500 uppercase tracking-tight mb-1">
                                {key.replace(/([A-Z])/g, ' $1').trim()} {/* Convert camelCase to Space Case */}
                              </div>
                              <div className="font-semibold text-gray-900">
                                {typeof value === 'number'
                                  ? value.toLocaleString('en-IN', { minimumFractionDigits: 2 })
                                  : String(value)}
                              </div>
                            </div>
                          ))}
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