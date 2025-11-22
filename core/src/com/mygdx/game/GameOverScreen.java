package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameOverScreen implements Screen {

    private final GameLluviaMenu game;
    private SpriteBatch batch;
    private OrthographicCamera camera;

    private Texture gameOverTexture;

    public GameOverScreen(final GameLluviaMenu game) {
        this.game = game;
        this.batch = game.getBatch();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // imagen de Game Over
        gameOverTexture = new Texture(Gdx.files.internal("gameOverScreen.png"));
        gameOverTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.draw(gameOverTexture, 0, 0, 800, 480);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.justTouched()) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (gameOverTexture != null) {
            gameOverTexture.dispose();
        }
    }
}
