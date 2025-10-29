package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {
    final GameLluviaMenu game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;
    private Tarro tarro;
    private Lluvia lluvia;

    // Assets
    private Texture texTarro;
    private Texture texGotaBuena;
    private Texture texGotaMala;
    private Texture texVidaExtra;
    private Texture texEscudo;
    private Texture texIman;
    private Texture texTormenta;
    private Texture texTrueno; // Asset del trueno

    private Sound sndHurt;
    private Sound sndDrop;
    private Sound sndVida;
    private Sound sndPowerup;
    private Sound sndTrueno; // Asset del trueno

    private Music rainMusic;

    public GameScreen(final GameLluviaMenu game) {
        this.game = game;
        this.batch = game.getBatch();
        this.font = game.getFont();

        // --- Carga de Assets ---
        texTarro = new Texture(Gdx.files.internal("bucket.png"));
        texGotaBuena = new Texture(Gdx.files.internal("drop.png"));
        texGotaMala = new Texture(Gdx.files.internal("dropBad.png"));
        texVidaExtra = new Texture(Gdx.files.internal("vidaExtra.png"));
        texEscudo = new Texture(Gdx.files.internal("shield.png"));
        texIman = new Texture(Gdx.files.internal("magnet.png"));
        texTormenta = new Texture(Gdx.files.internal("storm_pickup.png"));

        // ¡REVISA ESTAS DOS LÍNEAS!
        texTrueno = new Texture(Gdx.files.internal("trueno.png"));
        sndTrueno = Gdx.audio.newSound(Gdx.files.internal("trueno.wav"));

        sndHurt = Gdx.audio.newSound(Gdx.files.internal("hurt.ogg"));
        sndDrop = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        sndVida = Gdx.audio.newSound(Gdx.files.internal("life.wav"));
        sndPowerup = Gdx.audio.newSound(Gdx.files.internal("powerup.wav"));

        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        // --- Creación de Objetos ---
        tarro = new Tarro(texTarro, sndHurt);
        tarro.setEscudoTexture(texEscudo);

        lluvia = new Lluvia(texGotaBuena, texGotaMala, texVidaExtra,
                texEscudo, texIman, texTormenta,
                sndDrop, sndVida, sndPowerup, rainMusic);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        GameManager.getInstance().resetJuego();
        tarro.crear();
        lluvia.crear();
    }

    @Override
    public void render(float delta) {

        // Actualizar el "Cerebro"
        GameManager.getInstance().update(delta);

        // 1. Tocar el SONIDO del trueno (se dispara una vez)
        if (GameManager.getInstance().debeMostrarTrueno()) {
            sndTrueno.play();
        }

        ScreenUtils.clear(0, 0, 0.2f, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // --- DIBUJAR UI (NUEVO LAYOUT) ---

        // Fila Superior
        font.draw(batch, "HighScore : " + GameManager.getInstance().getHighscore(), 5, 475);
        font.draw(batch, "Tormenta: " + GameManager.getInstance().getContadorTormenta() + " / 3", 600, 475);

        // Fila Inferior (Y=40, justo encima del tarro en Y=20)
        font.draw(batch, "Gotas totales: " + GameManager.getInstance().getPuntos(), 5, 40);
        font.draw(batch, "Vidas : " + GameManager.getInstance().getVidas(), 670, 40);


        // --- Lógica del Juego ---
        if (!tarro.estaHerido()) {
            tarro.actualizarMovimiento();
        }
        lluvia.actualizarMovimiento(tarro);

        if (GameManager.getInstance().getVidas() <= 0) {
            GameManager.getInstance().actualizarHighscore();
            game.setScreen(new GameOverScreen(game));
            dispose();
        }

        // --- Dibujar Objetos del Juego ---
        tarro.dibujar(batch);
        lluvia.actualizarDibujoLluvia(batch);

        // 2. DIBUJAR LA IMAGEN del trueno (mientras dure la pausa)
        if (GameManager.getInstance().estaEnPausaDeTransicion()) {
            batch.draw(texTrueno,
                    camera.viewportWidth / 2 - texTrueno.getWidth() / 2,
                    camera.viewportHeight / 2 - texTrueno.getHeight() / 2);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {}
    @Override
    public void show() { lluvia.continuar(); }
    @Override
    public void hide() {}
    @Override
    public void pause() {
        lluvia.pausar();
        game.setScreen(new PausaScreen(game, this));
    }
    @Override
    public void resume() {}

    @Override
    public void dispose() {
        // Disponer de TODOS los assets
        texTarro.dispose();
        texGotaBuena.dispose();
        texGotaMala.dispose();
        texVidaExtra.dispose();
        texEscudo.dispose();
        texIman.dispose();
        texTormenta.dispose();
        texTrueno.dispose(); // ¡No olvidar!

        sndHurt.dispose();
        sndDrop.dispose();
        sndVida.dispose();
        sndPowerup.dispose();
        sndTrueno.dispose(); // ¡No olvidar!

        lluvia.destruir();
    }
}