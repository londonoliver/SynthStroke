import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.DoubleTable;
import com.jsyn.unitgen.*;
import com.jsyn.util.WaveRecorder;



public class Synth
{
	Synthesizer synth;
	FunctionOscillator waveFunction, pitchFunction, filterFunction, ampFunction;
	LineOut lineOut;
	Add freqAdder, filterAdder, ampAdder;
	DoubleTable waveTable, pitchTable, filterTable, ampTable;
	FilterBiquadCommon filter;
	
	UnitOscillator oscillator;
	WhiteNoise noise;
	
	WaveRecorder recorder;
	
	double[] waveArray, pitchArray, filterArray, ampArray;
	double pitchFrequency, duration, pitchAmplitude, filterAmplitude, ampAmplitude;
	double volume = 0.5;
	
	double soundDuration;
	double filterFrequency, resonance;
	
	int oscillatorIndex, filterIndex;
	
	boolean export; 
	File exportFile;
	
	public Synth()
	{
		
	}

	private void init()
	{	
		// Create a context for the synthesizer.
		synth = JSyn.createSynthesizer();
		
		// Start synthesizer using default stereo output at 44100 Hz.
		synth.start();
		
		switch(oscillatorIndex)
		{
		case 0:
			//Sawtooth
			oscillator = new SawtoothOscillatorBL();
			synth.add( (SawtoothOscillatorBL)oscillator );
			break;
		case 1:
			//Sine
			oscillator = new SineOscillator();
			synth.add( (SineOscillator)oscillator );
			break;
		case 2:
			//Square
			oscillator = new SquareOscillatorBL();
			synth.add( (SquareOscillatorBL)oscillator );
			break;
		case 3:
			//Triangle
			oscillator = new TriangleOscillator();
			synth.add( (TriangleOscillator)oscillator );
			break;
		case 4:
			//Noise
			synth.add( noise = new WhiteNoise() );
			break;
		default:
			System.out.println("Something wrong in switch(oscillatorIndex)");
		
		}
		
		switch(filterIndex)
		{
		case 0:
			// none
			filter = new FilterLowPass();
			synth.add((FilterLowPass)filter);
			break;
		case 1:
			// lowpass
			filter = new FilterLowPass();
			synth.add((FilterLowPass)filter);
			break;
		case 2:
			// bandpass
			filter = new FilterBandPass();
			synth.add((FilterBandPass)filter);
			break;
		case 3:
			// highpass
			filter = new FilterHighPass();
			synth.add((FilterHighPass)filter);
			break;
		default:
			System.out.println("Something wrong in switch(filterIndex)");
		}

		// Add UnitGenerators
	
		synth.add( pitchFunction = new FunctionOscillator() );
		synth.add( filterFunction = new FunctionOscillator() );
		synth.add( ampFunction = new FunctionOscillator() );
		synth.add( freqAdder = new Add() );
		synth.add( filterAdder = new Add() );
		synth.add( ampAdder = new Add() );


		

		
		
		// Add DoubleTables
		
		pitchTable = new DoubleTable(pitchArray);
		if(filterIndex == 0){
			filterTable = new DoubleTable(new double[filterArray.length]); // level the filter array
		}
		else
		{
			filterTable = new DoubleTable(filterArray);
		}
		ampTable = new DoubleTable(ampArray);
		
		
		// Add DoubleTables to FunctionOscillators
		
		pitchFunction.function.set( pitchTable );
		filterFunction.function.set( filterTable );
		ampFunction.function.set( ampTable );
		
		
		// Set FunctionOscillator phases to -1
		
		pitchFunction.phase.setValue(-1);
		filterFunction.phase.setValue(-1);
		ampFunction.phase.setValue(-1);
		

		
		// Connect function oscillators to respective parameters
		
		filterFunction.output.connect( filterAdder.inputA );
		filterAdder.output.connect( filter.frequency );	
		filter.Q.set( resonance );  // need to fix, adds dBs, distorts LineOut if not enough headroom
		
		if(oscillatorIndex != 4) 
		{
			pitchFunction.output.connect( freqAdder.inputA );		 
			freqAdder.output.connect( oscillator.frequency );	
			
			oscillator.output.connect(filter.input);
			
			ampFunction.output.connect( ampAdder.inputA );
			ampAdder.output.connect( oscillator.amplitude );
		}
		else
		{	
			noise.output.connect(filter.input);
			
			ampFunction.output.connect( ampAdder.inputA );
			ampAdder.output.connect( noise.amplitude );		
		}
		
		
		// Add a stereo audio output unit.
		
		synth.add( lineOut = new LineOut() );
		
		
		
		if(export)
		{
			// Default is stereo, 16 bits.
			try {
				recorder = new WaveRecorder( synth, exportFile );
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}	
			filter.output.connect( 0, recorder.getInput(), 0 ); 
			filter.output.connect( 0, recorder.getInput(), 1 ); 
			recorder.start();
		}
		else
		{
			// Connect the main waveFunction oscillator to both channels of the output.
			
			filter.output.connect( 0, lineOut.input, 0 );
			filter.output.connect( 0, lineOut.input, 1 );
		}
		

		
			

		
		
		// Set the frequency and amplitude for the sine wave
		
		if(oscillatorIndex != 4)
		{
			oscillator.frequency.set(pitchFrequency);
			oscillator.amplitude.set(volume);
		}
		else
		{
			noise.amplitude.set(volume);
		}
		
		pitchFunction.frequency.set(duration);
		pitchFunction.amplitude.set(pitchAmplitude);
		
		filterFunction.frequency.set(duration);
		filterFunction.amplitude.set(filterAmplitude);
		
		ampFunction.frequency.set(duration);
		ampFunction.amplitude.set(ampAmplitude);
		
		freqAdder.inputB.set(pitchFrequency);
		ampAdder.inputB.set(volume);	
		if(filterIndex == 0)
		{
			filterAdder.inputB.set(21000.0); // with a lowpass filter this is essentially "no" filter
		}
		else
		{
			filterAdder.inputB.set(filterFrequency);
		}
		
				

		// We only need to start the LineOut. It will pull data from the
		// oscillator.
		lineOut.start();

		
		// Sleep while the sound is generated in the background.
		try
		{
			double time = synth.getCurrentTime();
			// Sleep for a few seconds.
			synth.sleepUntil( time + soundDuration );
		} catch( InterruptedException e )
		{
			e.printStackTrace();
		}
		
		
		if(export){
			recorder.stop();
			try {
				recorder.close();
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
		
		
		// Stop everything.
		synth.stop();
	}
	
	public double[] getTable(Canvas c) {
		return c.getNormalizedTable();
	}
	
	public void setWaveArray(double [] wavetable){
		waveArray = wavetable;
	}
	
	public void setPitchArray(double[] pitchtable){
		pitchArray = pitchtable;
	}
	
	public void setFilterArray(double[] filtertable){
		filterArray = filtertable;
	}
	
	public void setAmpArray(double[] amptable){
		ampArray = amptable;
	}
	
	public void setOscillatorXY(double x, double y){
		pitchFrequency = x;
		volume = y;
	}
	
	public void setPitchTableXY(double y){
		pitchAmplitude = y;
	}
	
	public void setFilterTableXY(double x){
		duration = x;
	}
	
	public void setAmpTableXY(double x, double y){
		duration = x;
		ampAmplitude = y;
	}

	public static double secondsToHertz(double seconds)
	{
		double hertz = 1/seconds;
		return hertz;
	}
	
	public void setSoundDuration(double duration)
	{
		soundDuration = duration;
	}
	
	public void setFilterFrequency(double frequency)
	{
		filterFrequency = frequency;
	}
	
	
	public void setOscillatorIndex(int index)
	{
		oscillatorIndex = index;
	}
	
	public void set(int oscIndex, 
			double pitchFreq, double pitchAmp, 
			int filtIndex, double filtFreq, double filtAmp, double filtRes, 
			double ampAmp, double dur,
			boolean export, File exportFile)
	{
		oscillatorIndex = oscIndex;
		pitchFrequency = pitchFreq;
		pitchAmplitude = pitchAmp;
		filterIndex = filtIndex;
		filterFrequency = filtFreq;
		filterAmplitude = filtAmp;
		resonance = filtRes;
		ampAmplitude = ampAmp;
		duration = dur;
		this.export = export;
		this.exportFile = exportFile;
	}
	
	public static void main( String[] args )
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
	}
}
