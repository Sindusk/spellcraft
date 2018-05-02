package mod.sin.spellcraft;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.*;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.Expand;
import javassist.CannotCompileException;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;

public class SpellcraftCustomSpells {
	public static Logger logger = Logger.getLogger(SpellcraftCustomSpells.class.getName());

	public static void pollReplenish(){
		for(Item source : Items.getAllItems()){
			if(source.getSpellEffectPower(SpellcraftEnchants.REPLENISH) > 0 && source.isContainerLiquid()){
	            Item liquid = null;
	            for (Item contained2 : source.getItems()) {
	                if (!contained2.isFood() && !contained2.isLiquid() && !contained2.isRecipeItem() || contained2.isLiquid() && contained2.getTemplateId() != ItemList.water) {
	                	//logger.info("Container has invalid item and skips Replenish: "+source.getName()+" ["+source.getWurmId()+"] ("+source.getTileX()+", "+source.getTileY()+")");
	                    return;
	                }
	                if (!contained2.isLiquid()) continue;
	                liquid = contained2;
	            }
	            int volAvail = source.getFreeVolume();
	            Item contained2 = liquid;
                if (volAvail >= 1) {
                	int amountToAdd = (int) source.getSpellEffectPower(SpellcraftEnchants.REPLENISH)*10;
                	if(source.getRarity() >= 3){
                		amountToAdd *= 8;
                	}else if(source.getRarity() >= 2){
                		amountToAdd *= 4;
                	}else if(source.getRarity() >= 1){
                		amountToAdd *= 2;
                	}
                	if(amountToAdd > volAvail){
                		amountToAdd = volAvail;
                	}
                    if (liquid != null) {
                        int allWeight = liquid.getWeightGrams() + amountToAdd;
                        float newQl = ((float)(100 * amountToAdd) + contained2.getCurrentQualityLevel() * (float)contained2.getWeightGrams()) / (float)allWeight;
                        liquid.setWeight(liquid.getWeightGrams() + amountToAdd, true);
                        liquid.setQualityLevel(newQl);
                        liquid.setDamage(0.0f);
                    } else {
                        try {
                            Item water = ItemFactory.createItem(ItemList.water, 100.0f, (byte) 26, (byte) 0, null);
                            water.setSizes(1, 1, 1);
                            water.setWeight(amountToAdd, false);
                            if (!source.insertItem(water)) {
                            	logger.info("Could not insert water item for Replenish, decaying the water. ["+source.getWurmId()+"] ("+source.getTileX()+", "+source.getTileY()+")");
                                Items.decay(water.getWurmId(), water.getDbStrings());
                                return;
                            }
                        }
                        catch (NoSuchTemplateException nst) {
                            logger.log(Level.WARNING, "No template for water?!", nst);
                        }
                        catch (FailedException fe) {
                            logger.log(Level.WARNING, "Creation of water failed: ", fe);
                        }
                    }
                }
			}
		}
	}
    public static double getEfficiencyDifficulty(double diff, Item item){
        if(item != null && item.getSpellEffectPower(SpellcraftEnchants.EFFICIENCY) > 0){
            diff -= item.getSpellEffectPower(SpellcraftEnchants.EFFICIENCY)*SpellcraftMod.efficiencyDifficultyPerPower;
        }
        return diff;
    }
    public static float getSurfaceMiningChance(Creature performer, Skill mining, Item pickaxe){
	    if(pickaxe.getSpellEffectPower(SpellcraftEnchants.QUARRY) > 0){
	        float power = pickaxe.getSpellEffectPower(SpellcraftEnchants.QUARRY);
	        float knowledge = (float) mining.getKnowledge();
            return (knowledge/200f)+(power/200);
        }
	    return Math.max(0.2f, (float)mining.getKnowledge(0.0) / 200.0f);
    }
	public static void preInit(SpellcraftMod mod){
		try {
	    	ClassPool classPool = HookManager.getInstance().getClassPool();
	    	Class<SpellcraftCustomSpells> thisClass = SpellcraftCustomSpells.class;

	    	if(mod.spellEnablePhasing){
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
	    	if(mod.spellEnableReplenish){
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
	    	if(mod.spellEnableExpand){
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
			if(mod.spellEnableEfficiency){
                Util.setReason("Modify difficulty for tools enchanted with Efficiency.");
                CtClass ctSkill = classPool.get("com.wurmonline.server.skills.Skill");
                String replace = "$1 = "+SpellcraftCustomSpells.class.getName()+".getEfficiencyDifficulty($1, $2);";
                Util.insertBeforeDeclared(thisClass, ctSkill, "checkAdvance", replace);
            }
            if(mod.spellEnableQuarry){
	    	    Util.setReason("Change difficulty for successful surface mining with Quarry.");
	    	    CtClass ctTileRockBehaviour = classPool.get("com.wurmonline.server.behaviours.TileRockBehaviour");
	    	    String replace = "if($1 == 0.2f){" +
                        "  $_ = "+SpellcraftCustomSpells.class.getName()+".getSurfaceMiningChance(performer, mining, source);" +
                        "}else{" +
                        "  $_ = $proceed($$);" +
                        "}";
	    	    Util.instrumentDeclared(thisClass, ctTileRockBehaviour, "mine", "max", replace);
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
                            float modifier = target.getSpellEffectPower(SpellcraftEnchants.EXPAND);

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
                            float modifier = target.getSpellEffectPower(SpellcraftEnchants.EXPAND);

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
