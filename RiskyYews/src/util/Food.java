package util;

public enum Food {
	CAKE("Small fishing net"),
	HALFCAKE("Fly fishing rod");
	
	private String itemName;
	Food(String itemName){
		this.itemName = itemName;
	}
	public String getItemName(){
		return this.itemName;
	}
}
