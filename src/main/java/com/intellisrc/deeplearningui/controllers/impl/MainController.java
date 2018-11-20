package com.intellisrc.deeplearningui.controllers.impl;

import com.intellisrc.deeplearningui.controllers.AbstractController;
import com.intellisrc.deeplearningui.handler.ViewHandler;
import com.intellisrc.deeplearningui.util.Speed;
import com.intellisrc.deeplearningui.util.VideoPlayer;
import com.intellisrc.deeplearningui.util.Yolo;
import com.sun.jna.Memory;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import static com.intellisrc.deeplearningui.util.LocalData.COCO_CLASSES;
import static com.intellisrc.deeplearningui.util.LocalData.CUSTOMIZE_CLASSES;
import static com.intellisrc.deeplearningui.util.LocalData.TINY_COCO_CLASSES;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class MainController extends AbstractController {
    @FXML
    BorderPane bpMain;
    // buttons
    @FXML
    private Button btnNavigateYolo;
    @FXML
    private Button btnNavigateFile;
    @FXML
    private Button btnVideo;
    // choicebox
    @FXML
    private ChoiceBox<Speed> cbSpeed;
    @FXML
    private ChoiceBox<String> cbModels;
    @FXML
    private ChoiceBox<String> cbClasses;
    // imageview
    @FXML
    private ImageView imgvwMain;
    // labels
    @FXML
    private Label lblModel;
    @FXML
    private Label lblSpeed;
    // listview
    @FXML
    private ListView lvFiles;
    @FXML
    private ListView lvLogs;
    @FXML
    private ListView lvVideoList;

    // javafx
    private Pane imgvwPane;
    private FileChooser fileChooser;
    private Group group = new Group();
    private DirectoryChooser directoryChooser;
    // dl4j
    private ComputationGraph model;
    // strings
    private String modelDir;
    private String windowName;
    // lists
    private List<File> listOfImages = new ArrayList<>();
    private List<File> listOfVideos = new ArrayList<>();
    // object
    private VideoPlayer videoPlayer;
    private Yolo yolo;
    // boolean
    private volatile boolean stop = false;
    // slf4j
    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    // libraries
    private final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
    // int
    public final static AtomicInteger atomicInteger = new AtomicInteger();
    // float
    private FloatProperty videoSourceRatioProperty;
    // vlcj
    private WritablePixelFormat<ByteBuffer> pixelFormat = null;
    private WritableImage writableImage;
    private DirectMediaPlayerComponent mediaPlayerComponent;

    public MainController(ViewHandler viewhandler) {
        super(viewhandler);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        File folderPng = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "icons" + File.separator + "folder.png");
        File videoPng = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "icons" + File.separator + "video.png");
        log.info(folderPng.toString());
        addToLogs(folderPng.toString());
        Image openIcon = new Image(folderPng.toURI().toString());
        Image videoIcon = new Image(videoPng.toURI().toString());
        btnNavigateYolo.setGraphic(new ImageView(openIcon));
        btnNavigateFile.setGraphic(new ImageView(openIcon));
        btnVideo.setGraphic(new ImageView(videoIcon));
        lvFiles.setOrientation(Orientation.HORIZONTAL);

        cbSpeed.getItems().add(Speed.SLOW);
        cbSpeed.getItems().add(Speed.MEDIUM);
        cbSpeed.getItems().add(Speed.FAST);
        cbSpeed.getSelectionModel().select(0);

        cbModels.getItems().add("TinyYOLO");
        cbModels.getItems().add("YOLO2");
        cbModels.getSelectionModel().select(0);

        cbClasses.getItems().add("TINY COCO CLASSES");
        cbClasses.getItems().add("COCO CLASSES");
        cbClasses.getItems().add("CUSTOMIZE CLASSES");
        cbClasses.getSelectionModel().select(0);

        directoryChooser = new DirectoryChooser();
        fileChooser = new FileChooser();
        mouseEventListeners();
    }

    /**
     * Mouse Event Listeners
     */
    private void mouseEventListeners() {
        btnNavigateYolo.setOnMouseClicked(mouseEvent -> {
            File navDir = fileChooser.showOpenDialog(null);
            try {
                if (navDir == null) {
                    log.info("No file selected.");
                    addToLogs("No file selected.");
                } else {
                    lblModel.setText(lblModel.getText());
                    if (!navDir.getName().equals("")) {
                        modelDir = navDir.getAbsolutePath();
                        //model = ModelSerializer.restoreComputationGraph(modelDir);
                        lblModel.setText(lblModel.getText());
                        lblModel.setText(lblModel.getText() + " " + navDir.getName());
                        lblModel.setStyle("-fx-background-color: #395B5D22;");
                    } else {
                        log.info("No available model.");
                        addToLogs("No available model.");
                        lblModel.setText(lblModel.getText());
                    }
                }
            } catch (Exception e) {
                addToLogs("error in opening zip file");
                e.printStackTrace();
            }
        });

        btnNavigateFile.setOnMouseClicked(mouseEvent -> {
            File dir = directoryChooser.showDialog(bpMain.getScene().getWindow());
            try {
                displayImageToListView(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        btnVideo.setOnMouseClicked(mouseEvent -> {
            File vidFile = fileChooser.showOpenDialog(null);
            boolean isModelSelected = lblModel.getText().length() > 6;
            int what = 0;
            try {
                if (vidFile == null) {
                    log.info("No file selected.");
                    addToLogs("No file selected.");
                } else {
                    lblModel.setText(lblModel.getText());
                    if (!vidFile.getName().equals("")) {
                        videoPlayer = new VideoPlayer();
                        try {
                            addToLogs(cbSpeed.getSelectionModel().getSelectedItem().toString());
                            if (isModelSelected) { // if chosen a model trained with custom datasets
                                model = ModelSerializer.restoreComputationGraph(modelDir);
                                what = 2;
                            } else {
                                if (cbModels.getSelectionModel().isSelected(0)) { // pick the default YOLO2 model
                                    File file = new File(System.getProperty("user.dir")
                                            + File.separator
                                            + "resources"
                                            + File.separator
                                            + "models"
                                            + File.separator
                                            + "tiny-yolo-voc_dl4j_inference.v1.zip");
                                    model = ModelSerializer.restoreComputationGraph(file);
                                    what = 0;
                                } else if (cbModels.getSelectionModel().isSelected(1)) { // pick the default TinyYOLO model
                                    File file = new File(System.getProperty("user.dir")
                                            + File.separator
                                            + "resources"
                                            + File.separator
                                            + "models"
                                            + File.separator
                                            + "yolo2_dl4j_inference.v3.zip");
                                    model = ModelSerializer.restoreComputationGraph(file);
                                    what = 1;
                                }
                            }
                            int finalWhat = what;
                            /*Executors.newSingleThreadExecutor().submit(() -> {
                                try {
                                    startRealTimeVideoDetection(vidFile.getAbsolutePath(), cbSpeed.getSelectionModel().getSelectedItem(), model, finalWhat);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });*/
                            startRealTimeVideoDetection(vidFile.getAbsolutePath(), cbSpeed.getSelectionModel().getSelectedItem(), model, what);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        // TODO:
                    }
                }
            } catch (Exception e) {
                addToLogs("error in opening zip file");
                e.printStackTrace();
            }
        });

        lvFiles.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                if (lvFiles.getItems().size() > 0) {
                    int index = lvFiles.getSelectionModel().getSelectedIndex();
                    try {
                        displayImageToImageView(listOfImages.get(index));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        lvVideoList.setOnMouseClicked(mouseEvent -> {
            boolean isModelSelected = lblModel.getText().length() > 6;
            int what = 0;
            int selectedIndex = lvVideoList.getSelectionModel().getSelectedIndex();
            try {
                if (isModelSelected) { // if chosen a model trained with custom datasets
                    model = ModelSerializer.restoreComputationGraph(modelDir);
                    what = 2;
                } else {
                    if (cbModels.getSelectionModel().isSelected(0)) { // pick the default YOLO2 model
                        File fileTiny = new File(System.getProperty("user.dir")
                                + File.separator
                                + "resources"
                                + File.separator
                                + "models"
                                + File.separator
                                + "tiny-yolo-voc_dl4j_inference.v1.zip");
                        model = ModelSerializer.restoreComputationGraph(fileTiny);
                        what = 0;
                    } else if (cbModels.getSelectionModel().isSelected(1)) { // pick the default TinyYOLO model
                        File fileYolo2 = new File(System.getProperty("user.dir")
                                + File.separator
                                + "resources"
                                + File.separator
                                + "models"
                                + File.separator
                                + "yolo2_dl4j_inference.v3.zip");
                        model = ModelSerializer.restoreComputationGraph(fileYolo2);
                        what = 1;
                    }
                }
                startRealTimeVideoDetection(listOfVideos.get(selectedIndex).getAbsolutePath(), cbSpeed.getSelectionModel().getSelectedItem(), model, what);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Display image to ImageView with bounding boxes
     *
     * @param file image file to test
     * @throws IOException
     */
    private void displayImageToImageView(File file) throws IOException {
        boolean isUserPickedAModel = lblModel.getText().length() > 6;
        String[] CLASSES;

        if (isUserPickedAModel) { // if chosen a model trained with custom datasets
            model = ModelSerializer.restoreComputationGraph(modelDir);
        } else if (cbModels.getSelectionModel().isSelected(0)) { // pick the default YOLO2 model
            File fileYolo2 = new File(System.getProperty("user.dir")
                    + File.separator
                    + "resources"
                    + File.separator
                    + "models"
                    + File.separator
                    + "tiny-yolo-voc_dl4j_inference.v1.zip");
            model = ModelSerializer.restoreComputationGraph(fileYolo2);
        } else if (cbModels.getSelectionModel().isSelected(1)) { // pick the default TinyYOLO model
            File fileTiny = new File(System.getProperty("user.dir")
                    + File.separator
                    + "resources"
                    + File.separator
                    + "models"
                    + File.separator
                    + "yolo2_dl4j_inference.v3.zip");
            model = ModelSerializer.restoreComputationGraph(fileTiny);
        }

        if (cbClasses.getSelectionModel().getSelectedItem().equals("COCO CLASSES")) {
            CLASSES = COCO_CLASSES;
        } else if (cbClasses.getSelectionModel().getSelectedItem().equals("TINY COCO CLASSES")) {
            CLASSES = TINY_COCO_CLASSES;
        } else {
            CLASSES = CUSTOMIZE_CLASSES;
        }

        if (isUserPickedAModel) {
            beginDetection(cbSpeed.getSelectionModel().getSelectedItem(), file, CLASSES);
            lblModel.setStyle("-fx-background-color: #395B5D22;");
        } else {
            imgvwMain.setImage(new Image(new FileInputStream(file)));
            if (!isUserPickedAModel)
                lblModel.setStyle("-fx-background-color: red;");
            beginDetection(cbSpeed.getSelectionModel().getSelectedItem(), file, CLASSES);
            addToLogs("No Model Selected");
        }
    }

    /**
     * Begin the object detection
     *
     * @param selectedSpeed for the accuracy of network to detect an object
     * @param file image file to test
     * @param labels basically classes
     * @throws IOException
     */
    private void beginDetection(Speed selectedSpeed, File file, String[] labels) throws IOException {
        NativeImageLoader imageLoader = new NativeImageLoader();
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        Yolo2OutputLayer outputLayer = (Yolo2OutputLayer) model.getOutputLayer(0);
        BufferedImage read = ImageIO.read(file);
        INDArray indArray = prepareImage(read, selectedSpeed.width, selectedSpeed.height);
        INDArray results = model.outputSingle(indArray);
        List<DetectedObject> objs = outputLayer.getPredictedObjects(results, 0.5);
        Image imgFile = new Image(new File(file.getAbsolutePath()).toURI().toString());
        addToLogs(file.getName() + " : " + objs);

        double confidence = 0.00;
        DecimalFormat df = new DecimalFormat("#.##");

        Mat mat = imageLoader.asMat(indArray);
        Mat convertedMat = new Mat();
        mat.convertTo(convertedMat, CV_8U, 255, 0);
        int w = (int) imgFile.getWidth() * 2;
        int h = (int) imgFile.getHeight() * 2;
        Mat image = new Mat();
        resize(convertedMat, image, new Size(w, h));
        for (DetectedObject obj : objs) {
            double[] xy1 = obj.getTopLeftXY();
            double[] xy2 = obj.getBottomRightXY();
            String label = labels[obj.getPredictedClass()];
            int x1 = (int) Math.round(w * xy1[0] / 19);
            int y1 = (int) Math.round(h * xy1[1] / 19);
            int x2 = (int) Math.round(w * xy2[0] / 19);
            int y2 = (int) Math.round(h * xy2[1] / 19);
            confidence = Double.valueOf(df.format(obj.getConfidence()));
            rectangle(image, new Point(x1, y1), new Point(x2, y2), Scalar.RED);
            putText(image, label + " : " + confidence + "%", new Point(x1 + 2, y2 - 2), FONT_HERSHEY_DUPLEX, 1, Scalar.GREEN);
            addToLogs("predictedClass=" + obj.getPredictedClass());
        }
        BufferedImage bi = matToImage(image);
        imgvwMain.setImage(SwingFXUtils.toFXImage(bi, null));
    }

    /**
     * Converts/writes a Mat into a BufferedImage.
     *
     * @param mat Mat of type CV_8UC3 or CV_8UC1
     * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
     */
    private static BufferedImage matToImage(Mat mat) {
        if ( mat != null ) {
            int columns = mat.cols();
            int rows = mat.rows();
            int elementSize = (int) mat.elemSize();
            byte[] byteData = new byte[columns * rows * elementSize];
            int type;
            mat.data().get(byteData);
            switch (mat.channels()) {
                case 1:
                    type = BufferedImage.TYPE_BYTE_GRAY;
                    break;
                case 3:
                    type = BufferedImage.TYPE_3BYTE_BGR;
                    // bgr to rgb
                    byte b;
                    for(int i = 0; i< byteData.length; i=i+3) {
                        b = byteData[i];
                        byteData[i] = byteData[i+2];
                        byteData[i+2] = b;
                    }
                    break;
                default:
                    return null;
            }

            BufferedImage bi = new BufferedImage(columns, rows, type);
            bi.getRaster().setDataElements(0, 0, columns, rows, byteData);

            return bi;
        }
        return null;
    }

    /**
     * Convert image to INDArray Matrix
     *
     * @param convert BufferedImage
     * @param width Image's width
     * @param height Image's height
     * @return return INDArray
     * @throws IOException
     */
    private INDArray prepareImage(BufferedImage convert, int width, int height) throws IOException {
        NativeImageLoader loader = new NativeImageLoader(height, width, 3);
        ImagePreProcessingScaler imagePreProcessingScaler = new ImagePreProcessingScaler(0, 1);

        INDArray indArray = loader.asMatrix(convert);
        if (indArray == null) {
            return null;
        }
        imagePreProcessingScaler.transform(indArray);
        return indArray;
    }

    /**
     * Display images to ListView
     *
     * @param dir of images
     * @throws IOException
     */
    private void displayImageToListView(File dir) throws IOException {
        if (dir == null) {
            log.info("Directory is null");
            addToLogs("Directory is null");
        } else {
            lvFiles.getItems().clear();
            listOfImages.clear();
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files.length > 0) {
                    for (File file : files) {
                        if (!file.isDirectory()) {
                            if (ImageIO.read(file) != null) {
                                listOfImages.add(file);
                            } else {
                                addToLogs(file.getAbsolutePath());
                                try {
                                    listOfVideos.add(file);
                                    //lvVideoList.getChildren().add(initializeImageView(file));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                if (listOfImages.size() > 0) {
                    listOfImages.forEach(file -> {
                        lvFiles.getItems().add(createImageView(file));
                    });
                }
                // Static display
                if (listOfVideos.size() > 0) {
                    listOfVideos.forEach(file -> {
                        lvVideoList.getItems().add(displayTempVideoIcon());
                    });
                }
                // TODO: for playing video
                /*if (listOfVideos.size() > 0) {
                    listOfVideos.forEach(file -> {
                        try {
                            lvVideoList.getChildren().add(initializeImageView(file));
                            mediaPlayerComponent.getMediaPlayer().prepareMedia(file.getAbsolutePath());
                            mediaPlayerComponent.getMediaPlayer().start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }*/
            }
        }
    }

    private ImageView displayTempVideoIcon() {
        ImageView imageView = null;
        String dir = System.getProperty("user.dir") + File.separator
                + "src"
                + File.separator
                + "main"
                + File.separator
                + "resources"
                + File.separator
                + "icons"
                + File.separator
                + "videoicon.png";
        File file = new File(dir);
        try {
            final Image imageSized = new Image(new FileInputStream(file), 100, 0, true, true);
            imageView = new ImageView(imageSized);
            imageView.setFitWidth(75);
            imageView.setFitHeight(50);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return imageView;
    }

    /**
     * Custom Logging
     *
     * @param msg to display
     */
    public void addToLogs(String msg) {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String formattedDate = df.format(date);
        lvLogs.getItems().add(new Label("DL4J UI " + formattedDate + " $: " +msg));
        int index = lvLogs.getItems().size();
        lvLogs.scrollTo(index);
    }

    /**
     * Create an ImageView object
     *
     * @param imageFile image file
     * @return an ImageView object
     */
    private ImageView createImageView(final File imageFile) {
        imgvwMain.setImage(null);
        double width = 70;
        ImageView imageView = null;
        try {
            final Image imageSized = new Image(new FileInputStream(imageFile), width, 0, true, true);
            imageView = new ImageView(imageSized);
            imageView.setFitWidth(width);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return imageView;
    }

    public void startRealTimeVideoDetection(String videoFileName, Speed selectedIndex, ComputationGraph yoloModel, int what) throws java.lang.Exception {
        log.info("Start detecting video " + videoFileName);
        Platform.runLater(() -> addToLogs("Start detecting video " + videoFileName) );
        stop = false;
        yolo = new Yolo(selectedIndex, yoloModel, what);
        startYoloThread();
        runVideoMainThread(videoFileName, converter);
    }

    private void runVideoMainThread(String videoFileName, OpenCVFrameConverter.ToMat toMat) throws FrameGrabber.Exception {
        Platform.setImplicitExit(false);
        FFmpegFrameGrabber grabber = initFrameGrabber(videoFileName);
        while (!stop) {
            Frame frame = grabber.grab();
            if (frame == null) {
                log.info("Stopping");
                addToLogs("Stopping");
                stop();
                break;
            }
            if (frame.image == null) {
                continue;
            }
            yolo.push(frame);
            Mat mat = toMat.convert(frame);
            yolo.drawBoundingBoxesRectangles(frame, mat);
            BufferedImage bi = matToImage(mat);
            Image image = SwingFXUtils.toFXImage(bi, null);
            imgvwMain.setImage(image);

            char key = (char) waitKey(20);
            // Exit this loop on escape:
            if (key == 27) {
                stop();
                grabber.stop();
                break;
            }
        }
    }

    private FFmpegFrameGrabber initFrameGrabber(String videoFileName) throws FrameGrabber.Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFileName);
        grabber.start();
        return grabber;
    }

    private void startYoloThread() {
        Platform.setImplicitExit(false);
        Thread thread = new Thread(() -> {
            while (!stop) {
                try {
                    yolo.predictBoundingBoxes();
                } catch (Exception e) {
                    //ignoring a thread failure
                    //it may fail because the frame may be long gone when thread get chance to execute
                }
            }
            yolo = null;
            log.info("YOLO Thread Exit");
            Platform.runLater(() -> {
                addToLogs("Video Finished");
            });
        });
        thread.start();
    }

    public void stop() {
        if (!stop) {
            stop = true;
            //destroyAllWindows();
        }
    }


    /**
     * ***************************************************************
     * ***************************************************************
     * ************************ INNER CLASSES ************************
     * ***************************************************************
     * ***************************************************************
     */

    /**
     * @return
     * @throws IOException
     */
    private ImageView initializeImageView(File file) throws IOException {
        imgvwPane = new Pane();
        mediaPlayerComponent = new CanvasPlayerComponent();
        mediaPlayerComponent.getMediaPlayer().prepareMedia(file.getAbsolutePath());
        mediaPlayerComponent.getMediaPlayer().start();
        videoSourceRatioProperty = new SimpleFloatProperty(0.4f);
        pixelFormat = PixelFormat.getByteBgraInstance();

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        writableImage = new WritableImage((int) visualBounds.getWidth(), (int) visualBounds.getHeight());

        ImageView imageView = new ImageView(writableImage);
        imageView.setFitWidth(100);
        imageView.setFitHeight(75);
        //group.getChildren().add(imageView);
        //imgvwPane.getChildren().add(imageView);
        return imageView;
    }

    class CanvasPlayerComponent extends DirectMediaPlayerComponent {
        public CanvasPlayerComponent() {
            super(new CanvasBufferFormatCallback());
        }

        PixelWriter pixelWriter = null;

        private PixelWriter getPW() {
            if (pixelWriter == null) {
                pixelWriter = writableImage.getPixelWriter();
            }
            return pixelWriter;
        }

        @Override
        public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
            if (writableImage == null) {
                return;
            }
            Platform.runLater(() -> {
                Memory nativeBuffer = mediaPlayer.lock()[0];
                try {
                    ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
                    getPW().setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
                } finally {
                    mediaPlayer.unlock();
                }
            });
        }
    }

    class CanvasBufferFormatCallback implements BufferFormatCallback {
        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            Platform.runLater(() -> videoSourceRatioProperty.set((float) sourceHeight / (float) sourceWidth));
            return new RV32BufferFormat((int) visualBounds.getWidth(), (int) visualBounds.getHeight());
        }
    }
}