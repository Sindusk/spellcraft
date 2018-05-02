package mod.sin.spellcraft;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.shared.constants.Enchants;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import mod.sin.lib.Util;

public class SpellcraftSpellEffects {
	protected static Logger logger = Logger.getLogger(SpellcraftSpellEffects.class.getName());
	
	protected static ArrayList<Byte> speedEffectGroup = new ArrayList<Byte>();
	protected static ArrayList<Byte> skillgainEffectGroup = new ArrayList<Byte>();
	protected static ArrayList<Byte> weaponDamageEffectGroup = new ArrayList<Byte>();
	protected static ArrayList<Byte> armourEffectGroup = new ArrayList<Byte>();

	protected static ArrayList<ArrayList<Byte>> enchantGroups = new ArrayList<>();
	
	protected static boolean improvedEnchantGrouping = true;
	
	public static SpellEffect hasNegatingEffect(Item target, byte enchantment){
		if(!improvedEnchantGrouping){
			return null;
		}
		if(target.getSpellEffects() != null){
		    for(ArrayList<Byte> group : enchantGroups){
		        if(group.contains(enchantment)){
                    for(byte ench : group){
                        if(ench == enchantment){ continue; }
                        if(target.getBonusForSpellEffect(ench) > 0.0f){
                            return target.getSpellEffect(ench);
                        }
                    }
                }
            }
			if(speedEffectGroup.contains(enchantment)){
				for(byte ench : speedEffectGroup){
					if(ench == enchantment){ continue; }
					if(target.getBonusForSpellEffect(ench) > 0.0f){
						return target.getSpellEffect(ench);
					}
				}
			}
			if(skillgainEffectGroup.contains(enchantment)){
				for(byte ench : skillgainEffectGroup){
					if(ench == enchantment){ continue; }
					if(target.getBonusForSpellEffect(ench) > 0.0f){
						return target.getSpellEffect(ench);
					}
				}
			}
			if(weaponDamageEffectGroup.contains(enchantment)){
				for(byte ench : weaponDamageEffectGroup){
					if(ench == enchantment){ continue; }
					if(target.getBonusForSpellEffect(ench) > 0.0f){
						return target.getSpellEffect(ench);
					}
				}
			}
			if(armourEffectGroup.contains(enchantment)){
				for(byte ench : armourEffectGroup){
					if(ench == enchantment){ continue; }
					if(target.getBonusForSpellEffect(ench) > 0.0f){
						return target.getSpellEffect(ench);
					}
				}
			}
		}
		return null;
	}
	
	public static void preInit(SpellcraftMod mod){
		SpellcraftSpellEffects.improvedEnchantGrouping = mod.improvedEnchantGrouping;
		setEnchantGroups();
		try {
	    	ClassPool classPool = HookManager.getInstance().getClassPool();
	    	Class<SpellcraftSpellEffects> thisClass = SpellcraftSpellEffects.class;
	    	
	    	if(mod.improvedEnchantGrouping){
	    		CtClass[] preconditionParams = {
	    				classPool.get("com.wurmonline.server.skills.Skill"),
		        		classPool.get("com.wurmonline.server.creatures.Creature"),
		        		classPool.get("com.wurmonline.server.items.Item")
		        };
	    		
	    		// -- Tools/Speed/Skillgain Group -- //
	    		// Default spells
	    		String[] spells = {
	    				"BlessingDark",
	    				"CircleOfCunning",
	    				"Nimbleness",
	    				"WindOfAges",
	    				"Bloodthirst",
	    				"FlamingAura",
	    				"Frostbrand",
	    				"LifeTransfer",
	    				"RottingTouch",
	    				"Venom",
	    				"SharedPain",
	    				"WebArmour"
    				};
	    		for(String spell : spells){
		    		CtClass ctSpell = classPool.get("com.wurmonline.server.spells."+spell);
		    		String desc = Descriptor.ofMethod(CtClass.booleanType, preconditionParams);
		    		String body = "{ return mod.sin.spellcraft.spellchecks."+spell+"Checks.precondition(this.name, $2, $3); }";
		    		Util.setReason("Set precondition for "+spell);
		    		Util.setBodyDescribed(thisClass, ctSpell, "precondition", desc, body);
	    		}
	    	}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void addEnchantGroup(String[] split){
	    ArrayList<Byte> newGroup = new ArrayList<>();
	    for(String ench : split){
	        newGroup.add(Byte.valueOf(ench));
        }
        enchantGroups.add(newGroup);
    }
	public static void setEnchantGroups(){
		// Speed
		/*speedEffectGroup.add(Enchants.BUFF_BLESSINGDARK);
		speedEffectGroup.add(Enchants.BUFF_WIND_OF_AGES);
		speedEffectGroup.add(Enchants.BUFF_NIMBLENESS);
		speedEffectGroup.add((byte) 111);
		// Skill Gain
		skillgainEffectGroup.add(Enchants.BUFF_BLESSINGDARK);
		skillgainEffectGroup.add(Enchants.BUFF_CIRCLE_CUNNING);
		// Weapon Damage
		weaponDamageEffectGroup.add(Enchants.BUFF_BLOODTHIRST);
		weaponDamageEffectGroup.add(Enchants.BUFF_FLAMING_AURA);
		weaponDamageEffectGroup.add(Enchants.BUFF_FROSTBRAND);
		//weaponDamageEffectGroup.add(Enchants.BUFF_LIFETRANSFER);
		weaponDamageEffectGroup.add(Enchants.BUFF_ROTTING_TOUCH);
		weaponDamageEffectGroup.add(Enchants.BUFF_VENOM);
		// Armour Effect
		armourEffectGroup.add(Enchants.BUFF_SHARED_PAIN);
		armourEffectGroup.add(Enchants.BUFF_WEBARMOUR);*/
	}
}
