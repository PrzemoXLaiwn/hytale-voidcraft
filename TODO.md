# PUPPET WARS - 3D BATTLE SYSTEM IMPLEMENTATION PLAN

## Current Status Analysis

### ✅ What's Working:
- Match phase system (COUNTDOWN, GATHER, BUILD, PROGRAM, BATTLE, FINISHED)
- Resource tracking (SoulStone, BattleRust, LifeSpark, MindEssence)
- Puppet definition system (body types, weapons, armor, abilities)
- Behavior programming (conditions + actions + priorities)
- Text-based battle simulation
- Best-of-3 rounds
- Core HP system

### ❌ Critical Issues:
1. **Resources auto-collect** - should be manual block collection
2. **Battle is text-only** - needs real 3D NPC puppets fighting
3. **No NPC spawning** - puppet definitions never become real entities
4. **No visual feedback** - players can't see their puppets

---

## PHASE 1: 3D BATTLE SYSTEM (PRIORITY)

### Step 1: NPC Puppet Spawning System
**Files to create:**
- `src/main/java/pl/jailbreak/puppetwar/npc/PuppetNPCManager.java` - Manages NPC lifecycle
- `src/main/java/pl/jailbreak/puppetwar/npc/PuppetNPCEntity.java` - Wrapper for puppet NPC
- `src/main/java/pl/jailbreak/puppetwar/npc/PuppetSpawner.java` - Spawns puppets in arena

**Implementation:**
- Convert PuppetDefinition → Hytale NPC entity
- Spawn at team positions in arena
- Track NPC references for each puppet
- Handle NPC death/cleanup

### Step 2: Puppet AI System (Hytale NPC Integration)
**Files to create:**
- `src/main/java/pl/jailbreak/puppetwar/npc/ai/PuppetRole.java` - Base Role for puppets
- `src/main/java/pl/jailbreak/puppetwar/npc/ai/PuppetBlackboard.java` - Puppet AI memory
- `src/main/java/pl/jailbreak/puppetwar/npc/ai/instructions/` - Folder for Instructions
  - `AttackNearestInstruction.java`
  - `AttackWeakestInstruction.java`
  - `AttackCoreInstruction.java`
  - `DefendCoreInstruction.java`
  - `HealAllyInstruction.java`
  - `RetreatInstruction.java`
  - `UseAbilityInstruction.java`
- `src/main/java/pl/jailbreak/puppetwar/npc/ai/sensors/` - Folder for Sensors
  - `EnemyInRangeSensor.java`
  - `HpBelowSensor.java`
  - `AllyHpLowSensor.java`
  - `CoreAttackedSensor.java`
  - `AbilityReadySensor.java`

**Implementation:**
- Create Role for each puppet based on PuppetDefinition
- Convert BehaviorSlots → NPC Instructions (Sensor + Action + Motion)
- Use Blackboard for puppet state (HP, cooldowns, targets)
- Implement combat logic in Instructions

### Step 3: Combat System
**Files to modify:**
- `src/main/java/pl/jailbreak/puppetwar/match/MatchManager.java` - Replace text simulation with NPC combat
- `src/main/java/pl/jailbreak/puppetwar/npc/PuppetCombatSystem.java` - NEW: Handle damage, abilities, death

**Implementation:**
- Real damage between NPC entities
- Ability activation (visual effects)
- Death explosions, healing, buffs
- Core damage tracking
- Victory conditions based on NPC states

### Step 4: Visual Feedback
**Files to create:**
- `src/main/java/pl/jailbreak/puppetwar/npc/PuppetHealthBar.java` - Floating health bars
- `src/main/java/pl/jailbreak/puppetwar/npc/PuppetEffects.java` - Visual effects (abilities, damage)

**Implementation:**
- Health bars above puppets
- Damage numbers
- Ability visual effects
- Death animations

---

## PHASE 2: UI SYSTEM

### Step 1: Create UI Files
**Files to create:**
- `src/main/resources/Common/UI/Custom/PuppetBuilder.ui` - Puppet creation interface
- `src/main/resources/Common/UI/Custom/PuppetProgram.ui` - Behavior programming interface

**Implementation:**
- Body type selection buttons
- Weapon selection grid
- Armor/Ability cycling
- Resource cost display
- Army list
- Behavior slot configuration
- Condition/Action dropdowns
- Priority sliders

---

## PHASE 3: RESOURCE COLLECTION (LATER)

### Step 1: Physical Resource Blocks
**Files to create:**
- `src/main/java/pl/jailbreak/puppetwar/resources/ResourceBlockManager.java`
- `src/main/java/pl/jailbreak/puppetwar/resources/ResourceBlockType.java`

**Implementation:**
- Spawn resource blocks in player bases
- Detect block breaking
- Add resources to player inventory
- Visual indicators for resource types

---

## IMPLEMENTATION ORDER

### Week 1: Core NPC System
- [x] Analyze current code
- [x] Create PuppetNPCManager
- [x] Create PuppetNPCEntity
- [ ] Create PuppetCombatSystem
- [ ] Integrate with MatchManager
- [ ] Test basic NPC spawning
=======

### Week 2: AI & Combat
- [ ] Create PuppetRole system
- [ ] Implement basic Instructions (Attack, Defend)
- [ ] Create Sensors for conditions
- [ ] Implement combat damage system
- [ ] Test puppet vs puppet combat

### Week 3: Advanced Features
- [ ] Implement all abilities
- [ ] Add visual effects
- [ ] Health bars
- [ ] Battle commands integration
- [ ] Core damage system

### Week 4: UI & Polish
- [ ] Create PuppetBuilder.ui
- [ ] Create PuppetProgram.ui
- [ ] Test full match flow
- [ ] Balance tweaking
- [ ] Bug fixes

---

## TECHNICAL NOTES

### Hytale NPC API Usage:
```java
// Spawn NPC
NPCPlugin npcPlugin = server.getNPCPlugin();
Ref<EntityStore> npcRef = npcPlugin.spawnNPC(world, position, npcType);

// Create Role
Role puppetRole = new Role.Builder()
    .addInstruction(instruction)
    .setBlackboard(blackboard)
    .build();

// Apply Role to NPC
npcPlugin.setRole(npcRef, puppetRole);
```

### Puppet → NPC Mapping:
- PuppetBodyType → NPC model type
- PuppetWeapon → NPC equipment/attack type
- BehaviorSlot → Instruction (Sensor + Action + Motion)
- PuppetDefinition stats → NPC attributes (HP, damage, speed)

### Battle Flow:
1. BATTLE phase starts
2. Spawn all puppets for both teams
3. Apply Roles based on BehaviorSlots
4. NPCs fight autonomously using AI
5. Track deaths, Core HP
6. Declare winner when conditions met
7. Cleanup NPCs
8. Return to lobby

---

## NEXT STEPS

Starting with **PuppetNPCManager** and **PuppetSpawner** to get basic NPC spawning working.
