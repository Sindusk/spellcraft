package mod.sin.spellcraft;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.spells.Harden;
import com.wurmonline.server.spells.Phasing;
import com.wurmonline.server.spells.Replenish;
import com.wurmonline.server.spells.SpellHelper;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.spells.SummonSoul;

public class SpellcraftMod
implements WurmServerMod, Configurable, PreInitable, Initable, ServerStartedListener {
    private Logger logger;
    
    // Configuration options
	public boolean bDebug = false;
	public int maximumPlayerFaith = 100;
	public float priestFaithRequirement = 30;
	public boolean hourlyPrayer = true;
	public boolean scalePrayerGains = true;
	public boolean newFavorRegen = true;
	public boolean showCreatureSpellEffects = true;
	public boolean useNewDamageModifier = true;
	public boolean improvedEnchantGrouping = true;
	public boolean statuetteTweaks = true;
	
	// Rite spell changes
	public int riteHolyCropFavorReq = 2000;
	public int riteHolyCropFavorCost = 1000;
	public int riteDeathFavorReq = 2000;
	public int riteDeathFavorCost = 1000;
	public int riteSpringFavorReq = 2000;
	public int riteSpringFavorCost = 1000;
	public int riteSunFavorReq = 2000;
	public int riteSunFavorCost = 1000;
	
	public boolean riteHolyCropMassGenesis = true;
	public int riteHolyCropGenesisChance = 5;
	public int riteSpringPlayersRequired = 5;
	
	// Default spell tweak options
	public boolean scornHealWithoutDamage = true;
	
	// Custom spell enable toggles
	public boolean spellEnableHarden = true;
	public boolean spellEnablePhasing = true;
	public boolean spellEnableReplenish = true;
	public boolean spellEnableSummonSoul = true;
	
	// Custom spell god id's
	public List<String> hardenGods;
	public List<String> phasingGods;
	public List<String> replenishGods;
	public List<String> summonSoulGods;
	
	// Custom spell options
	//Harden
	public int hardenCastTime = 20;
	public int hardenCost = 30;
	public int hardenDifficulty = 60;
	public int hardenFaith = 40;
	public long hardenCooldown = 0;
	//Phasing
	public int phasingCastTime = 20;
	public int phasingCost = 30;
	public int phasingDifficulty = 60;
	public int phasingFaith = 50;
	public long phasingCooldown = 0;
	public float phasingPowerMultiplier = 0.5f;
	//Replenish
	public int replenishCastTime = 20;
	public int replenishCost = 40;
	public int replenishDifficulty = 60;
	public int replenishFaith = 60;
	public long replenishCooldown = 3600000;
	//Summon Soul
	public int summonSoulCastTime = 300;
	public int summonSoulCost = 100;
	public int summonSoulDifficulty = 10;
	public int summonSoulFaith = 80;
	public long summonSoulCooldown = 0;
	
	// Default spell modifications:
	public HashMap<String, Integer> spellCastTimes = new HashMap<String, Integer>();
	public HashMap<String, Integer> spellCosts = new HashMap<String, Integer>();
	public HashMap<String, Integer> spellDifficulties = new HashMap<String, Integer>();
	public HashMap<String, Integer> spellFaithRequirements = new HashMap<String, Integer>();
	public HashMap<String, Long> spellCooldowns = new HashMap<String, Long>();
	
	// Deity spell additions and removals:
	public HashMap<String, List<String>> addSpells = new HashMap<String, List<String>>();
	public HashMap<String, List<String>> removeSpells = new HashMap<String, List<String>>();
	
	public SpellcraftMod(){
		this.logger = Logger.getLogger(this.getClass().getName());
	}

	@Override
	public void configure(Properties properties) {
		this.logger.info("Beginning configuration...");
		// Base configuration options
        this.bDebug = Boolean.parseBoolean(properties.getProperty("debug", Boolean.toString(this.bDebug)));
        this.maximumPlayerFaith = Integer.parseInt(properties.getProperty("maximumPlayerFaith", Integer.toString(this.maximumPlayerFaith)));
        this.priestFaithRequirement = Float.parseFloat(properties.getProperty("priestFaithRequirement", Float.toString(this.priestFaithRequirement)));
        this.hourlyPrayer = Boolean.parseBoolean(properties.getProperty("hourlyPrayer", Boolean.toString(this.hourlyPrayer)));
        this.scalePrayerGains = Boolean.parseBoolean(properties.getProperty("scalePrayerGains", Boolean.toString(this.scalePrayerGains)));
        this.newFavorRegen = Boolean.parseBoolean(properties.getProperty("newFavorRegen", Boolean.toString(this.newFavorRegen)));
        this.showCreatureSpellEffects = Boolean.parseBoolean(properties.getProperty("showCreatureSpellEffects", Boolean.toString(this.showCreatureSpellEffects)));
        this.useNewDamageModifier = Boolean.parseBoolean(properties.getProperty("useNewDamageModifier", Boolean.toString(this.useNewDamageModifier)));
        this.improvedEnchantGrouping = Boolean.parseBoolean(properties.getProperty("improvedEnchantGrouping", Boolean.toString(this.improvedEnchantGrouping)));
        // Statuette tweaks
        this.statuetteTweaks = Boolean.parseBoolean(properties.getProperty("statuetteTweaks", Boolean.toString(this.statuetteTweaks)));
        SpellHelper.statuetteRarityPowerIncrease = Float.valueOf(properties.getProperty("statuetteRarityPowerIncrease", Float.toString(SpellHelper.statuetteRarityPowerIncrease)));
        SpellHelper.statuetteQualityBonusMod = Float.valueOf(properties.getProperty("statuetteQualityBonusMod", Float.toString(SpellHelper.statuetteQualityBonusMod)));
        // Rite changes
        this.riteHolyCropFavorReq = Integer.parseInt(properties.getProperty("riteHolyCropFavorReq", Integer.toString(this.riteHolyCropFavorReq)));
        this.riteHolyCropFavorCost = Integer.parseInt(properties.getProperty("riteHolyCropFavorCost", Integer.toString(this.riteHolyCropFavorCost)));
        this.riteDeathFavorReq = Integer.parseInt(properties.getProperty("riteDeathFavorReq", Integer.toString(this.riteDeathFavorReq)));
        this.riteDeathFavorCost = Integer.parseInt(properties.getProperty("riteDeathFavorCost", Integer.toString(this.riteDeathFavorCost)));
        this.riteSpringFavorReq = Integer.parseInt(properties.getProperty("riteSpringFavorReq", Integer.toString(this.riteSpringFavorReq)));
        this.riteSpringFavorCost = Integer.parseInt(properties.getProperty("riteSpringFavorCost", Integer.toString(this.riteSpringFavorCost)));
        this.riteSunFavorReq = Integer.parseInt(properties.getProperty("riteSunFavorReq", Integer.toString(this.riteSunFavorReq)));
        this.riteSunFavorCost = Integer.parseInt(properties.getProperty("riteSunFavorCost", Integer.toString(this.riteSunFavorCost)));
        // Rite special effects
        this.riteHolyCropMassGenesis = Boolean.parseBoolean(properties.getProperty("riteHolyCropMassGenesis", Boolean.toString(this.riteHolyCropMassGenesis)));
        this.riteHolyCropGenesisChance = Integer.parseInt(properties.getProperty("riteHolyCropGenesisChance", Integer.toString(this.riteHolyCropGenesisChance)));
        this.riteSpringPlayersRequired = Integer.parseInt(properties.getProperty("riteSpringPlayersRequired", Integer.toString(this.riteSpringPlayersRequired)));
        // Default spell tweaks
        this.scornHealWithoutDamage = Boolean.parseBoolean(properties.getProperty("scornHealWithoutDamage", Boolean.toString(this.scornHealWithoutDamage)));
        // Spell enable/disable
        this.spellEnableHarden = Boolean.parseBoolean(properties.getProperty("spellEnableHarden", Boolean.toString(this.spellEnableHarden)));
        this.spellEnablePhasing = Boolean.parseBoolean(properties.getProperty("spellEnablePhasing", Boolean.toString(this.spellEnablePhasing)));
        this.spellEnableReplenish = Boolean.parseBoolean(properties.getProperty("spellEnableReplenish", Boolean.toString(this.spellEnableReplenish)));
        this.spellEnableSummonSoul = Boolean.parseBoolean(properties.getProperty("spellEnableSummonSoul", Boolean.toString(this.spellEnableSummonSoul)));
        // Spell god id's
        hardenGods = Arrays.asList(properties.getProperty("hardenGods", "-1").split(","));
        phasingGods = Arrays.asList(properties.getProperty("phasingGods", "-1").split(","));
        replenishGods = Arrays.asList(properties.getProperty("replenishGods", "-1").split(","));
        summonSoulGods = Arrays.asList(properties.getProperty("phasingGods", "-1").split(","));
        // Spell options
        this.hardenCastTime = Integer.parseInt(properties.getProperty("hardenCastTime", Integer.toString(this.hardenCastTime)));
        this.hardenCost = Integer.parseInt(properties.getProperty("hardenCost", Integer.toString(this.hardenCost)));
        this.hardenDifficulty = Integer.parseInt(properties.getProperty("hardenDifficulty", Integer.toString(this.hardenDifficulty)));
        this.hardenFaith = Integer.parseInt(properties.getProperty("hardenFaith", Integer.toString(this.hardenFaith)));
        this.hardenCooldown = Long.parseLong(properties.getProperty("hardenCooldown", Long.toString(this.hardenCooldown)));
        this.phasingCastTime = Integer.parseInt(properties.getProperty("phasingCastTime", Integer.toString(this.phasingCastTime)));
        this.phasingCost = Integer.parseInt(properties.getProperty("phasingCost", Integer.toString(this.phasingCost)));
        this.phasingDifficulty = Integer.parseInt(properties.getProperty("phasingDifficulty", Integer.toString(this.phasingDifficulty)));
        this.phasingFaith = Integer.parseInt(properties.getProperty("phasingFaith", Integer.toString(this.phasingFaith)));
        this.phasingCooldown = Long.parseLong(properties.getProperty("phasingCooldown", Long.toString(this.phasingCooldown)));
        this.phasingPowerMultiplier = Float.parseFloat(properties.getProperty("phasingPowerMultiplier", Float.toString(this.phasingPowerMultiplier)));
        this.replenishCastTime = Integer.parseInt(properties.getProperty("replenishCastTime", Integer.toString(this.replenishCastTime)));
        this.replenishCost = Integer.parseInt(properties.getProperty("replenishCost", Integer.toString(this.replenishCost)));
        this.replenishDifficulty = Integer.parseInt(properties.getProperty("replenishDifficulty", Integer.toString(this.replenishDifficulty)));
        this.replenishFaith = Integer.parseInt(properties.getProperty("replenishFaith", Integer.toString(this.replenishFaith)));
        this.replenishCooldown = Long.parseLong(properties.getProperty("replenishCooldown", Long.toString(this.replenishCooldown)));
        this.summonSoulCastTime = Integer.parseInt(properties.getProperty("summonSoulCastTime", Integer.toString(this.summonSoulCastTime)));
        this.summonSoulCost = Integer.parseInt(properties.getProperty("summonSoulCost", Integer.toString(this.summonSoulCost)));
        this.summonSoulDifficulty = Integer.parseInt(properties.getProperty("summonSoulDifficulty", Integer.toString(this.summonSoulDifficulty)));
        this.summonSoulFaith = Integer.parseInt(properties.getProperty("summonSoulFaith", Integer.toString(this.summonSoulFaith)));
        this.summonSoulCooldown = Long.parseLong(properties.getProperty("summonSoulCooldown", Long.toString(this.summonSoulCooldown)));
        // Default spell modifications:
        for (String name : properties.stringPropertyNames()) {
            try {
                String value = properties.getProperty(name);
                switch (name) {
                    case "debug":
                    case "classname":
                    case "classpath":
                    case "sharedClassLoader":
                        break; //ignore
                    default:
                    	if (name.startsWith("casttime")) {
                        	String[] split = value.split(",");
                            String spellname = split[0];
                            int newVal = Integer.parseInt(split[1]);
                            spellCastTimes.put(spellname, newVal);
                    	} else if (name.startsWith("cost")) {
                            String[] split = value.split(",");
                            String spellname = split[0];
                            int newVal = Integer.parseInt(split[1]);
                            spellCosts.put(spellname, newVal);
                        } else if (name.startsWith("difficulty")) {
                        	String[] split = value.split(",");
                            String spellname = split[0];
                            int newVal = Integer.parseInt(split[1]);
                            spellDifficulties.put(spellname, newVal);
                        } else if (name.startsWith("faith")) {
                        	String[] split = value.split(",");
                            String spellname = split[0];
                            int newVal = Integer.parseInt(split[1]);
                            spellFaithRequirements.put(spellname, newVal);
                        } else if (name.startsWith("cooldown")) {
                        	String[] split = value.split(",");
                            String spellname = split[0];
                            long newVal = Long.parseLong(split[1]);
                            spellCooldowns.put(spellname, newVal);
                        } else if (name.startsWith("addspell")) {
                        	String[] split = value.split(";");
                            String spellname = split[0];
                            List<String> deityList = Arrays.asList(split[1].split(","));
                            addSpells.put(spellname, deityList);
                        } else if (name.startsWith("removespell")) {
                        	String[] split = value.split(";");
                            String spellname = split[0];
                            List<String> deityList = Arrays.asList(split[1].split(","));
                            removeSpells.put(spellname, deityList);
                        } else {
                            //Debug("Unknown config property: " + name);
                        }
                }
            } catch (Exception e) {
                Debug("Error processing property " + name);
                e.printStackTrace();
            }
        }
        
        try {
            String logsPath = Paths.get("mods", new String[0]) + "/logs/";
            File newDirectory = new File(logsPath);
            if (!newDirectory.exists()) {
                newDirectory.mkdirs();
            }
            FileHandler fh = new FileHandler(String.valueOf(String.valueOf(logsPath)) + this.getClass().getSimpleName() + ".log", 10240000, 200, true);
            if (this.bDebug) {
                fh.setLevel(Level.INFO);
            } else {
                fh.setLevel(Level.WARNING);
            }
            fh.setFormatter(new SimpleFormatter());
            this.logger.addHandler(fh);
        }
        catch (IOException ie) {
            System.err.println(String.valueOf(this.getClass().getName()) + ": Unable to add file handler to logger");
        }
        // Print values of mod configuration
        this.logger.info(" -- Mod Configuration -- ");
        this.logger.log(Level.INFO, "maximumPlayerFaith: " + this.maximumPlayerFaith);
        this.logger.log(Level.INFO, "priestFaithRequirement: " + this.priestFaithRequirement);
        logger.info("hourlyPrayer: " + this.hourlyPrayer);
        this.logger.log(Level.INFO, "scalePrayerGains: " + this.scalePrayerGains);
        this.logger.log(Level.INFO, "newFavorRegen: " + this.newFavorRegen);
        this.logger.log(Level.INFO, "showCreatureSpellEffects: " + this.showCreatureSpellEffects);
        this.logger.log(Level.INFO, "useNewDamageModifier: " + this.useNewDamageModifier);
        logger.info("Statuette Tweaks: "+statuetteTweaks);
        if(statuetteTweaks){
        	logger.info("Statuette bonus per QL: "+SpellHelper.statuetteQualityBonusMod);
        	logger.info("Statuette power per rarity: "+SpellHelper.statuetteRarityPowerIncrease);
        }
        this.logger.info(" -- Rite changes -- ");
        this.logger.log(Level.INFO, "riteHolyCropFavorReq: " + this.riteHolyCropFavorReq);
        this.logger.log(Level.INFO, "riteHolyCropFavorCost: " + this.riteHolyCropFavorCost);
        this.logger.log(Level.INFO, "riteDeathFavorReq: " + this.riteDeathFavorReq);
        this.logger.log(Level.INFO, "riteDeathFavorCost: " + this.riteDeathFavorCost);
        this.logger.log(Level.INFO, "riteSpringFavorReq: " + this.riteSpringFavorReq);
        this.logger.log(Level.INFO, "riteSpringFavorCost: " + this.riteSpringFavorCost);
        this.logger.log(Level.INFO, "riteSunFavorReq: " + this.riteSpringFavorReq);
        this.logger.log(Level.INFO, "riteSunFavorCost: " + this.riteSpringFavorCost);
        this.logger.log(Level.INFO, "riteHolyCropMassGenesis: " + this.riteHolyCropMassGenesis);
        this.logger.log(Level.INFO, "riteHolyCropGenesisChance: " + this.riteHolyCropGenesisChance);
        this.logger.log(Level.INFO, "riteSpringPlayersRequired: " + this.riteSpringPlayersRequired);
        this.logger.info(" -- Default spell tweaks -- ");
        this.logger.log(Level.INFO, "scornHealWithoutDamage: " + this.scornHealWithoutDamage);
        this.logger.info(" -- Custom Spell Configuration -- ");
        this.logger.log(Level.INFO, "spellEnableHarden: " + this.spellEnableHarden);
        this.logger.log(Level.INFO, "spellEnablePhasing: " + this.spellEnablePhasing);
        this.logger.log(Level.INFO, "spellEnableSummonSoul: " + this.spellEnableSummonSoul);
        if(spellEnableSummonSoul){
	        this.logger.log(Level.INFO, "hardenGods: " + this.hardenGods);
	        this.logger.log(Level.INFO, "hardenCastTime: " + this.hardenCastTime);
	        this.logger.log(Level.INFO, "hardenCost: " + this.hardenCost);
	        this.logger.log(Level.INFO, "hardenDifficulty: " + this.hardenDifficulty);
	        this.logger.log(Level.INFO, "hardenFaith: " + this.hardenFaith);
	        this.logger.log(Level.INFO, "hardenCooldown: " + this.hardenCooldown);
        }
        if(spellEnablePhasing){
	        this.logger.log(Level.INFO, "phasingGods: " + this.phasingGods);
	        this.logger.log(Level.INFO, "phasingCastTime: " + this.phasingCastTime);
	        this.logger.log(Level.INFO, "phasingCost: " + this.phasingCost);
	        this.logger.log(Level.INFO, "phasingDifficulty: " + this.phasingDifficulty);
	        this.logger.log(Level.INFO, "phasingFaith: " + this.phasingFaith);
	        this.logger.log(Level.INFO, "phasingCooldown: " + this.phasingCooldown);
	        this.logger.log(Level.INFO, "phasingPowerMultiplier: " + this.phasingPowerMultiplier);
        }
		if(spellEnableReplenish){
	        this.logger.log(Level.INFO, "replenishGods: " + this.replenishGods);
	        this.logger.log(Level.INFO, "replenishCastTime: " + this.replenishCastTime);
	        this.logger.log(Level.INFO, "replenishCost: " + this.replenishCost);
	        this.logger.log(Level.INFO, "replenishDifficulty: " + this.replenishDifficulty);
	        this.logger.log(Level.INFO, "replenishFaith: " + this.replenishFaith);
	        this.logger.log(Level.INFO, "replenishCooldown: " + this.replenishCooldown);
        }
        if(spellEnableSummonSoul){
	        this.logger.log(Level.INFO, "summonSoulGods: " + this.summonSoulGods);
	        this.logger.log(Level.INFO, "summonSoulCastTime: " + this.summonSoulCastTime);
	        this.logger.log(Level.INFO, "summonSoulCost: " + this.summonSoulCost);
	        this.logger.log(Level.INFO, "summonSoulDifficulty: " + this.summonSoulDifficulty);
	        this.logger.log(Level.INFO, "summonSoulFaith: " + this.summonSoulFaith);
	        this.logger.log(Level.INFO, "summonSoulCooldown: " + this.summonSoulCooldown);
        }
        this.logger.info(" -- Default spell modifications -- ");
        for(String spellname : spellCastTimes.keySet()){
        	this.logger.info(spellname+": cast time set to "+spellCastTimes.get(spellname));
        }
        for(String spellname : spellCosts.keySet()){
        	this.logger.info(spellname+": cost set to "+spellCosts.get(spellname));
        }
        for(String spellname : spellDifficulties.keySet()){
        	this.logger.info(spellname+": difficulty set to "+spellDifficulties.get(spellname));
        }
        for(String spellname : spellFaithRequirements.keySet()){
        	this.logger.info(spellname+": faith requirement set to "+spellFaithRequirements.get(spellname));
        }
        for(String spellname : spellCooldowns.keySet()){
        	this.logger.info(spellname+": cooldown set to "+spellCooldowns.get(spellname));
        }
        for(String spellname : addSpells.keySet()){
        	this.logger.info(spellname+": Adding to gods "+addSpells.get(spellname));
        }
        for(String spellname : removeSpells.keySet()){
        	this.logger.info(spellname+": Removing from gods "+removeSpells.get(spellname));
        }
        this.Debug("Debugging messages are enabled.");
        this.logger.info(" -- Configuration complete -- ");
    }
	
	public void onServerStarted(){
		SpellcraftSpellModifications.onServerStarted(this);
		new Runnable(){
			@Override
			public void run(){
				try{
					Harden harden = new Harden(hardenCastTime, hardenCost, hardenDifficulty, hardenFaith, hardenCooldown);
					Phasing phasing = new Phasing(phasingCastTime, phasingCost, phasingDifficulty, phasingFaith, phasingCooldown);
					Replenish replenish = new Replenish(replenishCastTime, replenishCost, replenishDifficulty, replenishFaith, replenishCooldown);
					SummonSoul summonSoul = new SummonSoul(summonSoulCastTime, summonSoulCost, summonSoulDifficulty, summonSoulFaith, summonSoulCooldown);
					// Add each spell to Spells
					ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), harden);
					ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), phasing);
					ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), replenish);
					ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), summonSoul);
					// Add spells to their proper deities:
					for(Deity deity : Deities.getDeities()){
						if(spellEnableHarden && (hardenGods.contains("-1") || hardenGods.contains(String.valueOf(deity.getNumber())))){
							deity.addSpell(harden);
							Debug("Adding spell "+harden.name+" to "+deity.getName());
						}
						if(spellEnablePhasing && (phasingGods.contains("-1") || phasingGods.contains(String.valueOf(deity.getNumber())))){
							deity.addSpell(phasing);
							Debug("Adding spell "+phasing.name+" to "+deity.getName());
						}
						if(spellEnableReplenish && (replenishGods.contains("-1") || replenishGods.contains(String.valueOf(deity.getNumber())))){
							deity.addSpell(replenish);
							Debug("Adding spell "+replenish.name+" to "+deity.getName());
						}
						if(spellEnableSummonSoul && (summonSoulGods.contains("-1") || summonSoulGods.contains(String.valueOf(deity.getNumber())))){
							deity.addSpell(summonSoul);
							Debug("Adding spell "+summonSoul.name+" to "+deity.getName());
						}
					}
				} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
		        }
			}
		}.run();
	}
    
	protected void Debug(String x) {
        if (this.bDebug) {
            System.out.println(String.valueOf(this.getClass().getSimpleName()) + ": " + x);
            System.out.flush();
            this.logger.log(Level.INFO, x);
        }
    }
	
	@Override
	public void preInit() {
		ModActions.init();
		SpellcraftTweaks.preInit(this);
		SpellcraftSpellModifications.preInit(this);
		SpellcraftSpellEffects.preInit(this);
		SpellcraftCustomSpells.preInit(this);
		if(useNewDamageModifier || spellEnableHarden){ // Don't need to edit the damage modifier unless we're using harden or new formula
			SpellcraftDamageModifier.preInit(this);
		}
		if(statuetteTweaks){
			SpellcraftStatuetteTweaks.patchSpellClass();
            SpellcraftStatuetteTweaks.patchCastingCalls();
		}
	}
	
	@Override
	public void init(){
		SpellcraftSpellModifications.init(this);
	}

}
