package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.EnumMap;
import java.util.Map;

// IMPORTA TU INTERFAZ Y ESTRATEGIAS (paquete imovimiento)
import com.mygdx.game.imovimiento.IComportamientoMovimiento;
import com.mygdx.game.imovimiento.MovimientoVertical;
import com.mygdx.game.imovimiento.MovimientoDiagonal;
import com.mygdx.game.imovimiento.MovimientoSerpenteante;

public class GameManager {

    private static GameManager instancia;

    public enum EstadoJuego {
        ETAPA_1,
        PAUSA_PARA_ETAPA_2,
        ETAPA_2,
        PAUSA_PARA_ETAPA_3,
        ETAPA_3,
        TORMENTA_ESPECIAL,
        PAUSA_POST_TORMENTA
    }

    // ---- Provider (Java 7, reemplaza Supplier) ----
    private interface Provider<T> { T get(); }

    // Estado núcleo
    private int vidas;
    private int puntos;
    private int highscore;
    private final int VIDAS_INICIALES = 3;

    private EstadoJuego estadoActual = EstadoJuego.ETAPA_1;
    private EstadoJuego estadoPrevio;
    private float timerEstado = 0f;
    private boolean mostrarEfectoTrueno = false;

    // Parametrización
    private final int SCORE_PARA_ETAPA_2 = 1000;
    private final int SCORE_PARA_ETAPA_3 = 2500;
    private final float DURACION_PAUSA = 2.0f;
    private final float DURACION_VIENTO = 5.0f;

    // Power-ups
    private boolean escudoActivo = false;
    private float timerEscudo = 0f;
    private boolean imanActivo = false;
    private float timerIman = 0f;

    // Tormenta
    private int contadorTormenta = 0;
    private final int MAX_TORMENTA_CARGA = 3;
    private final float DURACION_TORMENTA = 5.0f;
    private final float DURACION_PAUSA_POST_TORMENTA = 2.0f;

    // Dinámica Etapa 3
    private boolean vientoALaDerecha = true;

    // Estrategias precargadas
    private final IComportamientoMovimiento movRecto_Lento    = new MovimientoVertical(200f);
    private final IComportamientoMovimiento movRecto_Normal   = new MovimientoVertical(300f);
    private final IComportamientoMovimiento movRecto_Rapido   = new MovimientoVertical(450f);
    private final IComportamientoMovimiento movDiag_Derecha   = new MovimientoDiagonal(300f, 100f, 25f, null);
    private final IComportamientoMovimiento movDiag_Izquierda = new MovimientoDiagonal(300f, -100f, -25f, null);
    private final IComportamientoMovimiento movSerpiente      = new MovimientoSerpenteante(300f, 50f, 3f);
    private final IComportamientoMovimiento movTormenta       = new MovimientoVertical(700f);

    // Tablas de política (sin lambdas / sin getOrDefault)
    private final Map<EstadoJuego, Provider<IComportamientoMovimiento>> politicaGotas     = new EnumMap<EstadoJuego, Provider<IComportamientoMovimiento>>(EstadoJuego.class);
    private final Map<EstadoJuego, Provider<IComportamientoMovimiento>> politicaPowerups  = new EnumMap<EstadoJuego, Provider<IComportamientoMovimiento>>(EstadoJuego.class);

    // Persistencia
    private static final String PREFS_NAME = "GameLluviaPrefs";
    private static final String PREF_HIGHSCORE = "highscore";

    private GameManager() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        this.highscore = prefs.getInteger(PREF_HIGHSCORE, 0);
        configurarPoliticas();
    }

    public static GameManager getInstance() {
        if (instancia == null) instancia = new GameManager();
        return instancia;
    }

    private void configurarPoliticas() {
        // Gotas
        politicaGotas.put(EstadoJuego.ETAPA_1, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movRecto_Normal; }
        });
        politicaGotas.put(EstadoJuego.PAUSA_PARA_ETAPA_2, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movRecto_Normal; }
        });
        politicaGotas.put(EstadoJuego.ETAPA_2, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movDiag_Derecha; }
        });
        politicaGotas.put(EstadoJuego.PAUSA_PARA_ETAPA_3, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movDiag_Derecha; }
        });
        politicaGotas.put(EstadoJuego.ETAPA_3, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return vientoALaDerecha ? movDiag_Derecha : movDiag_Izquierda; }
        });
        politicaGotas.put(EstadoJuego.TORMENTA_ESPECIAL, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movTormenta; }
        });
        politicaGotas.put(EstadoJuego.PAUSA_POST_TORMENTA, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movRecto_Normal; }
        });

        // Powerups
        politicaPowerups.put(EstadoJuego.ETAPA_1, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movRecto_Lento; }
        });
        politicaPowerups.put(EstadoJuego.PAUSA_PARA_ETAPA_2, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movRecto_Lento; }
        });
        politicaPowerups.put(EstadoJuego.ETAPA_2, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movRecto_Rapido; }
        });
        politicaPowerups.put(EstadoJuego.PAUSA_PARA_ETAPA_3, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movRecto_Rapido; }
        });
        politicaPowerups.put(EstadoJuego.ETAPA_3, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movSerpiente; }
        });
        politicaPowerups.put(EstadoJuego.TORMENTA_ESPECIAL, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movRecto_Lento; } // fallback
        });
        politicaPowerups.put(EstadoJuego.PAUSA_POST_TORMENTA, new Provider<IComportamientoMovimiento>() {
            public IComportamientoMovimiento get() { return movRecto_Lento; }
        });
    }

    public void resetJuego() {
        vidas = VIDAS_INICIALES;
        puntos = 0;
        estadoActual = EstadoJuego.ETAPA_1;
        timerEstado = 0f;
        contadorTormenta = 0;
        escudoActivo = false;
        imanActivo = false;
        timerEscudo = 0f;
        timerIman = 0f;
        vientoALaDerecha = true;
    }

    public void update(float delta) {
        if (timerEstado > 0) timerEstado -= delta;

        if (escudoActivo) {
            timerEscudo -= delta;
            if (timerEscudo <= 0) escudoActivo = false;
        }

        if (imanActivo) {
            timerIman -= delta;
            if (timerIman <= 0) imanActivo = false;
        }

        if (estadoActual == EstadoJuego.ETAPA_1 && puntos >= SCORE_PARA_ETAPA_2) {
            estadoActual = EstadoJuego.PAUSA_PARA_ETAPA_2; timerEstado = DURACION_PAUSA; mostrarEfectoTrueno = true; return;
        }
        if (estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_2 && timerEstado <= 0) { estadoActual = EstadoJuego.ETAPA_2; return; }

        if (estadoActual == EstadoJuego.ETAPA_2 && puntos >= SCORE_PARA_ETAPA_3) {
            estadoActual = EstadoJuego.PAUSA_PARA_ETAPA_3; timerEstado = DURACION_PAUSA; mostrarEfectoTrueno = true; return;
        }
        if (estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_3 && timerEstado <= 0) {
            estadoActual = EstadoJuego.ETAPA_3; timerEstado = DURACION_VIENTO; vientoALaDerecha = true; return;
        }

        if (estadoActual == EstadoJuego.ETAPA_3 && timerEstado <= 0) {
            vientoALaDerecha = !vientoALaDerecha; timerEstado = DURACION_VIENTO; return;
        }

        if (estadoActual == EstadoJuego.TORMENTA_ESPECIAL && timerEstado <= 0) {
            estadoActual = EstadoJuego.PAUSA_POST_TORMENTA; timerEstado = DURACION_PAUSA_POST_TORMENTA; return;
        }
        if (estadoActual == EstadoJuego.PAUSA_POST_TORMENTA && timerEstado <= 0) {
            estadoActual = estadoPrevio;
            if (estadoActual == EstadoJuego.ETAPA_3) timerEstado = DURACION_VIENTO;
        }
    }

    public boolean debeMostrarTrueno() {
        if (mostrarEfectoTrueno) { mostrarEfectoTrueno = false; return true; }
        return false;
    }

    public EstadoJuego getEstadoActual() { return estadoActual; }

    public boolean estaEnPausa() {
        return estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_2 ||
                estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_3 ||
                estadoActual == EstadoJuego.PAUSA_POST_TORMENTA;
    }

    public boolean estaEnPausaDeTransicion() {
        return estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_2 ||
                estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_3;
    }

    public IComportamientoMovimiento getMovimientoParaGota() {
        Provider<IComportamientoMovimiento> prov = politicaGotas.get(estadoActual);
        if (prov != null) return prov.get();
        return movRecto_Normal; // fallback
    }

    public IComportamientoMovimiento getMovimientoParaPowerup() {
        Provider<IComportamientoMovimiento> prov = politicaPowerups.get(estadoActual);
        if (prov != null) return prov.get();
        return movRecto_Lento; // fallback
    }

    // Lógica de puntaje y vidas
    public void sumarPuntos(int cantidad) { puntos += cantidad; }
    public void perderVida() { if (vidas > 0) vidas--; }
    public void sumarVida() { vidas++; }

    public void actualizarHighscore() {
        if (puntos > highscore) {
            highscore = puntos;
            Preferences prefs = Gdx.app.getPreferences(PREF_HIGHSCORE);
            // OJO: bug típico — el nombre del pref era PREFS_NAME. Corregimos:
            prefs = Gdx.app.getPreferences(PREFS_NAME);
            prefs.putInteger(PREF_HIGHSCORE, highscore);
            prefs.flush();
        }
    }

    // Power-ups
    public void activarEscudo(float duracion) { escudoActivo = true; timerEscudo = duracion; }
    public boolean isEscudoActivo() { return escudoActivo; }
    public void consumirEscudo() { escudoActivo = false; timerEscudo = 0f; }

    public void activarIman(float duracion) { imanActivo = true; timerIman = duracion; }
    public boolean isImanActivo() { return imanActivo; }

    // Tormenta
    public void incrementarContadorTormenta() {
        if (estadoActual == EstadoJuego.TORMENTA_ESPECIAL || estadoActual == EstadoJuego.PAUSA_POST_TORMENTA) return;
        contadorTormenta++;
        if (contadorTormenta >= MAX_TORMENTA_CARGA) {
            contadorTormenta = 0;
            estadoPrevio = estadoActual;
            estadoActual = EstadoJuego.TORMENTA_ESPECIAL;
            timerEstado = DURACION_TORMENTA;
        }
    }

    // Getters UI
    public int getVidas() { return vidas; }
    public int getPuntos() { return puntos; }
    public int getHighscore() { return highscore; }
    public int getContadorTormenta() { return contadorTormenta; }
}
