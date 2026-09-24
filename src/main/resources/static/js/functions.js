(() => {

    const COLS = 7;
    let currentPage = 0;
    let currentSchema = '';

    function row(f) {
        return `<tr>
            <td class="mono">${f.name}</td>
            <td>${f.schema || '—'}</td>
            <td>${f.language || '—'}</td>
            <td class="mono" style="font-size:12px">${f.returnType || '—'}</td>
            <td style="color:var(--text-muted);font-size:12px;max-width:260px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap"
                title="${f.arguments || ''}">${f.arguments || '—'}</td>
            <td>${f.type || '—'}</td>
            <td>${f.volatile || '—'}</td>
        </tr>`;
    }

    function renderBody(data) {
        const body = document.getElementById('fn-body');
        if (!data?.length) {
            body.innerHTML = `<tr><td colspan="${COLS}" class="text-muted" style="padding:20px;text-align:center">No functions found</td></tr>`;
            return;
        }
        body.innerHTML = data.map(row).join('');
    }

    function renderPagination(resp) {
        const el = document.getElementById('fn-pagination');
        if (!resp || resp.totalPages <= 1) {
            el.style.display = 'none';
            return;
        }
        el.style.display = '';
        const prev = `<button class="btn btn-ghost btn-sm" ${resp.page === 0 ? 'disabled' : ''} onclick="FnPage.goto(${resp.page - 1})">‹ Prev</button>`;
        const next = `<button class="btn btn-ghost btn-sm" ${resp.page >= resp.totalPages - 1 ? 'disabled' : ''} onclick="FnPage.goto(${resp.page + 1})">Next ›</button>`;
        el.innerHTML = `<div style="display:flex;align-items:center;gap:8px;padding:10px 16px">
            ${prev}
            <span style="font-size:12px;color:var(--text-muted)">Page ${resp.page + 1} of ${resp.totalPages} (${resp.totalElements} total)</span>
            ${next}
        </div>`;
    }

    async function load(page = 0) {
        currentPage = page;
        const body = document.getElementById('fn-body');
        body.innerHTML = Nova.skeletonRows(8, COLS);

        const schema = currentSchema ? `&schema=${encodeURIComponent(currentSchema)}` : '';
        try {
            const resp = await Nova.apiFetch(`/functions?page=${page}&size=20${schema}`);
            // API may return a PageResponse wrapper or a plain array
            if (resp && typeof resp === 'object' && 'content' in resp) {
                renderBody(resp.content);
                renderPagination(resp);
            } else {
                renderBody(Array.isArray(resp) ? resp : []);
                document.getElementById('fn-pagination').style.display = 'none';
            }
        } catch (err) {
            body.innerHTML = `<tr><td colspan="${COLS}" class="text-muted" style="padding:20px;text-align:center">${err.message}</td></tr>`;
        }
    }

    async function populateSchemaFilter() {
        try {
            const schemas = await Nova.apiFetch('/schemas');
            const sel = document.getElementById('fn-schema-filter');
            (Array.isArray(schemas) ? schemas : schemas.content || []).forEach(s => {
                const opt = document.createElement('option');
                opt.value = s.name || s;
                opt.textContent = s.name || s;
                sel.appendChild(opt);
            });
        } catch (_) { /* non-critical */
        }
    }

    function init() {
        document.getElementById('fn-schema-filter').addEventListener('change', e => {
            currentSchema = e.target.value;
            if (Nova.isConnected()) load(0);
        });

        // If already connected when the page loads (navigating while connected),
        // load data immediately — nova:connected won't fire again in this case.
        if (Nova.isConnected()) {
            populateSchemaFilter();
            load(0);
        }
    }

    // Expose pagination helper globally so inline onclick handlers work
    window.FnPage = {goto: (p) => load(p)};

    document.addEventListener('DOMContentLoaded', init);
    document.addEventListener('nova:connected', () => {
        populateSchemaFilter();
        load(0);
    });
})();
