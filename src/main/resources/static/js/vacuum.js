const VacuumPage = (() => {

    let _progressTimer = null;
    let _currentSchema = '';

    // ── Stat card helpers ────────────────────────────────────────────────────
    function setText(id, val) {
        const el = document.getElementById(id);
        if (el) el.textContent = val ?? '—';
    }

    // ── Bloat ratio ──────────────────────────────────────────────────────────
    function bloatPct(live, dead) {
        const total = (live || 0) + (dead || 0);
        if (!total) return 0;
        return ((dead / total) * 100);
    }

    function bloatBadge(live, dead) {
        const pct = bloatPct(live, dead);
        if (pct > 30) return `<span class="badge badge-error">${pct.toFixed(1)}%</span>`;
        if (pct > 10) return `<span class="badge badge-warn">${pct.toFixed(1)}%</span>`;
        return `<span class="badge badge-ok">${pct.toFixed(1)}%</span>`;
    }

    // ── Progress bar for heap scan ───────────────────────────────────────────
    function heapPct(scanned, total) {
        if (!total) return '—';
        const pct = Math.min(100, Math.round((scanned / total) * 100));
        return `<div style="display:flex;align-items:center;gap:8px">
            <div class="progress-track" style="width:60px">
                <div class="progress-fill" style="width:${pct}%"></div>
            </div>
            <span>${pct}%</span>
        </div>`;
    }

    // ── Load active vacuum progress ──────────────────────────────────────────
    async function loadProgress() {
        const body = document.getElementById('vac-progress-body');
        if (!body) return;
        try {
            const data = await Nova.apiFetch('/vacuum/status');
            setText('vac-active-count', data.length);

            if (!data.length) {
                body.innerHTML = `<tr><td colspan="7"><div class="empty-state"><div class="empty-icon">✓</div><div class="empty-label">No vacuum jobs running</div></div></td></tr>`;
                stopProgressTimer();
                return;
            }

            body.innerHTML = data.map(v => `<tr>
                <td class="mono">${v.pid}</td>
                <td>${v.datname || '—'}</td>
                <td class="mono">${v.relname || '—'}</td>
                <td><span class="badge badge-info">${v.phase || '—'}</span></td>
                <td>${heapPct(v.heapBlksScanned, v.heapBlksTotal)}</td>
                <td>${heapPct(v.heapBlksVacuumed, v.heapBlksTotal)}</td>
                <td>${Nova.fmtNum(v.numDeadTuples)} / ${Nova.fmtNum(v.maxDeadTuples)}</td>
            </tr>`).join('');

            // auto-refresh while jobs are running
            startProgressTimer();
        } catch (err) {
            body.innerHTML = `<tr><td colspan="7" class="text-muted" style="padding:20px;text-align:center">Error: ${err.message}</td></tr>`;
        }
    }

    function startProgressTimer() {
        if (_progressTimer) return;
        _progressTimer = setInterval(loadProgress, 5000);
    }

    function stopProgressTimer() {
        if (_progressTimer) {
            clearInterval(_progressTimer);
            _progressTimer = null;
        }
    }

    // ── Load table vacuum info ────────────────────────────────────────────────
    async function loadTables() {
        const body = document.getElementById('vac-tables-body');
        if (body) body.innerHTML = Nova.skeletonRows(9, 6);

        const qs = _currentSchema ? `?schema=${encodeURIComponent(_currentSchema)}` : '';
        try {
            const data = await Nova.apiFetch(`/vacuum/tables${qs}`);

            // Update stat cards
            setText('vac-total-count', data.length);
            setText('vac-need-count', data.filter(t => t.deadTuples > 10000).length);
            setText('vac-never-count', data.filter(t => !t.lastVacuum && !t.lastAutovacuum).length);

            if (!body) return;
            if (!data.length) {
                body.innerHTML = `<tr><td colspan="9"><div class="empty-state"><div class="empty-icon">◻</div><div class="empty-label">No tables found</div></div></td></tr>`;
                return;
            }

            // sort by dead tuples descending
            data.sort((a, b) => (b.deadTuples || 0) - (a.deadTuples || 0));

            body.innerHTML = data.map(t => {
                const deadStyle = t.deadTuples > 10000
                    ? 'color:var(--error);font-weight:600'
                    : t.deadTuples > 1000 ? 'color:var(--warn)' : '';
                return `<tr>
                    <td class="mono" style="font-size:11px">${t.schema}</td>
                    <td class="mono">${t.table}</td>
                    <td>${Nova.fmtNum(t.liveTuples)}</td>
                    <td style="${deadStyle}">${Nova.fmtNum(t.deadTuples)}</td>
                    <td>${bloatBadge(t.liveTuples, t.deadTuples)}</td>
                    <td style="font-size:11px;color:var(--text-muted)">${t.lastVacuum ?? '—'}</td>
                    <td style="font-size:11px;color:var(--text-muted)">${t.lastAutovacuum ?? '—'}</td>
                    <td style="font-size:11px;color:var(--text-muted)">${t.lastAnalyze ?? t.lastAutoanalyze ?? '—'}</td>
                    <td>
                        <div style="display:flex;gap:6px">
                            <button class="btn btn-ghost btn-sm"
                                onclick="VacuumPage.runVacuum('${t.schema}','${t.table}',this)">VACUUM</button>
                            <button class="btn btn-ghost btn-sm"
                                onclick="VacuumPage.runAnalyze('${t.schema}','${t.table}',this)">ANALYZE</button>
                        </div>
                    </td>
                </tr>`;
            }).join('');
        } catch (err) {
            if (body) body.innerHTML = `<tr><td colspan="9" class="text-muted" style="padding:20px;text-align:center">Error: ${err.message}</td></tr>`;
            Nova.toast('Failed to load vacuum data: ' + err.message, 'error');
        }
    }

    // ── Run VACUUM ────────────────────────────────────────────────────────────
    async function runVacuum(schema, table, btn) {
        btn.disabled = true;
        btn.textContent = '…';
        try {
            await Nova.apiFetch('/vacuum/vacuum', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({schema, table})
            });
            Nova.toast(`VACUUM ${schema}.${table} completed`, 'success');
            loadTables();
            loadProgress();
        } catch (err) {
            Nova.toast(`VACUUM failed: ${err.message}`, 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'VACUUM';
        }
    }

    // ── Run ANALYZE ───────────────────────────────────────────────────────────
    async function runAnalyze(schema, table, btn) {
        btn.disabled = true;
        btn.textContent = '…';
        try {
            await Nova.apiFetch('/vacuum/analyze', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({schema, table})
            });
            Nova.toast(`ANALYZE ${schema}.${table} completed`, 'success');
            loadTables();
        } catch (err) {
            Nova.toast(`ANALYZE failed: ${err.message}`, 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'ANALYZE';
        }
    }

    // ── Schema filter ─────────────────────────────────────────────────────────
    async function populateSchemaFilter() {
        try {
            const schemas = await Nova.apiFetch('/schemas');
            const sel = document.getElementById('vacuum-schema-filter');
            if (!sel) return;
            (Array.isArray(schemas) ? schemas : (schemas.content ?? [])).forEach(s => {
                const opt = document.createElement('option');
                opt.value = s.name || s;
                opt.textContent = s.name || s;
                sel.appendChild(opt);
            });
        } catch { /* non-critical */ }
    }

    function refresh() {
        loadProgress();
        loadTables();
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    document.addEventListener('DOMContentLoaded', () => {
        document.getElementById('vacuum-schema-filter')?.addEventListener('change', e => {
            _currentSchema = e.target.value;
            loadTables();
        });

        if (Nova.isConnected()) {
            populateSchemaFilter();
            refresh();
        }
    });

    document.addEventListener('nova:connected', () => {
        populateSchemaFilter();
        refresh();
    });

    // Cleanup timer when navigating away
    window.addEventListener('beforeunload', stopProgressTimer);

    return { refresh, runVacuum, runAnalyze };
})();
