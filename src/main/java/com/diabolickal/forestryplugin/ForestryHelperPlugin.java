package com.diabolickal.forestryplugin;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.infobox.Counter;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import static net.runelite.client.util.Text.removeTags;

@Slf4j
@PluginDescriptor(
	name = "Forestry Helper"
)
public class ForestryHelperPlugin extends Plugin {
	@Inject
	public Client client;

	@Getter
	private final List<GameObject> activeRoots = new ArrayList<>();
	@Getter
	private final List<GameObject> saplingPiles = new ArrayList<>();
	@Getter
	private final List<NPC> floweringBushes = new ArrayList<>();

	public final HashSet<Actor> matchedBushes = new HashSet<>();

	@Getter
	private int[] saplingOrder = new int[3];
	@Inject
	private ForestryHelperConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ForestryHelperSceneOverlay forestryOverlay;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private ItemManager itemManager;
	@Inject
	private PanelComponent panelComponent;
	@Inject
	private ChatCommandManager chatCommandManager;

	@Inject
	private Notifier notifier;


	@Getter
	private int animaBarkCollected = 0;

	@Getter
	private long hourlyAnimaBark;

	private Counter animaCounter;
	private long animaStartTime;

	private final Pattern animaAmountPattern = Pattern.compile("\\b(\\d+)\\b");
	private boolean rootsEventStarted;
	@Getter
	private boolean saplingEventStarted;
	private boolean floweringTreeEventStarted;

	@Getter
	private boolean holdingThirdIngredient;

	private boolean receivedPollen;

	@Provides
	ForestryHelperConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(ForestryHelperConfig.class);
	}

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(forestryOverlay);
	}

	@Override
	protected void shutDown() throws Exception {
		reset();
	}

	@Subscribe
	public void onGameTick(GameTick gameTick) {
		if (!config.showAnimaGathered()) {
			animaBarkCollected = 0;
			removeInfoBox();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		String msg = removeTags(event.getMessage().toLowerCase());
		if (config.showAnimaGathered()) {
			if (msg.contains(Constants.CHAT_KEY_ANIMA_BARK)) {
				Matcher matcher = animaAmountPattern.matcher(msg);
				if (matcher.find()) {
					int number = tryParseInt(matcher.group(), 0);
					animaBarkCollected += number;
					removeInfoBox();
					addInfoBox();
				}
			}
		}

		if(config.highlightBushes()) {
			if(msg.contains(Constants.CHAT_KEY_CORRECT_BUSH)) {
				matchedBushes.add(client.getLocalPlayer().getInteracting());
			}
		}

		if (config.highlightSapling()) {
			if (msg.contains(Constants.CHAT_KEY_SAPLING_LOVE)) {
				int id = nameToId(msg);
				int slot = ingredientToOrder(msg);

				saplingOrder[slot] = id;
			}

			if (msg.contains(Constants.CHAT_KEY_SAPLING_ADDED) ||
				msg.contains(Constants.CHAT_KEY_MULCH_FIRST)) {
				holdingThirdIngredient = false;
			}

			if (msg.contains(Constants.CHAT_KEY_MULCH_THIRD)) {
				holdingThirdIngredient = true;
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event) {
		switch (event.getGameState()) {
			case HOPPING:
				clearEventFields();
				break;
			case LOGIN_SCREEN:
				reset();
				break;
		}
	}

	@Subscribe
	public void onGameObjectSpawned(final GameObjectSpawned event) {
		GameObject gameObject = event.getGameObject();
		int id = gameObject.getId();
		switch (id) {
			//Root Stuff
			case (Constants.ROOT_REGULAR_ID):
			case (Constants.ROOT_GREEN_ID):
				activeRoots.add(gameObject);
				if (config.notifyRoots() && !rootsEventStarted) {
					rootsEventStarted = true;
					notifier.notify(Constants.NOTIFY_ROOTS);
				}
				break;

			//Sapling Stuff
			case (Constants.SAPLING_ID84):
			case (Constants.SAPLING_ID85):
			case (Constants.SAPLING_ID87):
			case (Constants.SAPLING_ID88):
			case (Constants.SAPLING_ID90):
			case (Constants.SAPLING_ID91):
			case (Constants.ROTTING_LEAVES_ID):
			case (Constants.GREEN_LEAVES_ID):
			case (Constants.DROPPINGS_ID):
			case (Constants.WILD_MUSHROOM_ID96):
			case (Constants.WILD_MUSHROOM_ID97):
			case (Constants.WILD_MUSHROOM_ID98):
			case (Constants.SPLINTERED_BARK_ID):
				saplingPiles.add(gameObject);
				if (config.notifySapling() && !saplingEventStarted) {
					saplingEventStarted = true;
					notifier.notify(Constants.NOTIFY_SAPLING);
				}
				break;
		}
	}


	@Subscribe
	public void onGameObjectDespawned(final GameObjectDespawned event) {
		GameObject gameObject = event.getGameObject();
		int id = gameObject.getId();
		switch (id) {
			case (Constants.ROOT_REGULAR_ID):
			case (Constants.ROOT_GREEN_ID):
				activeRoots.remove(gameObject);
				if (activeRoots.size() == 0) {
					rootsEventStarted = false;
				}
				break;

			case (Constants.SAPLING_ID84):
			case (Constants.SAPLING_ID85):
			case (Constants.SAPLING_ID87):
			case (Constants.SAPLING_ID88):
			case (Constants.SAPLING_ID90):
			case (Constants.SAPLING_ID91):
			case (Constants.ROTTING_LEAVES_ID):
			case (Constants.GREEN_LEAVES_ID):
			case (Constants.DROPPINGS_ID):
			case (Constants.WILD_MUSHROOM_ID96):
			case (Constants.WILD_MUSHROOM_ID97):
			case (Constants.WILD_MUSHROOM_ID98):
			case (Constants.SPLINTERED_BARK_ID):
				saplingPiles.remove(gameObject);
				if (saplingPiles.size() == 0) {
					saplingEventStarted = false;
					saplingOrder = new int[3];
				}
				break;
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npc) {
		int id = npc.getNpc().getId();
		switch (id) {
			case (Constants.LEPRECHAUN_NPC_ID):
				if (config.notifyLeprechaun()) {
					notifier.notify(Constants.NOTIFY_LEPRECHAUN);
				}
				break;

			//Why is bush NPC???
			case (Constants.STRANGE_BUSH_ID):
				matchedBushes.clear();
			case (Constants.FLOWERING_BUSH_ID):
				floweringBushes.add(npc.getNpc());
				if (config.notifyBushes() && !floweringTreeEventStarted) {
					floweringTreeEventStarted = true;
					notifier.notify(Constants.NOTIFY_FLOWER_TREE);
				}
				break;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npc) {
		int id = npc.getNpc().getId();

		switch (id) {
			//Why is bush NPC???
			case (Constants.STRANGE_BUSH_ID):
			case (Constants.FLOWERING_BUSH_ID):
				floweringBushes.remove(npc.getNpc());
				if (floweringBushes.size() == 0) {
					floweringTreeEventStarted = false;
				}
				break;
		}
	}

	private void removeInfoBox() {
		if (animaCounter == null) {
			return;
		}
		infoBoxManager.removeInfoBox(animaCounter);
		animaCounter = null;
	}

	private void addInfoBox() {
		if (!config.showAnimaGathered()) {
			return;
		}

		BufferedImage animaImage = itemManager.getImage(Constants.ITEM_ANIMA_BARK_ID);
		animaCounter = new Counter(animaImage, this, animaBarkCollected);
		animaCounter.setTooltip(Constants.BARK_COUNTER_TOOLTIP);
		infoBoxManager.addInfoBox(animaCounter);
	}

	private void clearEventFields() {
		activeRoots.clear();
		saplingPiles.clear();
		floweringBushes.clear();
		saplingOrder = new int[3];
		matchedBushes.clear();
		rootsEventStarted = saplingEventStarted = floweringTreeEventStarted = false;
	}
	private void reset() {
		removeInfoBox();
		animaBarkCollected = 0;
		hourlyAnimaBark = 0;
		clearEventFields();
	}

	private int tryParseInt(String v, int defaultVal) {
		try {
			return Integer.parseInt(v);
		} catch (NumberFormatException e) {
			return defaultVal;
		}
	}

	private int ingredientToOrder(String msg) {
		int index = -1;
		if(msg.contains("first")) {
			index = 0;
		}
		if(msg.contains("second")) {
			index = 1;
		}
		if(msg.contains("third")) {
			index = 2;
		}
		return index;
	}

	private int nameToId(String name) {
		int id = 0;

		if(name.contains("wild mushrooms")) {
			id = Constants.WILD_MUSHROOM_ID96;
		}
		if(name.contains("splintered bark")) {
			id = Constants.SPLINTERED_BARK_ID;
		}
		if(name.contains("green leaves")) {
			id = Constants.GREEN_LEAVES_ID;
		}
		if(name.contains("rotting leaves")) {
			id = Constants.ROTTING_LEAVES_ID;
		}
		if(name.contains("droppings")) {
			id = Constants.DROPPINGS_ID;
		}

		return id;
	}

	private void updateInfoBox() {
		if (hourlyAnimaBark > 0) {
//			panelComponent.getChildren().add(TitleComponent.builder().text("Anima-Infused Bark/Hr").color(Color.white).build());
//			panelComponent.getChildren().add(LineComponent.builder().left("Bark/Hr:").right("" + hourlyAnimaBark).build());
		}
	}

	private long calculateHourlyAnimaBark() {
		long animaBarkPerSecond = 0;
		long elapsedTime = (System.currentTimeMillis() - animaStartTime) / 1000;

		if (elapsedTime > 0) {
			animaBarkPerSecond = (animaBarkCollected) / elapsedTime;
		}
		return animaBarkPerSecond * 3600 / 1000;
	}

	//TODO:Use built-in IDS when client is updated
//	@Subscribe
//	public void onGameObjectSpawned(GameObjectSpawned event) {
//		GameObject gameObject = event.getGameObject();
//		switch (gameObject.getId())
//		{
//			case ObjectID.TREE_ROOTS:
//			case ObjectID.TREE_ROOTS_47483:
//			case ObjectID.STRUGGLING_SAPLING:
//			case ObjectID.STRUGGLING_SAPLING_47485:
//			case ObjectID.STRUGGLING_SAPLING_47487:
//			case ObjectID.STRUGGLING_SAPLING_47488:
//			case ObjectID.STRUGGLING_SAPLING_47490:
//			case ObjectID.STRUGGLING_SAPLING_47491:
//				break;
//		}
//	}
}
