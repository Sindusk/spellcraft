package mod.sin.spellcraft.spellchecks;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.spells.SpellEffect;

public class EnchantMessageUtil {
	public static void sendCannotBeEnchantedMessage(Creature performer, Item target){
		performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.");
	}
	public static void sendNegatingEffectMessage(String name, Creature performer, Item target, SpellEffect negatingEffect){
		performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with " + negatingEffect.getName() +" which would negate the effect of "+name+".");
	}
}
