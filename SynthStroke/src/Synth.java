import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.DoubleTable;
import com.jsyn.unitgen.*;


public class Synth
{
	Synthesizer synth;
	FunctionOscillator waveFunction, pitchFunction, filterFunction, ampFunction;
	LineOut lineOut;
	Add freqAdder, filterAdder, ampAdder;
	DoubleTable waveTable, pitchTable, filterTable, ampTable;
	FilterLowPass filter;
	
	double[] waveArray, pitchArray, filterArray, ampArray;
	double waveX, waveY, pitchX, pitchY, filterX, filterY, ampX, ampY;
	
	public Synth()
	{
		
	}

	private void init()
	{	
		// Create a context for the synthesizer.
		synth = JSyn.createSynthesizer();
		
		// Start synthesizer using default stereo output at 44100 Hz.
		synth.start();
		

		// Add FunctionOscillators
		
		synth.add( waveFunction = new FunctionOscillator() );
		synth.add( pitchFunction = new FunctionOscillator() );
		synth.add( filterFunction = new FunctionOscillator() );
		synth.add( ampFunction = new FunctionOscillator() );
		synth.add( freqAdder = new Add() );
		synth.add( filterAdder = new Add() );
		synth.add( ampAdder = new Add() );
		synth.add( filter = new FilterLowPass() );
		
		
		// Add DoubleTables
		
		waveTable = new DoubleTable(waveArray);
		pitchTable = new DoubleTable(pitchArray);
		filterTable = new DoubleTable(filterArray);
		ampTable = new DoubleTable(ampArray);
		
		
		// Add DoubleTables to FunctionOscillators
		
		waveFunction.function.set( waveTable );
		pitchFunction.function.set( pitchTable );
		filterFunction.function.set( filterTable );
		ampFunction.function.set( ampTable );
		
		
		// Set FunctionOscillator phases to -1
		
		waveFunction.phase.setValue(-1);
		pitchFunction.phase.setValue(-1);
		filterFunction.phase.setValue(-1);
		ampFunction.phase.setValue(-1);
		
		
		// Connect function oscillators to respective parameters
		
		pitchFunction.output.connect( freqAdder.inputA );		 
		freqAdder.output.connect( waveFunction.frequency );		
		
		filterFunction.output.connect( filterAdder.inputA );
		filterAdder.output.connect( filter.frequency );
		filter.Q.set( 5.0 );
		waveFunction.output.connect(filter.input);
		
		ampFunction.output.connect( ampAdder.inputA );
		ampAdder.output.connect( waveFunction.amplitude );
	
		
		
		
		
		// Add a stereo audio output unit.
		
		synth.add( lineOut = new LineOut() );
		

		// Connect the main waveFunction oscillator to both channels of the output.
		
		filter.output.connect( 0, lineOut.input, 0 );
		filter.output.connect( 0, lineOut.input, 1 );

		
		// Set the frequency and amplitude for the sine wave
		
		waveFunction.frequency.set(waveX);
		waveFunction.amplitude.set(waveY);
		
		pitchFunction.frequency.set(pitchX);
		pitchFunction.amplitude.set(pitchY);
		
		filterFunction.frequency.set(filterX);
		filterFunction.amplitude.set(filterY);
		
		ampFunction.frequency.set(ampX);
		ampFunction.amplitude.set(ampY);
		
		freqAdder.inputB.set(waveX);
		ampAdder.inputB.set(waveY);
		
		filterAdder.inputB.set(800.0);
		
		

		// We only need to start the LineOut. It will pull data from the
		// oscillator.
		lineOut.start();

		
		// Sleep while the sound is generated in the background.
		try
		{
			double time = synth.getCurrentTime();
			// Sleep for a few seconds.
			synth.sleepUntil( time + 5 );
		} catch( InterruptedException e )
		{
			e.printStackTrace();
		}

		// Stop everything.
		synth.stop();
	}
	
	public double[] getTable(Canvas c) {
		return c.getTable();
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
	
	public void setWaveTableXY(double x, double y){
		waveX = x;
		waveY = y;
	}
	
	public void setPitchTableXY(double x, double y){
		pitchX = x;
		pitchY = y;
	}
	
	public void setFilterTableXY(double x, double y){
		filterX = x;
		filterY = y;
	}
	
	public void setAmpTableXY(double x, double y){
		ampX = x;
		ampY = y;
	}

	public static void main( String[] args )
	{
		
		Frame frame = new Frame();
		Synth synth = new Synth();
		
		while(true){
			if(frame.getPlay()){
				// Set parameters before calling play()
				synth.setWaveArray(frame.getWaveformCanvas().getTable());
				synth.setPitchArray(frame.getPitchCanvas().getTable());
				synth.setFilterArray(frame.getFilterCanvas().getTable());
				synth.setAmpArray(frame.getAmpCanvas().getTable());
				
				synth.setWaveTableXY(800.0, .2);
				synth.setPitchTableXY(.2, 200.0);
				synth.setFilterTableXY(.2, 10000.0 );
				synth.setAmpTableXY(.2, .2);
				
				synth.init(); 
				frame.setPlay(false);
			}
		}
	}
}
