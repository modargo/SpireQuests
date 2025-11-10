package spireQuests.quests;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import spireQuests.Anniv8Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static spireQuests.Anniv8Mod.makeID;

public abstract class QuestReward {
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString(makeID("QuestReward")).TEXT;
    private static final Map<String, RewardLoader> rewardLoaders = new HashMap<>();
    static {
        addRewardSaver(new RewardLoader(GoldReward.class, (save) -> new GoldReward(Integer.parseInt(save))));
    }

    private static void addRewardSaver(RewardLoader loader) {
        rewardLoaders.put(loader.key, loader);
    }

    public static QuestReward fromSave(QuestRewardSave save) {
        RewardLoader loader = rewardLoaders.get(save.type);
        if (loader == null) {
            Anniv8Mod.logger.error("Unable to load saved reward of type " + save.type);
            return null;
        }
        return loader.loader.apply(save.param);
    }



    public String rewardText;

    public QuestReward(String rewardText) {
        this.rewardText = rewardText;
    }

    public QuestRewardSave getSave() {
        return new QuestRewardSave(getClass().getSimpleName(), saveParam());
    }

    public abstract TextureRegion icon();
    protected abstract String saveParam();

    @Override
    public String toString() {
        return rewardText;
    }


    public static class GoldReward extends QuestReward {
        private static final TextureRegion img = new TextureRegion(ImageMaster.UI_GOLD, 8, 0, 48, 48);
        private final int amount;

        public GoldReward(int amount) {
            super(String.format(TEXT[0], amount));
            this.amount = amount;
        }

        @Override
        public TextureRegion icon() {
            return img;
        }

        @Override
        public String saveParam() {
            return String.valueOf(amount);
        }
    }

    public static class RelicReward extends QuestReward {
        private final AbstractRelic relic;
        private final TextureRegion img;

        public RelicReward(AbstractRelic r) {
            super(String.format(TEXT[1], r.name));
            this.relic = r;
            this.img = new TextureRegion(this.relic.img, 28, 28, 72, 72);
        }

        @Override
        public TextureRegion icon() {
            return img;
        }

        @Override
        protected String saveParam() {
            return relic.relicId;
        }
    }

    public static class RandomRelicReward extends QuestReward {
        private final AbstractRelic.RelicTier tier;

        public RandomRelicReward(AbstractRelic.RelicTier tier) {
            super(text(tier));
            this.tier = tier;
        }

        @Override
        public TextureRegion icon() {
            return ImageMaster.COPPER_COIN_1;
        }

        @Override
        protected String saveParam() {
            return tier.name();
        }

        private static String text(AbstractRelic.RelicTier tier) {
            String relicTier;
            switch (tier) {
                case COMMON:
                    relicTier = TEXT[2];
                    break;
                case UNCOMMON:
                    relicTier = TEXT[3];
                    break;
                default:
                    relicTier = TEXT[4];
                    break;
            }
            return String.format(TEXT[1], relicTier);
        }
    }

    private static class RewardLoader {
        public final String key;
        public final Function<String, ? extends QuestReward> loader;

        public <T extends QuestReward> RewardLoader(Class<T> type, Function<String, T> loader) {
            this.key = type.getSimpleName();
            this.loader = loader;
        }
    }

    public static class QuestRewardSave {
        public String type;
        public String param;

        public QuestRewardSave() {

        }

        public QuestRewardSave(String type, String param) {
            this.type = type;
            this.param = param;
        }
    }
}
