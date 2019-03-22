package com.wurmonline.server.spells;

import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import mod.sin.spellcraft.SpellcraftSpellEffects;
import mod.sin.spellcraft.spellchecks.EnchantMessageUtil;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class Efficiency extends ItemEnchantment {

	public Efficiency(int casttime, int cost, int difficulty, int faith, long cooldown){
		super("Efficiency", ModActions.getNextActionId(), casttime, cost, difficulty, faith, cooldown);
		this.targetItem = true;
		this.enchantment = SpellcraftSpell.EFFICIENCY.getEnchant();
		this.effectdesc = "will be more effective when used.";
		this.description = "is more effective";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "enchanting",
                new int[] { 2 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */ });
        ModActions.registerAction(actionEntry);
	}
    public Efficiency(SpellcraftSpell spell){
        super(spell.getName(), ModActions.getNextActionId(), spell.getCastTime(), spell.getCost(), spell.getDifficulty(), spell.getFaith(), spell.getCooldown());
        this.targetItem = true;
        this.enchantment = spell.getEnchant();
        this.effectdesc = "will be more effective when used.";
        this.description = "is more effective";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "enchanting",
                new int[] { 2 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */ });
        ModActions.registerAction(actionEntry);
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Item target) {
        if(!Efficiency.mayBeEnchanted(target)){
			EnchantMessageUtil.sendCannotBeEnchantedMessage(performer, target);
        	return false;
        }
        SpellEffect negatingEffect = SpellcraftSpellEffects.hasNegatingEffect(target, SpellcraftSpell.EFFICIENCY.getEnchant());
        if(negatingEffect != null){
            EnchantMessageUtil.sendNegatingEffectMessage(name, performer, target, negatingEffect);
            return false;
        }
        return true;
    }
}
