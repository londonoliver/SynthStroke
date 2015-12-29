import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.DoubleTable;
import com.jsyn.unitgen.*;


public class WaveTable
{
	Synthesizer synth;
	SawtoothOscillatorBL osc;
	FunctionOscillator lfo;
	LineOut lineOut;
	Add freqAdder;

	private void test(double[] d)
	{
		// Create a context for the synthesizer.
		synth = JSyn.createSynthesizer();
		
		// Start synthesizer using default stereo output at 44100 Hz.
		synth.start();

		// Add a FunctionOscillator
		DoubleTable myTable = new DoubleTable(d);
		synth.add( osc = new SawtoothOscillatorBL() );
		synth.add( lfo = new FunctionOscillator() );
		synth.add( freqAdder    = new Add() );
		
		lfo.function.set( myTable );
		
		lfo.output.connect( freqAdder.inputA );
		freqAdder.output.connect( osc.frequency );
		
		
		// Add a stereo audio output unit.
		synth.add( lineOut = new LineOut() );

		// Connect the oscillator to both channels of the output.
		osc.output.connect( 0, lineOut.input, 0 );
		osc.output.connect( 0, lineOut.input, 1 );

		// Set the frequency and amplitude for the sine wave
		
		osc.frequency.set(800.0);
		osc.amplitude.set(.4);
		lfo.frequency.set(.3);
		lfo.amplitude.set(100);
		freqAdder.inputB.set(800.0);
		

		// We only need to start the LineOut. It will pull data from the
		// oscillator.
		lineOut.start();

		System.out.println( "You should now be hearing a sine wave. ---------" );
		
		// Sleep while the sound is generated in the background.
		try
		{
			double time = synth.getCurrentTime();
			// Sleep for a few seconds.
			synth.sleepUntil( time + 10.0 );
		} catch( InterruptedException e )
		{
			e.printStackTrace();
		}

		System.out.println( "Stop playing. -------------------" );
		// Stop everything.
		synth.stop();
	}

	public static void main( String[] args )
	{
		Canvas canvas = new Canvas();
		while(true)
		{
			double[] d = canvas.getTable();
			new WaveTable().test(d);
		}
	}
}
