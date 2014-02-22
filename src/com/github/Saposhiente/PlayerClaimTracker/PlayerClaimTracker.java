package com.github.Saposhiente.PlayerClaimTracker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Keeps track of players' claims on land or other resources, and reclaims the
 * resources when those claims expire. Claims are created when the player logs
 * out, and expire if they do not log back in within CLAIM_LIFETIME
 * milliseconds. 
 *
 * @author Saposhiente
 * @version 1.2
 */
public class PlayerClaimTracker implements Serializable {

    private static final long serialVersionUID = -2454216575891332443L; //Random, constant number to ensure that you don't encounter problems in serialization
    static final long CLAIM_LIFETIME = 10000000; //Amount of time, in milliseconds, to wait before expiring a claim

    static class DoublyLinkedListNode implements Serializable {

        private static final long serialVersionUID = 2744319047691572257L;
        public final String playerName; //should never be null
        public UUID prev; //null iff it's the first node or it's not part of the list
        public UUID next; //null iff it's the last node or it's not part of the list

        public DoublyLinkedListNode(String playerName, UUID prev, UUID next) {
            this.playerName = playerName;
            this.prev = prev;
            this.next = next;
        }
    }
    static final int expectedNumberOfPlayers = 100; //set this to a high estimate of the total number of players with inactive claims at one time to improve performance
    final Map<UUID, DoublyLinkedListNode> claimList = new HashMap<>(expectedNumberOfPlayers);
    UUID oldestClaim = null;
    UUID newestClaim = null;

    public static abstract class ResourceReclaimer {

        public abstract void reclaimResourcesOf(OfflinePlayer player);

        public Logger getLogger() {
            return Bukkit.getLogger();
        }

        public OfflinePlayer getOfflinePlayer(String name) {
            return Bukkit.getOfflinePlayer(name);
        }
    }
    private transient ResourceReclaimer reclaimer = null; //effectively final, mutable only for when this is deserialized

    protected ResourceReclaimer getReclaimer() {
        return reclaimer;
    }

    /**
     * Makes a new tracker that, when it detects an expired claim, will call the
     * reclaimResourcesOf(OfflinePlayer) function of the provided
     * ResourceReclaimer
     *
     * @param reclaimer The resource reclaimer to use
     */
    public PlayerClaimTracker(ResourceReclaimer reclaimer) {
        setResourceReclaimer(reclaimer);
    }

    /**
     * Call this function only immediately after deserializing this
     *
     * @param reclaimer The resource reclaimer to use (same as in the
     * constructor)
     */
    public final void setResourceReclaimer(ResourceReclaimer reclaimer) {
        if (this.reclaimer == null) {
            this.reclaimer = reclaimer;
        } else {
            throw new IllegalStateException("Can only set the resource reclaimer once!");
        }
    }

    /**
     * Call this function whenever you want to check if any players' claims have
     * expired, perhaps on a daily or weekly basis, depending on how quickly
     * their claims expire This calls the reclaimResourcesOf(OfflinePlayer)
     * function of the ResourceReclaimer when a player's claims expire.
     */
    public void checkForExpiredClaims() {
        if (oldestClaim != null) {
            checkForExpiredClaims(claimList.get(oldestClaim));
        }
    }

    private void checkForExpiredClaims(DoublyLinkedListNode claim) {
        assert (claim.prev == null);
        OfflinePlayer claimant = reclaimer.getOfflinePlayer(claim.playerName);
        boolean next = false;
        if (claimant == null) {
            reclaimer.getLogger().log(Level.WARNING, "[PlayerClaimTracker] Offline player {0} not found. Did you delete their data while they were holding a claim? Expiring their claim...", claim.playerName);
            next = true;
        } else if (claimant.getLastPlayed() + CLAIM_LIFETIME < System.currentTimeMillis()) {
            reclaimer.reclaimResourcesOf(claimant);
            next = true;
        } //If the oldest claim is not expired, none of the claims are expired
        if (next) {
            oldestClaim = claim.next;
            claim.next = null;
            if (oldestClaim != null) {
                DoublyLinkedListNode nextClaim = claimList.get(oldestClaim);
                nextClaim.prev = null;
                checkForExpiredClaims(nextClaim);
            } else {
                newestClaim = null; //we've reached the end of the claim list
            }
        }
    }

    /**
     * Call this function occasionally to reclaim memory used by players that
     * have not logged in for a long time, perhaps on a monthly basis. When a
     * player's claims expire, they are set to expired and the resources are
     * reclaimed, but to save time the entries are not removed from the database
     * (so that if the player rejoins they can be quickly added back to the
     * database). This function removes all expired claims from the database. It
     * is a good idea to call this function before calling
     * checkForExpiredClaims(), so that claims that only recently expired are
     * not deleted.
     */
    public void purgeOutdatedEntries() {
        Iterator<Entry<UUID, DoublyLinkedListNode>> iterator = claimList.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<UUID, DoublyLinkedListNode> e = iterator.next();
            if (e.getValue().prev == null && e.getValue().next == null && e.getKey() != oldestClaim) { //Has no links and is not the only element in the list
                assert (e.getKey() != newestClaim);
                iterator.remove();
            }
        }
    }

    /**
     * Call this function whenever a player joins the server, to renew any
     * claims they have.
     *
     * @param player The player who joined the game Removes the player's claim
     * from the list of inactive claims (without marking the claim as expired)
     */
    public void onPlayerJoin(Player player) { //
        UUID id = player.getUniqueId();
        DoublyLinkedListNode claim = claimList.get(id);
        if (claim != null) {
            if (claim.prev == null) {
                assert (id == oldestClaim); //If a non-oldest claim doesn't have a previous claim, you have a problem
                oldestClaim = claim.next;
            } else {
                assert (id != oldestClaim); //If the oldest claim has a previous claim, you have a problem
                claimList.get(claim.prev).next = claim.next; //Have the list skip over the entry
            }
            if (claim.next == null) {
                assert (id == newestClaim); //If a non-newest claim doesn't have a next claim, you have a problem
                newestClaim = claim.prev;
            } else {
                assert (id != newestClaim); //If the newest claim has a next claim, you have a problem
                claimList.get(claim.next).prev = claim.prev;
            }
            claim.prev = null; //No point in actually removing the claim from the hash, which takes O(log(n)) time, when
            claim.next = null; // we're just going to put it right back later, especially when we can just remove the links in O(1) time
        }
    }

    /**
     * Call this function whenever a player who has claims disconnects This
     * function adds a claim owned by the player to the database to be tracked.
     *
     * @param player The player who disconnected while holding one or more
     * claims on resources
     */
    public void onPlayerQuit(Player player) {
        UUID id = player.getUniqueId();
        DoublyLinkedListNode claim = claimList.get(id);
        if (claim == null) {//First time the player has quit
            claim = new DoublyLinkedListNode(player.getName(), newestClaim, null);
            claimList.put(id, claim);
        } else {
            claim.prev = newestClaim;
            assert (claim.next == null); //Should have been a node with no connections
        }
        if (newestClaim == null) { //first claim
            oldestClaim = id;
        } else {
            claimList.get(newestClaim).next = id;
        }
        newestClaim = id;
    }
}