package pl.jailbreak.achievements;

public class Achievement {
    private final String id;
    private final String name;
    private final String description;
    private final AchievementType type;
    private final long requirement;
    private final long reward;

    public Achievement(String id, String name, String description, AchievementType type, long requirement, long reward) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.requirement = requirement;
        this.reward = reward;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public AchievementType getType() { return type; }
    public long getRequirement() { return requirement; }
    public long getReward() { return reward; }
}
