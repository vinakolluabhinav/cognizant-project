import { useState } from 'react'
import { syncCustomer, getAllCustomers, getCustomerByCif, updateCustomer } from '../../services/customerService'
import api from '../../services/api'
import LoadingSpinner from '../common/LoadingSpinner'

// --- Default Form States ---
const SYNC_INIT = { cifNumber: '', fullName: '', segment: 'RETAIL', kycStatus: 'PENDING', status: 'ACTIVE', userId: '', userEmail: '' }
const EDIT_INIT = { cifNumber: '', kycStatus: '', status: '' }

export default function OnboardCustomer() {
  // --- Sync Form States ---
  const [form, setForm]             = useState(SYNC_INIT)
  const [emailStatus, setEmailStatus] = useState('') // 'loading' | 'found' | 'not_found' | ''
  const [loading, setLoading]       = useState(false)
  const [msg, setMsg]               = useState('')
  const [error, setError]           = useState('')

  // --- Link User States ---
  const [linkCif, setLinkCif]         = useState('')
  const [linkEmail, setLinkEmail]     = useState('')
  const [linkLoading, setLinkLoading] = useState(false)
  const [linkMsg, setLinkMsg]         = useState('')
  const [linkError, setLinkError]     = useState('')

  // --- Edit Customer States ---
  const [editForm, setEditForm]         = useState(EDIT_INIT)
  const [editCustomer, setEditCustomer] = useState(null)
  const [lookupLoading, setLookupLoading] = useState(false)
  const [editLoading, setEditLoading]   = useState(false)
  const [editMsg, setEditMsg]           = useState('')
  const [editError, setEditError]       = useState('')

  // --- Customer List States ---
  const [customers, setCustomers]     = useState(null)
  const [listLoading, setListLoading] = useState(false)

  // --- Helpers: Update nested form state ---
  const set     = (k, v) => setForm(f => ({ ...f, [k]: v }))
  const setEdit = (k, v) => setEditForm(f => ({ ...f, [k]: v }))

  // --- Logic: Auto-lookup userId by email on blur ---
  const lookupEmail = async (email) => {
    if (!email || !email.includes('@')) return
    setEmailStatus('loading')
    try {
      const res = await api.get('/v1/users/by-email', { params: { email } })
      set('userId', res.data.userId)
      setEmailStatus('found')
    } catch {
      set('userId', '')
      setEmailStatus('not_found')
    }
  }

  // --- Logic: Sync New Customer ---
  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setMsg('')
    setError('')
    try {
      const r = await syncCustomer(form)
      const linkNote = form.userId ? ` (linked to user ID: ${form.userId})` : ' (no user linked yet)'
      setMsg(`✓ Customer synced — CIF: ${r.cifNumber || form.cifNumber}${linkNote}`)
      setForm(SYNC_INIT)
      setEmailStatus('')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to sync customer')
    } finally {
      setLoading(false)
    }
  }

  // --- Logic: Link Existing Customer to a User Account ---
  const handleLink = async (e) => {
    e.preventDefault()
    setLinkLoading(true)
    setLinkMsg('')
    setLinkError('')
    try {
      // Step 1 — resolve email → userId
      const userRes = await api.get('/v1/users/by-email', { params: { email: linkEmail } })
      const userId  = userRes.data.userId
      const name    = userRes.data.name
      // Step 2 — patch the customer record with the resolved userId
      await api.patch(`/v1/customer-reference/${linkCif}/link-user`, null, { params: { userId } })
      setLinkMsg(`✓ Customer ${linkCif} linked to user "${name}" (ID: ${userId})`)
      setLinkCif('')
      setLinkEmail('')
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || ''
      if (err.response?.status === 404 && linkEmail) {
        setLinkError(`No user found with email: ${linkEmail}`)
      } else if (err.response?.status === 404 && linkCif) {
        setLinkError(`No customer found with CIF: ${linkCif}`)
      } else {
        setLinkError(typeof msg === 'string' ? msg : 'Failed to link user')
      }
    } finally {
      setLinkLoading(false)
    }
  }

  // --- Logic: Look Up Customer for Editing ---
  const handleLookup = async () => {
    if (!editForm.cifNumber) return
    setLookupLoading(true)
    setEditError('')
    setEditCustomer(null)
    try {
      const c = await getCustomerByCif(editForm.cifNumber)
      setEditCustomer(c)
      setEditForm({ cifNumber: c.cifNumber, kycStatus: c.kycStatus, status: c.status })
    } catch (err) {
      setEditError(err.response?.data?.message || 'Customer not found')
    } finally {
      setLookupLoading(false)
    }
  }

  // --- Logic: Update Customer KYC and Status ---
  const handleUpdate = async (e) => {
    e.preventDefault()
    setEditLoading(true)
    setEditMsg('')
    setEditError('')
    try {
      await updateCustomer(editForm.cifNumber, editForm.kycStatus, editForm.status)
      setEditMsg('Customer updated successfully')
      setEditCustomer(null)
      setEditForm(EDIT_INIT)
    } catch (err) {
      setEditError(err.response?.data?.message || 'Failed to update customer')
    } finally {
      setEditLoading(false)
    }
  }

  // --- Logic: Load All Customers ---
  const handleViewAll = async () => {
    setListLoading(true)
    try {
      setCustomers(await getAllCustomers())
    } catch {
      /* silent */
    } finally {
      setListLoading(false)
    }
  }

  return (
    <div>
      <h1 className="page-title mb-6">Onboard Customer</h1>

      {/* --- SECTION: Sync New Customer --- */}
      <div className="card mb-6">
        <h2 className="section-title">Sync New Customer</h2>
        <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="label">CIF Number *</label>
            <input className="input" value={form.cifNumber} onChange={e => set('cifNumber', e.target.value)} placeholder="e.g. CIF005" required />
          </div>
          <div>
            <label className="label">Full Name *</label>
            <input className="input" value={form.fullName} onChange={e => set('fullName', e.target.value)} placeholder="John Doe" required />
          </div>
          <div>
            <label className="label">Segment *</label>
            <select className="input" value={form.segment} onChange={e => set('segment', e.target.value)}>
              <option value="RETAIL">RETAIL</option>
              <option value="CORPORATE">CORPORATE</option>
            </select>
          </div>
          <div>
            {/* Email field — auto-looks up userId on blur */}
            <label className="label">
              Customer Email
              <span className="text-gray-400 font-normal text-xs ml-1">(optional — links to their login account)</span>
            </label>
            <div className="relative">
              <input
                className="input pr-28"
                type="email"
                value={form.userEmail}
                onChange={e => { set('userEmail', e.target.value); setEmailStatus('') }}
                onBlur={e => lookupEmail(e.target.value)}
                placeholder="customer@gmail.com"
              />
              <div className="absolute right-3 top-1/2 -translate-y-1/2 text-xs pointer-events-none">
                {emailStatus === 'loading'   && <span className="text-gray-400">Searching…</span>}
                {emailStatus === 'found'     && <span className="text-green-600 font-semibold">✓ User found</span>}
                {emailStatus === 'not_found' && <span className="text-amber-500">⚠ Not registered yet</span>}
              </div>
            </div>
            {emailStatus === 'not_found' && (
              <p className="text-xs text-amber-600 mt-1">Customer can be synced now and linked later once they register.</p>
            )}
          </div>
          <div>
            <label className="label">KYC Status *</label>
            <select className="input" value={form.kycStatus} onChange={e => set('kycStatus', e.target.value)}>
              <option>PENDING</option><option>VERIFIED</option><option>REJECTED</option>
            </select>
          </div>
          <div>
            <label className="label">Account Status *</label>
            <select className="input" value={form.status} onChange={e => set('status', e.target.value)}>
              <option>ACTIVE</option><option>INACTIVE</option><option>SUSPENDED</option>
            </select>
          </div>
          <div className="sm:col-span-2 flex gap-3 pt-2">
            <button className="btn-primary" type="submit" disabled={loading}>
              {loading ? 'Syncing…' : 'Sync Customer'}
            </button>
          </div>
        </form>
        {msg   && <div className="alert-success mt-3">{msg}</div>}
        {error && <div className="alert-error mt-3">{error}</div>}
      </div>

      {/* --- SECTION: Link User to Existing Customer --- */}
      <div className="card mb-6">
        <h2 className="section-title">Link User to Existing Customer</h2>
        <p className="text-sm text-gray-500 mb-4">
          Use this when a customer was synced before they registered. Enter their CIF and registered email to link them.
        </p>
        <form onSubmit={handleLink} className="grid grid-cols-1 sm:grid-cols-3 gap-4 items-end">
          <div>
            <label className="label">CIF Number *</label>
            <input className="input" value={linkCif} onChange={e => { setLinkCif(e.target.value); setLinkMsg(''); setLinkError('') }} placeholder="e.g. CIF005" required />
          </div>
          <div>
            <label className="label">Registered Email *</label>
            <input className="input" type="email" value={linkEmail} onChange={e => { setLinkEmail(e.target.value); setLinkMsg(''); setLinkError('') }} placeholder="customer@gmail.com" required />
          </div>
          <button className="btn-primary" type="submit" disabled={linkLoading}>
            {linkLoading ? 'Linking…' : 'Link Account'}
          </button>
        </form>
        {linkMsg   && <div className="alert-success mt-3">{linkMsg}</div>}
        {linkError && <div className="alert-error mt-3">{linkError}</div>}
      </div>

      {/* --- SECTION: Update Customer Status --- */}
      <div className="card mb-6">
        <h2 className="section-title">Update Customer Status</h2>
        <div className="flex gap-3 mb-4">
          <input
            className="input flex-1 max-w-xs"
            placeholder="Enter CIF Number"
            value={editForm.cifNumber}
            onChange={e => setEdit('cifNumber', e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleLookup()}
          />
          <button className="btn-secondary" onClick={handleLookup} disabled={lookupLoading}>
            {lookupLoading ? 'Looking up…' : 'Look Up'}
          </button>
        </div>
        {editCustomer && (
          <form onSubmit={handleUpdate} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Customer summary banner */}
            <div className="sm:col-span-2 p-3 bg-gray-50 rounded-lg text-sm flex items-center gap-3">
              <span className="font-semibold text-gray-800">{editCustomer.fullName}</span>
              <span className="badge-blue">{editCustomer.segment}</span>
              <span className="text-gray-400 text-xs">CIF: {editCustomer.cifNumber}</span>
              {editCustomer.userId
                ? <span className="text-green-600 text-xs font-medium">✓ Linked (User ID: {editCustomer.userId})</span>
                : <span className="text-amber-500 text-xs">⚠ No user linked</span>
              }
            </div>
            <div>
              <label className="label">KYC Status</label>
              <select className="input" value={editForm.kycStatus} onChange={e => setEdit('kycStatus', e.target.value)}>
                <option>PENDING</option><option>VERIFIED</option><option>REJECTED</option>
              </select>
            </div>
            <div>
              <label className="label">Account Status</label>
              <select className="input" value={editForm.status} onChange={e => setEdit('status', e.target.value)}>
                <option>ACTIVE</option><option>INACTIVE</option><option>SUSPENDED</option>
              </select>
            </div>
            <div className="sm:col-span-2 flex gap-3">
              <button className="btn-primary" type="submit" disabled={editLoading}>
                {editLoading ? 'Updating…' : 'Update Customer'}
              </button>
              <button type="button" className="btn-secondary" onClick={() => { setEditCustomer(null); setEditForm(EDIT_INIT) }}>
                Cancel
              </button>
            </div>
          </form>
        )}
        {editMsg   && <div className="alert-success mt-3">{editMsg}</div>}
        {editError && <div className="alert-error mt-3">{editError}</div>}
      </div>

      {/* --- SECTION: All Customers Table --- */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <h2 className="section-title mb-0">All Customers</h2>
          <button className="btn-secondary text-sm" onClick={handleViewAll}>Load / Refresh</button>
        </div>
        {listLoading && <LoadingSpinner />}
        {customers && (
          customers.length === 0
            ? <p className="text-center text-gray-400 py-8">No customers found</p>
            : (
              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      {['CIF', 'Name', 'Segment', 'KYC', 'Status', 'User Linked', 'Created'].map(h => (
                        <th key={h} className="th">{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {customers.map(c => (
                      <tr key={c.cifNumber} className="tr-hover">
                        <td className="td font-mono text-xs">{c.cifNumber}</td>
                        <td className="td font-medium">{c.fullName}</td>
                        <td className="td"><span className="badge-blue">{c.segment}</span></td>
                        <td className="td">
                          <span className={c.kycStatus === 'VERIFIED' ? 'badge-green' : c.kycStatus === 'REJECTED' ? 'badge-red' : 'badge-yellow'}>
                            {c.kycStatus}
                          </span>
                        </td>
                        <td className="td">
                          <span className={c.status === 'ACTIVE' ? 'badge-green' : 'badge-red'}>{c.status}</span>
                        </td>
                        <td className="td text-xs">
                          {c.userId
                            ? <span className="text-green-600 font-medium">✓ ID: {c.userId}</span>
                            : <span className="text-amber-500">Not linked</span>
                          }
                        </td>
                        <td className="td text-gray-400 text-xs">
                          {c.createdAt ? new Date(c.createdAt).toLocaleDateString() : '—'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
        )}
      </div>
    </div>
  )
}
