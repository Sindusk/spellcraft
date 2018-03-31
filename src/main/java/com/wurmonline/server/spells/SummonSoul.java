package com.wurmonline.server.spells;

import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.SummonSoulQuestion;
import com.wurmonline.server.skills.Skill;

public class SummonSoul extends ReligiousSpell {

	public SummonSoul(int casttime, int cost, int difficulty, int faith, long cooldown){
		super("Summon Soul", ModActions.getNextActionId(), casttime, cost, difficulty, faith, cooldown);
		this.targetTile = true;
		this.targetCreature = true;
		this.targetItem = true;
		this.description = "summons a player to your location";

        ActionEntry actionEntry = ActionEntry.createEntry((short) number, name, "enchanting",
                new int[] { 2 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */,
                        48 /* ACTION_TYPE_ENEMY_ALWAYS */ });
        ModActions.registerAction(actionEntry);
	}

	public static boolean mayCastSummonSoul(Creature performer){
		if(Servers.localServer.PVPSERVER && performer.getEnemyPresense() > 0){
			performer.getCommunicator().sendNormalServerMessage("Enemies are nearby, you cannot cast Summon Soul right now.");
			return false;
		}
		return true;
	}
	
	@Override
    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
        return SummonSoul.mayCastSummonSoul(performer);
    }

	@Override
    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer, int heightOffset, Tiles.TileBorderDirection dir) {
        return SummonSoul.mayCastSummonSoul(performer);
    }

	@Override
    boolean precondition(Skill castSkill, Creature performer, Creature target) {
        return SummonSoul.mayCastSummonSoul(performer);
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Item target) {
        return SummonSoul.mayCastSummonSoul(performer);
    }
	
	@Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        SummonSoulQuestion ssq = new SummonSoulQuestion(performer, "Summon Soul", "Which soul do you wish to summon?", performer.getWurmId(), power);
        ssq.sendQuestion();
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
    	SummonSoulQuestion ssq = new SummonSoulQuestion(performer, "Summon Soul", "Which soul do you wish to summon?", performer.getWurmId(), power);
        ssq.sendQuestion();
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
    	SummonSoulQuestion ssq = new SummonSoulQuestion(performer, "Summon Soul", "Which soul do you wish to summon?", performer.getWurmId(), power);
        ssq.sendQuestion();
    }
}
