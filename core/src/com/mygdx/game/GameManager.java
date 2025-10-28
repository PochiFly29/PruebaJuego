package com.mygdx.game;

// Imports necesarios para la persistencia
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

// Singleton para gestionar el estado del juego
public class GameManager {
    private static GameManager instancia;

    private int vidas;
    private int puntos;
    private int highscore;

    private final int VIDAS_INICIALES = 3;

    // Constantes para la persistencia
    private static final String PREFS_NAME = "GameLluviaPrefs";
    private static final String PREF_HIGHSCORE = "highscore";

    // Constructor privado para evitar instanciación externa
    private GameManager() {
        // Cargar el highscore guardado
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        this.highscore = prefs.getInteger(PREF_HIGHSCORE, 0); // 0 si no existe
    }

    // Método de acceso global
    public static GameManager getInstance() {
        if (instancia == null) {
            instancia = new GameManager();
        }
        return instancia;
    }

    // Resetea el estado para una nueva partida
    public void resetJuego() {
        this.vidas = VIDAS_INICIALES;
        this.puntos = 0;
    }

    // --- Métodos de Lógica de Juego ---

    public void sumarPuntos(int cantidad) {
        this.puntos += cantidad;
    }

    public void perderVida() {
        if (vidas > 0) {
            this.vidas--;
        }
    }

    public void sumarVida() {
        this.vidas++;
    }

    // Comprueba y actualiza el highscore
    public void actualizarHighscore() {
        if (this.puntos > this.highscore) {
            this.highscore = this.puntos;

            // Guardar el nuevo highscore en disco
            Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
            prefs.putInteger(PREF_HIGHSCORE, this.highscore);
            prefs.flush(); // Fundamental para que se guarde
        }
    }

    // --- Getters para la UI ---

    public int getVidas() {
        return vidas;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getHighscore() {
        return highscore;
    }
}