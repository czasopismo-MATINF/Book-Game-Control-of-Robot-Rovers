import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;

import java.lang.InterruptedException;

import java.awt.Toolkit;

import game.control.robot.rovers.board.*;
import game.control.robot.rovers.*;


 Planet planet;
  
  void loadBoard(String filePath) {

    try (FileInputStream fileInputStream = new FileInputStream(new File(filePath));
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
        planet = (Planet) objectInputStream.readObject();
    } catch(ClassNotFoundException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  
  void printArea(Area area) {

    if( area.getRocks() > 0 ) {
      textSize(15);
      fill(255,255,255);
      text(String.valueOf(area.getRocks()), 10, 90);
    }
    if( area.getBatteries().size() > 0 ) {
       textSize(15);
       fill(255,255,255);
       text(String.valueOf(area.getBatteries().size()), 80, 90);
    }
    if (area.getChargingStations().size() > 0 ) {
       textSize(15);
       fill(255,255,255);
       text(String.valueOf(area.getChargingStations().size()), 80, 20);
    }
    if( area.hasChasm() ) {
      color(0,0,0);
      strokeWeight(2);
      line(10,10,20,30);
      line(20,30,90,10);
      line(10,90,20,40);
      line(20,40,90,30);
      strokeWeight(1);
    }
    if( area.getRobots().size() > 0 ) {
      for(Robot r : area.getRobots()) {
         fill(0,0,0);
         circle(random(10,90),random(10,90),10); 
      }
    }
    if( area.getMotherShip() != null ) {
        strokeWeight(2);
        line(5,35,35,5);
        line(35,5,65,5);
        line(65,5,95,35);
        line(95,35,95,65);
        line(95,65,65,95);
        line(65,95,35,95);
        line(35,95,5,65);
        line(5,65,5,35);
        strokeWeight(1);
    }
    if( area.getBlizzards().size() > 0 ) {
        for(int i = 0; i < area.getBlizzardVolume(); ++i) {
           fill(255,255,255);
           color(255,255,255);
           for(int j = 0; j < 10; ++j) {
              circle(random(0,100),random(0,100),3); 
           }
        }
    }
    
  }
  
  void printBoard() {
    
    background(200,200,200);
    
    int width = planet.getWidth();
    int height = planet.getHeight();
    
    for(int i = 0; i <= width; ++i) {
      line(xorigin+xshift*areaSize+areaSize*i,yorigin+yshift*areaSize,xorigin+xshift*areaSize+areaSize*i,yorigin+yshift*areaSize+areaSize*height);
    }
    
    for(int j = 0; j <= height; ++j) {
      line(xorigin+xshift*areaSize,yorigin+yshift*areaSize+areaSize*j,xorigin+xshift*areaSize+areaSize*width,yorigin+yshift*areaSize+areaSize*j);
    }
    
    for(int i = 0; i < planet.getWidth(); ++i) {
      for(int j = 0; j < planet.getHeight(); ++j) {
            translate(xorigin+xshift*areaSize+areaSize*i,yorigin+yshift*areaSize+areaSize*j);
            pushMatrix();
            scale(areaSize/100);
            printArea(planet.getSurface()[i][j]);
            popMatrix();
            translate(-xorigin-xshift*areaSize-areaSize*i,-yorigin-yshift*areaSize-areaSize*j);
        }
      }
    
}
  
  Toolkit tk = Toolkit.getDefaultToolkit();
  
  int timer = millis();
  
  float xorigin = 0;
  float yorigin = 0;
  float areaSize = 60;
  float xshift;
  float yshift;
  
void keyPressed() {
  
  if (key == CODED) {
    if (keyCode == UP) {
      yshift -= 1;
    } else if (keyCode == DOWN) {
      yshift += 1;
    } else if (keyCode == RIGHT) {
      xshift += 1;
    } else if (keyCode == LEFT) {
      xshift -= 1;
    }
  }
  
  if (key == '+' || key == '=') {
      areaSize += 1;
  }
  if (key == '-' || key == '_') {
      areaSize -= 1;
  }
  
}
//String filePath =  "planet ... file ... path";
String filePath = "C:\\Users\\mikol\\git\\Book-Game-Control-of-Robot-Rovers\\Game_Control_of_Robot_Rovers_Eclipse\\board.in";

/*
void setup() {

   size(600,600);
    
   loadBoard(filePath);
   printBoard();
   timer = millis();
   tk.beep();
}
  
void draw() {
  
   if(millis() - timer > 5000) {
     loadBoard(filePath);
     printBoard();
     timer = millis();
     tk.beep();
  }
  
}
*/
ControlRobotTurnGameBoardAndCommands game;
ControlRobotTurnGameConcurrentShell gameShell;
ExecutorService shellThreadPool;
Future<Boolean> gameResult;
{
  loadBoard(filePath);
  game = new ControlRobotTurnGameBoardAndCommands();
  game.setPlanet(planet);
  gameShell = new ControlRobotTurnGameConcurrentShell(game);
  shellThreadPool = Executors.newFixedThreadPool(1);
  planet = game.getPlanet();
}

void printFinishScreen() {
  
  //background(0,0,0);
  
}

void setup() {

   size(600,600);
   
   printBoard();
   timer = millis();
   tk.beep();
   
   gameResult = shellThreadPool.submit(gameShell);

}
  
void draw() {
  
   if(millis() - timer > 1000) {

   synchronized(game) {
       printBoard();
   }
     
   if(gameResult.isDone()) {
       
     try {
       printFinishScreen();
     } catch (Exception e) {
       e.printStackTrace();
     }
       
    }

     timer = millis();
     tk.beep();
     
   }
  
}
