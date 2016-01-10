import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.PaintListener;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;

@ScriptManifest(author = "Fhuckyou", category = Category.COMBAT, name = "RiskyGuardKiller", version = 1.0)
public class main extends AbstractScript implements PaintListener {
	
	//Variables vc = new Variables();ds
	
	private State state;
	private final BasicStroke stroke1 = new BasicStroke(5);
	
	
	
	private enum State{
        ATT, NULL, LOOT, STEAL, EAT, HOPWORLDS, DROP, PICKUP;//states
}
	
	private State getState(){
		boolean inCombat = getLocalPlayer().isInCombat();
		boolean hasBolts = getEquipment().contains("Bone bolts");
		
		ss = getGameObjects().closest(stalls -> stalls != null
												&& stalls.distance(goodStall) < 2
												&& stalls.getName() != null
												&& stalls.getName().equals(j));
		
		food = getInventory().get(fz -> fz != null && fz.hasAction("Eat"));
		
		stall = getGameObjects().closest(stall -> stall != null
												&& stall.getName() != null
												&& stall.getName().equals(j)
												&& getLocalPlayer().getInteractingCharacter() == null);
		
		item = getGroundItems().closest(ite -> ite != null
										&& ite.getName() != null
										&& ite.getName().equals(i)
										&& getLocalPlayer().getInteractingCharacter() == null);
		
		currentNpc = getNpcs().closest(npc -> npc != null 
										&& npc.getName() != null 
										&& npc.getName().equals(npc2)
										&& !npc.isInCombat()
										&& npc.getLevel() == 20
										&& getLocalPlayer().getInteractingCharacter() == null
										&& npc.distance(centerPoint) < 8);
		
		if(currentNpc != null
				&& getCombat().getHealthPercent() >= 90
				&& food != null
				&& hasBolts) {
			return state.ATT;
		}
		
		if(getDialogues().canContinue()) {
			getDialogues().clickContinue();
		}
		
		if(!hasBolts && !getLocalPlayer().isInCombat() && !getLocalPlayer().isMoving()) {
			getTabs().logout();
			currentState = "Logging out.";
			sleep(13000);
		} else if(!hasBolts && getLocalPlayer().isInCombat()) {
			getWalking().walk(saftey);
			currentState = "Running to saftey";
		}
		
		if(getInventory().isFull() && getInventory().contains("Bread")
				|| getInventory().isFull() && getInventory().contains("Chocolate slice")) {
			return state.DROP;
		}
		
		//if(getPlayers().all().size() > 7 && currentNpc == null) {
			//return state.HOPWORLDS;
		//}
		
		if(getLocalPlayer().getInteractingCharacter() == null && !getLocalPlayer().isInCombat()) {
			if(food == null) {
			return state.STEAL;
		}
	}
		
		if(currentNpc == null && getLocalPlayer().distance(centerPoint) > 8 && hasBolts) {
			getWalking().walk(centerPoint);
		}
		
		if(getCombat().getHealthPercent() <= 70 && food != null) {
			return state.EAT;
		}
		
		//if(item != null) {
			//return state.LOOT;
		//}
		
			return state.NULL;//doing nothing
	
	}
	private NPC currentNpc;
	public GroundItem item;
	private GameObject stall;
	private Item food;
	private String j = "Baker's stall";
	public String i = "Bone bolts";
	public String npc2 = "Guard";
	private GameObject ss;
	public Tile goodStall = new Tile(2656, 3311, 0);
	public Tile centerPoint = new Tile(2661, 3307, 0);
	public Tile saftey = new Tile(2654, 3284, 0);
	private Timer r = new Timer();
	private String currentState = "";
	
	@Override
	public int onLoop() {
		
		state = getState();
		
		switch(state) {
		
		case ATT:
				currentState = "Attacking gaurd.";
				log("Potential attack to " + currentNpc.getName());
				currentNpc.interact("Attack");
				sleep(3000);
				sleepUntil(() -> getLocalPlayer().getInteractingCharacter() == null, 2000);
			break;
			
		case LOOT:
				currentState = "Looting.";
				if(item != null && item.interact("Take")) {
					sleepUntil(() -> item == null, 2000);
				}
				log("took");
			break;
			
		//case HOPWORLDS:
			//getWorldHopper().hopWorld(74);
			//break;
			
		case EAT:
			currentState = "Eating";
			
			for(int i = 0; i < 28; i++){//Credit to Nezz's open source
				Item food = getInventory().getItemInSlot(i);
				if(food != null && food.hasAction("Eat") && getCombat().getHealthPercent() != 100){
					getInventory().slotInteract(i, "Eat");
					sleep(600,900);
					break;
				}
			}
			break;
			
		case DROP:
				currentState = "Dropping bread and/or Chocolate slices";
				if(getInventory().isFull()) {
					getInventory().dropAll("Bread");
					getInventory().dropAll("Chocolate slice");
				}
			break;
			
		case STEAL:
			currentState = "Getting more food.";
			log("stealing");
			
			if(ss != null && getLocalPlayer().distance(goodStall) < 5 && stall != null && !getLocalPlayer().isInCombat()) {
				if(food == null) {
					stall.interact("Steal-from");
				}
			}
			
			if(ss != null 
					&& stall != null
					&& stall.distance(getLocalPlayer()) > 5
					|| getLocalPlayer().distance(goodStall) > 1) {
				getWalking().walk(goodStall);
			}
			break;
			
		case NULL:
			currentState = "Waiting...";
			log("Waiting.");
			break;
			
		}
        return 0;
    }
	
	public void onPaint(Graphics g) {
		
		g.setColor(Color.WHITE);
		g.setFont(new Font("Courier New", 0, 16));
		g.drawString("Time Running: " + r.formatTime(), 12, 285);
		g.drawString("Experience(p/h): " + getSkillTracker().getGainedExperience(Skill.RANGED) + "(" + getSkillTracker().getGainedExperiencePerHour(Skill.RANGED) + ")", 12, 300);
		g.drawString("Level(gained): " + getSkills().getRealLevel(Skill.RANGED) +"(" + getSkillTracker().getGainedLevels(Skill.RANGED) + ")", 12, 315);
		//DecimalFormat df = new DecimalFormat("#");
		//g.drawString("Total $ earned:"+ df.format(totalGpGained) + "Gp ", 250, 396);
		if(state != null)
			g.drawString("State: " + currentState.toString(), 12, 330);//409
	}
	
	public void onStart() {
		getSkillTracker().start(Skill.RANGED);
	}
}
