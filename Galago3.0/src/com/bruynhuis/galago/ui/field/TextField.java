/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bruynhuis.galago.ui.field;

import com.bruynhuis.galago.ui.ImageWidget;
import com.bruynhuis.galago.ui.button.TouchKeyNames;
import com.bruynhuis.galago.ui.listener.KeyboardListener;
import com.bruynhuis.galago.ui.listener.FocusListener;
import com.bruynhuis.galago.ui.listener.TouchButtonListener;
import com.bruynhuis.galago.ui.panel.Panel;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.LineWrapMode;
import com.jme3.font.Rectangle;
import com.jme3.input.InputManager;
import com.jme3.input.KeyNames;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.bruynhuis.galago.ui.effect.Effect;

/**
 * A TextField to enter text with the keyboard.
 * 
 * @author nidebruyn
 */
public class TextField extends ImageWidget {
    
    protected Panel panel;
    protected InputManager inputManager;
    protected Camera cam;
    protected CollisionResults results;
    protected Ray ray;
    protected int uid;
    protected String id;
    protected TouchButtonListener touchButtonListener;
    protected ActionListener actionListener;
    private boolean enabled = true;
    private boolean wasDown = false;
    protected BitmapText bitmapText;
    protected float fontSize = 20;
    private boolean focus = false;
    protected int maxLength = 10;    
    protected boolean caps = false;
    protected boolean shift = false;
    
    protected KeyboardListener keyboardListener;
    protected FocusListener focusListener;
    protected KeyNames keyNames = new KeyNames();
    protected TouchKeyNames touchKeyNames = new TouchKeyNames();
    
    /**
     * 
     * @param panel
     * @param id 
     */
    public TextField(Panel panel, String id) {
        this(panel, id, "Resources/textfield.png", 300, 40);
    }
    
    /**
     * 
     * @param panel
     * @param id
     * @param pictureFile 
     */
    public TextField(Panel panel, String id, String pictureFile) {
        this(panel, id, pictureFile, 300, 40);
    }
    
    public TextField(Panel panel, String id, String pictureFile, float width, float height) {
        this(panel, id, pictureFile, width, height, false);
    }

    /**
     * 
     * @param panel
     * @param id
     * @param pictureFile
     * @param width
     * @param height 
     */
    public TextField(Panel panel, String id, String pictureFile, float width, float height, boolean lockScale) {
        super(panel.getWindow(), panel, pictureFile, width, height, lockScale);
        this.id = id;
        setName(id);
        
        //Init the text
        float xP = -getWidth() * 0.5f;
        float yP = getHeight() * 0.5f;
        float recWidth = getWidth();
        float factor = 1f;
        float recHeight = (getHeight()*0.5f) * factor;
        float padding = 0f;
               
        bitmapText = window.getBitmapFont().createLabel(id);
        bitmapText.setText(" ");
        bitmapText.setBox(new Rectangle(xP+padding, yP, recWidth+padding, recHeight));
        bitmapText.setSize(fontSize * window.getScaleFactorHeight());      // font size
        bitmapText.setColor(ColorRGBA.DarkGray);// font color
        bitmapText.setAlignment(BitmapFont.Align.Left);
        bitmapText.setVerticalAlignment(BitmapFont.VAlign.Center);
        bitmapText.setLineWrapMode(LineWrapMode.NoWrap);
        widgetNode.attachChild(bitmapText);

        this.inputManager = window.getInputManager();
        this.cam = window.getApplication().getCamera();
        this.results = new CollisionResults();
        this.ray = new Ray(cam.getLocation(), cam.getDirection());

        actionListener = new ActionListener() {
            public void onAction(String name, boolean isPressed, float tpf) {
                results.clear();

                if (isVisible() && isEnabled()) {

                    if ((TextField.this.id + "MOUSE").equals(name)) {

                        // 1. calc direction
                        Vector3f origin = new Vector3f(inputManager.getCursorPosition().x, inputManager.getCursorPosition().y, 1f);
                        Vector3f direction = new Vector3f(0, 0, -1);

                        // 2. Aim the ray from cam loc to cam direction.        
                        ray.setOrigin(origin);
                        ray.setDirection(direction);

                        // 3. Collect intersections between Ray and Shootables in results list.
                        window.getWindowNode().collideWith(ray, results);

                        // 5. Use the results (we mark the hit object)
                        if (results.size() > 0) {

                            for (int i = 0; i < results.size(); i++) {
                                CollisionResult cr = results.getCollision(i);
//                                System.out.println("\t-> Hit: " + cr.getGeometry().getParent().getName());

                                if (widgetNode.hasChild(cr.getGeometry())) {
//                                    System.out.println("\t\t\tCollision -> " + TouchButton.this.id);
                                    if (isPressed) {
                                        wasDown = true;
                                        getWindow().removeFocusFromFields();
                                        focus = true;
                                        fireFocusListener(TextField.this.id);
                                    }
                                }
                            }
                        }

                    }
                }
                
            }
        };

        inputManager.addRawInputListener(new RawInputListener() {

            public void beginInput() {

            }

            public void endInput() {

            }

            public void onJoyAxisEvent(JoyAxisEvent evt) {

            }

            public void onJoyButtonEvent(JoyButtonEvent evt) {

            }

            public void onMouseMotionEvent(MouseMotionEvent evt) {

            }

            public void onMouseButtonEvent(MouseButtonEvent evt) {

            }

            public void onKeyEvent(KeyInputEvent evt) {
//                System.out.println("Keyinput ***************** Key = " + evt.getKeyCode());
                
                if (enabled && focus && evt.isReleased()) {
                    String keyChar = keyNames.getName(evt.getKeyCode());
//                    System.out.println("Keyinput ***************** Char = " + keyChar);
                    
                    if (evt.getKeyCode() == 14) {
                        if (getText().length() > 0) {
                            setText(getText().substring(0, getText().length()-1));
                        }                        
                    } else if (evt.getKeyCode() == 15) {
                        focus = false;
                        
                    } else if (keyChar != null && evt.getKeyCode() == 57) {
                        setText(getText() + " ");
                        
                    } else if (keyChar != null && evt.getKeyCode() == 58) {
                        caps = !caps;
                        
                    } else if (keyChar != null && keyChar.length() == 1) {
                        if (!caps) {
                            keyChar = keyChar.toLowerCase();
                        }
                        setText(getText() + keyChar);
                    }
                    
                    if (getText().length() > maxLength) {
                        setText(getText().substring(0, maxLength));
                    }
                    
                    fireKeyboardListener(evt);
                    
                }

            }

            public void onTouchEvent(TouchEvent evt) {
//                System.out.println("Touchinput ***************** Keycode = " + evt.getKeyCode());
                
                if (enabled && focus && evt.getType().equals(TouchEvent.Type.KEY_DOWN)) {
                    String keyChar = touchKeyNames.getName(evt.getKeyCode());
                    System.out.println("\n\n\nTouchinput ***************** KeyCode = " + evt.getKeyCode());
                    
                    if (evt.getKeyCode() == 67) { //backspace
                        if (getText().length() > 0) {
                            setText(getText().substring(0, getText().length()-1));
                        }                        
                        
                    } else if (keyChar != null && evt.getKeyCode() == 62) { //space
                        setText(getText() + " ");
                        
                    } else if (keyChar != null && evt.getKeyCode() == 59) { //shift
                        caps = !caps;
                        
                    } else if (keyChar != null && keyChar.length() == 1) {
                        //TODO:
//                        if (!caps) {
                            keyChar = keyChar.toLowerCase();                            
//                        }
                        setText(getText() + keyChar);
                    }
                    
                    if (getText().length() > maxLength) {
                        setText(getText().substring(0, maxLength));
                    }
                    
//                    if (evt.getKeyCode() == 67) {
//                        if (getText().length() > 0) {
//                            setText(getText().substring(0, getText().length()-1));
//                        }                        
//                        
//                    } else if (evt.getKeyCode() == 59) {
//                        if (getText().length() > 0) {
//                            setText(getText().substring(0, getText().length()-1));
//                        }                        
//                        
//                    } else if (keyChar != null && keyChar.length() == 1) {
//                        setText(getText() + keyChar);
//                    }
//                    
//                    if (getText().length() > maxLength) {
//                        setText(getText().substring(0, maxLength));
//                    }
                    
                }


            }
        });
        
        //NICKI
        panel.add(this);
        
//        bitmapText.setLocalTranslation(bitmapText.getLocalTranslation().x, bitmapText.getLocalTranslation().y, 0.001f);
    }
    
    @Override
    protected boolean isBatched() {
        return false;
    }
    
    public void addKeyboardListener(KeyboardListener keyboardListener) {
        this.keyboardListener = keyboardListener;
    }
    
    protected void fireKeyboardListener(KeyInputEvent event) {
        if (keyboardListener != null) {
            keyboardListener.doKeyPressed(event);
        }
    }
    
    public void addFocusListener(FocusListener focusListener1) {
        this.focusListener = focusListener1;
    }
    
    protected void fireFocusListener(String id) {
        if (focusListener != null) {
            focusListener.doFocus(id);
        }
    }
    
    /**
     *
     * @param align
     */
    public void setTextAlignment(BitmapFont.Align align) {
        bitmapText.setAlignment(align);
    }

    /**
     *
     * @param vAlign
     */
    public void setTextVerticalAlignment(BitmapFont.VAlign vAlign) {
        bitmapText.setVerticalAlignment(vAlign);
    }

    /**
     * 
     * @param maxLength 
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
    
    public String getText() {
        return bitmapText.getText();                
    }
    
    /**
     * 
     * @param text 
     */
    public void setText(String text) {
        if (text == null || text.length() == 0) text = " ";
        bitmapText.setText(text);
    }
    
    /**
     * 
     * @param colorRGBA 
     */
    public void setTextColor(ColorRGBA colorRGBA) {
        bitmapText.setColor(colorRGBA);
    }
    
    /**
     * 
     * @param size 
     */
    public void setFontSize(float size) {
        bitmapText.setSize(size*window.getScaleFactorHeight());// font size
    }

    @Override
    public void add(Node parent) {
        super.add(parent);
        String mappingName = TextField.this.id + "MOUSE";
        if (!inputManager.hasMapping(mappingName)) {
            inputManager.addMapping(mappingName, new MouseButtonTrigger(0));
        }

        inputManager.addListener(actionListener, TextField.this.id + "MOUSE");
    }

    @Override
    public void remove() {
        super.remove();
        inputManager.removeListener(actionListener);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        
        if (visible) {
            inputManager.addListener(actionListener, TextField.this.id + "MOUSE");
            bitmapText.setCullHint(Spatial.CullHint.Never);
        } else {
            inputManager.removeListener(actionListener);
            bitmapText.setCullHint(Spatial.CullHint.Always);
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 
     * @param enabled 
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        for (Effect effect : effects) {
            effect.fireEnabled(enabled);
        }
    }

    public void blur() {
        focus = false;
    }
}
