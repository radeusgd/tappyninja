package com.radeusgd.tappyninja;

import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

class Item{
	public static final float radius = 20f;
	Item(Vector2 pos, Vector2 velocity){
		this.pos = pos;
		this.velocity = velocity;
	}
	Vector2 pos, velocity;
	public boolean clicked(Vector2 click){
		return false;
	}
	public void update(float dt){
		velocity.y-=dt*100f;
		velocity.x-=Math.signum(velocity.x)*dt*30f;
		pos.mulAdd(velocity, dt);
	}
	public boolean outOfScreen(){
		return false;
	}
	public final Vector2 getPos(){
		return pos;
	}
}

public class Game extends ApplicationAdapter implements InputProcessor{
	ShapeRenderer shapes;
	SpriteBatch scoreBatch;
	Array<Item> items = new Array<Item>();
	int lives = 0;
	int score = 0;
	BitmapFont font;
	FreeTypeFontGenerator fontGen;
	
	@Override
	public void create () {
		Gdx.input.setInputProcessor(this);
		shapes = new ShapeRenderer();
		scoreBatch = new SpriteBatch();
		fontGen = new FreeTypeFontGenerator(Gdx.files.internal("grotesque.ttf"));
		FreeTypeFontParameter params = new FreeTypeFontParameter();
		params.size = 20;
		params.characters = "0123456789 TaptolyScre:.+-";
		font = fontGen.generateFont(params);
	}
	
	Random random = new Random();
	
	private void addItem(){
		float pos = random.nextFloat()*Gdx.graphics.getWidth();
		Item item = new Item(new Vector2(pos, 0f), new Vector2((Gdx.graphics.getWidth()*0.5f-pos)*0.3f, 300f));
		//TODO color
		items.add(item);
	}
	
	float genCounter = 0;
	float genSpeed = 1.2f;
	
	private double currentTime;
	
	@Override
	public void render () {
		double newTime = TimeUtils.millis() / 1000.0;
        double frameTime = Math.min(newTime - currentTime, 0.25);
        float deltaTime = (float)frameTime;
        currentTime = newTime;
        
		Gdx.gl.glClearColor(0.2f, 0.5f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if(lives>0){//game mode
			genSpeed -= deltaTime * 0.01f;
			genSpeed = Math.max(0.07f, genSpeed);
			if(genCounter<=0f){
				genCounter = genSpeed;
				addItem();
			}else{
				genCounter-=deltaTime;
			}
			shapes.begin(ShapeType.Filled);
			for(Item i : items){
				i.update(deltaTime);
				shapes.setColor(Color.GREEN);
				shapes.circle(i.getPos().x, i.getPos().y, Item.radius);
				//shapes.circle(i.getPos().x, i.getPos()., Item.radius);
			}
			shapes.end();
		}
		scoreBatch.begin();
		font.draw(scoreBatch, "Score: "+Integer.toString(score), 30, 30);
		if(lives<=0){
			font.draw(scoreBatch, "Tap to play", Gdx.graphics.getWidth()*0.5f, Gdx.graphics.getHeight()*0.5f);
		}
		scoreBatch.end();
	}
	
	@Override
	public void dispose(){
		scoreBatch.dispose();
		shapes.dispose();
		font.dispose();
		fontGen.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(lives<=0){
			lives=4;//restart game
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
