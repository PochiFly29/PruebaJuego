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

    public boolean debeMostrarTrueno() {
        if (mostrarEfectoTrueno) {
            mostrarEfectoTrueno = false; // Se consume el flag
            return true;
        }
        return false;
    }

    // --- Máquina de Estados (Patrón State) ---
    public enum EstadoJuego {
        ETAPA_1,
        PAUSA_PARA_ETAPA_2,
        ETAPA_2,
        PAUSA_PARA_ETAPA_3,
        ETAPA_3,
        TORMENTA_ESPECIAL,
        PAUSA_POST_TORMENTA
    }
    private EstadoJuego estadoActual = EstadoJuego.ETAPA_1;
    private EstadoJuego estadoPrevio;
    private float timerEstado = 0f;
    private boolean mostrarEfectoTrueno = false;

    // --- Lógica de Juego ---
    private final int SCORE_PARA_ETAPA_2 = 1000;
    private final int SCORE_PARA_ETAPA_3 = 2500;
    private final float DURACION_PAUSA = 2.0f;
    private final float DURACION_VIENTO = 5.0f;
    private boolean vientoALaDerecha = true;

    // Estados de Power-Ups ---
    private boolean escudoActivo = false;
    private float timerEscudo = 0f;
    private boolean imanActivo = false;
    private float timerIman = 0f;

    // Lógica de Súper Tormenta ---
    private int contadorTormenta = 0;
    private final int MAX_TORMENTA_CARGA = 3; // Necesitas 3 para activar
    private final float DURACION_TORMENTA = 5.0f; // 5s de súper lluvia
    private final float DURACION_PAUSA_POST_TORMENTA = 2.0f;

    // --- Estrategias Pre-cacheadas ---
    private IComportamientoMovimiento movRecto_Lento = new MovimientoRecto(200f);
    private IComportamientoMovimiento movRecto_Normal = new MovimientoRecto(300f);
    private IComportamientoMovimiento movRecto_Rapido = new MovimientoRecto(450f);
    private IComportamientoMovimiento movDiag_Derecha = new MovimientoDiagonal(300f, 100f, 25f);
    private IComportamientoMovimiento movDiag_Izquierda = new MovimientoDiagonal(300f, -100f, -25f);
    private IComportamientoMovimiento movSerpiente = new MovimientoSerpenteante(300f, 50f, 3f);
    private IComportamientoMovimiento movTormenta = new MovimientoRecto(700f);

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
        this.contadorTormenta = 0;
        this.escudoActivo = false;
        this.imanActivo = false;
        this.timerEscudo = 0f;
        this.timerIman = 0f;
    }

    /**
     * El "reloj" del Cerebro. Maneja los timers y transiciones de estado.
     * Llamar desde GameScreen.render().
     */
    public void update(float delta) {

        // Actualizar timer de estado
        if (timerEstado > 0) {
            timerEstado -= delta;
        }

        // Actualizar timer de escudo
        if (escudoActivo) {
            timerEscudo -= delta;
            if (timerEscudo <= 0) {
                escudoActivo = false;
            }
        }

        // Actualizar timer de imán
        if (imanActivo) {
            timerIman -= delta;
            if (timerIman <= 0) {
                imanActivo = false;
            }
        }

        switch (estadoActual) {
            case ETAPA_1:
                if (puntos >= SCORE_PARA_ETAPA_2) {
                    estadoActual = EstadoJuego.PAUSA_PARA_ETAPA_2;
                    timerEstado = DURACION_PAUSA;
                    mostrarEfectoTrueno = true; // ¡Se activa el flag!
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
                    mostrarEfectoTrueno = true; // ¡Se activa el flag!
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
            case TORMENTA_ESPECIAL:
                if (timerEstado <= 0) {
                    estadoActual = EstadoJuego.PAUSA_POST_TORMENTA;
                    timerEstado = DURACION_PAUSA_POST_TORMENTA;
                }
                break;
            case PAUSA_POST_TORMENTA:
                if (timerEstado <= 0) {
                    // Regresar al estado guardado
                    estadoActual = estadoPrevio;
                    // Reiniciar el timer de viento si volvemos a Etapa 3
                    if(estadoActual == EstadoJuego.ETAPA_3) {
                        timerEstado = DURACION_VIENTO;
                    }
                }
                break;
        }
    }

    // --- Métodos de consulta para la Fábrica (Lluvia) ---

    public EstadoJuego getEstadoActual() {
        return estadoActual;
    }

    public boolean estaEnPausa() {
        return estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_2 ||
                estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_3 ||
                estadoActual == EstadoJuego.PAUSA_POST_TORMENTA;
    }

    public IComportamientoMovimiento getMovimientoParaGota() {
        // Chequeo especial para tormenta
        if (estadoActual == EstadoJuego.TORMENTA_ESPECIAL) {
            return movTormenta;
        }

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
                // Los power-ups no caen durante la tormenta
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

    // Métodos para Power-Ups ---

    public void activarEscudo(float duracion) {
        this.escudoActivo = true;
        this.timerEscudo = duracion;
    }

    public boolean isEscudoActivo() {
        return escudoActivo;
    }

    public void consumirEscudo() {
        this.escudoActivo = false;
        this.timerEscudo = 0;
    }

    public void activarIman(float duracion) {
        this.imanActivo = true;
        this.timerIman = duracion;
    }

    public boolean isImanActivo() {
        return imanActivo;
    }

    public void incrementarContadorTormenta() {
        // No sumar si ya está en una tormenta
        if (estadoActual == EstadoJuego.TORMENTA_ESPECIAL ||
                estadoActual == EstadoJuego.PAUSA_POST_TORMENTA) return;

        this.contadorTormenta++;
        if (this.contadorTormenta >= MAX_TORMENTA_CARGA) {
            this.contadorTormenta = 0;
            // Guardar estado actual para volver
            this.estadoPrevio = this.estadoActual;
            // Activar la tormenta
            this.estadoActual = EstadoJuego.TORMENTA_ESPECIAL;
            this.timerEstado = DURACION_TORMENTA;
        }
    }

    public boolean estaEnPausaDeTransicion() {
        return estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_2 ||
                estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_3;
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

    public int getContadorTormenta() {
        return contadorTormenta;
    }
}