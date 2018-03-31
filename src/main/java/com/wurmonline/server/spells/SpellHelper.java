package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SpellHelper
{
	public static Logger logger = Logger.getLogger(SpellHelper.class.getName());
    public static float statuetteRarityPowerIncrease = 0.f;
    public static float statuetteQualityBonusMod = 0.f;

    private static final Map<Creature, Item> statuettes = new ConcurrentHashMap<Creature, Item>();

    public static float getStatuettePowerIncrement(final Creature performer) {
        Item statuette = statuettes.get(performer);
        if (statuette == null){
        	return 0;
        }

        return statuette.getRarity() * statuetteRarityPowerIncrease;
    }

    public static float getStatuetteBonus(final Creature performer) {
        Item statuette = statuettes.get(performer);
        if (statuette == null){
        	return 0;
        }

        return statuette.getCurrentQualityLevel() * statuetteQualityBonusMod;
    }



    public static boolean castSpell(final Creature performer, final Spell spell, final Item item, final float counter, Item source) {
    	statuettes.put(performer, spell.religious ? source : null);
        boolean result = spell.run(performer, item, counter);
        statuettes.remove(performer);
        return result;
    }

    public static boolean castSpell(final Creature performer, final Spell spell, final Creature target, final float counter, Item source) {
        statuettes.put(performer, spell.religious ? source : null);
        boolean result = spell.run(performer, target, counter);
        statuettes.remove(performer);
        return result;
    }

    public static boolean castSpell(final Creature performer, final Spell spell, final int tilex, final int tiley, final int layer, final int heightOffset, final Tiles.TileBorderDirection dir, final float counter, Item source) {
    	statuettes.put(performer, spell.religious ? source : null);
        boolean result = spell.run(performer, tilex, tiley, layer, heightOffset, dir, counter);
        statuettes.remove(performer);
        return result;
    }

    public static boolean castSpell(final Creature performer, final Spell spell, final Wound target, final float counter, Item source) {
    	statuettes.put(performer, spell.religious ? source : null);
        boolean result = spell.run(performer, target, counter);
        statuettes.remove(performer);
        return result;
    }

    public static boolean castSpell(final Creature performer, final Spell spell, final int tilex, final int tiley, final int layer, final int heightOffset, final float counter, Item source) {
    	statuettes.put(performer, spell.religious ? source : null);
        boolean result = spell.run(performer, tilex, tiley, layer, heightOffset, counter);
        statuettes.remove(performer);
        return result;
    }

}
