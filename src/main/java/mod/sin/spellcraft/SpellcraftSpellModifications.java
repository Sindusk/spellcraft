package mod.sin.spellcraft;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.Spells;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.lib.Util;

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
							if(spell.targetCreature){
								try {
									Set<Spell> creatureSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "creatureSpells"));
									creatureSpells.remove(spell);
								} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
										| NoSuchFieldException e) {
									e.printStackTrace();
								}
							}
							if(spell.targetItem){
								try {
									Set<Spell> itemSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "itemSpells"));
									itemSpells.remove(spell);
								} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
										| NoSuchFieldException e) {
									e.printStackTrace();
								}
							}
							if(spell.targetWound){
								try {
									Set<Spell> woundSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "woundSpells"));
									woundSpells.remove(spell);
								} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
										| NoSuchFieldException e) {
									e.printStackTrace();
								}
							}
							if(spell.targetTile){
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
									if(spell.targetCreature){
										try {
											Set<Spell> creatureSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "creatureSpells"));
											creatureSpells.remove(spell);
										} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
												| NoSuchFieldException e) {
											e.printStackTrace();
										}
									}
									if(spell.targetItem){
										try {
											Set<Spell> itemSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "itemSpells"));
											itemSpells.remove(spell);
										} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
												| NoSuchFieldException e) {
											e.printStackTrace();
										}
									}
									if(spell.targetWound){
										try {
											Set<Spell> woundSpells = ReflectionUtil.getPrivateField(deity, ReflectionUtil.getField(deity.getClass(), "woundSpells"));
											woundSpells.remove(spell);
										} catch (IllegalArgumentException | IllegalAccessException | ClassCastException
												| NoSuchFieldException e) {
											e.printStackTrace();
										}
									}
									if(spell.targetTile){
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
	public static void onServerStarted(SpellcraftMod mod){
		modifyDefaultSpells(mod);
		editDeitySpells(mod);
	}
	public static void preInit(SpellcraftMod mod){
		ModActions.init();
		
		try{
			ClassPool classPool = HookManager.getInstance().getClassPool();
			Class<SpellcraftSpellModifications> thisClass = SpellcraftSpellModifications.class;
			
			// - Buff Scorn of Libila to heal even when damage is not dealt -
			if(mod.scornHealWithoutDamage){
				CtClass ctScornOfLibila = classPool.get("com.wurmonline.server.spells.ScornOfLibila");
				String replace = "damdealt = 100;"
                		+ "$_ = $proceed($$);";
				Util.setReason("Allow Scorn of Libila to heal without dealing damage.");
				Util.instrumentDeclared(thisClass, ctScornOfLibila, "doEffect", "getCreatures", replace);
				/*ctScornOfLibila.getDeclaredMethod("doEffect").instrument(new ExprEditor(){
	                public void edit(MethodCall m) throws CannotCompileException {
	                    if (m.getMethodName().equals("getCreatures")) {
	                        m.replace("damdealt = 100;"
	                        		+ "$_ = $proceed($$);");
	                        return;
	                    }
	                }
	            });*/
			}
	        
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void init(SpellcraftMod mod){
		
	}
}
