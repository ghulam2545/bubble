document.addEventListener('DOMContentLoaded', async () => {

    // ── Stat card refs ─────────────────────────────────────────────────────
    const totalSizeEl = document.getElementById('storage-total-size');
    const tablesSizeEl = document.getElementById('storage-tables-size');
    const indexSizeEl = document.getElementById('storage-index-size');

    const schemaBody = document.getElementById('schema-storage-body');
    const tableBody = document.getElementById('table-storage-body');
    const indexBody = document.getElementById('index-storage-body');
    const largestBody = document.getElementById('largest-storage-body');

    const schemaFilter = document.getElementById('storage-schema-filter');
    const tableLimit = document.getElementById('storage-table-limit');

    let currentSchema = '';

    // ── Load database summary ──────────────────────────────────────────────
    async function loadDbSummary() {
        try {
            const data = await Nova.apiFetch('/storage/database');
            if (totalSizeEl) totalSizeEl.textContent = data.totalSize || '—';
            if (tablesSizeEl) tablesSizeEl.textContent = data.tablesSize || '—';
            if (indexSizeEl) indexSizeEl.textContent = data.indexesSize || '—';
        } catch (err) {
            Nova.toast('Storage summary failed: ' + err.message, 'error');
        }
    }

    // ── Schema storage ─────────────────────────────────────────────────────
    async function loadSchemaStorage() {
        if (schemaBody) schemaBody.innerHTML = Nova.skeletonRows(4, 4);
        try {
            const data = await Nova.apiFetch('/storage/schemas');
            if (!schemaBody) return;
            if (!data.length) {
                schemaBody.innerHTML = emptyRow(4);
                return;
            }

            const maxBytes = Math.max(...data.map(s => s.totalSizeBytes || 0), 1);
            schemaBody.innerHTML = data.map(s => `<tr>
                <td class="mono">${s.schema}</td>
                <td>${s.tablesCount ?? '—'}</td>
                <td>${s.indexesCount ?? '—'}</td>
                <td>
                    <div style="display:flex;align-items:center;gap:10px">
                        <div class="progress-track" style="width:80px;flex-shrink:0">
                            <div class="progress-fill" style="width:${Math.max(4, (s.totalSizeBytes / maxBytes * 100)) | 0}%"></div>
                        </div>
                        <span>${s.totalSizePretty || '—'}</span>
                    </div>
                </td>
            </tr>`).join('');
        } catch (err) {
            if (schemaBody) schemaBody.innerHTML = errRow(4, err.message);
        }
    }

    // ── Table storage ──────────────────────────────────────────────────────
    async function loadTableStorage() {
        if (tableBody) tableBody.innerHTML = Nova.skeletonRows(5, 5);
        const schema = currentSchema || '';
        const limit = tableLimit?.value || 50;
        try {
            const qs = new URLSearchParams();
            if (schema) qs.set('schema', schema);
            qs.set('limit', limit);
            const data = await Nova.apiFetch('/storage/tables?' + qs);
            if (!tableBody) return;
            if (!data.length) {
                tableBody.innerHTML = emptyRow(5);
                return;
            }
            const maxBytes = Math.max(...data.map(r => r.totalSizeBytes || 0), 1);
            tableBody.innerHTML = data.map(r => `<tr>
                <td class="mono">${r.schema}</td>
                <td class="mono">${r.relationName}</td>
                <td>${r.tableSizePretty || '—'}</td>
                <td>${r.indexSizePretty || '—'}</td>
                <td>
                    <div style="display:flex;align-items:center;gap:10px">
                        <div class="progress-track" style="width:70px;flex-shrink:0">
                            <div class="progress-fill" style="width:${Math.max(4, (r.totalSizeBytes / maxBytes * 100)) | 0}%"></div>
                        </div>
                        <span>${r.totalSizePretty || '—'}</span>
                    </div>
                </td>
            </tr>`).join('');
        } catch (err) {
            if (tableBody) tableBody.innerHTML = errRow(5, err.message);
        }
    }

    // ── Index storage ──────────────────────────────────────────────────────
    async function loadIndexStorage() {
        if (indexBody) indexBody.innerHTML = Nova.skeletonRows(4, 4);
        const schema = currentSchema || '';
        const limit = tableLimit?.value || 50;
        try {
            const qs = new URLSearchParams();
            if (schema) qs.set('schema', schema);
            qs.set('limit', limit);
            const data = await Nova.apiFetch('/storage/indexes?' + qs);
            if (!indexBody) return;
            if (!data.length) {
                indexBody.innerHTML = emptyRow(4);
                return;
            }
            const maxBytes = Math.max(...data.map(r => r.totalSizeBytes || 0), 1);
            indexBody.innerHTML = data.map(r => `<tr>
                <td class="mono">${r.schema}</td>
                <td class="mono">${r.relationName}</td>
                <td><span class="badge badge-info">${r.relationType || 'INDEX'}</span></td>
                <td>
                    <div style="display:flex;align-items:center;gap:10px">
                        <div class="progress-track" style="width:70px;flex-shrink:0">
                            <div class="progress-fill" style="width:${Math.max(4, (r.totalSizeBytes / maxBytes * 100)) | 0}%;background:var(--info)"></div>
                        </div>
                        <span>${r.totalSizePretty || '—'}</span>
                    </div>
                </td>
            </tr>`).join('');
        } catch (err) {
            if (indexBody) indexBody.innerHTML = errRow(4, err.message);
        }
    }

    // ── Largest relations ──────────────────────────────────────────────────
    async function loadLargest() {
        if (largestBody) largestBody.innerHTML = Nova.skeletonRows(4, 6);
        try {
            const data = await Nova.apiFetch('/storage/largest?limit=20');
            if (!largestBody) return;
            if (!data.length) {
                largestBody.innerHTML = emptyRow(4);
                return;
            }
            const maxBytes = Math.max(...data.map(r => r.totalSizeBytes || 0), 1);
            largestBody.innerHTML = data.map((r, i) => `<tr>
                <td class="text-muted" style="font-size:11px">${i + 1}</td>
                <td class="mono">${r.schema}.${r.relationName}</td>
                <td><span class="badge ${r.relationType === 'TABLE' ? 'badge-ok' : r.relationType === 'INDEX' ? 'badge-info' : 'badge-muted'}">${r.relationType}</span></td>
                <td>
                    <div style="display:flex;align-items:center;gap:10px">
                        <div class="progress-track" style="width:100px;flex-shrink:0">
                            <div class="progress-fill" style="width:${Math.max(4, (r.totalSizeBytes / maxBytes * 100)) | 0}%"></div>
                        </div>
                        <span>${r.totalSizePretty || '—'}</span>
                    </div>
                </td>
            </tr>`).join('');
        } catch (err) {
            if (largestBody) largestBody.innerHTML = errRow(4, err.message);
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    function emptyRow(cols) {
        return `<tr><td colspan="${cols}"><div class="empty-state"><div class="empty-icon">◻</div><div class="empty-label">No data</div></div></td></tr>`;
    }

    function errRow(cols, msg) {
        return `<tr><td colspan="${cols}" class="text-error" style="padding:16px;text-align:center">Error: ${msg}</td></tr>`;
    }

    // ── Events ─────────────────────────────────────────────────────────────
    schemaFilter?.addEventListener('change', () => {
        currentSchema = schemaFilter.value;
        loadTableStorage();
        loadIndexStorage();
    });
    tableLimit?.addEventListener('change', () => {
        loadTableStorage();
        loadIndexStorage();
    });

    // init tabs
    const tabContainer = document.getElementById('storage-tabs');
    if (tabContainer) Nova.initTabs(tabContainer);

    // load all sections
    loadDbSummary();
    loadSchemaStorage();
    loadTableStorage();
    loadIndexStorage();
    loadLargest();
});
