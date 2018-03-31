package mod.sin.spellcraft.spellchecks;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.spells.BlessingDark;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.shared.constants.Enchants;

import mod.sin.spellcraft.SpellcraftSpellEffects;

public class BlessingDarkChecks {
	public static boolean precondition(String name, Creature performer, Item target){
		if(!BlessingDark.mayBeEnchanted(target)){
			EnchantMessageUtil.sendCannotBeEnchantedMessage(performer, target);
        	return false;
		}
		SpellEffect negatingEffect = SpellcraftSpellEffects.hasNegatingEffect(target, Enchants.BUFF_BLESSINGDARK);
		if(negatingEffect != null){
			EnchantMessageUtil.sendNegatingEffectMessage(name, performer, target, negatingEffect);
        	return false;
        }
		return true;
	}
}
