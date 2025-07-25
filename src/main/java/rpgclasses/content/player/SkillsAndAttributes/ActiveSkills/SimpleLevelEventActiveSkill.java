package rpgclasses.content.player.SkillsAndAttributes.ActiveSkills;

import necesse.engine.network.packet.PacketLevelEvent;
import necesse.engine.registries.LevelEventRegistry;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.mobs.PlayerMob;
import rpgclasses.data.PlayerData;

abstract public class SimpleLevelEventActiveSkill extends ActiveSkill {
    public SimpleLevelEventActiveSkill(String familyID, String stringID, String color, int levelMax, int requiredClassLevel) {
        super(familyID, stringID, color, levelMax, requiredClassLevel);
    }

    public SimpleLevelEventActiveSkill(String stringID, String color, int levelMax, int requiredClassLevel) {
        this(stringID, stringID, color, levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUSe);
        addLevelEvent(player, playerData, activeSkillLevel, seed, isInUSe);
    }

    public void addLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUSe) {
        LevelEvent event = getLevelEvent(player, playerData, activeSkillLevel, seed, isInUSe);
        player.getLevel().entityManager.addLevelEventHidden(event);
        player.getServer().network.sendToClientsWithEntity(new PacketLevelEvent(event), event);
    }

    @Override
    public void registerSkillLevelEvents() {
        LevelEventRegistry.registerEvent(getLevelEventID(), getLevelEventClass());
    }

    abstract public LevelEvent getLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse);

    abstract public Class<? extends LevelEvent> getLevelEventClass();

    public String getLevelEventID() {
        return stringID + "levelevent";
    }
}
