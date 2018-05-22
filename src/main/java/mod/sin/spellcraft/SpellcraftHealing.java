package mod.sin.spellcraft;

import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import mod.sin.lib.Util;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.HashMap;
import java.util.logging.Logger;

public class SpellcraftHealing {
    protected static Logger logger = Logger.getLogger(SpellcraftHealing.class.getName());

    public static class HealingResist{
        public Creature creature;
        public long lastUpdated;
        public double currentResistance;
        public long fullyExpires;
        public HealingResist(Creature creature, long lastUpdated, double currentResistance){
            this.creature = creature;
            this.lastUpdated = lastUpdated;
            this.currentResistance = currentResistance;
            this.fullyExpires = lastUpdated;
        }
    }

    protected static HashMap<Long,HealingResist> resistances = new HashMap<>();
    protected static final double RECOVERY_SECOND = 0.0005d;

    protected static double updateResistance(Creature creature, HealingResist res, double additionalResistance){
        long timeDelta = System.currentTimeMillis() - res.lastUpdated;
        double secondsPassed = timeDelta / (double) TimeConstants.SECOND_MILLIS;
        res.currentResistance = Math.min(1d, res.currentResistance+(secondsPassed*RECOVERY_SECOND));
        res.currentResistance = Math.max(0d, res.currentResistance-additionalResistance);
        res.lastUpdated = System.currentTimeMillis();
        double secondsUntilFullyHealed = (1-(res.currentResistance))/RECOVERY_SECOND;
        res.fullyExpires = (long) (System.currentTimeMillis()+(secondsUntilFullyHealed*TimeConstants.SECOND_MILLIS));
        creature.getCommunicator().sendAddStatusEffect(SpellEffectsEnum.RES_HEAL, (int) secondsUntilFullyHealed);
        return res.currentResistance;
    }
    public static double getHealingResistance(Creature creature){
        if(resistances.containsKey(creature.getWurmId())) {
            HealingResist res = resistances.get(creature.getWurmId());
            return updateResistance(creature, res, 0);
        }
        return 1f;
    }
    public static void addHealingResistance(Creature creature, double power){
        double reduction = power / (65535d*2d);
        if(resistances.containsKey(creature.getWurmId())){
            HealingResist res = resistances.get(creature.getWurmId());
            updateResistance(creature, res, reduction);
        }else{
            resistances.put(creature.getWurmId(), new HealingResist(creature, System.currentTimeMillis(), 1-reduction));
            updateResistance(creature, resistances.get(creature.getWurmId()), 0);
        }
    }

    public static void doCureSpell(Skill castSkill, double power, Creature performer, Wound target, String name, double basePower, double multPower){
        boolean doeff = true;
        if (target != null && target.getCreature() != null) {
            Creature tarCret = target.getCreature();
            if (tarCret.isReborn()) {
                doeff = false;
                performer.getCommunicator().sendNormalServerMessage("The wound grows.", (byte) 3);
                target.modifySeverity(1000);
            }
            if (doeff) {
                double resistance = getHealingResistance(tarCret);
                double toHeal = basePower+(power*multPower);
                toHeal *= resistance;
                if(performer.getCultist() != null && performer.getCultist().healsFaster()){
                    toHeal *= 1.5;
                }
                VolaTile t = Zones.getTileOrNull(target.getCreature().getTileX(), target.getCreature().getTileY(), target.getCreature().isOnSurface());
                if (t != null) {
                    t.sendAttachCreatureEffect(target.getCreature(), (byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
                }
                if (target.getSeverity() <= toHeal) {
                    addHealingResistance(tarCret, target.getSeverity());
                    target.heal();
                    performer.getCommunicator().sendNormalServerMessage("You completely heal the "+target.getName()+" wound with "+name+".", (byte) 2);
                    if(performer != tarCret) {
                        tarCret.getCommunicator().sendNormalServerMessage(performer.getName() + " completely heals your "+target.getName()+" wound with " + name + ".", (byte) 2);
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You partially heal the "+target.getName()+" wound with "+name+".", (byte) 2);
                    if(performer != tarCret) {
                        tarCret.getCommunicator().sendNormalServerMessage(performer.getName() + " partially heals your "+target.getName()+" wound with " + name + ".", (byte) 2);
                    }
                    addHealingResistance(tarCret, toHeal);
                    target.modifySeverity((int) -toHeal);
                }
            }
        }else{
            logger.info(name+" somehow healed a wound on a creature that doesn't exist?");
        }
    }
    public static void doCureLight(Skill castSkill, double power, Creature performer, Wound target){
        doCureSpell(castSkill, power, performer, target, "Cure light", 3000, 100);
    }
    public static void doCureMedium(Skill castSkill, double power, Creature performer, Wound target){
        doCureSpell(castSkill, power, performer, target, "Cure medium", 6000, 200);
    }
    public static void doCureSerious(Skill castSkill, double power, Creature performer, Wound target){
        doCureSpell(castSkill, power, performer, target, "Cure medium", 12000, 400);
    }
    public static void doHeal(Skill castSkill, double power, Creature performer, Creature target){
        String name = "Heal";
        boolean doeff = true;
        if (target != null) {
            if (target.isReborn()) {
                doeff = false;
                performer.getCommunicator().sendNormalServerMessage("You slay " + target.getNameWithGenus() + ".", (byte) 4);
                Server.getInstance().broadCastAction(performer.getName() + " slays " + target.getNameWithGenus() + "!", performer, 5);
                target.addAttacker(performer);
                target.die(false);
            }
            if (doeff) {
                Wounds tWounds = target.getBody().getWounds();
                if (tWounds == null) {
                    performer.getCommunicator().sendNormalServerMessage(target.getName()+" has no wounds to heal.", (byte) 4);
                    return;
                }
                double resistance = getHealingResistance(target);
                double healingPool = 30000+(power*500);
                if(performer.getCultist() != null && performer.getCultist().healsFaster()){
                    healingPool *= 1.5;
                }
                healingPool *= resistance;
                //int healingPool = (int)(Math.max(20.0, power) / 100.0 * 65535.0 * 1.5);
                for (Wound w : tWounds.getWounds()) {
                    if (w.getSeverity() > healingPool) continue;
                    healingPool = healingPool - w.getSeverity();
                    addHealingResistance(target, w.getSeverity());
                    w.heal();
                }
                if (tWounds.getWounds().length > 0 && healingPool > 0) {
                    addHealingResistance(target, healingPool);
                    tWounds.getWounds()[Server.rand.nextInt(tWounds.getWounds().length)].modifySeverity((int) -healingPool);
                }
                if (tWounds.getWounds().length > 0) {
                    performer.getCommunicator().sendNormalServerMessage("You heal some of " + target.getNameWithGenus() + "'s wounds with "+name+".", (byte) 4);
                    if(performer != target) {
                        target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " heals some of your wounds with "+name+".", (byte) 4);
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You fully heal " + target.getNameWithGenus() + " with "+name+".", (byte) 4);
                    if(performer != target) {
                        target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " heals your wounds with " + name + ".", (byte) 4);
                    }
                }
                VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
                if (t != null) {
                    t.sendAttachCreatureEffect(target, (byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
                }
            }
        }
    }
    public static void doLightOfFo(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset){
        String name = "Light of Fo";
        performer.getCommunicator().sendNormalServerMessage("You cast "+name+"!", (byte) 4);
        Server.getInstance().broadCastAction(performer.getName() + " casts " + name + "!", performer, 10);
        int sx = Zones.safeTileX(tilex - (int)Math.max(3.0, power / 20.0));
        int sy = Zones.safeTileY(tiley - (int)Math.max(3.0, power / 20.0));
        int ex = Zones.safeTileX(tilex + (int)Math.max(3.0, power / 20.0));
        int ey = Zones.safeTileY(tiley + (int)Math.max(3.0, power / 20.0));
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
                if (t == null) continue;
                for (Creature lCret : t.getCreatures()) {
                    Village pVill;
                    Village lVill;
                    boolean dontHeal = true;
                    if (lCret.getKingdomId() == performer.getKingdomId() || lCret.getAttitude(performer) == 1) {
                        dontHeal = false;
                    }
                    if ((lVill = lCret.getCitizenVillage()) != null && lVill.isEnemy(performer)) {
                        dontHeal = true;
                    }
                    if (!dontHeal && (pVill = performer.getCitizenVillage()) != null && pVill.isEnemy(lCret)) {
                        dontHeal = true;
                    }
                    if (dontHeal) continue;
                    Wounds tWounds = lCret.getBody().getWounds();
                    if (tWounds == null) {
                        continue;
                    }
                    double resistance = getHealingResistance(lCret);
                    double healingPool = 15000+(power*500);
                    if(performer.getCultist() != null && performer.getCultist().healsFaster()){
                        healingPool *= 1.5;
                    }
                    int woundsHealed = 0;
                    healingPool *= resistance;
                    int maxWoundHeal = (int) (healingPool*0.2);
                    for (Wound w : tWounds.getWounds()) {
                        if (woundsHealed >= 5) break;
                        if (w.getSeverity() < maxWoundHeal) continue;
                        healingPool = healingPool - maxWoundHeal;
                        addHealingResistance(lCret, maxWoundHeal);
                        w.modifySeverity(-maxWoundHeal);
                        woundsHealed++;
                    }
                    if(woundsHealed < 5){
                        for (Wound w : tWounds.getWounds()) {
                            if (woundsHealed >= 5 || healingPool <= 0) break;
                            if(w.getSeverity() >= healingPool) {
                                addHealingResistance(lCret, healingPool);
                                w.modifySeverity((int) -healingPool);
                                break;
                            }else{
                                addHealingResistance(lCret, w.getSeverity());
                                healingPool = healingPool - w.getSeverity();
                                w.heal();
                                woundsHealed++;
                            }
                        }
                    }
                    if(woundsHealed > 0) {
                        lCret.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus()+" heals some of your wounds with "+name, (byte) 4);
                        VolaTile tt = Zones.getTileOrNull(lCret.getTileX(), lCret.getTileY(), lCret.isOnSurface());
                        if (tt == null) continue;
                        tt.sendAttachCreatureEffect(lCret, (byte) 11, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
                    }
                }
            }
        }
    }
    public static void doScornOfLibila(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset){
        String name = "Scorn of Libila";
        performer.getCommunicator().sendNormalServerMessage("You cast "+name+"!", (byte) 4);
        Structure currstr = performer.getCurrentTile().getStructure();
        int sx = Zones.safeTileX(performer.getTileX() - (int)Math.max(3.0, power / 20.0));
        int sy = Zones.safeTileY(performer.getTileY() - (int)Math.max(3.0, power / 20.0));
        int ex = Zones.safeTileX(performer.getTileX() + (int)Math.max(3.0, power / 20.0));
        int ey = Zones.safeTileY(performer.getTileY() + (int)Math.max(3.0, power / 20.0));
        int damdealt = 0;
        if(SpellcraftMod.scornHealWithoutDamage){
            damdealt = 999;
        }
        double healingPool = 60000+(power*1000);
        if(performer.getCultist() != null && performer.getCultist().healsFaster()){
            healingPool *= 1.5;
        }
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
                if (t == null) continue;
                Creature[] crets2 = t.getCreatures();
                for (Creature lCret : crets2) {
                    if (lCret.isUnique() || lCret.isInvulnerable() || lCret.getAttitude(performer) != 2) continue;
                    t.sendAttachCreatureEffect(lCret, (byte) 8, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
                    healingPool += power*160;
                    ++damdealt;
                    if (lCret.addWoundOfType(performer, Wound.TYPE_INTERNAL, 1, false, 1.0f, false, power * 80.0 * (double)lCret.addSpellResistance((short) 448))) continue;
                    lCret.setTarget(performer.getWurmId(), false);
                }
            }
        }
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
                if (t != null) {
                    Creature[] crets2 = t.getCreatures();
                    for (Creature lCret : crets2) {
                        Village pVill;
                        Village lVill;
                        boolean dontHeal = true;
                        if (lCret.getKingdomId() == performer.getKingdomId() || lCret.getAttitude(performer) == 1) {
                            dontHeal = false;
                        }
                        if ((lVill = lCret.getCitizenVillage()) != null && lVill.isEnemy(performer)) {
                            dontHeal = true;
                        }
                        if (!dontHeal && (pVill = performer.getCitizenVillage()) != null && pVill.isEnemy(lCret)) {
                            dontHeal = true;
                        }
                        if (dontHeal) continue;
                        double resistance = getHealingResistance(lCret);
                        double tempHealingPool = healingPool;
                        tempHealingPool *= resistance;
                        Wounds tWounds = lCret.getBody().getWounds();
                        if (tWounds == null) {
                            continue;
                        }
                        Wound woundToHeal = null;
                        for (Wound w : tWounds.getWounds()) {
                            if(woundToHeal == null){
                                woundToHeal = w;
                            }else{
                                if(w.getSeverity() < woundToHeal.getSeverity()){
                                    woundToHeal = w;
                                }
                            }
                        }
                        if(woundToHeal == null || damdealt <= 0) continue;
                        if(woundToHeal.getSeverity() > tempHealingPool){
                            lCret.getCommunicator().sendNormalServerMessage(performer.getName()+" partially heals your "+woundToHeal.getName()+" wound with "+name, (byte) 4);
                            addHealingResistance(lCret, tempHealingPool);
                            healingPool -= tempHealingPool;
                            woundToHeal.modifySeverity((int) -tempHealingPool);
                            t.sendAttachCreatureEffect(lCret, (byte) 9, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
                            damdealt--;
                        }else{
                            lCret.getCommunicator().sendNormalServerMessage(performer.getName()+" fully heals your "+woundToHeal.getName()+" wound with "+name, (byte) 4);
                            addHealingResistance(lCret, woundToHeal.getSeverity());
                            healingPool -= woundToHeal.getSeverity();
                            woundToHeal.heal();
                            t.sendAttachCreatureEffect(lCret, (byte) 9, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
                            damdealt--;
                            woundToHeal = null;
                            for (Wound w : tWounds.getWounds()) {
                                if(woundToHeal == null){
                                    woundToHeal = w;
                                }else{
                                    if(w.getSeverity() < woundToHeal.getSeverity()){
                                        woundToHeal = w;
                                    }
                                }
                            }
                            if(woundToHeal == null || damdealt <= 0) continue;
                            if(woundToHeal.getSeverity() > tempHealingPool){
                                lCret.getCommunicator().sendNormalServerMessage(performer.getName()+" partially heals your "+woundToHeal.getName()+" wound with "+name, (byte) 4);
                                addHealingResistance(lCret, tempHealingPool);
                                healingPool -= tempHealingPool;
                                woundToHeal.modifySeverity((int) -tempHealingPool);
                                damdealt--;
                            }else {
                                lCret.getCommunicator().sendNormalServerMessage(performer.getName()+" fully heals your "+woundToHeal.getName()+" wound with "+name, (byte) 4);
                                addHealingResistance(lCret, woundToHeal.getSeverity());
                                healingPool -= woundToHeal.getSeverity();
                                woundToHeal.heal();
                                damdealt--;
                            }
                        }
                    }
                }
                if (damdealt <= 0) break;
            }
            if (damdealt <= 0) break;
        }
    }

    public static long lastPolledHealing = 0;
    public static final long pollHealingTime = TimeConstants.SECOND_MILLIS;
    public static void onServerPoll(){
        if(lastPolledHealing + pollHealingTime < System.currentTimeMillis()){
            for(Long wid : resistances.keySet()){
                HealingResist res = resistances.get(wid);
                if(res.fullyExpires < System.currentTimeMillis()){
                    if(res.creature != null){
                        res.creature.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.RES_HEAL);
                    }
                    resistances.remove(wid);
                }
            }
            lastPolledHealing = System.currentTimeMillis();
        }
    }
    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            Class<SpellcraftHealing> thisClass = SpellcraftHealing.class;
            String replace;

            Util.setReason("Healing Recode - Cure Light");
            CtClass ctCureLight = classPool.get("com.wurmonline.server.spells.CureLight");
            replace = SpellcraftHealing.class.getName()+".doCureLight($1, $2, $3, $4);";
            Util.setBodyDeclared(thisClass, ctCureLight, "doEffect", replace);

            Util.setReason("Healing Recode - Cure Medium");
            CtClass ctCureMedium = classPool.get("com.wurmonline.server.spells.CureMedium");
            replace = SpellcraftHealing.class.getName()+".doCureMedium($1, $2, $3, $4);";
            Util.setBodyDeclared(thisClass, ctCureMedium, "doEffect", replace);

            Util.setReason("Healing Recode - Cure Serious");
            CtClass ctCureSerious = classPool.get("com.wurmonline.server.spells.CureSerious");
            replace = SpellcraftHealing.class.getName()+".doCureSerious($1, $2, $3, $4);";
            Util.setBodyDeclared(thisClass, ctCureSerious, "doEffect", replace);

            Util.setReason("Healing Recode - Heal");
            CtClass ctHeal = classPool.get("com.wurmonline.server.spells.Heal");
            replace = SpellcraftHealing.class.getName()+".doHeal($1, $2, $3, $4);";
            Util.setBodyDeclared(thisClass, ctHeal, "doEffect", replace);

            Util.setReason("Healing Recode - Light of Fo");
            CtClass ctLightOfFo = classPool.get("com.wurmonline.server.spells.LightOfFo");
            replace = SpellcraftHealing.class.getName()+".doLightOfFo($1, $2, $3, $4, $5, $6, $7);";
            Util.setBodyDeclared(thisClass, ctLightOfFo, "doEffect", replace);

            Util.setReason("Healing Recode - Scorn of Libila");
            CtClass ctScornOfLibila = classPool.get("com.wurmonline.server.spells.ScornOfLibila");
            replace = SpellcraftHealing.class.getName()+".doScornOfLibila($1, $2, $3, $4, $5, $6, $7);";
            Util.setBodyDeclared(thisClass, ctScornOfLibila, "doEffect", replace);

        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }
}
