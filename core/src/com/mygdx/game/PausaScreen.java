package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PausaScreen {

    private final Texture overlayVHS;
    private final Sound sndPause;
    private final Sound sndResume;

    private final BitmapFont fontTitulo;
    private final BitmapFont fontSubtitulo;

    private boolean activo = false;

    public PausaScreen(BitmapFont fontTitulo, BitmapFont fontSubtitulo) {
        this.fontTitulo = fontTitulo;
        this.fontSubtitulo = fontSubtitulo;

        overlayVHS = new Texture(Gdx.files.internal("MenuPausa.png"));
        sndPause   = Gdx.audio.newSound(Gdx.files.internal("pause.mp3"));
        sndResume  = Gdx.audio.newSound(Gdx.files.internal("resume.mp3"));
    }

    public void toggle() {
        activo = !activo;
        if (activo) sndPause.play();
        else sndResume.play();
    }

    public boolean isActivo() {
        return activo;
    }

    public void render(SpriteBatch batch, OrthographicCamera camera) {
        if (!activo) return;

        float w = camera.viewportWidth;
        float h = camera.viewportHeight;

        batch.setColor(1f, 1f, 1f, 0.65f);
        batch.draw(overlayVHS, 0, 0, w, h);
        batch.setColor(1f, 1f, 1f, 1f);

        fontTitulo.draw(batch, "PAUSA", w * 0.67f, h * 0.89f);
        fontSubtitulo.draw(batch, "[ESC] PARA CONTINUAR", w * 0.66f, h * 0.76f);
    }

    public void dispose() {
        overlayVHS.dispose();
        sndPause.dispose();
        sndResume.dispose();
    }
}
