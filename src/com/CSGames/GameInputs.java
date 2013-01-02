package com.CSGames;

public class GameInputs {
	
	// =========================================================== 
	// Fields
	// ===========================================================
	
	public int canvasWidth = 0;
	public int canvasHeight = 0;
	
	public Position punteroDPos = new Position(0, 0);
	public boolean punteroD = false;
	public boolean punteroDArr = false;
	public boolean punteroDAbj = false;
	public boolean punteroDDcha = false;
	public boolean punteroDIzda = false;
	public boolean punteroDFin = false;

	
	public Position punteroIPos = new Position(0, 0);
	public boolean punteroI = false;
	public boolean punteroIArr = false;
	public boolean punteroIAbj = false;
	public boolean punteroIDcha = false;
	public boolean punteroIIzda = false;
	public boolean punteroIFin = false;
	
	public boolean teclaEspacio = false;
	public boolean teclaDcha = false;
	public boolean teclaIzda = false;
	public boolean teclaArr = false;
	
    private static GameInputs INSTANCE = new GameInputs();
	
	// =========================================================== 
	// Constructors
	// ===========================================================
	
    private GameInputs() {}
    
	// =========================================================== 
	// Methods
	// ===========================================================
 
    public static GameInputs getInstance() {
        return INSTANCE;
    }	
    
    /** apaga todos los eventos del input*/
    public void processed() {
    	punteroDArr = false;
    	punteroDAbj = false;
    	punteroDDcha = false;
    	punteroDIzda = false;
    	punteroDFin = false;
    	
    	punteroIArr = false;
    	punteroIAbj = false;
    	punteroIDcha = false;
    	punteroIIzda = false;
    	punteroIFin = false;
    }
}
