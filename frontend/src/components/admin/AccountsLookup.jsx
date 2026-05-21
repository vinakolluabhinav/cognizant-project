import { useState, useEffect } from 'react'
import { getAccountById, getTermDepositByAccountId, createCasaAccount, createTermDeposit } from '../../services/accountService'
import { getAllProducts } from '../../services/productService'
import LoadingSpinner from '../common/LoadingSpinner'

// --- Default Form States ---
const CASA_INIT = { cifNumber: '', productId: '', category: 'SAVINGS', currency: 'INR' }
const TD_INIT = { cifNumber: '', productId: '', principalAmount: '', tenureMonths: '', currency: 'INR', payoutMode: 'CreditAccount', rate: '' }

export default function AccountsLookup() {
  // --- Main View State ---
  const [tab, setTab] = useState('lookup') // 'lookup' or 'create'

  // --- Lookup States ---
  const [accountId, setAccountId] = useState('')
  const [account, setAccount] = useState(null)
  const [td, setTd] = useState(null) // Stores extra details if account is a TD
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // --- Creation States ---
  const [createTab, setCreateTab] = useState('CASA') // 'CASA' or 'TD'
  const [casa, setCasa] = useState(CASA_INIT)
  const [tdForm, setTdForm] = useState(TD_INIT)
  const [products, setProducts] = useState([])
  const [saving, setSaving] = useState(false)
  const [msg, setMsg] = useState('')
  const [createError, setCreateError] = useState('')

  // --- Effects: Load available products for the dropdowns ---
  useEffect(() => {
    getAllProducts().then(setProducts).catch(() => { })
  }, [])

  // --- Helpers: Update nested form state ---
  const setC = (k, v) => setCasa(f => ({ ...f, [k]: v }))
  const setT = (k, v) => setTdForm(f => ({ ...f, [k]: v }))

  // --- Logic: Data Normalization for Backend Compatibility ---
  const getCategory = (p) => (p.category || p.Category || '').toUpperCase()
  const getProductId = (p) => p.productID ?? p.ProductID ?? p.productId
  const getProductName = (p) => p.productName || p.ProductName || ''

  const casaProducts = products.filter(p => ['SAVINGS', 'CURRENT'].includes(getCategory(p)))
  const tdProducts = products.filter(p => ['FD', 'RD'].includes(getCategory(p)))

  // --- Logic: Search for Account ---
  const handleSearch = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    setAccount(null)
    setTd(null)

    try {
      const acc = await getAccountById(accountId)
      setAccount(acc)

      // If the account is a Term Deposit, fetch the specialized TD details
      if (['FD', 'RD'].includes((acc.category || '').toUpperCase())) {
        try {
          setTd(await getTermDepositByAccountId(accountId))
        } catch {
          /* Account is categorized as TD but details not found */
        }
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Account not found')
    } finally {
      setLoading(false)
    }
  }

  // --- Logic: Open New Account ---
  const handleCreate = async (e) => {
    e.preventDefault()
    setSaving(true)
    setMsg('')
    setCreateError('')

    try {
      let r
      if (createTab === 'CASA') {
        r = await createCasaAccount({ ...casa, productId: parseInt(casa.productId) })
      } else {
        // Convert strings to proper numeric types for the API
        r = await createTermDeposit({
          ...tdForm,
          productId: parseInt(tdForm.productId),
          principalAmount: parseFloat(tdForm.principalAmount),
          tenureMonths: parseInt(tdForm.tenureMonths),
          rate: tdForm.rate ? parseFloat(tdForm.rate) : undefined,
        })
      }
      setMsg(`✓ Account created — ID: ${r.accountId}, Number: ${r.accountNumber}`)
      createTab === 'CASA' ? setCasa(CASA_INIT) : setTdForm(TD_INIT)
    } catch (err) {
      setCreateError(err.response?.data?.message || 'Failed to create account')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <h1 className="page-title mb-6">Accounts</h1>

      {/* View Switcher: Lookup vs Create */}
      <div className="flex gap-2 mb-6">
        {[['lookup', 'Lookup Account'], ['create', 'Open New Account']].map(([val, label]) => (
          <button
            key={val}
            onClick={() => { setTab(val); setMsg(''); setError('') }}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${tab === val ? 'bg-brand-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
          >
            {label}
          </button>
        ))}
      </div>

      {/* --- VIEW: LOOKUP --- */}
      {tab === 'lookup' && (
        <>
          <div className="card mb-6">
            <h2 className="section-title">Search by Account ID</h2>
            <form onSubmit={handleSearch} className="flex gap-3">
              <input
                className="input max-w-xs"
                placeholder="Account ID e.g. 1"
                value={accountId}
                onChange={e => setAccountId(e.target.value)}
                required
              />
              <button className="btn-primary" type="submit" disabled={loading}>Search</button>
            </form>
          </div>

          {loading && <LoadingSpinner />}
          {error && <div className="alert-error mb-4">{error}</div>}

          {account && (
            <div className="space-y-4">
              {/* Basic Account Data */}
              <div className="card">
                <h2 className="section-title">Deposit Account</h2>
                <dl className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  {[
                    ['Account ID', account.accountId],
                    ['Account Number', account.accountNumber],
                    ['Category', account.category],
                    ['Currency', account.currency],
                    ['Balance', parseFloat(account.currentBalance || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })],
                    ['Status', account.status],
                    ['Customer ID', account.customerId],
                    ['Open Date', account.openDate],
                  ].map(([k, v]) => (
                    <div key={k}>
                      <dt className="text-xs text-gray-500 uppercase tracking-wide">{k}</dt>
                      <dd className="mt-1 text-sm font-medium">{String(v ?? '—')}</dd>
                    </div>
                  ))}
                </dl>
              </div>

              {/* Conditional TD Details */}
              {td && (
                <div className="card">
                  <h2 className="section-title">Term Deposit Details</h2>
                  <dl className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    {[
                      ['TD ID', td.tdId],
                      ['Principal', parseFloat(td.principalAmount || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })],
                      ['Rate', `${td.rate}%`],
                      ['Tenure', `${td.tenureMonths} months`],
                      ['Start Date', td.startDate],
                      ['Maturity Date', td.maturityDate],
                      ['Payout Mode', td.payoutMode],
                      ['Status', td.status],
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
          )}
        </>
      )}

      {/* --- VIEW: CREATE --- */}
      {tab === 'create' && (
        <div className="card">
          {/* Sub-tabs for Account Type */}
          <div className="flex gap-2 mb-6">
            {[['CASA', 'CASA Account'], ['TD', 'Term Deposit']].map(([val, label]) => (
              <button
                key={val}
                onClick={() => { setCreateTab(val); setMsg(''); setCreateError('') }}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${createTab === val ? 'bg-brand-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
              >
                {label}
              </button>
            ))}
          </div>

          <form onSubmit={handleCreate} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {createTab === 'CASA' ? (
              /* CASA FORM */
              <>
                <div>
                  <label className="label">CIF Number *</label>
                  <input className="input" value={casa.cifNumber} onChange={e => setC('cifNumber', e.target.value)} required />
                </div>
                <div>
                  <label className="label">Product *</label>
                  <select className="input" value={casa.productId} onChange={e => setC('productId', e.target.value)} required>
                    <option value="">— Select Product —</option>
                    {casaProducts.map(p => (
                      <option key={getProductId(p)} value={getProductId(p)}>
                        {getProductName(p)} ({getCategory(p)})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="label">Category</label>
                  <select className="input" value={casa.category} onChange={e => setC('category', e.target.value)}>
                    <option value="SAVINGS">Savings Account</option>
                    <option value="CURRENT">Current Account</option>
                  </select>
                </div>
                <div>
                  <label className="label">Currency</label>
                  <select className="input" value={casa.currency} onChange={e => setC('currency', e.target.value)}>
                    <option value="INR">INR</option><option value="USD">USD</option><option value="EUR">EUR</option>
                  </select>
                </div>
              </>
            ) : (
              /* TERM DEPOSIT FORM */
              <>
                <div>
                  <label className="label">CIF Number *</label>
                  <input className="input" value={tdForm.cifNumber} onChange={e => setT('cifNumber', e.target.value)} required />
                </div>
                <div>
                  <label className="label">Product *</label>
                  <select className="input" value={tdForm.productId} onChange={e => setT('productId', e.target.value)} required>
                    <option value="">— Select Product —</option>
                    {tdProducts.map(p => (
                      <option key={getProductId(p)} value={getProductId(p)}>
                        {getProductName(p)} ({getCategory(p)})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="label">Principal Amount (₹) *</label>
                  <input className="input" type="number" step="0.01" value={tdForm.principalAmount} onChange={e => setT('principalAmount', e.target.value)} required />
                </div>
                <div>
                  <label className="label">Tenure (months) *</label>
                  <input className="input" type="number" value={tdForm.tenureMonths} onChange={e => setT('tenureMonths', e.target.value)} required />
                </div>
                <div>
                  <label className="label">Interest Rate % (optional)</label>
                  <input className="input" type="number" step="0.01" value={tdForm.rate} onChange={e => setT('rate', e.target.value)} />
                </div>
                <div>
                  <label className="label">Payout Mode</label>
                  <select className="input" value={tdForm.payoutMode} onChange={e => setT('payoutMode', e.target.value)}>
                    <option value="CreditAccount">Credit to Account</option>
                    <option value="Transfer">Transfer</option>
                  </select>
                </div>
              </>
            )}
            <div className="sm:col-span-2">
              <button className="btn-primary" type="submit" disabled={saving}>
                {saving ? 'Creating…' : `Open ${createTab === 'CASA' ? 'CASA' : 'Term Deposit'} Account`}
              </button>
            </div>
          </form>
          {msg && <div className="alert-success mt-4">{msg}</div>}
          {createError && <div className="alert-error mt-4">{createError}</div>}
        </div>
      )}
    </div>
  )
}