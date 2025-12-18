package spireQuests.quests.modargo;

import basemod.helpers.CardPowerTip;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.curses.*;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import spireQuests.patches.QuestTriggers;
import spireQuests.quests.AbstractQuest;
import spireQuests.quests.QuestReward;
import spireQuests.quests.modargo.cards.embracethecurse.Curseblast;
import spireQuests.quests.modargo.cards.embracethecurse.NetherStrike;
import spireQuests.quests.modargo.cards.embracethecurse.SkullGaze;
import spireQuests.quests.modargo.cards.embracethecurse.WickedExchange;
import spireQuests.quests.modargo.relics.TheLivingCurse;
import spireQuests.quests.pandemonium.PristinePatch;

import java.util.*;
import java.util.stream.Collectors;

public class EmbraceTheCurseQuest extends AbstractQuest {
    public EmbraceTheCurseQuest() {
        super(QuestType.LONG, QuestDifficulty.CHALLENGE);

        // We wrap everything up in our own custom methods and classes in order to do things that the framework didn't
        // fully anticipate (specifically, to have a quest with rewards that you get at each milestone).
        // Secondarily, this structure makes adjusting the milestones easy to do with minimal code changes.
        this.CreateCurseTracker(1, new NetherStrike(), 1);
        this.CreateCurseTracker(3, new WickedExchange(), 1);
        this.CreateCurseTracker(4, new Curseblast(), 1);
        this.CreateCurseTracker(5, new SkullGaze(), 1);
        this.CreateCurseTracker(7, new WickedExchange(), 1);
        this.CreateCurseTracker(8, new Curseblast(), 1);

        isAutoComplete = true;
        titleScale = 0.9f;
    }

    @Override
    public String getDescription() {
        String milestones = this.trackers.stream().filter(t -> t instanceof AddCurseTracker).map(t -> ((AddCurseTracker)t).getTarget() + "").collect(Collectors.joining("/"));
        return String.format(super.getDescription(), milestones);
    }

    @Override
    protected void assignTrackerText(Tracker questTracker) {
        questTracker.text = questStrings.TRACKER_TEXT[0];
    }

    @Override
    public void onStart() {
        super.onStart();
        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(Settings.WIDTH / 2.0f, Settings.HEIGHT / 2.0f, new TheLivingCurse());
    }

    @Override
    public void onComplete() {
        super.onComplete();
        // The rewards are given out after each milestone, and should be all gone by this point, but just to be sure, clear them here
        questRewards.clear();
    }

    @Override
    public void makeTooltips(List<PowerTip> tipList) {
        super.makeTooltips(tipList);
        HashSet<String> seenCardIDs = new HashSet<>();
        // Remove any duplicates
        for (Iterator<PowerTip> iterator = tipList.iterator(); iterator.hasNext(); ) {
            PowerTip tip = iterator.next();
            if (!(tip instanceof CardPowerTip)) {
                continue;
            }
            String cardID = ((CardPowerTip)tip).card.cardID;
            if (seenCardIDs.contains(cardID)) {
                iterator.remove();
            }
            else {
                seenCardIDs.add(cardID);
            }
        }
        AbstractRelic relic = new TheLivingCurse();
        tipList.add(0, relic.tips.get(0));
    }

    private void CreateCurseTracker(int count, AbstractCard card, int amount) {
        new AddCurseTracker(count, this, card, amount).add(this);
        this.addReward(new QuestReward.CardReward(card, amount));
    }

    @Override
    public void loadSave(String[] questData, QuestReward.QuestRewardSave[] questRewardSaves) {
        super.loadSave(questData, questRewardSaves);
        for (Tracker tracker : this.trackers) {
            if (tracker.isComplete() && tracker instanceof AddCurseTracker) {
                ((AddCurseTracker)tracker).cleanup();
            }
        }
    }

    private static class CurseCardReward extends QuestReward.CardReward {
        public final int count;

        public CurseCardReward(AbstractCard card, int amount, int count) {
            super(card, amount);
            this.count = count;
        }
    }

    private static class AddCurseTracker extends TriggerTracker<AbstractCard> {
        private final AbstractQuest quest;
        private final AbstractCard card;
        private final int amount;

        public AddCurseTracker(int count, AbstractQuest quest, AbstractCard card, int amount) {
            super(QuestTriggers.ADD_CARD, count);
            this.quest = quest;
            this.card = card;
            this.amount = amount;
            this.triggerCondition(c -> c.color == AbstractCard.CardColor.CURSE);
        }

        @Override
        public void trigger(AbstractCard param) {
            boolean wasComplete = this.isComplete();
            super.trigger(param);
            if (!wasComplete && this.isComplete()) {
                float spacing = 150.0F;
                int half = this.amount / 2;
                float startX = Settings.WIDTH / 2.0F - spacing * half;
                for (int i = 0; i < this.amount; i++) {
                    AbstractDungeon.topLevelEffectsQueue.add(new ShowCardAndObtainEffect(card, startX + (i - 1) * spacing, Settings.HEIGHT / 2.0f));
                }
                this.cleanup();
            }
        }

        public int getTarget() {
            return this.targetCount;
        }

        public void cleanup() {
            this.quest.questRewards.removeIf(r -> r instanceof CurseCardReward && ((CurseCardReward)r).count == this.targetCount);
            this.hide();
        }
    }

    private static AbstractCard getCurse(AbstractCard.CardRarity rarity) {
        // We only generate the normal base game curses because we don't want to mix in potentially weird modded curses
        List<AbstractCard> mostlyHarmlessCurses = Arrays.asList(new Clumsy(), new Injury(), new Parasite(), new Writhe());
        List<AbstractCard> harmfulCurses = Arrays.asList(new Decay(), new Doubt(), new Normality(), new Pain(), new Regret(), new Shame());
        List<AbstractCard> options = new ArrayList<>();
        switch (rarity) {
            case RARE:
                options.addAll(mostlyHarmlessCurses);
                break;
            case UNCOMMON:
                options.addAll(mostlyHarmlessCurses);
                if (AbstractDungeon.miscRng.randomBoolean()) {
                    options.addAll(harmfulCurses);
                }
                break;
            default:
                options.addAll(mostlyHarmlessCurses);
                options.addAll(harmfulCurses);
                break;
        }
        return options.get(AbstractDungeon.miscRng.random(options.size() - 1));
    }

    // We piggyback on PristinePatch to ensure this patch happens first, so we don't overwrite pristine cards
    @SpirePatch(clz = PristinePatch.class, method = "addPristineModifier")
    public static class AddCursesToCardRewardsPatch {
        @SpirePrefixPatch
        public static ArrayList<AbstractCard> addCurses(ArrayList<AbstractCard> __result) {
            if (TheLivingCurse.hasRelic()) {
                // This is effectively "1 card in 8 is replaced by a curse, but never multiple in one reward, and don't replace rares"
                float chance = 1 - (float)Math.pow(0.875, __result.size());
                boolean addCurse = AbstractDungeon.miscRng.randomBoolean(chance);
                if (addCurse) {
                    int i = AbstractDungeon.miscRng.random(__result.size() - 1);
                    __result.set(i, getCurse(__result.get(i).rarity));
                }
            }
            return __result;
        }
    }
}
