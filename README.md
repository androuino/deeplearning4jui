# deeplearning4jui
This is a UI test for trained model in DeepLearning4J Library.

## GETTING STARTED
Assuming at least Java JDK 1.8 is installed already and you cloned or downloaded this reposority.

## Prerequisites
This is a step by step process to get this code running:

**First step.** Install VLC but this is optional if libavcodec.so is compatible with the version of Linux or Ubuntu machine and JavaFX Library. If not then go through this steps:
  > sudo aptitude
  
  > then search for vlc and install

**Second step.** Add this to your `build.gradle` file if you are building with gradle:
  ```
  // dl4j
  compile group: 'org.nd4j', name: 'nd4j-native-platform', version: '1.0.0-beta2'
  compile group: 'org.deeplearning4j', name: 'deeplearning4j-core', version: '1.0.0-beta2'
  compile group: 'org.deeplearning4j', name: 'deeplearning4j-nlp', version: '1.0.0-beta2'
  compile group: 'org.datavec', name: 'datavec-api', version: '1.0.0-beta2'
  compile group: 'org.deeplearning4j', name: 'deeplearning4j-zoo', version: '1.0.0-beta2'
  // logs
  compile group: 'org.slf4j', name: 'slf4j-api', version: '1.7.25'
  compile group: 'org.slf4j', name: 'slf4j-log4j12', version: '1.7.25'
  // bytedeco
  compile group: 'org.bytedeco.javacpp-presets', name: 'ffmpeg', version: '4.0.2-1.4.3'
  compile group: 'org.bytedeco.javacpp-presets', name: 'ffmpeg-platform', version: '4.0.2-1.4.3'
  // vlcj
  compile group: 'uk.co.caprica', name: 'vlcj', version: '3.10.1'
  ```
  
**Third step.** Add the `model.zip` file to `home/dir/deeplearningui/resources/models/` that you are going to use for testing.

**Fourth step.** Modify the `LocalData.java` and put your customized classes if you have one.

## Graphical sample of how to use the program
![DL4JUI](https://github.com/androuino/deeplearning4jui/blob/master/src/main/resources/img/DL4J%20UI%202018_001.png)
![DL4JUI](https://github.com/androuino/deeplearning4jui/blob/master/src/main/resources/img/DL4J%20UI%202018_002.png)
![DL4JUI](https://github.com/androuino/deeplearning4jui/blob/master/src/main/resources/img/DL4J%20UI%202018_003.png)
![DL4JUI](https://github.com/androuino/deeplearning4jui/blob/master/src/main/resources/img/DL4J%20UI%202018_004.png)
![DL4JUI](https://github.com/androuino/deeplearning4jui/blob/master/src/main/resources/img/DL4J%20UI%202018_005.png)
![DL4JUI](https://github.com/androuino/deeplearning4jui/blob/master/src/main/resources/img/DL4J%20UI%202018_006.png)