package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {
    final GameLluviaMenu game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;
    private Tarro tarro;
    private Lluvia lluvia;

    public GameScreen(final GameLluviaMenu game) {
        this.game = game;
        this.batch = game.getBatch(); // usa el batch del Game

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = 28;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        this.font = gen.generateFont(p);
        gen.dispose();

        // ---- Texturas con filtro Linear ----
        Sound hurtSound = Gdx.audio.newSound(Gdx.files.internal("hurt.ogg"));

        Texture bucketTexture = new Texture(Gdx.files.internal("bucket.png"));
        bucketTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Texture gota = new Texture(Gdx.files.internal("drop.png"));
        gota.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Texture gotaMala = new Texture(Gdx.files.internal("dropBad.png"));
        gotaMala.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        tarro = new Tarro(bucketTexture, hurtSound);

        Sound dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        Music rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        Music windMusic = Gdx.audio.newMusic(Gdx.files.internal("wind.mp3"));

        lluvia = new Lluvia(gota, gotaMala, dropSound, rainMusic, windMusic);

        // ---- Cámara ----
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        tarro.crear();
        lluvia.crear();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        if ((Gdx.input.isKeyJustPressed(Input.Keys.R))){
            game.setScreen(new GameScreen(game));
            dispose();
            return;
        } else if ((Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))){
            pause();
        }
        batch.begin();
        font.draw(batch, "Gotas totales: " + tarro.getPuntos(), 5, 475);
        font.draw(batch, "Vidas : " + tarro.getVidas(), 670, 475);
        font.draw(batch, "HighScore : " + game.getHigherScore(), camera.viewportWidth/2 - 50, 475);

        if (!tarro.estaHerido()) {
            tarro.actualizarMovimiento();
            if (!lluvia.actualizarMovimiento(tarro)) {
                if (game.getHigherScore() < tarro.getPuntos())
                    game.setHigherScore(tarro.getPuntos());
                game.setScreen(new GameOverScreen(game));
                dispose();
            }
        }

        tarro.dibujar(batch);
        lluvia.actualizarDibujoLluvia(batch);
        batch.end();
    }

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
	  // continuar con sonido de lluvia
	  lluvia.continuar();
	}

	@Override
	public void hide() {

	}

	@Override
	public void pause() {
		lluvia.pausar();
		game.setScreen(new PausaScreen(game, this)); 
	}

	@Override
	public void resume() {

	}

    @Override
    public void dispose() {
        if (font != null) font.dispose();

        tarro.destruir();
        lluvia.destruir();
    }
}
