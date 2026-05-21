import { useState, useRef, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { useLocation } from 'react-router-dom'

// --- Configuration: Role-specific system prompt context ---
const ROLE_CONTEXT = {
  CUSTOMER: `You are a helpful banking assistant for DepositCoreX. 
The user is a CUSTOMER. Help them with:
- Understanding their account balances and transactions
- Explaining how interest works on their savings accounts
- Guiding them to generate statements
- Explaining what Fixed Deposits (FD) are and how maturity works
- Answering questions about holds on their account
- Navigating to the right section of the app
Keep answers concise, friendly, and in simple language.`,

  BRANCH_OFFICER: `You are an expert banking operations assistant for DepositCoreX.
The user is a BRANCH OFFICER. Help them with:
- Customer onboarding process and KYC requirements
- Opening CASA (Savings/Current) and Term Deposit accounts
- Posting transactions (debit/credit)
- Placing holds and creating standing instructions
- Processing premature TD closures and penalty calculations
- Generating account statements for customers
- Troubleshooting common errors
Be precise and professional.`,

  OPERATIONS_OFFICER: `You are an expert banking operations assistant for DepositCoreX.
The user is an OPERATIONS OFFICER. Help them with:
- Reviewing and reversing transactions
- Understanding GL (General Ledger) postings and double-entry accounting
- Manually accruing and posting interest
- Processing TD maturity (PAYOUT vs RENEW)
- Managing holds and standing instructions
- Generating reports (ALL/CASA/FD/MONTHLY scopes)
- Sending notifications to customers
Be technical and precise.`,

  FINANCE_ANALYST: `You are a financial analysis assistant for DepositCoreX.
The user is a FINANCE ANALYST. Help them with:
- Analyzing GL postings and identifying discrepancies
- Understanding interest accrual vs posting difference
- Product configuration — interest slabs, tenures, methods (SIMPLE vs COMPOUNDED)
- Maturity simulation — how to calculate FD returns
- Reading deposit reports and understanding metrics
- Interpreting transaction data
Be analytical and detailed.`,

  CORE_ADMIN: `You are a system administration assistant for DepositCoreX.
The user is a CORE ADMIN with full system access. Help them with:
- User management — creating, activating, deactivating users
- Role assignments for staff (BRANCH_OFFICER, OPERATIONS_OFFICER, FINANCE_ANALYST)
- Customer record management and CIF linking
- Product catalog management
- System-wide transaction oversight
- Any module in the platform
Be comprehensive and technically detailed.`,
}

// --- Configuration: Page-aware context hints ---
const PAGE_HINTS = {
  '/accounts':      'User is currently viewing their accounts.',
  '/transactions':  'User is currently on the transactions page.',
  '/statements':    'User is currently on statements — they may want to generate one.',
  '/notifications': 'User is viewing their notifications.',
  '/onboard':       'User is on the customer onboarding page.',
  '/open-account':  'User is opening a new account.',
  '/transaction':   'User is posting a transaction.',
  '/holds':         'User is on the Holds & Standing Instructions page.',
  '/td-closure':    'User is processing a TD premature closure.',
  '/interest':      'User is on the interest accrual/posting page.',
  '/td':            'User is on the TD maturity processing page.',
  '/gl':            'User is viewing GL postings.',
  '/reports':       'User is on the reports generation page.',
  '/products':      'User is configuring deposit products.',
  '/users':         'User is on the user management page.',
  '/customers':     'User is viewing customer records.',
}

// --- Quick Suggestion Chips by Role ---
const SUGGESTIONS = {
  CUSTOMER: [
    'What is my interest rate?',
    'How do I generate a statement?',
    'What is a Fixed Deposit?',
    'Why is there a hold on my account?',
  ],
  BRANCH_OFFICER: [
    'How do I onboard a new customer?',
    'What KYC is needed to open an account?',
    'How to process premature FD closure?',
    'How do standing instructions work?',
  ],
  OPERATIONS_OFFICER: [
    'How do I reverse a transaction?',
    'Difference between accrual and posting?',
    'How to process TD maturity?',
    'What are GL postings?',
  ],
  FINANCE_ANALYST: [
    'How is compound interest calculated?',
    'What is the difference between FD and RD?',
    'How do I simulate FD returns?',
    'What does the MONTHLY report show?',
  ],
  CORE_ADMIN: [
    'How do I create a new staff user?',
    'How to link customer to user account?',
    'How do I configure a new FD product?',
    'How does the interest scheduler work?',
  ],
}

export default function AIAssistant() {
  const { auth }     = useAuth()
  const location     = useLocation()

  // --- UI States ---
  const [open, setOpen]         = useState(false)
  const [minimized, setMinimized] = useState(false)

  // --- Chat States ---
  const [messages, setMessages] = useState([])
  const [input, setInput]       = useState('')
  const [loading, setLoading]   = useState(false)

  const messagesEndRef = useRef(null)
  const inputRef       = useRef(null)

  // --- Effects: Scroll to bottom on new message ---
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // --- Effects: Focus input when chat opens ---
  useEffect(() => {
    if (open && !minimized) {
      setTimeout(() => inputRef.current?.focus(), 100)
    }
  }, [open, minimized])

  // --- Logic: Get page hint based on current URL ---
  const getPageHint = () => {
    const path = location.pathname
    for (const [key, hint] of Object.entries(PAGE_HINTS)) {
      if (path.includes(key)) return hint
    }
    return ''
  }

  // --- Logic: Send message to Claude API ---
  const sendMessage = async (text) => {
    const userText = (text || input).trim()
    if (!userText || loading) return

    const userMsg = { role: 'user', content: userText }
    const updatedMessages = [...messages, userMsg]
    setMessages(updatedMessages)
    setInput('')
    setLoading(true)

    try {
      // Build system prompt with role context + page awareness
      const pageHint  = getPageHint()
      const systemPrompt = `${ROLE_CONTEXT[auth?.role] || ROLE_CONTEXT.CUSTOMER}

Current user: ${auth?.name} (Role: ${auth?.role})
${pageHint ? `Context: ${pageHint}` : ''}

Platform: DepositCoreX — Core Banking Deposits Platform
Modules: IAM, Customer Onboarding, CASA Accounts, Product Config, Transactions, Interest Accrual/Posting, Holds & SI, TD Servicing, Statements, Notifications

Always be helpful, concise, and accurate. If asked about something outside banking, politely redirect to banking topics.`

      const response = await fetch('https://api.anthropic.com/v1/messages', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model:      'claude-sonnet-4-20250514',
          max_tokens: 1000,
          system:     systemPrompt,
          messages:   updatedMessages,
        }),
      })

      const data = await response.json()
      const reply = data.content?.[0]?.text || 'Sorry, I could not get a response.'

      setMessages(prev => [...prev, { role: 'assistant', content: reply }])
    } catch (err) {
      setMessages(prev => [...prev, {
        role:    'assistant',
        content: '⚠️ I could not connect right now. Please try again.',
      }])
    } finally {
      setLoading(false)
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  const clearChat = () => setMessages([])

  // --- Helpers: Format message text with basic markdown-like styling ---
  const formatText = (text) => {
    return text
      .split('\n')
      .map((line, i) => {
        if (line.startsWith('**') && line.endsWith('**')) {
          return <p key={i} className="font-semibold">{line.slice(2, -2)}</p>
        }
        if (line.startsWith('- ') || line.startsWith('• ')) {
          return <p key={i} className="pl-3">• {line.slice(2)}</p>
        }
        if (line.trim() === '') return <br key={i} />
        return <p key={i}>{line}</p>
      })
  }

  // Don't render if not logged in
  if (!auth) return null

  const suggestions = SUGGESTIONS[auth?.role] || SUGGESTIONS.CUSTOMER

  return (
    <>
      {/* ── Floating Button ── */}
      {!open && (
        <button
          onClick={() => setOpen(true)}
          className="fixed bottom-6 right-6 z-50 w-14 h-14 bg-brand-600 hover:bg-brand-700 text-white rounded-full shadow-lg flex items-center justify-center text-2xl transition-all hover:scale-110 active:scale-95"
          title="AI Banking Assistant"
        >
          🤖
        </button>
      )}

      {/* ── Chat Window ── */}
      {open && (
        <div className={`fixed bottom-6 right-6 z-50 w-96 bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col transition-all ${
          minimized ? 'h-14' : 'h-[560px]'
        }`}>

          {/* Header */}
          <div className="flex items-center justify-between px-4 py-3 bg-brand-600 rounded-t-2xl">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center text-lg">🤖</div>
              <div>
                <p className="text-white text-sm font-semibold leading-tight">DCX Assistant</p>
                <p className="text-brand-100 text-xs">AI-powered banking help</p>
              </div>
            </div>
            <div className="flex items-center gap-1">
              {/* Clear chat */}
              {messages.length > 0 && !minimized && (
                <button onClick={clearChat}
                  className="text-white/60 hover:text-white text-xs px-2 py-1 rounded transition-colors"
                  title="Clear chat">
                  🗑️
                </button>
              )}
              {/* Minimize */}
              <button onClick={() => setMinimized(m => !m)}
                className="text-white/70 hover:text-white w-7 h-7 flex items-center justify-center rounded transition-colors"
                title={minimized ? 'Expand' : 'Minimize'}>
                {minimized ? '▲' : '▼'}
              </button>
              {/* Close */}
              <button onClick={() => { setOpen(false); setMinimized(false) }}
                className="text-white/70 hover:text-white w-7 h-7 flex items-center justify-center rounded transition-colors"
                title="Close">
                ✕
              </button>
            </div>
          </div>

          {/* Body — hidden when minimized */}
          {!minimized && (
            <>
              {/* Messages Area */}
              <div className="flex-1 overflow-y-auto px-4 py-3 space-y-3 scrollbar-hide">
                {/* Welcome message */}
                {messages.length === 0 && (
                  <div className="space-y-3">
                    <div className="flex gap-2">
                      <div className="w-7 h-7 bg-brand-100 rounded-full flex items-center justify-center text-sm flex-shrink-0 mt-0.5">🤖</div>
                      <div className="bg-gray-100 rounded-2xl rounded-tl-none px-3 py-2 text-sm text-gray-700 max-w-[85%]">
                        <p className="font-medium mb-1">Hi {auth.name}! 👋</p>
                        <p className="text-xs text-gray-500">
                          I'm your AI banking assistant. Ask me anything about DepositCoreX — accounts, transactions, interest, or how to use any feature.
                        </p>
                      </div>
                    </div>

                    {/* Suggestion chips */}
                    <div className="space-y-1.5">
                      <p className="text-xs text-gray-400 pl-9">Try asking:</p>
                      {suggestions.map((s, i) => (
                        <button key={i} onClick={() => sendMessage(s)}
                          className="block w-full text-left text-xs bg-brand-50 hover:bg-brand-100 text-brand-700 rounded-xl px-3 py-2 transition-colors border border-brand-100">
                          {s}
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                {/* Chat messages */}
                {messages.map((m, i) => (
                  <div key={i} className={`flex gap-2 ${m.role === 'user' ? 'flex-row-reverse' : ''}`}>
                    {/* Avatar */}
                    <div className={`w-7 h-7 rounded-full flex items-center justify-center text-sm flex-shrink-0 mt-0.5 ${
                      m.role === 'user' ? 'bg-brand-600 text-white text-xs font-bold' : 'bg-brand-100'
                    }`}>
                      {m.role === 'user' ? auth.name[0].toUpperCase() : '🤖'}
                    </div>
                    {/* Bubble */}
                    <div className={`rounded-2xl px-3 py-2 text-sm max-w-[80%] ${
                      m.role === 'user'
                        ? 'bg-brand-600 text-white rounded-tr-none'
                        : 'bg-gray-100 text-gray-700 rounded-tl-none'
                    }`}>
                      <div className="space-y-0.5 text-xs leading-relaxed">
                        {formatText(m.content)}
                      </div>
                    </div>
                  </div>
                ))}

                {/* Typing indicator */}
                {loading && (
                  <div className="flex gap-2">
                    <div className="w-7 h-7 bg-brand-100 rounded-full flex items-center justify-center text-sm flex-shrink-0">🤖</div>
                    <div className="bg-gray-100 rounded-2xl rounded-tl-none px-4 py-3">
                      <div className="flex gap-1 items-center">
                        <span className="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                        <span className="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                        <span className="w-1.5 h-1.5 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
                      </div>
                    </div>
                  </div>
                )}
                <div ref={messagesEndRef} />
              </div>

              {/* Input Area */}
              <div className="px-3 py-3 border-t border-gray-100">
                <div className="flex gap-2 items-end">
                  <textarea
                    ref={inputRef}
                    className="flex-1 resize-none rounded-xl border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:border-brand-400 focus:ring-1 focus:ring-brand-100 max-h-24 min-h-[38px]"
                    placeholder="Ask me anything about banking…"
                    value={input}
                    onChange={e => setInput(e.target.value)}
                    onKeyDown={handleKeyDown}
                    rows={1}
                  />
                  <button
                    onClick={() => sendMessage()}
                    disabled={!input.trim() || loading}
                    className="w-9 h-9 bg-brand-600 hover:bg-brand-700 disabled:bg-gray-200 disabled:cursor-not-allowed text-white rounded-xl flex items-center justify-center transition-colors flex-shrink-0"
                  >
                    {loading ? (
                      <span className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    ) : (
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-4 h-4">
                        <path d="M3.478 2.405a.75.75 0 00-.926.94l2.432 7.905H13.5a.75.75 0 010 1.5H4.984l-2.432 7.905a.75.75 0 00.926.94 60.519 60.519 0 0018.445-8.986.75.75 0 000-1.218A60.517 60.517 0 003.478 2.405z" />
                      </svg>
                    )}
                  </button>
                </div>
                <p className="text-xs text-gray-400 mt-1.5 text-center">
                  Powered by Claude AI · Press Enter to send
                </p>
              </div>
            </>
          )}
        </div>
      )}
    </>
  )
}
