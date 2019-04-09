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
import mod.sin.lib.Prop;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;

public class SpellcraftMod
implements WurmServerMod, Configurable, PreInitable, Initable, ServerPollListener, ServerStartedListener {
    private Logger logger = Logger.getLogger(SpellcraftMod.class.getName());
    
    // Configuration options
	public boolean bDebug = false;
	public int maximumPlayerFaith = 100;
	public float priestFaithRequirement = 30;
	public boolean hourlyPrayer = true;
	public boolean scalePrayerGains = true;
	public boolean newFavorRegen = true;
	public boolean useNewDamageModifier = true;
	public static boolean improvedEnchantGrouping = true;
	public boolean statuetteTweaks = true;
	public static boolean onlyShowValidSpells = true;
	public static boolean allSpellsGamemasters = true;
	
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
    public static boolean useRecodedSmite = true;

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

	// Default spell modifications:
	public HashMap<String, Integer> spellCastTimes = new HashMap<>();
	public HashMap<String, Integer> spellCosts = new HashMap<>();
	public HashMap<String, Integer> spellDifficulties = new HashMap<>();
	public HashMap<String, Integer> spellFaithRequirements = new HashMap<>();
	public HashMap<String, Long> spellCooldowns = new HashMap<>();
	
	// Deity spell additions and removals:
	public HashMap<String, List<String>> addSpells = new HashMap<>();
	public HashMap<String, List<String>> removeSpells = new HashMap<>();

	@Override
	public void configure(Properties properties) {
		this.logger.info("Beginning configuration...");
		Prop.properties = properties;
		// Base configuration options
        this.bDebug = Boolean.parseBoolean(properties.getProperty("debug", Boolean.toString(this.bDebug)));
        this.maximumPlayerFaith = Integer.parseInt(properties.getProperty("maximumPlayerFaith", Integer.toString(this.maximumPlayerFaith)));
        this.priestFaithRequirement = Float.parseFloat(properties.getProperty("priestFaithRequirement", Float.toString(this.priestFaithRequirement)));
        this.hourlyPrayer = Boolean.parseBoolean(properties.getProperty("hourlyPrayer", Boolean.toString(this.hourlyPrayer)));
        this.scalePrayerGains = Boolean.parseBoolean(properties.getProperty("scalePrayerGains", Boolean.toString(this.scalePrayerGains)));
        this.newFavorRegen = Boolean.parseBoolean(properties.getProperty("newFavorRegen", Boolean.toString(this.newFavorRegen)));
        this.useNewDamageModifier = Boolean.parseBoolean(properties.getProperty("useNewDamageModifier", Boolean.toString(this.useNewDamageModifier)));
        improvedEnchantGrouping = Boolean.parseBoolean(properties.getProperty("improvedEnchantGrouping", Boolean.toString(improvedEnchantGrouping)));
        onlyShowValidSpells = Prop.getBooleanProperty("onlyShowValidSpells", onlyShowValidSpells);
        allSpellsGamemasters = Prop.getBooleanProperty("allSpellsGamemasters", allSpellsGamemasters);
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
        //reduceScornHealingDone = Boolean.parseBoolean(properties.getProperty("reduceScornHealingDone", Boolean.toString(reduceScornHealingDone)));
        useRecodedSmite = Boolean.parseBoolean(properties.getProperty("useRecodedSmite", Boolean.toString(useRecodedSmite)));
        //increaseFranticChargeDuration = Boolean.parseBoolean(properties.getProperty("increaseFranticChargeDuration", Boolean.toString(increaseFranticChargeDuration)));
        //healingRedone = Boolean.parseBoolean(properties.getProperty("healingRedone", Boolean.toString(healingRedone)));
        // Custom spell configurations
        for(SpellcraftSpell spell : SpellcraftSpell.values()){
            spell.setEnabled(Prop.getBooleanProperty("spellEnable"+spell.getName().replaceAll(" ", ""), true));
            spell.setGods(Prop.getStringProperty(spell.getPropertyName()+"Gods", "-1"));
            spell.setCastTime(Prop.getIntegerProperty(spell.getPropertyName()+"CastTime", 30));
            spell.setCost(Prop.getIntegerProperty(spell.getPropertyName()+"Cost", 50));
            spell.setDifficulty(Prop.getIntegerProperty(spell.getPropertyName()+"Difficulty", 50));
            spell.setFaith(Prop.getIntegerProperty(spell.getPropertyName()+"Faith", 50));
            spell.setCooldown(Prop.getLongProperty(spell.getPropertyName()+"Cooldown", 0));
        }
        phasingPowerMultiplier = Float.parseFloat(properties.getProperty("phasingPowerMultiplier", Float.toString(phasingPowerMultiplier)));
        expandEffectModifier = Float.parseFloat(properties.getProperty("expandEffectModifier", Float.toString(expandEffectModifier)));
        efficiencyDifficultyPerPower = Float.parseFloat(properties.getProperty("efficiencyDifficultyPerPower", Float.toString(efficiencyDifficultyPerPower)));
        quarryEffectiveness = Float.parseFloat(properties.getProperty("quarryEffectiveness", Float.toString(quarryEffectiveness)));
        prowessEffectiveness = Prop.getFloatProperty("prowessEffectiveness", prowessEffectiveness);
        industryEffectiveness = Prop.getFloatProperty("industryEffectiveness", industryEffectiveness);
        enduranceEffectiveness = Prop.getFloatProperty("enduranceEffectiveness", enduranceEffectiveness);
        acuityEffectiveness = Prop.getFloatProperty("acuityEffectiveness", acuityEffectiveness);
        titanforgedMultiplier = Prop.getFloatProperty("titanforgedMultiplier", titanforgedMultiplier);

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

        // Print values of mod configuration
        this.logger.info(" -- Mod Configuration -- ");
        this.logger.log(Level.INFO, "maximumPlayerFaith: " + this.maximumPlayerFaith);
        this.logger.log(Level.INFO, "priestFaithRequirement: " + this.priestFaithRequirement);
        logger.info("hourlyPrayer: " + this.hourlyPrayer);
        this.logger.log(Level.INFO, "scalePrayerGains: " + this.scalePrayerGains);
        this.logger.log(Level.INFO, "newFavorRegen: " + this.newFavorRegen);
        this.logger.log(Level.INFO, "useNewDamageModifier: " + this.useNewDamageModifier);
        logger.info("Only Show Valid Spells: "+onlyShowValidSpells);
        logger.info("All Spells Gamemasters: "+allSpellsGamemasters);
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
        //this.logger.log(Level.INFO, "reduceScornHealingDone: " + reduceScornHealingDone);
        this.logger.log(Level.INFO, "useRecodedSmite: " + useRecodedSmite);
        //this.logger.log(Level.INFO, "increaseFranticChargeDuration: " + increaseFranticChargeDuration);
        //this.logger.log(Level.INFO, "healingRedone: " + healingRedone);
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
		SpellcraftSpellEffects.onServerStarted();
		new Runnable(){
			@Override
			public void run(){
				try{
                    SpellcraftSpell.HARDEN.setSpell(new Harden(SpellcraftSpell.HARDEN));
                    SpellcraftSpell.PHASING.setSpell(new Phasing(SpellcraftSpell.PHASING));
                    SpellcraftSpell.REPLENISH.setSpell(new Replenish(SpellcraftSpell.REPLENISH));
                    //SpellcraftSpell.SUMMON_SOUL.setSpell(new SummonSoul(SpellcraftSpell.SUMMON_SOUL));
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
		SpellcraftCustomSpells.preInit(this);
		if(useNewDamageModifier || SpellcraftSpell.HARDEN.isEnabled()){ // Don't need to edit the damage modifier unless we're using harden or new formula
			SpellcraftDamageModifier.preInit(this);
		}
		if(statuetteTweaks){
			SpellcraftStatuetteTweaks.patchSpellClass();
            SpellcraftStatuetteTweaks.patchCastingCalls();
		}
		/*if(healingRedone){
		    SpellcraftHealing.preInit();
        }*/
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
