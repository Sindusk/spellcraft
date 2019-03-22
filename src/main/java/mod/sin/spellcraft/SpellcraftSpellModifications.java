package mod.sin.spellcraft;

import com.wurmonline.server.Server;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class SpellcraftSpellModifications {
	protected static Logger logger = Logger.getLogger(SpellcraftSpellModifications.class.getName());
	
	// Makes changes to default spells:
	protected static void modifyDefaultSpells(SpellcraftMod mod){
		String name;
		for(Spell spell : Spells.getAllSpells()){
			name = spell.getName();
			if(mod.spellCastTimes.containsKey(name)){
				int newCastTime = mod.spellCastTimes.get(name);
				try {
					logger.info("Setting "+name+" cast time to "+newCastTime);
					ReflectionUtil.setPrivateField(spell, ReflectionUtil.getField(Spell.class, "castingTime"), newCastTime);
				} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
						| NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			if(mod.spellCosts.containsKey(name)){
				int newCost = mod.spellCosts.get(name);
				try {
					logger.info("Setting "+name+" cost to "+newCost);
					ReflectionUtil.setPrivateField(spell, ReflectionUtil.getField(Spell.class, "cost"), newCost);
				} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
						| NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			if(mod.spellDifficulties.containsKey(name)){
				int newDifficulty = mod.spellDifficulties.get(name);
				try {
					logger.info("Setting "+name+" difficulty to "+newDifficulty);
					ReflectionUtil.setPrivateField(spell, ReflectionUtil.getField(Spell.class, "difficulty"), newDifficulty);
				} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
						| NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			if(mod.spellFaithRequirements.containsKey(name)){
				int newFaith = mod.spellFaithRequirements.get(name);
				try {
					logger.info("Setting "+name+" faith requirement to "+newFaith);
					ReflectionUtil.setPrivateField(spell, ReflectionUtil.getField(Spell.class, "level"), newFaith);
				} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
						| NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			if(mod.spellCooldowns.containsKey(name)){
				long newCooldown = mod.spellCooldowns.get(name);
				try {
					logger.info("Setting "+name+" cooldown to "+newCooldown);
					ReflectionUtil.setPrivateField(spell, ReflectionUtil.getField(Spell.class, "cooldown"), newCooldown);
				} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
						| NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
		}
	}
	// Edits which deities can cast which spells:
	public static void editDeitySpells(SpellcraftMod mod){
		for(Deity deity : Deities.getDeities()){
			for(Spell spell : Spells.getAllSpells()){
				if(!deity.hasSpell(spell)){
					// Process potential addition
					if(mod.addSpells.containsKey(spell.getName())){
						List<String> deityNums = mod.addSpells.get(spell.getName());
						if(deityNums.contains("-1")){
							logger.info("Adding spell "+spell.getName()+" to "+deity.getName());
							deity.addSpell(spell);
						}else{
							for(String num : deityNums){
								if(Integer.parseInt(num) == deity.number){
									logger.info("Adding spell "+spell.getName()+" to "+deity.getName());
									deity.addSpell(spell);
								}
							}
						}
					}
				}else{
					if(mod.removeSpells.containsKey(spell.getName())){
						List<String> deityNums = mod.removeSpells.get(spell.getName());
						if(deityNums.contains("-1")){
							logger.info("Removing spell "+spell.getName()+" from "+deity.getName());
							deity.getSpells().remove(spell);
							if(spell.isTargetCreature()){
								try {
									Set<Spell> creatureSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "creatureSpells"));
									creatureSpells.remove(spell);
								} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
										| NoSuchFieldException e) {
									e.printStackTrace();
								}
							}
							if(spell.isTargetAnyItem()){
								try {
									Set<Spell> itemSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "itemSpells"));
									itemSpells.remove(spell);
								} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
										| NoSuchFieldException e) {
									e.printStackTrace();
								}
							}
							if(spell.isTargetWound()){
								try {
									Set<Spell> woundSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "woundSpells"));
									woundSpells.remove(spell);
								} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
										| NoSuchFieldException e) {
									e.printStackTrace();
								}
							}
							if(spell.isTargetTile()){
								try {
									Set<Spell> tileSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "tileSpells"));
									tileSpells.remove(spell);
								} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
										| NoSuchFieldException e) {
									e.printStackTrace();
								}
							}
						}else{
							for(String num : deityNums){
								if(Integer.parseInt(num) == deity.number){
									logger.info("Removing spell "+spell.getName()+" from "+deity.getName());
									deity.getSpells().remove(spell);
									if(spell.isTargetCreature()){
										try {
											Set<Spell> creatureSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "creatureSpells"));
											creatureSpells.remove(spell);
										} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
												| NoSuchFieldException e) {
											e.printStackTrace();
										}
									}
									if(spell.isTargetAnyItem()){
										try {
											Set<Spell> itemSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "itemSpells"));
											itemSpells.remove(spell);
										} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
												| NoSuchFieldException e) {
											e.printStackTrace();
										}
									}
									if(spell.isTargetWound()){
										try {
											Set<Spell> woundSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "woundSpells"));
											woundSpells.remove(spell);
										} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
												| NoSuchFieldException e) {
											e.printStackTrace();
										}
									}
									if(spell.isTargetTile()){
										try {
											Set<Spell> tileSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "tileSpells"));
											tileSpells.remove(spell);
										} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
												| NoSuchFieldException e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public static void doRecodedSmite(Skill castSkill, double power, Creature performer, Creature target) {
        /*if (Server.rand.nextFloat() > target.addSpellResistance((short) 252)) {
            performer.getCommunicator().sendNormalServerMessage(target.getName() + " resists your attempt to smite " + target.getHimHerItString() + ".", (byte) 3);
            target.getCommunicator().sendSafeServerMessage(performer.getName() + " tries to smite you but you resist.", (byte) 4);
            return;
        }*/
		int damage = target.getStatus().damage;
		int minhealth = 65435;
		float maxdam = Math.max(0, minhealth - damage);
		if (maxdam > 500.0f) {
			if(Server.rand.nextFloat()*(1-(performer.getSoulStrength().getKnowledge()/100f)) > target.addSpellResistance((short) 252)){
				performer.getCommunicator().sendNormalServerMessage("You smite " + target.getName() + ".", (byte) 2);
				maxdam *= 0.25f;
			}else{
				performer.getCommunicator().sendNormalServerMessage("You smite " + target.getName() + " with all your might.", (byte) 2);
			}
			float armourMultiplier = (float) (1+(power/30d));
			target.getCommunicator().sendAlertServerMessage(performer.getName() + " smites you.", (byte) 4);
			target.addWoundOfType(null, Wound.TYPE_BURN, 0, false, Math.min(1.0f, target.getArmourMod()*armourMultiplier), false, maxdam, 0f, 0f, true, true);
			VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
			if (t != null) {
				t.sendAttachCreatureEffect(target, (byte) 10, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
			}
		} else {
			performer.getCommunicator().sendNormalServerMessage("You try to smite " + target.getName() + " but there seems to be no effect.", (byte) 3);
			target.getCommunicator().sendNormalServerMessage(performer.getName() + " tries to smite you but to no avail.", (byte) 4);
		}
	}

	public static void onServerStarted(SpellcraftMod mod){
		modifyDefaultSpells(mod);
		editDeitySpells(mod);
		/* Not necessary until editing spells
		for(Spell spell : Spells.getAllSpells()){
			try {
			    if(SpellcraftMod.increaseFranticChargeDuration) {
                    if (spell.getName().equals("Frantic charge")) {
                        ReflectionUtil.setPrivateField(spell, ReflectionUtil.getField(spell.getClass(), "durationModifier"), 20f);
                        logger.info("Set frantic charge duration modifier.");
                    }
                }
			} catch (IllegalAccessException | NoSuchFieldException e) {
				e.printStackTrace();
			}
		}*/
	}
	public static void preInit(){
		//ModActions.init();
		try{
			ClassPool classPool = HookManager.getInstance().getClassPool();
			Class<SpellcraftSpellModifications> thisClass = SpellcraftSpellModifications.class;

			if(SpellcraftMod.scornHealWithoutDamage){
                Util.setReason("Allow Scorn of Libila to heal without dealing damage.");
				CtClass ctScornOfLibila = classPool.get("com.wurmonline.server.spells.ScornOfLibila");
				String replace = "damdealt = 100;"
                		+ "$_ = $proceed($$);";
				Util.instrumentDeclared(thisClass, ctScornOfLibila, "doEffect", "getCreatures", replace);
			}
			/* Removed with Wurm Unlimited 1.9 - No longer functional and probably not necessary.
			if(SpellcraftMod.reduceScornHealingDone){
                Util.setReason("Reduce effectiveness of Scorn of Libila healing to 33% effect.");
				CtClass ctScornOfLibila = classPool.get("com.wurmonline.server.spells.ScornOfLibila");
				String replace = "$_ = $proceed($1 / 3);";
				Util.instrumentDeclared(thisClass, ctScornOfLibila, "doEffect", "healRandomWound", replace);
			}*/

			if(SpellcraftMod.useRecodedSmite){
                Util.setReason("Use recoded smite method.");
                CtClass ctSmite = classPool.get("com.wurmonline.server.spells.Smite");
                String replace = "{ "+SpellcraftSpellModifications.class.getName()+".doRecodedSmite($1, $2, $3, $4); }";
                Util.setBodyDeclared(thisClass, ctSmite, "doEffect", replace);
            }
	        
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void init(SpellcraftMod mod){
		
	}
}
