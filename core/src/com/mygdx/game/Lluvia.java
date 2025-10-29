package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.iescenario.EscenarioNormal;
import com.mygdx.game.iescenario.EscenarioStrat;
import com.mygdx.game.iescenario.EscenarioTorrencial;
import com.mygdx.game.imovimiento.CaidaDiagonal;
import com.mygdx.game.imovimiento.CaidaVertical;
import com.mygdx.game.imovimiento.MovimientoStrat;

public class Lluvia {

    private Array<Objeto> objetos;     // Lista de gotas (de cualquier tipo)
    private Texture gotaBuena;
    private Texture gotaMala;
    private Sound dropSound;
    private Music rainMusic;
    private Music windMusic;
    private boolean soloBuenas = false;
    private int prevPuntos = 0;

    private EscenarioStrat escenario;
    private MovimientoStrat movimientoEscenarioActual; // cuál usar ahora

    public void setSoloBuenas(boolean soloBuenas) { this.soloBuenas = soloBuenas; }
    public Lluvia(Texture gotaBuena, Texture gotaMala, Sound dropSound, Music rainMusic, Music windMusic) {
        this.gotaBuena = gotaBuena;
        this.gotaMala = gotaMala;
        this.dropSound = dropSound;
        this.rainMusic = rainMusic;
        this.windMusic = windMusic;
    }

    /** Inicializa las estructuras y comienza la música. */
    public void crear() {
        objetos = new Array<>();
        movimientoEscenarioActual = new CaidaVertical();
        movimientoEscenarioActual.onStart();
        escenario = new EscenarioNormal();
        escenario.init(this);

        rainMusic.setLooping(true);
        rainMusic.play();
        if (windMusic != null) windMusic.setLooping(true);
    }

    public void cambiarEscenarioNormal() {
        escenario = new EscenarioNormal();
        escenario.init(this);
        setSoloBuenas(false);
    }

    /** Crea una nueva gota aleatoria (buena o mala) y la agrega a la lista. */
    public void crearGotaDeLluvia() {

        Objeto nuevaGota;
        if (soloBuenas) {
            nuevaGota = new GotaPuntos(gotaBuena, dropSound);
        } else {
            if (MathUtils.random(1, 10) < 5) nuevaGota = new GotaDaño(gotaMala);
            else nuevaGota = new GotaPuntos(gotaBuena, dropSound);
        }
        nuevaGota.getArea().x = MathUtils.random(0, 800 - 64);
        nuevaGota.getArea().y = 480;

        nuevaGota.setMovimiento(movimientoEscenarioActual.crearNueva());

        objetos.add(nuevaGota);
    }

    /**
     * Actualiza la posición y estado de todas las gotas.
     * Retorna false si el jugador se queda sin vidas (Game Over).
     */
    public boolean actualizarMovimiento(Tarro tarro) {
        float delta = Gdx.graphics.getDeltaTime();
        int p = tarro.getPuntos();

        if (prevPuntos < 200 && p >= 200 && !(escenario instanceof EscenarioTorrencial)) {
            escenario = new EscenarioTorrencial();
            escenario.init(this);

            // detener la estrategia anterior
            movimientoEscenarioActual.onStop();

            // activar diagonal con viento
            movimientoEscenarioActual = new CaidaDiagonal(500f, +130f, windMusic);
            movimientoEscenarioActual.onStart();
        }

        prevPuntos = p;

        escenario.update(this, tarro, delta);  // delega el spawn al escenario

        for (int i = 0; i < objetos.size; i++) {
            Objeto obj = objetos.get(i);
            boolean sigueActivo = obj.actualizarYVerificar(tarro, delta);
            if (!sigueActivo) {
                objetos.removeIndex(i);
                i--;
                if (tarro.getVidas() <= 0) return false;
            }
        }
        return true;
    }

    /** Dibuja todas las gotas activas. */
    public void actualizarDibujoLluvia(SpriteBatch batch) {
        for (Objeto obj : objetos) {
            obj.dibujar(batch);
        }
    }

    /** Libera recursos. */
    public void destruir() {
        if (movimientoEscenarioActual != null) movimientoEscenarioActual.onStop();
        dropSound.dispose();
        rainMusic.dispose();
        if (windMusic != null) windMusic.dispose();
    }


    /** Pausa la música de lluvia. */
    public void pausar() {
        rainMusic.stop();
    }

    /** Reanuda la música de lluvia. */
    public void continuar() {
        rainMusic.play();
    }
}
