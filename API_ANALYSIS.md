# Hytale API Analysis: Right-Click vs F-Key Distinction

## Summary
**YES - You CAN distinguish between right-click and F-key interactions!**

The `UseBlockEvent.Pre` class provides access to `InteractionType`, which contains distinct values for different interaction methods.

## Key Classes

### 1. UseBlockEvent (Base Class)
**Package**: `com.hypixel.hytale.server.core.event.events.ecs`

**Key Methods**:
- `getInteractionType()` → **InteractionType enum** - Identifies HOW the player interacted
- `getContext()` → InteractionContext - Additional context about the interaction
- `getTargetBlock()` → Vector3i - Block coordinates
- `getBlockType()` → BlockType - What type of block was interacted with

### 2. UseBlockEvent.Pre (Cancellable)
**Extends**: UseBlockEvent + ICancellableEcsEvent

**Key Methods**:
- `isCancelled()` → boolean
- `setCancelled(boolean)` → void

**This is the event to listen to** - it fires BEFORE the action executes, allowing cancellation.

### 3. InteractionType Enum - THE SOLUTION
**Package**: `com.hypixel.hytale.protocol`

**Available Values**:
```
Primary          - Left click (attack/mine with primary action)
Secondary        - Right click (use/place with secondary action)
Ability1         - Ability key 1
Ability2         - Ability key 2
Ability3         - Ability key 3
Use              - Generic use action (E key / Use key)
Pick             - Pick up block (possibly F key or similar)
Pickup           - Pick up item
CollisionEnter   - Entity collision
CollisionLeave   - Entity collision end
Collision        - Active collision
EntityStatEffect - Stat effect interaction
SwapTo           - Item swap to
SwapFrom         - Item swap from
Death            - Death event
Wielding         - Item wielding
ProjectileSpawn  - Projectile spawned
ProjectileHit    - Projectile hit
ProjectileMiss   - Projectile missed
ProjectileBounce - Projectile bounce
Held             - Item held (main hand)
HeldOffhand      - Item held (off-hand)
Equipped         - Item equipped
Dodge            - Dodge action
GameModeSwap     - Game mode swap
```

## Solution Implementation

```java
@EventListener(UseBlockEvent.Pre.class)
public void onUseBlock(UseBlockEvent.Pre event) {
    InteractionType type = event.getInteractionType();
    
    // Distinguish between interactions
    if (type == InteractionType.Secondary) {
        // Right-click interaction
        System.out.println("Player right-clicked block");
    } else if (type == InteractionType.Primary) {
        // Left-click/attack interaction
        System.out.println("Player left-clicked block");
    } else if (type == InteractionType.Use) {
        // E key / Use key (might be the F-key equivalent)
        System.out.println("Player pressed Use key");
    } else if (type == InteractionType.Pick) {
        // Might be F-key for picking blocks
        System.out.println("Player pressed Pick key");
    }
}
```

## InteractionContext Details

The event also provides `InteractionContext context = event.getContext()` with:
- `getHeldItem()` → ItemStack of the item player was holding
- `getHeldItemContainer()` → Container reference (hotbar or tools)
- `getHeldItemSlot()` → Slot index
- `getEntity()` → Reference to the player entity performing the action

## Event Timing

- **UseBlockEvent.Pre** fires BEFORE the action happens (allows cancellation)
- **UseBlockEvent.Post** fires AFTER the action happens (read-only)

## Likely Mappings

Based on InteractionType values:
- **Secondary** = Right-click on block
- **Primary** = Left-click on block (mining)
- **Use** = E key (default use key, possibly what F maps to in Hytale)
- **Pick** = F key (picking up blocks) or similar pickup action
- **Pickup** = Item pickup (see InteractivelyPickupItemEvent below)

## Related Event: InteractivelyPickupItemEvent

For item pickups (not block interactions):
```java
public class InteractivelyPickupItemEvent extends CancellableEcsEvent {
    private ItemStack itemStack;
    public ItemStack getItemStack();
    public void setItemStack(ItemStack);
}
```

This is separate from block interactions and fires when picking up ground items.

## Conclusion

You have precise control via `InteractionType` enum values. Test with the values listed above to map them to your specific keybinds and understand how Hytale's F-key (or equivalent) translates to InteractionType values in your environment.
