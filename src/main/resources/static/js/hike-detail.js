(function () {
    const section = document.querySelector('section[data-hike-id]');
    if (!section || section.dataset.organizer !== 'true') {
        return;
    }

    const hikeId = section.dataset.hikeId;
    const form = document.getElementById('gearAjaxForm');
    const messageEl = document.getElementById('gearAjaxMessage');
    const tbody = document.getElementById('gearTableBody');

    if (!form || !tbody) {
        return;
    }

    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        const name = document.getElementById('gearName').value.trim();
        const weightKg = parseFloat(document.getElementById('gearWeight').value);

        if (!name || !weightKg || weightKg <= 0) {
            messageEl.textContent = 'Укажите название и вес > 0';
            messageEl.className = 'small mt-2 text-danger';
            return;
        }

        try {
            const response = await fetch('/api/hikes/' + hikeId + '/gear/shared', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify({ name: name, weightKg: weightKg, type: 'SHARED' })
            });

            if (!response.ok) {
                const err = await response.json().catch(() => ({}));
                throw new Error(err.detail || 'Ошибка добавления');
            }

            await response.json();
            form.reset();
            messageEl.textContent = 'Предмет добавлен';
            messageEl.className = 'small mt-2 text-success';
            setTimeout(function () { window.location.reload(); }, 500);
        } catch (err) {
            messageEl.textContent = err.message;
            messageEl.className = 'small mt-2 text-danger';
        }
    });
})();
