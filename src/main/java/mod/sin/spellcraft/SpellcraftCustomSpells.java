package mod.sin.spellcraft;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.NoSuchTemplateException;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.lib.Util;

public class SpellcraftCustomSpells {
	public static Logger logger = Logger.getLogger(SpellcraftCustomSpells.class.getName());
	public static void pollReplenish(){
		for(Item source : Items.getAllItems()){
			if(source.getBonusForSpellEffect((byte) 112) > 0 && source.isContainerLiquid()){
	            Item liquid = null;
	            for (Item contained2 : source.getItems()) {
	                if (!contained2.isFood() && !contained2.isLiquid() && !contained2.isRecipeItem() || contained2.isLiquid() && contained2.getTemplateId() != ItemList.water) {
	                	logger.info("Container has invalid item and skips Replenish: "+source.getName()+" ["+source.getWurmId()+"] ("+source.getTileX()+", "+source.getTileY()+")");
	                    return;
	                }
	                if (!contained2.isLiquid()) continue;
	                liquid = contained2;
	            }
	            int volAvail = source.getFreeVolume();
	            Item contained2 = liquid;
                if (volAvail >= 1) {
                	int amountToAdd = (int) source.getBonusForSpellEffect((byte) 112)*10;
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
                    if (liquid != null && contained2 != null) {
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
                            logger.log(Level.WARNING, "No template for water?!", (Throwable)((Object)nst));
                        }
                        catch (FailedException fe) {
                            logger.log(Level.WARNING, "Creation of water failed: ", (Throwable)((Object)fe));
                        }
                    }
                }
			}
		}
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
				CtClass ctServer = classPool.get("com.wurmonline.server.Server");
				String replace = SpellcraftCustomSpells.class.getName()+".pollReplenish();"
                		+ "$_ = $proceed($$);";
				Util.setReason("Enable replenish functionality.");
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
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
}
