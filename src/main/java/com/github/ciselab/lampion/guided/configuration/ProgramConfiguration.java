package com.github.ciselab.lampion.guided.configuration;

public class ProgramConfiguration {

    private int maxTimeInMin = 480;
    private boolean useGA = true;

    private long seed = 2022;

    private String directoryPath = System.getProperty("user.dir").replace("\\", "/");
    private String dataDirectoryPath = directoryPath + "/code2vec/data/";
    private String bashPath = "C:/Program Files/Git/bin/bash.exe";

    private String modelPath = "models/java14_model/saved_model_iter8.release";
    private Integer bashRetries = 3;

    public void setModelPath(String arg) {
        if (arg == null || arg.isEmpty() || arg.isBlank()){
            throw new IllegalArgumentException("Model Path cannot be null or empty");
        }
        this.modelPath = arg;
    }

    public String getModelPath(){
        return this.modelPath;
    }

    public int getMaxTimeInMin() {
        return maxTimeInMin;
    }

    public void setMaxTimeInMin(int maxTimeInMin) {
        this.maxTimeInMin = maxTimeInMin;
    }

    public boolean isUseGA() {
        return useGA;
    }

    public void setUseGA(boolean useGA) {
        this.useGA = useGA;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public String getDataDirectoryPath() {
        return dataDirectoryPath;
    }

    public void setDataDirectoryPath(String dataDirectoryPath) {
        this.dataDirectoryPath = dataDirectoryPath;
    }

    public String getBashPath() {
        return bashPath;
    }

    public void setBashPath(String bashPath) {
        this.bashPath = bashPath;
    }

    public long getSeed(){
        return this.seed;
    }

    public void setSeed(long seed){
        this.seed = seed;
    }

    public Integer getBashRetries() {
        return this.bashRetries;
    }
}
