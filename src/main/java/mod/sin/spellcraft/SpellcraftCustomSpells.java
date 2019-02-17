package mod.sin.spellcraft;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.spells.Expand;
import com.wurmonline.server.spells.SpellcraftSpell;
import com.wurmonline.shared.constants.Enchants;
import javassist.*;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpellcraftCustomSpells {
	public static Logger logger = Logger.getLogger(SpellcraftCustomSpells.class.getName());

	public static void pollReplenish(){
		for(Item source : Items.getAllItems()){
			if(source.getBonusForSpellEffect(SpellcraftSpell.REPLENISH.getEnchant()) > 0){
                //logger.info("Replenish on " + source.getName() + " " + source.getDescription() + " [" + source.getTileX() + ", " + source.getTileY() + "]");
                if(source.isContainerLiquid()) {
                    int volAvail = source.getFreeVolume();
                    Item liquid = null;
                    boolean dontRefill = false;
                    for (Item contained : source.getItems()) {
                        // Skip items placed on top of the container.
                        if (contained.isPlacedOnParent()){
                            continue;
                        }
                        /*if (!contained2.isFood() && !contained2.isLiquid() && !contained2.isRecipeItem() || contained2.isLiquid() && contained2.getTemplateId() != ItemList.water) {
                            //logger.info("Container has invalid item and skips Replenish: "+source.getName()+" ["+source.getWurmId()+"] ("+source.getTileX()+", "+source.getTileY()+")");
                            return;
                        }*/
                        if (contained.getTemplateId() != ItemList.water) {
                            //logger.info("FAIL: Container has non-water object: " + contained.getName());
                            dontRefill = true;
                            break;
                        }
                        if (!(contained.getVolume() > 0)) {
                            logger.info("Container has zero-volume item inside: " + contained.getName() + " (wurmid " + source.getWurmId() + ")");
                        }
                        liquid = contained;
                    }
                    if(dontRefill){
                        continue;
                    }
                    //logger.info("Volume available: " + volAvail);
                    if (volAvail >= 1) {
                        int amountToAdd = (int) source.getBonusForSpellEffect(SpellcraftSpell.REPLENISH.getEnchant()) * 10;
                        if (source.getRarity() >= 3) {
                            amountToAdd *= 8;
                        } else if (source.getRarity() >= 2) {
                            amountToAdd *= 4;
                        } else if (source.getRarity() >= 1) {
                            amountToAdd *= 2;
                        }
                        if (amountToAdd > volAvail) {
                            amountToAdd = volAvail;
                        }
                        if (liquid != null) {
                            int allWeight = liquid.getWeightGrams() + amountToAdd;
                            float newQl = ((float) (100 * amountToAdd) + liquid.getCurrentQualityLevel() * (float) liquid.getWeightGrams()) / (float) allWeight;
                            liquid.setWeight(liquid.getWeightGrams() + amountToAdd, true);
                            liquid.setQualityLevel(newQl);
                            liquid.setDamage(0.0f);
                        } else {
                            try {
                                Item water = ItemFactory.createItem(ItemList.water, 100.0f, (byte) 26, (byte) 0, null);
                                water.setSizes(1, 1, 1);
                                water.setWeight(amountToAdd, false);
                                if (!source.insertItem(water, true)) {
                                    logger.info("Could not insert water item for Replenish, decaying the water. [" + source.getWurmId() + "] (" + source.getTileX() + ", " + source.getTileY() + ")");
                                    Items.destroyItem(water.getWurmId());
                                    return;
                                }
                            } catch (NoSuchTemplateException nst) {
                                logger.log(Level.WARNING, "No template for water?!", nst);
                            } catch (FailedException fe) {
                                logger.log(Level.WARNING, "Creation of water failed: ", fe);
                            }
                        }
                    }
                }else{
                    logger.info("Replenish casted on non-liquid container.");
                }
			}
		}
	}
	protected static float getPowerForJeweleryEnchant(Creature creature, byte enchant){
        Item[] bodyItems = creature.getBody().getContainersAndWornItems();
        float value = 0.0f;
        int i = 0;
        while(i < bodyItems.length){
            if(bodyItems[i].isEnchantableJewelry()){
                if(bodyItems[i].getBonusForSpellEffect(enchant) > 0){
                    value = Math.max(value, bodyItems[i].getBonusForSpellEffect(enchant));
                }
            }
            i++;
        }
        return value;
    }
    public static double getNewDifficulty(Skill skill, double diff, Item item){
        if(item != null){
            if(item.getBonusForSpellEffect(SpellcraftSpell.EFFICIENCY.getEnchant()) > 0) {
                diff -= item.getBonusForSpellEffect(SpellcraftSpell.EFFICIENCY.getEnchant())
                        * SpellcraftMod.efficiencyDifficultyPerPower;
            }
            if(item.getBonusForSpellEffect(SpellcraftSpell.TITANFORGED.getEnchant()) > 0){
                diff -= item.getBonusForSpellEffect(SpellcraftSpell.TITANFORGED.getEnchant())
                        * SpellcraftMod.efficiencyDifficultyPerPower * SpellcraftMod.titanforgedMultiplier;
            }
        }
        try {
            Skills parent = ReflectionUtil.getPrivateField(skill, ReflectionUtil.getField(skill.getClass(), "parent"));
            if(parent != null && parent.getId() != -10){
                Creature holder = Server.getInstance().getCreature(parent.getId());
                if(holder != null && holder.isPlayer()){
                    float industryPower = getPowerForJeweleryEnchant(holder, SpellcraftSpell.INDUSTRY.getEnchant());
                    if(industryPower > 0){
                        diff -= industryPower * SpellcraftMod.industryEffectiveness;
                    }
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchPlayerException | NoSuchCreatureException e) {
            e.printStackTrace();
        }
        return diff;
    }
    public static float getFavorCostMultiplier(Skill skill, float favorCost){
        float mult = 1f;
        try {
            if(favorCost > 10) {
                Skills parent = ReflectionUtil.getPrivateField(skill, ReflectionUtil.getField(skill.getClass(), "parent"));
                if (parent != null && parent.getId() != -10) {
                    Creature holder = Server.getInstance().getCreature(parent.getId());
                    if (holder != null && holder.isPlayer()) {
                        float industryPower = getPowerForJeweleryEnchant(holder, SpellcraftSpell.ACUITY.getEnchant());
                        if (industryPower > 0) {
                            mult *= 1 - (industryPower * SpellcraftMod.acuityEffectiveness);
                        }
                    }
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchPlayerException | NoSuchCreatureException e) {
            e.printStackTrace();
        }
        return mult;
    }
    public static float getNewStaminaMod(Creature creature, float staminaMod){
	    float mod = 0.0f;
	    if(creature != null && creature.isPlayer()){
	        float endurancePower = getPowerForJeweleryEnchant(creature, SpellcraftSpell.ENDURANCE.getEnchant());
	        if(endurancePower > 0){
	            float maxChange = 1-staminaMod;
	            float percentChange = endurancePower * SpellcraftMod.enduranceEffectiveness;
	            mod += maxChange * percentChange;
            }
        }
        return mod;
    }
    public static float getSurfaceMiningChance(Creature performer, Skill mining, Item pickaxe){
	    if(pickaxe.getBonusForSpellEffect(SpellcraftSpell.QUARRY.getEnchant()) > 0){
	        float power = pickaxe.getBonusForSpellEffect(SpellcraftSpell.QUARRY.getEnchant());
	        float knowledge = (float) mining.getKnowledge();
            return (knowledge/200f)+(power/200);
        }
	    return Math.max(0.2f, (float)mining.getKnowledge(0.0) / 200.0f);
    }
    public static float addProwessModifier(float combatRating, Creature cret, Creature opponent){
        //logger.info("Checking additive ("+cret.getName()+" vs "+opponent.getName()+"), combatRating = "+combatRating);
        float add = 0.0f;
        if(cret != null && cret.isPlayer()){
            float prowessPower = getPowerForJeweleryEnchant(cret, SpellcraftSpell.PROWESS.getEnchant());
            if(prowessPower > 0) {
                add += (prowessPower * SpellcraftMod.prowessEffectiveness);
            }
        }
        return add;
    }
    public static float getNewSpellSkillBonus(Item item){
        if(item.isArtifact() && item.isWeapon()){
            return 99f;
        }
        float newValue = item.getBonusForSpellEffect(Enchants.BUFF_CIRCLE_CUNNING);
        newValue += item.getBonusForSpellEffect(Enchants.BUFF_BLESSINGDARK);
        newValue += item.getBonusForSpellEffect(SpellcraftSpell.TITANFORGED.getEnchant())*SpellcraftMod.titanforgedMultiplier;
        return newValue;
    }
    public static float getNewSpellSpeedBonus(Item item){
        float newValue = item.getBonusForSpellEffect(Enchants.BUFF_WIND_OF_AGES);
        newValue += item.getBonusForSpellEffect(Enchants.BUFF_BLESSINGDARK);
        newValue += item.getBonusForSpellEffect(SpellcraftSpell.TITANFORGED.getEnchant())*SpellcraftMod.titanforgedMultiplier;
        return newValue;
    }
	public static void preInit(SpellcraftMod mod){
		try {
	    	ClassPool classPool = HookManager.getInstance().getClassPool();
	    	Class<SpellcraftCustomSpells> thisClass = SpellcraftCustomSpells.class;

	    	if(SpellcraftSpell.PHASING.isEnabled()){
		        CtClass ctCombatHandler = classPool.get("com.wurmonline.server.creatures.CombatHandler");
		        String replace = "$_ = $proceed($1+(weapon.getBonusForSpellEffect((byte)111)*"+String.valueOf(mod.phasingPowerMultiplier)+"), $2, $3, $4, $5, $6, $7);";
		        Util.setReason("Add phasing spell effect to shield blocks.");
		        Util.instrumentDeclared(thisClass, ctCombatHandler, "checkShield", "skillCheck", replace);
	    		/*ctCombatHandler.getDeclaredMethod("checkShield").instrument(new ExprEditor(){
				    public void edit(MethodCall m) throws CannotCompileException {
				        if (m.getMethodName().equals("skillCheck")) {
				        	m.replace("$_ = $proceed($1+(weapon.getBonusForSpellEffect((byte)111)*"+String.valueOf(mod.phasingPowerMultiplier)+"), $2, $3, $4, $5, $6, $7);");
				            return;
				        }
				    }
				});*/
	    	}
	    	if(SpellcraftSpell.REPLENISH.isEnabled()){
                Util.setReason("Enable replenish functionality.");
				CtClass ctServer = classPool.get("com.wurmonline.server.Server");
				String replace = SpellcraftCustomSpells.class.getName()+".pollReplenish();"
                		+ "$_ = $proceed($$);";
				Util.instrumentDeclared(thisClass, ctServer, "run", "pruneTransfers", replace);
	            /*ctServer.getDeclaredMethod("run").instrument(new ExprEditor(){
	                public void edit(MethodCall m) throws CannotCompileException {
	                    if (m.getMethodName().equals("pruneTransfers")) {
	                        m.replace("mod.sin.spellcraft.SpellcraftCustomSpells.pollReplenish();"
	                        		+ "$_ = $proceed($$);");
	                        return;
	                    }
	                }
	            });*/
	    	}
	    	if(SpellcraftSpell.EXPAND.isEnabled()){
				ExprEditor exprEditor = new ExprEditor() {
					@Override
					public void edit(MethodCall m) throws CannotCompileException {
						if ("com.wurmonline.server.items.ItemTemplate".equals(m.getClassName())) {
							if ("getContainerSizeX".equals(m.getMethodName())) {
								m.replace("$_ = this.getContainerSizeX();");
							} else if ("getContainerSizeY".equals(m.getMethodName())) {
								m.replace("$_ = this.getContainerSizeY();");
							} else if ("getContainerSizeZ".equals(m.getMethodName())) {
								m.replace("$_ = this.getContainerSizeZ();");
							}
						}
					}
				};

				String descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { classPool.get("com.wurmonline.server.items.Item"), CtClass.booleanType });
				classPool.get("com.wurmonline.server.items.Item").getMethod("insertItem", descriptor).instrument(exprEditor);

				descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] { classPool.get("com.wurmonline.server.items.Item"), CtClass.booleanType });
				classPool.get("com.wurmonline.server.items.Item").getMethod("testInsertHollowItem", descriptor).instrument(exprEditor);
			}
			if(SpellcraftSpell.EFFICIENCY.isEnabled() || SpellcraftSpell.INDUSTRY.isEnabled() || SpellcraftSpell.TITANFORGED.isEnabled()){
                Util.setReason("Modify difficulty for custom spells.");
                CtClass ctSkill = classPool.get("com.wurmonline.server.skills.Skill");
                String replace = "$1 = "+SpellcraftCustomSpells.class.getName()+".getNewDifficulty($0, $1, $2);";
                Util.insertBeforeDeclared(thisClass, ctSkill, "checkAdvance", replace);
            }
            if(SpellcraftSpell.QUARRY.isEnabled()){
	    	    Util.setReason("Change difficulty for successful surface mining with Quarry.");
	    	    CtClass ctTileRockBehaviour = classPool.get("com.wurmonline.server.behaviours.TileRockBehaviour");
	    	    String replace = "if($1 == 0.2f){" +
                        "  $_ = "+SpellcraftCustomSpells.class.getName()+".getSurfaceMiningChance(performer, mining, source);" +
                        "}else{" +
                        "  $_ = $proceed($$);" +
                        "}";
	    	    Util.instrumentDeclared(thisClass, ctTileRockBehaviour, "mine", "max", replace);
            }
            if(SpellcraftSpell.PROWESS.isEnabled()){
                Util.setReason("Enable prowess to increase combat rating.");
                CtClass ctCombatHandler = classPool.get("com.wurmonline.server.creatures.CombatHandler");
                String replace = "combatRating += "+SpellcraftCustomSpells.class.getName()+".addProwessModifier(combatRating, this.creature, $1);" +
                        "$_ = $proceed($$);";
                Util.instrumentDeclared(thisClass, ctCombatHandler, "getCombatRating", "getFlankingModifier", replace);
            }
            if(SpellcraftSpell.ENDURANCE.isEnabled()){
                Util.setReason("Enable Endurance to reduce stamina usage.");
                CtClass ctCreatureStatus = classPool.get("com.wurmonline.server.creatures.CreatureStatus");
                String replace = "staminaMod += "+SpellcraftCustomSpells.class.getName()+".getNewStaminaMod(this.statusHolder, staminaMod);" +
                        "$_ = $proceed($$);";
                Util.instrumentDeclared(thisClass, ctCreatureStatus, "modifyStamina", "getCultist", replace);
            }
            if(SpellcraftSpell.ACUITY.isEnabled()){
                CtClass ctSpell = classPool.get("com.wurmonline.server.spells.Spell");
                CtMethod[] ctRuns = ctSpell.getDeclaredMethods("run");
                for(CtMethod method : ctRuns){
                    method.instrument(new ExprEditor(){
                        public void edit(MethodCall m) throws CannotCompileException {
                            if (m.getMethodName().equals("depleteFavor")) {
                                m.replace("$1 *= "+SpellcraftCustomSpells.class.getName()+".getFavorCostMultiplier(castSkill, $1);"
                                        + "$_ = $proceed($$);");
                                logger.info("Instrumented depleteFavor in run()");
                            }
                        }
                    });
                }
            }
            if(SpellcraftSpell.TITANFORGED.isEnabled()){
                Util.setReason("Allow Titanforged to stack skill gain.");
                CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
                String replace = "return "+SpellcraftCustomSpells.class.getName()+".getNewSpellSkillBonus($0);";
                Util.setBodyDeclared(thisClass, ctItem, "getSpellSkillBonus", replace);

                Util.setReason("Allow Titanforged to stack speed bonus.");
                replace = "return "+SpellcraftCustomSpells.class.getName()+".getNewSpellSpeedBonus($0);";
                Util.setBodyDeclared(thisClass, ctItem, "getSpellSpeedBonus", replace);
            }
		} catch (NotFoundException | CannotCompileException e) {
			e.printStackTrace();
		}
	}
	public static void init(SpellcraftMod mod){
        HookManager.getInstance().registerHook("com.wurmonline.server.items.Item", "getContainerVolume", "()I", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object volume = method.invoke(proxy, args);

                        if (volume instanceof Number && proxy instanceof Item && Expand.isValidContainer((Item) proxy)) {
                            Item target = (Item)proxy;

                            //float modifier = BagOfHolding.getSpellEffect(target);
                            float modifier = target.getBonusForSpellEffect(SpellcraftSpell.EXPAND.getEnchant());

                            if (mod.expandEffectModifier == 0) {
                                if (modifier > 1) {
                                    double newVolume = Math.min(Integer.MAX_VALUE, modifier * ((Number) volume).doubleValue());
                                    return (int) newVolume;
                                }
                            } else if (modifier > 0) {
                                double scale = 1 + modifier * modifier * mod.expandEffectModifier * 0.0001;
                                double newVolume = Math.min(Integer.MAX_VALUE, scale * ((Number) volume).doubleValue());
                                return (int) newVolume;
                            }
                        }

                        return volume;
                    }
                };
            }
        });

        InvocationHandlerFactory invocationHandlerFactory = new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object dimension = method.invoke(proxy, args);

                        if (dimension instanceof Number && proxy instanceof Item && Expand.isValidContainer((Item) proxy)) {
                            Item target = (Item)proxy;

                            //float modifier = BagOfHolding.getSpellEffect(target);
                            float modifier = target.getBonusForSpellEffect(SpellcraftSpell.EXPAND.getEnchant());

                            if (mod.expandEffectModifier == 0) {
                                if (modifier > 1) {
                                    double newDimension = Math.min(1200, Math.cbrt(modifier) * ((Number) dimension).doubleValue());
                                    return (int) newDimension;
                                }
                            } else if (modifier > 0) {
                                double scale = 1 + modifier * modifier * mod.expandEffectModifier * 0.0001;
                                double newDimension = Math.min(1200, Math.cbrt(scale) * ((Number) dimension).doubleValue());
                                return (int) newDimension;
                            }
                        }

                        return dimension;
                    }
                };
            }
        };

        HookManager.getInstance().registerHook("com.wurmonline.server.items.Item", "getContainerSizeX", "()I", invocationHandlerFactory);
        HookManager.getInstance().registerHook("com.wurmonline.server.items.Item", "getContainerSizeY", "()I", invocationHandlerFactory);
        HookManager.getInstance().registerHook("com.wurmonline.server.items.Item", "getContainerSizeZ", "()I", invocationHandlerFactory);
    }
}
