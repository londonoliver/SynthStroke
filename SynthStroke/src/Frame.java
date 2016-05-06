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

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


public class Frame {

	JFrame frame;
	volatile Canvas waveformCanvas, pitchCanvas, filterCanvas, ampCanvas;
	volatile boolean play = false;
	JButton play_button, clear_button;
	JTabbedPane tabbedPane;
	JComboBox comboBox, oscillatorComboBox;
	JLabel slot1Label, slot2Label, slot3Label, slot4Label;
	volatile DraggableSpinner filterTypeSpinner, filterResonanceSpinner, filterAmplitudeSpinner, filterFrequencySpinner;
	volatile DraggableSpinner pitchFrequencySpinner, pitchAmplitudeSpinner, ampAmplitudeSpinner, ampDurationSpinner;
	JButton exportButton;
	JLabel skin;
	JPanel screenPanel;
	JMenuBar menuBar;
	JMenu menu;
	JFileChooser fileChooser;
    int returnValue;
    File file = null;
    JPanel keyboardPanel;
    Main main;
	   

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
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		frame.setPreferredSize(new Dimension(914, 573));	
		frame.setTitle("Synth Stroke");
		frame.getContentPane().setLayout(null);			
		frame.setVisible(true);
		
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(441, 71, 399, 242); // change width value (399) to 381 for canvas width to be 360 pixels
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
		play_button.setBounds(441, 325, 74, 29);
		frame.getContentPane().add(play_button);
        play_button.addActionListener(new ActionListener() {
        	 
            public void actionPerformed(ActionEvent e)
            {
            	setPlay(true);
            }
        }); 
				
		
		clear_button = new JButton("Clear");
		clear_button.setBounds(527, 325, 87, 29);
		clear_button.addActionListener(new ActionListener() {
       	 
            public void actionPerformed(ActionEvent e)
            {
            	clear();
            }
        }); 
		frame.getContentPane().add(clear_button);
		clear_button.setVisible(false);
		
		
        
	    ChangeListener changeListener = new ChangeListener() {
	        public void stateChanged(ChangeEvent changeEvent) {
	          JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
	          int index = sourceTabbedPane.getSelectedIndex();
	          setSpinners(index);	      
	          sourceTabbedPane.requestFocus();
	        }
	      };
	    tabbedPane.addChangeListener(changeListener);
	    
	    exportButton = new JButton("Export");
	    exportButton.setBounds(723, 325, 117, 29);
	    frame.getContentPane().add(exportButton);

	    
	    screenPanel = new JPanel();
	    screenPanel.setBounds(89, 88, 282, 277);
	    frame.getContentPane().add(screenPanel);
	    screenPanel.setOpaque(false);
        screenPanel.setBackground(new Color(0,0,0,0));
        screenPanel.setLayout(null);
        
        
        slot1Label = new JLabel("Filter");
        slot1Label.setBounds(6, 6, 133, 14);
        screenPanel.add(slot1Label);
        slot1Label.setFont(new Font("Courier", Font.PLAIN, 13));
        slot1Label.setForeground(Color.darkGray);
        
        slot2Label = new JLabel("Frequency");
        slot2Label.setBounds(6, 40, 133, 14);
        screenPanel.add(slot2Label);
        slot2Label.setFont(new Font("Courier", Font.PLAIN, 13));
        slot2Label.setForeground(Color.darkGray);
        
        slot3Label = new JLabel("Amplitude");
        slot3Label.setBounds(6, 74, 133, 14);
        screenPanel.add(slot3Label);
        slot3Label.setFont(new Font("Courier", Font.PLAIN, 13));
        slot3Label.setForeground(Color.darkGray);
        
        slot4Label = new JLabel("Resonance");
        slot4Label.setBounds(6, 108, 133, 14);
        screenPanel.add(slot4Label);
        slot4Label.setFont(new Font("Courier", Font.PLAIN, 13));
        slot4Label.setForeground(Color.darkGray);
		
		filterResonanceSpinner = new DraggableSpinner(0.1, 0.1, 5.0, 0.1, true, false); // must be >= 0.1
		filterResonanceSpinner.setBounds(157, 108, 125, 16);
		screenPanel.add(filterResonanceSpinner);
		
		
		filterFrequencySpinner = new DraggableSpinner(20000.0, 20.0, 20000.0, 10.0, true, false);
		filterFrequencySpinner.setBounds(157, 40, 125, 16);
		screenPanel.add(filterFrequencySpinner);
		filterFrequencySpinner.getSpinner().setLocation(0, 0);
		filterFrequencySpinner.setUnits("Hz");
		
		filterAmplitudeSpinner = new DraggableSpinner(0.0, 0.0, validMaxValue(filterFrequencySpinner.getValue()), 10.0, true, false);
		filterAmplitudeSpinner.setBounds(157, 74, 122, 16);
		screenPanel.add(filterAmplitudeSpinner);
		
		
		filterTypeSpinner = new DraggableSpinner(0.0, 0.0, 0.0, 0.0, true, true);
		filterTypeSpinner.setBounds(157, 6, 125, 16);
		screenPanel.add(filterTypeSpinner);
		
		pitchFrequencySpinner = new DraggableSpinner(800.0, 20.0, 20000.0, 10.0, true, false);
		pitchFrequencySpinner.setBounds(157, 6, 125, 16);
		screenPanel.add(pitchFrequencySpinner);
		pitchFrequencySpinner.setUnits("Hz");
		
		pitchAmplitudeSpinner = new DraggableSpinner(0.0, 0.0, validMaxValue(pitchFrequencySpinner.getValue()), 10.0, true, false);
		pitchAmplitudeSpinner.setBounds(157, 40, 125, 16);
		screenPanel.add(pitchAmplitudeSpinner);
		
		ampAmplitudeSpinner = new DraggableSpinner(0.2, 0.0, 0.5, 0.1, true, false);
		ampAmplitudeSpinner.setBounds(157, 6, 125, 16);
		screenPanel.add(ampAmplitudeSpinner);
		
		ampDurationSpinner = new DraggableSpinner(1.0, 0.5, 5.0, 0.1, true, false);
		ampDurationSpinner.setBounds(157, 40, 125, 16);
		screenPanel.add(ampDurationSpinner);
		ampDurationSpinner.setUnits("sec");
		
		
		comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {
				"Sawtooth", "Sine", "Square", "Triangle", "Noise"}));
		comboBox.setBounds(561, 397, 114, 28);
		frame.getContentPane().add(comboBox);
        comboBox.addActionListener(new ActionListener() {
       	 
            public void actionPerformed(ActionEvent e)
            {
            	JComboBox comboBox = (JComboBox)e.getSource();
            	switch(comboBox.getSelectedIndex())
            	{
            	case 0:
            		getCurrentCanvas().setFinalTable(getCurrentCanvas().sawTable());
            		getCurrentCanvas().repaint();
            		setFunctionOscillators();
            		break;
            	case 1:
            		getCurrentCanvas().setFinalTable(getCurrentCanvas().sineTable());
            		getCurrentCanvas().repaint();
            		setFunctionOscillators();
            		break;
            	case 2:
            		getCurrentCanvas().setFinalTable(getCurrentCanvas().squareTable());
            		getCurrentCanvas().repaint();
            		setFunctionOscillators();
            		break;
            	case 3:
            		getCurrentCanvas().setFinalTable(getCurrentCanvas().triangleTable());
            		getCurrentCanvas().repaint();
            		setFunctionOscillators();
            		break;
            	case 4:
            		getCurrentCanvas().setFinalTable(getCurrentCanvas().noiseTable());
            		getCurrentCanvas().repaint();
            		setFunctionOscillators();
            		break;
            	default:
            		System.out.println("unsupported comboBox index: " + comboBox.getSelectedIndex());
            	}
            }
        });
		
		
        
        fileChooser = new JFileChooser();
        
        
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
        
        
		skin = new JLabel("");
		skin.setIcon(new ImageIcon("res/GUI 12.png"));
		skin.setBounds(0, 0, 914, 551);
		frame.getContentPane().add(skin);
		
		//------------ Spinner Listeners ----------------------------------------
		
		ampDurationSpinner.getSpinner().addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				double duration = secondsToHertz(ampDurationSpinner.getValue());
				main.pitchFunctionOsc.frequency.set(duration);
				main.filterFunctionOsc.frequency.set(duration);
				main.ampFunctionOsc.frequency.set(duration);
				
				main.holdAdd.inputB.set(ampDurationSpinner.getValue());
			}	
		});
		
		filterTypeSpinner.getSpinner().addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				setSpinners(tabbedPane.getSelectedIndex());
			}	
		});
		
		filterFrequencySpinner.getSpinner().addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				double max = validMaxValue(filterFrequencySpinner.getValue());
				filterAmplitudeSpinner.setMax(max);
				if(filterAmplitudeSpinner.getValue() > max)
				{
					filterAmplitudeSpinner.setValue(max);
				}
			}	
		});
		
		
		pitchFrequencySpinner.getSpinner().addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e) {
				double max = validMaxValue(pitchFrequencySpinner.getValue());
				pitchAmplitudeSpinner.setMax(max);
				if(pitchAmplitudeSpinner.getValue() > max)
				{
					pitchAmplitudeSpinner.setValue(max);
				}
				//main.pitchAmplitude = pitchAmplitudeSpinner.getValue();
			}
			
		});
		
		
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

		
		
		
		
		setSpinners(0);

		
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
		getCurrentCanvas().clearCanvas();
		getCurrentCanvas().repaint();
		main.pitchFunctionOsc.function.set(new DoubleTable(pitchCanvas.getNormalizedTable()));
		main.filterFunctionOsc.function.set(new DoubleTable(filterCanvas.getNormalizedTable()));
	}
	
	
	public void setSpinners(int index){
		switch(index){
		
		case 0: //waveform
										
			clear_button.setVisible(false);
			
			try { // make the program less responsive (L&F)
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			slot1Label.setText("Choose Waveform");
			slot2Label.setText("");
			slot3Label.setText("");
			slot4Label.setText("");
			
			pitchFrequencySpinner.setVisible(false);
			pitchAmplitudeSpinner.setVisible(false);
			
			filterTypeSpinner.setVisible(false);
			filterResonanceSpinner.setVisible(false);
			filterAmplitudeSpinner.setVisible(false);
			filterFrequencySpinner.setVisible(false);
			
			ampAmplitudeSpinner.setVisible(false);
			ampDurationSpinner.setVisible(false);
			
			comboBox.setVisible(false);
			
			
			break;
			
		case 1: //pitch
						
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			clear_button.setVisible(true);
			
			slot1Label.setText("Frequency");
			slot2Label.setText("Amplitude");
			slot3Label.setText("");
			slot4Label.setText("");
			
			pitchFrequencySpinner.setVisible(true);
			pitchAmplitudeSpinner.setVisible(true);
			
			filterTypeSpinner.setVisible(false);
			filterResonanceSpinner.setVisible(false);
			filterAmplitudeSpinner.setVisible(false);
			filterFrequencySpinner.setVisible(false);
			
			ampAmplitudeSpinner.setVisible(false);
			ampDurationSpinner.setVisible(false);
			
			comboBox.setVisible(true);
			
			break;			
		
		case 2: //filter
			
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
						
			clear_button.setVisible(true);
			
			slot1Label.setText("Filter");
			slot2Label.setText("Frequency");
			slot3Label.setText("Amplitude");
			slot4Label.setText("Resonance");
			
			pitchFrequencySpinner.setVisible(false);
			pitchAmplitudeSpinner.setVisible(false);
			
			filterTypeSpinner.setVisible(true);
			if(getFilterType() == 0)
			{
				filterResonanceSpinner.setVisible(false);
				filterAmplitudeSpinner.setVisible(false);
				filterFrequencySpinner.setVisible(false);
				
				slot2Label.setVisible(false);
				slot3Label.setVisible(false);
				slot4Label.setVisible(false);
			}
			else
			{
				filterResonanceSpinner.setVisible(true);
				filterAmplitudeSpinner.setVisible(true);
				filterFrequencySpinner.setVisible(true);
				
				slot2Label.setVisible(true);
				slot3Label.setVisible(true);
				slot4Label.setVisible(true);
			}
			
			ampAmplitudeSpinner.setVisible(false);
			ampDurationSpinner.setVisible(false);
			
			comboBox.setVisible(true);
			
			break;	
			
		case 3: // amp
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			clear_button.setVisible(true);
			
			slot1Label.setText("Amplitude");
			slot2Label.setText("Duration");
			slot3Label.setText("");
			slot4Label.setText("");
			
			pitchFrequencySpinner.setVisible(false);
			pitchAmplitudeSpinner.setVisible(false);
			
			filterTypeSpinner.setVisible(false);
			filterResonanceSpinner.setVisible(false);
			filterAmplitudeSpinner.setVisible(false);
			filterFrequencySpinner.setVisible(false);
			
			ampAmplitudeSpinner.setVisible(true);
			ampDurationSpinner.setVisible(true);
			
			comboBox.setVisible(true);
			
			break;
		
		default: 
			
			System.out.println("Something went wrong in Frame.setSpinners()");
		
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
	
	
	
	public int getFilterType()
	{
		int type;
		String string = (String)filterTypeSpinner.spinner.getValue();
		if(string == "none"){
			type = 0;
		}else if(string == "lowpass"){
			type = 1;
		}else if(string == "bandpass"){
			type = 2;
		}else if(string == "highpass"){
			type = 3;
		}else{
			type = 0;
			System.out.println("Something went wrong in Frame.getFilterType(), filter type = " + string);
		}
		return type;
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
}
