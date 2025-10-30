package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.mygdx.game.imovimiento.IComportamientoMovimiento;
import com.mygdx.game.imovimiento.MovimientoDiagonal;
import com.mygdx.game.imovimiento.MovimientoSerpenteante;
import com.mygdx.game.imovimiento.MovimientoVertical;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class GameManager {

    // ---------- Singleton ----------
    private static GameManager instancia;
    public static GameManager getInstance() {
        if (instancia == null) instancia = new GameManager();
        return instancia;
    }

    // ---------- AudioBus genérico ----------
    public static class AudioBus {
        private final Map<String, Music> musics = new HashMap<String, Music>();
        private final Map<String, Integer> refs = new HashMap<String, Integer>();

        public void register(String name, Music music) {
            musics.put(name, music);
            refs.put(name, 0);
        }

        public synchronized void playRef(String name, boolean loop) {
            Music m = musics.get(name);
            if (m == null) return;
            Integer r = refs.get(name);
            if (r == null) r = 0;
            if (r == 0) { m.setLooping(loop); m.play(); }
            refs.put(name, r + 1);
        }

        public synchronized void stopRef(String name) {
            Music m = musics.get(name);
            if (m == null) return;
            Integer r = refs.get(name);
            if (r == null) r = 0;
            if (r > 0) {
                r = r - 1;
                refs.put(name, r);
                if (r == 0) { m.stop(); m.setPosition(0f); }
            }
        }

        public void stopAll() {
            for (Music m : musics.values()) {
                try { m.stop(); m.setPosition(0f); } catch (Exception ignored) {}
            }
            for (String k : refs.keySet()) refs.put(k, 0);
        }
    }

    private final AudioBus audioBus = new AudioBus();

    public void setWindMusic(Music wind) { audioBus.register("wind", wind); }
    public void startWind() { audioBus.playRef("wind", true); }
    public void stopWind()  { audioBus.stopRef("wind"); }
    public AudioBus getAudioBus() { return audioBus; }

    // ---------- Estado del juego ----------
    public enum EstadoJuego {
        ETAPA_1, PAUSA_PARA_ETAPA_2, ETAPA_2, PAUSA_PARA_ETAPA_3, ETAPA_3,
        TORMENTA_ESPECIAL, PAUSA_POST_TORMENTA
    }

    private EstadoJuego estadoActual = EstadoJuego.ETAPA_1;
    private EstadoJuego estadoPrevio;
    private float timerEstado = 0f;
    private boolean mostrarEfectoTrueno = false;

    private int vidas;
    private int puntos;
    private int highscore;
    private static final int VIDAS_INICIALES = 3;

    // Parámetros
    private static final int SCORE_PARA_ETAPA_2 = 1000;
    private static final int SCORE_PARA_ETAPA_3 = 2500;
    private static final float DURACION_PAUSA = 2.0f;
    private static final float DURACION_VIENTO = 5.0f;

    // Power-ups
    private boolean escudoActivo = false;
    private float timerEscudo = 0f;
    private boolean imanActivo = false;
    private float timerIman = 0f;

    // Tormenta especial
    private int contadorTormenta = 0;
    private static final int MAX_TORMENTA_CARGA = 3;
    private static final float DURACION_TORMENTA = 5.0f;
    private static final float DURACION_PAUSA_POST_TORMENTA = 2.0f;

    // Viento en ETAPA_3
    private boolean vientoALaDerecha = true;

    // ---------- Estrategias (Strategy) ----------
    private IComportamientoMovimiento movRecto_Lento   = new MovimientoVertical(200f);
    private IComportamientoMovimiento movRecto_Normal  = new MovimientoVertical(300f);
    private IComportamientoMovimiento movRecto_Rapido  = new MovimientoVertical(450f);
    // diagonales sin Music directo (lo maneja AudioBus)
    private IComportamientoMovimiento movDiag_Derecha  = new MovimientoDiagonal(300f,  100f,  25f);
    private IComportamientoMovimiento movDiag_Izquierda= new MovimientoDiagonal(300f, -100f, -25f);
    private IComportamientoMovimiento movSerpiente     = new MovimientoSerpenteante(300f, 50f, 3f);
    private IComportamientoMovimiento movTormenta      = new MovimientoVertical(700f);

    // Política sin lambdas (Java 7)
    private interface Politica { IComportamientoMovimiento get(); }
    private final Map<EstadoJuego, Politica> politicaGotas = new EnumMap<EstadoJuego, Politica>(EstadoJuego.class);
    private final Map<EstadoJuego, Politica> politicaPowerups = new EnumMap<EstadoJuego, Politica>(EstadoJuego.class);

    // Persistencia
    private static final String PREFS_NAME = "GameLluviaPrefs";
    private static final String PREF_HIGHSCORE = "highscore";

    private GameManager() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        this.highscore = prefs.getInteger(PREF_HIGHSCORE, 0);
        configurarPoliticas();
        resetJuego();
    }

    private void configurarPoliticas() {
        politicaGotas.put(EstadoJuego.ETAPA_1, new Politica() {
            public IComportamientoMovimiento get() { return movRecto_Normal; }
        });
        politicaGotas.put(EstadoJuego.PAUSA_PARA_ETAPA_2, new Politica() {
            public IComportamientoMovimiento get() { return movRecto_Normal; }
        });
        politicaGotas.put(EstadoJuego.ETAPA_2, new Politica() {
            public IComportamientoMovimiento get() { return movDiag_Derecha; }
        });
        politicaGotas.put(EstadoJuego.PAUSA_PARA_ETAPA_3, new Politica() {
            public IComportamientoMovimiento get() { return movDiag_Derecha; }
        });
        politicaGotas.put(EstadoJuego.ETAPA_3, new Politica() {
            public IComportamientoMovimiento get() {
                return (vientoALaDerecha) ? movDiag_Derecha : movDiag_Izquierda;
            }
        });
        politicaGotas.put(EstadoJuego.TORMENTA_ESPECIAL, new Politica() {
            public IComportamientoMovimiento get() { return movTormenta; }
        });
        politicaGotas.put(EstadoJuego.PAUSA_POST_TORMENTA, new Politica() {
            public IComportamientoMovimiento get() { return movRecto_Normal; }
        });

        politicaPowerups.put(EstadoJuego.ETAPA_1, new Politica() {
            public IComportamientoMovimiento get() { return movRecto_Lento; }
        });
        politicaPowerups.put(EstadoJuego.PAUSA_PARA_ETAPA_2, new Politica() {
            public IComportamientoMovimiento get() { return movRecto_Lento; }
        });
        politicaPowerups.put(EstadoJuego.ETAPA_2, new Politica() {
            public IComportamientoMovimiento get() { return movRecto_Rapido; }
        });
        politicaPowerups.put(EstadoJuego.PAUSA_PARA_ETAPA_3, new Politica() {
            public IComportamientoMovimiento get() { return movRecto_Rapido; }
        });
        politicaPowerups.put(EstadoJuego.ETAPA_3, new Politica() {
            public IComportamientoMovimiento get() { return movSerpiente; }
        });
        politicaPowerups.put(EstadoJuego.TORMENTA_ESPECIAL, new Politica() {
            public IComportamientoMovimiento get() { return movRecto_Lento; }
        });
        politicaPowerups.put(EstadoJuego.PAUSA_POST_TORMENTA, new Politica() {
            public IComportamientoMovimiento get() { return movRecto_Lento; }
        });
    }

    // ---------- Ciclo de estado ----------
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
        audioBus.stopAll(); // limpia audios
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
            estadoActual = EstadoJuego.PAUSA_PARA_ETAPA_2;
            timerEstado = DURACION_PAUSA;
            mostrarEfectoTrueno = true;
            return;
        }
        if (estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_2 && timerEstado <= 0) {
            estadoActual = EstadoJuego.ETAPA_2;
            return;
        }

        if (estadoActual == EstadoJuego.ETAPA_2 && puntos >= SCORE_PARA_ETAPA_3) {
            estadoActual = EstadoJuego.PAUSA_PARA_ETAPA_3;
            timerEstado = DURACION_PAUSA;
            mostrarEfectoTrueno = true;
            return;
        }
        if (estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_3 && timerEstado <= 0) {
            estadoActual = EstadoJuego.ETAPA_3;
            timerEstado = DURACION_VIENTO;
            vientoALaDerecha = true;
            return;
        }

        if (estadoActual == EstadoJuego.ETAPA_3 && timerEstado <= 0) {
            vientoALaDerecha = !vientoALaDerecha;
            timerEstado = DURACION_VIENTO;
            return;
        }

        if (estadoActual == EstadoJuego.TORMENTA_ESPECIAL && timerEstado <= 0) {
            estadoActual = EstadoJuego.PAUSA_POST_TORMENTA;
            timerEstado = DURACION_PAUSA_POST_TORMENTA;
            return;
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

    // ---------- Consultas para Lluvia ----------
    public EstadoJuego getEstadoActual() { return estadoActual; }

    public boolean estaEnPausa() {
        return estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_2
                || estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_3
                || estadoActual == EstadoJuego.PAUSA_POST_TORMENTA;
    }

    public boolean estaEnPausaDeTransicion() {
        return estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_2
                || estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_3;
    }

    public IComportamientoMovimiento getMovimientoParaGota() {
        Politica p = politicaGotas.get(estadoActual);
        return (p != null) ? p.get() : movRecto_Normal;
    }

    public IComportamientoMovimiento getMovimientoParaPowerup() {
        Politica p = politicaPowerups.get(estadoActual);
        return (p != null) ? p.get() : movRecto_Lento;
    }

    // ---------- Lógica de puntuación y vidas ----------
    public void sumarPuntos(int cantidad) { puntos += cantidad; }

    public void perderVida() { if (vidas > 0) vidas--; }

    public void sumarVida() { vidas++; }

    public void actualizarHighscore() {
        if (puntos > highscore) {
            highscore = puntos;
            Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
            prefs.putInteger(PREF_HIGHSCORE, highscore);
            prefs.flush();
        }
    }

    // ---------- Power-ups ----------
    public void activarEscudo(float duracion) { escudoActivo = true; timerEscudo = duracion; }
    public boolean isEscudoActivo() { return escudoActivo; }
    public void consumirEscudo() { escudoActivo = false; timerEscudo = 0f; }

    public void activarIman(float duracion) { imanActivo = true; timerIman = duracion; }
    public boolean isImanActivo() { return imanActivo; }

    // ---------- Tormenta especial ----------
    public void incrementarContadorTormenta() {
        if (estadoActual == EstadoJuego.TORMENTA_ESPECIAL
                || estadoActual == EstadoJuego.PAUSA_POST_TORMENTA) return;
        contadorTormenta++;
        if (contadorTormenta >= MAX_TORMENTA_CARGA) {
            contadorTormenta = 0;
            estadoPrevio = estadoActual;
            estadoActual = EstadoJuego.TORMENTA_ESPECIAL;
            timerEstado = DURACION_TORMENTA;
        }
    }

    // ---------- Getters UI ----------
    public int getVidas() { return vidas; }
    public int getPuntos() { return puntos; }
    public int getHighscore() { return highscore; }
    public int getContadorTormenta() { return contadorTormenta; }
}
