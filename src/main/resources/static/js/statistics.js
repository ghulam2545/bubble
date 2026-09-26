const StatsPage = (() => {

    let _allRows = [];

    function kv(label, value) {
        return `<tr><td style="color:var(--text-muted);width:45%">${label}</td><td>${value ?? '—'}</td></tr>`;
    }

    function pct(v) {
        if (v == null) return '—';
        return (v * 100).toFixed(2) + '%';
    }

    async function load() {
        if (!Nova.isConnected()) return;

        try {
            const [tables, db] = await Promise.all([
                Nova.apiFetch('/statistics/tables?limit=200'),
                Nova.apiFetch('/statistics/database')
            ]);

            // ── Stat cards ────────────────────────────────────────────────
            const cacheHitEl = document.getElementById('stat-cache-hit');
            if (cacheHitEl) {
                const ratio = db.cacheHitRatio;
                cacheHitEl.textContent = pct(ratio);
                if (ratio != null && ratio < 0.90) cacheHitEl.style.color = 'var(--error)';
                else if (ratio != null && ratio < 0.95) cacheHitEl.style.color = 'var(--warn)';
                else cacheHitEl.style.color = 'var(--ok)';
            }
            const commitsEl = document.getElementById('stat-commits');
            if (commitsEl) commitsEl.textContent = Nova.fmtNum(db.xactCommit);

            const rollEl = document.getElementById('stat-rollbacks');
            if (rollEl) rollEl.textContent = Nova.fmtNum(db.xactRollback);

            const dlEl = document.getElementById('stat-deadlocks');
            if (dlEl) {
                dlEl.textContent = Nova.fmtNum(db.deadlocks);
                if (db.deadlocks > 0) dlEl.style.color = 'var(--error)';
            }

            // ── Table rows ────────────────────────────────────────────────
            _allRows = Array.isArray(tables) ? tables : (tables.content ?? []);
            renderTableRows(_allRows);

            // ── DB stats key-value table ──────────────────────────────────
            const dbRows = [
                kv('Database', db.databaseName),
                kv('Active backends', Nova.fmtNum(db.numbackends)),
                kv('Cache hit ratio', pct(db.cacheHitRatio)),
                kv('Commits', Nova.fmtNum(db.xactCommit)),
                kv('Rollbacks', Nova.fmtNum(db.xactRollback)),
                kv('Blks read (disk)', Nova.fmtNum(db.blksRead)),
                kv('Blks hit (cache)', Nova.fmtNum(db.blksHit)),
                kv('Tuples returned', Nova.fmtNum(db.tupReturned)),
                kv('Tuples fetched', Nova.fmtNum(db.tupFetched)),
                kv('Tuples inserted', Nova.fmtNum(db.tupInserted)),
                kv('Tuples updated', Nova.fmtNum(db.tupUpdated)),
                kv('Tuples deleted', Nova.fmtNum(db.tupDeleted)),
                kv('Deadlocks', Nova.fmtNum(db.deadlocks)),
                kv('Conflicts', Nova.fmtNum(db.conflicts)),
                kv('Temp files', Nova.fmtNum(db.tempFiles)),
                kv('Temp bytes', Nova.fmtBytes(db.tempBytes)),
                kv('Blk read time (ms)', db.blkReadTime?.toFixed(2) ?? '—'),
                kv('Blk write time (ms)', db.blkWriteTime?.toFixed(2) ?? '—'),
                kv('Stats reset at', db.statsReset ?? '—'),
            ];
            const dbBody = document.getElementById('stats-db-body');
            if (dbBody) dbBody.innerHTML = dbRows.join('');

        } catch (err) {
            Nova.toast('Failed to load statistics: ' + err.message, 'error');
        }
    }

    function renderTableRows(rows) {
        const body = document.getElementById('stats-table-body');
        if (!body) return;
        if (!rows.length) {
            body.innerHTML = '<tr><td colspan="10" class="text-muted" style="padding:20px;text-align:center">No data</td></tr>';
            return;
        }
        body.innerHTML = rows.map(r => {
            // colour dead-tuple ratio
            const bloatPct = r.nLiveTup > 0
                ? ((r.nDeadTup / (r.nLiveTup + r.nDeadTup)) * 100).toFixed(1)
                : 0;
            const deadStyle = bloatPct > 20
                ? 'color:var(--error)'
                : bloatPct > 10 ? 'color:var(--warn)' : '';

            return `<tr>
                <td class="mono" style="font-size:11px">${r.schema}</td>
                <td class="mono">${r.table}</td>
                <td>${Nova.fmtNum(r.seqScan)}</td>
                <td>${Nova.fmtNum(r.idxScan)}</td>
                <td>${Nova.fmtNum(r.nLiveTup)}</td>
                <td style="${deadStyle}">${Nova.fmtNum(r.nDeadTup)}</td>
                <td>${Nova.fmtNum(r.nTupIns)}</td>
                <td>${Nova.fmtNum(r.nTupUpd)}</td>
                <td>${Nova.fmtNum(r.nTupDel)}</td>
                <td style="font-size:11px;color:var(--text-muted)">${r.lastAutovacuum ?? r.lastVacuum ?? '—'}</td>
            </tr>`;
        }).join('');
    }

    function initFilter() {
        const input = document.getElementById('stats-filter');
        if (!input) return;
        input.addEventListener('input', () => {
            const q = input.value.toLowerCase();
            renderTableRows(q
                ? _allRows.filter(r => r.table.toLowerCase().includes(q) || r.schema.toLowerCase().includes(q))
                : _allRows
            );
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        const tabsEl = document.getElementById('stats-tabs');
        if (tabsEl) Nova.initTabs(tabsEl);
        initFilter();
        if (Nova.isConnected()) load();
    });
    document.addEventListener('nova:connected', load);

    return { refresh: load };
})();
