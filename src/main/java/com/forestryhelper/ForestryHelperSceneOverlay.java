package com.forestryhelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class ForestryHelperSceneOverlay extends Overlay {
	@Inject
	private ForestryHelperConfig config;

	private final ForestryHelperPlugin plugin;

	@Inject
	public ForestryHelperSceneOverlay(ForestryHelperPlugin plugin) {
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		if (config.highlightRoots()) {
			highlightRoots(graphics);
		}
		if (config.highlightSapling()) {
			highlightSaplingEvent(graphics);
		}
		if (config.highlightBushes()) {
			highlightBushes(graphics);
		}
		return null;
	}

	private void highlightRoots(Graphics2D graphics) {
		for (GameObject root : plugin.getActiveRoots()) {
			int id = root.getId();
			final Polygon poly = root.getCanvasTilePoly();
			if (poly != null) {
				OverlayUtil.renderPolygon(graphics, poly, id == Constants.ROOT_REGULAR_ID ? config.regularRootColor() : config.glowingRootColor());
			}
		}
	}

	private void highlightSaplingEvent(Graphics2D graphics) {
		for (GameObject sap : plugin.getSaplingPiles()) {
			int id = sap.getId();
			//Wild mushroom has 3 IDs
			if (id == Constants.WILD_MUSHROOM_ID97 || id == Constants.WILD_MUSHROOM_ID98) {
				id = Constants.WILD_MUSHROOM_ID96;
			}
			final Polygon poly = sap.getCanvasTilePoly();
			Color saplingColor = plugin.isHoldingThirdIngredient() ? config.saplingReadyColor() : config.saplingColor();
			Color ingredientColor = Color.cyan;
			Color nonIngredientColor = Color.YELLOW;
			if(plugin.getSaplingOrder()[0] != 0 && plugin.getSaplingOrder()[1] != 0 && plugin.getSaplingOrder()[2] != 0) {
				nonIngredientColor = Color.red;
			}
			if (poly != null) {
				if (isSapling(id)) {
					OverlayUtil.renderPolygon(graphics, poly, saplingColor);
				} else {    //Highlight Piles
					Color finalPileColor;
					if (id == plugin.getSaplingOrder()[0] ||
						id == plugin.getSaplingOrder()[1] ||
						id == plugin.getSaplingOrder()[2]) {
						finalPileColor = ingredientColor;
						drawOrderNumbers(id, graphics, sap.getCanvasLocation(), finalPileColor);
					} else {
						finalPileColor = nonIngredientColor;
					}
					OverlayUtil.renderPolygon(graphics, poly, finalPileColor);
				}
			}
		}
	}

	private boolean isSapling(int id) {

		return id == Constants.SAPLING_ID84 ||
			id == Constants.SAPLING_ID85 ||
			id == Constants.SAPLING_ID87 ||
			id == Constants.SAPLING_ID88 ||
			id == Constants.SAPLING_ID90 ||
			id == Constants.SAPLING_ID91;
	}
	private void drawOrderNumbers(int sapId, Graphics2D graphics, Point point, Color color) {
		StringBuilder sb = new StringBuilder();
		if (sapId == plugin.getSaplingOrder()[0]){
			sb.append("1");
		}
		if (sapId == plugin.getSaplingOrder()[1]){
			if(sb.length() > 0)
				sb.append("-");
			sb.append("2");
		}
		if (sapId == plugin.getSaplingOrder()[2]){
			if(sb.length() > 0)
				sb.append("-");
			sb.append("3");
		}
		OverlayUtil.renderTextLocation(graphics, point, sb.toString(), color);
	}

	private void highlightBushes(Graphics2D graphics) {
		for (NPC npc : plugin.getFloweringBushes()) {
			final Polygon poly = npc.getCanvasTilePoly();
			Color color = config.nonMatchingBushColor();
			if(npc.getId() == Constants.STRANGE_BUSH_ID) {
				color = Color.red;
				if(plugin.matchedBushes.contains(npc)) {
					plugin.matchedBushes.remove(npc);
				}
			}
			if(plugin.matchedBushes.contains(npc))
				color = config.matchingBushColor();
			if (poly != null) {
				OverlayUtil.renderPolygon(graphics, poly, color);
			}
		}
	}
}
