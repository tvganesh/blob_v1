package com.tvganesh.blob;

/* 
 * Developed by Tinniam V Ganesh, 17 Jan 2013
 * Uses Box2D physics engine and AndEngine
 * Based on http://gwtbox2d.appspot.com/ BlobJoint demo
 */


import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import android.hardware.SensorManager;
import android.util.Log;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;


public class Blob extends SimpleBaseGameActivity implements IAccelerationListener {
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	public static final float PIXEL_TO_METER_RATIO_DEFAULT = 32.0f;
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
	
	
	
	
   
    
    private Scene mScene;
    
    private PhysicsWorld mPhysicsWorld;
	private ITiledTextureRegion mCircleFaceTextureRegion;
	private TextureRegion mBrickTextureRegion;
	private TextureRegion mBrick1TextureRegion,mBrick2TextureRegion,mBrick3TextureRegion;
	
    
    private static FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(50f, 0.0f, 0.5f);
	public EngineOptions onCreateEngineOptions() {
		
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}
	
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");	
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 314, 332, TextureOptions.BILINEAR);		
		
		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_circle_tiled.png", 0, 0, 2, 1); // 64x32
		this.mBitmapTextureAtlas.load();		
		
		this.mBrickTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "brick.png",64, 32);
		this.mBitmapTextureAtlas.load();		
		
		this.mBrick1TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "brick1.png",164, 82);
		this.mBitmapTextureAtlas.load();	
		
		this.mBrick2TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "brick2.png",214, 132);
		this.mBitmapTextureAtlas.load();
		
		this.mBrick3TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "brick3.png",264, 282);
		this.mBitmapTextureAtlas.load();
	

	}
	
	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		
		// Create Blob scene
		this.initBlob(mScene);
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		return mScene;		
		
	}
	
	public void initBlob(Scene mScene){
		
		
		Sprite brick, brick1,brick2,brick3;
		Body brickBody,brick1Body,brick2Body,brick3Body;
		
		final Body circleBody[] = new Body[20];
		final Line connectionLine[] = new Line[20];
		final AnimatedSprite circle[] = new AnimatedSprite[20];
		
	
		
		//Create the floor,ceiling and walls
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.0f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		
		DistanceJointDef distanceJoint = new DistanceJointDef();
		float cx = 0.0f;
		float cy = 10.0f;
		float rx = 5.0f;
		float ry = 5.0f;
		final int nBodies = 20;
		float bodyRadius = 0.5f;
		final float PI=3.1415f;
		float centers[][] = new float[20][2];
		
		// Add 20 circle bodies around an ellipse
		for (int i=0; i<nBodies; ++i) {
			FIXTURE_DEF = PhysicsFactory.createFixtureDef(30f, 0.5f, 0.5f);
		    
			float lineWidth = 5.0f;
			
			//Ellipse : x= a cos (theta) y = b sin (theta)
			float angle = (2 * PI* i)/20;
			float x = cx + rx * (float)Math.sin(angle);
			float y = cy + ry * (float)Math.cos(angle);
			
			// Scale appropriately for screen size
			float x1 = (x + 10) * 30 - 100;
			float y1 = y * 20;
			centers[i][0] = x1;
			centers[i][1] = y1;
			
		     
			  Vector2 v1 = new Vector2(x1,y1);
			  final VertexBufferObjectManager vb = this.getVertexBufferObjectManager();
			  circle[i] = new AnimatedSprite(x1, y1, this.mCircleFaceTextureRegion, this.getVertexBufferObjectManager());
			  circleBody[i] = PhysicsFactory.createCircleBody(this.mPhysicsWorld, circle[i], BodyType.DynamicBody, FIXTURE_DEF);
			  
			  // Join adjacent bodies
			  if(i > 0) {
				     connectionLine[i] = new Line(centers[i][0],centers[i][1],centers[i-1][0],centers[i-1][1],lineWidth,this.getVertexBufferObjectManager());
				     connectionLine[i].setColor(0.0f,0.0f,1.0f);
				     this.mScene.attachChild(connectionLine[i]);	
				     
				     
			  }
			  
			  // Join the first body with the last body
			  if(i == 19){
				  connectionLine[0] = new Line(centers[0][0],centers[0][1],centers[19][0],centers[19][1],lineWidth,this.getVertexBufferObjectManager());
				  connectionLine[0].setColor(.0f,.0f,1.0f);
				  this.mScene.attachChild(connectionLine[0]);	
			  }
			 
			  // Update connection line so that the line moves along with the body
			  this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(circle[i], circleBody[i], true, true) {
				  @Override
					public void onUpdate(final float pSecondsElapsed) {
						super.onUpdate(pSecondsElapsed);
						for(int i=1;i < nBodies;i++) {
							connectionLine[i].setPosition(circle[i].getX(),circle[i].getY(),circle[i-1].getX(),circle[i-1].getY());
						
						}
						connectionLine[0].setPosition(circle[0].getX(),circle[0].getY(),circle[19].getX(),circle[19].getY());
			        }
			  }		  
					  
			);
			  
			  this.mScene.attachChild(circle[i]);  
			  
		}	
		//  Create a distanceJoint between every other day
		for(int i= 0;i < nBodies-1; i++)  {
			
		   for(int j=i+1; j< nBodies ; j++){
			   Vector2 v1 = new Vector2(centers[i][0]/PIXEL_TO_METER_RATIO_DEFAULT,centers[i][1]/PIXEL_TO_METER_RATIO_DEFAULT);
			   Vector2 v2 = new Vector2(centers[j][0]/PIXEL_TO_METER_RATIO_DEFAULT,centers[j][1]/PIXEL_TO_METER_RATIO_DEFAULT);
			   distanceJoint.initialize(circleBody[i], circleBody[(j)], v1, v2);		   
			   distanceJoint.collideConnected = true;
			   distanceJoint.dampingRatio = 1.0f;
			   distanceJoint.frequencyHz = 10.0f;
			   this.mPhysicsWorld.createJoint(distanceJoint);
			   
			   
		   }
		}
	/*	// Create a revoluteJoint between adjacent bodies - Lacks stiffness
		for( int i = 1; i < nBodies; i++ ) {
		  final RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
		  revoluteJointDef.initialize(circleBody[i], circleBody[i-1], circleBody[i].getWorldCenter());
		  revoluteJointDef.enableMotor = false;
		  revoluteJointDef.motorSpeed = 0;
		  revoluteJointDef.maxMotorTorque = 0;
		  this.mPhysicsWorld.createJoint(revoluteJointDef);
		}
		  // Create a revolute joint between first and last bodies
		  final RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
		  revoluteJointDef.initialize(circleBody[0], circleBody[19], circleBody[0].getWorldCenter());
		  revoluteJointDef.enableMotor = false;
		  revoluteJointDef.motorSpeed = 0;
		  revoluteJointDef.maxMotorTorque = 0;
		  this.mPhysicsWorld.createJoint(revoluteJointDef);*/
		
		// Create a weldJoint between adjacent bodies - Weld Joint has more stiffness
		for( int i = 1; i < nBodies; i++ ) {
			  final WeldJointDef weldJointDef = new WeldJointDef();
			  weldJointDef.initialize(circleBody[i], circleBody[i-1], circleBody[i].getWorldCenter());
			  this.mPhysicsWorld.createJoint(weldJointDef);
		}
		
	    // Create a weld joint between first and last bodies
		final WeldJointDef weldJointDef = new WeldJointDef();
		weldJointDef.initialize(circleBody[0], circleBody[19], circleBody[0].getWorldCenter());	  
	    this.mPhysicsWorld.createJoint(weldJointDef);
		
		  // Create a brick above the blob. Make it slightly heavy
		  FIXTURE_DEF = PhysicsFactory.createFixtureDef(50f, 0.5f, 0.5f);
		  brick = new Sprite(150,10, this.mBrickTextureRegion, this.getVertexBufferObjectManager());			
		  brickBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, brick, BodyType.DynamicBody, FIXTURE_DEF);	  
		  
		  this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(brick, brickBody, true, true));
		  this.mScene.attachChild(brick);	
		  
			
		  // Create a brick1 inside the blob. 
		  FIXTURE_DEF = PhysicsFactory.createFixtureDef(30f, 0.5f, 0.5f);
		  brick1 = new Sprite(120,140, this.mBrick1TextureRegion, this.getVertexBufferObjectManager());			
		  brick1Body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, brick1, BodyType.DynamicBody, FIXTURE_DEF);	    
		  this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(brick1, brick1Body, true, true));
		  this.mScene.attachChild(brick1);	
		  
		  // Create a brick2 inside the blob. 
		  FIXTURE_DEF = PhysicsFactory.createFixtureDef(30f, 0.5f, 0.5f);
		  brick2 = new Sprite(180,140, this.mBrick2TextureRegion, this.getVertexBufferObjectManager());			
		  brick2Body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, brick2, BodyType.DynamicBody, FIXTURE_DEF);	    
		  this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(brick2, brick2Body, true, true));
		  this.mScene.attachChild(brick2);	
		  
		  // Create a brick3 inside the blob. 
		  FIXTURE_DEF = PhysicsFactory.createFixtureDef(30f, 0.5f, 0.5f);
		  brick3 = new Sprite(260,170, this.mBrick3TextureRegion, this.getVertexBufferObjectManager());			
		  brick3Body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, brick3, BodyType.DynamicBody, FIXTURE_DEF);	    
		  this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(brick3, brick3Body, true, true));
		  this.mScene.attachChild(brick3);	
		  
		  
		
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
	}


	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
		
	}


	@Override
	public void onResumeGame() {
		super.onResumeGame();

		this.enableAccelerationSensor(this);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerationSensor();
	}
	
	

}
