 /*
 * Created on 05-feb-2005
 */
package javavis.desktop.gui;

import java.awt.*;
 
 /**************************************************************************
 * File: Arrows.java               Class: Arrows
 * Goal: A self contained class that will calculate and draw arrows
 *       based on a From location and a To location
 *
 * Written: Wilfred Dittmer             (3/20/01)
 *
 * For:Cooperative Association for Internet Data Analysis
 ***************************************************************************
 ***************************************************************************
 By accessing this software, ARROWS, you are duly informed of and 
 agree to be bound by the conditions described below in this notice:
  
 This software product, ARROWS, is developed by Wilfred Dittmer 
 and copyrighted(C) 1998 by the University of California, 
 San Diego (UCSD), with all rights reserved.  UCSD administers the NSF 
 grant to CAIDA, number NCR-9711092, under which this code was developed. 
  
 There is no charge for ARROWS software. You can redistribute it and/or
 modify it under the terms of the GNU General Public License, v. 2 dated
 June 1991 which is incorporated by reference herein.  ARROWS is
 distributed WITHOUT ANY WARRANTY, IMPLIED OR EXPRESS, OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE or that the use of
 it will not infringe on any third party's intellectual property
 rights.
  
 You should have received a copy of the GNU GPL along with the ARROWS
 program.  Copies can also be obtained from
 http://www.gnu.org/copyleft/gpl.html or by writing to
  
                         University of California, San Diego
                         SDSC/CAIDA
                         9500 Gilman Dr., MS-0505
                         La Jolla, CA 92093 - 0505  USA
  
   Or contact INFO@CAIDA.ORG
 **************************************************************************/

 public class Arrow {
     // Draw arrows
     private static boolean drawArrows = true;
     private static int arrowLength = 17;
     private static double arrowAngle = 0.8;

     public static void init(int arrowLength, double arrowAngle) {
     }

     public static void swapDrawArrows() {
         drawArrows = !drawArrows;
     }

     public static boolean getShowArrows() {
         return drawArrows;
     }

     public static void increaseArrowSize() {
         arrowLength += 4;
     }

     public static void decreaseArrowSize() {
         arrowLength -= 4;
         if (arrowLength < 4) {
            arrowLength = 4;
         }
     }

     public static void increaseArrowAngle() {
         arrowAngle += 0.1;
         if (arrowAngle >= 0.8) {
             arrowAngle = 0.7;
         }
     }

     public static void decreaseArrowAngle() {
         arrowAngle -= 0.1;
         if (arrowAngle <= 0) {
             arrowAngle = 0.1;
         }        
     }

     public static void drawArrow(Graphics g, int fromX, int fromY, 
                                              int toX, int toY) {

         // Determine the direction of the arrow
         //              / positive, arrow points to the left
         // fromX - toX <  0, arrow goes straight up/down
         //              \ negative, arrow points to the right

         //              / positive, arrow points up
         // fromY - toY <  0, arrow goes straight left/right
         //              \ negative, arrow points down

         int arrowSize = (int) ((Math.tan(arrowAngle)*arrowLength)/2);
         int xDir;
         int yDir;
         int[] xArrow = new int[3];
         int[] yArrow = new int[3];
         int xTemp = 0;
         int yTemp = 0;

         if (fromX < 0 && toX < 0) {
             xDir = (Math.abs(toX) - Math.abs(fromX));
         } else {
             xDir = fromX - toX;
         }

         if (fromY <0 && toY < 0) {
             yDir = (Math.abs(toY) - Math.abs(fromY));
         } else {
             yDir = fromY - toY;
         }

         if (xDir == 0) {
             if (yDir == 0) {
                 // Should not happen
                 return;
             } else if (yDir > 0) {
                 // Straight UP
                 xArrow[0] = toX;
                 yArrow[0] = toY;
                 yTemp = toY + arrowLength;
                 xTemp = toX - arrowSize;
                 xArrow[1] = xTemp;
                 yArrow[1] = yTemp;
                 xTemp = toX + arrowSize;
                 xArrow[2] = xTemp;
                 yArrow[2] = yTemp;
                 g.fillPolygon(xArrow, yArrow, 3);
             } else {
                 // Straight Down
                 xArrow[0] = toX;
                 yArrow[0] = toY;
                 yTemp = toY - arrowLength;
                 xTemp = toX - arrowSize;
                 xArrow[1] = xTemp;
                 yArrow[1] = yTemp;
                 xTemp = toX + arrowSize;
                 xArrow[2] = xTemp;
                 yArrow[2] = yTemp;
                 g.fillPolygon(xArrow, yArrow, 3);
             }
         }

         if (xDir > 0) {
             if (yDir == 0) {
                 // Straight Left
                 xArrow[0] = toX;
                 yArrow[0] = toY;
                 xTemp = toX + arrowLength;
                 yTemp = toY - arrowSize;
                 xArrow[1] = xTemp;
                 yArrow[1] = yTemp;
                 yTemp = toY + arrowSize;
                 xArrow[2] = xTemp;
                 yArrow[2] = yTemp;
                 g.fillPolygon(xArrow, yArrow, 3);
             } else if (yDir > 0) {
                 // Left Up
                 drawLUArrow(g,fromX, fromY, toX, toY);
             } else {
                 // Left Down
                 drawLDArrow(g,fromX, fromY, toX, toY);
             }
         }

         if (xDir < 0) {
             if (yDir == 0) {
                 // Straight Right
                 xArrow[0] = toX;
                 yArrow[0] = toY;
                 xTemp = toX - arrowLength;
                 yTemp = toY - arrowSize;
                 xArrow[1] = xTemp;
                 yArrow[1] = yTemp;
                 yTemp = toY + arrowSize;
                 xArrow[2] = xTemp;
                 yArrow[2] = yTemp;
                 g.fillPolygon(xArrow, yArrow, 3);
             } else if (yDir > 0) {
                 // Right Up
                 drawRUArrow(g,fromX, fromY, toX, toY);
             } else {
                 // Right down
                 drawRDArrow(g,fromX, fromY, toX, toY);
             }
         }
     }

     private static void drawLUArrow(Graphics g, int fromX, int fromY,
                                     int toX, int toY) {
         int[] xArrow = new int[3];
         int[] yArrow = new int[3];
         double angle = 0;
         double slantLine = 0;
         double radian = 0;
         int xTemp = 0;
         int yTemp = 0;
         int xDistance = 0;
         int yDistance = 0;
         double width = 0;
         double height = 0;

         double arrowDegree = arrowAngle/2;

         // from point always has greater fromX and fromY
         width = Math.abs(fromX - toX);
         height = Math.abs(fromY - toY);

         xArrow[0] = toX;
         yArrow[0] = toY;

         // Calculate angle between the line and the horizontal line
         radian = Math.atan(height/width);

         // Calculate length of the arrow at on side
         slantLine = arrowLength/Math.cos(arrowDegree);

         // Calculate the angle between horizontal line and slantLine
         angle = arrowDegree - radian;

         if (angle <= 0) {
             // The point is below the horizontal line
             angle = Math.abs(angle);

             // Calculate the x distance from toX        
             xDistance = (int) (Math.cos(angle)*slantLine);
             xTemp = toX + xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.sin(angle)*slantLine);
             yTemp = toY + yDistance;
         } else {
             // The point is above the horizontal line

             // Calculate the x distance from toX
             xDistance = (int) (Math.cos(angle)*slantLine);
             xTemp = toX + xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.sin(angle)*slantLine);
             yTemp = toY - yDistance;
         }

         xArrow[1] = xTemp;
         yArrow[1] = yTemp;

         // Now calculate the other point
         radian = (Math.PI*0.5) - radian;

         // Calculate the angle between vertical line and slantLine
         angle = arrowDegree - radian;

         if (angle <= 0) {
             // The point is to the right of the vertical line
             angle = Math.abs(angle);

             // Calculate the x distance from toX        
             xDistance = (int) (Math.sin(angle)*slantLine);
             xTemp = toX + xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.cos(angle)*slantLine);
             yTemp = toY + yDistance;
         } else {
             // The point is to the left the vertical line

             // Calculate the x distance from toX
             xDistance = (int) (Math.sin(angle)*slantLine);
             xTemp = toX - xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.cos(angle)*slantLine);
             yTemp = toY + yDistance;
         }

         xArrow[2] = xTemp;
         yArrow[2] = yTemp;

         // Draw the thing
         g.fillPolygon(xArrow, yArrow, 3);
     }


     private static void drawLDArrow(Graphics g, int fromX, int fromY,
                                     int toX, int toY) {
         int[] xArrow = new int[3];
         int[] yArrow = new int[3];
         double angle = 0;
         double slantLine = 0;
         double radian = 0;
         int xTemp = 0;
         int yTemp = 0;
         int xDistance = 0;
         int yDistance = 0;
         double width = 0;
         double height = 0;

         double arrowDegree = arrowAngle/2;

         // We know that from point is right and above of to point
         width = Math.abs(fromX - toX);
         height = Math.abs(toY - fromY);
         xArrow[0] = toX;
         yArrow[0] = toY;

         // Calculate angle between the line and the horizontal line
         radian = Math.atan(height/width);

         // Calculate length of the arrow at on side
         slantLine = arrowLength/Math.cos(arrowDegree);

         // Calculate the angle between horizontal line and slantLine
         angle = arrowDegree - radian;

         if (angle <= 0) {
             // The point is above the horizontal line
             angle = Math.abs(angle);

             // Calculate the x distance from toX        
             xDistance = (int) (Math.cos(angle)*slantLine);
             xTemp = toX + xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.sin(angle)*slantLine);
             yTemp = toY - yDistance;
         } else {
             // The point is below the horizontal line

             // Calculate the x distance from toX
             xDistance = (int) (Math.cos(angle)*slantLine);
             xTemp = toX + xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.sin(angle)*slantLine);
             yTemp = toY + yDistance;
         }

         xArrow[1] = xTemp;
         yArrow[1] = yTemp;

         // Now calculate the other point
         radian = (Math.PI*0.5) - radian;

         // Calculate the angle between vertical line and slantLine
         angle = arrowDegree - radian;

         if (angle <= 0) {
             // The point is to the right of the vertical line
             angle = Math.abs(angle);

             // Calculate the x distance from toX        
             xDistance = (int) (Math.sin(angle)*slantLine);
             xTemp = toX + xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.cos(angle)*slantLine);
             yTemp = toY - yDistance;
         } else {
             // The point is to the left the vertical line

             // Calculate the x distance from toX
             xDistance = (int) (Math.sin(angle)*slantLine);
             xTemp = toX - xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.cos(angle)*slantLine);
             yTemp = toY - yDistance;
         }

         xArrow[2] = xTemp;
         yArrow[2] = yTemp;

         // Draw the thing
         g.fillPolygon(xArrow, yArrow, 3);
     }


     private static void drawRUArrow(Graphics g, int fromX, int fromY,
                                     int toX, int toY) {
         int[] xArrow = new int[3];
         int[] yArrow = new int[3];
         double angle = 0;
         double slantLine = 0;
         double radian = 0;
         int xTemp = 0;
         int yTemp = 0;
         int xDistance = 0;
         int yDistance = 0;
         double width = 0;
         double height = 0;

         double arrowDegree = arrowAngle/2;

         // From point is to left and below to point
         width = Math.abs(toX - fromX);
         height = Math.abs(toY - fromY);
         xArrow[0] = toX;
         yArrow[0] = toY;

         // Calculate angle between the line and the horizontal line
         radian = Math.atan(height/width);

         // Calculate length of the arrow at on side
         slantLine = arrowLength/Math.cos(arrowDegree);

         // Calculate the angle between horizontal line and slantLine
         angle = arrowDegree - radian;

         if (angle <= 0) {
             // The point is below the horizontal line
             angle = Math.abs(angle);

             // Calculate the x distance from toX        
             xDistance = (int) (Math.cos(angle)*slantLine);
             xTemp = toX - xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.sin(angle)*slantLine);
             yTemp = toY + yDistance;
         } else {
             // The point is above the horizontal line

             // Calculate the x distance from toX
             xDistance = (int) (Math.cos(angle)*slantLine);
             xTemp = toX - xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.sin(angle)*slantLine);
             yTemp = toY - yDistance;
         }

         xArrow[1] = xTemp;
         yArrow[1] = yTemp;

         // Now calculate the other point
         radian = (Math.PI*0.5) - radian;

         // Calculate the angle between vertical line and slantLine
         angle = arrowDegree - radian;

         if (angle <= 0) {
             // The point is to the left of the vertical line
             angle = Math.abs(angle);

             // Calculate the x distance from toX        
             xDistance = (int) (Math.sin(angle)*slantLine);
             xTemp = toX - xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.cos(angle)*slantLine);
             yTemp = toY + yDistance;
         } else {
             // The point is to the right the vertical line

             // Calculate the x distance from toX
             xDistance = (int) (Math.sin(angle)*slantLine);
             xTemp = toX + xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.cos(angle)*slantLine);
             yTemp = toY + yDistance;
         }

         xArrow[2] = xTemp;
         yArrow[2] = yTemp;

         // Draw the thing
         g.fillPolygon(xArrow, yArrow, 3);
     }


     private static void drawRDArrow(Graphics g, int fromX, int fromY,
                                     int toX, int toY) {
         int[] xArrow = new int[3];
         int[] yArrow = new int[3];
         double angle = 0;
         double slantLine = 0;
         double radian = 0;
         int xTemp = 0;
         int yTemp = 0;
         int xDistance = 0;
         int yDistance = 0;
         double width = 0;
         double height = 0;

         double arrowDegree = arrowAngle/2;
         // To point always has greater toX and toY
         width = Math.abs(toX - fromX);
         height = Math.abs(toY - fromY);

         xArrow[0] = toX;
         yArrow[0] = toY;

         // Calculate angle between the line and the horizontal line
         radian = Math.atan(height/width);

         // Calculate length of the arrow at on side
         slantLine = arrowLength/Math.cos(arrowDegree);

         // Calculate the angle between horizontal line and slantLine
         angle = arrowDegree - radian;

         if (angle <= 0) {
             // The point is above the horizontal line
             angle = Math.abs(angle);

             // Calculate the x distance from toX        
             xDistance = (int) (Math.cos(angle)*slantLine);
             xTemp = toX - xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.sin(angle)*slantLine);
             yTemp = toY - yDistance;
         } else {
             // The point is below the horizontal line

             // Calculate the x distance from toX
             xDistance = (int) (Math.cos(angle)*slantLine);
             xTemp = toX - xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.sin(angle)*slantLine);
             yTemp = toY + yDistance;
         }

         xArrow[1] = xTemp;
         yArrow[1] = yTemp;

         // Now calculate the other point
         radian = (Math.PI*0.5) - radian;

         // Calculate the angle between vertical line and slantLine
         angle = arrowDegree - radian;

         if (angle < 0) {
             // The point is to the left of the vertical line
             angle = Math.abs(angle);

             // Calculate the x distance from toX        
             xDistance = (int) (Math.sin(angle)*slantLine);
             xTemp = toX - xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.cos(angle)*slantLine);
             yTemp = toY - yDistance;
         } else {
             // The point is to the right the vertical line

             // Calculate the x distance from toX
             xDistance = (int) (Math.sin(angle)*slantLine);
             xTemp = toX + xDistance;

             // Calculate the y distance from toY
             yDistance = (int) (Math.cos(angle)*slantLine);
             yTemp = toY - yDistance;
         }

         xArrow[2] = xTemp;
         yArrow[2] = yTemp;

         // Draw the thing
         g.fillPolygon(xArrow, yArrow, 3);
     }
 }
