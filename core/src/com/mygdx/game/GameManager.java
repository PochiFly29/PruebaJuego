package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

// El "Cerebro" (Singleton, State, Mediator)
public class GameManager {

    // --- Singleton ---
    private static GameManager instancia;

    // --- Estado del Juego ---
    private int vidas;
    private int puntos;
    private int highscore;
    private final int VIDAS_INICIALES = 3;

    // --- Persistencia ---
    private static final String PREFS_NAME = "GameLluviaPrefs";
    private static final String PREF_HIGHSCORE = "highscore";

    // --- Máquina de Estados (Patrón State) ---
    public enum EstadoJuego {
        ETAPA_1,
        PAUSA_PARA_ETAPA_2,
        ETAPA_2,
        PAUSA_PARA_ETAPA_3,
        ETAPA_3
    }
    private EstadoJuego estadoActual = EstadoJuego.ETAPA_1;
    private float timerEstado = 0f;

    // --- Lógica de Juego ---
    private final int SCORE_PARA_ETAPA_2 = 1000;
    private final int SCORE_PARA_ETAPA_3 = 2500;
    private final float DURACION_PAUSA = 2.0f;
    private final float DURACION_VIENTO = 5.0f;
    private boolean vientoALaDerecha = true;

    // --- Estrategias Pre-cacheadas ---
    private IComportamientoMovimiento movRecto_Lento = new MovimientoRecto(200f);
    private IComportamientoMovimiento movRecto_Normal = new MovimientoRecto(300f);
    private IComportamientoMovimiento movRecto_Rapido = new MovimientoRecto(450f);
    private IComportamientoMovimiento movDiag_Derecha = new MovimientoDiagonal(300f, 100f, 25f);
    private IComportamientoMovimiento movDiag_Izquierda = new MovimientoDiagonal(300f, -100f, -25f);
    private IComportamientoMovimiento movSerpiente = new MovimientoSerpenteante(300f, 50f, 3f);

    // --- Constructor y Singleton ---
    private GameManager() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        this.highscore = prefs.getInteger(PREF_HIGHSCORE, 0);
    }

    public static GameManager getInstance() {
        if (instancia == null) {
            instancia = new GameManager();
        }
        return instancia;
    }

    public void resetJuego() {
        this.vidas = VIDAS_INICIALES;
        this.puntos = 0;
        this.estadoActual = EstadoJuego.ETAPA_1;
        this.timerEstado = 0f;
    }

    /**
     * El "reloj" del Cerebro. Maneja los timers y transiciones de estado.
     * Llamar desde GameScreen.render().
     */
    public void update(float delta) {

        if (timerEstado > 0) {
            timerEstado -= delta;
        }

        switch (estadoActual) {
            case ETAPA_1:
                if (puntos >= SCORE_PARA_ETAPA_2) {
                    estadoActual = EstadoJuego.PAUSA_PARA_ETAPA_2;
                    timerEstado = DURACION_PAUSA;
                }
                break;

            case PAUSA_PARA_ETAPA_2:
                if (timerEstado <= 0) {
                    estadoActual = EstadoJuego.ETAPA_2;
                }
                break;

            case ETAPA_2:
                if (puntos >= SCORE_PARA_ETAPA_3) {
                    estadoActual = EstadoJuego.PAUSA_PARA_ETAPA_3;
                    timerEstado = DURACION_PAUSA;
                }
                break;

            case PAUSA_PARA_ETAPA_3:
                if (timerEstado <= 0) {
                    estadoActual = EstadoJuego.ETAPA_3;
                    timerEstado = DURACION_VIENTO;
                    vientoALaDerecha = true;
                }
                break;

            case ETAPA_3:
                if (timerEstado <= 0) {
                    vientoALaDerecha = !vientoALaDerecha;
                    timerEstado = DURACION_VIENTO;
                }
                break;
        }
    }

    // --- Métodos de consulta para la Fábrica (Lluvia) ---

    public boolean estaEnPausa() {
        return estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_2 ||
                estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_3;
    }

    public IComportamientoMovimiento getMovimientoParaGota() {
        switch (estadoActual) {
            case ETAPA_1:
            case PAUSA_PARA_ETAPA_2:
                return movRecto_Normal;
            case ETAPA_2:
            case PAUSA_PARA_ETAPA_3:
                return movDiag_Derecha;
            case ETAPA_3:
                return (vientoALaDerecha) ? movDiag_Derecha : movDiag_Izquierda;
            default:
                return movRecto_Normal;
        }
    }

    public IComportamientoMovimiento getMovimientoParaPowerup() {
        switch (estadoActual) {
            case ETAPA_1:
            case PAUSA_PARA_ETAPA_2:
                return movRecto_Lento;
            case ETAPA_2:
            case PAUSA_PARA_ETAPA_3:
                return movRecto_Rapido;
            case ETAPA_3:
                return movSerpiente;
            default:
                return movRecto_Lento;
        }
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

    public void actualizarHighscore() {
        if (this.puntos > this.highscore) {
            this.highscore = this.puntos;
            Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
            prefs.putInteger(PREF_HIGHSCORE, this.highscore);
            prefs.flush();
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