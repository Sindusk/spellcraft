package mod.sin.spellcraft;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.wurmonline.server.spells.EnchantUtil;
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

	protected static ArrayList<ArrayList<Byte>> enchantGroups = new ArrayList<>();

	public static void addEnchantGroup(String[] split){
	    ArrayList<Byte> newGroup = new ArrayList<>();
	    for(String ench : split){
	        newGroup.add(Byte.valueOf(ench));
        }
        enchantGroups.add(newGroup);
    }
	public static void onServerStarted(){
		if (SpellcraftMod.improvedEnchantGrouping) {
			EnchantUtil.enchantGroups = enchantGroups;
		}
	}
}
