package spireQuests.quests.modargo.relics;

import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import spireQuests.abstracts.AbstractSQRelic;

import static spireQuests.Anniv8Mod.makeID;

public class TheLivingCurse extends AbstractSQRelic {
    public static final String ID = makeID(TheLivingCurse.class.getSimpleName());

    public TheLivingCurse() {
        super(ID, "modargo", RelicTier.SPECIAL, LandingSound.MAGICAL);
    }

    @Override
    public void onCardDraw(AbstractCard card) {
        if (card.color == AbstractCard.CardColor.CURSE) {
            this.addToBot(new DrawCardAction(1));
        }
    }

    public static boolean hasRelic() {
        return CardCrawlGame.isInARun() && AbstractDungeon.player.hasRelic(ID);
    }
}
