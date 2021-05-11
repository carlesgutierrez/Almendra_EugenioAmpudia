/**
 * Fachada YoloData Client for an Artistic project of Eugenio Ampudia "Almendra" 
 * receiver osc messages and play cat animations with following some kind of logic
 * Code by Carles Gutiérrez. Videos by Jaime  
 */

import processing.awt.PSurfaceAWT;
import processing.video.*;

PFont f;
import java.util.concurrent.CopyOnWriteArrayList;
int widthDesiredScale = 192;
int heightDesiredScale = 157;
float scaleRawSize = 0.3; //TODO find the real relation between VideoCamera dims and Screen Final
Boolean bDrawInfo = false;

Boolean bBackgroundAlpha = false;
int alphaBk = 200;

Boolean bModoAuto = false;
int timeSleepMax = 5000;
int lastTimeSleep; 

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
import spout.*;
PGraphics pgrSpout; // Canvas to receive a texture
PImage imgSpout; // Image to receive a texture
Spout spout; // DECLARE A SPOUT OBJECT

//-----------------------------
void showCredits(int _fx, int _initY, int _gapY) {
  int fx = _fx;
  int fy = _initY;
  push();

  text("Almendra, de Eugenio Ampudia", 192*.5 + fx, 168*.5+fy);  
  fy += _gapY;
  text("Producción: Biphaus", 192*.5 + fx, 168*.5+fy);
  fy += _gapY;
  text("Biphaus Prod: Javier Bonilla", 192*.5 + fx, 168*.5+fy);
  fy += _gapY;
  text("Biphaus Anim: Jaime Esteban", 192*.5 + fx, 168*.5+fy);
  fy += _gapY;
  text("Biphaus Post: Cristian Alarcón", 192*.5 + fx, 168*.5+fy);
  fy += _gapY;
  text("Software: Carles Gutiérrez", 192*.5 + fx, 168*.5 +fy);
  fy += _gapY;

  pop();
}

//-------------------------------
void setup() {

  size(400, 400);
  background(0);

  //Window properties
  PSurfaceAWT awtSurface = (PSurfaceAWT)surface;
  PSurfaceAWT.SmoothCanvas smoothCanvas = (PSurfaceAWT.SmoothCanvas)awtSurface.getNative();
  smoothCanvas.getFrame().setAlwaysOnTop(true);
  smoothCanvas.getFrame().removeNotify();
  smoothCanvas.getFrame().setUndecorated(true);
  smoothCanvas.getFrame().setLocation(0, 0);//2560
  smoothCanvas.getFrame().addNotify();
  showCredits(-43, 7, 15); //TODO uncomment or locate on better place
  
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
  noSmooth();
}

//------------------------------------
void setup_clientSpout() {
  // Create a canvas or an image to receive the data.
  //pgrSpout = createGraphics(width, height, PConstants.P2D);
  //imgSpout = createImage(width, height, ARGB);
  // CREATE A NEW SPOUT OBJECT
  //spout = new Spout(this);
  //spout.createReceiver("Camera");
}

//-----------------------------------
void updateSpout() {

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
void setupCat() {

  movCatIdle = new Movie(this, "output1.mov");//"MOV_1_v1.mp4");
  movCatIdle.loop();
  movCatIdle.speed(1);
  movCatIdle.jump(0);

  movCatLlepa = new Movie(this, "output2.mov");//"MOV_2_v1.mp4");
  movCatLlepa.loop();
  movCatLlepa.speed(1);
  movCatLlepa.jump(0);

  movCatLook = new Movie(this, "output3.mov");//"MOV_3_v1.mp4");
  movCatLook.loop();
  movCatLook.speed(1);
  movCatLook.jump(0);

  movCatScrath = new Movie(this, "output4.mov");//"MOV_4_v1.mp4");
  movCatScrath.loop();
  movCatScrath.speed(1);
  movCatScrath.jump(0);

  movCatHunter = new Movie(this, "output5.mov");//"MOV_4_v1.mp4");
  movCatHunter.loop();
  movCatHunter.speed(1);
  movCatHunter.jump(0);
  //playStopAllMoviesOnce();
}

//----------------------------------
void updateMainCatLogic() {
  if (catId == 1) {
    float md2 = movCatIdle.duration();
    float mt2 = movCatIdle.time();
    float pctThreshold = md2/1.01;
    if (mt2 >= pctThreshold) {
      if (prioAvailableId > 0 ) {
        //Check this prio a bit random
        float p = random(1);
        if (prioAvailableId == 2 && p>0.8) { //low p
          playLlepaCat();
          prioAvailableId = 0; // reset prio
          println("end idle -> lets llepa");
        } else if (prioAvailableId == 2 && p<0.1) {//very low p
          //playLookCat();
          //prioAvailableId = 0; 
          //println("end idle -> llepa but Look");
          println("end idle -> would Look");
        } else if (prioAvailableId == 3 && p<0.5) {//regular p
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
void updateCatsLogics() {

  //updatePlayStopAllMoviesOnce();//Just fisrt sec

  updateMainCatLogic(); //(idle) 1 -> 2,3,4,5

  //2,3,4,5 -> 1(idle)
  if (catId == 2) {
    float md2 = movCatLlepa.duration();
    float mt2 = movCatLlepa.time();
    float pctThreshold = md2/1.01;
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
    float pctThreshold = md2/1.01;
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
    float pctThreshold = md2/1.01;
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
    float pctThreshold = md2/1.01;
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
void drawCat()
{

  updateCatsLogics();

  push();
  imageMode(CENTER);

  float mapScale = 1; //map(mouseX, 0, width, 1, 3);


  if (catId == 1)image(movCatIdle, 192*.5, 157*.5 - dFix, 192*mapScale, 157*mapScale);
  if (catId == 2)image(movCatLlepa, 192*.5, 157*.5 - dFix, 192*mapScale, 157*mapScale);
  if (catId == 3)image(movCatLook, 192*.5, 157*.5 - dFix, 192*mapScale, 157*mapScale);
  if (catId == 4)image(movCatScrath, 192*.5, 157*.5 - dFix, 192*mapScale, 157*mapScale);
  if (catId == 5)image(movCatHunter, 192*.5, 157*.5 - dFix, 192*mapScale, 157*mapScale);

  if (bShowRectDims) { //Just a visual ref of the video, lets set this False
    noFill();
    stroke(255);
    rect(0, - dFix, 192, 157);
  }

  pop();
}

void movieEvent(Movie movie) {
 if (catId == 1)
  movCatIdle.read();
 else if (catId == 2)
  movCatLlepa.read();
 else if (catId == 3)
  movCatLook.read();
 else if (catId == 4)
  movCatScrath.read();
 else if (catId == 5)
  movCatHunter.read();
}

//-----------------------------------
void drawSpout() {
  //image(imgSpout, 0, 0, width, height);
}

//-----------------------------------
void draw() {

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
  text("Autonomo["+str(bModoAuto)+"]", 130, 20);
  

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
void drawFacadeContourInside()
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

void mousePressed() {
  bDrawInfo = true;
}

void mouseReleased() {
  bDrawInfo = false;
}


void keyPressed() {

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
  
  if(key == 'A' || key == 'a') bModoAuto = !bModoAuto;
}

//-----------------------
void playIdleCat() {   
  catId = 1; 

  movCatIdle.play();
    movCatIdle.jump(0);
  movCatLlepa.stop(); 
  movCatLook.stop(); 
  movCatScrath.stop();
  movCatHunter.stop();
}

//-----------------------
void playLlepaCat() {
  catId = 2; 
  movCatIdle.stop(); 
  movCatLlepa.play();
    movCatLlepa.jump(0);
  movCatLook.stop(); 
  movCatScrath.stop();
  movCatHunter.stop();
}


//-----------------------
void playLookCat() {
  catId = 3; 
  movCatIdle.stop(); 
  movCatLlepa.stop(); 
  movCatLook.play();
    movCatLook.jump(0);
  movCatScrath.stop();
  movCatHunter.stop();
}

//-----------------------
void playScrathCat() {
  catId = 4; 
  movCatIdle.stop(); 
  movCatLlepa.stop(); 
  movCatLook.stop(); 
  movCatScrath.play();
    movCatScrath.jump(0);
  movCatHunter.stop();
}

//-----------------------
void playHunterCat() {
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
