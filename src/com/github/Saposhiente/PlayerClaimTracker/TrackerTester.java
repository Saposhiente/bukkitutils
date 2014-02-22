/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.Saposhiente.PlayerClaimTracker;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

/**
 *
 * @author Saposhiente
 */
public class TrackerTester {
    public static final Map<String, Long> playtimes = new HashMap<>();
    static {
        playtimes.put("Expired", 0L);
        playtimes.put("Future", Long.MAX_VALUE - PlayerClaimTracker.CLAIM_LIFETIME - 10000);
        playtimes.put("Now", System.currentTimeMillis());
        playtimes.put("Past", System.currentTimeMillis() - PlayerClaimTracker.CLAIM_LIFETIME - 1000);
    }
    public static void main(String[] args) {
        PlayerClaimTracker tracker = new PlayerClaimTracker(new PlayerClaimTracker.ResourceReclaimer() {
            @Override
            public void reclaimResourcesOf(OfflinePlayer player) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Logger getLogger() {
                return Logger.getLogger("TrackerTester");
            }

            @Override
            public OfflinePlayer getOfflinePlayer(String name) {
                return players.get(name);
            }
            
        });
        testPlayer expired = new testPlayer("Expired");
        testPlayer future = new testPlayer("Future");
        testPlayer now = new testPlayer("Now");
        testPlayer past = new testPlayer("Past");
        tracker.onPlayerJoin(expired);
        tracker.onPlayerJoin(past);
        tracker.onPlayerQuit(expired);
        tracker.onPlayerJoin(future);
        tracker.onPlayerQuit(future);
        tracker.onPlayerQuit(past);
        tracker.onPlayerJoin(now);
        tracker.onPlayerJoin(future);
        tracker.onPlayerQuit(now);
        tracker.onPlayerQuit(future);
        tracker.checkForExpiredClaims();
        System.out.println(tracker.oldestClaim);
        System.out.println(tracker.newestClaim);
        for (Entry<UUID,PlayerClaimTracker.DoublyLinkedListNode> e : tracker.claimList.entrySet()) {
            System.out.println(e);
            System.out.println("--" + e.getValue().playerName);
            System.out.println("--" + e.getValue().prev);
            System.out.println("--" + e.getValue().next);
        }
        
    }
    public static final Map<String, testOfflinePlayer> players = new HashMap<>();
    public static class testOfflinePlayer implements OfflinePlayer {
        public final String name;
        public final long lastPlayed;

        public testOfflinePlayer(String name) {
            this.name = name;
            this.lastPlayed = playtimes.get(name);
            players.put(name, this);
        }
        
        @Override
        public boolean isOnline() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isBanned() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setBanned(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isWhitelisted() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setWhitelisted(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Player getPlayer() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getFirstPlayed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getLastPlayed() {
            return lastPlayed;
        }

        @Override
        public boolean hasPlayedBefore() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Location getBedSpawnLocation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isOp() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setOp(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<String, Object> serialize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    public static class testPlayer extends testOfflinePlayer implements Player {
        public final UUID id;

        public testPlayer(String name) {
            super(name);
            id = new UUID(lastPlayed, name.codePointAt(name.length() - 1));
        }
        
        @Override
        public boolean isOnline() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isBanned() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setBanned(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isWhitelisted() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setWhitelisted(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Player getPlayer() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getFirstPlayed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getLastPlayed() {
            return lastPlayed;
        }

        @Override
        public boolean hasPlayedBefore() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Location getBedSpawnLocation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isOp() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setOp(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<String, Object> serialize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDisplayName(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getPlayerListName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setPlayerListName(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setCompassTarget(Location lctn) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Location getCompassTarget() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InetSocketAddress getAddress() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendRawMessage(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void kickPlayer(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void chat(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean performCommand(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isSneaking() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSneaking(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isSprinting() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSprinting(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void saveData() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void loadData() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSleepingIgnored(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isSleepingIgnored() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void playNote(Location lctn, byte b, byte b1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void playNote(Location lctn, Instrument i, Note note) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void playSound(Location lctn, Sound sound, float f, float f1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void playEffect(Location lctn, Effect effect, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T> void playEffect(Location lctn, Effect effect, T t) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendBlockChange(Location lctn, Material mtrl, byte b) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean sendChunkChange(Location lctn, int i, int i1, int i2, byte[] bytes) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendBlockChange(Location lctn, int i, byte b) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendMap(MapView mv) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateInventory() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void awardAchievement(Achievement a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void incrementStatistic(Statistic ststc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void incrementStatistic(Statistic ststc, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void incrementStatistic(Statistic ststc, Material mtrl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void incrementStatistic(Statistic ststc, Material mtrl, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setPlayerTime(long l, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getPlayerTime() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getPlayerTimeOffset() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isPlayerTimeRelative() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void resetPlayerTime() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setPlayerWeather(WeatherType wt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public WeatherType getPlayerWeather() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void resetPlayerWeather() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void giveExp(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void giveExpLevels(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float getExp() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setExp(float f) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getLevel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLevel(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getTotalExperience() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setTotalExperience(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float getExhaustion() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setExhaustion(float f) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float getSaturation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSaturation(float f) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getFoodLevel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFoodLevel(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setBedSpawnLocation(Location lctn) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setBedSpawnLocation(Location lctn, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean getAllowFlight() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAllowFlight(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void hidePlayer(Player player) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void showPlayer(Player player) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean canSee(Player player) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isOnGround() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isFlying() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFlying(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFlySpeed(float f) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setWalkSpeed(float f) throws IllegalArgumentException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float getFlySpeed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float getWalkSpeed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setTexturePack(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Scoreboard getScoreboard() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setScoreboard(Scoreboard scrbrd) throws IllegalArgumentException, IllegalStateException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public PlayerInventory getInventory() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Inventory getEnderChest() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean setWindowProperty(Property prprt, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InventoryView getOpenInventory() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InventoryView openInventory(Inventory invntr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InventoryView openWorkbench(Location lctn, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InventoryView openEnchanting(Location lctn, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void openInventory(InventoryView iv) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void closeInventory() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ItemStack getItemInHand() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setItemInHand(ItemStack is) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ItemStack getItemOnCursor() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setItemOnCursor(ItemStack is) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isSleeping() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getSleepTicks() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public GameMode getGameMode() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setGameMode(GameMode gm) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isBlocking() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getExpToLevel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getEyeHeight() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getEyeHeight(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Location getEyeLocation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Block> getLineOfSight(HashSet<Byte> hs, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Block getTargetBlock(HashSet<Byte> hs, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hs, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Egg throwEgg() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Snowball throwSnowball() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Arrow shootArrow() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T extends Projectile> T launchProjectile(Class<? extends T> type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getRemainingAir() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setRemainingAir(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getMaximumAir() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setMaximumAir(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getMaximumNoDamageTicks() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setMaximumNoDamageTicks(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getLastDamage() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLastDamage(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getNoDamageTicks() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setNoDamageTicks(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Player getKiller() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addPotionEffect(PotionEffect pe) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addPotionEffect(PotionEffect pe, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addPotionEffects(Collection<PotionEffect> clctn) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean hasPotionEffect(PotionEffectType pet) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removePotionEffect(PotionEffectType pet) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<PotionEffect> getActivePotionEffects() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean hasLineOfSight(Entity entity) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean getRemoveWhenFarAway() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setRemoveWhenFarAway(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public EntityEquipment getEquipment() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setCanPickupItems(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean getCanPickupItems() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setCustomName(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getCustomName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setCustomNameVisible(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCustomNameVisible() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Location getLocation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Location getLocation(Location lctn) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setVelocity(Vector vector) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Vector getVelocity() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public World getWorld() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean teleport(Location lctn) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean teleport(Location lctn, TeleportCause tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean teleport(Entity entity) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean teleport(Entity entity, TeleportCause tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<Entity> getNearbyEntities(double d, double d1, double d2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getEntityId() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getFireTicks() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getMaxFireTicks() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFireTicks(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isDead() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isValid() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Server getServer() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Entity getPassenger() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean setPassenger(Entity entity) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean eject() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float getFallDistance() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFallDistance(float f) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLastDamageCause(EntityDamageEvent ede) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public EntityDamageEvent getLastDamageCause() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public UUID getUniqueId() {
            return id;
        }

        @Override
        public int getTicksLived() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setTicksLived(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void playEffect(EntityEffect ee) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public EntityType getType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isInsideVehicle() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean leaveVehicle() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Entity getVehicle() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setMetadata(String string, MetadataValue mv) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<MetadataValue> getMetadata(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean hasMetadata(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removeMetadata(String string, Plugin plugin) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void damage(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void damage(int i, Entity entity) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getHealth() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setHealth(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getMaxHealth() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setMaxHealth(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void resetMaxHealth() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isPermissionSet(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isPermissionSet(Permission prmsn) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean hasPermission(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean hasPermission(Permission prmsn) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removeAttachment(PermissionAttachment pa) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void recalculatePermissions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<PermissionAttachmentInfo> getEffectivePermissions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isConversing() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void acceptConversationInput(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean beginConversation(Conversation c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void abandonConversation(Conversation c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void abandonConversation(Conversation c, ConversationAbandonedEvent cae) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendMessage(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendMessage(String[] strings) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendPluginMessage(Plugin plugin, String string, byte[] bytes) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<String> getListeningPluginChannels() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
