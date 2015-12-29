import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics; 
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;


public class Canvas
{
    
	final static MyPanel panel = new MyPanel();

	
	public static void main(String[] args) 
	{
        SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() {
                createAndShowGUI(); 
            }
        });
    }
    
    
	public Canvas() 
    {	
    	SwingUtilities.invokeLater(new Runnable() 
    	{
            public void run() {
                createAndShowGUI(); 
            }
        });
    }

    
	private static void createAndShowGUI() 
    {
		
        JFrame f = new JFrame( "Canvas Demo" );
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        f.add( panel );
        f.setPreferredSize( new Dimension(800,423) );
        f.pack();
        f.setResizable( false );
        f.setVisible( true );
        
    }

	
	public double[] getTable() 
	{
		if(panel != null) 
		{
			return panel.getTable();
		}
		else
		{
			System.out.println( "panel.getTable null" );
			return null;
		}
	} 

}

class MyPanel extends JPanel 
{

    
	Line line = new Line();
	final int PANEL_WIDTH = 800;
	final int PANEL_HEIGHT = 400;	
	int prevX = 0;
	double prevY = PANEL_HEIGHT / 2;
	final Dimension PANEL_DIMENSION = new Dimension( PANEL_WIDTH, PANEL_HEIGHT) ;
	final int TABLE_SIZE = PANEL_WIDTH;
	double[] finalTable = new double[ TABLE_SIZE ];
	boolean storeYVal = true;
	int storedYVal;
	boolean set = false; // used in paintComponent
	
	double slope = 0.0;
	double tempY = 0.0;


    public MyPanel() 
    {
    	
        addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
            	moveSquare( e.getX(), e.getY(), true ); 
            }
        });

        addMouseMotionListener(new MouseAdapter()
        {
            public void mouseDragged(MouseEvent e)
            {
            	moveSquare( e.getX(), e.getY(), false );
            }
        });

    }

    private void moveSquare( int x, int y, boolean newLine )
    {
    	
    	// if you are starting a new line
    	if ( newLine ) 
    	{
    		// initialize beginning point
    		prevX = x;
    		prevY = y;
    		
    		// it is possible to store a y value
    		storeYVal = true;
    		
    		// a line has been set
    		set = true;
    	}
 	
    	
    	// if the mouse position has changed and is within the panel constraints
    	if ( ( ( x != prevX)  || ( y != prevY ) ) && x < PANEL_WIDTH && x >= 0 && y < PANEL_HEIGHT && y >= 0 )
    	{
    		
    		// if the mouse is not increasing along the positive horizontal axis 
    		if ( x <= prevX ) 
    		{
    			
    			// check if it is possible to store a y value
    			if ( storeYVal == true ) 
    			{
    				// if so, store the last y value before the mouse stopped increasing along the horizontal axis
    				// this y value will be used in setting our line 
    				storedYVal = (int) prevY;
    			}
    			
    			// once a y value has been stored, it will no longer be possible to store another y value 
    			// until the mouse resumes increasing along the horizontal axis 
    			storeYVal = false;
    			
    			// since x is either staying the same or decreasing, set x to the previous value of x to ensure that
    			// we are not backtracking in our line 
    			x = prevX;
    			
    			// store this x value's y coordinate in the final table
    			finalTable[x] = y;
    			
    			// repaint to clear the entire one-pixel column at this x coordinate
    			repaint( x, 0, 1, PANEL_HEIGHT );
    			
    			// set a line from our stored y value to the new y value
    			line.setLine( x, storedYVal, x, y );
    			
    			// repaint the one-pixel column from the stored y value to the new y value
    			repaint( x, storedYVal, 1, y );
    			
    		}  
    		// else the mouse is increasing along the positive horizontal axis
    		else 
    		{
   
    			// it is now possible to store another y value
    			storeYVal = true;
    			
    			// calculate the slope between the previous mouse coordinates and the current mouse coordinates
    			// this is necessary because there is a jump between coordinates when dragging the mouse quickly 
    			slope = ( y - prevY ) / ( (double) ( x - prevX ) );
    			
    			// in order to linearly interpolate between the previous mouse coordinates and the current mouse coordinates,
    			// first set a temporary y value equal to the previous y value
    	        tempY = prevY;
    	        
    	        // then linearly interpolate between the previous and the current mouse coordinates
    	        for( int i = prevX; i <= x; i++ ) 
    	        {
    	        	// store the temporary y in the final table
    	        	finalTable[i] = tempY;
    	        	
    	        	// this shouldn't happen, but if it does, print message
    	        	if ( slope == Double.POSITIVE_INFINITY || slope == Double.NEGATIVE_INFINITY ) 
    	        	{
    	        		System.out.println( "Infinity slope \nSlope = " + slope + " x = " + x + " y = " + y );	        		
    		        }
    	        	
    	        	// this happens sometimes when drawing near the top of the canvas and although it does not appear
    	        	// to affect the drawing of the line, I'm not sure if it will cause bugs in the audio later,
    	        	// which is why I have left this check in
    	        	if( sign(tempY+slope) == -1 )
    	        	{
    	        		System.out.println( "tempY+slope is negative \nSlope = " + slope + "    x = " + x + "    y = " + y + "   tempY+slope = " + (tempY+slope) );
    	        	}
    	        	
    	        	// increment the temporary y by the slope
    	        	tempY += slope;
    	        	
    	        	
    	        	// set a line from the previous coordinates to the current coordinates
    		        line.setLine( prevX, (int) prevY, x, y );
    		        
    		        // repaint entire screen from the previous x + 1 to x
    		        repaint( prevX + 1, 0, x - prevX, PANEL_HEIGHT );
    	        }
    		}
	       
	        // set current coordinates to previous coordinates
	        prevX = x;
	        prevY = y;
    	}

    }
    
    
    // clears canvas and final table
    public void clearCanvas() 
    {
    	repaint(new Rectangle(PANEL_DIMENSION));
    	set = false;
    	storeYVal = true;
    	for(int i = 0; i < finalTable.length; i++)
    	{
    		finalTable[i] = 0.0;
    	}
    }
    
    
    // returns preferred size
    public Dimension getPreferredSize() 
    {
        return PANEL_DIMENSION;
    }
    
    
    // returns the normalized finalTable
    public double[] getTable() 
    {
    	return normalize(finalTable);
    }
    
    
    // saw wave table
    public double[] sawTable() 
    {
    	double[] d = new double[TABLE_SIZE];
    	double slope = 1.0/(double)TABLE_SIZE;
    	double val = 0;
    	for(int i = 0; i < d.length; i++)
    	{
    		d[i] = val;
    		val += slope;
    	}
    	return d;
    }
    
    
    // sine wave table
    public double[] sineTable() 
    {
    	double[] d = new double[180];
    	for(int i = 0; i < d.length; i++)
    	{
    		d[i] = Math.sin(Math.toRadians((double)i));
    	}
    	return d;
    } 
    
    
    // normalizes the finalTable so the values are between -1 and 1
    public double[] normalize(double[] t) 
    {
    	double d;
    	for(int i = 0; i < PANEL_WIDTH; i++)
    	{
    		d = t[i];
    		d =(((double)PANEL_HEIGHT - d)/(double)PANEL_HEIGHT * 2.0) - 1.0;
    		t[i] = d;
    	}
    	return t;
    }
    
    
    // checks the sign of a double
    public int sign(double f) 
    {
        if (f != f) throw new IllegalArgumentException("NaN");
        if (f == 0) return 0;
        f *= Double.POSITIVE_INFINITY;
        if (f == Double.POSITIVE_INFINITY) return +1;
        if (f == Double.NEGATIVE_INFINITY) return -1;
        //this should never be reached
        throw new IllegalArgumentException("Unfathomed double");
    }
    
    
    // paintComponent
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        // paint a line 
        line.paintLine(g, set);
    }  
    
    
} 


// draws a line for the canvas 
class Line
{

    private int xPos;
    private int yPos;
    private int width;
    private int height;
    boolean set = false;
    
    // sets line at the coordinates
    public void setLine(int startx, int starty, int endx, int endy)
    { 
        this.xPos = startx;
        this.yPos = starty;
        this.width = endx;
        this.height = endy;
    }
    
    // paints the line if a line has been set
    public void paintLine(Graphics g, boolean set)
    {
        g.setColor(Color.BLACK);
        if(set)
        g.drawLine(xPos,yPos,width,height);
    }
}
