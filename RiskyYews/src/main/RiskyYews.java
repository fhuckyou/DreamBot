package main;
import java.lang.Thread.State;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;

@ScriptManifest(author = "Fhuckyou", category = Category.WOODCUTTING, name = "RiskyYews", version = 1.0)
public class RiskyYews extends AbstractScript {
	private State state;

	
	
	private enum State{
        CHOP, NULL, RUN, BANK, WALKTOYEW;//states
}
	
	private State getState(){
		
		nextYew = getGameObjects().closest(yew -> yew != null
				&& yew.getName() != null
				&& yew.getName().equals(i)
				&& !getLocalPlayer().isAnimating()
				&& !getLocalPlayer().isInCombat());
		
		currentYew = getGameObjects().closest(yew -> yew != null
				&& yew.getName() != null
				&& yew.getName().equals(i)
				&& yew.distance(t) < 5
				&& !getLocalPlayer().isAnimating()
				&& !getLocalPlayer().isInCombat());
		
		if(currentYew == null && !getInventory().isFull()
				&& getInventory().contains("Steel axe")
				//&& getInventory().contains("Cake")
				&& !getLocalPlayer().isAnimating()
				&& getLocalPlayer().distance(t) > 25) {
			return state.WALKTOYEW;
		}
		
		if(currentYew != null && !getInventory().isFull()
				&& getInventory().contains("Steel axe")
				//&& getInventory().contains("Cake")
				&& !getLocalPlayer().isAnimating()
				&& getLocalPlayer().distance(t) > 25) {
			log("fsdf");
			return state.WALKTOYEW;		
			}
		
		
		
		if(!getInventory().contains("Steel axe")// || !getInventory().contains("Cake")
				|| getInventory().isFull()) {
			return state.BANK;
		}
		
		if(getLocalPlayer().isInCombat()) {
			return state.RUN;
		}
		
		if(currentYew != null && !getInventory().isFull()
				//&& getInventory().contains("Cake")
				&& getInventory().contains("Steel axe")) {
			return state.CHOP;
		}
			return state.NULL;//doing nothing
	
	}
	private GameObject currentYew;
	private GameObject nextYew;
	private final Tile t = new Tile(3022,3315,0);
	private String i = "Yew";
	
	@Override
	public int onLoop() {
		state = getState();
		
		switch(state) {
		
		case CHOP:
			if(currentYew.interact("Chop down")) {
				sleepUntil(() -> !getLocalPlayer().isAnimating(), 2000);
				log("Potential tree to cut" + currentYew.getName());
			}
			break;
			
		case WALKTOYEW:
			log("walking");
				getWalking().walk(t);
			break;
			
		case BANK:
				log("Banking yews");
				if(!getBank().isOpen() && getBank().openClosest()) {
					getBank().open();				
					}
				
				if (getBank().isOpen()) {
					if(!getInventory().contains("Steel axe") 
							//|| !getInventory().contains("Cake")
							|| getInventory().isFull()) {
							getBank().depositAllItems();
							sleep(500);
							getBank().withdraw("Cake", 4);
							sleep(500);
							getBank().withdraw("Steel axe");
							sleep(500);
							getWalking().walk(t);
							}
						}
							break;
		
		case RUN:
				log("Running to safezone");
			break;
			
		case NULL:
			log("Waiting.");
			break;
			
		}
        return 0;
    }
	
	public void onStart() {
	}

}
