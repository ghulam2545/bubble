(() => {

    function row(e) {
        return `<tr>
            <td class="mono">${e.name}</td>
            <td>${e.version || '—'}</td>
            <td>${e.schema || '—'}</td>
            <td>${e.relocatable ? 'Yes' : 'No'}</td>
            <td style="color:var(--text-muted);font-size:12px">${e.comment || '—'}</td>
        </tr>`;
    }

    function rowAvail(e) {
        return `<tr>
            <td class="mono">${e.name}</td>
            <td>${e.defaultVersion || '—'}</td>
            <td style="color:var(--text-muted);font-size:12px">${e.comment || '—'}</td>
        </tr>`;
    }

    function render(bodyId, data, fn, cols) {
        const body = document.getElementById(bodyId);
        if (!data?.length) {
            body.innerHTML = `<tr><td colspan="${cols}" class="text-muted" style="padding:20px;text-align:center">No data</td></tr>`;
            return;
        }
        body.innerHTML = data.map(fn).join('');
    }

    let loaded = new Set();

    async function loadTab(tab) {
        if (loaded.has(tab)) return;
        loaded.add(tab);
        const ep = tab === 'installed' ? '/extensions' : '/extensions/available';
        const bodyId = tab === 'installed' ? 'ext-installed-body' : 'ext-available-body';
        const cols = tab === 'installed' ? 5 : 3;
        const fn = tab === 'installed' ? row : rowAvail;

        document.getElementById(bodyId).innerHTML = Nova.skeletonRows(5, cols);
        try {
            const data = await Nova.apiFetch(ep);
            render(bodyId, data, fn, cols);
        } catch (err) {
            document.getElementById(bodyId).innerHTML =
                `<tr><td colspan="${cols}" class="text-muted" style="padding:20px;text-align:center">${err.message}</td></tr>`;
        }
    }

    function init() {
        const container = document.getElementById('ext-tabs');
        const tabs = container.querySelectorAll('.tab');
        const panels = container.querySelectorAll('.tab-panel');

        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                tabs.forEach(t => t.classList.remove('active'));
                panels.forEach(p => p.classList.remove('active'));
                tab.classList.add('active');
                container.querySelector(`.tab-panel[data-tab="${tab.dataset.tab}"]`)?.classList.add('active');
                if (Nova.isConnected()) loadTab(tab.dataset.tab);
            });
        });

        if (tabs.length) tabs[0].click();
    }

    function load() {
        if (!Nova.isConnected()) return;
        const active = document.querySelector('#ext-tabs .tab.active');
        if (active) loadTab(active.dataset.tab);
    }

    document.addEventListener('DOMContentLoaded', init);
    document.addEventListener('nova:connected', load);
})();
