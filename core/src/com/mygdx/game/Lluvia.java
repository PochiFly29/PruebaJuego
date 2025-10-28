package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Lluvia {

    // Lista de objetos polimórficos
    private Array<ObjetoQueCae> objetosEnPantalla;

    private long lastDropTime;

    // Almacén de assets para inyectar en los objetos
    private Texture texGotaBuena;
    private Texture texGotaMala;
    private Texture texVidaExtra;
    private Sound sndDrop;
    private Sound sndVida;
    private Music rainMusic;

    private float velocidadCaida = 300f;
    private float velocidadHorizontal = 100f;
    private float anguloRotacion = 25f;

    public Lluvia(Texture gotaBuena, Texture gotaMala, Texture vidaExtra, Sound dropSound, Sound lifeSound, Music mm) {
        this.rainMusic = mm;
        this.sndDrop = dropSound;
        this.sndVida = lifeSound;
        this.texGotaBuena = gotaBuena;
        this.texGotaMala = gotaMala;
        this.texVidaExtra = vidaExtra;
    }

    public void crear() {
        objetosEnPantalla = new Array<ObjetoQueCae>();
        crearObjetoQueCae();

        rainMusic.setLooping(true);
        rainMusic.play();
    }

    // Lógica del Spawner
    private void crearObjetoQueCae() {

        // --- 1. CÁLCULO DE SPAWN CORREGIDO ---

        // Constantes del juego
        float altoPantalla = 480;
        float anchoPantalla = 800;
        float anchoGota = 64; // Asumiendo hitbox.width = 64

        // Calculamos cuánto se moverá una gota horizontalmente en toda su caída
        float tiempoDeCaida = altoPantalla / velocidadCaida; // (ej: 480 / 300 = 1.6 seg)
        float derivaHorizontal = velocidadHorizontal * tiempoDeCaida; // (ej: 100 * 1.6 = 160 pixeles)

        // Calculamos el nuevo rango de spawn
        // El objetivo es que las gotas ATERRICEN entre x=0 y x=(800-64)

        // Para aterrizar en x=0, debe spawnear en (0 - deriva)
        float spawnMinX = 0 - derivaHorizontal;

        // Para aterrizar en x=(800-64), debe spawnear en ( (800-64) - deriva)
        float spawnMaxX = (anchoPantalla - anchoGota) - derivaHorizontal;

        // Creamos el hitbox con el nuevo rango X
        Rectangle hitbox = new Rectangle();
        hitbox.x = MathUtils.random(spawnMinX, spawnMaxX); // ¡Rango corregido!
        hitbox.y = 480; // (Spawn Y sin cambios)
        hitbox.width = anchoGota;
        hitbox.height = 64;


        // --- 2. LÓGICA DE CREACIÓN (con ángulo corregido) ---

        // Creamos la estrategia diagonal usando nuestras variables
        IComportamientoMovimiento movimiento = new MovimientoDiagonal(
                velocidadCaida,
                velocidadHorizontal,
                anguloRotacion // <-- ¡Ángulo positivo!
        );


        // Lógica de % de spawn (con tu balanceo de 1% de vida)
        float chance = MathUtils.random();

        if (chance < 0.70f) { // 70% Gota Buena
            objetosEnPantalla.add(new GotaBuena(texGotaBuena, hitbox, movimiento, sndDrop));
        } else if (chance < 0.99f) { // 29% Gota Mala
            objetosEnPantalla.add(new GotaMala(texGotaMala, hitbox, movimiento));
        } else { // 1% Vida Extra
            objetosEnPantalla.add(new VidaExtra(texVidaExtra, hitbox, movimiento, sndVida));
        }

        lastDropTime = TimeUtils.nanoTime();
    }

    // Actualiza todos los objetos en pantalla
    public void actualizarMovimiento(Tarro tarro) {
        // Generar nuevos objetos
        if (TimeUtils.nanoTime() - lastDropTime > 100000000)
            crearObjetoQueCae();

        // Iterar al revés para eliminación segura
        for (int i = objetosEnPantalla.size - 1; i >= 0; i--) {
            ObjetoQueCae objeto = objetosEnPantalla.get(i);

            // Llamar al Template Method
            objeto.update(Gdx.graphics.getDeltaTime(), tarro);

            // Limpiar objetos marcados
            if (objeto.marcadoParaEliminar) {
                objetosEnPantalla.removeIndex(i);
            }
        }
    }

    // Dibuja todos los objetos en pantalla
    public void actualizarDibujoLluvia(SpriteBatch batch) {
        for (ObjetoQueCae objeto : objetosEnPantalla) {
            objeto.dibujar(batch);
        }
    }

    public void destruir() {
        rainMusic.dispose();
    }

    public void pausar() {
        rainMusic.stop();
    }

    public void continuar() {
        rainMusic.play();
    }
}