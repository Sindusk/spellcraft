package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.skills.Skill;
import mod.sin.spellcraft.SpellcraftEnchants;
import mod.sin.spellcraft.spellchecks.EnchantMessageUtil;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

public class Quarry extends ReligiousSpell {

	public Quarry(int casttime, int cost, int difficulty, int faith, long cooldown){
		super("Quarry", ModActions.getNextActionId(), casttime, cost, difficulty, faith, cooldown);
		this.targetItem = true;
		this.enchantment = SpellcraftEnchants.QUARRY;
		this.effectdesc = "will mine surfaces more effectively.";
		this.description = "mines surfaces more effectively";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "enchanting",
                new int[] { 2 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */ });
        ModActions.registerAction(actionEntry);
	}

    @Override
    boolean precondition(Skill castSkill, Creature performer, Item target) {
        if(!Quarry.mayBeEnchanted(target)){
			EnchantMessageUtil.sendCannotBeEnchantedMessage(performer, target);
        	return false;
        }else if(target.getTemplateId() != ItemList.pickAxe){
            performer.getCommunicator().sendNormalServerMessage("This spell can only be cast on a pick axe.");
            return false;
        }
        return true;
    }
	
	@Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
        if (!Quarry.mayBeEnchanted(target) || target.getTemplateId() != ItemList.pickAxe) {
            performer.getCommunicator().sendNormalServerMessage("The spell fizzles.", (byte) 3);
            return;
        }
        ItemSpellEffects effs = target.getSpellEffects();
        if (effs == null) {
            effs = new ItemSpellEffects(target.getWurmId());
        }
        SpellEffect eff = effs.getSpellEffect(this.enchantment);
        if (eff == null) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " will now mine surfaces more effectively.", (byte) 2);
            eff = new SpellEffect(target.getWurmId(), this.enchantment, (float)power, 20000000);
            effs.addSpellEffect(eff);
            Server.getInstance().broadCastAction(performer.getNameWithGenus() + " looks pleased.", performer, 5);
        } else if ((double)eff.getPower() > power) {
            performer.getCommunicator().sendNormalServerMessage("You frown as you fail to improve the power.", (byte) 3);
            Server.getInstance().broadCastAction(performer.getNameWithGenus() + " frowns.", performer, 5);
        } else {
            performer.getCommunicator().sendNormalServerMessage("You succeed in improving the power of the " + this.name + ".", (byte) 2);
            eff.improvePower((float)power);
            Server.getInstance().broadCastAction(performer.getNameWithGenus() + " looks pleased.", performer, 5);
        }
    }

    @Override
    void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
        this.checkDestroyItem(power, performer, target);
    }
}
