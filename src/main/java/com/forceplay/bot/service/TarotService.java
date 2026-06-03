package com.forceplay.bot.service;

import com.forceplay.bot.dto.tarot.TarotDto;
import com.forceplay.bot.dto.tarot.TelegramWebAppUser;
import com.forceplay.bot.model.TarotArcana;
import com.forceplay.bot.model.TarotDeck;
import com.forceplay.bot.model.TarotDraw;
import com.forceplay.bot.model.TarotDrawCard;
import com.forceplay.bot.model.TarotDrawStatus;
import com.forceplay.bot.model.TarotFeedEntry;
import com.forceplay.bot.model.TarotPurchase;
import com.forceplay.bot.model.TarotPurchaseStatus;
import com.forceplay.bot.model.TarotRarity;
import com.forceplay.bot.model.TarotReward;
import com.forceplay.bot.model.TarotSeason;
import com.forceplay.bot.model.TarotUserProfile;
import com.forceplay.bot.model.User;
import com.forceplay.bot.model.UserTarotArcana;
import com.forceplay.bot.repository.TarotArcanaRepository;
import com.forceplay.bot.repository.TarotDeckRepository;
import com.forceplay.bot.repository.TarotDrawCardRepository;
import com.forceplay.bot.repository.TarotDrawRepository;
import com.forceplay.bot.repository.TarotFeedEntryRepository;
import com.forceplay.bot.repository.TarotPurchaseRepository;
import com.forceplay.bot.repository.TarotRewardRepository;
import com.forceplay.bot.repository.TarotSeasonRepository;
import com.forceplay.bot.repository.TarotUserProfileRepository;
import com.forceplay.bot.repository.UserTarotArcanaRepository;
import com.forceplay.bot.util.ForcePlayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.invoices.CreateInvoiceLink;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class TarotService {

    private static final ZoneId DAILY_ZONE = ZoneId.of("Europe/Kiev");
    private static final List<TarotDrawStatus> FINISHED_STATUSES = List.of(TarotDrawStatus.REVEALED, TarotDrawStatus.CLAIMED);

    private final TelegramWebAppAuthService authService;
    private final UserService userService;
    private final TarotSeasonRepository seasonRepository;
    private final TarotDeckRepository deckRepository;
    private final TarotArcanaRepository arcanaRepository;
    private final TarotRewardRepository rewardRepository;
    private final TarotUserProfileRepository profileRepository;
    private final TarotDrawRepository drawRepository;
    private final TarotDrawCardRepository drawCardRepository;
    private final UserTarotArcanaRepository userArcanaRepository;
    private final TarotFeedEntryRepository feedEntryRepository;
    private final TarotPurchaseRepository purchaseRepository;
    private final JdbcTemplate jdbcTemplate;
    private final AdminService adminService;
    private final TelegramGateway telegramGateway;
    private final TelegramBotPropertiesAccessor botPropertiesAccessor;
    private final TelegramClient telegramClient;

    @Transactional
    public TarotDto.StateResponse state(String initData) {
        TelegramWebAppUser telegramUser = authService.authenticate(initData);
        TarotUserProfile profile = profileRepository.findByUserTelegramId(telegramUser.id())
                .orElseGet(() -> buildTransientProfile(userService.getOrCreateUser(telegramUser.id(), telegramUser.languageCode())));
        TarotSeason season = activeSeason();
        return new TarotDto.StateResponse(
                profileDto(profile, season),
                drawRepository.findFirstByUserTelegramIdAndStatusOrderByCreatedAtDesc(telegramUser.id(), TarotDrawStatus.OPEN)
                        .map(this::drawDtoHidden)
                        .orElse(null),
                assetConfig(),
                purchasePackages()
        );
    }

    @Transactional
    public TarotDto.DrawDto startDraw(String initData) {
        TelegramWebAppUser telegramUser = authService.authenticate(initData);
        Optional<TarotDraw> existing = drawRepository.findFirstByUserTelegramIdAndStatusOrderByCreatedAtDesc(telegramUser.id(), TarotDrawStatus.OPEN);
        if (existing.isPresent()) {
            return drawDtoHidden(existing.get());
        }

        User user = userService.getOrCreateUser(telegramUser.id(), telegramUser.languageCode());
        TarotUserProfile profile = lockedProfile(user);
        resetFreeDrawsIfNeeded(profile);
        consumeDraw(profile);

        TarotSeason season = activeSeason();
        TarotDeck deck = activeDeck(season);
        int pityThreshold = intSetting("pity_threshold", 50);
        boolean guaranteedRoyal = profile.getPityCounter() >= pityThreshold;
        TarotDraw draw = drawRepository.save(TarotDraw.builder()
                .user(user)
                .season(season)
                .deck(deck)
                .status(TarotDrawStatus.OPEN)
                .guaranteedRoyal(guaranteedRoyal)
                .createdAt(OffsetDateTime.now())
                .build());

        int royalIndex = guaranteedRoyal ? ThreadLocalRandom.current().nextInt(3) : -1;
        for (int index = 0; index < 3; index++) {
            TarotReward reward = royalIndex == index
                    ? weightedReward(season, List.of(TarotRarity.ROYAL))
                    : weightedReward(season, List.of(TarotRarity.COMMON, TarotRarity.RARE));
            drawCardRepository.save(TarotDrawCard.builder()
                    .draw(draw)
                    .cardIndex(index)
                    .reward(reward)
                    .arcana(randomArcana(season))
                    .royalCard(reward.getRarity() == TarotRarity.ROYAL)
                    .selected(false)
                    .build());
        }

        profile.setUpdatedAt(OffsetDateTime.now());
        return drawDtoHidden(draw);
    }

    @Transactional
    public TarotDto.RevealResponse reveal(String initData, Long drawId, int cardIndex) {
        TelegramWebAppUser telegramUser = authService.authenticate(initData);
        if (cardIndex < 0 || cardIndex > 2) {
            throw new ForcePlayException("Карта не найдена.");
        }

        TarotDraw draw = drawRepository.lockByIdAndUserTelegramId(drawId, telegramUser.id())
                .orElseThrow(() -> new ForcePlayException("Расклад не найден."));
        TarotSeason season = draw.getSeason();
        TarotUserProfile profile = lockedProfile(draw.getUser());

        if (draw.getStatus() != TarotDrawStatus.OPEN) {
            return new TarotDto.RevealResponse(drawDtoRevealed(draw), rewardDto(draw.getSelectedReward()),
                    arcanaDto(draw.getSelectedArcana()), draw.getSelectedReward().getRarity() == TarotRarity.ROYAL,
                    false, profileDto(profile, season));
        }

        TarotDrawCard selectedCard = drawCardRepository.findByDrawIdAndCardIndex(drawId, cardIndex)
                .orElseThrow(() -> new ForcePlayException("Карта не найдена."));
        selectedCard.setSelected(true);
        draw.setSelectedCardIndex(cardIndex);
        draw.setSelectedReward(selectedCard.getReward());
        draw.setSelectedArcana(selectedCard.getArcana());
        draw.setStatus(TarotDrawStatus.REVEALED);
        draw.setRevealedAt(OffsetDateTime.now());

        boolean royal = selectedCard.getReward().getRarity() == TarotRarity.ROYAL;
        if (royal) {
            profile.setPityCounter(0);
        } else {
            profile.setPityCounter(profile.getPityCounter() + 1);
        }
        profile.setUpdatedAt(OffsetDateTime.now());

        boolean newlyCollected = userArcanaRepository.findByUserTelegramIdAndArcanaId(telegramUser.id(), selectedCard.getArcana().getId()).isEmpty();
        if (newlyCollected) {
            userArcanaRepository.save(UserTarotArcana.builder()
                    .user(draw.getUser())
                    .arcana(selectedCard.getArcana())
                    .firstOpenedAt(OffsetDateTime.now())
                    .build());
        }

        TarotFeedEntry feedEntry = feedEntryRepository.save(TarotFeedEntry.builder()
                .user(draw.getUser())
                .reward(selectedCard.getReward())
                .arcana(selectedCard.getArcana())
                .rarity(selectedCard.getReward().getRarity())
                .createdAt(OffsetDateTime.now())
                .build());

        if (royal) {
            announceRoyal(feedEntry, telegramUser.displayName());
        }

        return new TarotDto.RevealResponse(drawDtoRevealed(draw), rewardDto(selectedCard.getReward()),
                arcanaDto(selectedCard.getArcana()), royal, newlyCollected, profileDto(profile, season));
    }

    @Transactional
    public TarotDto.ClaimResponse claim(String initData, Long drawId) {
        TelegramWebAppUser telegramUser = authService.authenticate(initData);
        TarotDraw draw = drawRepository.lockByIdAndUserTelegramId(drawId, telegramUser.id())
                .orElseThrow(() -> new ForcePlayException("Расклад не найден."));
        if (draw.getStatus() == TarotDrawStatus.OPEN) {
            throw new ForcePlayException("Сначала выберите карту.");
        }
        if (draw.getStatus() == TarotDrawStatus.CLAIMED) {
            return new TarotDto.ClaimResponse(draw.getId(), draw.getStatus().name());
        }
        draw.setStatus(TarotDrawStatus.CLAIMED);
        draw.setClaimedAt(OffsetDateTime.now());
        return new TarotDto.ClaimResponse(draw.getId(), draw.getStatus().name());
    }

    @Transactional(readOnly = true)
    public List<TarotDto.HistoryItemDto> history(String initData, TarotRarity rarity) {
        TelegramWebAppUser telegramUser = authService.authenticate(initData);
        return drawRepository.findHistory(telegramUser.id(), FINISHED_STATUSES, rarity, PageRequest.of(0, 50)).stream()
                .map(draw -> new TarotDto.HistoryItemDto(draw.getId(), draw.getCreatedAt(),
                        rewardDto(draw.getSelectedReward()), arcanaDto(draw.getSelectedArcana())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TarotDto.FeedItemDto> feed() {
        return feedEntryRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50)).stream()
                .map(entry -> new TarotDto.FeedItemDto(entry.getId(), username(entry.getUser()), entry.getCreatedAt(),
                        rewardDto(entry.getReward()), arcanaDto(entry.getArcana())))
                .toList();
    }

    @Transactional
    public TarotDto.InvoiceResponse invoice(String initData, String packageCode) {
        TelegramWebAppUser telegramUser = authService.authenticate(initData);
        TarotDto.PurchasePackageDto pack = purchasePackages().stream()
                .filter(item -> item.enabled() && item.code().equals(packageCode))
                .findFirst()
                .orElseThrow(() -> new ForcePlayException("Пакет гаданий не найден."));
        User user = userService.getOrCreateUser(telegramUser.id(), telegramUser.languageCode());
        TarotPurchase purchase = purchaseRepository.save(TarotPurchase.builder()
                .user(user)
                .packageCode(pack.code())
                .draws(pack.draws())
                .starsAmount(pack.starsPrice())
                .payload("tarot:" + telegramUser.id() + ":" + System.nanoTime())
                .status(TarotPurchaseStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build());
        purchase.setPayload("tarot:" + purchase.getId() + ":" + pack.code());
        try {
            String invoiceUrl = telegramClient.execute(CreateInvoiceLink.builder()
                    .title("Карты таро")
                    .description(pack.draws() + " дополнительных гаданий")
                    .payload(purchase.getPayload())
                    .providerToken("")
                    .currency("XTR")
                    .prices(List.of(new LabeledPrice(pack.draws() + " гаданий", pack.starsPrice())))
                    .build());
            return new TarotDto.InvoiceResponse(invoiceUrl);
        } catch (TelegramApiException exception) {
            purchase.setStatus(TarotPurchaseStatus.FAILED);
            throw new ForcePlayException("Не удалось создать счет Telegram Stars.");
        }
    }

    @Transactional
    public void completePurchase(String payload, String telegramPaymentChargeId, int totalAmount) {
        if (payload == null || !payload.startsWith("tarot:")) {
            return;
        }
        TarotPurchase purchase = purchaseRepository.findByPayload(payload)
                .orElseThrow(() -> new ForcePlayException("Покупка Таро не найдена."));
        if (purchase.getStatus() == TarotPurchaseStatus.PAID) {
            return;
        }
        if (purchase.getStarsAmount() != totalAmount) {
            purchase.setStatus(TarotPurchaseStatus.FAILED);
            throw new ForcePlayException("Сумма платежа Telegram Stars не совпадает.");
        }
        TarotUserProfile profile = lockedProfile(purchase.getUser());
        profile.setPurchasedDraws(profile.getPurchasedDraws() + purchase.getDraws());
        profile.setUpdatedAt(OffsetDateTime.now());
        purchase.setStatus(TarotPurchaseStatus.PAID);
        purchase.setTelegramPaymentChargeId(telegramPaymentChargeId);
        purchase.setPaidAt(OffsetDateTime.now());
    }

    @Transactional(readOnly = true)
    public boolean canAcceptPurchase(String payload) {
        return payload != null && payload.startsWith("tarot:")
                && purchaseRepository.findByPayload(payload)
                .filter(purchase -> purchase.getStatus() == TarotPurchaseStatus.PENDING)
                .isPresent();
    }

    @Transactional(readOnly = true)
    public TarotDto.AdminStatsResponse adminStats(String initData) {
        TelegramWebAppUser telegramUser = authService.authenticate(initData);
        if (!adminService.isAdmin(telegramUser.id())) {
            throw new ForcePlayException("Недостаточно прав.");
        }
        long paid = purchaseRepository.countByStatus(TarotPurchaseStatus.PAID);
        Long revenue = jdbcTemplate.queryForObject(
                "select coalesce(sum(stars_amount), 0) from tarot_purchases where status = 'PAID'",
                Long.class
        );
        return new TarotDto.AdminStatsResponse(
                drawRepository.countByStatusIn(FINISHED_STATUSES),
                drawRepository.countBySelectedRewardRarityAndStatusIn(TarotRarity.COMMON, FINISHED_STATUSES),
                drawRepository.countBySelectedRewardRarityAndStatusIn(TarotRarity.RARE, FINISHED_STATUSES),
                drawRepository.countBySelectedRewardRarityAndStatusIn(TarotRarity.ROYAL, FINISHED_STATUSES),
                paid,
                revenue == null ? 0 : revenue
        );
    }

    private TarotUserProfile lockedProfile(User user) {
        return profileRepository.lockByUserTelegramId(user.getTelegramId())
                .orElseGet(() -> profileRepository.save(TarotUserProfile.builder()
                        .user(user)
                        .freeDrawsUsed(0)
                        .purchasedDraws(0)
                        .pityCounter(0)
                        .createdAt(OffsetDateTime.now())
                        .updatedAt(OffsetDateTime.now())
                        .build()));
    }

    private TarotUserProfile buildTransientProfile(User user) {
        return TarotUserProfile.builder()
                .user(user)
                .freeDrawsUsed(0)
                .purchasedDraws(0)
                .pityCounter(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    private void consumeDraw(TarotUserProfile profile) {
        int freeLimit = freeDrawLimit(profile);
        if (profile.getFreeDrawsUsed() < freeLimit) {
            profile.setFreeDrawsUsed(profile.getFreeDrawsUsed() + 1);
            return;
        }
        if (profile.getPurchasedDraws() > 0) {
            profile.setPurchasedDraws(profile.getPurchasedDraws() - 1);
            return;
        }
        throw new ForcePlayException("Нет доступных гаданий.");
    }

    private void resetFreeDrawsIfNeeded(TarotUserProfile profile) {
        LocalDate today = LocalDate.now(DAILY_ZONE);
        if (!today.equals(profile.getFreeDrawsUsedDate())) {
            profile.setFreeDrawsUsedDate(today);
            profile.setFreeDrawsUsed(0);
        }
    }

    private int freeDrawLimit(TarotUserProfile profile) {
        boolean vip = profile.getVipUntil() != null && profile.getVipUntil().isAfter(OffsetDateTime.now());
        return vip ? intSetting("free_draws_vip", 2) : intSetting("free_draws_regular", 1);
    }

    private TarotSeason activeSeason() {
        return seasonRepository.findFirstByActiveTrueOrderByStartDateDesc()
                .orElseThrow(() -> new ForcePlayException("Активный сезон Таро не настроен."));
    }

    private TarotDeck activeDeck(TarotSeason season) {
        return deckRepository.findFirstBySeasonAndActiveTrueOrderByIdAsc(season)
                .orElseThrow(() -> new ForcePlayException("Активная колода Таро не настроена."));
    }

    private TarotReward weightedReward(TarotSeason season, List<TarotRarity> rarities) {
        List<TarotReward> rewards = rewardRepository.findAllBySeasonAndEnabledTrueAndRarityIn(season, rarities).stream()
                .filter(reward -> reward.getWeight() > 0)
                .toList();
        if (rewards.isEmpty()) {
            throw new ForcePlayException("Пул наград Таро пуст.");
        }
        int total = rewards.stream().mapToInt(TarotReward::getWeight).sum();
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (TarotReward reward : rewards) {
            roll -= reward.getWeight();
            if (roll < 0) {
                return reward;
            }
        }
        return rewards.getLast();
    }

    private TarotArcana randomArcana(TarotSeason season) {
        List<TarotArcana> arcana = arcanaRepository.findAllBySeasonOrderByIdAsc(season);
        if (arcana.isEmpty()) {
            throw new ForcePlayException("Арканы Таро не настроены.");
        }
        return arcana.get(ThreadLocalRandom.current().nextInt(arcana.size()));
    }

    private TarotDto.ProfileDto profileDto(TarotUserProfile profile, TarotSeason season) {
        resetFreeDrawsIfNeeded(profile);
        int freeLimit = freeDrawLimit(profile);
        int available = Math.max(0, freeLimit - profile.getFreeDrawsUsed()) + profile.getPurchasedDraws();
        boolean vip = profile.getVipUntil() != null && profile.getVipUntil().isAfter(OffsetDateTime.now());
        return new TarotDto.ProfileDto(
                available,
                freeLimit,
                profile.getFreeDrawsUsed(),
                profile.getPurchasedDraws(),
                profile.getPityCounter(),
                intSetting("pity_threshold", 50),
                userArcanaRepository.countByUserTelegramId(profile.getUser().getTelegramId()),
                arcanaRepository.countBySeason(season),
                vip,
                profile.getVipUntil()
        );
    }

    private TarotDto.DrawDto drawDtoHidden(TarotDraw draw) {
        List<TarotDto.DrawCardDto> cards = drawCardRepository.findAllByDrawIdOrderByCardIndexAsc(draw.getId()).stream()
                .map(card -> new TarotDto.DrawCardDto(card.getCardIndex(), false, false, null, null))
                .toList();
        return new TarotDto.DrawDto(draw.getId(), draw.getStatus().name(), draw.isGuaranteedRoyal(), cards);
    }

    private TarotDto.DrawDto drawDtoRevealed(TarotDraw draw) {
        List<TarotDto.DrawCardDto> cards = drawCardRepository.findAllByDrawIdOrderByCardIndexAsc(draw.getId()).stream()
                .map(card -> new TarotDto.DrawCardDto(card.getCardIndex(), card.isSelected(), card.isSelected(),
                        card.isSelected() ? rewardDto(card.getReward()) : null,
                        card.isSelected() ? arcanaDto(card.getArcana()) : null))
                .toList();
        return new TarotDto.DrawDto(draw.getId(), draw.getStatus().name(), draw.isGuaranteedRoyal(), cards);
    }

    private TarotDto.RewardDto rewardDto(TarotReward reward) {
        if (reward == null) {
            return null;
        }
        return new TarotDto.RewardDto(reward.getId(), reward.getName(), reward.getDescription(), reward.getIcon(), reward.getRarity());
    }

    private TarotDto.ArcanaDto arcanaDto(TarotArcana arcana) {
        if (arcana == null) {
            return null;
        }
        return new TarotDto.ArcanaDto(arcana.getId(), arcana.getName(), arcana.getImage(), arcana.getDescription(), arcana.getRarity());
    }

    private TarotDto.AssetConfig assetConfig() {
        List<String> cardBacks = List.of(
                setting("card_back_1", "/tarot/assets/card_back_1.jpg"),
                setting("card_back_2", "/tarot/assets/card_back_1.jpg")
        );
        return new TarotDto.AssetConfig(setting("background", "/tarot/assets/astaria_background.jpg"), cardBacks);
    }

    private List<TarotDto.PurchasePackageDto> purchasePackages() {
        return jdbcTemplate.query("""
                        select code, draws, stars_price, enabled
                        from tarot_purchase_packages
                        order by draws
                        """,
                (rs, rowNum) -> new TarotDto.PurchasePackageDto(
                        rs.getString("code"),
                        rs.getInt("draws"),
                        rs.getInt("stars_price"),
                        rs.getBoolean("enabled")
                ));
    }

    private int intSetting(String key, int fallback) {
        try {
            return Integer.parseInt(setting(key, String.valueOf(fallback)));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private String setting(String key, String fallback) {
        List<String> values = jdbcTemplate.queryForList("select value from tarot_settings where key = ?", String.class, key);
        return values.isEmpty() ? fallback : values.getFirst();
    }

    private String username(User user) {
        return "ID " + user.getTelegramId();
    }

    private void announceRoyal(TarotFeedEntry entry, String username) {
        String text = """
                👑 Судьба улыбнулась герою!

                %s

                открыл Карту Короля Судьбы

                Награда:
                %s
                """.formatted(username, entry.getReward().getName());
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("🔮 Открыть Таро")
                        .webApp(WebAppInfo.builder().url(botPropertiesAccessor.tarotWebappUrl()).build())
                        .build()
        )));
        adminService.getBroadcastTargets().stream()
                .sorted(Comparator.naturalOrder())
                .forEach(chatId -> {
                    try {
                        telegramGateway.sendText(chatId, text, markup);
                    } catch (Exception exception) {
                        log.warn("Failed to send tarot royal announcement to {}", chatId, exception);
                    }
                });
    }
}
