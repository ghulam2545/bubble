document.addEventListener('DOMContentLoaded', async () => {

    const tableBody = document.getElementById('schemas-table-body');
    const detailPane = document.getElementById('schema-detail');
    const filterInp = document.getElementById('schema-filter');
    const sysToggle = document.getElementById('include-system');

    let allSchemas = [];

    // ── Load schemas list ──────────────────────────────────────────────────
    async function loadSchemas() {
        const includeSystem = sysToggle?.checked || false;
        if (tableBody) tableBody.innerHTML = Nova.skeletonRows(6, 4);
        try {
            allSchemas = await Nova.apiFetch(`/schemas?includeSystem=${includeSystem}`);
            renderTable(allSchemas);
        } catch (err) {
            Nova.toast('Failed to load schemas: ' + err.message, 'error');
            if (tableBody) tableBody.innerHTML = `<tr><td colspan="6" class="text-muted" style="padding:20px;text-align:center">Error loading data</td></tr>`;
        }
    }

    function renderTable(schemas) {
        if (!tableBody) return;
        if (!schemas.length) {
            tableBody.innerHTML = `<tr><td colspan="6"><div class="empty-state"><div class="empty-icon">◻</div><div class="empty-label">No schemas found</div></div></td></tr>`;
            return;
        }
        tableBody.innerHTML = schemas.map(s => `
            <tr class="schema-row" data-name="${s.name}" style="cursor:pointer">
                <td class="mono">${s.name}</td>
                <td>${s.owner || '—'}</td>
                <td>${s.tablesCount ?? '—'}</td>
                <td>${s.viewsCount ?? '—'}</td>
                <td>${s.sequencesCount ?? '—'}</td>
                <td>${s.totalSize || '—'}</td>
            </tr>
        `).join('');

        tableBody.querySelectorAll('.schema-row').forEach(row => {
            row.addEventListener('click', () => loadSchemaDetail(row.dataset.name));
        });
    }

    // ── Schema detail ──────────────────────────────────────────────────────
    async function loadSchemaDetail(name) {
        if (!detailPane) return;

        // highlight active row
        document.querySelectorAll('.schema-row').forEach(r => r.classList.remove('active-row'));
        document.querySelector(`.schema-row[data-name="${name}"]`)?.classList.add('active-row');

        detailPane.classList.remove('hidden');
        document.getElementById('detail-schema-name').textContent = name;

        // init tabs
        Nova.initTabs(detailPane);

        // load sub-resources in parallel
        const loadList = async (endpoint, listId, emptyMsg) => {
            const el = document.getElementById(listId);
            if (!el) return;
            el.innerHTML = '<div class="skeleton" style="height:13px;margin:6px 0;width:60%"></div>'.repeat(3);
            try {
                const items = await Nova.apiFetch(`/schemas/${name}/${endpoint}`);
                if (!items.length) {
                    el.innerHTML = `<div class="empty-label text-muted">${emptyMsg}</div>`;
                    return;
                }
                el.innerHTML = items.map(i => `<div class="mono" style="padding:5px 0;border-bottom:1px solid var(--border);font-size:12px">${i}</div>`).join('');
            } catch {
                el.innerHTML = '<div class="text-error" style="font-size:12px">Failed to load</div>';
            }
        };

        loadList('tables', 'detail-tables', 'No tables');
        loadList('views', 'detail-views', 'No views');
        loadList('sequences', 'detail-sequences', 'No sequences');
        loadList('functions', 'detail-functions', 'No functions');
        loadList('types', 'detail-types', 'No types');
    }

    // ── Filter ─────────────────────────────────────────────────────────────
    filterInp?.addEventListener('input', () => {
        const q = filterInp.value.toLowerCase();
        renderTable(allSchemas.filter(s => s.name.toLowerCase().includes(q)));
    });

    sysToggle?.addEventListener('change', loadSchemas);

    loadSchemas();
});
