package com.wurmonline.server.spells;

import java.util.Arrays;
import java.util.List;

public enum SpellcraftSpell {
    HARDEN("Harden", "harden", (byte) 110),
    PHASING("Phasing", "phasing", (byte) 111),
    REPLENISH("Replenish", "replenish", (byte) 112),
    EXPAND("Expand", "expand", (byte) 113),
    EFFICIENCY("Efficiency", "efficiency", (byte) 114),
    QUARRY("Quarry", "quarry", (byte) 115),
    PROWESS("Prowess", "prowess", (byte) 116),
    INDUSTRY("Industry", "industry", (byte) 117),
    ENDURANCE("Endurance", "endurance", (byte) 118),
    ACUITY("Acuity", "acuity", (byte) 119),
    TITANFORGED("Titanforged", "titanforged", (byte) 120),
    LABOURING_SPIRIT("Labouring Spirit", "labouringSpirit", (byte) 121);
    // Base Properties
    String name;
    String propName;
    byte enchant;
    ReligiousSpell spell;
    // Configuration
    boolean enabled = true;
    List<String> gods;
    int castTime;
    int cost;
    int difficulty;
    int faith;
    long cooldown;
    SpellcraftSpell(String name, String propName, byte ench){
        this.name = name;
        this.propName = propName;
        this.enchant = ench;
    }
    public String getName(){
        return name;
    }
    public String getPropertyName(){
        return propName;
    }
    public byte getEnchant(){
        return enchant;
    }
    public ReligiousSpell getSpell(){
        return spell;
    }
    public void setSpell(ReligiousSpell spell){
        this.spell = spell;
    }
    public boolean isEnabled(){
        return enabled;
    }
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }
    public List<String> getGods(){
        return gods;
    }
    public void setGods(String gods){
        this.gods = Arrays.asList(gods.split(","));
    }
    public int getCastTime(){
        return castTime;
    }
    public void setCastTime(int castTime){
        this.castTime = castTime;
    }
    public int getCost(){
        return cost;
    }
    public void setCost(int cost){
        this.cost = cost;
    }
    public int getDifficulty(){
        return difficulty;
    }
    public void setDifficulty(int difficulty){
        this.difficulty = difficulty;
    }
    public int getFaith(){
        return faith;
    }
    public void setFaith(int faith){
        this.faith = faith;
    }
    public long getCooldown(){
        return cooldown;
    }
    public void setCooldown(long cooldown){
        this.cooldown = cooldown;
    }
}
