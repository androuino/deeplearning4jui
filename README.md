# deeplearning4jui
This is a UI test for trained model using DeepLearning4J Library.
Some part of my code and images or videos were from this [link](https://github.com/PacktPublishing/Java-Machine-Learning-for-Computer-Vision) so if you want to
get more knowledge about DeepLearning, you may want to check on this repo.

## GETTING STARTED
Assuming at least Java JDK 1.8 and JavaFX Library are installed already and you cloned or downloaded this repository.

## Prerequisites
This is a step by step process to get this code running:

**First step.** Install VLC but this is optional if the version of *libavcodec.so* you have is compatible with the version of Linux or Ubuntu machine and JavaFX Library isntalled. If not then go through this steps because we're not going to use MediaPlayer from JavaFX Library because I can't get it to work with my Ubuntu version unless it's working for you:
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
  
**Third step.** If `model` directory is not present in `resources`, go ahead and make one then add the `model.zip` file to `home/dir/deeplearningui/resources/models/` that you are going to use for testing.

**Fourth step.** Modify the `LocalData.java` and put your customized classes if you have one.

## Graphical sample of how to use it
![DL4JUI](https://github.com/androuino/deeplearning4jui/blob/master/src/main/resources/img/DL4J%20UI%202018_001.png)

*You can choose a model from specific directory by clicking the button with folder icon at the left top corner or use the pretrained model from darknet*

![DL4JUI](https://github.com/androuino/deeplearning4jui/blob/master/src/main/resources/img/DL4J%20UI%202018_002.png)

*Load your `Validation` or `Test` dataset*

![DL4JUI](https://github.com/androuino/deeplearning4jui/blob/master/src/main/resources/img/DL4J%20UI%202018_003.png)
![DL4JUI](https://github.com/androuino/deeplearning4jui/blob/master/src/main/resources/img/DL4J%20UI%202018_004.png)

*Click one picture or video you want your model to start testing object detection*

![DL4JUI](https://github.com/androuino/deeplearning4jui/blob/master/src/main/resources/img/DL4J%20UI%202018_005.png)
![DL4JUI](https://github.com/androuino/deeplearning4jui/blob/master/src/main/resources/img/DL4J%20UI%202018_006.png)

**Useful Links**

[deeplearning4j.org](http://deeplearning4j.org)

[deepearning4j library repository](https://github.com/deeplearning4j/deeplearning4j)

[deeplearning4j examples repository](https://github.com/deeplearning4j/dl4j-examples)

