import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.awt.Toolkit;
import game.control.robotic.rovers.board.*;
  
 Planet planet;
  
  void loadBoard(String fileName) {

    try (FileInputStream fileInputStream = new FileInputStream(fileName);
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
    
}
  
  Toolkit tk = Toolkit.getDefaultToolkit();
  
  int timer = millis();
  
  float xorigin = 0;
  float yorigin = 0;
  float areaSize = 20;
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
  
String filePath = "enter ... file ... path ... to planet file";

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
