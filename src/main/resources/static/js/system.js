const SystemPage = (() => {

    function row(label, value) {
        return `<tr><td style="color:var(--text-muted);width:45%">${label}</td><td>${value ?? '—'}</td></tr>`;
    }

    async function load() {
        if (!Nova.isConnected()) return;

        try {
            const [health, info, version] = await Promise.all([
                Nova.apiFetch('/activity/health'),
                Nova.apiFetch('/activity/database'),
                Nova.apiFetch('/activity/version')
            ]);

            // Stat cards
            const statusEl = document.getElementById('sys-status');
            if (statusEl) {
                statusEl.textContent = health.status ?? '—';
                statusEl.style.color = health.status === 'UP' ? 'var(--success, #34d399)' : 'var(--danger, #f87171)';
            }
            document.getElementById('sys-uptime').textContent = info.uptime ?? '—';
            document.getElementById('sys-connections').textContent =
                `${info.activeConnections ?? '?'} / ${info.maxConnections ?? '?'}`;
            document.getElementById('sys-timezone').textContent = info.timezone ?? '—';

            // DB info table
            const dbRows = [
                row('Database', info.databaseName),
                row('Current user', info.currentUser),
                row('Current schema', info.currentSchema),
                row('Server started', info.serverStartTime),
                row('Max connections', info.maxConnections),
                row('Active connections', info.activeConnections),
            ];
            document.getElementById('sys-db-info-body').innerHTML = dbRows.join('');

            // Connection pool
            const pool = health.connectionPool ?? {};
            const poolKeys = Object.keys(pool);
            if (poolKeys.length) {
                document.getElementById('sys-pool-body').innerHTML =
                    poolKeys.map(k => row(k, pool[k])).join('');
            } else {
                document.getElementById('sys-pool-body').innerHTML =
                    '<tr><td colspan="2" class="text-muted" style="padding:20px;text-align:center">No pool data</td></tr>';
            }

            // Version
            document.getElementById('sys-version-text').textContent =
                version.version ?? info.postgresVersion ?? '—';

        } catch (err) {
            Nova.toast('Failed to load system info: ' + err.message, 'error');
        }
    }

    function refresh() {
        load();
    }

    document.addEventListener('DOMContentLoaded', load);
    document.addEventListener('nova:connected', load);

    return {refresh};
})();
