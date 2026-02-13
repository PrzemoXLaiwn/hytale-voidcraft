package pl.jailbreak.puppetwar.npc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.npc.NPCPlugin;

import pl.jailbreak.puppetwar.puppet.PuppetDefinition;

/**
 * Manages the lifecycle of puppet NPCs during battle.
 * Handles spawning, tracking, damage, and cleanup of puppet entities.
 */
public class PuppetNPCManager {

    private final Map<UUID, PuppetNPCEntity> activePuppets = new ConcurrentHashMap<>();
    private final Map<Integer, List<UUID>> teamPuppets = new ConcurrentHashMap<>();
    
    private World battleWorld;
    private NPCPlugin npcPlugin;

    public PuppetNPCManager() {
        teamPuppets.put(1, new ArrayList<>());
        teamPuppets.put(2, new ArrayList<>());
    }

    /**
     * Initialize the manager with the battle world
     */
    public void initialize(World world) {
        this.battleWorld = world;
        // NPCPlugin will be accessed when needed
        System.out.println("[PuppetWars] PuppetNPCManager initialized for world: " + world.getName());
    }

    /**
     * Spawn a puppet army for a team
     */
    public boolean spawnTeamArmy(int team, List<PuppetDefinition> army, Vector3d basePosition) {
        if (battleWorld == null || npcPlugin == null) {
            System.out.println("[PuppetWars] Cannot spawn army - world or NPCPlugin not initialized");
            return false;
        }

        List<UUID> teamList = teamPuppets.get(team);
        if (teamList == null) {
            teamList = new ArrayList<>();
            teamPuppets.put(team, teamList);
        }

        int spawnIndex = 0;
        for (PuppetDefinition puppet : army) {
            Vector3d spawnPos = calculateSpawnPosition(basePosition, team, spawnIndex, army.size());
            UUID puppetId = spawnPuppet(puppet, team, spawnPos);
            
            if (puppetId != null) {
                teamList.add(puppetId);
                spawnIndex++;
            } else {
                System.out.println("[PuppetWars] Failed to spawn puppet: " + puppet.getName());
            }
        }

        System.out.println("[PuppetWars] Spawned " + spawnIndex + " puppets for team " + team);
        return spawnIndex > 0;
    }

    /**
     * Spawn a single puppet NPC
     */
    private UUID spawnPuppet(PuppetDefinition puppet, int team, Vector3d position) {
        try {
            UUID puppetId = UUID.randomUUID();
            
            // Create puppet entity wrapper
            PuppetNPCEntity puppetEntity = new PuppetNPCEntity(puppetId, puppet, team, position);
            
            // Spawn the actual NPC in the world
            if (puppetEntity.spawn(battleWorld, npcPlugin)) {
                activePuppets.put(puppetId, puppetEntity);
                System.out.println("[PuppetWars] Spawned puppet: " + puppet.getName() + " at " + position);
                return puppetId;
            }
            
        } catch (Exception e) {
            System.out.println("[PuppetWars] Error spawning puppet: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Calculate spawn position for a puppet in formation
     */
    private Vector3d calculateSpawnPosition(Vector3d basePos, int team, int index, int totalCount) {
        // Arrange puppets in a grid formation
        int columns = (int) Math.ceil(Math.sqrt(totalCount));
        int row = index / columns;
        int col = index % columns;
        
        double spacing = 2.5; // blocks between puppets
        double offsetX = (col - columns / 2.0) * spacing;
        double offsetZ = row * spacing;
        
        // Team 2 faces opposite direction
        if (team == 2) {
            offsetZ = -offsetZ;
        }
        
        return new Vector3d(
            basePos.getX() + offsetX,
            basePos.getY(),
            basePos.getZ() + offsetZ
        );
    }

    /**
     * Apply damage to a puppet
     */
    public boolean damagePuppet(UUID puppetId, int damage) {
        PuppetNPCEntity puppet = activePuppets.get(puppetId);
        if (puppet == null || puppet.isDead()) {
            return false;
        }
        
        puppet.takeDamage(damage);
        
        if (puppet.isDead()) {
            handlePuppetDeath(puppetId);
        }
        
        return true;
    }

    /**
     * Heal a puppet
     */
    public boolean healPuppet(UUID puppetId, int amount) {
        PuppetNPCEntity puppet = activePuppets.get(puppetId);
        if (puppet == null || puppet.isDead()) {
            return false;
        }
        
        puppet.heal(amount);
        return true;
    }

    /**
     * Handle puppet death
     */
    private void handlePuppetDeath(UUID puppetId) {
        PuppetNPCEntity puppet = activePuppets.get(puppetId);
        if (puppet == null) return;
        
        System.out.println("[PuppetWars] Puppet died: " + puppet.getDefinition().getName());
        
        // Trigger death ability if applicable
        if (puppet.getDefinition().hasAbility()) {
            // Death explosion, etc. will be handled by combat system
        }
        
        // Remove from team list
        for (List<UUID> teamList : teamPuppets.values()) {
            teamList.remove(puppetId);
        }
    }

    /**
     * Get puppet by ID
     */
    public PuppetNPCEntity getPuppet(UUID puppetId) {
        return activePuppets.get(puppetId);
    }

    /**
     * Get all puppets for a team
     */
    public List<PuppetNPCEntity> getTeamPuppets(int team) {
        List<UUID> teamList = teamPuppets.get(team);
        if (teamList == null) return new ArrayList<>();
        
        List<PuppetNPCEntity> puppets = new ArrayList<>();
        for (UUID id : teamList) {
            PuppetNPCEntity puppet = activePuppets.get(id);
            if (puppet != null && !puppet.isDead()) {
                puppets.add(puppet);
            }
        }
        return puppets;
    }

    /**
     * Get alive puppet count for a team
     */
    public int getAlivePuppetCount(int team) {
        return (int) getTeamPuppets(team).stream()
            .filter(p -> !p.isDead())
            .count();
    }

    /**
     * Find nearest enemy puppet
     */
    public PuppetNPCEntity findNearestEnemy(int myTeam, Vector3d position) {
        int enemyTeam = myTeam == 1 ? 2 : 1;
        List<PuppetNPCEntity> enemies = getTeamPuppets(enemyTeam);
        
        PuppetNPCEntity nearest = null;
        double minDist = Double.MAX_VALUE;
        
        for (PuppetNPCEntity enemy : enemies) {
            if (enemy.isDead()) continue;
            
            double dist = position.distanceTo(enemy.getPosition());
            if (dist < minDist) {
                minDist = dist;
                nearest = enemy;
            }
        }
        
        return nearest;
    }

    /**
     * Find weakest enemy puppet
     */
    public PuppetNPCEntity findWeakestEnemy(int myTeam) {
        int enemyTeam = myTeam == 1 ? 2 : 1;
        List<PuppetNPCEntity> enemies = getTeamPuppets(enemyTeam);
        
        PuppetNPCEntity weakest = null;
        int minHp = Integer.MAX_VALUE;
        
        for (PuppetNPCEntity enemy : enemies) {
            if (enemy.isDead()) continue;
            
            if (enemy.getCurrentHp() < minHp) {
                minHp = enemy.getCurrentHp();
                weakest = enemy;
            }
        }
        
        return weakest;
    }

    /**
     * Find ally with lowest HP
     */
    public PuppetNPCEntity findLowestHpAlly(int myTeam, UUID excludeId) {
        List<PuppetNPCEntity> allies = getTeamPuppets(myTeam);
        
        PuppetNPCEntity lowest = null;
        int minHp = Integer.MAX_VALUE;
        
        for (PuppetNPCEntity ally : allies) {
            if (ally.isDead() || ally.getId().equals(excludeId)) continue;
            
            if (ally.getCurrentHp() < ally.getMaxHp() && ally.getCurrentHp() < minHp) {
                minHp = ally.getCurrentHp();
                lowest = ally;
            }
        }
        
        return lowest;
    }

    /**
     * Cleanup all puppets (end of battle)
     */
    public void cleanup() {
        System.out.println("[PuppetWars] Cleaning up " + activePuppets.size() + " puppets");
        
        for (PuppetNPCEntity puppet : activePuppets.values()) {
            puppet.despawn();
        }
        
        activePuppets.clear();
        teamPuppets.get(1).clear();
        teamPuppets.get(2).clear();
    }

    /**
     * Get all active puppets
     */
    public Collection<PuppetNPCEntity> getAllPuppets() {
        return activePuppets.values();
    }

    /**
     * Check if team has any alive puppets
     */
    public boolean hasAlivePuppets(int team) {
        return getAlivePuppetCount(team) > 0;
    }
}
