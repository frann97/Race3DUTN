/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import animations.particleAnimations;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import sounds.Audio3D;
import statics.Constant;
import userInterface.GUI;

public class GRAPHICS extends AbstractAppState implements ActionListener, PhysicsCollisionListener  {
    
    //game variables
    private final Node rootNode;
    private BulletAppState bulletAppState;
    private final AssetManager assetManager;
    private final InputManager inputManager;    
    private final Node localRootNode = new Node("Level 1");
    
    //player variables
    private final Node vehicleNode = new Node("vehicleNode");
    private double endurance = Constant.MAX_LIFE;
    private VehicleControl vehicle;
    private final float accelerationForce = 500.0f;
    private final float deaccelerationForce = 100.0f;
    private final float brakeForce = 100.0f;
    private float steeringValue = 0;
    private float accelerationValue = 0;
    private float deaccelerationValue = 0;
    final private Vector3f jumpForce = new Vector3f(0, 3000, 0);
    
    //GUI VARIABLES
    private final GUI GUInterface;
    
    //Particle variables
    private final particleAnimations pAnimations;
    
    //camera variables
    private final FlyByCamera flyByCamera;
    private final Camera camera;
    private ChaseCamera chaseCam;
    
    //audio variables
    private final Audio3D audio;
    
    // terrain variables
    private TerrainQuad terrain;
    private Material matRock;
    private Material matWire;
    private final boolean wireframe = false;
    private final boolean triPlanar = false;
    private final float grassScale = 64;
    private final float dirtScale = 16;
    private final float rockScale = 128;
    Material mat_terrain;
    
    public GRAPHICS(SimpleApplication app) {
        rootNode = app.getRootNode();
        assetManager = app.getAssetManager();
        inputManager = app.getInputManager();
        flyByCamera = app.getFlyByCamera();
        GUInterface = new GUI(app.getGuiNode(), assetManager);
        pAnimations = new particleAnimations(assetManager);
        audio = new Audio3D(rootNode, assetManager);
        camera = app.getCamera();
    }

    
    // ---------------------------------- ENGINE ---------------------------------------------
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.setDebugEnabled(false);
        
        //i make myself as a listener
        getPhysicsSpace().addCollisionListener(this);

        TerrainCreator();
        setupKeys();
        buildPlayer();
        setUpLight();
        initializeHud();
    }
    
    private void TerrainCreator(){
        //i create a material for all terrains
        matRock = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        matRock.setBoolean("useTriPlanarMapping", false);

        //load alpha and heighmap
        matRock.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/raceAlpha.png"));
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/race.png");

        //grass dirt and rock
        Texture grass = assetManager.loadTexture("Textures/Terrain/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex1", grass);
        matRock.setFloat("Tex1Scale", grassScale);
        Texture dirt = assetManager.loadTexture("Textures/Terrain/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex2", dirt);
        matRock.setFloat("Tex2Scale", dirtScale);
        Texture rock = assetManager.loadTexture("Textures/Terrain/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex3", rock);
        matRock.setFloat("Tex3Scale", rockScale);

        // wireframe of the material
        matWire = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matWire.getAdditionalRenderState().setWireframe(true);
        matWire.setColor("Color", ColorRGBA.Green);

        // CREATE HEIGHTMAP
        AbstractHeightMap heightmap = null;
        try {
            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 1f);
            heightmap.load();

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Here we create the actual terrain. The tiles will be 65x65, and the total size of the
         * terrain will be 513x513. It uses the heightmap we created to generate the height values.
         */
        /*
         * Optimal terrain patch size is 65 (64x64).
         * The total size is up to you. At 1025, it ran fine for me (200+FPS), however at
         * size=2049, it got really slow. But that is a jump from 2 million to 8 million triangles...
         */
        terrain = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        TerrainLodControl control = new TerrainLodControl(terrain, camera);
        control.setLodCalculator( new DistanceLodCalculator(65, 2.7f) ); // patch size, and a multiplier
        terrain.addControl(control);
        terrain.setMaterial(matRock);
        terrain.setLocalTranslation(0, -100, 0);
        terrain.setLocalScale(2f, 0.5f, 2f);
        /** 6. Add physics: */
        terrain.addControl(new RigidBodyControl(0));
        bulletAppState.getPhysicsSpace().add(terrain);
        
        //create sky
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
        
        //ensamble
        rootNode.attachChild(terrain);

        
    }
    
    private void setUpLight() {
        //creating 2 lights, one ambient one directional
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
      }
    
    private PhysicsSpace getPhysicsSpace(){
        //the physics listener
        return bulletAppState.getPhysicsSpace();
    }

    private void setupKeys() {
        //the key mapping
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Reverse", new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("Horn", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Reverse");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Space");
        inputManager.addListener(this, "Reset");
        inputManager.addListener(this, "Horn");
    }

    private void attachWeels(Node vehicleNode){
        //setting suspension values for wheels, this can be a bit tricky
        //see also https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
        float stiffness = 10.0f;//200=f1 car
        float compValue = .3f; //(should be lower than damp)
        float dampValue = .4f;
        vehicle.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionStiffness(stiffness);
        vehicle.setMaxSuspensionForce(10000.0f);
        
        //Create four wheels and add them at their locations
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
        float radius = 0.5f;
        float restLength = 0.3f;
        float yOff = 0.5f;
        float xOff = 1f;
        float zOff = 2f;

        //load the 3d model of the weels
        Spatial ruedita = assetManager.loadModel("Models/littleWeels.j3o");
        localRootNode.attachChild(ruedita);
        
        
        Node node1 = new Node("wheel 1 node");
        Node weelShape1 = (Node) localRootNode.getChild("weel1");
        node1.attachChild(weelShape1);
        weelShape1.rotate(0, FastMath.HALF_PI, 0);
        vehicle.addWheel(node1, new Vector3f(-xOff, yOff, zOff),
                         wheelDirection, wheelAxle, restLength, radius, true);

        Node node2 = new Node("wheel 2 node");
        Node weelShape2 = (Node) localRootNode.getChild("weel2");
        node2.attachChild(weelShape2);
        weelShape2.rotate(0, FastMath.HALF_PI, 0);
        vehicle.addWheel(node2, new Vector3f(xOff, yOff, zOff),
                         wheelDirection, wheelAxle, restLength, radius, true);

        Node node3 = new Node("wheel 3 node");
        Node weelShape3 = (Node) localRootNode.getChild("weel3");
        node3.attachChild(weelShape3);
        weelShape3.rotate(0, FastMath.HALF_PI, 0);
        vehicle.addWheel(node3, new Vector3f(-xOff, yOff, -zOff),
                         wheelDirection, wheelAxle, restLength, radius, false);

        Node node4 = new Node("wheel 4 node");
        Node weelShape4 = (Node) localRootNode.getChild("weel4");
        node4.attachChild(weelShape4);
        weelShape4.rotate(0, FastMath.HALF_PI, 0);
        vehicle.addWheel(node4, new Vector3f(xOff, yOff, -zOff),
                         wheelDirection, wheelAxle, restLength, radius, false);

        vehicleNode.attachChild(node1);
        vehicleNode.attachChild(node2);
        vehicleNode.attachChild(node3);
        vehicleNode.attachChild(node4);
    }
    
    private void buildPlayer() {
        //load the visible part of the cart
        Spatial carsito = assetManager.loadModel("Models/littleCar.j3o");
        localRootNode.attachChild(carsito);
        Node carNode = (Node) localRootNode.getChild("car");
        vehicleNode.attachChild(carNode);
        
        //create a compound shape and attach the BoxCollisionShape for the car body at 0,1,0
        //this shifts the effective center of mass of the BoxCollisionShape to 0,-1,0
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        BoxCollisionShape box = new BoxCollisionShape(new Vector3f(1.2f, 0.5f, 2.8f));
        compoundShape.addChildShape(box, new Vector3f(0, 1, 0));

        //create vehicle node
        vehicle = new VehicleControl(compoundShape, 400);
        vehicleNode.addControl(vehicle);
        
        
        //making smoke
        pAnimations.attachSmoke((Node) carNode.getChild("tailpipe"));

        //making weels
        attachWeels(vehicleNode);
        
        //start up position
        vehicle.setPhysicsLocation(new Vector3f(Constant.SP_X, Constant.SP_Y, Constant.SP_Z));
        rootNode.attachChild(vehicleNode);

        //i add this object to the physics enviroment
        getPhysicsSpace().add(vehicle);
        
        //set up the camera to the just created player
        flyByCamera.setEnabled(false);
        chaseCam = new ChaseCamera(camera, vehicleNode, inputManager);
        chaseCam.setInvertVerticalAxis(true);
        chaseCam.setDragToRotate(false);
    }
    
    @Override
    public void update(float tpf) {
        GUInterface.UpdateHUD(endurance, vehicle);
    }
    
    @Override
    public void collision(PhysicsCollisionEvent event) {
        if ( event.getNodeA().getName().equals("vehicleNode") ) {
            double impactDamage = event.getAppliedImpulse() / 100;
            endurance -= impactDamage;
        } else if ( event.getNodeB().getName().equals("vehicleNode") ) {
            double impactDamage = event.getAppliedImpulse()  / 100;
            endurance -= impactDamage;
        }
        
        if(endurance <= 0){pAnimations.setOnFire((Node) vehicleNode.getChild("engine"));}
    }
    
    @Override
    public void onAction(String name, boolean keyPressed, float tpf) {
        
        if(endurance >= 1){
            if (name.equals("Lefts")) {
                if (keyPressed) {
                        steeringValue += .5f;
                    } else {
                        steeringValue -= .5f;
                    }
                    vehicle.steer(steeringValue);
                } else if (name.equals("Rights")) {
                    if (keyPressed) {
                        steeringValue -= .5f;
                    } else {
                        steeringValue += .5f;
                    }
                    vehicle.steer(steeringValue);
                } else if (name.equals("Ups")) {
                    if (keyPressed) {
                        accelerationValue += accelerationForce;
                    } else {
                        accelerationValue -= accelerationForce;
                    }
                    vehicle.accelerate(accelerationValue);
                } else if (name.equals("Downs")) {
                    if (keyPressed) {
                        vehicle.brake(brakeForce);
                        if (vehicle.getCurrentVehicleSpeedKmHour() > 1) {audio.playBrakes();}
                    } else {
                        vehicle.brake(0f);
                        audio.stopBrakes();
                    }
                } else if (name.equals("Reverse")) {
                    if (keyPressed) {
                        deaccelerationValue -= deaccelerationForce;
                    } else {
                        deaccelerationValue += deaccelerationForce;
                    }
                    vehicle.accelerate(deaccelerationValue);
                } else if (name.equals("Horn")) {
                    if (keyPressed) {
                        audio.playHorn();
                    }
                    vehicle.accelerate(deaccelerationValue);
                } else if (name.equals("Space")) {
                    if (keyPressed) {
                        vehicle.applyImpulse(jumpForce, Vector3f.ZERO);
                    }
                } else if (name.equals("Reset")) {
                    if (keyPressed) {
                        System.out.println("Reset");
                        vehicle.setPhysicsLocation(new Vector3f(Constant.SP_X, Constant.SP_Y, Constant.SP_Z));
                        vehicle.setPhysicsRotation(new Matrix3f());
                        vehicle.setLinearVelocity(Vector3f.ZERO);
                        vehicle.setAngularVelocity(Vector3f.ZERO);
                        vehicle.resetSuspension();
                    } else {
                }
            }
        }
    }   
    
    // ---------------------------------- ENGINE ---------------------------------------------

    // ---------------------------------- GUI ---------------------------------------------
    private void initializeHud(){
        GUInterface.drawLife(ColorRGBA.Green, "LIFE: " + endurance, 300, 0, 30);
        GUInterface.drawSpeed(ColorRGBA.Green, "Speed: " + (int)vehicle.getCurrentVehicleSpeedKmHour(), 500, 0, 30);
    }
    // ---------------------------------- GUI ---------------------------------------------

}