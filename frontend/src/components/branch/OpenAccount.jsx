import { useState, useEffect } from 'react'
import { createCasaAccount, createTermDeposit } from '../../services/accountService'
import { getAllProducts } from '../../services/productService'

// --- Default Initial Form States ---
const CASA_INIT = { cifNumber: '', productId: '', category: 'SAVINGS', currency: 'INR' }
const TD_INIT = { cifNumber: '', productId: '', principalAmount: '', tenureMonths: '', currency: 'INR', payoutMode: 'CreditAccount', rate: '' }

export default function OpenAccount() {
  // --- UI View State ---
  const [tab, setTab] = useState('CASA') // Toggles between 'CASA' and 'TD'

  // --- Data & Form States ---
  const [casa, setCasa] = useState(CASA_INIT)
  const [td, setTd] = useState(TD_INIT)
  const [products, setProducts] = useState([]) // Master list of products from backend
  const [loading, setLoading] = useState(false)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')

  // --- Effects: Fetch product catalog on page load ---
  useEffect(() => {
    getAllProducts().then(setProducts).catch(() => { })
  }, [])

  // --- Helpers: Dynamic State Updates ---
  const setC = (k, v) => setCasa(f => ({ ...f, [k]: v }))
  const setT = (k, v) => setTd(f => ({ ...f, [k]: v }))

  // --- Logic: Data Normalization ---
  // Backend services often return mixed casing; these helpers ensure we extract correctly
  const getCategory = (p) => (p.category || p.Category || '').toUpperCase()
  const getProductId = (p) => p.productID ?? p.ProductID ?? p.productId
  const getProductName = (p) => p.productName || p.ProductName || ''

  // Filter products based on the currently selected tab
  const casaProducts = products.filter(p => ['SAVINGS', 'CURRENT'].includes(getCategory(p)))
  const tdProducts = products.filter(p => ['FD', 'RD'].includes(getCategory(p)))

  // Fallback: Use full list if filters return empty (useful for dev/testing)
  const casaOpts = casaProducts.length > 0 ? casaProducts : products
  const tdOpts = tdProducts.length > 0 ? tdProducts : products

  // --- Logic: Submit to Backend ---
  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true); setMsg(''); setError('')

    try {
      let r
      if (tab === 'CASA') {
        // Send CASA payload
        r = await createCasaAccount({ ...casa, productId: parseInt(casa.productId) })
      } else {
        // Send TD payload with explicit numeric type conversions
        r = await createTermDeposit({
          ...td,
          productId: parseInt(td.productId),
          principalAmount: parseFloat(td.principalAmount),
          tenureMonths: parseInt(td.tenureMonths),
          rate: td.rate ? parseFloat(td.rate) : undefined,
        })
      }
      setMsg(`✓ Account created — ID: ${r.accountId}, Number: ${r.accountNumber}`)

      // Reset the active form only
      tab === 'CASA' ? setCasa(CASA_INIT) : setTd(TD_INIT)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create account')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1 className="page-title mb-6">Open Account</h1>

      <div className="card">
        {/* Tab Selection Navigation */}
        <div className="flex gap-2 mb-6">
          {[['CASA', 'CASA Account'], ['TD', 'Term Deposit']].map(([val, label]) => (
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

        <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {tab === 'CASA' ? (
            /* --- RENDER CASA FORM --- */
            <>
              <div>
                <label className="label">CIF Number *</label>
                <input className="input" value={casa.cifNumber} onChange={e => setC('cifNumber', e.target.value)} placeholder="e.g. CIF001" required />
                <p className="text-xs text-gray-400 mt-1">Customer must have VERIFIED KYC</p>
              </div>
              <div>
                <label className="label">Product *</label>
                <select className="input" value={casa.productId} onChange={e => setC('productId', e.target.value)} required>
                  <option value="">— Select Product —</option>
                  {casaOpts.map(p => (
                    <option key={getProductId(p)} value={getProductId(p)}>
                      {getProductName(p)} ({getCategory(p)})
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="label">Category</label>
                <select className="input" value={casa.category} onChange={e => setC('category', e.target.value)}>
                  <option value="SAVINGS">Savings Account (SA)</option>
                  <option value="CURRENT">Current Account (CA)</option>
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
            /* --- RENDER TD FORM --- */
            <>
              <div>
                <label className="label">CIF Number *</label>
                <input className="input" value={td.cifNumber} onChange={e => setT('cifNumber', e.target.value)} required />
              </div>
              <div>
                <label className="label">Product *</label>
                <select className="input" value={td.productId} onChange={e => setT('productId', e.target.value)} required>
                  <option value="">— Select Product —</option>
                  {tdOpts.map(p => (
                    <option key={getProductId(p)} value={getProductId(p)}>
                      {getProductName(p)} ({getCategory(p)})
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="label">Principal Amount *</label>
                <input className="input" type="number" step="0.01" value={td.principalAmount} onChange={e => setT('principalAmount', e.target.value)} required />
              </div>
              <div>
                <label className="label">Tenure (months) *</label>
                <input className="input" type="number" value={td.tenureMonths} onChange={e => setT('tenureMonths', e.target.value)} required />
              </div>
              <div>
                <label className="label">Interest Rate % (optional)</label>
                <input className="input" type="number" step="0.01" value={td.rate} onChange={e => setT('rate', e.target.value)} />
              </div>
              <div>
                <label className="label">Payout Mode</label>
                <select className="input" value={td.payoutMode} onChange={e => setT('payoutMode', e.target.value)}>
                  <option value="CreditAccount">Credit to Account</option>
                  <option value="Transfer">Transfer</option>
                </select>
              </div>
            </>
          )}
          <div className="sm:col-span-2">
            <button className="btn-primary w-full" type="submit" disabled={loading}>
              {loading ? 'Creating…' : `Open ${tab === 'CASA' ? 'CASA' : 'Term Deposit'} Account`}
            </button>
          </div>
        </form>
        {msg && <div className="alert-success mt-4">{msg}</div>}
        {error && <div className="alert-error mt-4">{error}</div>}
      </div>
    </div>
  )
}