const StatsPage = (() => {

    let _allRows = [];

    function kv(label, value) {
        return `<tr><td style="color:var(--text-muted);width:45%">${label}</td><td>${value ?? '—'}</td></tr>`;
    }

    function pct(v) {
        return v == null ? '—' : v.toFixed(2) + '%';
    }

    async function load() {
        if (!Nova.isConnected()) return;

        try {
            const [tables, db] = await Promise.all([
                Nova.apiFetch('/statistics/tables?limit=100'),
                Nova.apiFetch('/statistics/database')
            ]);

            // Stat cards
            document.getElementById('stat-cache-hit').textContent = pct(db.cacheHitRatio);
            document.getElementById('stat-commits').textContent = Nova.fmtNum(db.xactCommit);
            document.getElementById('stat-rollbacks').textContent = Nova.fmtNum(db.xactRollback);
            document.getElementById('stat-deadlocks').textContent = Nova.fmtNum(db.deadlocks);

            // Table rows
            _allRows = tables;
            renderTableRows(tables);

            // DB stats table
            const dbRows = [
                kv('Database', db.databaseName),
                kv('Active backends', Nova.fmtNum(db.numbackends)),
                kv('Cache hit ratio', pct(db.cacheHitRatio)),
                kv('Commits', Nova.fmtNum(db.xactCommit)),
                kv('Rollbacks', Nova.fmtNum(db.xactRollback)),
                kv('Tuples returned', Nova.fmtNum(db.tupReturned)),
                kv('Tuples fetched', Nova.fmtNum(db.tupFetched)),
                kv('Tuples inserted', Nova.fmtNum(db.tupInserted)),
                kv('Tuples updated', Nova.fmtNum(db.tupUpdated)),
                kv('Tuples deleted', Nova.fmtNum(db.tupDeleted)),
                kv('Deadlocks', Nova.fmtNum(db.deadlocks)),
                kv('Temp files', Nova.fmtNum(db.tempFiles)),
                kv('Temp bytes', Nova.fmtBytes(db.tempBytes)),
                kv('Conflicts', Nova.fmtNum(db.conflicts)),
                kv('Stats reset', db.statsReset ?? '—'),
            ];
            document.getElementById('stats-db-body').innerHTML = dbRows.join('');

        } catch (err) {
            Nova.toast('Failed to load statistics: ' + err.message, 'error');
        }
    }

    function renderTableRows(rows) {
        const body = document.getElementById('stats-table-body');
        if (!rows.length) {
            body.innerHTML = '<tr><td colspan="10" class="text-muted" style="padding:20px;text-align:center">No data</td></tr>';
            return;
        }
        body.innerHTML = rows.map(r => `
            <tr>
                <td>${r.schema}</td>
                <td>${r.table}</td>
                <td>${Nova.fmtNum(r.seqScan)}</td>
                <td>${Nova.fmtNum(r.idxScan)}</td>
                <td>${Nova.fmtNum(r.nLiveTup)}</td>
                <td>${Nova.fmtNum(r.nDeadTup)}</td>
                <td>${Nova.fmtNum(r.nTupIns)}</td>
                <td>${Nova.fmtNum(r.nTupUpd)}</td>
                <td>${Nova.fmtNum(r.nTupDel)}</td>
                <td>${r.lastAutovacuum ?? r.lastVacuum ?? '—'}</td>
            </tr>`).join('');
    }

    function initFilter() {
        const input = document.getElementById('stats-filter');
        if (!input) return;
        input.addEventListener('input', () => {
            const q = input.value.toLowerCase();
            renderTableRows(q ? _allRows.filter(r =>
                r.table.toLowerCase().includes(q) || r.schema.toLowerCase().includes(q)
            ) : _allRows);
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        Nova.initTabs(document.getElementById('stats-tabs'));
        initFilter();
        load();
    });
    document.addEventListener('nova:connected', load);

    return {};
})();
