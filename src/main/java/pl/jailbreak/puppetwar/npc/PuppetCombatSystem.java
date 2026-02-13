package pl.jailbreak.puppetwar.npc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.hypixel.hytale.math.vector.Vector3d;

import pl.jailbreak.puppetwar.PuppetWarConfig;
import pl.jailbreak.puppetwar.match.BattleCommand;
import pl.jailbreak.puppetwar.puppet.BehaviorSlot;
import pl.jailbreak.puppetwar.puppet.PuppetAbility;
import pl.jailbreak.puppetwar.puppet.PuppetAction;
import pl.jailbreak.puppetwar.puppet.PuppetBodyType;
import pl.jailbreak.puppetwar.puppet.PuppetCondition;
import pl.jailbreak.puppetwar.puppet.PuppetDefinition;

/**
 * Handles combat logic between puppet NPCs.
 * Processes attacks, abilities, healing, and death effects.
 */
public class PuppetCombatSystem {

    private final PuppetNPCManager npcManager;
    private final Map<UUID, Long> lastAttackTime = new HashMap<>();
    private final Map<UUID, BattleCommand> activeCommands = new HashMap<>();
    private final Map<UUID, Integer> commandDurations = new HashMap<>();
    
    // Core entities (not actual NPCs, just tracking)
    private int core1Hp = PuppetWarConfig.CORE_HP;
    private int core2Hp = PuppetWarConfig.CORE_HP;
    private Vector3d core1Position;
    private Vector3d core2Position;

    public PuppetCombatSystem(PuppetNPCManager npcManager) {
        this.npcManager = npcManager;
    }

    /**
     * Initialize core positions
     */
    public void initializeCores(Vector3d pos1, Vector3d pos2) {
        this.core1Position = pos1;
        this.core2Position = pos2;
        this.core1Hp = PuppetWarConfig.CORE_HP;
        this.core2Hp = PuppetWarConfig.CORE_HP;
    }

    /**
     * Reset combat state for new round
     */
    public void reset() {
        lastAttackTime.clear();
        activeCommands.clear();
        commandDurations.clear();
        core1Hp = PuppetWarConfig.CORE_HP;
        core2Hp = PuppetWarConfig.CORE_HP;
    }

    /**
     * Main combat tick - process all puppet actions
     */
    public List<String> processCombatTick() {
        List<String> events = new ArrayList<>();
        
        // Update command durations
        tickCommands();
        
        // Process team 1 puppets
        for (PuppetNPCEntity puppet : npcManager.getTeamPuppets(1)) {
            if (!puppet.isDead()) {
                puppet.tick();
                processPuppetAction(puppet, 2, events);
            }
        }
        
        // Process team 2 puppets
        for (PuppetNPCEntity puppet : npcManager.getTeamPuppets(2)) {
            if (!puppet.isDead()) {
                puppet.tick();
                processPuppetAction(puppet, 1, events);
            }
        }
        
        // Check for death explosions
        checkDeathAbilities(1, events);
        checkDeathAbilities(2, events);
        
        return events;
    }

    /**
     * Process a single puppet's action
     */
    private void processPuppetAction(PuppetNPCEntity puppet, int enemyTeam, List<String> events) {
        PuppetDefinition def = puppet.getDefinition();
        int myTeam = puppet.getTeam();
        
        // Determine action based on behavior slots and active commands
        PuppetAction action = determineAction(puppet, enemyTeam);
        
        // Execute the action
        executeAction(puppet, action, enemyTeam, events);
    }

    /**
     * Determine what action a puppet should take
     */
    private PuppetAction determineAction(PuppetNPCEntity puppet, int enemyTeam) {
        // Check for active battle command first
        BattleCommand cmd = activeCommands.get(puppet.getTeam() == 1 ? UUID.randomUUID() : UUID.randomUUID());
        if (cmd != null) {
            return switch (cmd) {
                case CHARGE -> PuppetAction.ATTACK_CORE;
                case RETREAT -> PuppetAction.DEFEND_CORE;
                case FOCUS -> PuppetAction.ATTACK_WEAKEST;
            };
        }
        
        // Evaluate behavior slots by priority
        List<BehaviorSlot> slots = new ArrayList<>(puppet.getDefinition().getBehaviorSlots());
        slots.sort((a, b) -> b.getPriority() - a.getPriority());
        
        for (BehaviorSlot slot : slots) {
            if (evaluateCondition(slot.getCondition(), slot.getConditionParam(), puppet, enemyTeam)) {
                return slot.getAction();
            }
        }
        
        // Default action
        return PuppetAction.ATTACK_NEAREST;
    }

    /**
     * Evaluate if a condition is met
     */
    private boolean evaluateCondition(PuppetCondition condition, int param, PuppetNPCEntity puppet, int enemyTeam) {
        return switch (condition) {
            case ALWAYS -> true;
            case ENEMY_IN_RANGE -> npcManager.getAlivePuppetCount(enemyTeam) > 0;
            case HP_BELOW -> {
                int hp = puppet.getCurrentHp();
                int max = puppet.getMaxHp();
                yield max > 0 && (hp * 100 / max) < param;
            }
            case ALLY_HP_LOW -> {
                PuppetNPCEntity ally = npcManager.findLowestHpAlly(puppet.getTeam(), puppet.getId());
                if (ally == null) yield false;
                int hp = ally.getCurrentHp();
                int max = ally.getMaxHp();
                yield max > 0 && (hp * 100 / max) < param;
            }
            case ENEMIES_OUTNUMBER -> npcManager.getAlivePuppetCount(enemyTeam) > param;
            case CORE_ATTACKED -> {
                int coreHp = puppet.getTeam() == 1 ? core1Hp : core2Hp;
                yield coreHp < PuppetWarConfig.CORE_HP * 0.8;
            }
            case ABILITY_READY -> puppet.isAbilityReady();
        };
    }

    /**
     * Execute a puppet action
     */
    private void executeAction(PuppetNPCEntity puppet, PuppetAction action, int enemyTeam, List<String> events) {
        PuppetDefinition def = puppet.getDefinition();
        int baseDmg = def.getEffectiveDamage();
        
        switch (action) {
            case ATTACK_NEAREST -> {
                PuppetNPCEntity target = npcManager.findNearestEnemy(puppet.getTeam(), puppet.getPosition());
                if (target != null) {
                    attackPuppet(puppet, target, baseDmg, events);
                } else {
                    attackCore(puppet, enemyTeam, baseDmg, events);
                }
            }
            case ATTACK_WEAKEST -> {
                PuppetNPCEntity target = npcManager.findWeakestEnemy(puppet.getTeam());
                if (target != null) {
                    attackPuppet(puppet, target, baseDmg, events);
                } else {
                    attackCore(puppet, enemyTeam, baseDmg, events);
                }
            }
            case ATTACK_CORE -> attackCore(puppet, enemyTeam, baseDmg, events);
            case DEFEND_CORE -> {
                PuppetNPCEntity target = npcManager.findNearestEnemy(puppet.getTeam(), puppet.getPosition());
                if (target != null) {
                    attackPuppet(puppet, target, (int)(baseDmg * 0.7), events);
                }
            }
            case HEAL_ALLY -> {
                if (def.getWeapon().isHealer()) {
                    PuppetNPCEntity ally = npcManager.findLowestHpAlly(puppet.getTeam(), puppet.getId());
                    if (ally != null) {
                        int heal = 8 + (int)(Math.random() * 4);
                        npcManager.healPuppet(ally.getId(), heal);
                        events.add(puppet.getDefinition().getName() + " heals " + ally.getDefinition().getName() + " for " + heal + " HP");
                    }
                }
            }
            case RETREAT -> {
                int selfHeal = 3 + (int)(Math.random() * 3);
                npcManager.healPuppet(puppet.getId(), selfHeal);
                events.add(puppet.getDefinition().getName() + " retreats (+"+selfHeal+" HP)");
            }
            case USE_ABILITY -> {
                if (puppet.useAbility()) {
                    executeAbility(puppet, enemyTeam, events);
                }
            }
            case PATROL, STAND_GROUND -> {
                PuppetNPCEntity target = npcManager.findNearestEnemy(puppet.getTeam(), puppet.getPosition());
                if (target != null) {
                    attackPuppet(puppet, target, (int)(baseDmg * 0.8), events);
                }
            }
            case FOLLOW_COLOSSUS -> {
                boolean hasColossus = npcManager.getTeamPuppets(puppet.getTeam()).stream()
                    .anyMatch(p -> p.getDefinition().getBodyType() == PuppetBodyType.COLOSSUS);
                double mult = hasColossus ? 1.2 : 0.9;
                PuppetNPCEntity target = npcManager.findNearestEnemy(puppet.getTeam(), puppet.getPosition());
                if (target != null) {
                    attackPuppet(puppet, target, (int)(baseDmg * mult), events);
                }
            }
            case FLANK -> {
                PuppetNPCEntity target = npcManager.findWeakestEnemy(puppet.getTeam());
                if (target != null) {
                    int dmg = (int)(baseDmg * (1.0 + Math.random() * 0.5));
                    npcManager.damagePuppet(target.getId(), dmg);
                    events.add(puppet.getDefinition().getName() + " flanks " + target.getDefinition().getName() + " for " + dmg + " dmg!");
                }
            }
        }
    }

    /**
     * Attack another puppet
     */
    private void attackPuppet(PuppetNPCEntity attacker, PuppetNPCEntity target, int baseDmg, List<String> events) {
        int damage = calculateDamage(attacker, target, baseDmg);
        
        if (damage <= 0) {
            events.add(attacker.getDefinition().getName() + " attacks " + target.getDefinition().getName() + " - DODGED!");
            return;
        }
        
        npcManager.damagePuppet(target.getId(), damage);
        attacker.markAttack();
        
        String msg = attacker.getDefinition().getName() + " attacks " + target.getDefinition().getName() + " for " + damage + " dmg";
        
        // Check for special weapon effects
        if (attacker.getDefinition().getWeapon().hasKnockback()) {
            msg += " KNOCKBACK!";
        }
        
        if (target.isDead()) {
            msg += " [KILLED]";
        }
        
        events.add(msg);
    }

    /**
     * Attack enemy core
     */
    private void attackCore(PuppetNPCEntity attacker, int enemyTeam, int baseDmg, List<String> events) {
        int damage = (int)(baseDmg * (0.8 + Math.random() * 0.4));
        
        if (enemyTeam == 1) {
            core1Hp -= damage;
            core1Hp = Math.max(0, core1Hp);
        } else {
            core2Hp -= damage;
            core2Hp = Math.max(0, core2Hp);
        }
        
        attacker.markAttack();
        events.add(attacker.getDefinition().getName() + " attacks enemy Core for " + damage + " dmg!");
    }

    /**
     * Calculate damage with armor, weapon effects, etc.
     */
    private int calculateDamage(PuppetNPCEntity attacker, PuppetNPCEntity target, int baseDmg) {
        int raw = (int)(baseDmg * (0.7 + Math.random() * 0.6));
        
        // Apply armor reduction
        double armorMult = target.getDefinition().getArmor().getDamageMultiplier();
        
        // Crossbow ignores 50% armor
        if (attacker.getDefinition().getWeapon().ignoresArmor()) {
            armorMult = 1.0 - (target.getDefinition().getArmor().getDamageReduction() / 200.0);
        }
        
        raw = (int)(raw * armorMult);
        
        // Shield blocks 50% damage
        if (target.getDefinition().getWeapon().blocksDamage()) {
            raw = raw / 2;
        }
        
        // Dodge chance based on speed
        double speed = target.getDefinition().getEffectiveSpeed();
        if (Math.random() < (speed - 0.5) * 0.2) {
            return 0; // Dodged
        }
        
        return Math.max(1, raw);
    }

    /**
     * Execute ability effects
     */
    private void executeAbility(PuppetNPCEntity puppet, int enemyTeam, List<String> events) {
        PuppetAbility ability = puppet.getDefinition().getAbility();
        if (ability == null) return;
        
        switch (ability) {
            case CHARGE -> {
                PuppetNPCEntity target = npcManager.findNearestEnemy(puppet.getTeam(), puppet.getPosition());
                if (target != null) {
                    npcManager.damagePuppet(target.getId(), 10);
                    events.add(puppet.getDefinition().getName() + " uses CHARGE! +10 DMG to " + target.getDefinition().getName());
                }
            }
            case ENERGY_SHIELD -> {
                npcManager.healPuppet(puppet.getId(), 40);
                events.add(puppet.getDefinition().getName() + " activates ENERGY SHIELD! (+40 absorb)");
            }
            case TAUNT -> {
                events.add(puppet.getDefinition().getName() + " uses TAUNT! Enemies focus it!");
                // Taunt logic would be handled by AI system
            }
            case INVISIBILITY -> {
                npcManager.healPuppet(puppet.getId(), 15);
                events.add(puppet.getDefinition().getName() + " goes INVISIBLE!");
            }
            default -> events.add(puppet.getDefinition().getName() + " uses " + ability.getDisplayName() + "!");
        }
    }

    /**
     * Check for death explosion abilities
     */
    private void checkDeathAbilities(int team, List<String> events) {
        List<PuppetNPCEntity> puppets = npcManager.getTeamPuppets(team);
        int enemyTeam = team == 1 ? 2 : 1;
        
        for (PuppetNPCEntity puppet : puppets) {
            if (puppet.isDead() && puppet.getDefinition().hasAbility() 
                && puppet.getDefinition().getAbility() == PuppetAbility.DEATH_EXPLOSION
                && puppet.getAbilityCooldown() != -999) {
                
                // Mark as triggered
                // Apply AoE damage to nearby enemies
                int hits = 0;
                for (PuppetNPCEntity enemy : npcManager.getTeamPuppets(enemyTeam)) {
                    if (!enemy.isDead()) {
                        npcManager.damagePuppet(enemy.getId(), 25);
                        hits++;
                    }
                }
                
                if (hits > 0) {
                    events.add(puppet.getDefinition().getName() + " EXPLODES! 25 dmg to " + hits + " enemies!");
                }
            }
        }
    }

    /**
     * Set active battle command for a team
     */
    public void setCommand(int team, BattleCommand command, int duration) {
        UUID key = team == 1 ? new UUID(1, 0) : new UUID(2, 0);
        activeCommands.put(key, command);
        commandDurations.put(key, duration);
    }

    /**
     * Tick command durations
     */
    private void tickCommands() {
        Iterator<Map.Entry<UUID, Integer>> it = commandDurations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            int newDuration = entry.getValue() - 1;
            if (newDuration <= 0) {
                activeCommands.remove(entry.getKey());
                it.remove();
            } else {
                entry.setValue(newDuration);
            }
        }
    }

    // Getters
    public int getCore1Hp() { return core1Hp; }
    public int getCore2Hp() { return core2Hp; }
    public boolean isCore1Destroyed() { return core1Hp <= 0; }
    public boolean isCore2Destroyed() { return core2Hp <= 0; }
}
