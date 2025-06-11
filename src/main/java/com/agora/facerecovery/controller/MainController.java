package com.agora.facerecovery.controller;

import com.agora.facerecovery.model.FaceResult;
import com.agora.facerecovery.model.ResultRow;
import com.agora.facerecovery.utils.ExcelExporter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML private ImageView originalImageView, withGlassesImageView, ganImageView, mtcnnImageView;
    @FXML private Label originalFileNameLabel, glassesScoreLabel, ganScoreLabel, mtcnnScoreLabel;
    @FXML private TableView<ResultRow> resultsTable;
    @FXML private TableColumn<ResultRow, String> fileNameCol, errorCol;
    @FXML private TableColumn<ResultRow, Number> ganScoreCol, ganTimeCol, mtcnnScoreCol, mtcnnTimeCol;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Button resumeButton;

    private final List<FaceResult> faceResults = new ArrayList<>();
    private final ObservableList<ResultRow> resultRows = FXCollections.observableArrayList();
    private int currentIndex = 0;

    private volatile boolean paused = false;
    private volatile boolean stopped = false;

    @FXML
    public void initialize() {
        loadAllImages();
        if (!faceResults.isEmpty()) {
            showCurrentImage();
        }

        if (resultsTable != null) {
            fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
            ganScoreCol.setCellValueFactory(new PropertyValueFactory<>("ganScore"));
            ganTimeCol.setCellValueFactory(new PropertyValueFactory<>("ganTime"));
            mtcnnScoreCol.setCellValueFactory(new PropertyValueFactory<>("mtcnnScore"));
            mtcnnTimeCol.setCellValueFactory(new PropertyValueFactory<>("mtcnnTime"));
            errorCol.setCellValueFactory(new PropertyValueFactory<>("error"));
            resultsTable.setItems(resultRows);
        }

        if (resumeButton != null) resumeButton.setDisable(true);
        
        Platform.runLater(this::processAllInBackground);
    }

    private void loadAllImages() {
        File baseDir = new File("data/smallSet");
        File glassesDir = new File("data/withglasses");
        File cleanDir = new File("data/smallSetClean");

        File[] files = baseDir.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if (files == null) return;

        faceResults.clear();
        for (File file : files) {
            String name = file.getName();
            File withGlasses = new File(glassesDir, name);
            File cleaned = new File(cleanDir, name);

            if (!withGlasses.exists() || !cleaned.exists()) {
                System.out.println("Пропущен: " + name);
                continue;
            }

            faceResults.add(new FaceResult(file, withGlasses, cleaned));
        }
    }

    private void showCurrentImage() {
        if (faceResults.isEmpty() || currentIndex < 0 || currentIndex >= faceResults.size()) return;

        FaceResult result = faceResults.get(currentIndex);
        originalImageView.setImage(new Image(result.getOriginal().toURI().toString()));
        withGlassesImageView.setImage(new Image(result.getWithGlasses().toURI().toString()));
        ganImageView.setImage(new Image(result.getCleaned().toURI().toString()));
        mtcnnImageView.setImage(null);

        originalFileNameLabel.setText(result.getOriginal().getName());
        glassesScoreLabel.setText("Score: -");
        ganScoreLabel.setText("Score: -");
        mtcnnScoreLabel.setText("Score: -");
    }

    private Task<String> runPythonScript(String script, File imageFile) {
        return new Task<>() {
            @Override
            protected String call() throws Exception {
                ProcessBuilder pb = new ProcessBuilder("python", script, imageFile.getAbsolutePath());
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String output = reader.readLine();
                    process.waitFor();
                    return (output != null && !output.contains("error")) ? output : "0.0;0.0";
                }
            }
        };
    }

    private void runAndCollectResults(int index) {
        if (faceResults.isEmpty() || index < 0 || index >= faceResults.size()) return;

        FaceResult result = faceResults.get(index);
        File ganInput = result.getWithGlasses();
        File mtcnnInput = result.getCleaned();
        String fileName = result.getOriginal().getName();

        Task<String> ganTask = runPythonScript("python/python/process_gan.py", ganInput);
        Task<String> mtcnnTask = runPythonScript("python/python/process_mtcnn.py", mtcnnInput);

        final double[] ganScore = {0.0}, ganTime = {0.0}, mtcnnScore = {0.0}, mtcnnTime = {0.0};
        final StringBuilder errorMsg = new StringBuilder();

        ganTask.setOnSucceeded(e -> {
            String resultStr = ganTask.getValue();
            if (resultStr != null && resultStr.contains(";")) {
                String[] parts = resultStr.split(";");
                ganScore[0] = Double.parseDouble(parts[0]);
                ganTime[0] = Double.parseDouble(parts[1]);
                Platform.runLater(() -> ganScoreLabel.setText("Score: " + parts[0] + " (t=" + parts[1] + "s)"));
            } else {
                errorMsg.append("Ошибка GAN. ");
            }
        });

        mtcnnTask.setOnSucceeded(e -> {
            String resultStr = mtcnnTask.getValue();
            if (resultStr != null && resultStr.contains(";")) {
                String[] parts = resultStr.split(";");
                mtcnnScore[0] = Double.parseDouble(parts[0]);
                mtcnnTime[0] = Double.parseDouble(parts[1]);
                Platform.runLater(() -> mtcnnScoreLabel.setText("Score: " + parts[0] + " (t=" + parts[1] + "s)"));
            } else {
                errorMsg.append("Ошибка MTCNN.");
            }

            Platform.runLater(() -> {
                resultRows.add(new ResultRow(fileName, ganScore[0], ganTime[0], mtcnnScore[0], mtcnnTime[0], errorMsg.toString()));
                updateProgressBar(index + 1);
            });
        });

        new Thread(ganTask).start();
        new Thread(mtcnnTask).start();
    }

    private void processAllInBackground() {
        stopped = false;
        paused = false;

        new Thread(() -> {
            for (currentIndex = 0; currentIndex < faceResults.size(); currentIndex++) {
                if (stopped) break;

                while (paused) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {
                    }
                }

                int index = currentIndex;
                Platform.runLater(() -> showCurrentImage());
                runAndCollectResults(index);

                try {
                    Thread.sleep(100); // визуальное обновление
                } catch (InterruptedException ignored) {
                }
            }

            Platform.runLater(() -> {
                progressBar.setProgress(1.0);
                progressLabel.setText("Готово");
            });
        }).start();
    }
        //new Thread(() -> {
         //   for (currentIndex = 0; currentIndex < faceResults.size(); currentIndex++) {
          //      int index = currentIndex;
          //      Platform.runLater(() -> showCurrentImage());
          //      runAndCollectResults(index);
          //      try {
          //          Thread.sleep(100); // для визуального обновления
          //      } catch (InterruptedException ignored) {}
           // }
           // Platform.runLater(() -> {
          //      progressBar.setProgress(1.0);
          //      progressLabel.setText("Готово");
         //   });
        // }).start();
   // }

    private void updateProgressBar(int processed) {
        double progress = (double) processed / faceResults.size();
        progressBar.setProgress(progress);
        progressLabel.setText("Обработано: " + processed + " из " + faceResults.size());
    }

    @FXML
    private void onExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(resultsTable.getScene().getWindow());
        if (file != null) exportToCSV(file);
    }

    @FXML
    private void onExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File file = fileChooser.showSaveDialog(resultsTable.getScene().getWindow());
        if (file != null) exportToExcel(file);
    }
   // @FXML
   // private void onExport() {
   //     FileChooser fileChooser = new FileChooser();
   //     fileChooser.setTitle("Сохранить");
   //     fileChooser.getExtensionFilters().addAll(
   //             new FileChooser.ExtensionFilter("CSV", "*.csv"),
   //             new FileChooser.ExtensionFilter("Excel", "*.xlsx")
   //     );
   //     File file = fileChooser.showSaveDialog(resultsTable.getScene().getWindow());
    //    if (file == null) return;

    //    if (file.getName().endsWith(".xlsx")) exportToExcel(file);
    //    else exportToCSV(file);
   //  }

    private void exportToCSV(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Файл,GAN Score,GAN Время,MTCNN Score,MTCNN Время,Ошибка");
            for (ResultRow row : resultRows) {
                writer.printf("%s,%.4f,%.3f,%.4f,%.3f,%s%n",
                        row.getFileName(), row.getGanScore(), row.getGanTime(),
                        row.getMtcnnScore(), row.getMtcnnTime(), row.getError());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportToExcel(File file) {
        try {
            ExcelExporter.exportToExcel(resultRows, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   // private void exportToExcel(File file) {
   //     try (XSSFWorkbook workbook = new XSSFWorkbook()) {
   //         XSSFSheet sheet = workbook.createSheet("Results");

  //          String[] headers = {"Файл", "GAN Score", "GAN Время", "MTCNN Score", "MTCNN Время"};
  //          Row header = sheet.createRow(0);
  //          for (int i = 0; i < headers.length; i++) {
  //              header.createCell(i).setCellValue(headers[i]);
  //          }

  //          for (int i = 0; i < resultRows.size(); i++) {
  //               ResultRow row = resultRows.get(i);
  //              Row r = sheet.createRow(i + 1);
  //              r.createCell(0).setCellValue(row.getFileName());
  //              r.createCell(1).setCellValue(row.getGanScore());
  //              r.createCell(2).setCellValue(row.getGanTime());
  //              r.createCell(3).setCellValue(row.getMtcnnScore());
  //              r.createCell(4).setCellValue(row.getMtcnnTime());
  //          }

  //          try (FileOutputStream out = new FileOutputStream(file)) {
  //              workbook.write(out);
  //          }

  //      } catch (IOException e) {
  //          e.printStackTrace();
  //      }
  //  }

    @FXML private void onRun() {
        runAndCollectResults(currentIndex);
    }

    @FXML private void onPause() {
        paused = !paused;
        progressLabel.setText(paused ? "⏸ Пауза" : "▶ Продолжение");
        // System.out.println("⏸ Пауза");
    }

    @FXML private void onStop() {
        stopped = true;
        paused = false;

        Platform.runLater(() -> {
            progressLabel.setText("⏹ Остановлено");
            if (resumeButton != null) resumeButton.setDisable(false);
        });
       // System.out.println("⏹ Остановка");
    }

    @FXML
    private void onResumeFromStop() {
        if (!stopped) return; // только если действительно была остановка

        stopped = false;
        paused = false;

        Platform.runLater(() -> {
            progressLabel.setText("▶ Продолжение с текущего");
            if (resumeButton != null) resumeButton.setDisable(true);
        });

        new Thread(() -> {
            for (; currentIndex < faceResults.size(); currentIndex++) {
                if (stopped) break;

                while (paused) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {}
                }

                int index = currentIndex;
                Platform.runLater(() -> showCurrentImage());
                runAndCollectResults(index);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }

            Platform.runLater(() -> {
                if (!stopped) {
                    progressBar.setProgress(1.0);
                    progressLabel.setText("Готово");
                    resumeButton.setDisable(true);
                }
            });
        }).start();
    }

    @FXML private void onNext() {
        if (currentIndex < faceResults.size() - 1) {
            currentIndex++;
            showCurrentImage();
        }
    }

    @FXML private void onPrev() {
        if (currentIndex > 0) {
            currentIndex--;
            showCurrentImage();
        }
    }

    @FXML
    private void onBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        chooser.showOpenDialog(new Stage());
    }
}