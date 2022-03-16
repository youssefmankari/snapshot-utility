package layout;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class Ellipse2DDemo extends Frame {
       
   public Ellipse2DDemo(){
      super("Java AWT Examples");
      prepareGUI();
   }

   public static void main(String[] args){
      Ellipse2DDemo  awtGraphicsDemo = new Ellipse2DDemo();  
      awtGraphicsDemo.setVisible(true);
   }

   private void prepareGUI(){
      setSize(800,900);
      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent windowEvent){
            System.exit(0);
         }        
      }); 
   }    

   @Override
   public void paint(Graphics g) {
      Ellipse2D shape = new Ellipse2D.Float();
      shape.setFrame(300, 150, 300,400);
      Graphics2D g2 = (Graphics2D) g; 
      g2.draw (shape);
      Font font = new Font("Serif", Font.PLAIN, 24);
      g2.setFont(font);
      g.drawString("Welcome to TutorialsPoint", 50, 70);
      g2.drawString("Ellipse2D.Oval", 100, 120); 
   }
}