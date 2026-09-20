const QueryPage = (() => {

    const LIMIT = 20;

    function truncate(s, n = 80) {
        if (!s) return '—';
        return s.length > n ? s.slice(0, n) + '…' : s;
    }

    function qRow(q, cols) {
        const cells = cols.map(c => `<td>${c(q)}</td>`).join('');
        return `<tr style="cursor:pointer" onclick="QueryPage.showDetail(${q.queryId})">${cells}</tr>`;
    }

    function renderRows(bodyId, rows, cols) {
        const body = document.getElementById(bodyId);
        if (!rows || !rows.length) {
            body.innerHTML = `<tr><td colspan="${cols.length}" class="text-muted" style="padding:20px;text-align:center">No data</td></tr>`;
            return;
        }
        body.innerHTML = rows.map(r => qRow(r, cols)).join('');
    }

    /* column definitions per tab */
    const COLS = {
        top: [
            q => `<span title="${q.query}">${truncate(q.query)}</span>`,
            q => Nova.fmtNum(q.calls),
            q => Nova.fmtDur(q.totalExecTime / 1000),
            q => Nova.fmtDur(q.meanExecTime / 1000),
            q => Nova.fmtNum(q.rows),
        ],
        slow: [
            q => `<span title="${q.query}">${truncate(q.query)}</span>`,
            q => Nova.fmtNum(q.calls),
            q => Nova.fmtDur(q.maxExecTime / 1000),
            q => Nova.fmtDur(q.meanExecTime / 1000),
            q => Nova.fmtDur(q.stddevExecTime / 1000),
        ],
        calls: [
            q => `<span title="${q.query}">${truncate(q.query)}</span>`,
            q => Nova.fmtNum(q.calls),
            q => Nova.fmtDur(q.totalExecTime / 1000),
            q => Nova.fmtNum(q.rows),
            q => Nova.fmtDur(q.meanExecTime / 1000),
        ],
        io: [
            q => `<span title="${q.query}">${truncate(q.query)}</span>`,
            q => Nova.fmtNum(q.calls),
            q => Nova.fmtNum(q.sharedBlksRead),
            q => Nova.fmtNum(q.sharedBlksHit),
            q => Nova.fmtDur(q.blkReadTime / 1000),
        ],
        cpu: [
            q => `<span title="${q.query}">${truncate(q.query)}</span>`,
            q => Nova.fmtNum(q.calls),
            q => Nova.fmtDur(q.totalExecTime / 1000),
            q => Nova.fmtDur(q.meanExecTime / 1000),
            q => Nova.fmtNum(q.rows),
        ],
        temp: [
            q => `<span title="${q.query}">${truncate(q.query)}</span>`,
            q => Nova.fmtNum(q.calls),
            q => Nova.fmtNum(q.tempBlksRead),
            q => Nova.fmtNum(q.tempBlksWritten),
            q => Nova.fmtDur(q.meanExecTime / 1000),
        ],
    };

    /* cache for detail lookup */
    let _cache = {};

    async function loadTab(tab) {
        const endpoints = {
            top: '/queries/top',
            slow: '/queries/slow',
            calls: '/queries/calls',
            io: '/queries/io',
            cpu: '/queries/cpu',
            temp: '/queries/temp',
        };
        const bodyIds = {
            top: 'q-top-body', slow: 'q-slow-body', calls: 'q-calls-body',
            io: 'q-io-body', cpu: 'q-cpu-body', temp: 'q-temp-body',
        };

        const ep = endpoints[tab];
        const bodyId = bodyIds[tab];
        if (!ep || !bodyId) return;

        // skeleton
        document.getElementById(bodyId).innerHTML = Nova.skeletonRows(5);

        try {
            const data = await Nova.apiFetch(`${ep}?limit=${LIMIT}`);
            data.forEach(q => {
                _cache[q.queryId] = q;
            });
            renderRows(bodyId, data, COLS[tab]);
        } catch (err) {
            document.getElementById(bodyId).innerHTML =
                `<tr><td colspan="5" class="text-muted" style="padding:20px;text-align:center">${err.message}</td></tr>`;
        }
    }

    function showDetail(queryId) {
        const q = _cache[queryId];
        if (!q) return;

        document.getElementById('query-detail-sql').textContent = q.query ?? '—';
        document.getElementById('query-detail-meta').innerHTML = [
            `<tr><td style="color:var(--text-muted)">Calls</td><td>${Nova.fmtNum(q.calls)}</td></tr>`,
            `<tr><td style="color:var(--text-muted)">Total time</td><td>${Nova.fmtDur(q.totalExecTime / 1000)}</td></tr>`,
            `<tr><td style="color:var(--text-muted)">Mean time</td><td>${Nova.fmtDur(q.meanExecTime / 1000)}</td></tr>`,
            `<tr><td style="color:var(--text-muted)">Min / Max</td><td>${Nova.fmtDur(q.minExecTime / 1000)} / ${Nova.fmtDur(q.maxExecTime / 1000)}</td></tr>`,
            `<tr><td style="color:var(--text-muted)">Rows</td><td>${Nova.fmtNum(q.rows)}</td></tr>`,
            `<tr><td style="color:var(--text-muted)">Shared blks hit</td><td>${Nova.fmtNum(q.sharedBlksHit)}</td></tr>`,
            `<tr><td style="color:var(--text-muted)">Shared blks read</td><td>${Nova.fmtNum(q.sharedBlksRead)}</td></tr>`,
            `<tr><td style="color:var(--text-muted)">Temp blks read</td><td>${Nova.fmtNum(q.tempBlksRead)}</td></tr>`,
            `<tr><td style="color:var(--text-muted)">Temp blks written</td><td>${Nova.fmtNum(q.tempBlksWritten)}</td></tr>`,
        ].join('');

        Nova.openModal('query-detail-modal');
    }

    function initTabs() {
        const container = document.getElementById('query-tabs');
        const tabs = container.querySelectorAll('.tab');
        const panels = container.querySelectorAll('.tab-panel');
        let loaded = new Set();

        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                tabs.forEach(t => t.classList.remove('active'));
                panels.forEach(p => p.classList.remove('active'));
                tab.classList.add('active');
                const key = tab.dataset.tab;
                container.querySelector(`.tab-panel[data-tab="${key}"]`)?.classList.add('active');
                if (Nova.isConnected() && !loaded.has(key)) {
                    loaded.add(key);
                    loadTab(key);
                }
            });
        });

        if (tabs.length) tabs[0].click();
    }

    async function load() {
        if (!Nova.isConnected()) return;
        // load the currently active tab
        const activeTab = document.querySelector('#query-tabs .tab.active');
        if (activeTab) loadTab(activeTab.dataset.tab);
    }

    document.addEventListener('DOMContentLoaded', () => {
        initTabs();
        load();
    });
    document.addEventListener('nova:connected', load);

    return {showDetail};
})();
