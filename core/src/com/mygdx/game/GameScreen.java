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
    private Sound sndHurt;
    private Sound sndDrop;
    private Sound sndVida;
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

        sndHurt = Gdx.audio.newSound(Gdx.files.internal("hurt.ogg"));
        sndDrop = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        sndVida = Gdx.audio.newSound(Gdx.files.internal("life.wav"));

        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        // --- Creación de Objetos ---
        tarro = new Tarro(texTarro, sndHurt);

        // Inyectar assets en el controlador de Lluvia
        lluvia = new Lluvia(texGotaBuena, texGotaMala, texVidaExtra, sndDrop, sndVida, rainMusic);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // Resetear el estado del juego
        GameManager.getInstance().resetJuego();

        tarro.crear();
        lluvia.crear();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // --- Dibujar UI (Leyendo desde GameManager) ---
        font.draw(batch, "Gotas totales: " + GameManager.getInstance().getPuntos(), 5, 475);
        font.draw(batch, "Vidas : " + GameManager.getInstance().getVidas(), 670, 475);
        font.draw(batch, "HighScore : " + GameManager.getInstance().getHighscore(), camera.viewportWidth / 2 - 50, 475);

        if (!tarro.estaHerido()) {
            tarro.actualizarMovimiento();
        }

        // Actualizar la lógica de la lluvia
        lluvia.actualizarMovimiento(tarro);

        // --- Chequeo de Game Over (Leyendo desde GameManager) ---
        if (GameManager.getInstance().getVidas() <= 0) {
            // Actualizar HighScore
            GameManager.getInstance().actualizarHighscore();

            // Ir a la ventana de fin de juego
            game.setScreen(new GameOverScreen(game));
            dispose();
        }

        // Dibujar los elementos del juego
        tarro.dibujar(batch);
        lluvia.actualizarDibujoLluvia(batch);

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
        // Disponer de TODOS los assets cargados en esta pantalla
        texTarro.dispose();
        texGotaBuena.dispose();
        texGotaMala.dispose();
        texVidaExtra.dispose();
        sndHurt.dispose();
        sndDrop.dispose();
        sndVida.dispose();
        lluvia.destruir(); // Dispone de rainMusic
    }
}