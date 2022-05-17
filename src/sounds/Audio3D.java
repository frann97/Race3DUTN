/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sounds;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;

/**
 *
 * @author Fran
 */
public class Audio3D {
    private AudioNode audio_brakes;
    private AudioNode audio_nature;
    private AudioNode audio_engineGurgle;
    private AudioNode audio_horn;
    private final Node rootNode;
    private final AssetManager assetManager;
    
    public Audio3D(Node root, AssetManager asset){
        rootNode = root;
        assetManager = asset;
        
        initAudio();
    }
    
    private void initAudio(){
        /** We create two audio nodes. */
        /* gun shot sound is to be triggered by a mouse click. */
        audio_brakes = new AudioNode(assetManager, "Sounds/brakes.wav", AudioData.DataType.Buffer);
        audio_brakes.setPositional(false);
        audio_brakes.setLooping(false);
        audio_brakes.setVolume(2);
        rootNode.attachChild(audio_brakes);
        
        audio_horn = new AudioNode(assetManager, "Sounds/sf_horn_21.wav", AudioData.DataType.Buffer);
        audio_horn.setPositional(false);
        audio_horn.setLooping(false);
        audio_horn.setVolume(2);
        rootNode.attachChild(audio_horn);

        /* nature sound - keeps playing in a loop. */
        audio_nature = new AudioNode(assetManager, "Sounds/forest.ogg", AudioData.DataType.Stream);
        audio_nature.setLooping(true);  // activate continuous playing
        audio_nature.setPositional(false);
        audio_nature.setVolume(3);
        rootNode.attachChild(audio_nature);
        audio_nature.play(); // play continuously!
        
        /* nature sound - keeps playing in a loop. */
        audio_engineGurgle = new AudioNode(assetManager, "Sounds/engine_gurgle.ogg", AudioData.DataType.Stream);
        audio_engineGurgle.setLooping(true);  // activate continuous playing
        audio_engineGurgle.setPositional(false);
        audio_engineGurgle.setVolume(0.2f);
        rootNode.attachChild(audio_engineGurgle);
        audio_engineGurgle.play(); // play continuously! 
    }
    
    public void playBrakes(){
        audio_brakes.play();
    }
    
    public void stopBrakes(){
        audio_brakes.stop();
    }
    
    public void playHorn(){
        audio_horn.play();
    }
    
    public void stopHorn(){
        audio_horn.stop();
    }
}
