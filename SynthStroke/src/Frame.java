import java.awt.Dimension;


public class Frame {

	JFrame frame;
	volatile Canvas waveform, pitch, filter, amp;
	DraggableSpinner waveformXSpinner, pitchXSpinner, filterXSpinner, ampXSpinner;
	volatile boolean play = false;
	JButton play_button;
	JTabbedPane tabbedPane;

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
		tabbedPane.setBounds(441, 71, 399, 250);
		tabbedPane.setBackground(Color.LIGHT_GRAY);
		frame.getContentPane().add(tabbedPane);
		
		
		waveform = new Canvas();
		tabbedPane.addTab("Waveform", null, waveform, null);
		waveform.setBackground(Color.WHITE);
		waveform.setLayout(null);
		
		
		
		pitch = new Canvas();
		pitch.setBackground(Color.WHITE);
		tabbedPane.addTab("Pitch", null, pitch, null);
		
		filter = new Canvas();
		filter.setBackground(Color.WHITE);
		tabbedPane.addTab("Filter", null, filter, null);

		
		amp = new Canvas();
		amp.setBackground(Color.WHITE);
		tabbedPane.addTab("Amplitude", null, amp, null);
		
		waveformXSpinner = new DraggableSpinner(0.0, -10.0, 10.0, .1, true);
		waveformXSpinner.setBounds(721, 334, 119, 28);
		frame.getContentPane().add(waveformXSpinner);
		if(tabbedPane.getSelectedIndex() == 0){
			waveformXSpinner.setVisible(true);
		}else{
			waveformXSpinner.setVisible(false);
		}
		
		
		pitchXSpinner = new DraggableSpinner(0.0, -10.0, 10.0, .1, true);
		pitchXSpinner.setBounds(721, 334, 119, 28);
		frame.getContentPane().add(pitchXSpinner);
		if(tabbedPane.getSelectedIndex() == 1){
			pitchXSpinner.setVisible(true);
		}else{
			pitchXSpinner.setVisible(false);
		}
		
		
		filterXSpinner = new DraggableSpinner(0.0, -10.0, 10.0, .1, true);
		filterXSpinner.setBounds(721, 334, 119, 28);
		frame.getContentPane().add(filterXSpinner);
		if(tabbedPane.getSelectedIndex() == 2){
			filterXSpinner.setVisible(true);
		}else{
			filterXSpinner.setVisible(false);
		}
		
		
		ampXSpinner = new DraggableSpinner(0.0, -10.0, 10.0, .1, true);
		ampXSpinner.setBounds(721, 334, 119, 28);
		frame.getContentPane().add(ampXSpinner);
		if(tabbedPane.getSelectedIndex() == 3){
			ampXSpinner.setVisible(true);
		}else{
			ampXSpinner.setVisible(false);
		}
		
		
	    ChangeListener changeListener = new ChangeListener() {
	        public void stateChanged(ChangeEvent changeEvent) {
	          JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
	          int index = sourceTabbedPane.getSelectedIndex();
	          setXSlider(index);
	          //System.out.println("Tab changed to: " + sourceTabbedPane.getTitleAt(index));
	          
	        }
	      };
	      tabbedPane.addChangeListener(changeListener);
		
		play_button = new JButton("Play");
		play_button.setBounds(451, 333, 74, 29);
        play_button.addActionListener(new ActionListener() {
        	 
            public void actionPerformed(ActionEvent e)
            {
            	setPlay(true);
            }
        }); 
		frame.getContentPane().add(play_button);
		
		JButton clear_button = new JButton("Clear");
		clear_button.setBounds(545, 333, 87, 29);
		clear_button.addActionListener(new ActionListener() {
       	 
            public void actionPerformed(ActionEvent e)
            {
            	clear();
            }
        }); 
		frame.getContentPane().add(clear_button);
		
		
		
		JLabel skin = new JLabel();
		skin.setIcon(new ImageIcon("res/GUI 9.png"));
		skin.setBounds(0, 0, 914, 551);
		frame.getContentPane().add(skin);
		

		
		// must call pack before setting canvases
		frame.pack();
		
		// set canvases
		waveform.set(waveform.getWidth(), waveform.getHeight());
		pitch.set(pitch.getWidth(), pitch.getHeight());
		filter.set(filter.getWidth(), filter.getHeight());
		amp.set(amp.getWidth(), amp.getHeight());
		
		// must call frame.setResizable() after frame.pack()
		frame.setResizable(false);

		
	}
	
	public boolean getPlay(){
		return play;
	}
	
	public void setPlay(boolean b){
		play = b;
	}
	
	public void clear(){
		getCurrentCanvas().clearCanvas();
		getCurrentCanvas().repaint();
	}
	
	
	public void setXSlider(int index){
		switch(index){
		case 0:
			waveformXSpinner.setVisible(true);
			pitchXSpinner.setVisible(false);
			filterXSpinner.setVisible(false);
			ampXSpinner.setVisible(false);
			break;
		case 1:
			waveformXSpinner.setVisible(false);
			pitchXSpinner.setVisible(true);
			filterXSpinner.setVisible(false);
			ampXSpinner.setVisible(false);
			break;			
		case 2:
			waveformXSpinner.setVisible(false);
			pitchXSpinner.setVisible(false);
			filterXSpinner.setVisible(true);
			ampXSpinner.setVisible(false);
			break;			
		case 3:
			waveformXSpinner.setVisible(false);
			pitchXSpinner.setVisible(false);
			filterXSpinner.setVisible(false);
			ampXSpinner.setVisible(true);
			break;			
		}
	}
	
	// returns canvas table
	public double[] getTable(Canvas c) {
		return c.getTable();
	}
	
	// return waveform canvas
	public Canvas getWaveformCanvas(){
		if(waveform == null){
			System.out.println("waveform canvas null");
			return null;
		}else{
			return waveform;
		}
	}
	
	// return pitch canvas
	public Canvas getPitchCanvas(){
		if(pitch == null){
			System.out.println("pitch canvas null");
			return null;
		}else{
			return pitch;
		}
	}
	
	// return filter canvas
	public Canvas getFilterCanvas(){
		if(filter == null){
			System.out.println("filter canvas null");
			return null;
		}else{
			return filter;
		}
	}
	
	// return amp canvas
	public Canvas getAmpCanvas(){
		if(amp == null){
			System.out.println("amp canvas null");
			return null;
		}else{
			return amp;
		}
	}
	
	public Canvas getCurrentCanvas(){
		int index = tabbedPane.getSelectedIndex();
		switch(index){
		case 0:
			return getWaveformCanvas();
		case 1:
			return getPitchCanvas();
		case 2:
			return getFilterCanvas();
		case 3:
			return getAmpCanvas();
		default:
			System.out.println("Frame.getCurrentCanvas() null");
			return null;
		}
		
		
	}
}
