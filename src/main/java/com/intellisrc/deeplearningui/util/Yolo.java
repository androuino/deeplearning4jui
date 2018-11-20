package com.intellisrc.deeplearningui.util;

import com.intellisrc.deeplearningui.handler.ViewHandler;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer;
import org.deeplearning4j.nn.layers.objdetect.YoloUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static com.intellisrc.deeplearningui.util.LocalData.COCO_CLASSES;
import static com.intellisrc.deeplearningui.util.LocalData.CUSTOMIZE_CLASSES;
import static com.intellisrc.deeplearningui.util.LocalData.TINY_COCO_CLASSES;
import static org.bytedeco.javacpp.opencv_core.FONT_HERSHEY_DUPLEX;
import org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

public class Yolo implements ViewHandler {
    // strings
    private static final Logger log = LoggerFactory.getLogger(Yolo.class);
    private String msg = "";
    // double
    private static final double DETECTION_THRESHOLD = 0.5;
    // object
    private final Speed selectedSpeed;
    private final Stack<Frame> stack = new Stack();
    // list
    private volatile List<DetectedObject> predictedObjects;
    private HashMap<Integer, String> map;
    private HashMap<String, String> groupMap;
    // dl4j
    private ComputationGraph model;

    public Yolo(Speed selectedSpeed, ComputationGraph yolo, int what) throws IOException {
        this.selectedSpeed = selectedSpeed;

        switch (what) {
            case 0:
                //prepareCustomizeYoloLabels();
                prepareTinyYOLOLabels();
                break;
            case 1:
                prepareYOLOLabels();
                break;
            case 2:
                prepareLabels(CUSTOMIZE_CLASSES);
                break;
        }
        model = yolo;
        warmUp(selectedSpeed);
    }

    private void warmUp(Speed selectedSpeed) throws IOException {
        Yolo2OutputLayer outputLayer = (Yolo2OutputLayer) model.getOutputLayer(0);
        BufferedImage read = ImageIO.read(new File(System.getProperty("user.dir")
                + File.separator
                + "src"
                + File.separator
                + "main"
                + File.separator
                + "resources"
                + File.separator
                + "img"
                + File.separator
                + "sample.jpg"));
        INDArray indArray = prepareImage(read, selectedSpeed.width, selectedSpeed.height);
        INDArray indArrayResults = model.outputSingle(indArray);
        outputLayer.getPredictedObjects(indArrayResults, DETECTION_THRESHOLD);
    }

    public void push(Frame matFrame) {
        stack.push(matFrame);
    }

    private INDArray prepareImage(Frame frame, int width, int height) throws IOException {
        if (frame == null || frame.image == null) {
            return null;
        }
        BufferedImage convert = new Java2DFrameConverter().convert(frame);
        return prepareImage(convert, width, height);
    }

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

    private void prepareYOLOLabels() {
        prepareLabels(COCO_CLASSES);
    }

    private void prepareTinyYOLOLabels() {
        prepareLabels(TINY_COCO_CLASSES);
    }

    private void prepareCustomizeYoloLabels() {
        prepareLabels(CUSTOMIZE_CLASSES);
    }

    private void prepareLabels(String[] cocoClasses) {
        if (map == null) {
            groupMap = new HashMap<>();
            groupMap.put("bridgestone", cocoClasses[0]);
            groupMap.put("dunlop", cocoClasses[1]);
            groupMap.put("goodyear", cocoClasses[2]);
            groupMap.put("yokohama", cocoClasses[3]);
            int i = 0;
            map = new HashMap<>();
            for (String s1 : cocoClasses) {
                map.put(i++, s1);
                groupMap.putIfAbsent(s1, s1);
            }
        }
    }

    public void drawBoundingBoxesRectangles(Frame frame, Mat matFrame) {
        if (invalidData(frame, matFrame)) return;

        ArrayList<DetectedObject> detectedObjects = new ArrayList<>(predictedObjects);
        YoloUtils.nms(detectedObjects, 0.5);
        for (DetectedObject detectedObject : detectedObjects) {
            createBoundingBoxRectangle(matFrame, frame.imageWidth, frame.imageHeight, detectedObject);
        }

    }

    private boolean invalidData(Frame frame, Mat matFrame) {
        return predictedObjects == null || matFrame == null || frame == null;
    }

    public void predictBoundingBoxes() throws IOException {
        long start = System.currentTimeMillis();
        Yolo2OutputLayer outputLayer = (Yolo2OutputLayer) model.getOutputLayer(0);
        INDArray indArray = prepareImage(stack.pop(), selectedSpeed.width, selectedSpeed.height);
        log.info("stack of frames size " + stack.size());
        logsBus("stack of frames size " + stack.size());
        if (indArray == null) {
            return;
        }

        INDArray indArrayResults = model.outputSingle(indArray);
        if (indArrayResults == null) {
            return;
        }
        predictedObjects = outputLayer.getPredictedObjects(indArrayResults, DETECTION_THRESHOLD);

        log.info("stack of predictions size " + predictedObjects.size());
        logsBus("stack of predictions size " + predictedObjects.size());
        log.info("Prediction time " + (System.currentTimeMillis() - start) / 1000d);
        logsBus("Prediction time " + (System.currentTimeMillis() - start) / 1000d);
    }

    private void createBoundingBoxRectangle(Mat file, int w, int h, DetectedObject obj) {
        double[] xy1 = obj.getTopLeftXY();
        double[] xy2 = obj.getBottomRightXY();
        int predictedClass = obj.getPredictedClass();
        int x1 = (int) Math.round(w * xy1[0] / selectedSpeed.gridWidth);
        int y1 = (int) Math.round(h * xy1[1] / selectedSpeed.gridHeight);
        int x2 = (int) Math.round(w * xy2[0] / selectedSpeed.gridWidth);
        int y2 = (int) Math.round(h * xy2[1] / selectedSpeed.gridHeight);
        rectangle(file, new Point(x1, y1), new Point(x2, y2), Scalar.RED);
        putText(file, groupMap.get(map.get(predictedClass)), new Point(x1 + 2, y2 - 2), FONT_HERSHEY_DUPLEX, 0.5, Scalar.GREEN);
    }

    @Override
    public void launchMainScene() throws IOException {

    }

    @Override
    public void logsBus(String msg) {
        this.msg = msg;
    }

    @Override
    public String getTransportedMsg() {
        return null;
    }
}
