package org.eclipse.gemoc.addon.ganttchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.CategoryLineAnnotation;
import org.jfree.chart.annotations.CategoryPointerAnnotation;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYIntervalDataItem;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

public class GanttChartForLF extends JFrame {
	
	
	public XYIntervalSeriesCollection theDatatSet = new XYIntervalSeriesCollection();
	public JFreeChart chart;
    private static final long serialVersionUID = 1L;

    public GanttChartForLF(String applicationTitle, String chartTitle, String[] taskNames) {
	super(applicationTitle);

	// based on the dataset we create the chart
	chart = ChartFactory.createXYBarChart(chartTitle, "Reaction", true, "Time", theDatatSet,
			PlotOrientation.HORIZONTAL,true,true,true);
	((XYPlot)chart.getPlot()).setDomainAxis(new NumberAxis());
	((XYPlot)chart.getPlot()).setDomainAxis(new SymbolAxis("Reactions", taskNames));	
	
	
	XYBarRenderer renderer = new XYBarRenderer();
    renderer.setUseYInterval(true);
    ((XYPlot)chart.getPlot()).setRenderer(renderer);
    
	// Adding chart into a chart panel
	ChartPanel chartPanel = new ChartPanel(chart);

	// settind default size
	chartPanel.setPreferredSize(new java.awt.Dimension(2500, 600));

	// add to contentPane
	setContentPane(chartPanel);
	

//	 XYPlot plot = (XYPlot) this.chart.getPlot();
//	 int dataSetIndx = plot.getDatasetCount();
//	 XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
//	 lines = new TaskSeries("");
//	 dataset.addSeries(lines);
//	 plot.setDataset(dataSetIndx, dataset);
//
//	LineAndShapeGanttRenderer lineRenderer = new LineAndShapeGanttRenderer(true, false);
//	plot.setRenderer(dataSetIndx, lineRenderer);
	
	
    }

    public static void main(String[] args) {
    
	GanttChartForLF chart = new GanttChartForLF("LF Execution",
		"LF execution", new String[]{"r1", "r2"});
	chart.pack();
	chart.setVisible(true);
	
	
	XYIntervalSeries other = new XYIntervalSeries("r1");
	XYIntervalDataItem t1 = new XYIntervalDataItem(0, 10, 5);
//	other.add(0, -0.2, 0.2, 10, 10, 15);
	other.add(t1, true);
	chart.theDatatSet.addSeries(other);
	
	XYIntervalSeries  another = new XYIntervalSeries("r2");
	XYIntervalDataItem t2 = new XYIntervalDataItem(1, 2, 5);
	another.add(t2, true);
//	another.add(1, 1-0.2, 1+0.2, 2, 1, 5);
	another.add(1, 1-0.2, 1+0.2, 12, 10, 18);
	chart.theDatatSet.addSeries(another);
//	((TaskSeriesCollection)chart.theDatatSet).add(other);
//	other.add(new Task("r2", new SimpleTimePeriod(5, 12)));
//
//	other.add(new Task("r3", new SimpleTimePeriod(8, 18)));
//
//	other.add(new Task("r4", new SimpleTimePeriod(0, 25)));
	
	
//	 // Enter data using BufferReader
//    BufferedReader reader = new BufferedReader(
//        new InputStreamReader(System.in));
//
//    // Reading data using readLine
//    try {
//		String name = reader.readLine();
//	} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
	
	
	
	
	
	XYPlot plot = (XYPlot) chart.chart.getPlot();
	BasicStroke bs = new BasicStroke(2.0f);
	
	
//	bs.createStrokedShape(createArrowShape(new Point(20,20), new Point(300,300)));
	
	XYLineAnnotation c = new XYLineAnnotation(t2.getX(),t2.getYHighValue(),t1.getX(), t1.getYValue(), bs, Color.red);
	plot.addAnnotation(c);
	
	
	
	
	
	
	
	
	
	
//	System.out.println("here");
//	Task t3 = new Task("r1", new SimpleTimePeriod(12, 15));
//	other.add(t3);
//	
////	Task t4 = new Task("r1", new SimpleTimePeriod(25, 30));
////	other.add(t4);
//
//	Task t5 = new Task("r2", new SimpleTimePeriod(15, 20));
//	another.add(t5);
//	
//
//    CategoryPlot plot2 = (CategoryPlot) chart.chart.getPlot();
//    TaskSeries lines = new TaskSeries("");
////    ((TaskSeriesCollection)chart.theDatatSet).add(lines);
//    int dataSetIndx = plot2.getDatasetCount();
//    TaskSeriesCollection dataset = new TaskSeriesCollection();
//    dataset.add(lines);
//    plot2.setDataset(dataSetIndx, dataset);
//
//	LineAndShapeGanttRenderer lineRenderer = new LineAndShapeGanttRenderer(true, false);
//	plot2.setRenderer(dataSetIndx, lineRenderer);
//	java.awt.geom.Line2D.Double line = new java.awt.geom.Line2D.Double();
//	
//	
//	LineUtils.clipLine(line, new Rectangle(1,1,1,1));
//	
////    lines.add(new LineUtils("r1", new SimpleTimePeriod(3, 5)));
	
    }
    
    public static Shape createArrowShape(Point fromPt, Point toPt) {
        Polygon arrowPolygon = new Polygon();
        arrowPolygon.addPoint(-6,1);
        arrowPolygon.addPoint(3,1);
        arrowPolygon.addPoint(3,3);
        arrowPolygon.addPoint(6,0);
        arrowPolygon.addPoint(3,-3);
        arrowPolygon.addPoint(3,-1);
        arrowPolygon.addPoint(-6,-1);


        Point midPoint = midpoint(fromPt, toPt);

        double rotate = Math.atan2(toPt.y - fromPt.y, toPt.x - fromPt.x);

        AffineTransform transform = new AffineTransform();
        transform.translate(midPoint.x, midPoint.y);
        double ptDistance = fromPt.distance(toPt);
        double scale = ptDistance / 12.0; // 12 because it's the length of the arrow polygon.
        transform.scale(scale, scale);
        transform.rotate(rotate);

        return transform.createTransformedShape(arrowPolygon);
    }

    private static Point midpoint(Point p1, Point p2) {
        return new Point((int)((p1.x + p2.x)/2.0), 
                         (int)((p1.y + p2.y)/2.0));
    }
    
}
