package main;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.lang.Thread.State;
import java.text.DecimalFormat;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.PaintListener;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.message.Message;

import util.Variables;

@ScriptManifest(author = "Fhuckyou", category = Category.WOODCUTTING, name = "RiskyYews", version = 1.0)
public class RiskyYews extends AbstractScript implements PaintListener {
	private State state;
	
	private Timer r = new Timer();
	
	private int costOfItem;
	private int itemsMade;
	private double gpGained;
	private double totalGpGained;
	
	private GameObject eastYew;
	private GameObject westYew;
	private final Tile tt = new Tile(2997,3314,0);
	private final Tile t = new Tile(3022,3315,0);
	private String i = "Yew";
	private String currentState = "";

	
	
	private enum State{
        CHOP, WAITING, RUN, BANK, WALKTOYEW, WALKTOYEW2;//states
}
	
	private State getState(){
		eastYew = getGameObjects().closest(yew -> yew != null
				&& yew.getName() != null
				&& yew.getName().equals(i)
				&& yew.distance(tt) < 8
				&& !getLocalPlayer().isAnimating()
				&& !getLocalPlayer().isInCombat());
		
		westYew = getGameObjects().closest(yew -> yew != null
				&& yew.getName() != null
				&& yew.getName().equals(i)
				&& yew.distance(t) < 5
				&& !getLocalPlayer().isAnimating()
				&& !getLocalPlayer().isInCombat());
		
		if(westYew == null && eastYew != null
				&& !getInventory().isFull()
				//&& getInventory().contains("Cake")
				&& getInventory().contains("Steel axe")
				&& !getLocalPlayer().isAnimating()
				&& getLocalPlayer().distance(t) < 8) {
			log("in 2");
			return state.WALKTOYEW2;
		}
		
		if(westYew != null && eastYew == null
				&& !getInventory().isFull()
				//&& getInventory().contains("Cake")
				&& getInventory().contains("Steel axe")
				&& !getLocalPlayer().isAnimating()
				&& getLocalPlayer().distance(tt) < 8) {
			log("in 1");
			return state.WALKTOYEW;
		}
		
		
		
		if(!getInventory().contains("Steel axe")// || !getInventory().contains("Cake")
				|| getInventory().isFull()) {
			return state.BANK;
		}
		
		if(getLocalPlayer().isInCombat()) {
			return state.RUN;
		}
		
		if(westYew != null && !getInventory().isFull()
				//&& getInventory().contains("Cake")
				&& getInventory().contains("Steel axe")
				|| eastYew != null && !getInventory().isFull()
						//&& getInventory().contains("Cake")
						&& getInventory().contains("Steel axe")) {
			return state.CHOP;
		}
		
		if(westYew != null || eastYew != null) {
			if(!getInventory().isFull()
				//&& getInventory().contains("Cake")
				&& getInventory().contains("Steel axe")) {
			return state.CHOP;
		}
		}
		return state.WAITING;
	}
	private final Tile saftey = new Tile(3007,3339,0);
	
	public void onMessage(Message m) {
		if (m.getMessage().contains("You get some yew logs.")) {
			itemsMade += 1;
		}
	}
	
	@Override
	public int onLoop() {
		state = getState();
		
		switch(state) {
		
		case CHOP:
			if(getLocalPlayer().distance(eastYew) < getLocalPlayer().distance(westYew)) {
				getWalking().walk(eastYew);
				sleepUntil(() -> eastYew.isOnScreen(), 2000);
				if(eastYew != null && eastYew.isOnScreen()) {
					eastYew.interact("Chop down");
					sleep(500);
					sleepUntil(() -> !getLocalPlayer().isAnimating(), 2000);
					log("Potential tree to cut" + westYew.getName());
				}
			} else {
				getWalking().walk(westYew);
				sleepUntil(() -> westYew.isOnScreen(), 2000);
				if(westYew != null && westYew.isOnScreen()) {
					westYew.interact("Chop down");
					sleep(500);
					sleepUntil(() -> !getLocalPlayer().isAnimating(), 2000);
					log("Potential tree to cut" + westYew.getName());
				}
			}
			break;
			
		case WALKTOYEW:
			log("walking");
				getWalking().walk(t);
			break;
			
		case WALKTOYEW2:
			log("walking2");
				getWalking().walk(tt);
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
				if(!getWalking().isRunEnabled()) {
					getWalking().toggleRun();
				}
				getWalking().walk(saftey);
				
				if(!getTabs().isOpen(Tab.LOGOUT) && !getLocalPlayer().isInCombat()) {
					getTabs().logout();
				} else if(getTabs().isOpen(Tab.LOGOUT) && !getLocalPlayer().isInCombat()){
					getTabs().logout();
				}
				sleep(13000);
			break;
			
		case WAITING:
			currentState = "Chopping yew tree..";
			log("Waiting.");
			break;
			
		}
        return 0;
    }
	
	public void onPaint(Graphics g) {
		gpGained = itemsMade - costOfItem;
		totalGpGained = gpGained / 1000;
		
		g.setColor(Color.RED);
		g.setFont(new Font("Courier New", 0, 16));
		g.drawString("Time Running: " + r.formatTime(), 250, 357);
		g.drawString("Experience(p/h): " + getSkillTracker().getGainedExperience(Skill.WOODCUTTING) + "(" + getSkillTracker().getGainedExperiencePerHour(Skill.WOODCUTTING) + ")", 250, 370);
		g.drawString("Level(gained): " + getSkills().getRealLevel(Skill.WOODCUTTING) +"(" + getSkillTracker().getGainedLevels(Skill.WOODCUTTING) + ")", 250, 383);
		//DecimalFormat df = new DecimalFormat("#");
		//g.drawString("Total $ earned:"+ df.format(totalGpGained) + "Gp ", 250, 396);
		if(state != null)
			g.drawString("State: " + currentState.toString(), 250, 396);//409
	}
	
	public void onStart() {
		getSkillTracker().start(Skill.WOODCUTTING);
		costOfItem = 500;
	}

}
