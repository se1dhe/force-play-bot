const tg = window.Telegram?.WebApp;
const state = {
    data: null,
    activeDraw: null,
    selectedRarity: '',
    busy: false,
    currentTab: 'draw'
};

const nodes = {
    app: document.getElementById('app'),
    preloader: document.getElementById('preloader'),
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
    dynamicBg: document.getElementById('dynamicBg'),
    cardAura: document.getElementById('cardAura')
};

function getInitData() {
    return tg?.initData || '';
}

tg?.ready();
tg?.expand();

// Initialize Lucide Icons
lucide.createIcons();

// Preloader Logic
window.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
        gsap.to(nodes.preloader, { 
            opacity: 0, 
            duration: 0.8, 
            ease: 'power2.inOut',
            onComplete: () => {
                nodes.preloader.style.display = 'none';
                gsap.to(nodes.app, { opacity: 1, duration: 0.6 });
            }
        });
    }, 1200);
});

nodes.startButton.addEventListener('click', startDraw);
nodes.claimButton.addEventListener('click', claimAndContinue);

// Bottom Navigation logic
document.querySelectorAll('.nav-btn').forEach((button) => {
    button.addEventListener('click', () => switchTab(button.dataset.tab));
});

// Chips filter logic
document.querySelectorAll('.chip').forEach((button) => {
    button.addEventListener('click', () => {
        document.querySelectorAll('.chip').forEach((item) => {
            item.classList.remove('active', 'bg-gold/20', 'border-gold', 'text-gold');
            item.classList.add('bg-panel', 'border-white/10', 'text-muted');
        });
        button.classList.add('active', 'bg-gold/20', 'border-gold', 'text-gold');
        button.classList.remove('bg-panel', 'border-white/10', 'text-muted');
        state.selectedRarity = button.dataset.rarity;
        loadHistory();
    });
});

renderDraw(null);
loadState();

async function api(path, options = {}) {
    const initData = getInitData();
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
        
        if (data.activeDraw && data.activeDraw.status !== 'OPEN') {
            const revealedCard = data.activeDraw.cards.find(c => c.revealed);
            if (revealedCard && revealedCard.reward) {
                renderResult({ reward: revealedCard.reward, arcana: { name: revealedCard.reward.arcanaName || 'Реликвия' } });
                nodes.result.classList.remove('hidden');
                nodes.result.style.opacity = '1';
                nodes.result.style.display = '';
                nodes.claimButton.classList.remove('hidden');
                nodes.claimButton.style.opacity = '1';
                nodes.claimButton.style.display = '';
            }
        } else {
            nodes.result.classList.add('hidden');
            nodes.result.style.display = '';
            nodes.claimButton.classList.add('hidden');
            nodes.claimButton.style.display = '';
        }
    });
}

async function startDraw() {
    return withBusy(async () => {
        await startDrawFlow();
    });
}

async function startDrawFlow() {
    const draw = await api('/api/tarot/draws/start', { method: 'POST' });
    state.activeDraw = draw;
    nodes.result.classList.add('hidden');
    nodes.result.style.display = '';
    nodes.claimButton.classList.add('hidden');
    nodes.claimButton.style.display = '';
    renderDraw(draw);
    tg?.HapticFeedback?.impactOccurred('medium');
    await loadState();
}

async function revealCard(cardIndex) {
    if (!state.activeDraw || state.activeDraw.status !== 'OPEN') {
        return;
    }
    return withBusy(async () => {
        const selectedCardEl = document.querySelector(`.card-wrapper[data-card-index="${cardIndex}"]`);
        const otherCardsEl = Array.from(document.querySelectorAll('.card-wrapper')).filter(el => Number(el.dataset.cardIndex) !== cardIndex);
        
        if (!selectedCardEl) return;
        
        // Disable vanilla-tilt
        if (selectedCardEl.vanillaTilt) selectedCardEl.vanillaTilt.destroy();
        otherCardsEl.forEach(el => el.vanillaTilt && el.vanillaTilt.destroy());

        tg?.HapticFeedback?.impactOccurred('medium');

        // Cinematic GSAP Timeline: Step 1 - Focus on selected card
        const tl = gsap.timeline();
        
        // Dim and drop other cards
        tl.to(otherCardsEl, {
            y: 50,
            opacity: 0,
            filter: 'blur(10px)',
            duration: 0.6,
            stagger: 0.1,
            ease: 'power3.inOut'
        }, 0);

        // Calculate offset to move selected card to exact center
        const rect = selectedCardEl.getBoundingClientRect();
        const centerX = window.innerWidth / 2 - (rect.left + rect.width / 2);
        // Move it UP slightly relative to its grid position
        const yOffset = -50; 
        
        // Float to center and scale
        tl.to(selectedCardEl, {
            x: centerX,
            y: yOffset,
            scale: 1.2,
            zIndex: 50,
            duration: 0.8,
            ease: 'power4.out'
        }, 0);

        // Fetch result while animation starts
        const result = await api(`/api/tarot/draws/${state.activeDraw.id}/reveal`, {
            method: 'POST',
            body: JSON.stringify({ cardIndex })
        });

        const selectedCardData = result.draw.cards.find(c => c.cardIndex === cardIndex);
        const rarity = selectedCardData?.reward?.rarity?.toLowerCase() || 'common';

        // Add Aura based on rarity
        let auraColor = 'rgba(255,255,255,0.4)';
        if (rarity === 'rare') auraColor = 'rgba(65, 105, 225, 0.6)';
        if (rarity === 'royal') auraColor = 'rgba(223, 177, 91, 0.8)';
        
        nodes.cardAura.style.background = `radial-gradient(circle at center, ${auraColor} 0%, transparent 60%)`;
        tl.to(nodes.cardAura, { opacity: 1, duration: 0.5 }, "-=0.3");

        // Suspense shake and levitate
        tl.to(selectedCardEl, {
            y: yOffset - 10,
            rotationZ: () => Math.random() * 4 - 2,
            duration: 0.1,
            yoyo: true,
            repeat: 9,
            ease: 'sine.inOut'
        });

        if (rarity === 'royal') {
            tg?.HapticFeedback?.impactOccurred('heavy');
            tl.to('body', { x: -3, duration: 0.05, yoyo: true, repeat: 10 }, "-=1");
        } else if (rarity === 'rare') {
            tg?.HapticFeedback?.impactOccurred('medium');
        } else {
            tg?.HapticFeedback?.impactOccurred('light');
        }

        // Wait for timeline to reach flip
        await new Promise(r => setTimeout(r, 1800));

        // Render card content before flip
        const inner = selectedCardEl.querySelector('.card-inner');
        if (selectedCardData && selectedCardData.reward) {
            const front = selectedCardEl.querySelector('.card-front');
            if (rarity === 'royal') front.classList.add('bg-gradient-to-br', 'from-[#b38e42]', 'to-[#5c3b12]', 'border-[#ffe099]');
            front.innerHTML = `
                <div class="flex flex-col items-center justify-center h-full gap-2 p-2 relative z-10 text-center">
                    ${iconTemplate(selectedCardData.reward.icon, 'w-12 h-12')}
                    <span class="text-sm font-bold text-white drop-shadow-lg">${escapeHtml(selectedCardData.reward.name)}</span>
                    <span class="text-[10px] uppercase tracking-widest ${rarity === 'royal' ? 'text-black font-black' : 'text-muted'}">${rarityLabel(selectedCardData.reward.rarity)}</span>
                </div>
            `;
        }

        // The FLIP!
        gsap.to(inner, {
            rotateY: 180,
            duration: 0.8,
            ease: 'back.out(1.4)'
        });

        // Fire confetti
        fireConfetti(rarity);
        if (rarity === 'royal') {
            tg?.HapticFeedback?.notificationOccurred('success');
            gsap.to('body', { x: -5, duration: 0.05, yoyo: true, repeat: 6 });
        } else {
            tg?.HapticFeedback?.impactOccurred('medium');
        }

        await new Promise(r => setTimeout(r, 1000));

        state.activeDraw = result.draw;
        renderProfile(result.profile);
        gsap.set(selectedCardEl, { clearProps: 'x,y,scale,zIndex,filter,opacity,rotationZ' });
        
        nodes.cardAura.style.opacity = '0';
        nodes.cards.classList.add('hidden');
        renderResult(result);
        
        // Fade in result
        gsap.fromTo(nodes.result, { opacity: 0, y: 30, scale: 0.9 }, { opacity: 1, y: 0, scale: 1, duration: 0.6, ease: 'back.out(1.2)', display: 'block' });
        nodes.claimButton.classList.remove('hidden');
        gsap.fromTo(nodes.claimButton, { opacity: 0, y: 20 }, { opacity: 1, y: 0, duration: 0.5, delay: 0.2 });
    });
}

async function claimAndContinue() {
    if (!state.activeDraw) return;
    return withBusy(async () => {
        await api(`/api/tarot/draws/${state.activeDraw.id}/claim`, { method: 'POST' });
        state.activeDraw = null;
        await loadState();

        if (state.data?.profile?.availableDraws > 0) {
            await startDrawFlow();
        } else {
            showToast('Трофей забран. Расклады закончились.');
        }
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
                showToast('Расклады добавлены');
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
            : '<div class="p-6 text-center text-muted border border-white/5 rounded-xl bg-panel shadow-glass">Первый трофей появится здесь.</div>';
    });
}

async function loadFeed() {
    return withBusy(async () => {
        const feed = await api('/api/tarot/feed');
        nodes.feedList.innerHTML = feed.length
            ? feed.map(feedItem).join('')
            : '<div class="p-6 text-center text-muted border border-white/5 rounded-xl bg-panel shadow-glass">Эпические открытия игроков появятся здесь.</div>';
    });
}

function switchTab(tab) {
    state.currentTab = tab;
    // Update nav buttons
    document.querySelectorAll('.nav-btn').forEach((btn) => {
        const isActive = btn.dataset.tab === tab;
        btn.classList.toggle('text-gold', isActive);
        btn.classList.toggle('text-muted', !isActive);
    });

    // Crossfade panels
    const currentPanel = document.querySelector('.panel-section:not(.hidden)');
    const nextPanel = document.querySelector(`.panel-section[data-panel="${tab}"]`);
    
    if (currentPanel && currentPanel !== nextPanel) {
        gsap.to(currentPanel, { 
            opacity: 0, x: -20, duration: 0.3, 
            onComplete: () => {
                currentPanel.classList.add('hidden');
                nextPanel.classList.remove('hidden');
                gsap.fromTo(nextPanel, { opacity: 0, x: 20 }, { opacity: 1, x: 0, duration: 0.4, ease: 'power2.out' });
            }
        });
    }

    if (tab === 'history') loadHistory();
    if (tab === 'feed') loadFeed();
}

function renderProfile(profile) {
    // GSAP Number counter effect for draws
    if (nodes.drawsCount.innerText !== String(profile.availableDraws)) {
        gsap.from(nodes.drawsCount, { scale: 1.5, color: '#ffffff', duration: 0.5 });
    }
    nodes.drawsCount.textContent = profile.availableDraws;
    nodes.pityCount.textContent = `${profile.pityCounter}/${profile.pityThreshold}`;
    nodes.collectionCount.textContent = `${profile.collectionOpened}/${profile.collectionTotal}`;
    
    if (profile.availableDraws <= 0 && !state.activeDraw) {
        nodes.startButton.classList.add('opacity-50', 'grayscale', 'pointer-events-none');
    } else {
        nodes.startButton.classList.remove('opacity-50', 'grayscale', 'pointer-events-none');
    }
}

function renderDraw(draw) {
    const cards = draw?.cards?.length ? draw.cards : [0, 1, 2].map((cardIndex) => ({ cardIndex }));
    nodes.cards.classList.remove('hidden');
    nodes.cards.classList.toggle('max-w-[210px]', draw && draw.status !== 'OPEN');
    nodes.cards.classList.toggle('max-w-[300px]', !(draw && draw.status !== 'OPEN'));
    nodes.cards.innerHTML = cards.map((card) => cardTemplate(card, draw?.status)).join('');
    
    nodes.cards.querySelectorAll('.card-wrapper').forEach((wrapper) => {
        if (draw?.status === 'OPEN' && !wrapper.classList.contains('revealed')) {
            wrapper.addEventListener('click', () => revealCard(Number(wrapper.dataset.cardIndex)));
            // Initialize VanillaTilt for unrevealed open cards
            VanillaTilt.init(wrapper.querySelector('.card'), {
                max: 15,
                speed: 400,
                glare: true,
                "max-glare": 0.4,
                perspective: 1000
            });
        }
    });

    if (draw && draw.status === 'OPEN') {
        nodes.startButton.classList.add('hidden');
    } else {
        nodes.startButton.classList.remove('hidden');
    }
    
    // Add entry animation for cards
    if (!draw || draw.status !== 'OPEN') {
        gsap.fromTo('.card-wrapper', 
            { y: 50, opacity: 0, rotateY: -180 }, 
            { y: 0, opacity: 1, rotateY: 0, duration: 0.8, stagger: 0.1, ease: 'back.out(1.2)' }
        );
    }
}

function renderResult(result) {
    const isRoyal = result.reward.rarity === 'ROYAL';
    nodes.result.innerHTML = `
        <h2 class="text-xl font-cinzel font-bold ${isRoyal ? 'text-[#ffd700] drop-shadow-[0_0_10px_#dfb15b]' : 'text-gold'} mb-1 leading-none">
            ${iconTemplate(result.reward.icon, 'w-12 h-12 mx-auto mb-2')} ${escapeHtml(result.reward.name)}
        </h2>
        <p class="text-[11px] text-gray-300 mb-3 leading-tight px-2">${escapeHtml(result.reward.description || '')}</p>
        <div class="flex flex-col gap-1.5">
            <div class="flex justify-between items-center bg-black/40 px-3 py-1.5 rounded-lg border border-white/5">
                <span class="text-[10px] text-muted uppercase tracking-wider">Реликвия</span>
                <strong class="text-xs text-white">${escapeHtml(result.arcana?.name || result.reward?.arcanaName || 'Реликвия')}</strong>
            </div>
            <div class="flex justify-between items-center bg-black/40 px-3 py-1.5 rounded-lg border border-white/5">
                <span class="text-[10px] text-muted uppercase tracking-wider">Грейд</span>
                <strong class="text-xs ${isRoyal ? 'text-[#dfb15b]' : 'text-white'}">${rarityLabel(result.reward.rarity)}</strong>
            </div>
        </div>
    `;
}

function renderPackages(packages) {
    nodes.packages.innerHTML = packages
        .filter((pack) => pack.enabled)
        .map((pack) => `
            <button class="buy-btn relative overflow-hidden flex items-center justify-center gap-2 min-h-[42px] px-2 py-1.5 rounded-lg bg-black/35 border border-white/10 shadow-glass hover:border-gold/50 active:scale-95 transition-all" data-package="${pack.code}">
                <div class="flex flex-col items-center leading-none">
                    <strong class="text-sm text-white font-outfit leading-none">${pack.draws}</strong>
                    <span class="text-[8px] text-muted uppercase tracking-wider mt-0.5">раскл.</span>
                </div>
                <div class="flex items-center gap-1 bg-black/50 px-1.5 py-0.5 rounded-full border border-gold/20">
                    <i data-lucide="star" class="w-2.5 h-2.5 text-gold"></i>
                    <span class="text-[11px] text-gold font-bold">${pack.starsPrice}</span>
                </div>
            </button>
        `).join('');
        
    lucide.createIcons();
    nodes.packages.querySelectorAll('.buy-btn').forEach((button) => {
        button.addEventListener('click', () => buyPackage(button.dataset.package));
    });
}

function cardTemplate(card, status) {
    const revealed = status !== 'OPEN' && card.revealed;
    const reward = card.reward;
    const royal = reward?.rarity === 'ROYAL';
    
    // Base classes for the 3D card
    let innerTransform = revealed ? 'rotateY(180deg)' : 'rotateY(0deg)';
    
    return `
        <div class="card-wrapper relative w-full aspect-[0.64] ${revealed ? 'revealed' : ''} transition-all duration-500" data-card-index="${card.cardIndex}">
            <div class="card w-full h-full relative cursor-pointer" style="transform-style: preserve-3d; perspective: 1000px;">
                <div class="card-inner absolute inset-0 w-full h-full" style="transform-style: preserve-3d; transition: transform 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275); transform: ${innerTransform};">
                    
                    <!-- BACK -->
                    <div class="card-face card-back absolute inset-0 w-full h-full rounded-lg border-2 border-gold/30 shadow-[0_10px_20px_rgba(0,0,0,0.6)] bg-cover bg-center" style="backface-visibility: hidden; background-image: var(--card-back, url('/tarot/assets/card_back_1.jpg'));">
                        <div class="absolute inset-0 shadow-[inset_0_0_20px_rgba(0,0,0,0.8)] rounded-lg"></div>
                    </div>
                    
                    <!-- FRONT -->
                    <div class="card-face card-front absolute inset-0 w-full h-full rounded-lg border-2 ${royal ? 'border-[#ffe099] bg-gradient-to-br from-[#2b1706] to-[#b38e42]' : 'border-[#a37c56] bg-gradient-to-br from-[#1b1622] to-[#5e3b26]'} shadow-[0_10px_20px_rgba(0,0,0,0.6)]" style="backface-visibility: hidden; transform: rotateY(180deg);">
                        <div class="absolute inset-0 shadow-[inset_0_0_20px_rgba(0,0,0,0.8)] rounded-lg"></div>
                        <div class="flex flex-col items-center justify-center h-full gap-2 p-2 relative z-10 text-center">
                            ${iconTemplate(reward?.icon, 'w-10 h-10')}
                            <span class="text-xs font-bold text-white drop-shadow-lg leading-tight">${escapeHtml(reward?.name || 'Судьба')}</span>
                            <span class="text-[9px] uppercase tracking-widest ${royal ? 'text-[#38250f] font-black' : 'text-muted'}">${reward ? rarityLabel(reward.rarity) : ''}</span>
                        </div>
                    </div>
                    
                </div>
            </div>
        </div>
    `;
}

function historyItem(item) {
    const rarity = item.reward.rarity ? item.reward.rarity.toLowerCase() : 'common';
    const isRoyal = rarity === 'royal';
    
    return `
        <article class="flex flex-col gap-1 p-4 rounded-xl border ${isRoyal ? 'border-l-4 border-l-[#dfb15b] bg-gradient-to-r from-[#281e0f] to-[#140f08] shadow-[inset_0_0_15px_rgba(223,177,91,0.15)]' : 'border-l-4 border-l-gold-dark bg-gradient-to-r from-panel to-bg border-white/5'}">
            <strong class="text-base ${isRoyal ? 'text-[#dfb15b] drop-shadow-[0_0_5px_rgba(223,177,91,0.5)]' : 'text-gold'} flex items-center gap-2">${iconTemplate(item.reward.icon, 'w-8 h-8 shrink-0')} <span>${escapeHtml(item.reward.name)}</span></strong>
            <div class="flex justify-between items-center mt-1">
                <span class="text-xs text-muted">${rarityLabel(item.reward.rarity)}</span>
                <span class="text-[10px] text-gray-500">${formatDate(item.createdAt)}</span>
            </div>
            <div class="mt-2 pt-2 border-t border-white/5 text-xs text-gray-400">
                Реликвия: <span class="text-white">${escapeHtml(item.arcana.name)}</span>
            </div>
        </article>
    `;
}

function feedItem(item) {
    const rarity = item.reward.rarity ? item.reward.rarity.toLowerCase() : 'common';
    const isRoyal = rarity === 'royal';
    
    return `
        <article class="flex flex-col gap-1 p-4 rounded-xl border ${isRoyal ? 'border-l-4 border-l-[#dfb15b] bg-gradient-to-r from-[#281e0f] to-[#140f08] shadow-[inset_0_0_15px_rgba(223,177,91,0.15)]' : 'border-l-4 border-l-gold-dark bg-gradient-to-r from-panel to-bg border-white/5'}">
            <strong class="text-base ${isRoyal ? 'text-[#dfb15b]' : 'text-gold'} leading-tight">
                <span class="text-white text-sm font-normal mr-1">${escapeHtml(item.username)} открыл</span><br/>
                <span class="inline-flex items-center gap-2 mt-1">${iconTemplate(item.reward.icon, 'w-8 h-8 shrink-0')} <span>${escapeHtml(item.reward.name)}</span></span>
            </strong>
            <div class="flex justify-between items-center mt-1">
                <span class="text-xs text-muted">${rarityLabel(item.reward.rarity)}</span>
                <span class="text-[10px] text-gray-500">${formatDate(item.createdAt)}</span>
            </div>
        </article>
    `;
}

function applyAssets(assets) {
    if (assets.background) {
        nodes.dynamicBg.style.backgroundImage = `url("${assets.background}")`;
    }
    document.documentElement.style.setProperty('--card-back', `url("${assets.cardBacks?.[0] || '/tarot/assets/card_back_1.jpg'}")`);
}

async function withBusy(task) {
    if (state.busy) return;
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
    document.querySelectorAll('button:not(.nav-btn):not(.chip)').forEach((button) => {
        button.disabled = busy;
        if (busy) button.classList.add('opacity-50', 'pointer-events-none');
        else button.classList.remove('opacity-50', 'pointer-events-none');
    });
    if (busy) {
    }
}

function showToast(message) {
    nodes.toast.textContent = message;
    gsap.to(nodes.toast, { y: 0, opacity: 1, duration: 0.4, ease: 'back.out' });
    
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => {
        gsap.to(nodes.toast, { y: -20, opacity: 0, duration: 0.4, ease: 'power2.in' });
    }, 2600);
}

function fireConfetti(rarity) {
    const origin = { x: 0.5, y: 0.5 };
    if (rarity === 'royal') {
        confetti({ particleCount: 150, spread: 100, origin, colors: ['#FFD700', '#FFA500', '#ffffff'], gravity: 1.2, ticks: 400 });
        setTimeout(() => {
            confetti({ particleCount: 100, angle: 60, spread: 55, origin: { x: 0, y: 0.8 }, colors: ['#FFD700', '#FFA500'] });
            confetti({ particleCount: 100, angle: 120, spread: 55, origin: { x: 1, y: 0.8 }, colors: ['#FFD700', '#FFA500'] });
        }, 200);
    } else if (rarity === 'rare') {
        confetti({ particleCount: 80, spread: 70, origin, colors: ['#8A2BE2', '#4169E1', '#ffffff'] });
    } else {
        confetti({ particleCount: 40, spread: 50, origin, colors: ['#C0C0C0', '#ffffff'] });
    }
}

function rarityLabel(rarity) {
    return { COMMON: 'Обычная', RARE: 'Редкая', ROYAL: 'Эпическая' }[rarity] || rarity;
}

function iconTemplate(icon, className = 'w-8 h-8') {
    if (!icon) {
        return '<span class="text-3xl drop-shadow-md">✦</span>';
    }
    if (icon.startsWith('/')) {
        return `<img src="${escapeHtml(icon)}" alt="" class="${className} object-contain drop-shadow-[0_0_10px_rgba(223,177,91,0.45)] pixelated" loading="lazy">`;
    }
    return `<span class="text-3xl drop-shadow-md">${escapeHtml(icon)}</span>`;
}

function formatDate(value) {
    return new Intl.DateTimeFormat('ru-RU', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' }).format(new Date(value));
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' })[c]);
}
