package com.hm.achievement.runnable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

public class AchievePlayTimeRunnable implements Runnable {

	private AdvancedAchievements plugin;
	// List of achievements extracted from configuration.
	private int[] achievementPlayTimes;
	// Set corresponding to whether a player has obtained a specific
	// play time achievement.
	// Used as pseudo-caching system to reduce load on database.
	private HashSet<?>[] playerAchievements;

	public AchievePlayTimeRunnable(AdvancedAchievements plugin) {

		this.plugin = plugin;

		extractAchievementsFromConfig(plugin);

		playerAchievements = new HashSet<?>[achievementPlayTimes.length];
		for (int i = 0; i < playerAchievements.length; ++i)
			playerAchievements[i] = new HashSet<Player>();

	}

	/**
	 * Load list of achievements from configuration.
	 */
	public void extractAchievementsFromConfig(AdvancedAchievements plugin) {

		achievementPlayTimes = new int[plugin.getConfig().getConfigurationSection("PlayedTime").getKeys(false).size()];
		int i = 0;
		for (String playedTime : plugin.getConfig().getConfigurationSection("PlayedTime").getKeys(false)) {
			achievementPlayTimes[i] = Integer.valueOf(playedTime);
		}
	}

	public void run() {

		registerTimes();
	}

	/**
	 * Update play times and store them into server's memory until player
	 * disconnects.
	 */
	@SuppressWarnings("unchecked")
	public void registerTimes() {

		for (Player player : plugin.getConnectionListener().getJoinTime().keySet()) {

			for (int i = 0; i < achievementPlayTimes.length; i++) {
				if (System.currentTimeMillis() - plugin.getConnectionListener().getJoinTime().get(player)
						+ plugin.getConnectionListener().getPlayTime().get(player) > achievementPlayTimes[i] * 3600000
						&& !playerAchievements[i].contains(player)) {
					if (!plugin.getDb().hasPlayerAchievement(player,
							plugin.getConfig().getString("PlayedTime." + achievementPlayTimes[i] + ".Name"))) {

						plugin.getAchievementDisplay().displayAchievement(player,
								"PlayedTime." + achievementPlayTimes[i]);
						SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
						plugin.getDb().registerAchievement(player,
								plugin.getConfig().getString("PlayedTime." + achievementPlayTimes[i] + ".Name"),
								plugin.getConfig().getString("PlayedTime." + achievementPlayTimes[i] + ".Message"),
								format.format(new Date()));
						plugin.getReward().checkConfig(player, "PlayedTime." + achievementPlayTimes[i]);

					}
					((HashSet<Player>) playerAchievements[i]).add(player);
				}
			}
		}
	}

	public HashSet<?>[] getPlayerAchievements() {

		return playerAchievements;
	}

}