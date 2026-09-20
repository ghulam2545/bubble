const ActivityPage = (() => {

    /* cache keyed by pid for the detail modal */
    let _cache = {};

    function truncate(s, n = 70) {
        if (!s) return '—';
        return s.length > n ? s.slice(0, n) + '…' : s;
    }

    function stateBadge(state) {
        const colors = {
            active: '#34d399',
            idle: '#60a5fa',
            'idle in transaction': '#f59e0b',
            'idle in transaction (aborted)': '#f87171',
        };
        const c = colors[state] || '#94a3b8';
        return `<span style="display:inline-block;padding:2px 8px;border-radius:99px;font-size:11px;
                background:${c}22;color:${c};font-weight:600">${state ?? '—'}</span>`;
    }

    /* Standard row (all / active / idle tabs) */
    function stdRow(s) {
        return `<tr style="cursor:pointer" onclick="ActivityPage.showDetail(${s.pid})">
            <td>${s.pid}</td>
            <td>${s.usename ?? '—'}</td>
            <td>${s.applicationName ?? '—'}</td>
            <td>${s.clientAddr ?? 'local'}</td>
            <td>${stateBadge(s.state)}</td>
            <td>${s.waitEvent ? `${s.waitEventType}/${s.waitEvent}` : '—'}</td>
            <td>${s.durationSeconds != null ? Nova.fmtDur(s.durationSeconds) : '—'}</td>
            <td style="max-width:300px"><span title="${s.query ?? ''}">${truncate(s.query)}</span></td>
        </tr>`;
    }

    /* Waiting row (shows wait type + event instead of duration) */
    function waitRow(s) {
        return `<tr style="cursor:pointer" onclick="ActivityPage.showDetail(${s.pid})">
            <td>${s.pid}</td>
            <td>${s.usename ?? '—'}</td>
            <td>${s.applicationName ?? '—'}</td>
            <td>${s.clientAddr ?? 'local'}</td>
            <td>${stateBadge(s.state)}</td>
            <td>${s.waitEventType ?? '—'}</td>
            <td>${s.waitEvent ?? '—'}</td>
            <td style="max-width:260px"><span title="${s.query ?? ''}">${truncate(s.query)}</span></td>
        </tr>`;
    }

    /* Long-running row (7 cols) */
    function longRow(s) {
        return `<tr style="cursor:pointer" onclick="ActivityPage.showDetail(${s.pid})">
            <td>${s.pid}</td>
            <td>${s.usename ?? '—'}</td>
            <td>${s.applicationName ?? '—'}</td>
            <td>${s.clientAddr ?? 'local'}</td>
            <td>${stateBadge(s.state)}</td>
            <td>${s.durationSeconds != null ? Nova.fmtDur(s.durationSeconds) : '—'}</td>
            <td style="max-width:300px"><span title="${s.query ?? ''}">${truncate(s.query)}</span></td>
        </tr>`;
    }

    function renderBody(id, sessions, rowFn, cols = 8) {
        const body = document.getElementById(id);
        if (!sessions.length) {
            body.innerHTML = `<tr><td colspan="${cols}" class="text-muted" style="padding:20px;text-align:center">No sessions</td></tr>`;
            return;
        }
        sessions.forEach(s => {
            _cache[s.pid] = s;
        });
        body.innerHTML = sessions.map(rowFn).join('');
    }

    async function load() {
        if (!Nova.isConnected()) return;

        try {
            const [all, active, idle, waiting, longRunning] = await Promise.all([
                Nova.apiFetch('/activity?limit=100'),
                Nova.apiFetch('/activity/active'),
                Nova.apiFetch('/activity/idle'),
                Nova.apiFetch('/activity/waiting'),
                Nova.apiFetch('/activity/long-running'),
            ]);

            /* stat cards */
            document.getElementById('act-active-count').textContent = active.length;
            document.getElementById('act-idle-count').textContent = idle.length;
            document.getElementById('act-waiting-count').textContent = waiting.length;
            document.getElementById('act-long-count').textContent = longRunning.length;

            /* tables */
            renderBody('act-all-body', all, stdRow);
            renderBody('act-active-body', active, stdRow);
            renderBody('act-idle-body', idle, stdRow);
            renderBody('act-waiting-body', waiting, waitRow);
            renderBody('act-long-body', longRunning, longRow, 7);

        } catch (err) {
            Nova.toast('Failed to load activity: ' + err.message, 'error');
        }
    }

    function showDetail(pid) {
        const s = _cache[pid];
        if (!s) return;

        document.getElementById('act-detail-query').textContent = s.query ?? '—';

        function cell(label, value) {
            return `<div style="background:var(--bg-elevated);border-radius:6px;padding:10px 12px">
                <div style="font-size:10px;font-weight:600;color:var(--text-muted);text-transform:uppercase;
                            letter-spacing:.05em;margin-bottom:4px">${label}</div>
                <div style="font-size:13px;word-break:break-all">${value ?? '—'}</div>
            </div>`;
        }

        document.getElementById('act-detail-meta').innerHTML = [
            cell('PID', s.pid),
            cell('User', s.usename),
            cell('Application', s.applicationName),
            cell('Client', s.clientAddr ? `${s.clientAddr}${s.clientPort ? ':' + s.clientPort : ''}` : 'local'),
            cell('State', s.state),
            cell('Duration', s.durationSeconds != null ? Nova.fmtDur(s.durationSeconds) : null),
            cell('Wait Type', s.waitEventType),
            cell('Wait Event', s.waitEvent),
            cell('Backend Start', s.backendStart),
            cell('Xact Start', s.xactStart),
            cell('Query Start', s.queryStart),
            cell('Backend XID', s.backendXid),
            cell('Backend Xmin', s.backendXmin),
        ].join('');

        Nova.openModal('activity-detail-modal');
    }

    function refresh() {
        load();
    }

    document.addEventListener('DOMContentLoaded', () => {
        Nova.initTabs(document.getElementById('activity-tabs'));
        load();
    });
    document.addEventListener('nova:connected', load);

    return {refresh, showDetail};
})();
