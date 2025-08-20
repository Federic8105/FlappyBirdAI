package flappyBirdAI.controller;

public class GameStats {
    public int fps = 0, nGen = 1, nBirds = 0, nTubePassed = 0, nMaxTubePassed = 0, autoSaveThreshold = 50;
    public double bestLifeTime = 0;
    public boolean isAutoSaveEnabled = true;
}