/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package animations;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author Fran
 */
public class particleAnimations {
    private final AssetManager assetManager;
    
    public particleAnimations(AssetManager asset){
        assetManager = asset;
    }
    
    public void setOnFire(Node target){
        /** Uses Texture from jme3-test-data library! */
        ParticleEmitter fireEffect = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        fireMat.setTexture("Texture", assetManager.loadTexture("Textures/flame.png"));
        fireEffect.setMaterial(fireMat);
        fireEffect.setImagesX(2); fireEffect.setImagesY(2); // 2x2 texture animation
        fireEffect.setEndColor( new ColorRGBA(1f, 0f, 0f, 1f) );   // red
        fireEffect.setStartColor( new ColorRGBA(1f, 1f, 0f, 0.5f) ); // yellow
        fireEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fireEffect.setStartSize(0.9f);
        fireEffect.setEndSize(0.3f);
        fireEffect.setGravity(0f,0f,0f);
        fireEffect.setLowLife(0.5f);
        fireEffect.setHighLife(3f);
        fireEffect.setNumParticles(600);
        fireEffect.getParticleInfluencer().setVelocityVariation(0.3f);
        target.attachChild(fireEffect);            
    }
    
    public void explode(Node target){
        /** Explosion effect. Uses Texture from jme3-test-data library! */ 
        ParticleEmitter debrisEffect = new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 10);
        Material debrisMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        debrisMat.setTexture("Texture", assetManager.loadTexture("Textures/Debris.png"));
        debrisEffect.setMaterial(debrisMat);
        debrisEffect.setImagesX(3); debrisEffect.setImagesY(3); // 3x3 texture animation
        debrisEffect.setRotateSpeed(4);
        debrisEffect.setSelectRandomImage(true);
        debrisEffect.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 4, 0));
        debrisEffect.setStartColor(new ColorRGBA(1f, 1f, 1f, 1f));
        debrisEffect.setGravity(0f,6f,0f);
        debrisEffect.getParticleInfluencer().setVelocityVariation(.60f);
        target.attachChild(debrisEffect);
        debrisEffect.emitAllParticles();
       
    }
    
    public void attachSmoke(Node target){
        ParticleEmitter smoke = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat_red.setTexture("Texture", assetManager.loadTexture("Textures/Smoke.png"));
        smoke.setMaterial(mat_red);
        smoke.setImagesX(15); smoke.setImagesY(1); // 2x2 texture animation
        smoke.setEndColor(  new ColorRGBA(1f, 1f, 1f, 1f));   // white
        smoke.setStartColor(new ColorRGBA(0f, 0f, 0f, 0f)); // black
        smoke.getParticleInfluencer().setInitialVelocity(new Vector3f(0, .5f, 0));
        smoke.setStartSize(1.5f);
        smoke.setEndSize(0.1f);
        smoke.setGravity(0,-1,.5f);
        smoke.setLowLife(0.5f);
        smoke.setHighLife(3f);
        smoke.getParticleInfluencer().setVelocityVariation(0.3f);
        target.attachChild(smoke);
    }
}
