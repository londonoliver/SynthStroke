import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.DoubleTable;
import com.jsyn.midi.MessageParser;
import com.jsyn.unitgen.*;
import com.jsyn.util.WaveRecorder;



public class Synth 
{
	private static final long serialVersionUID = -3223845556175532115L;
	
	boolean export; 
	volatile boolean play = false;
	File exportFile;
	Frame frame;
	
	public Synth()
	{
		
	}

	public void init(boolean record)
	{	
		
		Synthesizer synth;
		FunctionOscillator waveFunction, pitchFunction, filterFunction, ampFunction;
		LineOut lineOut;
		Add freqAdder, filterAdder, ampAdder;
		DoubleTable waveTable, pitchTable, filterTable, ampTable;
		UnitOscillator oscillator;
		WhiteNoise noise;	
		WaveRecorder recorder = null;	
		double[] waveArray, pitchArray, filterArray, ampArray;
		double pitchFrequency, pitchAmplitude, filterAmplitude, ampAmplitude;
		double volume = 0.5;	
		double duration = 0.0;
		double filterFrequency, resonance;	
		int oscillatorIndex, filterIndex;	
		PowerOfTwo pitchPowerOfTwo;
	    PowerOfTwo filterPowerOfTwo;
	    PowerOfTwo ampPowerOfTwo;
	    
	    UnitOscillator lfo;
	    
	    UnitOscillator osc;  // made public, used to be private
	    FilterLowPass filter; // made public, was private
	    EnvelopeDAHDSR ampEnv;
	    Add cutoffAdder;
	    Multiply frequencyScaler;
	    Multiply filterScaler;
	    Multiply ampScaler;
	    
	    
	    FunctionOscillator pitchFunctionOsc;
	    
	    FunctionOscillator filterFunctionOsc;
	    double cutoff;
	    double Q;
	    double amplitude;
	    double frequency;
	    
	    FunctionOscillator ampFunctionOsc;

	    double hold = 1.0;
	    Add holdAdd;
	    
	    File exportFile = null;
	    
	    if(record)
	    {	    
			int returnValue;
			JFileChooser fileChooser = new JFileChooser();
			
			returnValue = fileChooser.showDialog(null, "Export");
			if (returnValue == JFileChooser.APPROVE_OPTION) {
	        	exportFile = fileChooser.getSelectedFile();
	        }
			
			if(exportFile == null)
			{
				record = false;
				System.out.println("No file chosen");
				return;
			}
	    }
		
		
		duration = frame.ampDurationSpinner.getValue();
		
		
		
		// Create a context for the synthesizer.
		synth = JSyn.createSynthesizer();
		
		// Start synthesizer using default stereo output at 44100 Hz.
		synth.start();
		
		synth.add(frequencyScaler = new Multiply());
		synth.add(filterScaler = new Multiply());
		synth.add(ampScaler = new Multiply());
        
        // Add a tone generator.
		synth.add(osc = new SawtoothOscillatorBL());

        // Use an envelope to control the amplitude.
		synth.add(ampEnv = new EnvelopeDAHDSR());

        // Use an envelope to control the filter cutoff.
		synth.add(filter = new FilterLowPass());
		synth.add(cutoffAdder = new Add());

        
        // --------- Pitch Stuff --------------------------------
        
        pitchArray = frame.pitchCanvas.getNormalizedTable();
        pitchFunctionOsc = new FunctionOscillator();
        pitchTable = new DoubleTable(pitchArray);
        pitchFunctionOsc.function.set(pitchTable);
        
        synth.add(pitchPowerOfTwo = new PowerOfTwo());
        synth.add(pitchFunctionOsc);
        
        pitchFunctionOsc.output.connect(pitchPowerOfTwo.input);
        pitchFunctionOsc.amplitude.set(0.5);
        pitchFunctionOsc.frequency.set(secondsToHertz(duration));
        
                
        // --------- Filter Stuff --------------------------------
        
        filterArray = frame.filterCanvas.getNormalizedTable();
        filterFunctionOsc = new FunctionOscillator();
        filterTable = new DoubleTable(filterArray);
        filterFunctionOsc.function.set(filterTable);
        
        synth.add(filterPowerOfTwo = new PowerOfTwo());
        synth.add(filterFunctionOsc);
        
        filterFunctionOsc.output.connect(filterPowerOfTwo.input);
        filterFunctionOsc.amplitude.set(0.5);
        filterFunctionOsc.frequency.set(secondsToHertz(duration));
        
        // --------- Amp Stuff -------------------------------------
        
        ampArray = frame.ampCanvas.getNormalizedTable();
        ampFunctionOsc = new FunctionOscillator();
        ampTable = new DoubleTable(ampArray);
        ampFunctionOsc.function.set(ampTable);
        
        synth.add(ampPowerOfTwo = new PowerOfTwo());
        synth.add(ampFunctionOsc);
        
        ampFunctionOsc.output.connect(ampPowerOfTwo.input);
        ampFunctionOsc.amplitude.set(1.0);
        ampFunctionOsc.frequency.set(secondsToHertz(duration));
        
        
        // -------- Connect Adders -----------------------------
        
        cutoffAdder.output.connect(filter.frequency);
        frequencyScaler.output.connect(osc.frequency);
        filterScaler.output.connect(filter.frequency);
        ampScaler.output.connect(osc.amplitude);
        osc.output.connect(filter.input);
        filter.output.connect(ampEnv.amplitude);
		
        frequency = frame.frequencySpinner.getValue();
        amplitude = 0.5;
        cutoff = 800.0; 
        filter.Q.set(1.0);
        
        frequencyScaler.inputA.set(frequency);
        filterScaler.inputA.set(cutoff);
        ampScaler.inputA.set(amplitude);
        
        
        pitchPowerOfTwo.output.connect(frequencyScaler.inputB);
        filterPowerOfTwo.output.connect(filterScaler.inputB);
        ampPowerOfTwo.output.connect(ampScaler.inputB);
        
        
        
        ampEnv.sustain.set(0.0);
        ampEnv.attack.set(0.01);
        ampEnv.hold.set(duration-0.03);
        ampEnv.decay.set(0.02);
        
        pitchFunctionOsc.phase.setValue(-1);
        filterFunctionOsc.phase.setValue(-1);
        ampFunctionOsc.phase.setValue(-1);
        
        
		// Add a stereo audio output unit.
		
		synth.add( lineOut = new LineOut() );
		
		if(record)
		{
			// Default is stereo, 16 bits.
			try {
				recorder = new WaveRecorder( synth, exportFile );
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}	
			ampEnv.output.connect( 0, recorder.getInput(), 0 ); 
			ampEnv.output.connect( 0, recorder.getInput(), 1 ); 
			recorder.start();
		}
		else
		{		
			ampEnv.output.connect( 0, lineOut.input, 0 );
			ampEnv.output.connect( 0, lineOut.input, 1 );
		}
		
		duration = frame.ampDurationSpinner.getValue();
		ampEnv.input.on();
		// We only need to start the LineOut. It will pull data from the
		// oscillator.
		lineOut.start();

		
		// Sleep while the sound is generated in the background.
		try
		{
			double time = synth.getCurrentTime();
			// Sleep for a few seconds.
			synth.sleepUntil( time + duration );
		} catch( InterruptedException e )
		{
			e.printStackTrace();
		}

		
		
		if(record)
		{
			recorder.stop();
			try {
				recorder.close();
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
		
		// Stop everything.
		synth.stop();
		ampEnv.input.off();
	}
	
	public double[] getTable(Canvas c) {
		return c.getNormalizedTable();
	}
	

	


	public static double secondsToHertz(double seconds)
	{
		double hertz = 1/seconds;
		return hertz;
	}
	
	
	/*public void record()
	{
		
		final Frame frame = new Frame();
		final Synth synth = new Synth();
		final JFileChooser fileChooser = new JFileChooser();
		
		
		
		frame.exportButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				File exportFile = null;
				int returnValue;
				
				returnValue = fileChooser.showDialog(null, "export");
				if (returnValue == JFileChooser.APPROVE_OPTION) {
		        	exportFile = fileChooser.getSelectedFile();
		        }
				
				if(exportFile != null)
				{
					synth.setPitchArray(frame.pitchCanvas.getNormalizedTable());
					synth.setFilterArray(frame.filterCanvas.getNormalizedTable());
					synth.setAmpArray(frame.ampCanvas.getNormalizedTable());
				
					
					synth.set(frame.oscillatorComboBox.getSelectedIndex(),
							frame.pitchFrequencySpinner.getValue(), frame.pitchAmplitudeSpinner.getValue(),
							frame.getFilterType(), frame.filterFrequencySpinner.getValue(), frame.filterAmplitudeSpinner.getValue(), frame.filterResonanceSpinner.getValue(),
							frame.ampAmplitudeSpinner.getValue(), secondsToHertz(frame.ampDurationSpinner.getValue()),
							true, exportFile);
					
					synth.setSoundDuration(frame.ampDurationSpinner.getValue());
					synth.init();
				}
			}
			
		});
		
		while(true){
			if(frame.getPlay()){
				// Set parameters before calling play()
				synth.setPitchArray(frame.pitchCanvas.getNormalizedTable());
				synth.setFilterArray(frame.filterCanvas.getNormalizedTable());
				synth.setAmpArray(frame.ampCanvas.getNormalizedTable());
			
				
				synth.set(frame.oscillatorComboBox.getSelectedIndex(),
						frame.pitchFrequencySpinner.getValue(), frame.pitchAmplitudeSpinner.getValue(),
						frame.getFilterType(), frame.filterFrequencySpinner.getValue(), frame.filterAmplitudeSpinner.getValue(), frame.filterResonanceSpinner.getValue(),
						frame.ampAmplitudeSpinner.getValue(), secondsToHertz(frame.ampDurationSpinner.getValue()),
						false, null);
				
				synth.setSoundDuration(frame.ampDurationSpinner.getValue());
				
				synth.init(); 
				frame.setPlay(false);
			}
			
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
		}
	}*/

	public void setFrame(Frame f)
	{
		frame = f;
	}
}