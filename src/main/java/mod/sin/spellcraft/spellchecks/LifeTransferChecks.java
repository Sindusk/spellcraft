package mod.sin.spellcraft.spellchecks;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.spells.LifeTransfer;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.shared.constants.Enchants;

import mod.sin.spellcraft.SpellcraftSpellEffects;

public class LifeTransferChecks {
	public static boolean precondition(String name, Creature performer, Item target){
		if(!LifeTransfer.mayBeEnchanted(target)){
			EnchantMessageUtil.sendCannotBeEnchantedMessage(performer, target);
        	return false;
		}
		SpellEffect negatingEffect = SpellcraftSpellEffects.hasNegatingEffect(target, Enchants.BUFF_LIFETRANSFER);
		if(negatingEffect != null){
			EnchantMessageUtil.sendNegatingEffectMessage(name, performer, target, negatingEffect);
        	return false;
        }
		return true;
	}
}
