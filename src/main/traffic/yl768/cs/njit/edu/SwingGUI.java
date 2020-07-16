package main.traffic.yl768.cs.njit.edu;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import traffic.yl768.cs.njit.edu.Scenario;

public class SwingGUI extends Frame{

   private static Scenario scen;

   private static JFrame mainFrame;
   private static JScrollPane jScrollPane;
   private static Image offScreenImage;
   private static JComponent contentPane;

   private static JPanel drawPanel = new JPanel() {
	   private static final long serialVersionUID = 1L;
	   protected void paintComponent(Graphics g) {
		   offScreenImage = scen.nextScenario(drawPanel.getGraphics(),mainFrame);
		   System.out.println("averagePassingTime: " + scen.averagePassingTime);
		   System.out.println("totalLaneChange: " + scen.totalLaneChange);
		   System.out.println("totalPassedCars: " + scen.totalPassedCars);
		   System.out.println("timestep: " + scen.timestep);
		   System.out.println("");
//     	   try {
//   			Thread.sleep(20);
//     	   } catch (InterruptedException e) {
//     		  e.printStackTrace();
//     	   }
		   g.drawImage(offScreenImage, 0, 0, null);
	       drawPanel.setPreferredSize(new Dimension(offScreenImage.getWidth(null),offScreenImage.getHeight(null)));
		   drawPanel.updateUI();
		   jScrollPane.setPreferredSize(new Dimension(mainFrame.getWidth(), mainFrame.getHeight()));
	   }
   };
   
   private static Runnable runUI = new Runnable(){
	   public void run() {
		   showJPanel();
	   }
   };

   public static void main(String[] args){
	   
	   prepareGUI();
	   scen = new Scenario();
	   scen.setAppPercentage(1.0);
	   scen.setLambda(0.666666667);
	   //scen.setDesireSpeed(30);
	   scen.heatingTime=1000;
	   scen.initiate();
	   javax.swing.SwingUtilities.invokeLater(runUI);
	   //new Thread(runUI).start(); 
   }


   private static void showJPanel(){
       jScrollPane.setViewportView(drawPanel);
       mainFrame.setVisible(true);
   }
   
   private static void prepareGUI(){
	      mainFrame = new JFrame("NJIT Traffic Sim");
	      mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	      
	      mainFrame.setSize(1015,630);
	      mainFrame.setLayout(new BorderLayout());

	      jScrollPane = new JScrollPane();
	      jScrollPane.setPreferredSize(new Dimension(1000,600));
	      
	      contentPane = new JPanel();
	      contentPane.add(jScrollPane);
	      contentPane.setOpaque(true); //content panes must be opaque
	 
	      mainFrame.setContentPane(contentPane);
	      mainFrame.setVisible(true); 
   }
   

}