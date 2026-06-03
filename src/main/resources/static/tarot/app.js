const tg = window.Telegram?.WebApp;
const state = {
    data: null,
    activeDraw: null,
    selectedRarity: '',
    busy: false
};

const nodes = {
    stage: document.getElementById('stage'),
    drawsCount: document.getElementById('drawsCount'),
    pityCount: document.getElementById('pityCount'),
    collectionCount: document.getElementById('collectionCount'),
    cards: document.getElementById('cards'),
    result: document.getElementById('result'),
    startButton: document.getElementById('startButton'),
    claimButton: document.getElementById('claimButton'),
    packages: document.getElementById('packages'),
    historyList: document.getElementById('historyList'),
    feedList: document.getElementById('feedList'),
    toast: document.getElementById('toast'),
    confetti: document.getElementById('confetti')
};

const initData = tg?.initData || '';

tg?.ready();
tg?.expand();

document.getElementById('refreshButton').addEventListener('click', loadState);
nodes.startButton.addEventListener('click', startDraw);
nodes.claimButton.addEventListener('click', claimReward);

document.querySelectorAll('.tab').forEach((button) => {
    button.addEventListener('click', () => switchTab(button.dataset.tab));
});

document.querySelectorAll('[data-rarity]').forEach((button) => {
    button.addEventListener('click', () => {
        document.querySelectorAll('[data-rarity]').forEach((item) => item.classList.remove('active'));
        button.classList.add('active');
        state.selectedRarity = button.dataset.rarity;
        loadHistory();
    });
});

renderDraw(null);
loadState();

async function api(path, options = {}) {
    if (!initData) {
        throw new Error('Откройте приложение через Telegram.');
    }
    const response = await fetch(path, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            'X-Telegram-Init-Data': initData,
            ...(options.headers || {})
        }
    });
    const payload = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(payload.error || 'Ошибка сервера.');
    }
    return payload;
}

async function loadState() {
    return withBusy(async () => {
        const data = await api('/api/tarot/state');
        state.data = data;
        state.activeDraw = data.activeDraw;
        applyAssets(data.assets);
        renderProfile(data.profile);
        renderPackages(data.packages);
        renderDraw(data.activeDraw);
    });
}

async function startDraw() {
    return withBusy(async () => {
        const draw = await api('/api/tarot/draws/start', { method: 'POST' });
        state.activeDraw = draw;
        nodes.result.hidden = true;
        nodes.claimButton.hidden = true;
        renderDraw(draw);
        tg?.HapticFeedback?.impactOccurred('medium');
        await loadState();
    });
}

async function revealCard(cardIndex) {
    if (!state.activeDraw || state.activeDraw.status !== 'OPEN') {
        return;
    }
    return withBusy(async () => {
        const result = await api(`/api/tarot/draws/${state.activeDraw.id}/reveal`, {
            method: 'POST',
            body: JSON.stringify({ cardIndex })
        });
        state.activeDraw = result.draw;
        renderProfile(result.profile);
        renderDraw(result.draw);
        renderResult(result);
        if (result.royal) {
            burstConfetti();
            tg?.HapticFeedback?.notificationOccurred('success');
        } else {
            tg?.HapticFeedback?.impactOccurred('light');
        }
    });
}

async function claimReward() {
    if (!state.activeDraw) {
        return;
    }
    return withBusy(async () => {
        await api(`/api/tarot/draws/${state.activeDraw.id}/claim`, { method: 'POST' });
        showToast('Награда получена.');
        nodes.claimButton.hidden = true;
        state.activeDraw = null;
        await loadState();
    });
}

async function buyPackage(code) {
    return withBusy(async () => {
        const invoice = await api('/api/tarot/payments/invoice', {
            method: 'POST',
            body: JSON.stringify({ packageCode: code })
        });
        tg?.openInvoice(invoice.invoiceUrl, (status) => {
            if (status === 'paid') {
                showToast('Покупка оплачена.');
                loadState();
            }
        });
    });
}

async function loadHistory() {
    return withBusy(async () => {
        const suffix = state.selectedRarity ? `?rarity=${state.selectedRarity}` : '';
        const history = await api(`/api/tarot/history${suffix}`);
        nodes.historyList.innerHTML = history.length
            ? history.map(historyItem).join('')
            : '<div class="list-item"><strong>История пуста</strong><span>Первое гадание появится здесь.</span></div>';
    });
}

async function loadFeed() {
    return withBusy(async () => {
        const feed = await api('/api/tarot/feed');
        nodes.feedList.innerHTML = feed.length
            ? feed.map(feedItem).join('')
            : '<div class="list-item"><strong>Лента пока пуста</strong><span>Открытия игроков появятся здесь.</span></div>';
    });
}

function switchTab(tab) {
    document.querySelectorAll('.tab').forEach((button) => button.classList.toggle('active', button.dataset.tab === tab));
    document.querySelectorAll('.panel').forEach((panel) => panel.classList.toggle('active', panel.dataset.panel === tab));
    if (tab === 'history') {
        loadHistory();
    }
    if (tab === 'feed') {
        loadFeed();
    }
}

function renderProfile(profile) {
    nodes.drawsCount.textContent = profile.availableDraws;
    nodes.pityCount.textContent = `${profile.pityCounter}/${profile.pityThreshold}`;
    nodes.collectionCount.textContent = `${profile.collectionOpened}/${profile.collectionTotal}`;
    nodes.startButton.disabled = profile.availableDraws <= 0 && !state.activeDraw;
}

function renderDraw(draw) {
    const cards = draw?.cards?.length ? draw.cards : [0, 1, 2].map((cardIndex) => ({ cardIndex }));
    nodes.cards.innerHTML = cards.map((card) => cardTemplate(card, draw?.status)).join('');
    nodes.cards.querySelectorAll('.card').forEach((button) => {
        button.addEventListener('click', () => revealCard(Number(button.dataset.cardIndex)));
    });
    nodes.startButton.hidden = Boolean(draw && draw.status === 'OPEN');
}

function renderResult(result) {
    nodes.result.hidden = false;
    nodes.result.innerHTML = `
        <h2>${result.reward.icon || ''} ${escapeHtml(result.reward.name)}</h2>
        <p>${escapeHtml(result.reward.description || '')}</p>
        <p>Аркан: <strong>${escapeHtml(result.arcana.name)}</strong></p>
        <p>Редкость: ${rarityLabel(result.reward.rarity)}</p>
    `;
    nodes.claimButton.hidden = false;
}

function renderPackages(packages) {
    nodes.packages.innerHTML = packages
        .filter((pack) => pack.enabled)
        .map((pack) => `<button class="buy-button" type="button" data-package="${pack.code}">${pack.draws} гад. <span>${pack.starsPrice} Stars</span></button>`)
        .join('');
    nodes.packages.querySelectorAll('[data-package]').forEach((button) => {
        button.addEventListener('click', () => buyPackage(button.dataset.package));
    });
}

function cardTemplate(card, status) {
    const revealed = status !== 'OPEN' && card.revealed;
    const reward = card.reward;
    const royal = reward?.rarity === 'ROYAL';
    return `
        <button class="card ${revealed ? 'revealed' : ''}" type="button" data-card-index="${card.cardIndex}" ${status !== 'OPEN' ? 'disabled' : ''} aria-label="Карта ${card.cardIndex + 1}">
            <span class="card-inner">
                <span class="card-face card-back"></span>
                <span class="card-face card-front ${royal ? 'royal' : ''}">
                    <span class="card-icon">${reward?.icon || '✦'}</span>
                    <span class="card-name">${escapeHtml(reward?.name || 'Судьба')}</span>
                    <span class="card-rarity">${reward ? rarityLabel(reward.rarity) : ''}</span>
                </span>
            </span>
        </button>
    `;
}

function historyItem(item) {
    return `
        <article class="list-item">
            <strong>${item.reward.icon || ''} ${escapeHtml(item.reward.name)}</strong>
            <span>${rarityLabel(item.reward.rarity)} · ${formatDate(item.createdAt)}</span>
            <span>Аркан: ${escapeHtml(item.arcana.name)}</span>
        </article>
    `;
}

function feedItem(item) {
    return `
        <article class="list-item">
            <strong>${escapeHtml(item.username)} открыл ${escapeHtml(item.reward.name)}</strong>
            <span>${rarityLabel(item.reward.rarity)} · ${formatDate(item.createdAt)}</span>
            <span>Аркан: ${escapeHtml(item.arcana.name)}</span>
        </article>
    `;
}

function applyAssets(assets) {
    document.documentElement.style.setProperty('--tarot-bg', `url("${assets.background}")`);
    document.documentElement.style.setProperty('--card-back', `url("${assets.cardBacks?.[0] || '/tarot/assets/card_back_1.jpg'}")`);
}

async function withBusy(task) {
    if (state.busy) {
        return;
    }
    state.busy = true;
    setBusy(true);
    try {
        await task();
    } catch (error) {
        showToast(error.message);
    } finally {
        state.busy = false;
        setBusy(false);
    }
}

function setBusy(busy) {
    document.querySelectorAll('button').forEach((button) => {
        if (!button.classList.contains('tab') && !button.classList.contains('chip')) {
            button.disabled = busy;
        }
    });
}

function showToast(message) {
    nodes.toast.textContent = message;
    nodes.toast.hidden = false;
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => {
        nodes.toast.hidden = true;
    }, 2600);
}

function burstConfetti() {
    nodes.confetti.innerHTML = '';
    for (let i = 0; i < 42; i++) {
        const piece = document.createElement('i');
        piece.style.left = `${Math.random() * 100}%`;
        piece.style.animationDelay = `${Math.random() * 260}ms`;
        piece.style.background = ['#f4c76b', '#58c5b4', '#c95f70', '#f5efe5'][i % 4];
        nodes.confetti.appendChild(piece);
    }
    window.setTimeout(() => {
        nodes.confetti.innerHTML = '';
    }, 1600);
}

function rarityLabel(rarity) {
    return {
        COMMON: 'Обычная',
        RARE: 'Редкая',
        ROYAL: 'Королевская'
    }[rarity] || rarity;
}

function formatDate(value) {
    return new Intl.DateTimeFormat('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).format(new Date(value));
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (char) => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    })[char]);
}
