package main;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.lang.Thread.State;
import java.text.DecimalFormat;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.PaintListener;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.message.Message;

@ScriptManifest(author = "Fhuckyou", category = Category.WOODCUTTING, name = "RiskyYews", version = 1.0)
public class RiskyYews extends AbstractScript implements PaintListener {
	private State state;
	
	private Timer r = new Timer();
	
	private int costOfItem;
	private int itemsMade;
	private double gpGained;
	private double totalGpGained;

	
	
	private enum State{
        CHOP, WAITING, RUN, BANK, WALKTOYEW;//states
}
	
	private State getState(){
		
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
		return state.WAITING;
	}
	private GameObject currentYew;
	private final Tile t = new Tile(3022,3315,0);
	private final Tile saftey = new Tile(3007,3339,0);
	private String i = "Yew";
	
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
				if(!getWalking().isRunEnabled()) {
					getWalking().toggleRun();
				}
				getWalking().walk(saftey);
			break;
			
		case WAITING:
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
			g.drawString("State: " + state.toString(), 250, 396);//409
	}
	
	public void onStart() {
		getSkillTracker().start(Skill.WOODCUTTING);
		costOfItem = 500;
	}

}
