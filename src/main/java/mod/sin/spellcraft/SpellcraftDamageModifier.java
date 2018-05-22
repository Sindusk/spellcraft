package mod.sin.spellcraft;

import com.wurmonline.server.spells.SpellcraftSpell;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.items.RuneUtilities;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.lib.Util;

import java.util.logging.Logger;

public class SpellcraftDamageModifier {
    protected static Logger logger = Logger.getLogger(SpellcraftDamageModifier.class.getName());

	protected static boolean useNewDamageModifier = true;

	public static void preInit(SpellcraftMod mod){
		useNewDamageModifier = mod.useNewDamageModifier;
		try {
			ClassPool classPool = HookManager.getInstance().getClassPool();
			Class<SpellcraftDamageModifier> thisClass = SpellcraftDamageModifier.class;
			
    		CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
	        //CtClass ctDamageModifier = classPool.get(new SpellcraftDamageModifier().getClass().getName());
	        //ctItem.addMethod(CtNewMethod.copy(ctDamageModifier.getDeclaredMethod("newGetDamageModifier"), ctItem, null));
    		String body = "{ return "+SpellcraftDamageModifier.class.getName()+".newGetDamageModifier(this); }";
    		Util.setReason("Enable new damage modifier.");
    		Util.setBodyDeclared(thisClass, ctItem, "getDamageModifier", body);
	        //ctItem.getDeclaredMethod("getDamageModifier").setBody("{ return mod.sin.spellcraft.SpellcraftDamageModifier.newGetDamageModifier(this); }");
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	
    public static float newGetDamageModifier(Item item) {
        float rotMod = 1.0f;
        float materialMod = 1.0f;
        if (item.getMaterial() == Materials.MATERIAL_WOOD_OAK) {
        	rotMod = 0.8f;
        } else if (item.getMaterial() == Materials.MATERIAL_WOOD_WILLOW) {
            rotMod = 0.7f;
        } else if (item.getMaterial() == Materials.MATERIAL_STEEL) {
            //rotMod = 0.8f;
        	rotMod = 0.7f;
        } else if (item.getMaterial() == Materials.MATERIAL_GLIMMERSTEEL) {
            rotMod = 0.6f;
        } else if (item.getMaterial() == Materials.MATERIAL_SERYLL){
        	rotMod = 0.6f;
        } else if (item.getMaterial() == Materials.MATERIAL_ADAMANTINE) {
            rotMod = 0.4f;
        }
        if (item.getSpellRotModifier() > 0.0f) {
        	if(item.getMaterial() == Materials.MATERIAL_ADAMANTINE || item.getMaterial() == Materials.MATERIAL_WOOD_PINE){
        		rotMod += item.getSpellRotModifier() / 200.0f;
        	}else{
        		rotMod += item.getSpellRotModifier() / 100.0f;
        	}
        }
        if (item.isCrude()) {
            rotMod *= 10.0f;
        }
        if (item.isCrystal()) {
            rotMod *= 0.1f;
        } else if (item.isFood()) {
            if (item.isHighNutrition()) {
                rotMod += (float)(item.isSalted() ? 5 : 10);
            }
            if (item.isGoodNutrition()) {
                rotMod += (float)(item.isSalted() ? 2 : 5);
            }
            if (item.isMediumNutrition()) {
                rotMod = (float)((double)rotMod + (item.isSalted() ? 1.5 : 3.0));
            }
        } else if (item.getTemplateId() == ItemList.fishingRodIronHook || item.getTemplateId() == ItemList.fishingRodWoodenHook) {
            if (item.getMaterial() == Materials.MATERIAL_WOOD_WILLOW) {
                if (item.getRarity() > 0) {
                    rotMod = (float)((double)rotMod * Math.pow(0.9, item.getRarity()));
                }
                materialMod = 0.7f;
            }
        } else if (item.getMaterial() == Materials.MATERIAL_WOOD_OAK) {
            if (item.getRarity() > 0) {
                rotMod = (float)((double)rotMod * Math.pow(0.9, item.getRarity()));
            }
            materialMod = 0.8f;
        }
        if (item.getRarity() > 0) {
        	float mod = (float) Math.pow(0.9, item.getRarity());
        	if(item.getMaterial() == Materials.MATERIAL_GLIMMERSTEEL || item.getMaterial() == Materials.MATERIAL_WOOD_LINDEN){
        		mod = (float) Math.pow(0.85, item.getRarity());
        	}
        	rotMod *= mod;
            //rotMod = (float)((double)rotMod * Math.pow(0.9, item.getRarity()));
        }
        if (item.getSpellEffects() != null) {
            rotMod *= item.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_DECAY);
        }
        if (item.getBonusForSpellEffect(SpellcraftSpell.HARDEN.getEnchant()) > 0.0f){ // Harden spell
        	float change = (0.3f * item.getBonusForSpellEffect(SpellcraftSpell.HARDEN.getEnchant()) / 100.0f);
        	if(item.getMaterial() == Materials.MATERIAL_SERYLL || item.getMaterial() == Materials.MATERIAL_WOOD_CEDAR){
        		change *= 1.5f;
        	}
        	rotMod *= 1.0f - change;
        }
        float quality = Math.max(1.0f, item.getQualityLevel() * (100.0f - item.getDamage()) / 100.0f);
        if(item.isRepairable() && useNewDamageModifier){
            // new formula
        	return (float) (((1-Math.pow(quality/100, 5))+4*Math.pow(quality/100, 5))/(Math.tan(((quality/100)*(1-Math.pow(quality/100, 5))+(quality/100)*Math.pow(quality/100, 5)*Math.PI/2))))*rotMod*materialMod;
        }else{
        	// old formula
        	return (100.0f * rotMod / Math.max(1.0f, item.getQualityLevel() * (100.0f - item.getDamage()) / 100.0f)) * materialMod;
        }
    }
}
