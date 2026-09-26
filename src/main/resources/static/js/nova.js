/* ── Connection state ──────────────────────────────────────────────────────── */
const Nova = (() => {

    // ── internal state ──────────────────────────────────────────────────────
    let _conn = JSON.parse(sessionStorage.getItem('nova_conn') || 'null');

    function saveConn(cfg) {
        _conn = cfg;
        sessionStorage.setItem('nova_conn', JSON.stringify(cfg));
        _renderConnWidget();
        document.dispatchEvent(new CustomEvent('nova:connected', {detail: cfg}));
    }

    function getConn() {
        return _conn;
    }

    function isConnected() {
        return !!_conn;
    }

    // ── BASE_URL ────────────────────────────────────────────────────────────
    const BASE_URL = '/backend/api/v1';

    async function apiFetch(path, opts = {}) {
        const res = await fetch(BASE_URL + path, {
            headers: {'Content-Type': 'application/json', ...(opts.headers || {})},
            ...opts
        });
        if (!res.ok) {
            const body = await res.text().catch(() => '');
            throw new Error(body || `HTTP ${res.status}`);
        }
        const ct = res.headers.get('Content-Type') || '';
        return ct.includes('application/json') ? res.json() : res.text();
    }

    // ── Toast ───────────────────────────────────────────────────────────────
    function toast(msg, type = 'info', duration = 3500) {
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
        const icons = {success: '✓', error: '✕', info: 'ℹ', warn: '⚠'};
        const t = document.createElement('div');
        t.className = `toast ${type === 'error' ? 'error' : type === 'success' ? 'success' : ''}`;
        t.innerHTML = `<span>${icons[type] || icons.info}</span><span>${msg}</span>`;
        container.appendChild(t);
        setTimeout(() => {
            t.style.opacity = '0';
            t.style.transition = 'opacity .3s';
            setTimeout(() => t.remove(), 320);
        }, duration);
    }

    // ── Modal ───────────────────────────────────────────────────────────────
    function openModal(id) {
        document.getElementById(id)?.classList.add('open');
    }

    function closeModal(id) {
        document.getElementById(id)?.classList.remove('open');
    }

    // Close on overlay click
    document.addEventListener('click', e => {
        if (e.target.classList.contains('modal-overlay')) {
            e.target.classList.remove('open');
        }
    });
    // Close on Escape
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal-overlay.open').forEach(m => m.classList.remove('open'));
        }
    });

    // ── Tabs ────────────────────────────────────────────────────────────────
    function initTabs(containerEl) {
        const tabs = containerEl.querySelectorAll('.tab');
        const panels = containerEl.querySelectorAll('.tab-panel');
        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                tabs.forEach(t => t.classList.remove('active'));
                panels.forEach(p => p.classList.remove('active'));
                tab.classList.add('active');
                const target = tab.dataset.tab;
                containerEl.querySelector(`.tab-panel[data-tab="${target}"]`)?.classList.add('active');
            });
        });
        // activate first by default
        if (tabs.length) {
            tabs[0].click();
        }
    }

    // ── Sidebar active link ──────────────────────────────────────────────────
    function _initSidebarActive() {
        const items = document.querySelectorAll('.nav-item[data-page]');
        const page = document.body.dataset.page;
        items.forEach(item => {
            if (item.dataset.page === page) item.classList.add('active');
            item.addEventListener('click', () => {
                const href = item.dataset.href;
                if (href) window.location.href = href;
            });
        });
    }

    // ── Connection widget render ─────────────────────────────────────────────
    function _renderConnWidget() {
        const dot = document.getElementById('conn-status-dot');
        const text = document.getElementById('conn-status-text');
        const host = document.getElementById('conn-host-label');
        if (!dot) return;
        if (_conn) {
            dot.className = 'status-dot connected';
            text.textContent = 'Connected';
            host.textContent = `${_conn.host}:${_conn.port} / ${_conn.database}`;
        } else {
            dot.className = 'status-dot disconnected';
            text.textContent = 'Not connected';
            host.textContent = '—';
        }
    }

    // ── Connect dialog logic ─────────────────────────────────────────────────
    function _initConnectModal() {
        const form = document.getElementById('connect-form');
        if (!form) return;

        form.addEventListener('submit', async e => {
            e.preventDefault();
            const btn = form.querySelector('[type=submit]');
            btn.disabled = true;
            btn.textContent = 'Connecting…';
            const cfg = {
                host: form.host.value.trim(),
                port: parseInt(form.port.value, 10) || 5432,
                database: form.database.value.trim(),
                username: form.username.value.trim(),
                password: form.password.value
            };
            try {
                const response = await fetch('/connect', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(cfg)
                });

                if (!response.ok) {
                    const message = await response.text();
                    toast(`Connection failed: ${message || 'Server error'}`, 'error');
                    return;
                }

                saveConn(cfg);
                closeModal('connect-modal');
                toast('Connected successfully — loading…', 'success');
                setTimeout(() => location.reload(), 800);
            } catch (err) {
                toast('Connection failed: ' + err.message, 'error');
            } finally {
                btn.disabled = false;
                btn.textContent = 'Connect';
            }
        });

        // pre-fill form from session
        if (_conn) {
            form.host.value = _conn.host || '';
            form.port.value = _conn.port || 5432;
            form.database.value = _conn.database || '';
            form.username.value = _conn.username || '';
        }
    }

    // ── Loading helpers ──────────────────────────────────────────────────────
    function skeletonRows(cols, rows = 5) {
        return Array.from({length: rows}, () =>
            `<tr>${Array.from({length: cols}, () =>
                `<td><div class="skeleton" style="height:13px;width:${60 + Math.random() * 30 | 0}%"></div></td>`
            ).join('')}</tr>`
        ).join('');
    }

    function fmtBytes(bytes) {
        if (!bytes && bytes !== 0) return '—';
        const units = ['B', 'KB', 'MB', 'GB', 'TB'];
        let i = 0;
        while (bytes >= 1024 && i < units.length - 1) {
            bytes /= 1024;
            i++;
        }
        return bytes.toFixed(i ? 1 : 0) + ' ' + units[i];
    }

    function fmtNum(n) {
        if (n == null) return '—';
        return Number(n).toLocaleString();
    }

    function fmtDur(secs) {
        if (secs == null) return '—';
        if (secs < 1) return Math.round(secs * 1000) + 'ms';
        if (secs < 60) return secs.toFixed(1) + 's';
        return (secs / 60).toFixed(1) + 'm';
    }

    // ── Init ─────────────────────────────────────────────────────────────────
    document.addEventListener('DOMContentLoaded', () => {
        _initSidebarActive();
        _renderConnWidget();
        _initConnectModal();
        _maybePromptConnect();
    });

    // ── Auto-prompt if not connected ─────────────────────────────────────────
    function _maybePromptConnect() {
        if (_conn) return;                          // already connected → do nothing

        const banner = document.getElementById('connect-welcome-banner');
        const hint   = document.getElementById('connect-modal-hint');

        // Show the welcome banner (hidden by default)
        if (banner) banner.style.display = 'block';

        // Helpful hint in the footer
        if (hint) hint.textContent = 'A database connection is required to use Nova.';

        // Small delay so the page paints first, then the modal slides in smoothly
        setTimeout(() => openModal('connect-modal'), 300);
    }


    return {
        getConn,
        isConnected,
        apiFetch,
        toast,
        openModal,
        closeModal,
        initTabs,
        skeletonRows,
        fmtBytes,
        fmtNum,
        fmtDur
    };
})();
