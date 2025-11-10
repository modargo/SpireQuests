package spireQuests.quests.example;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.relics.BurningBlood;
import spireQuests.patches.QuestTriggers;
import spireQuests.quests.AbstractQuest;
import spireQuests.quests.QuestReward;

import static spireQuests.Anniv8Mod.makeID;

public class TestQuest extends AbstractQuest {
    public TestQuest() {
        super(QuestType.LONG, QuestDifficulty.HARD);

        new TriggerTracker<>(QuestTriggers.ADD_CARD, 5)
            .triggerCondition((card)->card.rarity == AbstractCard.CardRarity.COMMON)
            .setResetTrigger(QuestTriggers.ADD_CARD, (card)->card.rarity != AbstractCard.CardRarity.COMMON)
            .add(this);

        new TriggerTracker<AbstractCard>(QuestTriggers.ADD_CARD, 1)
            {
                @Override
                public String progressString() {
                    return "";
                }
            }
            .triggerCondition((card)->card.rarity == AbstractCard.CardRarity.RARE)
            .add(this);

        new TriggerEvent<>(QuestTriggers.DECK_CHANGE,
                (param)->AbstractDungeon.player.damage(new DamageInfo(null, 1, DamageInfo.DamageType.HP_LOSS))
        );

        addReward(new QuestReward.GoldReward(100));
        addReward(new QuestReward.RelicReward(new BurningBlood()));
    }
}
