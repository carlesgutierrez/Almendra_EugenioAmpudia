import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.awt.PSurfaceAWT; 
import processing.video.*; 
import java.util.concurrent.CopyOnWriteArrayList; 
import spout.*; 
import oscP5.*; 
import netP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Gato_P5_BlobsTrackedYoloData extends PApplet {

/**
 * Fachada YoloData Client for an Artistic project of Eugenio Ampudia "Almendra" 
 * receiver osc messages and play cat animations with following some kind of logic
 * Code by Carles Gutiérrez. Videos by Jaime  
 */




PFont f;

int widthDesiredScale = 192;
int heightDesiredScale = 157;
float scaleRawSize = 0.3f; //TODO find the real relation between VideoCamera dims and Screen Final
Boolean bDrawInfo = false;

Boolean bBackgroundAlpha = false;
int alphaBk = 200;

//cat vars 
//boolean bAllVideosPlayedStarted = false;
//float millisPlayingStart = 1000;
//float millisPlaying= 0;
Movie movCatIdle;
Movie movCatLlepa;
Movie movCatLook;
Movie movCatScrath;
Movie movCatHunter;
Boolean bShowRectDims = false;
int dFix = 30;
int catId = 1;
int winPosX = -3;
int winPosY = -26;

int prioAvailableId = 0;
int thresholdLongPeople = 100;
int thresholdMinPeople4Action = 1;


//Spout
// IMPORT THE SPOUT LIBRARY

PGraphics pgrSpout; // Canvas to receive a texture
PImage imgSpout; // Image to receive a texture
Spout spout; // DECLARE A SPOUT OBJECT

//-----------------------------
public void showCredits(int _fx, int _initY, int _gapY) {
  int fx = _fx;
  int fy = _initY;
  push();

  text("Almendra, de Eugenio Ampudia", 192*.5f + fx, 168*.5f+fy);  
  fy += _gapY;
  text("Producción: Biphaus", 192*.5f + fx, 168*.5f+fy);
  fy += _gapY;
  text("Biphaus Prod: Javier Bonilla", 192*.5f + fx, 168*.5f+fy);
  fy += _gapY;
  text("Biphaus Anim: Jaime Esteban", 192*.5f + fx, 168*.5f+fy);
  fy += _gapY;
  text("Biphaus Post: Cristian Alarcón", 192*.5f + fx, 168*.5f+fy);
  fy += _gapY;
  text("Software: Carles Gutiérrez", 192*.5f + fx, 168*.5f +fy);
  fy += _gapY;

  pop();
}

//-------------------------------
public void setup() {

  
  background(0);

  //Window properties
  PSurfaceAWT awtSurface = (PSurfaceAWT)surface;
  PSurfaceAWT.SmoothCanvas smoothCanvas = (PSurfaceAWT.SmoothCanvas)awtSurface.getNative();
  smoothCanvas.getFrame().setAlwaysOnTop(true);
  smoothCanvas.getFrame().removeNotify();
  smoothCanvas.getFrame().setUndecorated(true);
  smoothCanvas.getFrame().setLocation(0, 0);//2560
  smoothCanvas.getFrame().addNotify();
  showCredits(-43, 7, 15);
  //fullScreen();
  
  //Usefull to use color blobs ids
  colorMode(HSB, 360, 255, 255);

  frameRate(30);

  // Create the font
  //printArray(PFont.list());
  f = createFont("SourceCodePro-Regular.ttf", 24);
  textFont(f);

  //textSize(8);
  //showCredits(-43, 20, 20);
  
  textSize(24);

  setup_clientSensor4Games();
  setup_clientSpout();

  //Cats
  setupCat();
  
}

//------------------------------------
public void setup_clientSpout() {
  // Create a canvas or an image to receive the data.
  //pgrSpout = createGraphics(width, height, PConstants.P2D);
  //imgSpout = createImage(width, height, ARGB);
  // CREATE A NEW SPOUT OBJECT
  //spout = new Spout(this);
  //spout.createReceiver("Camera");
}

//-----------------------------------
public void updateSpout() {

  // OPTION 1: Receive and draw the texture
  //spout.receiveTexture();

  // OPTION 2: Receive into PGraphics texture
  // pgr = spout.receiveTexture(pgr);
  // image(pgr, 0, 0, width, height);

  // OPTION 3: Receive into PImage texture
  // img = spout.receiveTexture(img);
  // image(img, 0, 0, width, height);

  // OPTION 4: Receive into PImage pixels
  //imgSpout = spout.receivePixels(imgSpout);
  //image(imgSpout, 0, 0, width, height);
}

//___________________________________
public void setupCat() {

  movCatIdle = new Movie(this, "output1.mov");//"MOV_1_v1.mp4");
  movCatIdle.loop();
  movCatIdle.speed(1);

  movCatLlepa = new Movie(this, "output2.mov");//"MOV_2_v1.mp4");
  movCatLlepa.loop();
  movCatLlepa.speed(1);

  movCatLook = new Movie(this, "output3.mov");//"MOV_3_v1.mp4");
  movCatLook.loop();
  movCatLook.speed(1);

  movCatScrath = new Movie(this, "output4.mov");//"MOV_4_v1.mp4");
  movCatScrath.loop();
  movCatScrath.speed(1);

  movCatHunter = new Movie(this, "output5.mov");//"MOV_4_v1.mp4");
  movCatHunter.loop();
  movCatHunter.speed(1);

  //playStopAllMoviesOnce();
}

//----------------------------------
public void updateMainCatLogic() {
  if (catId == 1) {
    float md2 = movCatIdle.duration();
    float mt2 = movCatIdle.time();
    float pctThreshold = md2/1.01f;
    if (mt2 >= pctThreshold) {
      if (prioAvailableId > 0 ) {
        //Check this prio a bit random
        float p = random(1);
        if (prioAvailableId == 2 && p>0.8f) { //low p
          playLlepaCat();
          prioAvailableId = 0; // reset prio
          println("end idle -> lets llepa");
        } else if (prioAvailableId == 2 && p<0.1f) {//very low p
          //playLookCat();
          //prioAvailableId = 0; 
          //println("end idle -> llepa but Look");
          println("end idle -> would Look");
        } else if (prioAvailableId == 3 && p<0.5f) {//regular p
          playLookCat();
          prioAvailableId = 0; 
          println("end idle -> lets Look");
        } else if (prioAvailableId == 5) {//p same as 4
          playHunterCat();
          prioAvailableId = 0; 
          println("end idle -> lets Hunt");
        } else if (prioAvailableId == 4) {//p same as 5
          playScrathCat();
          prioAvailableId = 0; 
          println("end idle -> lets Scrath");
        } else { 
          //do nothing
          println("keep idle");
        }
      }
    }

    //TODO add may be look or
  }
}

//----------------------------------
public void updateCatsLogics() {

  //updatePlayStopAllMoviesOnce();//Just fisrt sec

  updateMainCatLogic(); //(idle) 1 -> 2,3,4,5

  //2,3,4,5 -> 1(idle)
  if (catId == 2) {
    float md2 = movCatLlepa.duration();
    float mt2 = movCatLlepa.time();
    float pctThreshold = md2/1.01f;
    if (mt2 >= pctThreshold) {
      movCatLlepa.stop();
      movCatIdle.play();//reset to Idle
      catId = 1;//reset to Idle
      println(pctThreshold);
      println("end LLepa");
    }

    //TODO add may be look or
  }

  if (catId == 3) {
    float md2 = movCatLook.duration();
    float mt2 = movCatLook.time();
    float pctThreshold = md2/1.01f;
    if (mt2 >= pctThreshold) {
      println("mt2 ="+str(mt2));
      println(pctThreshold);
      if (true) {
        movCatLook.stop();
        movCatIdle.play();//reset to Idle
        movCatIdle.jump(0);
        catId = 1;
        println("end Look");
      }
    }
  }

  if (catId == 4) {
    float md2 = movCatScrath.duration();
    float mt2 = movCatScrath.time();
    float pctThreshold = md2/1.01f;
    if (mt2 >= pctThreshold) {
      movCatScrath.stop();
      movCatIdle.play();//reset to Idle
      movCatIdle.jump(0);
      catId = 1;
      println("mt2 ="+str(mt2));
      println(pctThreshold);
      println("end Scrath");
    }
  }

  if (catId == 5) {
    float md2 = movCatHunter.duration();
    float mt2 = movCatHunter.time();
    float pctThreshold = md2/1.01f;
    if (mt2 >= pctThreshold) {
      movCatHunter.stop();
      movCatIdle.play();//reset to Idle
      movCatIdle.jump(0);
      catId = 1;
      println("mt2 ="+str(mt2));
      println(pctThreshold);
      println("end Hunt");
    }
  }
}

//-----------------------------------
public void drawCat()
{

  updateCatsLogics();

  push();
  imageMode(CENTER);

  float mapScale = 1; //map(mouseX, 0, width, 1, 3);


  if (catId == 1)image(movCatIdle, 192*.5f, 157*.5f - dFix, 192*mapScale, 157*mapScale);
  if (catId == 2)image(movCatLlepa, 192*.5f, 157*.5f - dFix, 192*mapScale, 157*mapScale);
  if (catId == 3)image(movCatLook, 192*.5f, 157*.5f - dFix, 192*mapScale, 157*mapScale);
  if (catId == 4)image(movCatScrath, 192*.5f, 157*.5f - dFix, 192*mapScale, 157*mapScale);
  if (catId == 5)image(movCatHunter, 192*.5f, 157*.5f - dFix, 192*mapScale, 157*mapScale);

  if (bShowRectDims) { //Just a visual ref of the video, lets set this False
    noFill();
    stroke(255);
    rect(0, - dFix, 192, 157);
  }

  pop();
}

public void movieEvent(Movie movie) {
  if (catId == 1)movCatIdle.read();
  else if (catId == 2)movCatLlepa.read();
  else if (catId == 3)movCatLook.read();
  else if (catId == 4)movCatScrath.read();
  else if (catId == 5)movCatHunter.read();
}

//-----------------------------------
public void drawSpout() {
  //image(imgSpout, 0, 0, width, height);
}

//-----------------------------------
public void draw() {

  //Update
  update_blobsInfo();

  //drawgins
  background(0);

  //Text info
  fill(255);
  showCredits(-100, 195, 21);
  //text("Gato by Eugenio Ampudia", 0, height-0.15*height);
  //text("Software by @carlesgutierrez", 0, height-0.1*height);
  text("fps["+str((int)frameRate)+"]", 20, 20);

  pushMatrix();
  translate(40, 40 + 32);
  noFill();
  stroke(255, 0, 0);

  //Draw our cat
  drawCat();

  fill(0, 255, 0);
  draw_clientSensor4Games(widthDesiredScale, heightDesiredScale - 32, scaleRawSize, bDrawInfo);



  popMatrix();

  if (bShowRectDims) {
    strokeWeight(1);
    stroke(0, 255, 255); //RGB Contour Color. https://processing.org/reference/stroke_.html
    drawFacadeContourInside(); //Facade Contour
  }
}

//-----------------------------------
public void drawFacadeContourInside()
{

  //left line
  line(40, 72, 40, 196);

  //bottom
  line(40, 196, 231, 196);

  //right side
  line(231, 72, 231, 196);

  // steps
  //flat left
  line(40, 72, 76, 72);

  //vert
  line(76, 72, 76, 56);

  // hor
  line(76, 56, 112, 56);

  //vert
  line(112, 56, 112, 40);

  //top
  line(112, 40, 159, 40);

  //vert right side
  line(159, 40, 159, 56);

  //hors
  line(160, 56, 195, 56);

  //  vert
  line(195, 56, 195, 72);

  //hor
  line(196, 72, 231, 72);
}

public void mousePressed() {
  bDrawInfo = true;
}

public void mouseReleased() {
  bDrawInfo = false;
}


public void keyPressed() {

  if (keyCode==BACKSPACE)bDrawInfo = !bDrawInfo;
  if (key == 'b' || key == 'B')bBackgroundAlpha = !bBackgroundAlpha;
  if (keyCode == LEFT)alphaBk += 10; 
  if (alphaBk>255) alphaBk = 255;
  if (keyCode == RIGHT)alphaBk -= 10; 
  if (alphaBk<1) alphaBk = 1;

  if (key == 'r') {
    bShowRectDims = !bShowRectDims;
  } else if (key == '+') {
    dFix++;
    print("dFixY = "+str(dFix)+ " ");
  } else if (key == '-') {
    dFix--;
    print("dFixY = "+str(dFix)+ " ");
  }

  if (key == '1')playIdleCat();
  if (key == '2')playLlepaCat();
  if (key == '3')playLookCat();
  if (key == '4')playScrathCat();
  if (key == '5')playHunterCat();

  if (key == '1' || key == '2' ||key == '3' ||key == '4' || key == '5')println("Actual Cat is "+str(catId));
}

//-----------------------
public void playIdleCat() {   
  catId = 1; 
  movCatIdle.play();
  movCatIdle.jump(0); 
  movCatLlepa.stop(); 
  movCatLook.stop(); 
  movCatScrath.stop();
  movCatHunter.stop();
}

//-----------------------
public void playLlepaCat() {
  catId = 2; 
  movCatIdle.stop(); 
  movCatLlepa.play();
  movCatLlepa.jump(0);
  movCatLook.stop(); 
  movCatScrath.stop();
  movCatHunter.stop();
}


//-----------------------
public void playLookCat() {
  catId = 3; 
  movCatIdle.stop(); 
  movCatLlepa.stop(); 
  movCatLook.play();
  movCatLook.jump(0); 
  movCatScrath.stop();
  movCatHunter.stop();
}

//-----------------------
public void playScrathCat() {
  catId = 4; 
  movCatIdle.stop(); 
  movCatLlepa.stop(); 
  movCatLook.stop(); 
  movCatScrath.play();
  movCatScrath.jump(0);
  movCatHunter.stop();
}

//-----------------------
public void playHunterCat() {
  catId = 5; 
  movCatIdle.stop(); 
  movCatLlepa.stop(); 
  movCatLook.stop(); 
  movCatScrath.stop();
  movCatHunter.play();
  movCatHunter.jump(0);
}

//void updatePlayStopAllMoviesOnce(){
//  if(millis() - millisPlaying> millisPlayingStart && !bAllVideosPlayedStarted){
//    movCatIdle.stop(); 
//    movCatLlepa.stop(); 
//    movCatLook.stop(); 
//    movCatScrath.stop();
//    movCatHunter.stop();
//    bAllVideosPlayedStarted = true;
//    println("All videos are ready to play");
//  }
//}
//void playStopAllMoviesOnce() {

//  movCatIdle.play();
//  movCatLlepa.play();
//  movCatLook.play();  
//  movCatScrath.play(); 
//  movCatHunter.play(); 

//  millisPlaying = millis();
//}
class YoloBlob { //<>//
  float xPos;
  float yPos;
  int id; 
  int time; 
  //float probability;
  float wRawBlob;
  float hRawBlob;
  int statusActionH = 0; //0 is regular, -1 is down, 1 is up
  int statusActionW = 0; //0 is regular, -1 is thin, 1 is thick

  // Constructor
  YoloBlob() {
    xPos = -1;
    yPos = -1;
    id = -1; //Id will be the order received or ID from tracking
    time = -1; //time -1 if not tracking
    wRawBlob = 0;
    hRawBlob = 0;
    //TODO add Action Detected
    statusActionH = 0; //0 is regular, -1 is down, 1 is up
    statusActionW = 0; //0 is regular, -1 is thin, 1 is thick
  }

  // Custom method for updating the variables
  public void updateOSC() {
  }

  //------------------------------
  public void displayBlobInfo(int w, int h) {
    int deltaX = -40;
    int deltaY = -30; 
    text("["+str(id)+"]"+"("+str(time)+")", xPos*w+deltaX, yPos*h*1+deltaY);
  }

  //-------------------------------------------------
  // Custom method for drawing the object
  public void displayRandomColorRect(int w, int h, float _scaleRawDims) {

    textAlign(LEFT);
    //Draw Received Blob. Probability = quality person detection.
    int idColor = id*100 % 255;
    fill(idColor, 255, 255, 200);
    //stroke(255, 255, 255, 255);
    noStroke();
    strokeWeight(2);  // Thicker
    if (_scaleRawDims>0) {
      rectMode(CENTER);  // Set rectMode to CENTER
      rect(xPos*w, yPos*h, wRawBlob*_scaleRawDims, hRawBlob*_scaleRawDims);
    } else ellipse(xPos*w, yPos*h, 50, 50);
  }

  //-------------------------------------------------------
  public void displaySpoutRects(int w, int h, float _scaleRawDims) {
    textAlign(LEFT);
    //Draw Received Blob. Probability = quality person detection.
    int idColor = id*100 % 255;
    fill(idColor, 255, 255, 200);
    //stroke(255, 255, 255, 255);
    noStroke();
    strokeWeight(2);  // Thicker
    if (_scaleRawDims>0) {
      rectMode(CENTER);  // Set rectMode to CENTER
      rect(xPos*w, yPos*h, wRawBlob*_scaleRawDims, hRawBlob*_scaleRawDims);
    } else ellipse(xPos*w, yPos*h, 50, 50);
  }

  //-------------------------------------------------
  public void displayYoloRects(int w, int h, float _scaleRawDims) {

    pushStyle();

    int idColorRed = 0; // Red Hue color
    int idColorCyan = 180; // Cyan Hue color
    int idColor = id*100 % 255; // Using Id to set a Hue color

    noFill();

    //Full Rect detected and tracked
    stroke(idColor, 255, 255, 200);
    strokeWeight(1);
    rectMode(CORNER);  // Set rectMode to CENTER
    rect(xPos*w, yPos*h, wRawBlob*_scaleRawDims, hRawBlob*_scaleRawDims);

    // W Status actions feedback
    strokeWeight(3);  
    //width status
    if (statusActionW < 0)stroke(idColorRed, 255, 255, 200);
    else if (statusActionW > 0)stroke(idColorCyan, 255, 255, 200);
    else {
      strokeWeight(1);  // 
      stroke(idColorCyan, 0, 255, 200);
    }
    line(xPos*w, yPos*h, xPos*w+wRawBlob*_scaleRawDims, yPos*h);

    // H Status actions feedback
    strokeWeight(3);  
    if (statusActionH < 0)stroke(idColorRed, 255, 255, 200);
    else if (statusActionH > 0)stroke(idColorCyan, 255, 255, 200);
    else {
      strokeWeight(1); 
      stroke(idColorCyan, 0, 100, 200);
    }
    line(xPos*w, yPos*h, xPos*w, yPos*h+hRawBlob*_scaleRawDims);

    //TODO 
    //Draw Spout here
    popStyle();
  }

  //-------------------------------------------------
  public void displayAsPongUsers(int w, int h, float _scaleRawDims) {
    fill(200);
    stroke(200);
    //designed for cat mode. 2 rects, one per axis X, Y
    if (true) {
      rect(xPos*w, 1*h - 6, 15, 5);
      rect(0*w, yPos*h, 5, 15);
    } else {
      rect(xPos*w, 1*h, wRawBlob*_scaleRawDims, 5);
      rect(0*w, yPos*h, 5, hRawBlob*_scaleRawDims);
    }
  }

  //-------------------------------------------------
  public void displayRandomColorCircles(int w, int h, float _scaleRawDims) {
    int idColor = id*100 % 255; // Using Id to set a Hue color
    fill(idColor, 255, 255, 200);
    noStroke();
    strokeWeight(2);  // Thicker
    //Using time to control the size of the circle
    float radioCircle = map(time, 0, 200, 2, h*0.5f);

    ellipseMode(CENTER);  // Set rectMode to CENTER
    ellipse(xPos*w, yPos*h, radioCircle, radioCircle);
  }

  //-------------------------------------------------
  public void displayActionCircles(int w, int h, float _scaleRawDims) {

    int idColor = id*100 % 255; // Using Id to set a Hue color
    fill(idColor, 255, 255, 200);
    noStroke();
    strokeWeight(2);  // Thicker
    //Using time to control the size of the circle
    float radioCircle = map(time, 0, 200, 2, h*0.5f);

    ellipseMode(CENTER);  // Set rectMode to CENTER
    ellipse(xPos*w, yPos*h, radioCircle, radioCircle);
  }
}//YoloBlob end class




OscP5 oscP5;
NetAddress myRemoteLocation;

ArrayList<YoloBlob> blobs= new ArrayList<YoloBlob>();


//-------------------------------
public void setup_clientSensor4Games() {
  //setup OSC
  oscP5 = new OscP5(this, 12345);
  //myRemoteLocation = new NetAddress("127.0.0.1", 12345);

  blobs.clear();
}

//-----------------------------
public void update_blobsInfo() {
  
  boolean bBodyInteractionPeople = false;//detect body interaction people
  boolean bNewPeople = false;
  boolean bLongTimePeople = false;
  boolean bRightPeople = false;
  boolean bLeftPeople = false;
  boolean bUpPeople = false;
  //detect people running fast..when we receive that info. not now. todo

  float auxPosXAll = 0;
  float auxPosYAll = 0;
  float auxTimeAll = 0;
  int auxNewPeople = 0;
  int auxPeopleRightSide = 0; 
  int auxPeopleLeftSide = 0; 
  int auxPeopleUpSide = 0;
  int auxPeopleBodyInteraction = 0; 

  synchronized (blobs) {
    //println("NumBlobs to display" + str(blobs.size()));
    for (YoloBlob auxBlob : blobs) {
      auxTimeAll += auxBlob.time;

      if (auxBlob.time < thresholdLongPeople)   auxNewPeople++;
      if (auxBlob.xPos > 0.6f)  auxPeopleRightSide++; // people at right side plaza
      if (auxBlob.xPos < 0.4f)  auxPeopleLeftSide++; // people at left side plaza 
      if (auxBlob.statusActionW == -1) auxPeopleBodyInteraction++; //detect body interaction people
      if (auxBlob.yPos < 0.5f ) auxPeopleUpSide++; // 
    }
  }

  //detect people new
  if (auxNewPeople >= thresholdMinPeople4Action)bNewPeople = true;
  else bNewPeople = false;
  //detect people for a long time 
  if (auxTimeAll >= thresholdLongPeople)bLongTimePeople = true;
  else bLongTimePeople = false;

  //detect people at right
  if (auxPeopleRightSide >= thresholdMinPeople4Action)bRightPeople = true;
  else bRightPeople = false;
  //detect people at left
  if (auxPeopleLeftSide >= thresholdMinPeople4Action)bLeftPeople = true;
  else bLeftPeople = false;
  
  //detected people at UP
  if(auxPeopleUpSide > thresholdMinPeople4Action)bUpPeople = true;
  else bUpPeople = false;

  //detect body interaction people
  if (auxPeopleBodyInteraction >= thresholdMinPeople4Action)bBodyInteractionPeople = true;
  else bBodyInteractionPeople = false;
  
  //1 interaction
  //now time to choose priotiries mixing options
  if((bLongTimePeople && !bNewPeople)&&(catId==1))prioAvailableId = 2;//cat llepa
  if((bLongTimePeople && bNewPeople)&&(catId==1))prioAvailableId = 3;//cat look

  //else if((bLongTimePeople && bRightPeople)&&(catId==1))prioAvailableId = 4;//cat scracth
  //else if((bLongTimePeople && bLeftPeople)&&(catId==1))prioAvailableId = 5;//cat hunt
  
  if((bLongTimePeople && bNewPeople && bRightPeople)&&(catId==1))prioAvailableId = 5;//cat hunt
  else if((bLongTimePeople && !bNewPeople && bUpPeople)&&(catId==1))prioAvailableId = 4;//cat scracth

  if(bBodyInteractionPeople)prioAvailableId = 4;
  if(blobs.size()==0)prioAvailableId = 0; //&& random(1)<0.2

}

//-----------------------------
public void draw_clientSensor4Games(int w, int h, float _scaleRaWBlobSize, Boolean _bDrawInfo) {
  synchronized (blobs) {

    for (YoloBlob auxBlob : blobs) {
      //Diferent DRAW methods

      //Pong mode rects
      auxBlob.displayAsPongUsers(w, h, _scaleRaWBlobSize);

      //RandomColorRect
      //auxBlob.displayRandomColorRect(w, h, _scaleRaWBlobSize);

      //Draw Spout Texture and Rects
      //auxBlob.displaySpoutRects(w, h, _scaleRaWBlobSize);

      //Random Colored Circles 
      //auxBlob.displayRandomColorCircles(w, h, _scaleRaWBlobSize);

      //Random Colored Circles + Actions W/H
      //auxBlob.displayActionCircles(w, h, _scaleRaWBlobSize);

      //Draw Info
      if (_bDrawInfo) {
        auxBlob.displayYoloRects(w, h, _scaleRaWBlobSize);

        if (false) {
          fill(0, 200, 255, 250);
          pushMatrix();
          translate(60, 0, 0);
          auxBlob.displayBlobInfo(w, h);
          popMatrix();
        }
      }
    }
  }
}


//----------------------------------------------------
public void oscEvent(OscMessage theOscMessage) {
  //Uncomment to Debug OSC messages, this prints the address pattern and the typetag of the received OscMessage
  //print("### received an osc message.");
  //print(" addrpattern: "+theOscMessage.addrPattern());
  //println(" typetag: "+theOscMessage.typetag());

  if (theOscMessage.checkAddrPattern("/BlobsTrackedYoloData") == true) {
    //get how many new blobs are going to be received
    int numBlobs = theOscMessage.get(0).intValue();

    //Prepare a new Array of Blobs
    synchronized (blobs) {
      blobs.clear();
    }

    //Read and save OSC info
    for (int i = 0; i< numBlobs; i++) {
      int nItms = 8; // X items per received pakage. 
      //if (theOscMessage.checkTypetag("ffff")) {
      float posBlobX = theOscMessage.get(1+i*nItms+0).floatValue(); // X position [0..1]
      float posBlobY = theOscMessage.get(1+i*nItms+1).floatValue();  // Y position [0..1]
      float sizeBlobW = theOscMessage.get(1+i*nItms+2).floatValue(); // X position [0..1]
      float sizeBlobH = theOscMessage.get(1+i*nItms+3).floatValue();  // Y position [0..1] 
      int idBlob     = theOscMessage.get(1+i*nItms+4).intValue();
      int timeBlob   = theOscMessage.get(1+i*nItms+5).intValue();
      //float probBlob = theOscMessage.get(1+i*nItms+6).floatValue();
      int statusActionW = theOscMessage.get(1+i*nItms+6).intValue();
      int statusActionH = theOscMessage.get(1+i*nItms+7).intValue();
      //println("PRE blob("+str(idBlob)+") receive["+str(i)+"] x="+str(posBlobX)+" y="+str(posBlobY));

      //Save this in the new Array of Blob
      YoloBlob auxBlob = new YoloBlob();
      auxBlob.xPos = posBlobX;
      auxBlob.yPos = posBlobY;
      auxBlob.wRawBlob = sizeBlobW;
      auxBlob.hRawBlob = sizeBlobH;
      auxBlob.id = idBlob;
      auxBlob.time = timeBlob;
      //auxBlob.probability = probBlob; 
      auxBlob.statusActionW = statusActionW; //0 is regular, -1 is thin, 1 is thick
      auxBlob.statusActionH = statusActionH; //0 is regular, -1 is down, 1 is up


      synchronized (blobs) {
        blobs.add(auxBlob);
      }

      //println("POST blobs["+str(i)+"] x="+str(blobs.get(i).xPos)+" y="+str(blobs.get(i).yPos));
    }
  }
}
  public void settings() {  size(400, 400);  noSmooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Gato_P5_BlobsTrackedYoloData" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
