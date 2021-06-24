package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static java.lang.System.exit;


public class Main extends Application {

    VBox textVBox = new VBox();
    File file;
    int numOfWords;
    int numOfRows;
    int percentOfWords;
    int mistakeTolerance = 3 ;
    int memorisingSpeed = 3 - mistakeTolerance;
    boolean autosave;


    @Override
    public void start(Stage stage) {
        System.setProperty("file.encoding","utf-8");
        stage.setMaximized(true);
        stage.setTitle("Call to Memory");

        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, Color.WHITE);
        scene.getStylesheets().add(this.getClass().getResource("/stylesheet.css").toExternalForm());
        stage.getIcons().add(new Image("resources/brainstorm.png"));

        File options = new File("src\\resources\\options.txt");
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(options), "UTF8"));
            Scanner sc = new Scanner(bufferedReader);
            String string = sc.nextLine();
            String[] stringArr = string.split(" ");
            mistakeTolerance = Integer.parseInt(stringArr[0]);
            memorisingSpeed = Integer.parseInt(stringArr[1]);
            autosave = !"0".equals(stringArr[2]);

        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(we -> exit(0));

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                borderPane.setBackground(null);
                borderPane.setTop(menuLoad(stage));
                startScene(stage);
                scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, this);
            }
        });
    }

    public void startScene(Stage stage){
        stage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
        stage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
        VBox vbox = new VBox();
        vbox.setStyle("-fx-background-color: #FFFFFF;");
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(60);
        VBox vbox2 = new VBox();
        vbox2.setStyle("-fx-background-image: url('resources/start_menu.png');" +
                "-fx-background-repeat: stretch;" +
                "-fx-background-size: 832 459;" +
                "-fx-background-position: center center;");
        vbox2.setPrefSize(832, 459);
        vbox2.setAlignment(Pos.CENTER);
        vbox2.setSpacing(30);
        Label label = new Label("Start memorizing your texts!");
        label.setId("large-text");

        Button buttonNew = new Button();
        buttonNew.setId("create-new-button");
        Button buttonOpenPrev = new Button();
        buttonOpenPrev.setId("open-prev-button");
        Button buttonOpenNew = new Button();
        buttonOpenNew.setId("open-button");
        vbox2.getChildren().addAll(buttonNew, buttonOpenPrev, buttonOpenNew);
        vbox.getChildren().addAll(label, vbox2);
        buttonNew.setOnAction((ActionEvent e) -> {
            createFile(stage);
        });
        buttonOpenPrev.setOnAction((ActionEvent e) -> {
            openPrevious(stage);
        });

        buttonOpenNew.setOnAction((ActionEvent e) -> {
            openFile(stage);
        });

        ((BorderPane)(stage.getScene().getRoot())).setCenter(vbox);
    }

    public MenuBar menuLoad(Stage stage){
        MenuBar menuBar = new MenuBar();
        Menu menu1 = new Menu();
        Label l1 = new Label("File");

        menu1.setGraphic(l1);
        MenuItem menuItem1 = new MenuItem("New file...");
        MenuItem menuItem2 = new MenuItem("Open file...");
        MenuItem menuItem3 = new MenuItem("Open previous...");
        MenuItem menuItem4 = new MenuItem("Save");
        menu1.getItems().addAll(menuItem1, menuItem2, menuItem3, menuItem4);

        Menu menu4 = new Menu();

        Label l4 = new Label("Options");
        menu4.setGraphic(l4);
        menu4.getItems().add(new MenuItem("Personalization"));

        Menu menu5 = new Menu();
        Label l5 = new Label("Help");
        menu5.setGraphic(l5);
        menu5.getItems().add(new MenuItem("About (PL)"));

        menuBar.getMenus().addAll(menu1, menu4, menu5);
        menuBar.setStyle("-fx-background-color : #819dff;");

        // New file clicked
        menuItem1.setOnAction((ActionEvent e) -> {
            createFile(stage);
        });

        // Open file clicked
        menuItem2.setOnAction((ActionEvent e) -> {
            openFile(stage);
        });

        // Open previous clicked
        menuItem3.setOnAction((ActionEvent e) -> {
            openPrevious(stage);
        });

        // Save clicked
        menuItem4.setOnAction((ActionEvent e) -> {
            save(stage);
        });

        // Options clicked
        menu4.getItems().get(0).setOnAction((ActionEvent e) -> {
            options(stage);
        });

        return menuBar;
    }

    public void openFile(Stage stage){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        String fileName = selectedFile.getName();
        int i = 0;
        if (new File("src\\resources\\files\\" + fileName).exists())
            if (compareFiles(new File("src\\resources\\files\\" + fileName), selectedFile))
                i = -1;
            else
                for (i = 1; ; i++)
                    if (!new File("src\\resources\\files\\" + fileName.substring(0, fileName.indexOf(".")) + " (" + i + ")" + fileName.substring(fileName.indexOf("."))).exists())
                        break;
        switch (i) {
            case 0:
                file = new File("src\\resources\\files\\" + fileName);
                try {
                    Files.copy(selectedFile.toPath(), file.toPath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            case -1:
                file = new File("src\\resources\\files\\" + fileName);
                break;
            default:
                try {
                    file = new File("src\\resources\\files\\" + fileName.substring(0, fileName.lastIndexOf(".")) + " (" + i + ")" + fileName.substring(fileName.indexOf(".")));
                    Files.copy(selectedFile.toPath(), file.toPath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
        }
        workWithText(stage);
    }

    public void workWithText(Stage stage){
        numOfWords = 0;
        numOfRows = 0;
        percentOfWords = 5;
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            Scanner sc = new Scanner(bufferedReader);
            VBox vbox = new VBox();
            vbox.setStyle("-fx-background-color: #FFFFFF;");
            vbox.setAlignment(Pos.CENTER);
            Label top = new Label("Try to remember the text");
            top.setId("large-text");
            textVBox.setAlignment(Pos.CENTER);
            textVBox.getChildren().clear();

            boolean isFirstTime = true;

            while (sc.hasNextLine()) {
                int word = 0;
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER);
                hBox.setSpacing(10);
                String line = sc.nextLine();
                if (line.contains("endOfFile")){
                    isFirstTime = false;
                    int index = line.substring(10).indexOf(" ");
                    numOfWords = Integer.parseInt(line.substring(10, 10 + index));
                    percentOfWords = Integer.parseInt(line.substring(11 + index));
                }
                else {
                    int wordLength = 0;
                    for (int j = 0; j < line.length(); j++){
                        if (!Character.isWhitespace(line.charAt(j)) && j + 1 < line.length())
                            wordLength++;
                        else {
                            Label label = new Label(line.substring(j - wordLength, j));
                            if (j + 1 == line.length()){
                                label = new Label(line.substring(j - wordLength, j + 1));
                            }
                            label.setStyle("-fx-font-size: 15pt;");
                            hBox.getChildren().add(label);
                            wordLength = 0;
                            word++;
                        }
                    }
                }
                textVBox.getChildren().add(hBox);
                numOfRows++;
                if (isFirstTime)
                    numOfWords += word;
            }
            Label progress = new Label();
            progress.setStyle("-fx-font-size: 20pt; -fx-background-color : #b5c5ff;");
            Button buttonOk = new Button();
            buttonOk.setDefaultButton(true);
            buttonOk.setId("ok-button");
            vbox.setSpacing(30);
            vbox.getChildren().addAll(top, textVBox, progress, buttonOk);

            buttonOk.setOnAction((ActionEvent e) -> {
                top.setText("Complete the text");
                removeWords(stage, vbox);
            });

            ((BorderPane)(stage.getScene().getRoot())).setTop(menuLoad(stage));
            ((BorderPane)(stage.getScene().getRoot())).setCenter(vbox);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    public void removeWords(Stage stage, VBox vbox){
        final boolean isLastTime;
        Button buttonCheck = new Button();
        buttonCheck.setId("check-button");
        vbox.getChildren().remove(3);
        vbox.getChildren().add(buttonCheck);
        Random rand = new Random();
        int numOfWordsToRemove = percentOfWords;

        if (numOfWordsToRemove >= numOfWords){
            numOfWordsToRemove = numOfWords;
            isLastTime = true;
        }
        else isLastTime = false;
        ((Label)vbox.getChildren().get(2)).setText("  " + String.valueOf(numOfWordsToRemove) + '/' + String.valueOf(numOfWords) + "  ");

        MyNode [] nodes = new MyNode[numOfWordsToRemove];
        List<MyPoint> list = new ArrayList<>();
        List<MyPoint> listOfTextFields = new ArrayList<>();

        for (int i = 0; i < numOfWordsToRemove; i++){
            nodes[i] = new MyNode();
            int indexRow; // X
            int index;    // Y
            MyPoint point = new MyPoint(0, 0);
            a:
            while(true){
                indexRow = rand.nextInt(numOfRows);
                int numOfWordsInPerLine =((HBox)(textVBox.getChildren().get(indexRow))).getChildren().size();
                if (numOfWordsInPerLine == 0)
                    continue;
                index = rand.nextInt(numOfWordsInPerLine);
                point.setX(indexRow);
                point.setY(index);
                for (MyPoint p: list)
                    if((Integer)p.getX() == indexRow && (Integer)p.getY() == index)
                        continue a;
                break;
            }
            list.add(point);
            if (textVBox.getChildren().get(indexRow) instanceof HBox){
                if (((HBox)(textVBox.getChildren().get(indexRow))).getChildren().get(index) instanceof Label){
                    nodes[i].setIndexX(indexRow);
                    nodes[i].setIndexY(index);
                    nodes[i].setString(((Label)((HBox)(textVBox.getChildren().get(indexRow))).getChildren().get(index)).getText());
                    ((HBox)(textVBox.getChildren().get(indexRow))).getChildren().remove(index);
                    TextField textField = new TextField ();
                    textField.setStyle("-fx-control-inner-background: #b5c5ff;");
                    textField.setPrefWidth(60);
                    point = new MyPoint(indexRow * 10 + index, textField);
                    listOfTextFields.add(point);
                    textField.textProperty().addListener((ov, prevText, currText) -> {
                        Platform.runLater(() -> {
                            Text text = new Text(currText);
                            text.setFont(textField.getFont());
                            double width = text.getLayoutBounds().getWidth() + textField.getPadding().getLeft() + textField.getPadding().getRight() + 2d;
                            textField.setPrefWidth(width);
                        });
                    });
                    textField.setOnKeyPressed(keyEvent -> Platform.runLater(() -> {
                        if (keyEvent.getCode() == KeyCode.ENTER)  {
                            for (MyPoint point1 : listOfTextFields)
                                if (point1.getY() == textField){
                                    if (listOfTextFields.indexOf(point1) == listOfTextFields.size() - 1)
                                        checkWords(stage, vbox, nodes, isLastTime);
                                    else
                                        ((TextField)(listOfTextFields.get(listOfTextFields.indexOf(point1) + 1).getY())).requestFocus();
                                    break;
                                }
                        }
                    }));

                    ((HBox)(textVBox.getChildren().get(indexRow))).getChildren().add(index, textField);
                }
            }
        }
        Comparator<MyPoint> comparator = Comparator.comparingInt(p -> (Integer) p.getX());
        listOfTextFields.sort(comparator);
        buttonCheck.setOnAction((ActionEvent e) -> {
            checkWords(stage, vbox, nodes, isLastTime);
        });
    }

    public void checkWords(Stage stage, VBox vbox, MyNode [] nodes, boolean isLastTime){
        int numOfWrong = 0;
        for (MyNode node : nodes){
            if (((HBox)(textVBox.getChildren().get(node.getIndexX()))).getChildren().get(node.getIndexY()) instanceof TextField){
                if (compareTwoStrings(node.getString(), ((TextField)((HBox)(textVBox.getChildren().get(node.getIndexX()))).getChildren().get(node.getIndexY())).getText())){
                    ((HBox)(textVBox.getChildren().get(node.getIndexX()))).getChildren().get(node.getIndexY()).setStyle("-fx-control-inner-background: #cafeca;");
                }
                else{
                    numOfWrong++;
                    ((HBox)(textVBox.getChildren().get(node.getIndexX()))).getChildren().get(node.getIndexY()).setStyle("-fx-control-inner-background: #ffcbcb;");
                }
            }
        }
        if (isLastTime && numOfWrong == 0)
            endScene(stage);
        else {
            if (numOfWrong <= mistakeTolerance){
                int plus = memorisingSpeed;
                if (numOfWrong == 0) {
                    plus +=  mistakeTolerance;
                    ((Label)vbox.getChildren().get(0)).setText("Excellent!");
                }
                else {
                    ((Label)vbox.getChildren().get(0)).setText("You made " + numOfWrong + " mistakes");
                    plus =  mistakeTolerance + plus / numOfWrong ;
                    if (plus < 0) plus = 0;
                }
                percentOfWords += plus;
            }

            Button buttonOk = new Button();
            buttonOk.setId("ok-button");
            vbox.getChildren().remove(3);
            vbox.getChildren().add(buttonOk);

            buttonOk.setOnAction((ActionEvent e) -> {
                if (autosave)
                    save(stage);
                ((Label)vbox.getChildren().get(0)).setText("Complete the text");
                for (MyNode node : nodes){
                    if (((HBox)(textVBox.getChildren().get(node.getIndexX()))).getChildren().get(node.getIndexY()) instanceof TextField){
                        ((HBox)(textVBox.getChildren().get(node.getIndexX()))).getChildren().remove(node.getIndexY());
                        Label label = new Label(node.getString());
                        label.setStyle("-fx-font-size: 15pt;");
                        ((HBox)(textVBox.getChildren().get(node.getIndexX()))).getChildren().add(node.getIndexY(), label);
                    }
                }
                removeWords(stage, vbox);
            });
        }
    }


    public boolean compareTwoStrings(String firstString, String secondString){
        firstString = firstString.toLowerCase();
        secondString = secondString.toLowerCase();
        firstString = removeNonAlphanumerics(firstString);
        secondString = removeNonAlphanumerics(secondString);
        int i = LevensteinDistance.calculate(firstString, secondString);
        return i <= firstString.length()/4;
    }


    public boolean compareFiles(File firstFile, File secondFile){
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(firstFile), "UTF8"));
            Scanner sc = new Scanner(bufferedReader);
            BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(new FileInputStream(secondFile), "UTF8"));
            Scanner sc2 = new Scanner(bufferedReader2);
            while (sc.hasNextLine() && sc2.hasNextLine()){
                String line1 = sc.nextLine();
                String line2 = sc2.nextLine();
                if (line1.compareTo(line2) != 0){
                    return false;
                }
            }

            while (sc.hasNextLine()){
                String line = sc.nextLine();
                if (line.length() == 0)
                    continue;
                if (!line.contains("endOfFile"))
                    return false;
            }
            while (sc2.hasNextLine()){
                String line = sc2.nextLine();
                if (line.length() == 0)
                    continue;
                if (!line.contains("endOfFile"))
                    return false;
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        return true;
    }

    public String removeNonAlphanumerics (String string){
        String[] stringArray = string.split("\\W+");
        String result = "";
        for (int i = 0; i < stringArray.length; i++)
            result += stringArray[i];
        return result;
    }

    public void endScene(Stage stage){
        VBox vbox = new VBox();
        Image image = new Image("resources/success.gif");
        ImageView imageView = new ImageView(image);
        imageView.setScaleX(0.7);
        imageView.setScaleY(0.7);
        Label label = new Label ("Success!");
        label.setId("large-text");
        label.setStyle("-fx-font-size: 30pt;");

        Button buttonOk = new Button();
        buttonOk.setDefaultButton(true);
        buttonOk.setId("ok-button");
        vbox.setSpacing(20);
        vbox.getChildren().addAll(imageView, label, buttonOk);
        vbox.setAlignment(Pos.CENTER);

        ((BorderPane)(stage.getScene().getRoot())).setCenter(vbox);
        buttonOk.setOnAction((ActionEvent e) -> {
            startScene(stage);
        });
    }


    public void createFile(Stage stage){
        VBox vBox = new VBox();
        vBox.setStyle("-fx-background-color: #FFFFFF;");
        double width = stage.getWidth();
        double height = stage.getHeight();
        Label createText = new Label("Enter your text");
        createText.setId("large-text");

        createText.setStyle("-fx-font-size: 30pt;");
        TextArea textArea = new TextArea ();
        textArea.setMinSize(width/2.5, height/2);
        textArea.setMaxSize(width/2.5, height/2);
        textArea.setPromptText("Enter the text...");
        textArea.setStyle("-fx-control-inner-background: #d1dbff; -fx-prompt-text-fill: #000000; -fx-font-size: 18pt; " +
                "-fx-border-color: #d1dbff; -fx-border-width: 1; "
                + "-fx-border-radius: 0; -fx-focus-color: transparent");

        Button buttonCreateFile = new Button();
        buttonCreateFile.setDefaultButton(true);
        buttonCreateFile.setId("create-button");
        buttonCreateFile.setOnAction((ActionEvent e) -> {
            if (!textArea.getText().isEmpty()){
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
                fileChooser.getExtensionFilters().add(extFilter);
                File newFile = fileChooser.showSaveDialog(stage);

                if (newFile != null) {
                    try {
                        PrintWriter writer;
                        writer = new PrintWriter(newFile);
                        writer.println(textArea.getText());
                        writer.close();
                        String fileName = newFile.getName();
                        int i = 0;
                        if (new File(fileName).exists())
                            if (compareFiles(new File(fileName), newFile))
                                i = -1;
                            else
                                for (i = 1; ; i++)
                                    if (!new File(fileName + " (" + i + ")").exists())
                                        break;
                        switch (i) {
                            case 0:
                                file = new File("src\\resources\\files\\" + fileName);
                                try {
                                    Files.copy(newFile.toPath(), file.toPath());
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                break;
                            case -1:
                                file = new File("src\\resources\\files\\" + fileName);
                                break;
                            default:
                                try {
                                    file = new File("src\\resources\\files\\" + fileName.substring(0, fileName.lastIndexOf(".")) + " (" + i + ")" + fileName.substring(fileName.indexOf(".")));
                                    Files.copy(newFile.toPath(), file.toPath());
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                break;
                        }
                        workWithText(stage);
                    }
                    catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        });

        vBox.getChildren().addAll(createText, textArea, buttonCreateFile);
        vBox.setSpacing(50);
        vBox.setAlignment(Pos.CENTER);
        ((BorderPane)(stage.getScene().getRoot())).setTop(menuLoad(stage));
        ((BorderPane)(stage.getScene().getRoot())).setCenter(vBox);
    }

    public void save(Stage stage){
        List<String> fileContent = null;
        try {
            fileContent = new ArrayList<>(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean hasEndOfFile = false;
        for (int i = 0; i < fileContent.size(); i++) {
            if (fileContent.get(i).startsWith("endOfFile")) {
                System.out.println(numOfWords + " " + percentOfWords);
                fileContent.set(i, "endOfFile " + numOfWords + ' ' + percentOfWords);
                hasEndOfFile = true;
                break;
            }
        }
        if (!hasEndOfFile)
            fileContent.add("endOfFile " + numOfWords + ' ' + percentOfWords);

        try {
            Files.write(file.toPath(), fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openPrevious(Stage stage){
        Stage newStage = new Stage();
        newStage.getIcons().add(new Image("resources/brainstorm.png"));
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        newStage.setTitle("Previous imports");
        List files = new ArrayList<File>();
        File repo = new File ("src\\resources\\files");
        File[] fileList = repo.listFiles();
        int i = 0;
        for (File f : fileList)
            if (f.getPath().contains(".txt")){
                HBox hbox = new HBox();
                hbox.setAlignment(Pos.CENTER);
                if (i % 2 == 1)
                    hbox.setStyle("-fx-background-color: #E4EAFF;");
                Button button = new Button(f.getPath().substring(f.getPath().lastIndexOf("\\") + 1));
                button.setStyle("-fx-font-size: 15pt;");
                hbox.getChildren().add(button);
                vbox.getChildren().add(hbox);
                hbox.setMinHeight(stage.getHeight()/18);
                i++;
                button.setOnAction((ActionEvent e) -> {
                    newStage.close();
                    file = f;
                    workWithText(stage);
                });
            }
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, stage.getWidth()/3, stage.getHeight()/2);
        scene.getStylesheets().add(this.getClass().getResource("/stylesheet.css").toExternalForm());
        newStage.setScene(scene);
        newStage.show();
    }

    public void options(Stage stage){
        Stage newStage = new Stage();
        newStage.getIcons().add(new Image("resources/listing_option.png"));
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);

        HBox hbox1 = new HBox();
        hbox1.setAlignment(Pos.CENTER);
        Label label1 = new Label("Mistake tolerance: ");
        RadioButton radioButton1 = new RadioButton("low");
        RadioButton radioButton2 = new RadioButton("moderate");
        RadioButton radioButton3 = new RadioButton("high");
        ToggleGroup radioGroup1 = new ToggleGroup();
        radioButton1.setToggleGroup(radioGroup1);
        radioButton2.setToggleGroup(radioGroup1);
        radioButton3.setToggleGroup(radioGroup1);
        hbox1.getChildren().addAll(label1, radioButton1, radioButton2, radioButton3);

        HBox hbox2 = new HBox();
        hbox2.setAlignment(Pos.CENTER);
        Label label2 = new Label("Memorising speed: ");
        RadioButton radioButton4 = new RadioButton("low");
        RadioButton radioButton5 = new RadioButton("moderate");
        RadioButton radioButton6 = new RadioButton("high");
        ToggleGroup radioGroup2 = new ToggleGroup();
        radioButton4.setToggleGroup(radioGroup2);
        radioButton5.setToggleGroup(radioGroup2);
        radioButton6.setToggleGroup(radioGroup2);
        hbox2.getChildren().addAll(label2, radioButton4, radioButton5, radioButton6);

        CheckBox checkbox = new CheckBox("Autosave");

        radioButton1.setSelected(mistakeTolerance == 0);
        radioButton2.setSelected(mistakeTolerance == 3);
        radioButton3.setSelected(mistakeTolerance == 5);

        radioButton4.setSelected(memorisingSpeed == 1 - mistakeTolerance);
        radioButton5.setSelected(memorisingSpeed == 3 - mistakeTolerance);
        radioButton6.setSelected(memorisingSpeed == 5 - mistakeTolerance);

        checkbox.setSelected(autosave);

        newStage.setTitle("Personalization");
        Scene scene = new Scene(vbox, stage.getWidth()/4, stage.getHeight()/4);
        scene.getStylesheets().add(this.getClass().getResource("/stylesheet.css").toExternalForm());
        newStage.setScene(scene);
        newStage.show();
        Button buttonSave = new Button("Save");
        buttonSave.setDefaultButton(true);
        buttonSave.setOnAction((ActionEvent e) -> {
            newStage.close();
            if (radioButton1.isSelected())
                mistakeTolerance = 0;
            else if (radioButton2.isSelected())
                mistakeTolerance = 3;
            else if (radioButton3.isSelected())
                mistakeTolerance = 5;

            if (radioButton4.isSelected())
                memorisingSpeed = 1 - mistakeTolerance;
            else if (radioButton5.isSelected())
                memorisingSpeed = 3 - mistakeTolerance;
            else if (radioButton6.isSelected())
                memorisingSpeed = 5 - mistakeTolerance;

            if (checkbox.isSelected())
                autosave = true;

            String string = String.valueOf(mistakeTolerance) + ' ' + String.valueOf(memorisingSpeed) + ' ' +
                    (autosave ? "1" : "0");

            File fold = new File("src\\resources\\options.txt");
            fold.delete();

            File fnew = new File("src\\resources\\options.txt");
            try {
                FileWriter f2 = new FileWriter(fnew, false);
                f2.write(string);
                f2.close();
            }
            catch (IOException err) {
                err.printStackTrace();
            }
        });
        vbox.getChildren().addAll(hbox1, hbox2, checkbox, buttonSave);
    }

    public static void main(String[] args) {
        launch();
    }
}
