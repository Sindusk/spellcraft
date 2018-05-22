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

import com.wurmonline.server.spells.*;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;

public class SpellcraftMod
implements WurmServerMod, Configurable, PreInitable, Initable, ServerPollListener, ServerStartedListener {
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
    public static boolean scornHealWithoutDamage = true;
    public static boolean reduceScornHealingDone = true;
    public static boolean useRecodedSmite = true;
    public static boolean increaseFranticChargeDuration = true;
    public static boolean healingRedone = true;
	
	// Custom spell enable toggles
	/*public boolean spellEnableHarden = true;
	public boolean spellEnablePhasing = true;
	public boolean spellEnableReplenish = true;
    public boolean spellEnableSummonSoul = true;
    public boolean spellEnableExpand = true;
    public boolean spellEnableEfficiency = true;
    public boolean spellEnableQuarry = true;

    // Custom spell god id's
	public List<String> hardenGods;
	public List<String> phasingGods;
	public List<String> replenishGods;
    public List<String> summonSoulGods;
    public List<String> expandGods;
    public List<String> efficiencyGods;
    public List<String> quarryGods;*/
	
	// Custom spell options
    public float phasingPowerMultiplier = 0.5f;
    public float expandEffectModifier = 4;
    public static float efficiencyDifficultyPerPower = 0.05f;
    public static float quarryEffectiveness = 0.05f;
    public static float prowessEffectiveness = 0.01f;
    public static float industryEffectiveness = 0.01f;
    public static float enduranceEffectiveness = 0.001f;
    public static float acuityEffectiveness = 0.001f;
    public static float titanforgedMultiplier = 0.5f;

	//Harden
	/*public int hardenCastTime = 20;
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
    //Expand
    public int expandCastTime = 30;
    public int expandCost = 40;
    public int expandDifficulty = 60;
    public int expandFaith = 40;
    public long expandCooldown = 3600000;
    public float expandEffectModifier = 4;
    // Efficiency
    public int efficiencyCastTime = 30;
    public int efficiencyCost = 80;
    public int efficiencyDifficulty = 90;
    public int efficiencyFaith = 100;
    public long efficiencyCooldown = 600000;
    public static float efficiencyDifficultyPerPower = 0.05f;
    // Quarry
    public int quarryCastTime = 30;
    public int quarryCost = 40;
    public int quarryDifficulty = 60;
    public int quarryFaith = 50;
    public long quarryCooldown = 3600000;
    public static float quarryEffectiveness = 0.05f;*/

	// Default spell modifications:
	public HashMap<String, Integer> spellCastTimes = new HashMap<>();
	public HashMap<String, Integer> spellCosts = new HashMap<>();
	public HashMap<String, Integer> spellDifficulties = new HashMap<>();
	public HashMap<String, Integer> spellFaithRequirements = new HashMap<>();
	public HashMap<String, Long> spellCooldowns = new HashMap<>();
	
	// Deity spell additions and removals:
	public HashMap<String, List<String>> addSpells = new HashMap<>();
	public HashMap<String, List<String>> removeSpells = new HashMap<>();
	
	public SpellcraftMod(){
		this.logger = Logger.getLogger(this.getClass().getName());
	}

	protected boolean getBooleanProperty(Properties properties, String field, boolean def){
	    return Boolean.parseBoolean(properties.getProperty(field, Boolean.toString(def)));
    }
    public String getStringProperty(Properties properties, String field, String def){
	    return properties.getProperty(field, def);
    }
    public int getIntegerProperty(Properties properties, String field, int def){
        return Integer.parseInt(properties.getProperty(field, Integer.toString(def)));
    }
    public long getLongProperty(Properties properties, String field, long def){
        return Long.parseLong(properties.getProperty(field, Long.toString(def)));
    }
    public float getFloatProperty(Properties properties, String field, float def){
        return Float.parseFloat(properties.getProperty(field, Float.toString(def)));
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
        scornHealWithoutDamage = Boolean.parseBoolean(properties.getProperty("scornHealWithoutDamage", Boolean.toString(scornHealWithoutDamage)));
        reduceScornHealingDone = Boolean.parseBoolean(properties.getProperty("reduceScornHealingDone", Boolean.toString(reduceScornHealingDone)));
        useRecodedSmite = Boolean.parseBoolean(properties.getProperty("useRecodedSmite", Boolean.toString(useRecodedSmite)));
        increaseFranticChargeDuration = Boolean.parseBoolean(properties.getProperty("increaseFranticChargeDuration", Boolean.toString(increaseFranticChargeDuration)));
        healingRedone = Boolean.parseBoolean(properties.getProperty("healingRedone", Boolean.toString(healingRedone)));
        // Spell enable/disable
        for(SpellcraftSpell spell : SpellcraftSpell.values()){
            spell.setEnabled(getBooleanProperty(properties, "spellEnable"+spell.getName().replaceAll(" ", ""), true));
            spell.setGods(getStringProperty(properties, spell.getPropertyName()+"Gods", "-1"));
            spell.setCastTime(getIntegerProperty(properties, spell.getPropertyName()+"CastTime", 30));
            spell.setCost(getIntegerProperty(properties, spell.getPropertyName()+"Cost", 50));
            spell.setDifficulty(getIntegerProperty(properties, spell.getPropertyName()+"Difficulty", 50));
            spell.setFaith(getIntegerProperty(properties, spell.getPropertyName()+"Faith", 50));
            spell.setCooldown(getLongProperty(properties, spell.getPropertyName()+"Cooldown", 0));
        }
        phasingPowerMultiplier = Float.parseFloat(properties.getProperty("phasingPowerMultiplier", Float.toString(phasingPowerMultiplier)));
        expandEffectModifier = Float.parseFloat(properties.getProperty("expandEffectModifier", Float.toString(expandEffectModifier)));
        efficiencyDifficultyPerPower = Float.parseFloat(properties.getProperty("efficiencyDifficultyPerPower", Float.toString(efficiencyDifficultyPerPower)));
        quarryEffectiveness = Float.parseFloat(properties.getProperty("quarryEffectiveness", Float.toString(quarryEffectiveness)));
        prowessEffectiveness = getFloatProperty(properties, "prowessEffectiveness", prowessEffectiveness);
        industryEffectiveness = getFloatProperty(properties, "industryEffectiveness", industryEffectiveness);
        enduranceEffectiveness = getFloatProperty(properties, "enduranceEffectiveness", enduranceEffectiveness);
        acuityEffectiveness = getFloatProperty(properties, "acuityEffectiveness", acuityEffectiveness);
        titanforgedMultiplier = getFloatProperty(properties, "titanforgedMultiplier", titanforgedMultiplier);

        /*this.spellEnableHarden = Boolean.parseBoolean(properties.getProperty("spellEnableHarden", Boolean.toString(this.spellEnableHarden)));
        this.spellEnablePhasing = Boolean.parseBoolean(properties.getProperty("spellEnablePhasing", Boolean.toString(this.spellEnablePhasing)));
        this.spellEnableReplenish = Boolean.parseBoolean(properties.getProperty("spellEnableReplenish", Boolean.toString(this.spellEnableReplenish)));
        this.spellEnableSummonSoul = Boolean.parseBoolean(properties.getProperty("spellEnableSummonSoul", Boolean.toString(this.spellEnableSummonSoul)));
        this.spellEnableExpand = Boolean.parseBoolean(properties.getProperty("spellEnableExpand", Boolean.toString(this.spellEnableExpand)));
        this.spellEnableEfficiency = Boolean.parseBoolean(properties.getProperty("spellEnableEfficiency", Boolean.toString(this.spellEnableEfficiency)));
        this.spellEnableQuarry = Boolean.parseBoolean(properties.getProperty("spellEnableQuarry", Boolean.toString(this.spellEnableQuarry)));
        // Spell god id's
        hardenGods = Arrays.asList(properties.getProperty("hardenGods", "-1").split(","));
        phasingGods = Arrays.asList(properties.getProperty("phasingGods", "-1").split(","));
        replenishGods = Arrays.asList(properties.getProperty("replenishGods", "-1").split(","));
        summonSoulGods = Arrays.asList(properties.getProperty("summonSoulGods", "-1").split(","));
        expandGods = Arrays.asList(properties.getProperty("expandGods", "-1").split(","));
        efficiencyGods = Arrays.asList(properties.getProperty("efficiencyGods", "-1").split(","));
        quarryGods = Arrays.asList(properties.getProperty("quarryGods", "-1").split(","));*/
        // Spell options
        // Harden
        /*this.hardenCastTime = Integer.parseInt(properties.getProperty("hardenCastTime", Integer.toString(this.hardenCastTime)));
        this.hardenCost = Integer.parseInt(properties.getProperty("hardenCost", Integer.toString(this.hardenCost)));
        this.hardenDifficulty = Integer.parseInt(properties.getProperty("hardenDifficulty", Integer.toString(this.hardenDifficulty)));
        this.hardenFaith = Integer.parseInt(properties.getProperty("hardenFaith", Integer.toString(this.hardenFaith)));
        this.hardenCooldown = Long.parseLong(properties.getProperty("hardenCooldown", Long.toString(this.hardenCooldown)));
        // Phasing
        this.phasingCastTime = Integer.parseInt(properties.getProperty("phasingCastTime", Integer.toString(this.phasingCastTime)));
        this.phasingCost = Integer.parseInt(properties.getProperty("phasingCost", Integer.toString(this.phasingCost)));
        this.phasingDifficulty = Integer.parseInt(properties.getProperty("phasingDifficulty", Integer.toString(this.phasingDifficulty)));
        this.phasingFaith = Integer.parseInt(properties.getProperty("phasingFaith", Integer.toString(this.phasingFaith)));
        this.phasingCooldown = Long.parseLong(properties.getProperty("phasingCooldown", Long.toString(this.phasingCooldown)));
        this.phasingPowerMultiplier = Float.parseFloat(properties.getProperty("phasingPowerMultiplier", Float.toString(this.phasingPowerMultiplier)));
        // Replenish
        this.replenishCastTime = Integer.parseInt(properties.getProperty("replenishCastTime", Integer.toString(this.replenishCastTime)));
        this.replenishCost = Integer.parseInt(properties.getProperty("replenishCost", Integer.toString(this.replenishCost)));
        this.replenishDifficulty = Integer.parseInt(properties.getProperty("replenishDifficulty", Integer.toString(this.replenishDifficulty)));
        this.replenishFaith = Integer.parseInt(properties.getProperty("replenishFaith", Integer.toString(this.replenishFaith)));
        this.replenishCooldown = Long.parseLong(properties.getProperty("replenishCooldown", Long.toString(this.replenishCooldown)));
        // Summon Soul
        this.summonSoulCastTime = Integer.parseInt(properties.getProperty("summonSoulCastTime", Integer.toString(this.summonSoulCastTime)));
        this.summonSoulCost = Integer.parseInt(properties.getProperty("summonSoulCost", Integer.toString(this.summonSoulCost)));
        this.summonSoulDifficulty = Integer.parseInt(properties.getProperty("summonSoulDifficulty", Integer.toString(this.summonSoulDifficulty)));
        this.summonSoulFaith = Integer.parseInt(properties.getProperty("summonSoulFaith", Integer.toString(this.summonSoulFaith)));
        this.summonSoulCooldown = Long.parseLong(properties.getProperty("summonSoulCooldown", Long.toString(this.summonSoulCooldown)));
        // Expand
        this.expandCastTime = Integer.parseInt(properties.getProperty("expandCastTime", Integer.toString(this.expandCastTime)));
        this.expandCost = Integer.parseInt(properties.getProperty("expandCost", Integer.toString(this.expandCost)));
        this.expandDifficulty = Integer.parseInt(properties.getProperty("expandDifficulty", Integer.toString(this.expandDifficulty)));
        this.expandFaith = Integer.parseInt(properties.getProperty("expandFaith", Integer.toString(this.expandFaith)));
        this.expandCooldown = Long.parseLong(properties.getProperty("expandCooldown", Long.toString(this.expandCooldown)));
        this.expandEffectModifier = Float.parseFloat(properties.getProperty("expandEffectModifier", Float.toString(this.expandEffectModifier)));
        // Efficiency
        this.efficiencyCastTime = Integer.parseInt(properties.getProperty("efficiencyCastTime", Integer.toString(this.efficiencyCastTime)));
        this.efficiencyCost = Integer.parseInt(properties.getProperty("efficiencyCost", Integer.toString(this.efficiencyCost)));
        this.efficiencyDifficulty = Integer.parseInt(properties.getProperty("efficiencyDifficulty", Integer.toString(this.efficiencyDifficulty)));
        this.efficiencyFaith = Integer.parseInt(properties.getProperty("efficiencyFaith", Integer.toString(this.efficiencyFaith)));
        this.efficiencyCooldown = Long.parseLong(properties.getProperty("efficiencyCooldown", Long.toString(this.efficiencyCooldown)));
        efficiencyDifficultyPerPower = Float.parseFloat(properties.getProperty("efficiencyDifficultyPerPower", Float.toString(efficiencyDifficultyPerPower)));
        // Quarry
        this.quarryCastTime = Integer.parseInt(properties.getProperty("quarryCastTime", Integer.toString(this.quarryCastTime)));
        this.quarryCost = Integer.parseInt(properties.getProperty("quarryCost", Integer.toString(this.quarryCost)));
        this.quarryDifficulty = Integer.parseInt(properties.getProperty("quarryDifficulty", Integer.toString(this.quarryDifficulty)));
        this.quarryFaith = Integer.parseInt(properties.getProperty("quarryFaith", Integer.toString(this.quarryFaith)));
        this.quarryCooldown = Long.parseLong(properties.getProperty("quarryCooldown", Long.toString(this.quarryCooldown)));
        quarryEffectiveness = Float.parseFloat(properties.getProperty("quarryEffectiveness", Float.toString(quarryEffectiveness)));*/
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
                        } else if (name.startsWith("enchantGroup")) {
                            String[] split = value.split(",");
                            SpellcraftSpellEffects.addEnchantGroup(split);
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
        this.logger.log(Level.INFO, "scornHealWithoutDamage: " + scornHealWithoutDamage);
        this.logger.log(Level.INFO, "reduceScornHealingDone: " + reduceScornHealingDone);
        this.logger.log(Level.INFO, "useRecodedSmite: " + useRecodedSmite);
        this.logger.log(Level.INFO, "increaseFranticChargeDuration: " + increaseFranticChargeDuration);
        this.logger.log(Level.INFO, "healingRedone: " + healingRedone);
        this.logger.info(" -- Custom Spell Configuration -- ");
        for(SpellcraftSpell spell : SpellcraftSpell.values()){
            logger.info(spell.getName()+" enabled: "+spell.isEnabled());
            logger.info(spell.getName()+" gods: "+spell.getGods());
            logger.info(spell.getName()+" cast time: "+spell.getCastTime());
            logger.info(spell.getName()+" cost: "+spell.getCost());
            logger.info(spell.getName()+" difficulty: "+spell.getDifficulty());
            logger.info(spell.getName()+" faith: "+spell.getFaith());
            logger.info(spell.getName()+" cooldown: "+spell.getCooldown());
        }
        /*this.logger.log(Level.INFO, "spellEnableHarden: " + this.spellEnableHarden);
        this.logger.log(Level.INFO, "spellEnablePhasing: " + this.spellEnablePhasing);
        this.logger.log(Level.INFO, "spellEnableSummonSoul: " + this.spellEnableSummonSoul);
        this.logger.log(Level.INFO, "spellEnableReplenish: " + this.spellEnableReplenish);
        this.logger.log(Level.INFO, "spellEnableExpand: " + this.spellEnableExpand);
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
        if(spellEnableExpand){
            this.logger.log(Level.INFO, "expandGods: " + this.expandGods);
            this.logger.log(Level.INFO, "expandCastTime: " + this.expandCastTime);
            this.logger.log(Level.INFO, "expandCost: " + this.expandCost);
            this.logger.log(Level.INFO, "expandDifficulty: " + this.expandDifficulty);
            this.logger.log(Level.INFO, "expandFaith: " + this.expandFaith);
            this.logger.log(Level.INFO, "expandCooldown: " + this.expandCooldown);
            this.logger.log(Level.INFO, "expandEffectModifier: " + this.expandEffectModifier);
        }
        if(spellEnableEfficiency){
            this.logger.log(Level.INFO, "efficiencyGods: " + this.efficiencyGods);
            this.logger.log(Level.INFO, "efficiencyCastTime: " + this.efficiencyCastTime);
            this.logger.log(Level.INFO, "efficiencyCost: " + this.efficiencyCost);
            this.logger.log(Level.INFO, "efficiencyDifficulty: " + this.efficiencyDifficulty);
            this.logger.log(Level.INFO, "efficiencyFaith: " + this.efficiencyFaith);
            this.logger.log(Level.INFO, "efficiencyCooldown: " + this.efficiencyCooldown);
            this.logger.log(Level.INFO, "efficiencyDifficultyPerPower: " + efficiencyDifficultyPerPower);
        }
        if(spellEnableQuarry){
            this.logger.log(Level.INFO, "quarryGods: " + this.quarryGods);
            this.logger.log(Level.INFO, "quarryCastTime: " + this.quarryCastTime);
            this.logger.log(Level.INFO, "quarryCost: " + this.quarryCost);
            this.logger.log(Level.INFO, "quarryDifficulty: " + this.quarryDifficulty);
            this.logger.log(Level.INFO, "quarryFaith: " + this.quarryFaith);
            this.logger.log(Level.INFO, "quarryCooldown: " + this.quarryCooldown);
            this.logger.log(Level.INFO, "quarryEffectiveness: " + quarryEffectiveness);
        }*/
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
                    SpellcraftSpell.HARDEN.setSpell(new Harden(SpellcraftSpell.HARDEN));
                    SpellcraftSpell.PHASING.setSpell(new Phasing(SpellcraftSpell.PHASING));
                    SpellcraftSpell.REPLENISH.setSpell(new Replenish(SpellcraftSpell.REPLENISH));
                    SpellcraftSpell.SUMMON_SOUL.setSpell(new SummonSoul(SpellcraftSpell.SUMMON_SOUL));
                    SpellcraftSpell.EXPAND.setSpell(new Expand(SpellcraftSpell.EXPAND));
                    SpellcraftSpell.EFFICIENCY.setSpell(new Efficiency(SpellcraftSpell.EFFICIENCY));
                    SpellcraftSpell.QUARRY.setSpell(new Quarry(SpellcraftSpell.QUARRY));
                    SpellcraftSpell.PROWESS.setSpell(new Prowess(SpellcraftSpell.PROWESS));
                    SpellcraftSpell.INDUSTRY.setSpell(new Industry(SpellcraftSpell.INDUSTRY));
                    SpellcraftSpell.ENDURANCE.setSpell(new Endurance(SpellcraftSpell.ENDURANCE));
                    SpellcraftSpell.ACUITY.setSpell(new Acuity(SpellcraftSpell.ACUITY));
                    SpellcraftSpell.TITANFORGED.setSpell(new Titanforged(SpellcraftSpell.TITANFORGED));
                    SpellcraftSpell.LABOURING_SPIRIT.setSpell(new LabouringSpirit(SpellcraftSpell.LABOURING_SPIRIT));
                    for(SpellcraftSpell spell : SpellcraftSpell.values()){
                        ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), spell.getSpell());
                    }
					/*Harden harden = new Harden(hardenCastTime, hardenCost, hardenDifficulty, hardenFaith, hardenCooldown);
					Phasing phasing = new Phasing(phasingCastTime, phasingCost, phasingDifficulty, phasingFaith, phasingCooldown);
					Replenish replenish = new Replenish(replenishCastTime, replenishCost, replenishDifficulty, replenishFaith, replenishCooldown);
                    SummonSoul summonSoul = new SummonSoul(summonSoulCastTime, summonSoulCost, summonSoulDifficulty, summonSoulFaith, summonSoulCooldown);
                    Expand expand = new Expand(expandCastTime, expandCost, expandDifficulty, expandFaith, expandCooldown);
                    Efficiency efficiency = new Efficiency(efficiencyCastTime, efficiencyCost, efficiencyDifficulty, efficiencyFaith, efficiencyCooldown);
                    Quarry quarry = new Quarry(quarryCastTime, quarryCost, quarryDifficulty, quarryFaith, quarryCooldown);*/
                    // Add each spell to Spells
					/*ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), harden);
					ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), phasing);
					ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), replenish);
                    ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), summonSoul);
                    ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), expand);
                    ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), efficiency);
                    ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), quarry);*/
					// Add spells to their proper deities:
					for(Deity deity : Deities.getDeities()){
					    for(SpellcraftSpell spell : SpellcraftSpell.values()){
					        if(spell.isEnabled() && (spell.getGods().contains("-1") || spell.getGods().contains(String.valueOf(deity.getNumber())))){
					            deity.addSpell(spell.getSpell());
					            logger.info("Adding spell "+spell.getName()+" to "+deity.getName());
                            }
                        }
						/*if(spellEnableHarden && (hardenGods.contains("-1") || hardenGods.contains(String.valueOf(deity.getNumber())))){
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
                        if(spellEnableExpand && (expandGods.contains("-1") || expandGods.contains(String.valueOf(deity.getNumber())))){
                            deity.addSpell(expand);
                            Debug("Adding spell "+expand.name+" to "+deity.getName());
                        }
                        if(spellEnableEfficiency && (efficiencyGods.contains("-1") || efficiencyGods.contains(String.valueOf(deity.getNumber())))){
                            deity.addSpell(efficiency);
                            Debug("Adding spell "+efficiency.name+" to "+deity.getName());
                        }
                        if(spellEnableQuarry && (quarryGods.contains("-1") || quarryGods.contains(String.valueOf(deity.getNumber())))){
                            deity.addSpell(quarry);
                            Debug("Adding spell "+quarry.name+" to "+deity.getName());
                        }*/
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
		SpellcraftSpellModifications.preInit();
		SpellcraftSpellEffects.preInit(this);
		SpellcraftCustomSpells.preInit(this);
		if(useNewDamageModifier || SpellcraftSpell.HARDEN.isEnabled()){ // Don't need to edit the damage modifier unless we're using harden or new formula
			SpellcraftDamageModifier.preInit(this);
		}
		if(statuetteTweaks){
			SpellcraftStatuetteTweaks.patchSpellClass();
            SpellcraftStatuetteTweaks.patchCastingCalls();
		}
		if(healingRedone){
		    SpellcraftHealing.preInit();
        }
	}
	
	@Override
	public void init(){
		SpellcraftSpellModifications.init(this);
		SpellcraftCustomSpells.init(this);
	}

    @Override
    public void onServerPoll() {
        SpellcraftHealing.onServerPoll();
    }
}
