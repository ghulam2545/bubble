document.addEventListener('DOMContentLoaded', async () => {

    // ── stat placeholders ──────────────────────────────────────────────────
    const els = {
        dbName: document.getElementById('stat-db-name'),
        pgVersion: document.getElementById('stat-pg-version'),
        tableCount: document.getElementById('stat-tables'),
        totalSize: document.getElementById('stat-size'),
        activeConns: document.getElementById('stat-connections'),
        cacheHit: document.getElementById('stat-cache-hit'),
        deadlocks: document.getElementById('stat-deadlocks'),
        tps: document.getElementById('stat-tps'),
        connStatus: document.getElementById('db-status-badge'),
        connHost: document.getElementById('db-info-host'),
        connPort: document.getElementById('db-info-port'),
        connDb: document.getElementById('db-info-db'),
        activityBody: document.getElementById('activity-table-body'),
    };

    async function loadDashboard() {
        try {
            const [stats, storage, activity] = await Promise.all([
                Nova.apiFetch('/statistics/database'),
                Nova.apiFetch('/storage/database'),
                Nova.apiFetch('/activity/active')
            ]);

            // stat cards
            if (els.dbName) els.dbName.textContent = stats.databaseName || '—';
            if (els.tableCount) els.tableCount.textContent = '—';   // no direct endpoint; shown in schemas
            if (els.totalSize) els.totalSize.textContent = storage.totalSize || '—';
            if (els.activeConns) els.activeConns.textContent = stats.numbackends ?? '—';
            if (els.cacheHit) {
                const ratio = stats.cacheHitRatio != null ? (stats.cacheHitRatio * 100).toFixed(1) + '%' : '—';
                els.cacheHit.textContent = ratio;
                if (stats.cacheHitRatio < 0.9) els.cacheHit.classList.add('stat-warn');
            }
            if (els.deadlocks) els.deadlocks.textContent = Nova.fmtNum(stats.deadlocks);
            if (els.tps) els.tps.textContent = Nova.fmtNum(stats.xactCommit);

            // db info panel
            const conn = Nova.getConn();
            if (els.connStatus) els.connStatus.innerHTML = '<span class="badge badge-ok">● Healthy</span>';
            if (els.connHost) els.connHost.textContent = conn?.host || 'localhost';
            if (els.connPort) els.connPort.textContent = conn?.port || '5432';
            if (els.connDb) els.connDb.textContent = stats.databaseName || conn?.database || '—';

            // activity table
            renderActivity(activity || []);

        } catch (err) {
            Nova.toast('Failed to load dashboard: ' + err.message, 'error');
            if (els.activityBody) els.activityBody.innerHTML = `<tr><td colspan="5" class="text-muted" style="padding:20px;text-align:center">Not connected — click Connect to get started</td></tr>`;
        }
    }

    function renderActivity(sessions) {
        if (!els.activityBody) return;
        if (!sessions.length) {
            els.activityBody.innerHTML = `<tr><td colspan="5"><div class="empty-state"><div class="empty-icon">○</div><div class="empty-label">No active sessions</div></div></td></tr>`;
            return;
        }
        els.activityBody.innerHTML = sessions.slice(0, 8).map(s => {
            const stateClass = s.state === 'active' ? 'badge-ok' : s.state === 'idle' ? 'badge-muted' : 'badge-warn';
            return `<tr>
                <td class="mono">${s.pid}</td>
                <td>${s.username || '—'}</td>
                <td><span class="badge ${stateClass}">${s.state || '—'}</span></td>
                <td class="mono" style="max-width:260px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap" title="${(s.query || '').replace(/"/g, '&quot;')}">${s.query ? s.query.substring(0, 60) + (s.query.length > 60 ? '…' : '') : '—'}</td>
                <td>${Nova.fmtDur(s.durationSeconds)}</td>
            </tr>`;
        }).join('');
    }

    // ── Bar chart (query activity proxy using storage/schemas) ─────────────
    async function loadChart() {
        try {
            const schemas = await Nova.apiFetch('/storage/schemas');
            renderBarChart(schemas);
        } catch { /* ignore */
        }
    }

    function renderBarChart(schemas) {
        const canvas = document.getElementById('activity-chart');
        if (!canvas) return;
        const bars = canvas.querySelectorAll('.bar-col');
        // just use schema sizes for a visual bar chart placeholder
        const vals = schemas.map(s => s.totalSizeBytes || 0);
        const max = Math.max(...vals, 1);
        bars.forEach((bar, i) => {
            const pct = vals[i] != null ? Math.max(8, (vals[i] / max) * 100) : 8;
            bar.style.height = pct + '%';
            if (schemas[i]) bar.title = `${schemas[i].schema}: ${schemas[i].totalSizePretty}`;
        });
    }

    // ── Recent queries (from query metrics if available) ───────────────────
    async function loadLargestRelations() {
        try {
            const rel = await Nova.apiFetch('/storage/largest?limit=5');
            const body = document.getElementById('largest-table-body');
            if (!body) return;
            if (!rel.length) {
                body.innerHTML = `<tr><td colspan="4" class="text-muted" style="padding:16px;text-align:center">No data</td></tr>`;
                return;
            }
            body.innerHTML = rel.map(r => `<tr>
                <td class="mono">${r.schema}.${r.relationName}</td>
                <td><span class="badge badge-muted">${r.relationType}</span></td>
                <td>${r.totalSizePretty}</td>
                <td>
                    <div class="progress-track" style="width:100px">
                        <div class="progress-fill" style="width:${r.totalSizeBytes ? Math.min(100, (r.totalSizeBytes / 1e8) | 0) : 10}%"></div>
                    </div>
                </td>
            </tr>`).join('');
        } catch { /* ignore */
        }
    }

    loadDashboard();
    loadChart();
    loadLargestRelations();
});

// Reload all dashboard data when user connects mid-session
document.addEventListener('nova:connected', () => {
    Promise.all([
        fetch('/backend/api/v1/statistics/database').then(r => r.ok ? r.json() : null).catch(() => null),
        fetch('/backend/api/v1/storage/database').then(r => r.ok ? r.json() : null).catch(() => null),
        fetch('/backend/api/v1/activity/active').then(r => r.ok ? r.json() : null).catch(() => null),
    ]).then(([stats, storage, activity]) => {
        // stat cards
        const dbName = document.getElementById('stat-db-name');
        const totalSize = document.getElementById('stat-size');
        const activeConns = document.getElementById('stat-connections');
        const cacheHit = document.getElementById('stat-cache-hit');
        const deadlocks = document.getElementById('stat-deadlocks');
        const tps = document.getElementById('stat-tps');

        if (stats) {
            if (dbName) dbName.textContent = stats.databaseName || '—';
            if (activeConns) activeConns.textContent = stats.numbackends ?? '—';
            if (cacheHit) cacheHit.textContent = stats.cacheHitRatio != null ? (stats.cacheHitRatio * 100).toFixed(1) + '%' : '—';
            if (deadlocks) deadlocks.textContent = stats.deadlocks != null ? Number(stats.deadlocks).toLocaleString() : '—';
            if (tps) tps.textContent = stats.xactCommit != null ? Number(stats.xactCommit).toLocaleString() : '—';
        }
        if (storage && totalSize) totalSize.textContent = storage.totalSize || '—';
    }).catch(() => {});
});
