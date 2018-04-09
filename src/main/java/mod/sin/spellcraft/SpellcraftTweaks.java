package mod.sin.spellcraft;

import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import mod.sin.lib.Util;

public class SpellcraftTweaks {
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
	    	/*ctHolyCrop.getDeclaredMethod("precondition").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("getFavor")) {
	                    m.replace("$_ = $proceed($$)+"+String.valueOf(hcFavorChangePrecondition)+";"); // Add the favor difference to "spoof" the deity having enough
	                    return;
	                }
	            }
	        });*/
	    	final int hcFavorChangeDoEffect = defaultFavor-mod.riteHolyCropFavorReq;
	    	replace = "$_ = $proceed($$)+"+String.valueOf(hcFavorChangeDoEffect)+";";
	    	Util.setReason("Adjust Holy Crop favor cost");
	    	Util.instrumentDeclared(thisClass, ctHolyCrop, "doEffect", "getFavor", replace);
	    	/*ctHolyCrop.getDeclaredMethod("doEffect").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("getFavor")) {
	                    m.replace("$_ = $proceed($$)+"+String.valueOf(hcFavorChangeDoEffect)+";"); // Add the favor difference to "spoof" the deity having enough
	                    return;
	                }
	            }
	        });*/
	    	final int hcFavorCost = mod.riteHolyCropFavorCost;
	    	replace = "$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+ hcFavorCost +"));";
	    	Util.setReason("Adjust Holy Crop favor cost");
	    	Util.instrumentDeclared(thisClass, ctHolyCrop, "doEffect", "setFavor", replace);
	    	/*ctHolyCrop.getDeclaredMethod("doEffect").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("setFavor")) { // This changes the final argument after the "getFavor" instrument above, allowing us to call "getFavor" accurately
	                    m.replace("$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+Integer.valueOf(hcFavorCost)+"));");
	                    return;
	                }
	            }
	        });*/
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
		    	/*ctHolyCrop.getDeclaredMethod("doEffect").instrument(new ExprEditor(){
		            public void edit(MethodCall m) throws CannotCompileException {
		                if (m.getMethodName().equals("addHistory")) {
		                    m.replace("$_ = $proceed($$);"
		                    		+ "com.wurmonline.server.creatures.Creature[] allCreatures = com.wurmonline.server.creatures.Creatures.getInstance().getCreatures();"
		                    		+ "int i = 0;"
		                    		+ "while(i < allCreatures.length){"
		                    		+ "  if(allCreatures[i].isBred() && com.wurmonline.server.Server.rand.nextInt("+Integer.valueOf(mod.riteHolyCropGenesisChance)+") == 0){"
		                    		+ "    allCreatures[i].getStatus().removeRandomNegativeTrait();"
		                    		+ "  }"
		                    		+ "  i++;"
		                    		+ "}");
		                    return;
		                }
		            }
		        });*/
	    	}
	    	
	    	// - Rite of Death -
	    	CtClass ctRiteDeath = classPool.get("com.wurmonline.server.spells.RiteDeath");
	    	defaultFavor = 100000;
	    	final int rdFavorChangePrecondition = defaultFavor-mod.riteDeathFavorReq;
	    	replace = "$_ = $proceed()+"+Integer.valueOf(rdFavorChangePrecondition)+";";
	    	Util.setReason("Adjust Rite of Death favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteDeath, "precondition", "getFavor", replace);
	    	/*ctRiteDeath.getDeclaredMethod("precondition").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("getFavor")) {
	                    m.replace("$_ = $proceed()+"+Integer.valueOf(rdFavorChangePrecondition)+";"); // Add the favor difference to "spoof" the deity having enough
	                    return;
	                }
	            }
	        });*/
	    	final int rdFavorChangeDoEffect = defaultFavor-mod.riteDeathFavorReq;
	    	replace = "$_ = $proceed($$)+"+String.valueOf(rdFavorChangeDoEffect)+";";
	    	Util.setReason("Adjust Rite of Death favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteDeath, "doEffect", "getFavor", replace);
	    	/*ctRiteDeath.getDeclaredMethod("doEffect").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("getFavor")) {
	                    m.replace("$_ = $proceed($$)+"+String.valueOf(rdFavorChangeDoEffect)+";"); // Add the favor difference to "spoof" the deity having enough
	                    return;
	                }
	            }
	        });*/
	    	final int rdFavorCost = mod.riteDeathFavorCost;
	    	replace = "$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+Integer.valueOf(rdFavorCost)+"));";
	    	Util.setReason("Adjust Rite of Death favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteDeath, "doEffect", "setFavor", replace);
	    	/*ctRiteDeath.getDeclaredMethod("doEffect").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("setFavor")) { // This changes the final argument after the "getFavor" instrument above, allowing us to call "getFavor" accurately
	                    m.replace("$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+Integer.valueOf(rdFavorCost)+"));");
	                    return;
	                }
	            }
	        });*/
	    	
	    	// - Rite of Spring -
	    	CtClass ctRiteSpring = classPool.get("com.wurmonline.server.spells.RiteSpring");
	    	defaultFavor = 1000;
	    	replace = "$_ = 1;";
	    	Util.setReason("Set getActiveFollowers to return 1, making Rite of Spring a flat 1000 default favor cost.");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "precondition", "getActiveFollowers", replace);
	    	/*ctRiteSpring.getDeclaredMethod("precondition").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("getActiveFollowers")) {
	                    m.replace("$_ = 1;"); // Edit getActiveFollowers to always return 1, making it a flat 1000 default favor cost. [Precondition]
	                    return;
	                }
	            }
	        });*/
	    	Util.setReason("Set getActiveFollowers to return 1, making Rite of Spring a flat 1000 default favor cost.");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "doEffect", "getActiveFollowers", replace);
	    	/*ctRiteSpring.getDeclaredMethod("doEffect").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("getActiveFollowers")) {
	                    m.replace("$_ = 1;"); // Edit getActiveFollowers to always return 1, making it a flat 1000 default favor cost. [DoEffect]
	                    return;
	                }
	            }
	        });*/
	    	final int riteSpringPlayersRequired = mod.riteSpringPlayersRequired;
	    	replace = "$_ = $proceed($1, Math.min("+ riteSpringPlayersRequired +", $2));";
	    	Util.setReason("Edit the premium player requirement to cap out at 5 for Rite of Spring.");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "precondition", "max", replace);
	    	/*ctRiteSpring.getDeclaredMethod("precondition").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("max")) {
	                    m.replace("$_ = $proceed($1, Math.min("+Integer.valueOf(riteSpringPlayersRequired)+", $2));"); // Edit the premium player requirement to cap out at 5.
	                    return;
	                }
	            }
	        });*/
	    	final int riteSpringFavorChangePrecondition = defaultFavor-mod.riteSpringFavorReq;
	    	replace = "$_ = $proceed()+"+ riteSpringFavorChangePrecondition +";";
	    	Util.setReason("Adjust Rite of Spring favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "precondition", "getFavor", replace);
	    	/*ctRiteSpring.getDeclaredMethod("precondition").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("getFavor")) {
	                    m.replace("$_ = $proceed()+"+Integer.valueOf(riteSpringFavorChangePrecondition)+";"); // Add the favor difference to "spoof" the deity having enough
	                    return;
	                }
	            }
	        });*/
	    	final int riteSpringFavorChangeDoEffect = defaultFavor-mod.riteSpringFavorReq;
	    	replace = "$_ = $proceed($$)+"+String.valueOf(riteSpringFavorChangeDoEffect)+";";
	    	Util.setReason("Adjust Rite of Spring favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "doEffect", "getFavor", replace);
	    	/*ctRiteSpring.getDeclaredMethod("doEffect").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("getFavor")) {
	                    m.replace("$_ = $proceed($$)+"+String.valueOf(riteSpringFavorChangeDoEffect)+";"); // Add the favor difference to "spoof" the deity having enough
	                    return;
	                }
	            }
	        });*/
	    	final int riteSpringFavorCost = mod.riteSpringFavorCost;
	    	replace = "$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+ riteSpringFavorCost +"));";
	    	Util.setReason("Adjust Rite of Spring favor cost");
	    	Util.instrumentDeclared(thisClass, ctRiteSpring, "doEffect", "setFavor", replace);
	    	/*ctRiteSpring.getDeclaredMethod("doEffect").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("setFavor")) { // This changes the final argument after the "getFavor" instrument above, allowing us to call "getFavor" accurately
	                    m.replace("$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+Integer.valueOf(riteSpringFavorCost)+"));");
	                    return;
	                }
	            }
	        });*/
	    	
	    	// Ritual of the Sun
	    	CtClass ctRitualSun = classPool.get("com.wurmonline.server.spells.RitualSun");
	    	defaultFavor = 100000;
	    	final int riteSunFavorChangePrecondition = defaultFavor-mod.riteSunFavorReq;
	    	replace = "$_ = $proceed()+"+ riteSunFavorChangePrecondition +";";
	    	Util.setReason("Adjust Ritual of the Sun favor cost");
	    	Util.instrumentDeclared(thisClass, ctRitualSun, "precondition", "getFavor", replace);
	    	/*ctRitualSun.getDeclaredMethod("precondition").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("getFavor")) {
	                    m.replace("$_ = $proceed()+"+Integer.valueOf(riteSunFavorChangePrecondition)+";"); // Add the favor difference to "spoof" the deity having enough
	                    return;
	                }
	            }
	        });*/
	    	final int riteSunFavorChangeDoEffect = defaultFavor-mod.riteSunFavorReq;
	    	replace = "$_ = $proceed($$)+"+String.valueOf(riteSunFavorChangeDoEffect)+";";
	    	Util.setReason("Adjust Ritual of the Sun favor cost");
	    	Util.instrumentDeclared(thisClass, ctRitualSun, "doEffect", "getFavor", replace);
	    	/*ctRitualSun.getDeclaredMethod("doEffect").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("getFavor")) {
	                    m.replace("$_ = $proceed($$)+"+String.valueOf(riteSunFavorChangeDoEffect)+";"); // Add the favor difference to "spoof" the deity having enough
	                    return;
	                }
	            }
	        });*/
	    	final int riteSunFavorCost = mod.riteSunFavorCost;
	    	replace = "$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+Integer.valueOf(riteSunFavorCost)+"));";
	    	Util.setReason("Adjust Ritual of the Sun favor cost");
	    	Util.instrumentDeclared(thisClass, ctRitualSun, "doEffect", "setFavor", replace);
	    	/*ctRitualSun.getDeclaredMethod("doEffect").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("setFavor")) { // This changes the final argument after the "getFavor" instrument above, allowing us to call "getFavor" accurately
	                    m.replace("$_ = $proceed(Math.max(0, performer.getDeity().getFavor()-"+Integer.valueOf(riteSunFavorCost)+"));");
	                    return;
	                }
	            }
	        });*/
	    	replace = "$_ = $proceed(0f, true);";
	    	Util.setReason("Make Ritual of the Sun do a full refresh.");
	    	Util.instrumentDeclared(thisClass, ctRitualSun, "doEffect", "refresh", replace);
	    	/*ctRitualSun.getDeclaredMethod("doEffect").instrument(new ExprEditor(){
	            public void edit(MethodCall m) throws CannotCompileException {
	                if (m.getMethodName().equals("refresh")) {
	                    m.replace("$_ = $proceed(0f, true);"); // Do a full refresh instead of half refresh
	                    return;
	                }
	            }
	        });*/
	        
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	public static void preInit(SpellcraftMod mod){
		try{
			ClassPool classPool = HookManager.getInstance().getClassPool();
			Class<SpellcraftTweaks> thisClass = SpellcraftTweaks.class;
			String replace;

			// - Allow creature spell effects to be examined -
			if(mod.showCreatureSpellEffects){
		        CtClass ctCreatureBehaviour = classPool.get("com.wurmonline.server.behaviours.CreatureBehaviour");
		        replace = "if(target.getSpellEffects() != null){"
		        		+ "  int i = 0;"
		        		+ "  com.wurmonline.server.spells.SpellEffect[] effs = target.getSpellEffects().getEffects();"
		        		+ "  while(i < effs.length){"
		        		+ "    performer.getCommunicator().sendNormalServerMessage(effs[i].getName() + \" has been cast on it, so it has \" + effs[i].getLongDesc() + \" [\" + (int)effs[i].power + \"]\");"
		        		+ "    i++;"
		        		+ "  }"
		        		+ "}";
		        Util.setReason("Show creature spell effects when examined.");
		        Util.insertAfterDeclared(thisClass, ctCreatureBehaviour, "handle_EXAMINE", replace);
		        /*ctCreatureBehaviour.getDeclaredMethod("handle_EXAMINE").insertAfter("if(target.getSpellEffects() != null){"
		        		+ "  int i = 0;"
		        		+ "  com.wurmonline.server.spells.SpellEffect[] effs = target.getSpellEffects().getEffects();"
		        		+ "  while(i < effs.length){"
		        		+ "    performer.getCommunicator().sendNormalServerMessage(effs[i].getName() + \" has been cast on it, so it has \" + effs[i].getLongDesc() + \" [\" + (int)effs[i].power + \"]\");"
		        		+ "    i++;"
		        		+ "  }"
		        		+ "}");*/
			}
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
		        /*ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
		            public void edit(MethodCall m) throws CannotCompileException {
		                if (m.getMethodName().equals("min")) {
		                    m.replace("if($1 == 100.0){"
		                    		+ "  $_ = $proceed("+String.valueOf(mod.maximumPlayerFaith)+".0D, (double)$2);"
		                    		+ "}else{"
		                    		+ "  $_ = $proceed($$);"
		                    		+ "}");
		                    return;
		                }
		            }
		        });*/
		        // Uncap player favor to the new maximum faith.
		        replace = "if($1 == 100.0){"
                		+ "  $_ = $proceed("+String.valueOf(mod.maximumPlayerFaith)+".0D, (double)$2);"
                		+ "}else{"
                		+ "  $_ = $proceed($$);"
                		+ "}";
		        Util.setReason("Uncap player favor to the new maximum faith.");
		        Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFavor", "min", replace);
		        /*ctDbPlayerInfo.getDeclaredMethod("setFavor").instrument(new ExprEditor(){
		            public void edit(MethodCall m) throws CannotCompileException {
		                if (m.getMethodName().equals("min")) {
		                    m.replace("if($1 == 100.0){"
		                    		+ "  $_ = $proceed("+String.valueOf(mod.maximumPlayerFaith)+".0D, (double)$2);"
		                    		+ "}else{"
		                    		+ "  $_ = $proceed($$);"
		                    		+ "}");
		                    return;
		                }
		            }
		        });*/
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
		        /*ctPlayer.getDeclaredMethod("poll").instrument(new ExprEditor(){
		            public void edit(MethodCall m) throws CannotCompileException {
		                if (m.getMethodName().equals("pollFat")) {
		                    m.replace("this.pollFavor();"
		                    		+ "$_ = $proceed($$);");
		                    return;
		                }
		            }
		        });*/
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
		        /*ctHugeAltarBehaviour.getDeclaredMethod("getCommonBehaviours").instrument(new ExprEditor(){
		            public void edit(MethodCall m) throws CannotCompileException {
		                if (m.getMethodName().equals("getFaith")) {
		                    m.replace(replaceString);
		                    return;
		                }
		            }
		        });*/
		        Util.setReason("Change faith required to priest.");
		        Util.instrumentDescribed(thisClass, ctHugeAltarBehaviour, "action", actionDescriptor, "getFaith", replace);
		        /*ctHugeAltarBehaviour.getMethod("action", actionDescriptor).instrument(new ExprEditor(){
		            public void edit(MethodCall m) throws CannotCompileException {
		                if (m.getMethodName().equals("getFaith")) {
		                    m.replace(replaceString);
		                    return;
		                }
		            }
		        });*/
		        CtClass ctMethodsCreatures = classPool.get("com.wurmonline.server.behaviours.MethodsCreatures");
		        Util.setReason("Change faith required to priest.");
		        Util.instrumentDeclared(thisClass, ctMethodsCreatures, "sendAskPriestQuestion", "getFaith", replace);
		        /*ctMethodsCreatures.getDeclaredMethod("sendAskPriestQuestion").instrument(new ExprEditor(){
		            public void edit(MethodCall m) throws CannotCompileException {
		                if (m.getMethodName().equals("getFaith")) {
		                    m.replace(replaceString);
		                    return;
		                }
		            }
		        });*/

	            // - Fix de-priesting when gaining faith below 30 - //
	            CtClass ctDbPlayerInfo = classPool.get("com.wurmonline.server.players.DbPlayerInfo");
	            replace = "if($2 == 20.0f && $1 < 30){"
                		+ "  $_ = $proceed(30.0f, lFaith);"
                		+ "}else{"
                		+ "  $_ = $proceed($$);"
                		+ "}";
	            Util.setReason("Fix de-priesting when gaining faith below 30 as a priest.");
	            Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFaith", "min", replace);
	            /*ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
	                public void edit(MethodCall m) throws CannotCompileException {
	                    if (m.getMethodName().equals("min")) {
	                        m.replace("if($2 == 20.0f && $1 < 30){"
	                        		+ "  $_ = $proceed(30.0f, lFaith);"
	                        		+ "}else{"
	                        		+ "  $_ = $proceed($$);"
	                        		+ "}");
	                        return;
	                    }
	                }
	            });*/
	            replace = "$_ = $proceed(true);";
	            Util.setReason("Minor change for custom priest faith.");
	            Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFaith", "setPriest", replace);
	            /*ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
	                public void edit(MethodCall m) throws CannotCompileException {
	                    if (m.getMethodName().equals("setPriest")) {
	                        m.replace("$_ = $proceed(true);");
	                        return;
	                    }
	                }
	            });*/
	            replace = "$_ = null;";
	            Util.setReason("Minor change for custom priest faith.");
	            Util.instrumentDeclared(thisClass, ctDbPlayerInfo, "setFaith", "sendAlertServerMessage", replace);
	            /*ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
	                public void edit(MethodCall m) throws CannotCompileException {
	                    if (m.getMethodName().equals("sendAlertServerMessage")) {
	                        m.replace("$_ = null;");
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
