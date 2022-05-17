/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package userInterface;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import statics.Constant;

/**
 *
 * @author Fran
 */
public class GUI {
    private Node GUInterface;
    
    private BitmapFont myFont;
    private BitmapText guiLife;
    private BitmapText guiSpeed;
    private final AssetManager assetManager;
            
    public GUI(Node guiNode, AssetManager asset){
        GUInterface = guiNode;
        assetManager = asset;
        
        loadFonts();
    }
    
    private void  loadFonts(){
        myFont = (BitmapFont) assetManager.loadFont("Interface/fonts/DejaVuSansLight.fnt");
        
        guiLife = new BitmapText(myFont, false);
        guiSpeed = new BitmapText(myFont, false);
    }
    
    public void drawLife(ColorRGBA color, String text, int X, int Y, int size){
        guiLife.setSize(size);
        guiLife.setColor(color);                                        // font color
        guiLife.setText(text);                                          // the text
        guiLife.setLocalTranslation(X, guiLife.getLineHeight(), Y);     // position
        GUInterface.attachChild(guiLife);
    }
    
    public void drawSpeed(ColorRGBA color, String text, int X, int Y, int size){
        guiSpeed.setSize(size);
        guiSpeed.setColor(color);                                        // font color
        guiSpeed.setText(text);                                          // the text
        guiSpeed.setLocalTranslation(X, guiSpeed.getLineHeight(), Y);     // position
        GUInterface.attachChild(guiSpeed);
    }
    
    public void UpdateHUD(double endurance, VehicleControl vehicle){
        //GUI UPDATES
        if(endurance >= 0){
            guiLife.setText("LIFE: " + (int)((endurance * 100) / Constant.MAX_LIFE) + "%");
        }else{
            guiLife.setText("LIFE: DEAD!");
            guiLife.setColor(ColorRGBA.Red);
        }
        
        guiSpeed.setText("Speed: " +(int) vehicle.getCurrentVehicleSpeedKmHour());
    }
}
