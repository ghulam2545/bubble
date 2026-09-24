(() => {

    function badge(ok) {
        const color = ok ? 'var(--success, #22c55e)' : 'var(--danger, #ef4444)';
        return `<span style="color:${color};font-size:11px;font-weight:600">${ok ? '✓' : '✗'}</span>`;
    }

    function rowIndex(i) {
        return `<tr>
            <td class="mono">${i.indexName}</td>
            <td>${i.schema}</td>
            <td>${i.table}</td>
            <td>${i.indexType}</td>
            <td>${i.isUnique ? badge(true) + ' Yes' : 'No'}</td>
            <td>${i.sizePretty || '—'}</td>
            <td>${Nova.fmtNum(i.indexScans)}</td>
        </tr>`;
    }

    function rowHealth(h) {
        const statusColor = h.status === 'OK' ? 'var(--success,#22c55e)' : 'var(--warning,#f59e0b)';
        return `<tr>
            <td class="mono">${h.indexName}</td>
            <td>${h.schema}</td>
            <td>${h.table}</td>
            <td style="color:${statusColor};font-weight:600">${h.status}</td>
            <td>${h.sizePretty || '—'}</td>
            <td>${Nova.fmtNum(h.indexScans)}</td>
            <td style="font-size:11px;color:var(--text-muted)">${(h.issues || []).join(', ') || '—'}</td>
        </tr>`;
    }

    function rowSimple(i) {
        return `<tr>
            <td class="mono">${i.indexName}</td>
            <td>${i.schema}</td>
            <td>${i.table}</td>
            <td>${i.indexType}</td>
            <td>${i.sizePretty || '—'}</td>
            <td>${Nova.fmtNum(i.indexScans)}</td>
        </tr>`;
    }

    function rowInvalid(i) {
        return `<tr>
            <td class="mono">${i.indexName}</td>
            <td>${i.schema}</td>
            <td>${i.table}</td>
            <td>${i.indexType}</td>
            <td>${i.sizePretty || '—'}</td>
        </tr>`;
    }

    function rowDup(i) {
        return `<tr>
            <td class="mono">${i.indexName}</td>
            <td>${i.schema}</td>
            <td>${i.table}</td>
            <td style="font-size:11px">${(i.columns || []).join(', ') || '—'}</td>
            <td>${i.sizePretty || '—'}</td>
        </tr>`;
    }

    const TABS = {
        all:       { ep: '/indexes?size=200', bodyId: 'idx-all-body',       fn: rowIndex,   cols: 7 },
        health:    { ep: '/indexes/health',   bodyId: 'idx-health-body',    fn: rowHealth,  cols: 7 },
        unused:    { ep: '/indexes/unused',   bodyId: 'idx-unused-body',    fn: rowSimple,  cols: 6 },
        invalid:   { ep: '/indexes/invalid',  bodyId: 'idx-invalid-body',   fn: rowInvalid, cols: 5 },
        duplicate: { ep: '/indexes/duplicate',bodyId: 'idx-duplicate-body', fn: rowDup,     cols: 5 },
        largest:   { ep: '/indexes/largest?limit=50', bodyId: 'idx-largest-body', fn: rowSimple, cols: 6 },
    };

    let loaded = new Set();

    async function loadTab(tab) {
        if (loaded.has(tab)) return;
        loaded.add(tab);
        const { ep, bodyId, fn, cols } = TABS[tab];
        document.getElementById(bodyId).innerHTML = Nova.skeletonRows(6, cols);
        try {
            const data = await Nova.apiFetch(ep);
            const rows = Array.isArray(data) ? data : (data.content ?? []);
            if (!rows.length) {
                document.getElementById(bodyId).innerHTML =
                    `<tr><td colspan="${cols}"><div class="empty-state"><div class="empty-icon">◻</div><div class="empty-label">No indexes</div></div></td></tr>`;
                return;
            }
            document.getElementById(bodyId).innerHTML = rows.map(fn).join('');
        } catch (err) {
            document.getElementById(bodyId).innerHTML =
                `<tr><td colspan="${cols}" class="text-muted" style="padding:20px;text-align:center">${err.message}</td></tr>`;
        }
    }

    function init() {
        const container = document.getElementById('idx-tabs');
        const tabs = container.querySelectorAll('.tab');
        const panels = container.querySelectorAll('.tab-panel');

        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                tabs.forEach(t => t.classList.remove('active'));
                panels.forEach(p => p.classList.remove('active'));
                tab.classList.add('active');
                container.querySelector(`.tab-panel[data-tab="${tab.dataset.tab}"]`)?.classList.add('active');
                if (Nova.isConnected()) loadTab(tab.dataset.tab);
            });
        });

        if (tabs.length) tabs[0].click();
    }

    function load() {
        if (!Nova.isConnected()) return;
        const active = document.querySelector('#idx-tabs .tab.active');
        if (active) loadTab(active.dataset.tab);
    }

    document.addEventListener('DOMContentLoaded', init);
    document.addEventListener('nova:connected', load);
})();
