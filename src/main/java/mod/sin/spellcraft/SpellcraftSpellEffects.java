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
	
	protected static boolean improvedEnchantGrouping = true;
	
	public static SpellEffect hasNegatingEffect(Item target, byte enchantment){
		if(!improvedEnchantGrouping){
			return null;
		}
		if(target.getSpellEffects() != null){
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
	    		// Blessings of the Dark
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
	    		
				/*CtClass ctBlessingDark = classPool.get("com.wurmonline.server.spells.BlessingDark");
				ctBlessingDark.getMethod("precondition", Descriptor.ofMethod(CtClass.booleanType, preconditionParams))
					.setBody("{ return mod.sin.spellcraft.spellchecks.BlessingDarkChecks.precondition(this.name, $2, $3); }");

				// Circle of Cunning
				CtClass ctCircleOfCunning = classPool.get("com.wurmonline.server.spells.CircleOfCunning");
				ctCircleOfCunning.getMethod("precondition", Descriptor.ofMethod(CtClass.booleanType, preconditionParams))
					.setBody("{ return mod.sin.spellcraft.spellchecks.CircleOfCunningChecks.precondition(this.name, $2, $3); }");

				// Nimbleness
				CtClass ctNimbleness = classPool.get("com.wurmonline.server.spells.Nimbleness");
				ctNimbleness.getMethod("precondition", Descriptor.ofMethod(CtClass.booleanType, preconditionParams))
					.setBody("{ return mod.sin.spellcraft.spellchecks.NimblenessChecks.precondition(this.name, $2, $3); }");
				
				// Wind of Ages
				CtClass ctWindOfAges = classPool.get("com.wurmonline.server.spells.WindOfAges");
				ctWindOfAges.getMethod("precondition", Descriptor.ofMethod(CtClass.booleanType, preconditionParams))
					.setBody("{ return mod.sin.spellcraft.spellchecks.WindOfAgesChecks.precondition(this.name, $2, $3); }");*/
				
				// -- Weapon Damage Group -- //
				// Bloodthirst
				/*CtClass ctBloodthirst = classPool.get("com.wurmonline.server.spells.Bloodthirst");
				ctBloodthirst.getMethod("precondition", Descriptor.ofMethod(CtClass.booleanType, preconditionParams))
					.setBody("{ return mod.sin.spellcraft.spellchecks.BloodthirstChecks.precondition(this.name, $2, $3); }");

				// Flaming Aura
				CtClass ctFlamingAura = classPool.get("com.wurmonline.server.spells.FlamingAura");
				ctFlamingAura.getMethod("precondition", Descriptor.ofMethod(CtClass.booleanType, preconditionParams))
					.setBody("{ return mod.sin.spellcraft.spellchecks.FlamingAuraChecks.precondition(this.name, $2, $3); }");

				// Frostbrand
				CtClass ctFrostbrand = classPool.get("com.wurmonline.server.spells.Frostbrand");
				ctFrostbrand.getMethod("precondition", Descriptor.ofMethod(CtClass.booleanType, preconditionParams))
					.setBody("{ return mod.sin.spellcraft.spellchecks.FrostbrandChecks.precondition(this.name, $2, $3); }");

				// Life Transfer
				CtClass ctLifeTransfer = classPool.get("com.wurmonline.server.spells.LifeTransfer");
				ctLifeTransfer.getMethod("precondition", Descriptor.ofMethod(CtClass.booleanType, preconditionParams))
					.setBody("{ return mod.sin.spellcraft.spellchecks.LifeTransferChecks.precondition(this.name, $2, $3); }");
				
				// Rotting Touch
				CtClass ctRottingTouch = classPool.get("com.wurmonline.server.spells.RottingTouch");
				ctRottingTouch.getMethod("precondition", Descriptor.ofMethod(CtClass.booleanType, preconditionParams))
					.setBody("{ return mod.sin.spellcraft.spellchecks.RottingTouchChecks.precondition(this.name, $2, $3); }");

				// Venom
				CtClass ctVenom = classPool.get("com.wurmonline.server.spells.Venom");
				ctVenom.getMethod("precondition", Descriptor.ofMethod(CtClass.booleanType, preconditionParams))
					.setBody("{ return mod.sin.spellcraft.spellchecks.VenomChecks.precondition(this.name, $2, $3); }");*/
				
				// -- Armour Enchant Group -- //
				// Shared Pain
				/*CtClass ctSharedPain = classPool.get("com.wurmonline.server.spells.SharedPain");
				ctSharedPain.getMethod("precondition", Descriptor.ofMethod(CtClass.booleanType, preconditionParams))
					.setBody("{ return mod.sin.spellcraft.spellchecks.SharedPainChecks.precondition(this.name, $2, $3); }");
				// Web Armour
				CtClass ctWebArmour = classPool.get("com.wurmonline.server.spells.WebArmour");
				ctWebArmour.getMethod("precondition", Descriptor.ofMethod(CtClass.booleanType, preconditionParams))
					.setBody("{ return mod.sin.spellcraft.spellchecks.WebArmourChecks.precondition(this.name, $2, $3); }");*/
	    	}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void setEnchantGroups(){
		// Speed
		speedEffectGroup.add(Enchants.BUFF_BLESSINGDARK);
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
		armourEffectGroup.add(Enchants.BUFF_WEBARMOUR);
	}
}
