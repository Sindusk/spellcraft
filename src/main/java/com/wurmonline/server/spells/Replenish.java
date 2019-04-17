package com.wurmonline.server.spells;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import mod.sin.spellcraft.spellchecks.EnchantMessageUtil;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class Replenish extends ItemEnchantment {
    public Replenish(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetItem = true;
        this.enchantment = spell.getEnchant();
        this.effectdesc = "will magically fill with water.";
        this.description = "fills itself with water";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "enchanting",
                new int[] { 2 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */ });
        ModActions.registerAction(actionEntry);
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Item target) {
        if(!target.isContainerLiquid()){
            performer.getCommunicator().sendNormalServerMessage("That container cannot hold liquid.");
            return false;
        }
        SpellEffect negatingEffect = EnchantUtil.hasNegatingEffect(target, SpellcraftSpell.REPLENISH.getEnchant());
        if(negatingEffect != null){
            EnchantUtil.sendNegatingEffectMessage(name, performer, target, negatingEffect);
            return false;
        }
        return true;
    }

    @Override
    void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
        // Do nothing to prevent destruction of the item.
    }
}
