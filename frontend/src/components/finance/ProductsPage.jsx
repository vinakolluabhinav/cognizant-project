import { useEffect, useState } from 'react'
import { getAllProducts, createProduct, updateProduct, deleteProduct, simulateProduct } from '../../services/productService'
import { useAuth } from '../../context/AuthContext'
import LoadingSpinner from '../common/LoadingSpinner'

// --- Default Form States ---
const INIT      = { productName: '', category: 'SAVINGS', minAmount: '', maxAmount: '', minTenure: '', maxTenure: '', interestMethod: 'SIMPLE', status: 'ACTIVE', interestSlabs: [] }
const SIM_INIT  = { productId: '', amount: '', tenure: '' }
const SLAB_INIT = { tenureFrom: '', tenureTo: '', rate: '' }

export default function ProductsPage() {
  const { auth } = useAuth()

  // --- RBAC: Roles determine who can modify the product catalog ---
  const canEdit   = ['FINANCE_ANALYST', 'CORE_ADMIN'].includes(auth.role)
  const canDelete = auth.role === 'CORE_ADMIN'

  // --- Product Form States ---
  const [products, setProducts] = useState([])
  const [form, setForm]         = useState(INIT)
  const [editId, setEditId]     = useState(null)
  const [loading, setLoading]   = useState(true)
  const [saving, setSaving]     = useState(false)
  const [msg, setMsg]           = useState('')
  const [error, setError]       = useState('')

  // --- Interest Slab States ---
  const [slab, setSlab] = useState(SLAB_INIT)

  // --- Simulation States ---
  const [simForm, setSimForm]     = useState(SIM_INIT)
  const [simResult, setSimResult] = useState(null)
  const [simLoading, setSimLoading] = useState(false)
  const [simError, setSimError]   = useState('')

  // --- Effects: Load product catalog on mount ---
  useEffect(() => { fetchAll() }, [])

  // --- Logic: Fetch All Products ---
  const fetchAll = async () => {
    setLoading(true)
    try {
      setProducts(await getAllProducts())
    } catch {
      setError('Failed to load products')
    } finally {
      setLoading(false)
    }
  }

  // --- Helpers: Update nested form state ---
  const set    = (k, v) => setForm(f => ({ ...f, [k]: v }))
  const setSim = (k, v) => setSimForm(f => ({ ...f, [k]: v }))

  // --- Logic: Add Interest Rate Slab to the form ---
  const addSlab = () => {
    if (!slab.tenureFrom || !slab.tenureTo || !slab.rate) return
    set('interestSlabs', [...form.interestSlabs, {
      tenureFrom: parseInt(slab.tenureFrom),
      tenureTo:   parseInt(slab.tenureTo),
      rate:       parseFloat(slab.rate),
    }])
    setSlab(SLAB_INIT)
  }

  // --- Logic: Remove a Slab by index ---
  const removeSlab = (idx) => {
    set('interestSlabs', form.interestSlabs.filter((_, i) => i !== idx))
  }

  // --- Logic: Populate form for editing an existing product ---
  const startEdit = (p) => {
    setEditId(p.productID ?? p.ProductID)
    setForm({
      productName:    p.productName    || p.ProductName    || '',
      category:       p.category       || p.Category       || 'SAVINGS',
      minAmount:      p.minAmount      ?? p.MinAmount      ?? '',
      maxAmount:      p.maxAmount      ?? p.MaxAmount      ?? '',
      minTenure:      p.minTenure      ?? p.MinTenure      ?? '',
      maxTenure:      p.maxTenure      ?? p.MaxTenure      ?? '',
      interestMethod: p.interestMethod || p.InterestMethod || 'SIMPLE',
      status:         p.status         || p.Status         || 'ACTIVE',
      interestSlabs:  p.interestSlabs  ?? [],
    })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  // --- Logic: Save (Create or Update) Product ---
  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    setMsg('')
    setError('')
    try {
      const payload = {
        productName:    form.productName,
        category:       form.category,
        minAmount:      form.minAmount  ? parseFloat(form.minAmount)  : null,
        maxAmount:      form.maxAmount  ? parseFloat(form.maxAmount)  : null,
        minTenure:      form.minTenure  ? parseInt(form.minTenure)    : null,
        maxTenure:      form.maxTenure  ? parseInt(form.maxTenure)    : null,
        interestMethod: form.interestMethod,
        status:         form.status,
        interestSlabs:  form.interestSlabs,
      }
      if (editId) {
        await updateProduct(editId, payload)
        setMsg('Product updated successfully')
      } else {
        await createProduct(payload)
        setMsg('Product created successfully')
      }
      setForm(INIT)
      setEditId(null)
      fetchAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save product')
    } finally {
      setSaving(false)
    }
  }

  // --- Logic: Delete Product ---
  const handleDelete = async (id) => {
    if (!window.confirm('Delete this product? This action cannot be undone.')) return
    try {
      await deleteProduct(id)
      setMsg('Product deleted')
      fetchAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Delete failed')
    }
  }

  // --- Logic: Simulate Maturity for a Product ---
  const handleSimulate = async (e) => {
    e.preventDefault()
    setSimLoading(true)
    setSimResult(null)
    setSimError('')
    try {
      const res = await simulateProduct(
        parseInt(simForm.productId),
        parseFloat(simForm.amount),
        parseInt(simForm.tenure)
      )
      setSimResult(res)
    } catch (err) {
      setSimError(err.response?.data?.message || 'Simulation failed — ensure interest slabs are configured')
    } finally {
      setSimLoading(false)
    }
  }

  // --- Logic: Data Normalization for Backend Compatibility ---
  // Backend entity uses PascalCase (ProductID, ProductName) — normalize for consistent display
  const getProdId   = (p) => p.productID   ?? p.ProductID
  const getProdName = (p) => p.productName || p.ProductName || ''
  const getProdCat  = (p) => p.category    || p.Category    || ''
  const getProdStatus = (p) => p.status    || p.Status      || ''

  return (
    <div>
      <h1 className="page-title mb-6">Deposit Products</h1>

      {/* --- SECTION: Create / Edit Form (Finance Analyst & Admin only) --- */}
      {canEdit && (
        <div className="card mb-6">
          <h2 className="section-title">{editId ? 'Edit Product' : 'New Product'}</h2>
          <form onSubmit={handleSave} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="sm:col-span-2">
              <label className="label">Product Name *</label>
              <input
                className="input"
                value={form.productName}
                onChange={e => set('productName', e.target.value)}
                placeholder="e.g. Gold Premium Savings"
                required
              />
            </div>
            <div>
              <label className="label">Category</label>
              <select className="input" value={form.category} onChange={e => set('category', e.target.value)}>
                <option>SAVINGS</option><option>CURRENT</option><option>FD</option><option>RD</option>
              </select>
            </div>
            <div>
              <label className="label">Interest Method</label>
              <select className="input" value={form.interestMethod} onChange={e => set('interestMethod', e.target.value)}>
                <option value="SIMPLE">SIMPLE</option>
                <option value="COMPOUNDED">COMPOUNDED</option>
              </select>
            </div>
            <div>
              <label className="label">Min Amount (₹)</label>
              <input className="input" type="number" step="0.01" value={form.minAmount} onChange={e => set('minAmount', e.target.value)} />
            </div>
            <div>
              <label className="label">Max Amount (₹)</label>
              <input className="input" type="number" step="0.01" value={form.maxAmount} onChange={e => set('maxAmount', e.target.value)} />
            </div>
            <div>
              <label className="label">Min Tenure (months)</label>
              <input className="input" type="number" value={form.minTenure} onChange={e => set('minTenure', e.target.value)} />
            </div>
            <div>
              <label className="label">Max Tenure (months)</label>
              <input className="input" type="number" value={form.maxTenure} onChange={e => set('maxTenure', e.target.value)} />
            </div>
            <div>
              <label className="label">Status</label>
              <select className="input" value={form.status} onChange={e => set('status', e.target.value)}>
                <option>ACTIVE</option><option>INACTIVE</option>
              </select>
            </div>

            {/* Interest Rate Slabs — tenure-based rate configuration */}
            <div className="sm:col-span-2 lg:col-span-4">
              <label className="label">Interest Rate Slabs</label>
              <div className="border border-gray-200 rounded-lg p-3 bg-gray-50">
                {/* Existing slabs list */}
                {form.interestSlabs.length > 0 && (
                  <div className="mb-3 space-y-2">
                    {form.interestSlabs.map((s, i) => (
                      <div key={i} className="flex items-center justify-between bg-white rounded-lg px-3 py-2 border border-gray-200 text-sm">
                        <span className="text-gray-600">
                          Tenure <strong>{s.tenureFrom}–{s.tenureTo}</strong> months → <strong className="text-brand-600">{s.rate}% p.a.</strong>
                        </span>
                        <button type="button" className="text-red-500 hover:text-red-700 text-xs ml-4" onClick={() => removeSlab(i)}>
                          ✕ Remove
                        </button>
                      </div>
                    ))}
                  </div>
                )}
                {/* Add new slab row */}
                <div className="flex gap-2 items-end flex-wrap">
                  <div>
                    <label className="text-xs text-gray-500">From (months)</label>
                    <input className="input" type="number" placeholder="e.g. 6" value={slab.tenureFrom} onChange={e => setSlab(s => ({ ...s, tenureFrom: e.target.value }))} />
                  </div>
                  <div>
                    <label className="text-xs text-gray-500">To (months)</label>
                    <input className="input" type="number" placeholder="e.g. 12" value={slab.tenureTo} onChange={e => setSlab(s => ({ ...s, tenureTo: e.target.value }))} />
                  </div>
                  <div>
                    <label className="text-xs text-gray-500">Rate (% p.a.)</label>
                    <input className="input" type="number" step="0.01" placeholder="e.g. 7.25" value={slab.rate} onChange={e => setSlab(s => ({ ...s, rate: e.target.value }))} />
                  </div>
                  <button type="button" className="btn-secondary" onClick={addSlab}>+ Add Slab</button>
                </div>
                <p className="text-xs text-gray-400 mt-2">e.g. 6–12 months @ 7.25% p.a.</p>
              </div>
            </div>

            <div className="sm:col-span-2 lg:col-span-4 flex gap-3">
              <button className="btn-primary" type="submit" disabled={saving}>
                {saving ? 'Saving…' : editId ? 'Update Product' : 'Create Product'}
              </button>
              {editId && (
                <button type="button" className="btn-secondary" onClick={() => { setEditId(null); setForm(INIT) }}>
                  Cancel
                </button>
              )}
            </div>
          </form>
          {msg   && <div className="alert-success mt-3">{msg}</div>}
          {error && <div className="alert-error mt-3">{error}</div>}
        </div>
      )}

      {/* --- SECTION: Maturity Simulator --- */}
      <div className="card mb-6">
        <h2 className="section-title">Simulate Maturity</h2>
        <form onSubmit={handleSimulate} className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div>
            <label className="label">Product</label>
            <select className="input" value={simForm.productId} onChange={e => setSim('productId', e.target.value)} required>
              <option value="">— Select Product —</option>
              {products.map(p => (
                <option key={getProdId(p)} value={getProdId(p)}>
                  {getProdName(p)} ({getProdCat(p)})
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Principal Amount (₹)</label>
            <input className="input" type="number" step="0.01" placeholder="e.g. 20000" value={simForm.amount} onChange={e => setSim('amount', e.target.value)} required />
          </div>
          <div>
            <label className="label">Tenure (months)</label>
            <input className="input" type="number" placeholder="e.g. 12" value={simForm.tenure} onChange={e => setSim('tenure', e.target.value)} required />
          </div>
          <div className="sm:col-span-3">
            <button className="btn-primary" type="submit" disabled={simLoading}>
              {simLoading ? 'Simulating…' : 'Simulate'}
            </button>
          </div>
        </form>

        {simError && <div className="alert-error mt-3">{simError}</div>}

        {/* Simulation result card */}
        {simResult && (
          <div className="mt-4 rounded-xl overflow-hidden border border-brand-200">
            <div className="bg-brand-600 px-5 py-3">
              <div className="text-white text-sm font-semibold">Maturity Simulation Result</div>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-0 divide-x divide-y divide-gray-100">
              {[
                ['Invested Amount',   `₹ ${parseFloat(simResult.principal || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`,        false],
                ['Rate Used',         `${simResult.rateUsed}% p.a.`,                                                                              false],
                ['Interest Method',   simResult.method ?? '—',                                                                                    false],
                ['Estimated Returns', `₹ ${parseFloat(simResult.interestAmount || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`,    true],
                ['Total Value',       `₹ ${parseFloat(simResult.maturityAmount || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`,    true],
              ].map(([label, val, highlight]) => (
                <div key={label} className={`px-5 py-4 ${highlight ? 'bg-brand-50' : 'bg-white'}`}>
                  <div className="text-xs text-gray-500 uppercase tracking-wide mb-1">{label}</div>
                  <div className={`font-bold ${highlight ? 'text-brand-700 text-lg' : 'text-gray-800'}`}>{val}</div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* --- SECTION: Product Catalog Table --- */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <h2 className="section-title mb-0">All Products</h2>
          <button className="btn-secondary text-sm" onClick={fetchAll}>Refresh</button>
        </div>
        {loading ? <LoadingSpinner /> : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  {['ID', 'Name', 'Category', 'Method', 'Min Amt', 'Max Amt', 'Tenure', 'Status']
                    .concat(canEdit ? ['Actions'] : [])
                    .map(h => <th key={h} className="th">{h}</th>)}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {products.map(p => (
                  <tr key={getProdId(p)} className="tr-hover">
                    <td className="td font-mono text-xs">{getProdId(p)}</td>
                    <td className="td font-medium">{getProdName(p)}</td>
                    <td className="td"><span className="badge-blue">{getProdCat(p)}</span></td>
                    <td className="td text-sm">{p.interestMethod || p.InterestMethod || '—'}</td>
                    <td className="td text-sm">{p.minAmount ?? p.MinAmount ?? '—'}</td>
                    <td className="td text-sm">{p.maxAmount ?? p.MaxAmount ?? '—'}</td>
                    <td className="td text-sm">
                      {(p.minTenure ?? p.MinTenure ?? '—')}–{(p.maxTenure ?? p.MaxTenure ?? '—')} mo
                    </td>
                    <td className="td">
                      <span className={getProdStatus(p) === 'ACTIVE' ? 'badge-green' : 'badge-red'}>
                        {getProdStatus(p)}
                      </span>
                    </td>
                    {canEdit && (
                      <td className="td space-x-2">
                        <button className="text-xs text-brand-600 hover:underline" onClick={() => startEdit(p)}>Edit</button>
                        {canDelete && (
                          <button className="text-xs text-red-600 hover:underline" onClick={() => handleDelete(getProdId(p))}>Delete</button>
                        )}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
