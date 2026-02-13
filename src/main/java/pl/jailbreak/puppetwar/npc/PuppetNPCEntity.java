package pl.jailbreak.puppetwar.npc;

import java.util.UUID;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import pl.jailbreak.puppetwar.puppet.PuppetAbility;
import pl.jailbreak.puppetwar.puppet.PuppetDefinition;

/**
 * Represents a spawned puppet NPC entity in the world.
 * Wraps the Hytale NPC reference and tracks puppet-specific state.
 */
public class PuppetNPCEntity {

    private final UUID id;
    private final PuppetDefinition definition;
    private final int team;
    private final Vector3d spawnPosition;
    
    private Ref<EntityStore> npcRef;
    private int currentHp;
    private int maxHp;
    private boolean dead;
    
    // Combat state
    private int abilityCooldown;
    private UUID currentTarget;
    private long lastAttackTime;
    
    public PuppetNPCEntity(UUID id, PuppetDefinition definition, int team, Vector3d spawnPosition) {
        this.id = id;
        this.definition = definition;
        this.team = team;
        this.spawnPosition = spawnPosition;
        this.maxHp = definition.getEffectiveHp();
        this.currentHp = maxHp;
        this.dead = false;
        this.abilityCooldown = 0;
        this.lastAttackTime = 0;
    }

    /**
     * Spawn the NPC in the world
     */
    public boolean spawn(World world, NPCPlugin npcPlugin) {
        if (world == null || npcPlugin == null) {
            System.out.println("[PuppetWars] Cannot spawn - world or NPCPlugin is null");
            return false;
        }

        try {
            // Get NPC type based on body type
            String npcType = getNPCTypeForBody();
            
            // Spawn NPC at position
            // Note: This is a placeholder - actual Hytale API may differ
            // You'll need to use the correct NPCPlugin.spawnNPC method
            System.out.println("[PuppetWars] Spawning NPC type: " + npcType + " at " + spawnPosition);
            
            // TODO: Replace with actual Hytale NPC spawning
            // npcRef = npcPlugin.spawnNPC(world, spawnPosition, npcType);
            
            // For now, we'll simulate successful spawn
            // In real implementation, check if npcRef is valid
            
            return true;
            
        } catch (Exception e) {
            System.out.println("[PuppetWars] Error spawning NPC: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get NPC type string based on puppet body type
     */
    private String getNPCTypeForBody() {
        return switch (definition.getBodyType()) {
            case LIGHT -> "hytale:npc/humanoid_light";
            case MEDIUM -> "hytale:npc/humanoid_medium";
            case HEAVY -> "hytale:npc/humanoid_heavy";
            case FLYING -> "hytale:npc/flying_creature";
            case COLOSSUS -> "hytale:npc/giant";
        };
    }

    /**
     * Apply damage to this puppet
     */
    public void takeDamage(int damage) {
        if (dead) return;
        
        currentHp -= damage;
        if (currentHp <= 0) {
            currentHp = 0;
            dead = true;
            onDeath();
        }
    }

    /**
     * Heal this puppet
     */
    public void heal(int amount) {
        if (dead) return;
        
        currentHp = Math.min(currentHp + amount, maxHp);
    }

    /**
     * Handle puppet death
     */
    private void onDeath() {
        System.out.println("[PuppetWars] Puppet died: " + definition.getName() + " (Team " + team + ")");
        
        // Trigger death ability if applicable
        if (definition.hasAbility() && definition.getAbility() == PuppetAbility.DEATH_EXPLOSION) {
            // Death explosion will be handled by combat system
            System.out.println("[PuppetWars] Death explosion triggered for " + definition.getName());
        }
    }

    /**
     * Despawn the NPC from the world
     */
    public void despawn() {
        if (npcRef != null && npcRef.isValid()) {
            try {
                // TODO: Use actual Hytale API to remove NPC
                // npcPlugin.despawnNPC(npcRef);
                System.out.println("[PuppetWars] Despawned puppet: " + definition.getName());
            } catch (Exception e) {
                System.out.println("[PuppetWars] Error despawning NPC: " + e.getMessage());
            }
        }
    }

    /**
     * Update puppet state (called each tick)
     */
    public void tick() {
        if (dead) return;
        
        // Decrease ability cooldown
        if (abilityCooldown > 0) {
            abilityCooldown--;
        }
        
        // Apply passive abilities
        applyPassiveAbilities();
    }

    /**
     * Apply passive ability effects
     */
    private void applyPassiveAbilities() {
        if (!definition.hasAbility() || !definition.getAbility().isPassive()) {
            return;
        }
        
        switch (definition.getAbility()) {
            case REGENERATION:
                // Heal 3 HP per tick if not in combat
                if (System.currentTimeMillis() - lastAttackTime > 5000) {
                    heal(3);
                }
                break;
            case POWER_AURA:
                // Buff nearby allies - handled by combat system
                break;
            default:
                break;
        }
    }

    /**
     * Use ability
     */
    public boolean useAbility() {
        if (dead || !definition.hasAbility()) {
            return false;
        }
        
        PuppetAbility ability = definition.getAbility();
        if (ability.isPassive() || abilityCooldown > 0) {
            return false;
        }
        
        // Set cooldown
        abilityCooldown = ability.getCooldownSeconds();
        
        System.out.println("[PuppetWars] " + definition.getName() + " uses " + ability.getDisplayName());
        return true;
    }

    /**
     * Check if ability is ready
     */
    public boolean isAbilityReady() {
        return definition.hasAbility() 
            && !definition.getAbility().isPassive() 
            && abilityCooldown == 0;
    }

    /**
     * Get current position (from NPC ref or spawn position)
     */
    public Vector3d getPosition() {
        // TODO: Get actual position from NPC entity
        // For now, return spawn position
        return spawnPosition;
    }

    /**
     * Set current target
     */
    public void setTarget(UUID targetId) {
        this.currentTarget = targetId;
    }

    /**
     * Mark that this puppet attacked
     */
    public void markAttack() {
        this.lastAttackTime = System.currentTimeMillis();
    }

    // Getters
    public UUID getId() { return id; }
    public PuppetDefinition getDefinition() { return definition; }
    public int getTeam() { return team; }
    public int getCurrentHp() { return currentHp; }
    public int getMaxHp() { return maxHp; }
    public boolean isDead() { return dead; }
    public int getAbilityCooldown() { return abilityCooldown; }
    public UUID getCurrentTarget() { return currentTarget; }
    public Ref<EntityStore> getNpcRef() { return npcRef; }
    
    public double getHpPercentage() {
        return maxHp > 0 ? (double) currentHp / maxHp : 0.0;
    }
    
    public String getHpBar() {
        int bars = 10;
        int filled = (int) (getHpPercentage() * bars);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < bars; i++) {
            sb.append(i < filled ? "█" : "░");
        }
        sb.append("] ").append(currentHp).append("/").append(maxHp);
        return sb.toString();
    }
}
