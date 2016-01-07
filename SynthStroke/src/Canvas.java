import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics; 
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.Arrays;


class Canvas extends JPanel 
{

	int PANEL_WIDTH;
	int PANEL_HEIGHT;	
	int prevX;
	double prevY;
	double[] finalTable;
	boolean storeYVal = true;
	int storedYVal;
	boolean set = false; // used in paintComponent
	
	double slope = 0.0;
	double tempY = 0.0;
	
	double amplitude = 0.0;
	double duration = 1.0;


    public Canvas() 
    {
    	PANEL_WIDTH = 0;
    	PANEL_HEIGHT = 0;
    	finalTable = null;
    	
    	
    	
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
        
        addMouseMotionListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {            	
            	moveSquare( e.getX(), e.getY(), false );
            	System.out.println("released");
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
    			
    			// once a y value has been stored, it will no longer be possible to store another y value 
    			// until the mouse resumes increasing along the horizontal axis 
    			//storeYVal = false;
    			
    			// since x is either staying the same or decreasing, set x to the previous value of x to ensure that
    			// we are not backtracking in our line 
    			x = prevX;
    			
    			// store this x value's y coordinate in the final table
    			finalTable[x] = y;
    			
    			// repaint to clear the entire one-pixel column at this x coordinate
    			//repaint( x-2, 0, 10, PANEL_HEIGHT );
    			
    			
    		
    			
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
    	      
    		        
    		        // repaint entire screen from the previous x + 1 to x
    		        //repaint( prevX, 0, x - prevX, PANEL_HEIGHT );
    	        }
    		}
	       
	        // set current coordinates to previous coordinates
	        prevX = x;
	        prevY = y;
	        repaint();
    	}

    }
    
    
    // clears canvas and final table
    public void clearCanvas() 
    {
    	set = false;
    	storeYVal = true;
    	Arrays.fill(finalTable, ((double)PANEL_HEIGHT/2));
    }
    
    
    // returns preferred size
    public Dimension getPreferredSize() 
    {
        return (new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
    }
    
    
    // returns the normalized finalTable
    public double[] getTable() 
    {
    	double[] temp = new double[finalTable.length];
    	for(int i = 0; i < temp.length; i++)
    	{
    		temp[i] = finalTable[i];
    	}
    	return normalize(temp);
    }
    
    
    // saw wave table
    public double[] sawTable() 
    {
    	double[] d = new double[PANEL_WIDTH];
    	double slope = 1.0/(double)PANEL_WIDTH;
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
   
    
    public void set(int width, int height)
    {
    	PANEL_WIDTH = width;
    	PANEL_HEIGHT = height;
    	finalTable = new double[PANEL_WIDTH];
    	Arrays.fill(finalTable, ((double)PANEL_HEIGHT/2));
    	set = true;
    	
    }
    
    public double getAmplitude(){
    	return amplitude;
    }
    
    public double getDuration(){
    	return duration;
    }
    
    public void setAmplitude(double a){
    	amplitude = a;
    }
    
    public void setDuration(double d){
    	duration = d;
    }
    
    
    // paintComponent
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        if(finalTable != null)
        {
	        for(int i = 0; i < finalTable.length-1; i++)
	        {
	        	g.drawLine(i,(int)finalTable[i],i+1,(int)finalTable[i+1]);
	        }
        }
        
        
    }  
    
    
} 