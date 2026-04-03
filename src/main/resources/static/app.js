/**
 * TradeJournal — Portfolio Analytics Dashboard
 * app.js — Frontend Application Logic
 *
 * API Endpoints expected from TradeController.java:
 *   POST /api/trades          — Submit a new trade
 *   GET  /api/trades          — Get all trades
 *   GET  /api/analytics       — Get portfolio summary + strategy stats
 */

'use strict';

/* ═══════════════════════════════════════════════════
   CONFIG
═══════════════════════════════════════════════════ */
const API_BASE = '/api';   // Adjust if your servlet is mapped differently
let allTrades = [];        // Cache for filtering

/* ═══════════════════════════════════════════════════
   NAVIGATION
═══════════════════════════════════════════════════ */
const sections = {
  dashboard:   { el: 'section-dashboard',   title: 'Portfolio Dashboard',  subtitle: 'Real-time analytics & risk management' },
  'trade-entry':{ el: 'section-trade-entry',title: 'Log New Trade',        subtitle: 'Submit and validate against risk rules' },
  analytics:   { el: 'section-analytics',   title: 'Analytics',            subtitle: 'Deep-dive into performance data' },
  history:     { el: 'section-history',     title: 'Trade History',        subtitle: 'Full log of all executed trades' },
};

document.querySelectorAll('.nav-item').forEach(link => {
  link.addEventListener('click', e => {
    e.preventDefault();
    const key = link.dataset.section;
    navigateTo(key);
  });
});

function navigateTo(key) {
  if (!sections[key]) return;

  // Update nav active state
  document.querySelectorAll('.nav-item').forEach(l => l.classList.remove('active'));
  document.querySelector(`[data-section="${key}"]`).classList.add('active');

  // Show/hide sections
  Object.values(sections).forEach(s => {
    document.getElementById(s.el).classList.add('hidden');
    document.getElementById(s.el).classList.remove('active');
  });
  const target = document.getElementById(sections[key].el);
  target.classList.remove('hidden');
  target.classList.add('active');

  // Update header
  document.getElementById('page-title').textContent = sections[key].title;
  document.getElementById('page-subtitle').textContent = sections[key].subtitle;

  // Load data for the section
  if (key === 'dashboard' || key === 'analytics') loadAnalytics();
  if (key === 'history') loadTradeHistory();
}

/* ═══════════════════════════════════════════════════
   CLOCK
═══════════════════════════════════════════════════ */
function updateClock() {
  const now = new Date();
  document.getElementById('header-time').textContent =
    now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false });
}
setInterval(updateClock, 1000);
updateClock();

/* ═══════════════════════════════════════════════════
   TRADE VALUE PREVIEW
═══════════════════════════════════════════════════ */
function updateTradeValue() {
  const qty   = parseFloat(document.getElementById('quantity').value) || 0;
  const price = parseFloat(document.getElementById('price').value) || 0;
  const total = qty * price;
  document.getElementById('trade-value').textContent = formatCurrency(total);
}

document.getElementById('quantity').addEventListener('input', updateTradeValue);
document.getElementById('price').addEventListener('input', updateTradeValue);

/* ═══════════════════════════════════════════════════
   TRADE FORM SUBMISSION
═══════════════════════════════════════════════════ */
document.getElementById('trade-form').addEventListener('submit', async e => {
  e.preventDefault();
  closeAlert();
  closeSuccessAlert();

  if (!validateForm()) return;

  const payload = {
    symbol:     document.getElementById('symbol').value.trim().toUpperCase(),
    tradeType:  document.getElementById('tradeType').value,
    quantity:   parseFloat(document.getElementById('quantity').value),
    price:      parseFloat(document.getElementById('price').value),
    strategyId: document.getElementById('strategyId').value.trim(),
  };

  setSubmitLoading(true);

  try {
    const response = await fetch(`${API_BASE}/trades`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
      body: JSON.stringify(payload),
    });

    const data = await response.json();

    if (response.ok) {
      // Trade accepted — update UI
      showSuccessAlert(data.message || `Trade for ${payload.symbol} logged successfully.`);
      resetForm();

      // If analytics come back with the response, render them immediately
      if (data.analytics) renderAnalytics(data.analytics);
      if (data.trades)    { allTrades = data.trades; renderHistoryTable(allTrades); }

    } else if (response.status === 403) {
      // Risk rule violation
      const ruleViolated = data.rule    || 'Unknown Rule';
      const detail       = data.detail  || data.message || 'Trade rejected by risk engine.';
      showRiskAlert(`[${ruleViolated}] ${detail}`);

    } else {
      showRiskAlert(data.message || `Server error: ${response.status}`);
    }

  } catch (err) {
    console.error('Submit error:', err);
    showRiskAlert('Unable to reach the server. Please check your connection.');
    setConnectionStatus(false);
  } finally {
    setSubmitLoading(false);
  }
});

function validateForm() {
  let valid = true;
  const fields = ['symbol', 'tradeType', 'quantity', 'price', 'strategyId'];

  fields.forEach(id => {
    const el = document.getElementById(id);
    el.classList.remove('error');
    if (!el.value || el.value === '') {
      el.classList.add('error');
      valid = false;
    }
  });

  if (!valid) {
    showRiskAlert('Please fill in all required fields before submitting.');
  }
  return valid;
}

function resetForm() {
  document.getElementById('trade-form').reset();
  document.getElementById('trade-value').textContent = '$0.00';
  document.querySelectorAll('.form-group input, .form-group select').forEach(el => el.classList.remove('error'));
}

function setSubmitLoading(loading) {
  const btn    = document.getElementById('submit-btn');
  const text   = btn.querySelector('.btn-text');
  const spinner = document.getElementById('btn-spinner');

  btn.disabled = loading;
  text.textContent = loading ? 'Validating…' : 'Submit Trade';
  spinner.classList.toggle('hidden', !loading);
}

/* ═══════════════════════════════════════════════════
   ANALYTICS — LOAD & RENDER
═══════════════════════════════════════════════════ */
async function loadAnalytics() {
  try {
    const res  = await fetch(`${API_BASE}/analytics`, { headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    renderAnalytics(data);
    setConnectionStatus(true);
  } catch (err) {
    console.error('Analytics load error:', err);
    setConnectionStatus(false);
  }
}

function renderAnalytics(data) {
  const pnl      = data.totalPnl      ?? 0;
  const winRate  = data.winRate        ?? 0;
  const total    = data.totalTrades    ?? 0;
  const wins     = data.winningTrades  ?? 0;
  const strategies = Array.isArray(data.strategyStats) ? data.strategyStats : [];

  // ─ KPI Cards ─
  const pnlEl = document.getElementById('total-pnl');
  pnlEl.textContent = formatCurrency(pnl);
  pnlEl.className   = 'kpi-value ' + colorClass(pnl);

  document.getElementById('win-rate').textContent = formatPercent(winRate);
  document.getElementById('total-trades').textContent = total;

  const uniqueStrats = new Set(strategies.map(s => s.strategyId)).size;
  document.getElementById('active-strategies').textContent = uniqueStrats;

  document.getElementById('pnl-trend').textContent     = pnl >= 0 ? '↑ Profitable' : '↓ Net loss';
  document.getElementById('winrate-trend').textContent = `${wins} winning trades`;

  // ─ Analytics section ─
  const anPnl = document.getElementById('an-pnl');
  anPnl.textContent = formatCurrency(pnl);
  anPnl.className   = 'stat-value ' + colorClass(pnl);
  document.getElementById('an-winrate').textContent = formatPercent(winRate);
  document.getElementById('an-trades').textContent  = total;
  document.getElementById('an-wins').textContent    = wins;

  // ─ Strategy badge ─
  document.getElementById('strategy-count').textContent = `${strategies.length} strateg${strategies.length === 1 ? 'y' : 'ies'}`;

  // ─ Strategy tables (both dashboard & analytics tabs) ─
  renderStrategyTable('strategy-tbody', strategies);
  renderStrategyTable('analytics-strategy-tbody', strategies);
}

function renderStrategyTable(tbodyId, strategies) {
  const tbody = document.getElementById(tbodyId);
  if (!strategies || strategies.length === 0) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="6">No strategy data available</td></tr>`;
    return;
  }

  // Determine max absolute PnL for bar scaling
  const maxAbs = Math.max(...strategies.map(s => Math.abs(s.totalPnl ?? 0)), 1);

  tbody.innerHTML = strategies.map(s => {
    const pnl     = s.totalPnl     ?? 0;
    const wr      = s.winRate      ?? 0;
    const trades  = s.totalTrades  ?? 0;
    const avgPnl  = trades > 0 ? pnl / trades : 0;
    const barPct  = Math.round((Math.abs(pnl) / maxAbs) * 100);
    const barCls  = pnl >= 0 ? 'pos' : 'neg';
    const status  = pnl >= 0 ? '<span class="badge badge-buy">ACTIVE</span>' : '<span class="badge badge-sell">REVIEW</span>';

    return `<tr>
      <td><strong style="color:var(--text-primary)">${escHtml(s.strategyId)}</strong></td>
      <td class="mono">${trades}</td>
      <td class="mono ${wr >= 50 ? 'positive' : 'negative'}">${formatPercent(wr)}</td>
      <td class="mono ${colorClass(pnl)}">${formatCurrency(pnl)}</td>
      <td class="mono ${colorClass(avgPnl)}">${formatCurrency(avgPnl)}</td>
      <td>
        <div class="perf-bar-wrap">
          <div class="perf-bar-bg"><div class="perf-bar-fill ${barCls}" style="width:${barPct}%"></div></div>
          <span class="perf-bar-label">${barPct}%</span>
        </div>
      </td>
    </tr>`;
  }).join('');
}

/* ═══════════════════════════════════════════════════
   TRADE HISTORY — LOAD & RENDER
═══════════════════════════════════════════════════ */
async function loadTradeHistory() {
  try {
    const res  = await fetch(`${API_BASE}/trades`, { headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    allTrades  = Array.isArray(data) ? data : (data.trades ?? []);
    renderHistoryTable(allTrades);
    setConnectionStatus(true);
  } catch (err) {
    console.error('Trade history load error:', err);
    setConnectionStatus(false);
  }
}

function renderHistoryTable(trades) {
  const tbody = document.getElementById('history-tbody');
  if (!trades || trades.length === 0) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="8">No trade history found</td></tr>`;
    return;
  }

  tbody.innerHTML = [...trades].reverse().map(t => {
    const typeBadge = t.tradeType === 'BUY'
      ? '<span class="badge badge-buy">BUY</span>'
      : '<span class="badge badge-sell">SELL</span>';
    const totalVal  = (t.quantity ?? 0) * (t.price ?? 0);
    const ts = t.timestamp ? new Date(t.timestamp).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' }) : '—';

    return `<tr>
      <td class="mono" style="color:var(--text-muted)">#${t.id ?? '—'}</td>
      <td><strong style="color:var(--text-primary);font-family:var(--font-display);font-size:13px">${escHtml(t.symbol ?? '—')}</strong></td>
      <td>${typeBadge}</td>
      <td class="mono">${(t.quantity ?? 0).toLocaleString()}</td>
      <td class="mono">${formatCurrency(t.price ?? 0)}</td>
      <td class="mono">${formatCurrency(totalVal)}</td>
      <td class="mono" style="color:var(--accent)">${escHtml(t.strategyId ?? '—')}</td>
      <td class="mono" style="color:var(--text-muted)">${ts}</td>
    </tr>`;
  }).join('');
}

function filterTrades(query) {
  const q = query.toLowerCase();
  const filtered = allTrades.filter(t =>
    (t.symbol     ?? '').toLowerCase().includes(q) ||
    (t.strategyId ?? '').toLowerCase().includes(q)
  );
  renderHistoryTable(filtered);
}

/* ═══════════════════════════════════════════════════
   ALERTS
═══════════════════════════════════════════════════ */
function showRiskAlert(message) {
  document.getElementById('alert-message').textContent = message;
  document.getElementById('risk-alert-panel').classList.remove('hidden');
  document.getElementById('success-alert-panel').classList.add('hidden');
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function closeAlert() {
  document.getElementById('risk-alert-panel').classList.add('hidden');
}

function showSuccessAlert(message) {
  document.getElementById('success-message').textContent = message;
  document.getElementById('success-alert-panel').classList.remove('hidden');
  document.getElementById('risk-alert-panel').classList.add('hidden');
  window.scrollTo({ top: 0, behavior: 'smooth' });
  setTimeout(closeSuccessAlert, 6000);
}

function closeSuccessAlert() {
  document.getElementById('success-alert-panel').classList.add('hidden');
}

/* ═══════════════════════════════════════════════════
   STATUS & REFRESH
═══════════════════════════════════════════════════ */
function setConnectionStatus(ok) {
  const dot  = document.querySelector('.status-dot');
  const text = document.querySelector('.status-text');
  dot.className  = 'status-dot' + (ok ? '' : ' error');
  text.textContent = ok ? 'Connected' : 'Offline';
}

document.getElementById('btn-refresh').addEventListener('click', () => {
  loadAnalytics();
  if (!document.getElementById('section-history').classList.contains('hidden')) {
    loadTradeHistory();
  }
});

/* ═══════════════════════════════════════════════════
   FORMATTERS & HELPERS
═══════════════════════════════════════════════════ */
function formatCurrency(val) {
  const abs = Math.abs(val);
  const formatted = abs.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  return (val < 0 ? '-$' : '$') + formatted;
}

function formatPercent(val) {
  return (val ?? 0).toFixed(1) + '%';
}

function colorClass(val) {
  if (val > 0) return 'positive';
  if (val < 0) return 'negative';
  return 'neutral';
}

function escHtml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

/* ═══════════════════════════════════════════════════
   INIT
═══════════════════════════════════════════════════ */
(function init() {
  loadAnalytics();
})();
