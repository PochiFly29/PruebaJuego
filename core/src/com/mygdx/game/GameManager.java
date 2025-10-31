package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.mygdx.game.imovimiento.IMovimientos;
import com.mygdx.game.imovimiento.MovimientoDiagonal;
import com.mygdx.game.imovimiento.MovimientoSerpenteante;
import com.mygdx.game.imovimiento.MovimientoVertical;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class GameManager {

    private static GameManager instancia;
    public static GameManager getInstance() {
        if (instancia == null) instancia = new GameManager();
        return instancia;
    }

    public static class AudioBus {
        private final Map<String, Music> musics = new HashMap<String, Music>();
        private final Map<String, Integer> refs = new HashMap<String, Integer>();

        public void register(String name, Music music) {
            musics.put(name, music);
            refs.put(name, 0);
        }

        public synchronized void playRef(String name, boolean loop) {
            Music m = musics.get(name); if (m == null) return;
            Integer r = refs.get(name); if (r == null) r = 0;
            if (r == 0) { m.setLooping(loop); m.play(); }
            refs.put(name, r + 1);
        }
        public synchronized void stopRef(String name) {
            Music m = musics.get(name); if (m == null) return;
            Integer r = refs.get(name); if (r == null) r = 0;
            if (r > 0) {
                r = r - 1; refs.put(name, r);
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
    public AudioBus getAudioBus() {
        return audioBus;
    }

    private final AudioBus audioBus = new AudioBus();
    private boolean windOn = false;
    public void setWindMusic(Music wind) { audioBus.register("wind", wind); }
    public void startWind() { audioBus.playRef("wind", true); windOn = true; }
    public void stopWind()  { audioBus.stopRef("wind"); windOn = false; }
    private void syncWindWithState() {
        boolean debeSonar = (estadoActual == EstadoJuego.ETAPA_2 || estadoActual == EstadoJuego.ETAPA_3);
        if (debeSonar && !windOn) startWind();
        if (!debeSonar && windOn) stopWind();
    }

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

    private static final int SCORE_PARA_ETAPA_2 = 1000;
    private static final int SCORE_PARA_ETAPA_3 = 2500;
    private static final float DURACION_PAUSA = 2.0f;
    private static final float DURACION_VIENTO = 5.0f;

    private boolean escudoActivo = false;
    private float timerEscudo = 0f;
    private boolean imanActivo = false;
    private float timerIman = 0f;

    private int contadorTormenta = 0;
    private static final int MAX_TORMENTA_CARGA = 3;
    private static final float DURACION_TORMENTA = 5.0f;
    private static final float DURACION_PAUSA_POST_TORMENTA = 1.0f;

    private boolean vientoALaDerecha = true;

    private final IMovimientos movRecto_Lento = new MovimientoVertical(200f);
    private final IMovimientos movRecto_Normal = new MovimientoVertical(300f);
    private final IMovimientos movRecto_Rapido = new MovimientoVertical(450f);
    private final IMovimientos movDiag_Derecha = new MovimientoDiagonal(400f,  150f,  25f);
    private final IMovimientos movDiag_Izquierda = new MovimientoDiagonal(400f, -150f, -25f);
    private final IMovimientos movSerpiente = new MovimientoSerpenteante(300f, 50f, 3f);
    private final IMovimientos movTormenta  = new MovimientoVertical(700f);

    private interface Politica { IMovimientos get(); }
    private final Map<EstadoJuego, Politica> politicaGotas = new EnumMap<EstadoJuego, Politica>(EstadoJuego.class);
    private final Map<EstadoJuego, Politica> politicaPowerups = new EnumMap<EstadoJuego, Politica>(EstadoJuego.class);

    private static final String PREFS_NAME = "GameLluviaPrefs";
    private static final String PREF_HIGHSCORE = "highscore";

    private IMovimientos movGotaPrevioATormenta;

    private GameManager() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        this.highscore = prefs.getInteger(PREF_HIGHSCORE, 0);
        configurarPoliticas();
        resetJuego();
    }

    private void addPolitica(Map<EstadoJuego, Politica> mapa, EstadoJuego estado, final IMovimientos mov) {
        mapa.put(estado, new Politica() {
            public IMovimientos get() {
                return mov;
            }
        });
    }
    private void configurarPoliticas() {
        addPolitica(politicaGotas, EstadoJuego.ETAPA_1, movRecto_Normal);
        addPolitica(politicaGotas, EstadoJuego.PAUSA_PARA_ETAPA_2, movRecto_Normal);
        addPolitica(politicaGotas, EstadoJuego.ETAPA_2, movDiag_Derecha);
        addPolitica(politicaGotas, EstadoJuego.PAUSA_PARA_ETAPA_3, movDiag_Derecha);
        politicaGotas.put(EstadoJuego.ETAPA_3, new Politica() {
            public IMovimientos get() {
                return (vientoALaDerecha ? movDiag_Derecha : movDiag_Izquierda);
            }
        });
        addPolitica(politicaGotas, EstadoJuego.TORMENTA_ESPECIAL,   movTormenta);
        addPolitica(politicaGotas, EstadoJuego.PAUSA_POST_TORMENTA, movRecto_Normal);

        addPolitica(politicaPowerups, EstadoJuego.ETAPA_1, movRecto_Lento);
        addPolitica(politicaPowerups, EstadoJuego.PAUSA_PARA_ETAPA_2, movRecto_Lento);
        addPolitica(politicaPowerups, EstadoJuego.ETAPA_2, movRecto_Rapido);
        addPolitica(politicaPowerups, EstadoJuego.PAUSA_PARA_ETAPA_3, movRecto_Rapido);
        addPolitica(politicaPowerups, EstadoJuego.ETAPA_3, movSerpiente);
        addPolitica(politicaPowerups, EstadoJuego.TORMENTA_ESPECIAL, movRecto_Lento);
        addPolitica(politicaPowerups, EstadoJuego.PAUSA_POST_TORMENTA, movRecto_Lento);
    }

    private static boolean esPausa(EstadoJuego e) {
        return e == EstadoJuego.PAUSA_PARA_ETAPA_2 || e == EstadoJuego.PAUSA_PARA_ETAPA_3 || e == EstadoJuego.PAUSA_POST_TORMENTA;
    }
    private static boolean esTransicionConTrueno(EstadoJuego e) {
        return e == EstadoJuego.PAUSA_PARA_ETAPA_2 || e == EstadoJuego.PAUSA_PARA_ETAPA_3;
    }

    public void resetJuego() {
        vidas = VIDAS_INICIALES;
        puntos = 0;
        estadoActual = EstadoJuego.ETAPA_1;
        timerEstado = 0f;
        contadorTormenta = 0;
        escudoActivo = false; timerEscudo = 0f;
        imanActivo  = false; timerIman  = 0f;
        vientoALaDerecha = true;
        audioBus.stopAll(); windOn = false;
        movGotaPrevioATormenta = null;
    }

    public void update(float delta) {
        if (timerEstado > 0) timerEstado -= delta;

        if (escudoActivo) { timerEscudo -= delta; if (timerEscudo <= 0) escudoActivo = false; }
        if (imanActivo) { timerIman -= delta; if (timerIman <= 0) imanActivo = false; }

        if (estadoActual == EstadoJuego.ETAPA_1 && puntos >= SCORE_PARA_ETAPA_2) {
            toState(EstadoJuego.PAUSA_PARA_ETAPA_2, DURACION_PAUSA, true); return;
        }
        if (estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_2 && timerEstado <= 0f) {
            toState(EstadoJuego.ETAPA_2, 0f, false); return;
        }
        if (estadoActual == EstadoJuego.ETAPA_2 && puntos >= SCORE_PARA_ETAPA_3) {
            toState(EstadoJuego.PAUSA_PARA_ETAPA_3, DURACION_PAUSA, true); return;
        }
        if (estadoActual == EstadoJuego.PAUSA_PARA_ETAPA_3 && timerEstado <= 0f) {
            toState(EstadoJuego.ETAPA_3, DURACION_VIENTO, false);
            vientoALaDerecha = true; return;
        }
        if (estadoActual == EstadoJuego.ETAPA_3 && timerEstado <= 0f) {
            vientoALaDerecha = !vientoALaDerecha;
            timerEstado = DURACION_VIENTO;
        }
        if (estadoActual == EstadoJuego.TORMENTA_ESPECIAL && timerEstado <= 0f) {
            toState(EstadoJuego.PAUSA_POST_TORMENTA, DURACION_PAUSA_POST_TORMENTA, false); return;
        }
        if (estadoActual == EstadoJuego.PAUSA_POST_TORMENTA && timerEstado <= 0f) {
            estadoActual = estadoPrevio;
            if (estadoActual == EstadoJuego.ETAPA_3) timerEstado = DURACION_VIENTO;
        }
        syncWindWithState();
    }

    private void toState(EstadoJuego nuevo, float duracion, boolean trueno) {
        estadoActual = nuevo;
        timerEstado = duracion;
        if (trueno) mostrarEfectoTrueno = true;
        if (nuevo == EstadoJuego.TORMENTA_ESPECIAL) stopWind();
        syncWindWithState();
    }

    public boolean debeMostrarTrueno() {
        if (mostrarEfectoTrueno) { mostrarEfectoTrueno = false; return true; }
        return false;
    }

    public EstadoJuego getEstadoActual() { return estadoActual; }
    public boolean estaEnPausa() { return esPausa(estadoActual); }
    public boolean estaEnPausaDeTransicion() { return esTransicionConTrueno(estadoActual); }

    public IMovimientos getMovimientoParaGota() {
        Politica p = politicaGotas.get(estadoActual);
        return (p != null) ? p.get() : movRecto_Normal;
    }
    public IMovimientos getMovimientoParaPowerup() {
        Politica p = politicaPowerups.get(estadoActual);
        return (p != null) ? p.get() : movRecto_Lento;
    }

    public IMovimientos getMovimientoGotaPrevioATormenta() {
        return (movGotaPrevioATormenta != null) ? movGotaPrevioATormenta : getMovimientoParaGota();
    }

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

    public void activarEscudo(float duracion) { escudoActivo = true; timerEscudo = duracion; }
    public boolean isEscudoActivo() { return escudoActivo; }
    public void consumirEscudo() { escudoActivo = false; timerEscudo = 0f; }
    public void activarIman(float duracion) { imanActivo = true; timerIman = duracion; }
    public boolean isImanActivo() { return imanActivo; }

    public void incrementarContadorTormenta() {
        if (estadoActual == EstadoJuego.TORMENTA_ESPECIAL || estadoActual == EstadoJuego.PAUSA_POST_TORMENTA) return;
        contadorTormenta++;
        if (contadorTormenta >= MAX_TORMENTA_CARGA) {
            contadorTormenta = 0;
            estadoPrevio = estadoActual;
            movGotaPrevioATormenta = getMovimientoParaGota();
            estadoActual = EstadoJuego.TORMENTA_ESPECIAL;
            timerEstado = DURACION_TORMENTA;
            stopWind();
        }
    }

    public int getVidas() { return vidas; }
    public int getPuntos() { return puntos; }
    public int getHighscore() { return highscore; }
    public int getContadorTormenta() { return contadorTormenta; }
}
