package com.wurmonline.server.spells;


import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.skills.Skill;
import mod.sin.spellcraft.SpellcraftSpellEffects;
import mod.sin.spellcraft.spellchecks.EnchantMessageUtil;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class LabouringSpirit extends ItemEnchantment {
    public LabouringSpirit(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetItem = true;
        this.enchantment = spell.getEnchant();
        this.effectdesc = "seems to have some spirits bound to it.";
        this.description = "has spirits bound to it, performing mundane tasks.";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "enchanting",
                new int[] { 2 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */ });
        ModActions.registerAction(actionEntry);
    }

    public static boolean isValidTarget(Item target) {
        return target.isUnenchantedTurret();
    }

    @Override
    public boolean precondition(final Skill castSkill, final Creature performer, final Item target) {
        if (!isValidTarget(target)) {
            performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.");
            return false;
        }
        SpellEffect negatingEffect = SpellcraftSpellEffects.hasNegatingEffect(target, SpellcraftSpell.LABOURING_SPIRIT.getEnchant());
        if(negatingEffect != null){
            EnchantMessageUtil.sendNegatingEffectMessage(name, performer, target, negatingEffect);
            return false;
        }
        return true;
    }

    @Override
    void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
        // Do nothing to prevent destruction of the item.
    }

    public static float getSpellEffect(Item target) {
        if (!isValidTarget(target))
            return 0;

        ItemSpellEffects effs = target.getSpellEffects();
        if (effs == null)
            return 0;

        SpellEffect eff = effs.getSpellEffect(SpellcraftSpell.LABOURING_SPIRIT.getEnchant());
        if (eff == null)
            return 0;

        return eff.getPower();
    }
}
