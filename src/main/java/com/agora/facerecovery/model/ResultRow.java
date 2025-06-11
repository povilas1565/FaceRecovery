package com.agora.facerecovery.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ResultRow {

    private final StringProperty fileName;
    private final DoubleProperty ganScore;
    private final DoubleProperty ganTime;
    private final DoubleProperty mtcnnScore;
    private final DoubleProperty mtcnnTime;

    private final StringProperty error; // новое поле

    public ResultRow(String fileName, double ganScore, double ganTime, double mtcnnScore, double mtcnnTime, String error) {
        this.fileName = new SimpleStringProperty(fileName);
        this.ganScore = new SimpleDoubleProperty(ganScore);
        this.ganTime = new SimpleDoubleProperty(ganTime);
        this.mtcnnScore = new SimpleDoubleProperty(mtcnnScore);
        this.mtcnnTime = new SimpleDoubleProperty(mtcnnTime);
        this.error = new SimpleStringProperty(error);
    }

    public String getFileName() { return fileName.get(); }
    public double getGanScore() { return ganScore.get(); }
    public double getGanTime() { return ganTime.get(); }
    public double getMtcnnScore() { return mtcnnScore.get(); }
    public double getMtcnnTime() { return mtcnnTime.get(); }
    
    public String getError() { return error.get(); }

    public StringProperty fileNameProperty() { return fileName; }
    public DoubleProperty ganScoreProperty() { return ganScore; }
    public DoubleProperty ganTimeProperty() { return ganTime; }
    public DoubleProperty mtcnnScoreProperty() { return mtcnnScore; }
    public DoubleProperty mtcnnTimeProperty() { return mtcnnTime; }

}
