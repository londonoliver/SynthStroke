import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import java.awt.Color;

import javax.swing.UIManager;

import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.BorderLayout;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.JSeparator;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JRadioButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jsyn.data.DoubleTable;
import com.jsyn.unitgen.PowerOfTwo;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.SquareOscillatorBL;
import com.jsyn.unitgen.TriangleOscillator;
import com.jsyn.util.VoiceAllocator;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


public class Frame {

	JFrame frame;
	volatile Canvas waveformCanvas, pitchCanvas, filterCanvas, ampCanvas;
	volatile boolean play = false;
	JButton play_button, clear_button;
	JTabbedPane tabbedPane;
	JComboBox oscillatorComboBox;
	volatile DraggableSpinner ampDurationSpinner, frequencySpinner;
	JButton exportButton;
	JLabel skin;
	JMenuBar menuBar;
	JMenu menu;
	JFileChooser fileChooser;
    int returnValue;
    File file = null;
    JPanel keyboardPanel;
    Main main;
    private JLabel sineButton;
    private JLabel sawButton;
    private JLabel squareButton;
    private JLabel triangleButton;
    private JLabel noiseButton;
    public MidiPiano midiPiano;
    
    public Synth recorder;
	   

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame window = new Frame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Frame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		recorder = new Synth();
		recorder.setFrame(this);
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		frame.setPreferredSize(new Dimension(914, 573));	
		frame.setTitle("Synth Stroke");
		frame.getContentPane().setLayout(null);			
		frame.setVisible(true);
		
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(208, 49, 465, 312); // change width value (399) to 381 for canvas width to be 360 pixels
		tabbedPane.setBackground(Color.LIGHT_GRAY);
		frame.getContentPane().add(tabbedPane);

		
		oscillatorComboBox = new JComboBox();
		oscillatorComboBox.setModel(new DefaultComboBoxModel(new String[] {"Sawtooth", "Sine", "Square", "Triangle", "Noise"}));
		tabbedPane.addTab("Oscillator", null, oscillatorComboBox, null);

		
						
		pitchCanvas = new Canvas();
		pitchCanvas.setBackground(Color.WHITE);
		tabbedPane.addTab("Pitch", null, pitchCanvas, null);
		
		filterCanvas = new Canvas();
		filterCanvas.setBackground(Color.WHITE);
		tabbedPane.addTab("Filter", null, filterCanvas, null);

		
		ampCanvas = new Canvas();
		ampCanvas.setBackground(Color.WHITE);
		tabbedPane.addTab("Amplitude", null, ampCanvas, null);

		
		
	    play_button = new JButton("Play");
		play_button.setBounds(225, 386, 74, 29);
		frame.getContentPane().add(play_button);
		play_button.addActionListener(new ActionListener() {
	       	 
            public void actionPerformed(ActionEvent e)
            {
            	recorder.init(false);
            }
        });
				
		
		clear_button = new JButton("Clear");
		clear_button.setBounds(324, 386, 87, 29);
		clear_button.addActionListener(new ActionListener() {
       	 
            public void actionPerformed(ActionEvent e)
            {
            	clear();
            }
        }); 
		frame.getContentPane().add(clear_button);
		
		
        
	    ChangeListener changeListener = new ChangeListener() {
	        public void stateChanged(ChangeEvent changeEvent) {
	          JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
	          int index = sourceTabbedPane.getSelectedIndex();     
	          sourceTabbedPane.requestFocus();
	        }
	      };
	    tabbedPane.addChangeListener(changeListener);
	    
	    exportButton = new JButton("Export");
	    exportButton.setBounds(618, 386, 117, 29);
	    frame.getContentPane().add(exportButton);
	    exportButton.addActionListener(new ActionListener() {
	       	 
            public void actionPerformed(ActionEvent e)
            {
            	recorder.init(true);
            }
        });
	    
	    ampDurationSpinner = new DraggableSpinner(1.0, 0.5, 5.0, 0.1, true, false);
	    ampDurationSpinner.setBackground(Color.WHITE);
	    ampDurationSpinner.setForeground(Color.WHITE);
	    ampDurationSpinner.setBounds(65, 399, 125, 16);
	    frame.getContentPane().add(ampDurationSpinner);
	    ampDurationSpinner.setUnits("sec");
	    
	    //------------ Spinner Listeners ----------------------------------------
	    
	    ampDurationSpinner.getSpinner().addChangeListener(new ChangeListener(){

	    	public void stateChanged(ChangeEvent e) {
	    		double duration = getDuration();
	    		main.pitchFunctionOsc.frequency.set(duration);
	    		main.filterFunctionOsc.frequency.set(duration);
	    		main.ampFunctionOsc.frequency.set(duration);
	    		
	    		main.holdAdd.inputB.set(ampDurationSpinner.getValue());
	    	}	
	    });
		
		
        
        fileChooser = new JFileChooser();
        
        frequencySpinner = new DraggableSpinner(440.0, 10.0, 20000.0, 10.0, true, false);
        frequencySpinner.setUnits("Hz");
        frequencySpinner.setBounds(455, 386, 117, 16);
        frame.getContentPane().add(frequencySpinner);
        
        
        menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 914, 22);
		frame.getContentPane().add(menuBar);
		
		menu = new JMenu("File");
		menuBar.add(menu);
		
		JMenuItem saveMenuItem = new JMenuItem("Save");
		menu.add(saveMenuItem);
		saveMenuItem.addActionListener(new ActionListener()
		{ 
			public void actionPerformed(ActionEvent e) 
			{
				if(file == null)
				{
					returnValue = fileChooser.showSaveDialog(null);
			        if (returnValue == JFileChooser.APPROVE_OPTION) {
			        	file = fileChooser.getSelectedFile();
			        }
				}
				
				if(file == null)
				{
					System.out.println("No file chosen");
				}
				else
				{
					try {
						ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
						outputStream.writeObject(this);  // Write this Frame object
						outputStream.close();
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
			}
			
		});
		
		JMenuItem saveAsMenuItem = new JMenuItem("Save As");
		menu.add(saveAsMenuItem);
		saveAsMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				returnValue = fileChooser.showSaveDialog(null);
		        if (returnValue == JFileChooser.APPROVE_OPTION) {
		        	file = fileChooser.getSelectedFile();
		        	try {
						ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
						outputStream.writeObject(this);  // Write this Frame object
						outputStream.close();
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

		        }
			}
			
		});
		
		
		JMenuItem loadMenuItem = new JMenuItem("Load");
		menu.add(loadMenuItem);
		loadMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				returnValue = fileChooser.showOpenDialog(null);
		        if (returnValue == JFileChooser.APPROVE_OPTION) {
		        	file = fileChooser.getSelectedFile();
		        	try {
						ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
						inputStream.read();
						inputStream.close();
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
		        }		       
			}
			
		});
		
		sineButton = new JLabel("");
		sineButton.setBounds(674, 49, 40, 33);
		frame.getContentPane().add(sineButton);
		sineButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				setWaveform(1);
				super.mouseClicked(e);
			}
		});
		
		sawButton = new JLabel("");
		sawButton.setBounds(711, 49, 40, 33);
		frame.getContentPane().add(sawButton);
		sawButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				setWaveform(0);
				super.mouseClicked(e);
			}
		});
		
		squareButton = new JLabel("");
		squareButton.setBounds(674, 83, 40, 39);
		frame.getContentPane().add(squareButton);
		squareButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				setWaveform(2);
				super.mouseClicked(e);
			}
		});
		
		triangleButton = new JLabel("");
		triangleButton.setBounds(711, 83, 40, 39);
		frame.getContentPane().add(triangleButton);
		triangleButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				setWaveform(3);
				super.mouseClicked(e);
			}
		});
		
		noiseButton = new JLabel("");
		noiseButton.setBounds(674, 121, 40, 33);
		frame.getContentPane().add(noiseButton);
		noiseButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				setWaveform(4);
				super.mouseClicked(e);
			}
		});
		
		midiPiano = new MidiPiano();
		midiPiano.setBounds(51, 436, 815, 109);
		frame.getContentPane().add(midiPiano);
        
        
		skin = new JLabel("");
		skin.setIcon(new ImageIcon("/Users/London/Documents/workspace/SynthStroke2/res/GUI 13.png"));
		skin.setBounds(0, 0, 914, 551);
		frame.getContentPane().add(skin);
		
		
		//--------------- Mouse Listeners for Canvases -------------------------------------
		
		
		pitchCanvas.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
            	setFunctionOscillators();        
            }
        
        });
		
		filterCanvas.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
            	setFunctionOscillators();      
            }
        
        });
		
		ampCanvas.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
            	setFunctionOscillators();          
            }
        
        });
		
		
		skin.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JLabel label = (JLabel)e.getSource();
                label.requestFocus(); // takes focus away from draggablespinners if they have focus
            }
        });

		
		
		// must call pack before setting canvases
		frame.pack();
		
		// set canvases
		//waveformCanvas.set(waveformCanvas.getWidth(), waveformCanvas.getHeight());
		pitchCanvas.set(pitchCanvas.getWidth(), pitchCanvas.getHeight());
		filterCanvas.set(filterCanvas.getWidth(), filterCanvas.getHeight());
		ampCanvas.set(ampCanvas.getWidth(), ampCanvas.getHeight());
		
		
		
		// must call frame.setResizable() after frame.pack()
		frame.setResizable(false);

		
	} // end initialize
	
	public boolean getPlay(){
		return play;
	}
	
	public void setPlay(boolean b){
		play = b;
	}
	
	public void clear(){
		Canvas canvas = getCurrentCanvas();
		if(canvas != null)
		{
			getCurrentCanvas().clearCanvas();
			getCurrentCanvas().repaint();
			main.pitchFunctionOsc.function.set(new DoubleTable(pitchCanvas.getNormalizedTable()));
			main.filterFunctionOsc.function.set(new DoubleTable(filterCanvas.getNormalizedTable()));
			main.ampFunctionOsc.function.set(new DoubleTable(filterCanvas.getNormalizedTable()));
		}
	}
	
	
	
	
	// returns canvas table
	public double[] getTable(Canvas c) {
		return c.getNormalizedTable();
	}
	
	
	
	public Canvas getCurrentCanvas(){
		int index = tabbedPane.getSelectedIndex();
		switch(index){
		case 0:
			System.out.println("No waveform canvas, return null");
			return null;
		case 1:
			return pitchCanvas;
		case 2:
			return filterCanvas;
		case 3:
			return ampCanvas;
		default:
			System.out.println("Frame.getCurrentCanvas() null");
			return null;
		}		
	}

	
	// returns a valid maximum value for a spinner given a middle frequency
	public double validMaxValue(double middleFrequency){
		double d1 = middleFrequency;
		double d2 = 20000.0 - d1;
		return Math.min(d1, d2);
	}
	
	public void setMain(Main m){
		main = m;
	}
	
	public static double secondsToHertz(double seconds)
	{
		double hertz = 1/seconds;
		return hertz;
	}
	
	public void setFunctionOscillators()
	{
		main.pitchFunctionOsc.function.set(new DoubleTable(pitchCanvas.getNormalizedTable()));
		main.filterFunctionOsc.function.set(new DoubleTable(filterCanvas.getNormalizedTable()));
		main.ampFunctionOsc.function.set(new DoubleTable(ampCanvas.getNormalizedTable()));
	}
	
	public void setWaveform(int i)
	{
		Canvas canvas = getCurrentCanvas();
		if(canvas != null)
		{
			switch(i)
	    	{
	    	case 0: // saw
	    		canvas.setFinalTable(canvas.sawTable());
	    		canvas.repaint();
	    		setFunctionOscillators();
	    		break;
	    	case 1: // sine
	    		canvas.setFinalTable(canvas.sineTable());
	    		canvas.repaint();
	    		setFunctionOscillators();
	    		break;
	    	case 2: // square
	    		canvas.setFinalTable(canvas.squareTable());
	    		canvas.repaint();
	    		setFunctionOscillators();
	    		break;
	    	case 3: // triangle
	    		canvas.setFinalTable(canvas.triangleTable());
	    		canvas.repaint();
	    		setFunctionOscillators();
	    		break;
	    	case 4: // noise
	    		canvas.setFinalTable(canvas.noiseTable());
	    		canvas.repaint();
	    		setFunctionOscillators();
	    		break;
	    	default:
	    		System.out.println("setWaveform(); unsupported waveform index: " + i);
	    	}
		}
	}
	
	public double getDuration()
	{
		return secondsToHertz(ampDurationSpinner.getValue());
	}
	
	public MidiPiano getMidiPiano()
	{
		return midiPiano;
	}
}