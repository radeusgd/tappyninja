package com.radeusgd.tappyninja;

import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
//import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.radeusgd.tappyninja.Item.Type;

class Item{
	public static Random rand = new Random();
	public static float radius = 28f;
	public enum Type{
		NORMAL, SUPER, BOMB, LIFE
	}
	Type type;
	Item(Vector2 pos, Vector2 velocity){
		this.pos = pos;
		this.velocity = velocity;
		type = Type.NORMAL;
	}
	Vector2 pos, velocity;
	public float rnd = rand.nextFloat();
	public boolean living = true;
	public boolean clicked(Vector2 click){
		/*System.out.print(pos);
		System.out.print(" ");
		System.out.println(click);*/
		if(pos.dst2(click)<=radius*radius+1f) return true;
		return false;
	}
	public void update(float dt){
		velocity.y-=dt*(Gdx.graphics.getHeight()*100f/500f);
		velocity.x-=Math.signum(velocity.x)*dt*10f;
		pos.mulAdd(velocity, dt);
	}
	public boolean outOfScreen(){
		return (pos.y<-2f*radius);
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
		Item.radius = Math.max(Gdx.graphics.getHeight(),Gdx.graphics.getWidth())/(2f*14f);
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
		Item item = new Item(new Vector2(pos, -Item.radius), new Vector2((Gdx.graphics.getWidth()*0.5f-pos)*0.3f+0.15f*Gdx.graphics.getWidth()*(random.nextFloat()-0.5f), Gdx.graphics.getHeight()*(230f+random.nextFloat()*70f)/500f));
		float type = random.nextFloat();
		if(type>0.98){//-rndModifier){
			item.type = Type.LIFE;
		}
		else if(type>0.88){//-rndModifier){
			item.type = Type.SUPER;
		}else if(type>0.78){
			item.type = Type.BOMB;
		}
		items.add(item);
	}
	
	float rndModifier = 0f;
	float genCounter = 0;
	private final float startGenSpeed = 1.1f;
	float genSpeed = startGenSpeed;
	
	private double currentTime;
	private double gameOverTime=-1.f;
	
	@Override
	public void render () {
		double newTime = TimeUtils.millis() / 1000.0;
        double frameTime = Math.min(newTime - currentTime, 0.3);
        float deltaTime = (float)frameTime;
        currentTime = newTime;
        if(lives<=0 && gameOverTime==0f){
        	gameOverTime = currentTime;
        }
		Gdx.gl.glClearColor(0.2f, 0.5f, 1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if(lives>0){//game mode
			genSpeed -= deltaTime * (0.8f/60f);
			genSpeed = Math.max(0.37f, genSpeed);
			//if(genSpeed<0.5f) rndModifier = 0.02f;
			if(genCounter<=0f){
				genCounter = genSpeed;
				addItem();
			}else{
				genCounter-=deltaTime;
			}
			Gdx.gl.glEnable(GL20.GL_BLEND);
		    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			shapes.begin(ShapeType.Filled);
			for(int i=0;i<lives;i++){
				shapes.setColor(Color.RED);
				shapes.circle(i*30f+20f, Gdx.graphics.getHeight()-20f, 15f);
			}
			for(Item i : items){
				i.update(deltaTime);
				float alpha = (i.living) ? 0.9f : 0.3f;
				switch(i.type){
				case NORMAL:
					shapes.setColor(new Color(0f, 0.8f+0.2f*i.rnd, 0f, alpha));
					break;
				case SUPER:
					shapes.setColor(new Color(0f, 0.3f-0.3f*i.rnd, 0.8f+0.2f*i.rnd, alpha));
					break;
				case BOMB:
					shapes.setColor(new Color(0.15f*i.rnd, 0.15f*i.rnd, 0.15f*i.rnd, alpha));
					break;
				case LIFE:
					shapes.setColor(new Color(0.8f+0.2f*i.rnd, 0f, 0f, alpha));
					break;
				}
				shapes.circle(i.getPos().x, i.getPos().y, Item.radius);
				//shapes.circle(i.getPos().x, i.getPos()., Item.radius);
			}
			shapes.end();
			Gdx.gl.glDisable(GL20.GL_BLEND);
			//remove out of screen
			boolean markLives = false;
			Array<Item> toRemove = new Array<Item>();
			for(Item i : items){
				if(i.outOfScreen()){
					toRemove.add(i);
					if(i.living && i.type==Type.NORMAL){
						lives--;
						markLives=true;
						break;
					}
				}
			}
			if(markLives){
				for(Item i : items){
					i.living = false;
				}
			}
			for(Item i : toRemove){
				items.removeValue(i, true);
			}
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
		if(lives<=0 && currentTime - gameOverTime > 2f){//menu mode
			gameOverTime = 0f;
			score=0;
			lives=4;//restart game
			genSpeed = startGenSpeed;
			items.clear();
		}else{//game mode
			Array<Item> toRemove = new Array<Item>();
			Vector2 pos = new Vector2(screenX,Gdx.graphics.getHeight()-screenY);
			int plusScore = 0;
			for(Item i : items){
				if(i.clicked(pos)){
					toRemove.add(i);
					switch(i.type){
					case NORMAL:
						plusScore++;
						break;
					case SUPER:
						plusScore+=2;
						score++;
						break;
					case BOMB:
						lives--;
						break;
					case LIFE:
						lives++;
						break;
					}
				}
			}
			score += plusScore*plusScore;
			for(Item i : toRemove){
				items.removeValue(i, true);
			}
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
