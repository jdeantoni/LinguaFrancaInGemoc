package org.eclipse.gemoc.addon.ganttchart;

import java.util.Random;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

public class RoomManager extends ApplicationFrame {

  public RoomManager(final String title) {

     super(title);
     XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
     //6 different rooms
     int roomCount= 6;
     String[] rooms = new String[roomCount];
     for(int i = 0; i < roomCount; i++){
          rooms[i] = "Room 0" + i;
     }
     //4 different courses inclucing "free"
     int courseTypes = 4;
     String[] courseNames= new String[]{"Cooking","Math","Painting","Free"};
     int totalCourseCount = 30;
     Random r = new Random();
     //Time, until the respective room is occupied
     double[] startTimes = new double[roomCount];

     //Create series. Start and end times are used as y intervals, and the room is represented by the x value
     XYIntervalSeries[] series = new XYIntervalSeries[courseTypes];
     for(int i = 0; i < courseTypes; i++){
          series[i] = new XYIntervalSeries(courseNames[i]);
          dataset.addSeries(series[i]);
     }

     for(int k = 0; k < totalCourseCount; k++){
          //get a random room
          int currentRoom = r.nextInt(roomCount);
          //get a random course
          int currentCourse = r.nextInt(courseTypes);
          //get a random course duration (1-3 h)
          int time = r.nextInt(3) + 1;
          //Encode the room as x value. The width of the bar is only 0.6 to leave a small gap. The course starts 0.1 h/6 min after the end of the preceding course.
          series[currentCourse].add(currentRoom, currentRoom - 0.3, currentRoom + 0.3, startTimes[currentRoom], startTimes[currentRoom] +0.1, startTimes[currentRoom] + time - 0.1);
          //Increase start time for the current room
          startTimes[currentRoom] += time;
     }
     XYBarRenderer renderer = new XYBarRenderer();
     renderer.setUseYInterval(true);
     XYPlot plot = new XYPlot(dataset, new SymbolAxis("Rooms", rooms), new NumberAxis(), renderer);
     plot.setOrientation(PlotOrientation.HORIZONTAL);
     JFreeChart chart = new JFreeChart(plot);
     getContentPane().add(new ChartPanel(chart));

  }

  public static void main(final String[] args) {

     final RoomManager demo = new RoomManager("RoomManager");
     demo.pack();
     demo.setVisible(true);

  }

}
