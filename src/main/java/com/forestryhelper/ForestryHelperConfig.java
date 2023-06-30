package com.forestryhelper;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("forestry helper")
public interface ForestryHelperConfig extends Config {

	@ConfigSection(name = "General", description = "General Plugin Settings", position = 0)
	String generalSection = "generalSection";
	@ConfigSection(name = "Roots", description = "Roots Overlay Settings", position = 1)
	String rootsSection = "rootsSection";
	@ConfigSection(name = "Saplings", description = "Saplings Settings", position = 2)
	String saplingsSection = "saplingsSection";
	@ConfigSection(name = "Flowering Trees", description = "Flowering Trees Settings", position = 3)
	String floweringSection = "floweringSection";
	@ConfigSection(name = "Notifications", description = "Notifications", position = 4)
	String notifySection = "notifySection";

	//General Settings
	@ConfigItem(
		keyName = "showAnimaGathered",
		name = "Show Bark Gathered",
		description = "",
		position = 0,
		section = generalSection
	)
	default boolean showAnimaGathered() { return true; }

	//Roots Settings
	@ConfigItem(
		keyName = "highlightRoots",
		name = "Highlight Roots",
		description = "Enabling this will highlight roots.",
		position = 0,
		section = rootsSection
	)
	default boolean highlightRoots() { return true; }
	@Alpha
	@ConfigItem(
		keyName = "regularRootColor",
		name = "Regular Roots Color",
		description = "The highlight color of all the tiles regular roots reside on.",
		position = 1,
		section = rootsSection
	)
	default Color regularRootColor() { return Color.yellow; }


	@Alpha
	@ConfigItem(
		keyName = "glowingRootColor",
		name = "Glowing Roots Color",
		description = "The highlight color of the tile the glowing roots reside on.",
		position = 2,
		section = rootsSection
	)
	default Color glowingRootColor() { return Color.GREEN; }


	//Sapling Settings
	@ConfigItem(
		keyName = "highlightSapling",
		name = "Highlight Saplings and Piles",
		description = "Enabling this will highlight saplings and mulch ingredient piles.",
		position = 0,
		section = saplingsSection
	)
	default boolean highlightSapling() { return true; }

	@Alpha
	@ConfigItem(
		keyName = "saplingColor",
		name = "Default Sapling Color",
		description = "The highlight color of the sapling tile when you don't have all the mulch ingredients.",
		position = 1,
		section = saplingsSection
	)
	default Color saplingColor() { return Color.WHITE; }

	@Alpha
	@ConfigItem(
		keyName = "saplingReadyColor",
		name = "Sapling Ready Color",
		description = "The highlight color of the sapling tile when you do have all the mulch ingredients.",
		position = 2,
		section = saplingsSection
	)
	default Color saplingReadyColor() { return Color.GREEN; }

	@Alpha
	@ConfigItem(
		keyName = "correctIngredientColor",
		name = "Correct Ingredient Pile",
		description = "The highlight and text color of the all the correct ingredient piles.",
		position = 3,
		section = saplingsSection
	)
	default Color correctIngredientColor() { return Color.CYAN; }

	@Alpha
	@ConfigItem(
		keyName = "incorrectIngredientColor",
		name = "Incorrect Ingredient Pile",
		description = "The highlight color of the incorrect ingredient piles.",
		position = 3,
		section = saplingsSection
	)
	default Color incorrectIngredientColor() { return Color.RED; }


	//Flowering Trees Settings
	@ConfigItem(
		keyName = "highlightBushes",
		name = "Highlight Flowering Bushes",
		description = "Enabling this will highlight bushes from the Flowering Trees event.",
		position = 0,
		section = floweringSection
	)
	default boolean highlightBushes() { return true; }

	@Alpha
	@ConfigItem(
		keyName = "matchingBushColor",
		name = "Matching Color",
		description = "The color of the tiles of discovered matching bushes",
		position = 1,
		section = floweringSection
	)
	default Color matchingBushColor() { return Color.GREEN; }

	@Alpha
	@ConfigItem(
		keyName = "nonMatchingBushColor",
		name = "Non-Matching Color",
		description = "The color of the tiles of all the other bushes that arent matches.",
		position = 2,
		section = floweringSection
	)
	default Color nonMatchingBushColor() { return Color.YELLOW; }


	//Notifications
	@ConfigItem(
		keyName = "notifyRoots",
		name = "Notify On Roots",
		description = "Enabling this notify you when Rising Roots event starts.",
		position = 0,
		section = notifySection
	)
	default boolean notifyRoots() { return true; }

	@ConfigItem(
		keyName = "notifySapling",
		name = "Notify On Saplings",
		description = "Enabling this notify you when Struggling Sapling event starts.",
		position = 1,
		section = notifySection
	)
	default boolean notifySapling() { return true; }

	@ConfigItem(
		keyName = "notifyBushes",
		name = "Notify On Flowering Tree",
		description = "Enabling this notify you when Flowering Tree event starts.",
		position = 2,
		section = notifySection
	)
	default boolean notifyBushes() { return true; }

	@ConfigItem(
		keyName = "notifyLeprechaun",
		name = "Notify On Leprechaun",
		description = "Enabling this notify you when a Leprechaun appears.",
		position = 3,
		section = notifySection
	)
	default boolean notifyLeprechaun() { return true; }
}
