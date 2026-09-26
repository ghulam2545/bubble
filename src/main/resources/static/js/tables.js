(() => {

    let _currentPage   = 0;
    let _currentSchema = '';
    let _currentSearch = '';
    let _allRows       = [];

    const PAGE_SIZE = 50;

    // ── Helpers ───────────────────────────────────────────────────────────────
    function setText(id, val) {
        const el = document.getElementById(id);
        if (el) el.textContent = val ?? '—';
    }

    function kv(label, value) {
        return `<tr><td style="color:var(--text-muted);width:45%">${label}</td><td>${value ?? '—'}</td></tr>`;
    }

    function boolBadge(val) {
        return val
            ? '<span class="badge badge-ok">Yes</span>'
            : '<span class="badge badge-muted">No</span>';
    }

    function constraintBadge(type) {
        const map = {
            'PRIMARY KEY': 'badge-info',
            'FOREIGN KEY': 'badge-warn',
            'UNIQUE':      'badge-ok',
            'CHECK':       'badge-muted',
        };
        return `<span class="badge ${map[type] || 'badge-muted'}">${type}</span>`;
    }

    // ── Load tables list ──────────────────────────────────────────────────────
    async function loadTables(page = 0) {
        _currentPage = page;
        const body = document.getElementById('tables-body');
        if (body) body.innerHTML = Nova.skeletonRows(9, 6);

        const qs = new URLSearchParams({ page, size: PAGE_SIZE });
        if (_currentSchema) qs.set('schema', _currentSchema);

        try {
            const resp = await Nova.apiFetch(`/tables?${qs}`);
            const rows = Array.isArray(resp) ? resp : (resp.content ?? []);
            _allRows = rows;

            // stat cards
            setText('tbl-total', resp.totalElements ?? rows.length);
            setText('tbl-indexed',     rows.filter(r => r.hasIndexes).length);
            setText('tbl-partitioned', rows.filter(r => r.isPartitioned).length);
            setText('tbl-triggers',    rows.filter(r => r.hasTriggers).length);

            renderRows(rows);
            renderPagination(resp);
        } catch (err) {
            if (body) body.innerHTML = `<tr><td colspan="9" class="text-muted" style="padding:20px;text-align:center">Error: ${err.message}</td></tr>`;
            Nova.toast('Failed to load tables: ' + err.message, 'error');
        }
    }

    function renderRows(rows) {
        const body = document.getElementById('tables-body');
        if (!body) return;

        const q = _currentSearch.toLowerCase();
        const filtered = q
            ? rows.filter(r => r.table.toLowerCase().includes(q) || r.schema.toLowerCase().includes(q))
            : rows;

        if (!filtered.length) {
            body.innerHTML = `<tr><td colspan="9"><div class="empty-state"><div class="empty-icon">◻</div><div class="empty-label">No tables found</div></div></td></tr>`;
            return;
        }

        body.innerHTML = filtered.map(t => `<tr class="table-row" data-schema="${t.schema}" data-table="${t.table}" style="cursor:pointer">
            <td class="mono" style="font-size:11px">${t.schema}</td>
            <td class="mono"><strong>${t.table}</strong></td>
            <td>${Nova.fmtNum(t.rowsEstimate)}</td>
            <td>${t.tableSize || '—'}</td>
            <td>${t.indexSize || '—'}</td>
            <td>${t.totalSize || '—'}</td>
            <td>${boolBadge(t.hasIndexes)}</td>
            <td>${boolBadge(t.hasTriggers)}</td>
            <td>${boolBadge(t.isPartitioned)}</td>
        </tr>`).join('');

        body.querySelectorAll('.table-row').forEach(row => {
            row.addEventListener('click', () => openDetail(row.dataset.schema, row.dataset.table));
        });
    }

    function renderPagination(resp) {
        const el = document.getElementById('tables-pagination');
        if (!el) return;
        if (!resp || resp.totalPages <= 1) { el.style.display = 'none'; return; }
        el.style.display = '';
        el.innerHTML = `<div style="display:flex;align-items:center;gap:8px;padding:2px 0">
            <button class="btn btn-ghost btn-sm" ${resp.page === 0 ? 'disabled' : ''} onclick="TablesPage.goto(${resp.page - 1})">‹ Prev</button>
            <span style="font-size:12px;color:var(--text-muted)">Page ${resp.page + 1} of ${resp.totalPages} (${resp.totalElements} total)</span>
            <button class="btn btn-ghost btn-sm" ${resp.page >= resp.totalPages - 1 ? 'disabled' : ''} onclick="TablesPage.goto(${resp.page + 1})">Next ›</button>
        </div>`;
    }

    // ── Table detail modal ────────────────────────────────────────────────────
    async function openDetail(schema, table) {
        document.getElementById('table-detail-title').textContent = `${schema}.${table}`;
        document.getElementById('table-detail-size').textContent = '';

        // reset tab panels to loading state
        document.getElementById('detail-columns-body').innerHTML =
            `<tr><td colspan="6" class="text-muted" style="padding:20px;text-align:center">Loading…</td></tr>`;
        document.getElementById('detail-constraints-body').innerHTML =
            `<tr><td colspan="4" class="text-muted" style="padding:20px;text-align:center">Loading…</td></tr>`;
        document.getElementById('detail-size-body').innerHTML =
            `<tr><td colspan="2" class="text-muted" style="padding:20px;text-align:center">Loading…</td></tr>`;

        Nova.openModal('table-detail-modal');

        // init tabs
        const tabsEl = document.getElementById('table-detail-tabs');
        Nova.initTabs(tabsEl);

        // fetch in parallel
        const [columns, constraints, size] = await Promise.allSettled([
            Nova.apiFetch(`/tables/${schema}/${table}/columns`),
            Nova.apiFetch(`/tables/${schema}/${table}/constraints`),
            Nova.apiFetch(`/tables/${schema}/${table}/size`),
        ]);

        // columns
        const colBody = document.getElementById('detail-columns-body');
        if (columns.status === 'fulfilled') {
            const cols = columns.value;
            if (!cols.length) {
                colBody.innerHTML = `<tr><td colspan="6"><div class="empty-state"><div class="empty-label">No columns</div></div></td></tr>`;
            } else {
                colBody.innerHTML = cols.map(c => `<tr>
                    <td class="text-muted" style="font-size:11px">${c.ordinalPosition}</td>
                    <td class="mono"><strong>${c.columnName}</strong></td>
                    <td class="mono" style="font-size:11px">${c.udtName || c.dataType}</td>
                    <td>${c.nullable ? '<span class="badge badge-muted">NULL</span>' : '<span class="badge badge-ok">NOT NULL</span>'}</td>
                    <td class="mono" style="font-size:11px;color:var(--text-muted)">${c.defaultValue ?? '—'}</td>
                    <td style="font-size:11px;color:var(--text-muted)">${c.comment ?? '—'}</td>
                </tr>`).join('');
            }
        } else {
            colBody.innerHTML = `<tr><td colspan="6" class="text-error" style="padding:16px">Failed to load columns</td></tr>`;
        }

        // constraints
        const conBody = document.getElementById('detail-constraints-body');
        if (constraints.status === 'fulfilled') {
            const cons = constraints.value;
            if (!cons.length) {
                conBody.innerHTML = `<tr><td colspan="4"><div class="empty-state"><div class="empty-label">No constraints</div></div></td></tr>`;
            } else {
                conBody.innerHTML = cons.map(c => `<tr>
                    <td class="mono">${c.constraintName}</td>
                    <td>${constraintBadge(c.constraintType)}</td>
                    <td class="mono" style="font-size:11px">${(c.columns || []).join(', ') || '—'}</td>
                    <td style="font-size:11px;color:var(--text-muted);max-width:260px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap"
                        title="${(c.definition || '').replace(/"/g, '&quot;')}">${c.definition ?? '—'}</td>
                </tr>`).join('');
            }
        } else {
            conBody.innerHTML = `<tr><td colspan="4" class="text-error" style="padding:16px">Failed to load constraints</td></tr>`;
        }

        // size
        const sizeBody = document.getElementById('detail-size-body');
        if (size.status === 'fulfilled') {
            const s = size.value;
            document.getElementById('table-detail-size').textContent = s.totalSize ? `Total: ${s.totalSize}` : '';
            sizeBody.innerHTML = Object.entries(s).map(([k, v]) => kv(k, v)).join('');
        } else {
            sizeBody.innerHTML = `<tr><td colspan="2" class="text-error" style="padding:16px">Failed to load size info</td></tr>`;
        }
    }

    // ── Schema filter + search ─────────────────────────────────────────────────
    async function populateSchemaFilter() {
        try {
            const schemas = await Nova.apiFetch('/schemas');
            const sel = document.getElementById('tables-schema-filter');
            if (!sel) return;
            (Array.isArray(schemas) ? schemas : (schemas.content ?? [])).forEach(s => {
                const opt = document.createElement('option');
                opt.value = s.name || s;
                opt.textContent = s.name || s;
                sel.appendChild(opt);
            });
        } catch { /* non-critical */ }
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    document.addEventListener('DOMContentLoaded', () => {
        document.getElementById('tables-schema-filter')?.addEventListener('change', e => {
            _currentSchema = e.target.value;
            loadTables(0);
        });

        document.getElementById('tables-search')?.addEventListener('input', e => {
            _currentSearch = e.target.value;
            renderRows(_allRows);
        });

        if (Nova.isConnected()) {
            populateSchemaFilter();
            loadTables(0);
        }
    });

    document.addEventListener('nova:connected', () => {
        populateSchemaFilter();
        loadTables(0);
    });

    // expose for pagination buttons
    window.TablesPage = { goto: (p) => loadTables(p) };

})();
