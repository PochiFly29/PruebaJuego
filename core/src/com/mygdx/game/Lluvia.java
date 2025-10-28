package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Lluvia {

    private Array<Objeto> objetos;     // Lista de gotas (de cualquier tipo)
    private long lastDropTime;
    private Texture gotaBuena;
    private Texture gotaMala;
    private Sound dropSound;
    private Music rainMusic;

    public Lluvia(Texture gotaBuena, Texture gotaMala, Sound dropSound, Music rainMusic) {
        this.gotaBuena = gotaBuena;
        this.gotaMala = gotaMala;
        this.dropSound = dropSound;
        this.rainMusic = rainMusic;
    }

    /** Inicializa las estructuras y comienza la música. */
    public void crear() {
        objetos = new Array<>();
        crearGotaDeLluvia();

        rainMusic.setLooping(true);
        rainMusic.play();
    }

    /** Crea una nueva gota aleatoria (buena o mala) y la agrega a la lista. */
    private void crearGotaDeLluvia() {
        Objeto nuevaGota;

        // 50% de probabilidad de ser dañina
        if (MathUtils.random(1, 10) < 5) {
            nuevaGota = new GotaDaño(gotaMala);
        } else {
            nuevaGota = new GotaPuntos(gotaBuena, dropSound);
        }

        nuevaGota.getArea().x = MathUtils.random(0, 800 - 64);
        nuevaGota.getArea().y = 480;

        objetos.add(nuevaGota);
        lastDropTime = TimeUtils.nanoTime();
    }

    /**
     * Actualiza la posición y estado de todas las gotas.
     * Retorna false si el jugador se queda sin vidas (Game Over).
     */
    public boolean actualizarMovimiento(Tarro tarro) {
        if (TimeUtils.nanoTime() - lastDropTime > 100000000)
            crearGotaDeLluvia();

        float delta = Gdx.graphics.getDeltaTime();

        for (int i = 0; i < objetos.size; i++) {
            Objeto obj = objetos.get(i);
            boolean sigueActivo = obj.actualizarYVerificar(tarro, delta);

            // Si la gota ya no está activa (salió o colisionó), se elimina
            if (!sigueActivo) {
                objetos.removeIndex(i);
                i--; // evitar saltarse el siguiente elemento

                // Si el jugador perdió todas las vidas, termina el juego
                if (tarro.getVidas() <= 0)
                    return false;
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
        dropSound.dispose();
        rainMusic.dispose();
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
