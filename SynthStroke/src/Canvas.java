import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics; 
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.Arrays;
import java.util.Random;


class Canvas extends JPanel 
{

	int CANVAS_WIDTH;
	int CANVAS_HEIGHT;	
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
    	CANVAS_WIDTH = 0;
    	CANVAS_HEIGHT = 0;
    	finalTable = null;
    	
    	
        addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
            	requestFocus(); //takes focus from other draggablespinners when clicked
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
    	if ( ( ( x != prevX)  || ( y != prevY ) ) && x < CANVAS_WIDTH && x >= 0 && y < CANVAS_HEIGHT && y >= 0 )
    	{
    		
    		// if the mouse is not increasing along the positive horizontal axis 
    		if ( x <= prevX ) 
    		{
    			 			
    			// since x is either staying the same or decreasing, set x to the previous value of x to ensure that
    			// we are not backtracking in our line 
    			x = prevX;
    			
    			// store this x value's y coordinate in the final table
    			finalTable[x] = y;   			   			  		
    			
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
    	        		System.out.println( "tempY+slope is negative \nSlope = " + slope + "    x = " + x + "    y = " + y + " tempY = " +tempY + "   tempY+slope = " + (tempY+slope) );
    	        	}
    	        	
    	        	// increment the temporary y by the slope
    	        	tempY += slope;
    	  
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
    	Arrays.fill(finalTable, ((double)CANVAS_HEIGHT/2));
    }
    
    
    // returns preferred size
    public Dimension getPreferredSize() 
    {
        return (new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
    }
    
    
    // returns the normalized finalTable
    public double[] getNormalizedTable() 
    {
    	double[] temp = new double[finalTable.length];
    	for(int i = 0; i < temp.length; i++)
    	{
    		temp[i] = finalTable[i];
    	}
    	return normalize(temp);
    }
    
    
    // sets table as finalTable
    public void setFinalTable(double[] table)
    {
    	finalTable = table;
    }
    
    // saw wave table
    public double[] sawTable() 
    {
    	double[] sawTable = new double[CANVAS_WIDTH];
    	double slope = (double)CANVAS_HEIGHT/(double)CANVAS_WIDTH;
    	double val = CANVAS_HEIGHT;
    	for(int i = 0; i < sawTable.length; i++)
    	{
    		sawTable[i] = val;
    		val -= slope;
    	}
    	return sawTable;
    }
  
    
    // square wave table
    public double[] squareTable()
    {
    	double[] squareTable = sawTable();
    	for(int i = 0; i < squareTable.length; i++)
    	{
    		if(squareTable[i] > (CANVAS_HEIGHT/2)){
    			squareTable[i] = (double)(CANVAS_HEIGHT-1);
    		}
    		else
    		{
    			squareTable[i] = 0.0;
    		}
    	}
    	return squareTable;
    }
    
    
    // triangle wave table
    public double[] triangleTable()
    {
    	double[] triangleTable = new double[CANVAS_WIDTH];
    	double slope = CANVAS_HEIGHT/(CANVAS_WIDTH/2.0);
    	double d = CANVAS_HEIGHT;
    	for(int i = 0; i < (triangleTable.length/2); i++)
    	{
    		d -= slope;
    		triangleTable[i] = d;
    	}
    	d = 0.0;
    	for(int i = (triangleTable.length/2); i < triangleTable.length; i++)
    	{
    		triangleTable[i] = d; 
    		d += slope;
    	}
    	return triangleTable;
    }
    
    
    // sine wave table
    public double[] sineTable() 
    {
    	double[] sineTable = new double[CANVAS_WIDTH];
    	for(int i = 0; i < sineTable.length; i++)
    	{
    		sineTable[i] = Math.sin( Math.toRadians(i) ) * (CANVAS_HEIGHT-1)/2 + (CANVAS_HEIGHT)/2; 
    	}
    	return sineTable;
    } 
    
    
    // noise wave table
    public double[] noiseTable()
    {
    	double[] noiseTable = new double[CANVAS_WIDTH];
    	for(int i = 0; i < noiseTable.length; i++)
    	{
    		Random random = new Random();
    		double randomDouble = 0.0 + ((double)(CANVAS_HEIGHT-1) - 0.0) * random.nextDouble();
    		noiseTable[i] = randomDouble;
    	}
    	return noiseTable;
    }
    
    // normalizes the finalTable so the values are between -1 and 1
    public double[] normalize(double[] t) 
    {
    	double d;
    	for(int i = 0; i < CANVAS_WIDTH; i++)
    	{
    		d = t[i];
    		d =(((double)CANVAS_HEIGHT - d)/(double)CANVAS_HEIGHT * 2.0) - 1.0;
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
   
    // call to set values in frame class after calling pack()
    public void set(int width, int height)
    {
    	CANVAS_WIDTH = width;
    	CANVAS_HEIGHT = height;
    	finalTable = new double[CANVAS_WIDTH];
    	Arrays.fill(finalTable, ((double)CANVAS_HEIGHT/2));
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
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.blue);
        
        if(finalTable != null)
        {
	        for(int i = 0; i < finalTable.length-1; i++)
	        {
	        	g2.drawLine(i,(int)finalTable[i],i+1,(int)finalTable[i+1]);
	        }
        }
        
        
    }  
    
    
    
    
} 