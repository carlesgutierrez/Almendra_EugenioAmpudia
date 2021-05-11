import oscP5.*;
import netP5.*;


OscP5 oscP5;
NetAddress myRemoteLocation;

ArrayList<YoloBlob> blobs= new ArrayList<YoloBlob>();


//-------------------------------
void setup_clientSensor4Games() {
  //setup OSC
  oscP5 = new OscP5(this, 12345);
  //myRemoteLocation = new NetAddress("127.0.0.1", 12345);

  blobs.clear();
}

//-----------------------------
void update_blobsInfo() {

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
      if (auxBlob.xPos > 0.6)  auxPeopleRightSide++; // people at right side plaza
      if (auxBlob.xPos < 0.4)  auxPeopleLeftSide++; // people at left side plaza 
      if (auxBlob.statusActionW == -1) auxPeopleBodyInteraction++; //detect body interaction people
      if (auxBlob.yPos < 0.5 ) auxPeopleUpSide++; //
    }
  }

  if (bModoAuto) {
    
    if (millis() - lastTimeSleep > timeSleepMax) {
      prioAvailableId = floor(random(0, 7));
      if(prioAvailableId >= 6)prioAvailableId = 5;
      lastTimeSleep = millis();
      print("prioAvailableId = ");println(prioAvailableId);
    }
    
  } else {
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
    if (auxPeopleUpSide > thresholdMinPeople4Action)bUpPeople = true;
    else bUpPeople = false;

    //detect body interaction people
    if (auxPeopleBodyInteraction >= thresholdMinPeople4Action)bBodyInteractionPeople = true;
    else bBodyInteractionPeople = false;

    //1 interaction
    //now time to choose priotiries mixing options
    if ((bLongTimePeople && !bNewPeople)&&(catId==1))prioAvailableId = 2;//cat llepa
    if ((bLongTimePeople && bNewPeople)&&(catId==1))prioAvailableId = 3;//cat look

    //else if((bLongTimePeople && bRightPeople)&&(catId==1))prioAvailableId = 4;//cat scracth
    //else if((bLongTimePeople && bLeftPeople)&&(catId==1))prioAvailableId = 5;//cat hunt

    if ((bLongTimePeople && bNewPeople && bRightPeople)&&(catId==1))prioAvailableId = 5;//cat hunt
    else if ((bLongTimePeople && !bNewPeople && bUpPeople)&&(catId==1))prioAvailableId = 4;//cat scracth

    if (bBodyInteractionPeople)prioAvailableId = 4;
    if (blobs.size()==0)prioAvailableId = 0; //&& random(1)<0.2
  }
}

//-----------------------------
void draw_clientSensor4Games(int w, int h, float _scaleRaWBlobSize, Boolean _bDrawInfo) {
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
void oscEvent(OscMessage theOscMessage) {
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
