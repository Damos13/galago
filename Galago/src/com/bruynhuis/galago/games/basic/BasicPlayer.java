/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bruynhuis.galago.games.basic;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author nidebruyn
 */
public abstract class BasicPlayer {
    
    protected BasicGame game;
    protected Node playerNode;
    protected Vector3f startPosition;
    protected int lives = 3;
    protected int score = 0;

    public BasicPlayer(BasicGame basicGame) {
        this.game = basicGame;
    }
    
    public void load() {
        this.startPosition = game.getStartPosition().clone();
        
        //Load the player models
        playerNode = new Node(BasicGame.TYPE_PLAYER);
        playerNode.setLocalTranslation(startPosition);
        game.getLevelNode().attachChild(playerNode);

        init();
    }
    
    protected abstract void init();
    
    public void start() {
        
    }
    
    public void log(String text) {
        System.out.println(text);
    }
    
    public void close() {
        playerNode.removeFromParent();
    }

    public BasicGame getGame() {
        return game;
    }

    public Node getPlayerNode() {
        return playerNode;
    }
    
    public abstract Vector3f getPosition();
    
    public void doDamage(int hits) {
        if (lives > 0) {
            lives -= hits;
        } else {
            game.doGameOver();
            doDie();
        }
    }    

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
        game.fireScoreChangedListener(this.score);
    }
    
    public abstract void doDie();
}
