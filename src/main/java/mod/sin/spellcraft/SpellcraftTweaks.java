package mod.sin.spellcraft;

import com.wurmonline.server.Items;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.spells.*;
import com.wurmonline.shared.constants.Enchants;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import javassist.bytecode.Descriptor;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class SpellcraftTweaks {
    public static Logger logger = Logger.getLogger(SpellcraftTweaks.class.getName());

    protected static ArrayList<Byte> demiseEnchants = new ArrayList<>();
    protected static ArrayList<Byte> jewelryEnchants = new ArrayList<>();

	public static long lastEnchantDecay = 0;
	public static long enchantDecayInterval = TimeConstants.DAY_MILLIS;
	protected static boolean initializedEnchantDecay = false;

	public static void updateEnchantDecayTimer(){
		Connection dbcon;
		PreparedStatement ps;
		try {
			dbcon = ModSupportDb.getModSupportDb();
			ps = dbcon.prepareStatement("UPDATE ObjectiveTimers SET TIMER = " + String.valueOf(System.currentTimeMillis()) + " WHERE ID = \"ENCHANTDECAY\"");
			ps.executeUpdate();
			ps.close();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

    public static void onServerPoll(){
    	// Decay enchants at a rate of once per day. Skip if the enchant decay timer hasn't been intialized.
    	if(SpellcraftMod.enableEnchantDecay && initializedEnchantDecay){
    		// Enough time has passed to trigger the interval. Perform the enchant decay and update the timer.
			if(System.currentTimeMillis() > lastEnchantDecay + enchantDecayInterval){
				logger.info("Starting enchant decay tick.");
				long startTime = System.currentTimeMillis();
				float decayMinimum = SpellcraftMod.enchantDecayMinimum;
				float decayMinimumArrow = SpellcraftMod.enchantDecayArrowsMinimum;
				float decayPercentage = SpellcraftMod.enchantDecayPercentage;
				for (Item item : Items.getAllItems()){
					ItemSpellEffects effs = item.getSpellEffects();
					if(effs != null && effs.getEffects() != null && effs.getEffects().length > 0){
						for (SpellEffect eff : effs.getEffects()){
							// Skip Bloodthirst as it's expected to be higher than the minimum.
							if (eff.type == Enchants.BUFF_BLOODTHIRST) {
								continue;
							}
							// If the effect is a valid spell, and greater than the enchant decay minimum, decay it.
							if (eff.type >= 0){
								if ((item.isArrow() && eff.getPower() > decayMinimumArrow) || eff.getPower() > decayMinimum) {
									// Reduce the power by the defined amount between the minimum and maximum.
									float currentPower = eff.getPower();
									float decayTo = item.isArrow() ? decayMinimumArrow : decayMinimum;
									if (currentPower > decayTo) {
										float difference = currentPower - decayTo;
										float toAdjust = difference * decayPercentage;
										float newPower = currentPower - toAdjust;
										logger.info("Found enchant " + eff.getName() + " with power " + eff.getPower() + " on item " + item.getName() + ". Adjusting power by " + toAdjust + " down to " + newPower + ".");
										eff.setPower(newPower);
									}
								}
							}
						}
					}
				}
				long endTime = System.currentTimeMillis();
				long timeTaken = endTime - startTime;
				logger.info("Completed enchant decay tick. That took "+timeTaken+" millis.");
				lastEnchantDecay = System.currentTimeMillis();
				updateEnchantDecayTimer();
			}
		}
	}

	public static void onServerStarted(){
		try {
			Connection con = ModSupportDb.getModSupportDb();
			String sql;
			String tableName = "ObjectiveTimers";
			if (!ModSupportDb.hasTable(con, tableName)) {
				logger.info(tableName+" table not found in ModSupport. Creating table now.");
				sql = "CREATE TABLE "+tableName+" (ID VARCHAR(30) NOT NULL DEFAULT 'Unknown', TIMER LONG NOT NULL DEFAULT 0)";
				PreparedStatement ps = con.prepareStatement(sql);
				ps.execute();
				ps.close();
				try {
					Connection dbcon;
					dbcon = ModSupportDb.getModSupportDb();
					ps = dbcon.prepareStatement("INSERT INTO ObjectiveTimers (ID, TIMER) VALUES(\"ENCHANTDECAY\", 0)");
					ps.executeUpdate();
					ps.close();
				}
				catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}else{
				logger.info(tableName+" table was found in ModSupport. Checking to ensure it has the ENCHANTDECAY timer.");
				sql = "SELECT * FROM ObjectiveTimers WHERE ID = \"ENCHANTDECAY\"";
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				if(rs.next()){
					logger.info("Found an entry for ENCHANTDECAY");
				}else{
					logger.info("Did not find entry for ENCHANTDECAY, creating one now...");
					rs.close();
					ps.close();
					Connection dbcon = ModSupportDb.getModSupportDb();
					PreparedStatement ps2 = dbcon.prepareStatement("INSERT INTO ObjectiveTimers (ID, TIMER) VALUES(\"ENCHANTDECAY\", 0)");
					ps2.executeUpdate();
					ps2.close();
					logger.info("Successfully created the ENCHANTDECAY entry.");
				}
			}
			initializeEnchantDecayTimer();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void initializeEnchantDecayTimer(){
		Connection dbcon;
		PreparedStatement ps;
		boolean foundLeaderboardOpt = false;
		try {
			dbcon = ModSupportDb.getModSupportDb();
			ps = dbcon.prepareStatement("SELECT * FROM ObjectiveTimers WHERE ID = \"ENCHANTDECAY\"");
			ResultSet rs = ps.executeQuery();
			lastEnchantDecay = rs.getLong("TIMER");
			rs.close();
			ps.close();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		logger.info("Initialized Enchant Decay timer: "+lastEnchantDecay);
		initializedEnchantDecay = true;
	}

    protected static void initializeSpellArrays(){
    	jewelryEnchants.add((byte) 1); // Toxin
		jewelryEnchants.add((byte) 2); // Blaze
		jewelryEnchants.add((byte) 3); // Glacial
		jewelryEnchants.add((byte) 4); // Corrosion
		jewelryEnchants.add((byte) 5); // Acid Protection
		jewelryEnchants.add((byte) 6); // Frost Protection
		jewelryEnchants.add((byte) 7); // Fire Protection
		jewelryEnchants.add((byte) 8); // Poison Protection
		jewelryEnchants.add((byte) 29); // Nolocate
		jewelryEnchants.add(SpellcraftSpell.ACUITY.getEnchant());
		jewelryEnchants.add(SpellcraftSpell.ENDURANCE.getEnchant());
		jewelryEnchants.add(SpellcraftSpell.INDUSTRY.getEnchant());
		jewelryEnchants.add(SpellcraftSpell.PROWESS.getEnchant());
        demiseEnchants.add((byte) 9); // Human's Demise
        demiseEnchants.add((byte) 10); // Selfhealer's Demise
        demiseEnchants.add((byte) 11); // Animal's Demise
        demiseEnchants.add((byte) 12); // Dragon's Demise
    }

    protected static boolean canSpellApplyItem(Spell spell, Item target){
        if(!spell.isReligiousSpell()){
            return false;
        }
        if(spell.isItemEnchantment()){
            byte enchant = spell.getEnchantment();
            // Check for negation first
			if(target.getBonusForSpellEffect(enchant) > 0){
				return true;
			}
            // Custom spells
            if(spell.getName().equals(SpellcraftSpell.EXPAND.getName())){
                return Expand.isValidContainer(target);
            }else if(spell.getName().equals(SpellcraftSpell.LABOURING_SPIRIT.getName())){
                return LabouringSpirit.isValidTarget(target);
            }else if(spell.getName().equals(SpellcraftSpell.QUARRY.getName())){
                return target.getTemplateId() == ItemList.pickAxe;
            }else if(spell.getName().equals(SpellcraftSpell.REPLENISH.getName())){
                return target.isContainerLiquid();
            }
            // Jewelery enchants
            if(!target.isEnchantableJewelry()) {
				if (jewelryEnchants.contains(spell.getEnchantment())) {
					return false;
				}
            }
            if(enchant == 48 || enchant == 49 || enchant == 50){ // Lurker enchants
                if(target.getTemplateId() != ItemList.pendulum){
                    return false;
                }
            }
            if(enchant == Enchants.BUFF_COURIER || enchant == Enchants.BUFF_DARKMESSENGER){
				if(!target.isMailBox() && !target.isSpringFilled() && !target.isPuppet() && !target.isUnenchantedTurret() && !target.isEnchantedTurret() || target.hasCourier() && !target.isEnchantedTurret()){
					return false;
				}else{
					return true;
				}
			}
            return Spell.mayBeEnchanted(target);
        }else{
            if(spell.getName().equals("Vessel")){
                if (target.isGem()) {
                    if(target.isSource() || target.getData1() > 0){
                        return false;
                    }
                }else{
                    return false;
                }
            }else if(spell.getName().equals("Break Altar")){
                if(!target.isDomainItem()){
                    return false;
                }
                if(target.isHugeAltar() && !Deities.mayDestroyAltars()){
                    return false;
                }
            }else if(spell.getName().equals("Sunder")){
                if(!Spell.mayBeEnchanted(target)){
                    return false;
                }
            }
        }
        return true;
    }
    public static Spell[] newGetSpellsTargettingItems(Creature performer, Deity deity, Item target){
        Spell[] spells = deity.getSpellsTargettingItems((int) performer.getFaith());
        if(performer.getPower() > 0 && SpellcraftMod.allSpellsGamemasters){
            spells = Spells.getSpellsTargettingItems();
            Arrays.sort(spells);
        }
        ArrayList<Spell> newSpellList = new ArrayList<>();
        for(Spell spell : spells){
            if(canSpellApplyItem(spell, target)){
                newSpellList.add(spell);
            }
        }
        return newSpellList.toArray(new Spell[0]);
    }

    public static boolean canSpellApplyTile(Spell spell, int tilex, int tiley){
        if(!spell.isTargetTile()){
            return false;
        }
        if(!spell.isReligiousSpell()){
            return false;
        }
        return true;
    }
    public static Spell[] newGetSpellsTargettingTiles(Creature performer, Deity deity, int tilex, int tiley){
        Spell[] spells = deity.getSpellsTargettingTiles((int) performer.getFaith());
        if(performer.getPower() > 0 && SpellcraftMod.allSpellsGamemasters){
            spells = Spells.getAllSpells();
            Arrays.sort(spells);
        }
        ArrayList<Spell> newSpellList = new ArrayList<>();
        for(Spell spell : spells){
            if(canSpellApplyTile(spell, tilex, tiley)){
                newSpellList.add(spell);
            }
        }
        return newSpellList.toArray(new Spell[0]);
    }

    public static void newImprovePower(SpellEffect eff, Creature performer, float newpower){
		float maximum = 100f; // Base of 100 maximum power.

		// 5 additional maximum power for having the journal flag. [105 maximum]
		if (performer.hasFlag(Player.FLAG_INC_SPELL_POWER)){
			maximum += 5f;
		}
		// 0.5 additional maximum power per level of channeling [155 maximum]
		maximum += performer.getChannelingSkill().getKnowledge() * 0.5f;

		float minimum = Math.min(eff.getPower(), maximum); // Never allow the minimum to exceed the maximum value, else the bug occurs
		float mod = 5.0F * (1.0F - minimum / maximum);
		eff.setPower(mod + newpower);
	}

	public static void riteChanges(SpellcraftMod mod){
		try{
			ClassPool classPool = HookManager.getInstance().getClassPool();
			Class<SpellcraftTweaks> thisClass = SpellcraftTweaks.class;
			
			// - Holy Crop -
			CtClass ctHolyCrop = classPool.get("com.wurmonline.server.spells.HolyCrop");
			int defaultFavor = 100000;
			final int hcFavorChangePrecondition = defaultFavor-mod.riteHolyCropFavorReq;
			String replace = "$_ = $proceed($$)+"+String.valueOf(hcFavorChangePrecondition)+";";
			Util.setReason("Adjust Holy Crop favor cost");
			Util.instrumentDeclared(thisClass, ctHolyCrop, "precondition", "getFavor", replace);

	    	final int hcFavorChangeDoEffect = defaultFavor-mod.riteHolyCropFavorReq;
	    	replace = "$_ = $proceed($$)+"+String.valueOf(hcFavorChangeDoEffect)+";";
	    	Util.setReason("Adjust Holy Crop favor cost");
	    	Util.instrumentDeclared(thisClass, ctHolyCrop, "doEffect", "getFavor", replace);

	    	final int hcFavorCost = mod.riteHolyCropFavorCost;
	    	replace = "$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+ hcFavorCost +"));";
	    	Util.setReason("Adjust Holy Crop favor cost");
	    	Util.instrumentDeclared(thisClass, ctHolyCrop, "doEffect", "setFavor", replace);

	    	if(mod.riteHolyCropMassGenesis){
	    		replace = "$_ = $proceed($$);"
                		+ "com.wurmonline.server.creatures.Creature[] allCreatures = com.wurmonline.server.creatures.Creatures.getInstance().getCreatures();"
                		+ "int i = 0;"
                		+ "while(i < allCreatures.length){"
                		+ "  if(allCreatures[i].isBred() && com.wurmonline.server.Server.rand.nextInt("+ mod.riteHolyCropGenesisChance +") == 0){"
                		+ "    allCreatures[i].getStatus().removeRandomNegativeTrait();"
                		+ "  }"
                		+ "  i++;"
                		+ "}";
	    		Util.setReason("Make Holy Crop apply a mass Genesis effect to the map");
	    		Util.instrumentDeclared(thisClass, ctHolyCrop, "doEffect", "addHistory", replace);
	    	}
	    	
	    	// - Rite of Death -
	    	CtClass ctRiteDeath = classPool.get("com.wurmonline.server.spells.RiteDeath");
	    	defaultFavor = 100000;
	    	final int rdFavorChangePrecondition = defaultFavor-mod.riteDeathFavorReq;
	    	replace = "$_ = $proceed()+"+ rdFavorChangePrecondition +";";
	    	Util.setReason("Adjust Rite of Death favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteDeath, "precondition", "getFavor", replace);

	    	final int rdFavorChangeDoEffect = defaultFavor-mod.riteDeathFavorReq;
	    	replace = "$_ = $proceed($$)+"+String.valueOf(rdFavorChangeDoEffect)+";";
	    	Util.setReason("Adjust Rite of Death favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteDeath, "doEffect", "getFavor", replace);

	    	final int rdFavorCost = mod.riteDeathFavorCost;
	    	replace = "$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+ rdFavorCost +"));";
	    	Util.setReason("Adjust Rite of Death favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteDeath, "doEffect", "setFavor", replace);
	    	
	    	// - Rite of Spring -
	    	CtClass ctRiteSpring = classPool.get("com.wurmonline.server.spells.RiteSpring");
	    	defaultFavor = 1000;
	    	replace = "$_ = 1;";
	    	Util.setReason("Set getActiveFollowers to return 1, making Rite of Spring a flat 1000 default favor cost.");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "precondition", "getActiveFollowers", replace);

	    	Util.setReason("Set getActiveFollowers to return 1, making Rite of Spring a flat 1000 default favor cost.");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "doEffect", "getActiveFollowers", replace);

	    	final int riteSpringPlayersRequired = mod.riteSpringPlayersRequired;
	    	replace = "$_ = $proceed($1, Math.min("+ riteSpringPlayersRequired +", $2));";
	    	Util.setReason("Edit the premium player requirement to cap out at 5 for Rite of Spring.");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "precondition", "max", replace);

	    	final int riteSpringFavorChangePrecondition = defaultFavor-mod.riteSpringFavorReq;
	    	replace = "$_ = $proceed()+"+ riteSpringFavorChangePrecondition +";";
	    	Util.setReason("Adjust Rite of Spring favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "precondition", "getFavor", replace);

	    	final int riteSpringFavorChangeDoEffect = defaultFavor-mod.riteSpringFavorReq;
	    	replace = "$_ = $proceed($$)+"+String.valueOf(riteSpringFavorChangeDoEffect)+";";
	    	Util.setReason("Adjust Rite of Spring favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "doEffect", "getFavor", replace);

	    	final int riteSpringFavorCost = mod.riteSpringFavorCost;
	    	replace = "$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+ riteSpringFavorCost +"));";
	    	Util.setReason("Adjust Rite of Spring favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "doEffect", "setFavor", replace);
	    	
	    	// Ritual of the Sun
	    	CtClass ctRitualSun = classPool.get("com.wurmonline.server.spells.RitualSun");
	    	defaultFavor = 100000;
	    	final int riteSunFavorChangePrecondition = defaultFavor-mod.riteSunFavorReq;
	    	replace = "$_ = $proceed()+"+ riteSunFavorChangePrecondition +";";
	    	Util.setReason("Adjust Ritual of the Sun favor cost");
	    	Util.instrumentDeclared(thisClass, ctRitualSun, "precondition", "getFavor", replace);

	    	final int riteSunFavorChangeDoEffect = defaultFavor-mod.riteSunFavorReq;
	    	replace = "$_ = $proceed($$)+"+String.valueOf(riteSunFavorChangeDoEffect)+";";
	    	Util.setReason("Adjust Ritual of the Sun favor cost");
	    	Util.instrumentDeclared(thisClass, ctRitualSun, "doEffect", "getFavor", replace);

	    	final int riteSunFavorCost = mod.riteSunFavorCost;
	    	replace = "$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+ riteSunFavorCost +"));";
	    	Util.setReason("Adjust Ritual of the Sun favor cost");
	    	Util.instrumentDeclared(thisClass, ctRitualSun, "doEffect", "setFavor", replace);

	    	replace = "$_ = $proceed(0f, true);";
	    	Util.setReason("Make Ritual of the Sun do a full refresh.");
	    	Util.instrumentDeclared(thisClass, ctRitualSun, "doEffect", "refresh", replace);
	        
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void preInit(SpellcraftMod mod){
		try{
			ClassPool classPool = HookManager.getInstance().getClassPool();
			Class<SpellcraftTweaks> thisClass = SpellcraftTweaks.class;
			String replace;

			// - Set new maximum player faith -
			if(mod.maximumPlayerFaith != 100){
		        CtClass ctDbPlayerInfo = classPool.get("com.wurmonline.server.players.DbPlayerInfo");
		        replace = "if($1 == 100.0){"
                		+ "  $_ = $proceed("+String.valueOf(mod.maximumPlayerFaith)+".0D, (double)$2);"
                		+ "}else{"
                		+ "  $_ = $proceed($$);"
                		+ "}";
		        Util.setReason("Set new maximum player faith.");
		        Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFaith", "min", replace);

		        // Uncap player favor to the new maximum faith.
		        replace = "if($1 == 100.0){"
                		+ "  $_ = $proceed("+String.valueOf(mod.maximumPlayerFaith)+".0D, (double)$2);"
                		+ "}else{"
                		+ "  $_ = $proceed($$);"
                		+ "}";
		        Util.setReason("Uncap player favor to the new maximum faith.");
		        Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFavor", "min", replace);
			}
			
			// - Update prayer faith gains to scale to the new maximumFaith -
			if(mod.scalePrayerGains && mod.hourlyPrayer){
				CtClass ctPlayerInfo = classPool.get("com.wurmonline.server.players.PlayerInfo");
				replace = "$_ = $proceed(Math.min(3.0f, Math.max(0.001f, 3.0f*("+String.valueOf(mod.maximumPlayerFaith)+".0f - this.getFaith()) / (10.0f * Math.max(1.0f, this.getFaith())))));" +
						"this.lastFaith = System.currentTimeMillis() + 2400000;";
				Util.setReason("Scale prayer gains to the new maximum faith.");
				Util.instrumentDeclared(thisClass, ctPlayerInfo, "checkPrayerFaith", "modifyFaith", replace);
				Util.setReason("Unlock the maximum of 1 faith adjustment.");
				replace = "$_ = $proceed(3.0f, $2);";
				Util.instrumentDeclared(thisClass, ctPlayerInfo, "modifyFaith", "min", replace);
			}else if(mod.scalePrayerGains){
				CtClass ctPlayerInfo = classPool.get("com.wurmonline.server.players.PlayerInfo");
				replace = "$_ = $proceed(Math.min(1.0f, Math.max(0.001f, ("+String.valueOf(mod.maximumPlayerFaith)+".0f - this.getFaith()) / (10.0f * Math.max(1.0f, this.getFaith())))));";
				Util.setReason("Scale prayer gains to the new maximum faith.");
				Util.instrumentDeclared(thisClass, ctPlayerInfo, "checkPrayerFaith", "modifyFaith", replace);
			}

			// - Update favor regeneration -
			if(mod.newFavorRegen){
		        CtClass ctPlayer = classPool.get("com.wurmonline.server.players.Player");
		        replace = "this.pollFavor();"
                		+ "$_ = $proceed($$);";
		        Util.setReason("Adjust favor regeneration to scale to new faith limit.");
		        Util.instrumentDeclared(thisClass, ctPlayer, "poll", "pollFat", replace);

				Util.setReason("Adjust favor regeneration to scale to new faith limit.");
		        replace = "if($1 != this.saveFile.getFaith()){"
                		// CurrentFavor + lMod * max(100, (channelSkill+currentFaith)*2*[Title?1:2]) / max(1, currentFavor*30)
                		+ "  $_ = $proceed(this.saveFile.getFavor() + lMod * (Math.max(100.0f, (float)(this.getChannelingSkill().getKnowledge()+this.saveFile.getFaith())*2f*(com.wurmonline.server.kingdom.King.isOfficial(1501, this.getWurmId(), this.getKingdomId()) ? 2 : 1)) / (Math.max(1.0f, this.saveFile.getFavor()) * 300.0f)));"
                		+ "}else{"
                		+ "  $_ = $proceed($$);"
                		+ "}";
		        Util.instrumentDeclared(thisClass, ctPlayer, "pollFavor", "setFavor", replace);
			}
			
			// - Attempt to allow custom priest faith - //
			if(mod.priestFaithRequirement != 30){
		        CtClass ctHugeAltarBehaviour = classPool.get("com.wurmonline.server.behaviours.HugeAltarBehaviour");
		        String actionDescriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] {
		        		classPool.get("com.wurmonline.server.behaviours.Action"),
		        		classPool.get("com.wurmonline.server.creatures.Creature"),
		        		classPool.get("com.wurmonline.server.items.Item"),
		        		CtClass.shortType,
		        		CtClass.floatType});
		        replace = "if(performer.getFaith() >= "+String.valueOf(mod.priestFaithRequirement)+" && performer.getFaith() < 50){"
	            		+ "  $_ = 30.0f;"
	            		+ "}else{"
	            		+ "  $_ = $proceed($$);"
	            		+ "}";
		        Util.setReason("Change faith required to priest.");
		        Util.instrumentDeclared(thisClass, ctHugeAltarBehaviour, "getCommonBehaviours", "getFaith", replace);

		        Util.setReason("Change faith required to priest.");
		        Util.instrumentDescribed(thisClass, ctHugeAltarBehaviour, "action", actionDescriptor, "getFaith", replace);

		        CtClass ctMethodsCreatures = classPool.get("com.wurmonline.server.behaviours.MethodsCreatures");
		        Util.setReason("Change faith required to priest.");
		        Util.instrumentDeclared(thisClass, ctMethodsCreatures, "sendAskPriestQuestion", "getFaith", replace);

	            // - Fix de-priesting when gaining faith below 30 - //
				Util.setReason("Fix de-priesting when gaining faith below 30 as a priest.");
	            CtClass ctDbPlayerInfo = classPool.get("com.wurmonline.server.players.DbPlayerInfo");
	            replace = "if($2 == 20.0f && $1 < 30){"
                		+ "  $_ = $proceed(30.0f, lFaith);"
                		+ "}else{"
                		+ "  $_ = $proceed($$);"
                		+ "}";
	            Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFaith", "min", replace);

				Util.setReason("Minor change for custom priest faith.");
	            replace = "$_ = $proceed(true);";
	            Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFaith", "setPriest", replace);

				Util.setReason("Minor change for custom priest faith.");
	            replace = "$_ = null;";
	            Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFaith", "sendAlertServerMessage", replace);
			}

			if(SpellcraftMod.onlyShowValidSpells){
			    // Initialize array first
                initializeSpellArrays();

                Util.setReason("Only show valid spells in the ItemBehaviour list.");
                CtClass ctItemBehaviour = classPool.get("com.wurmonline.server.behaviours.ItemBehaviour");
                CtMethod[] itemMethods = ctItemBehaviour.getDeclaredMethods("getBehavioursFor");
                for(CtMethod method : itemMethods){
                    try {
                        method.instrument(new ExprEditor() {
                            @Override
                            public void edit(MethodCall m) throws CannotCompileException {
                                if (m.getMethodName().equals("getSpellsTargettingItems")) {
                                    String replace = "$_ = "+SpellcraftTweaks.class.getName()+".newGetSpellsTargettingItems(performer, $0, target);";
                                    m.replace(replace);
                                    logger.info("Replaced getSpellsTargettingItems in getBehaviourFor to make spells only show for working targets.");
                                }
                            }
                        });
                    } catch (CannotCompileException e) {
                        e.printStackTrace();
                    }
                }

                Util.setReason("Only show valid spells in the ItemBehaviour list.");
                CtClass ctTileBehaviour = classPool.get("com.wurmonline.server.behaviours.TileBehaviour");
                replace = "$_ = "+SpellcraftTweaks.class.getName()+".newGetSpellsTargettingTiles(performer, $0, tilex, tiley);";
                Util.instrumentDeclared(thisClass, ctTileBehaviour, "getTileAndFloorBehavioursFor", "getSpellsTargettingTiles", replace);
			}

			if(SpellcraftMod.allSpellsGamemasters) {
			    Util.setReason("Enable GM's to cast all spells.");
                CtClass ctAction = classPool.get("com.wurmonline.server.behaviours.Action");
                CtConstructor[] constructors = ctAction.getConstructors();
                for(CtConstructor constructor : constructors){
                    try {
                        constructor.instrument(new ExprEditor() {
                            @Override
                            public void edit(MethodCall m) throws CannotCompileException {
                                if (m.getMethodName().equals("hasSpell")) {
                                    String replace = "$_ = $proceed($$) || aPerformer.getPower() > 0;";
                                    m.replace(replace);
                                    logger.info("Replaced hasSpell in Action constructor to enable GM's to use all spells.");
                                }
                            }
                        });
                    } catch (CannotCompileException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (SpellcraftMod.crossFaithLinking) {
				Util.setReason("Enable Cross Faith Linking");
				CtClass ctList = classPool.get("java.util.List");
				CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
				CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
				CtClass ctCreatureBehaviour = classPool.get("com.wurmonline.server.behaviours.CreatureBehaviour");
				CtClass[] params = {
						ctCreature,
						ctItem,
						ctCreature
				};
				String desc = Descriptor.ofMethod(ctList, params);
				replace = "$_ = 0;";
				Util.instrumentDescribed(thisClass, ctCreatureBehaviour, "getBehavioursFor", desc, "getTemplateDeity", replace);

				Util.setReason("Enable Cross Faith Linking");
				Util.instrumentDeclared(thisClass, ctCreatureBehaviour, "handle_MAGICLINK", "getTemplateDeity", replace);
			}

			if (SpellcraftMod.fixHighPowerEnchants){
				Util.setReason("Fix high power enchant casts reducing actual power.");
				CtClass ctSpellEffect = classPool.get("com.wurmonline.server.spells.SpellEffect");
				replace = "{ "+SpellcraftTweaks.class.getName()+".newImprovePower($0, $1, $2); }";
				Util.setBodyDeclared(thisClass, ctSpellEffect, "improvePower", replace);
			}

            // Fix for not being able to cast Expand on magic containers.
			try {
				classPool.getCtClass("com.wurmonline.server.spells.Spell")
						.getMethod("run", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;F)Z")
						.instrument(new ExprEditor() {
							@Override
							public void edit(MethodCall m) throws CannotCompileException {
								if (m.getMethodName().equals("isMagicContainer")) m.replace("$_=false;");
							}
						});
			} catch (CannotCompileException e) {
				e.printStackTrace();
			}

		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
}
