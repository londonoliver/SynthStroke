import java.io.IOException;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.DoubleTable;
import com.jsyn.devices.javasound.MidiDeviceTools;
import com.jsyn.instruments.SubtractiveSynthVoice;
import com.jsyn.midi.MessageParser;
import com.jsyn.midi.MidiConstants;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.FunctionOscillator;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.PowerOfTwo;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.util.VoiceAllocator;
import com.softsynth.shared.time.TimeStamp;

/**
 * Connect a USB MIDI Keyboard to the internal MIDI Synthesizer using JavaSound.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class Main {
    private static final int MAX_VOICES = 8;
    private Synthesizer synth;
    private VoiceAllocator allocator;
    private LineOut lineOut;
    private double vibratoRate = 5.0;
    private double vibratoDepth = 0.0;

    private UnitOscillator lfo;
    private PowerOfTwo pitchPowerOfTwo;
    private PowerOfTwo filterPowerOfTwo;
    private PowerOfTwo ampPowerOfTwo;
    private MessageParser messageParser;
    private SynthVoice[] voices;
    
    
    volatile Frame frame;
    
    volatile FunctionOscillator pitchFunctionOsc;
    volatile double[] pitchArray;
    volatile DoubleTable pitchTable;
    volatile double pitchAmplitude;
    
    volatile FunctionOscillator filterFunctionOsc;
    volatile double[] filterArray;
    volatile DoubleTable filterTable;
    volatile double filterAmplitude;
    volatile double cutoff;
    volatile double Q;
    
    volatile FunctionOscillator ampFunctionOsc;
    volatile double[] ampArray;
    volatile DoubleTable ampTable;
    volatile double ampAmplitude;

    
    volatile double duration;
    volatile double hold = 1.0;
    volatile Add holdAdd;
    

    
    
    
    

    public static void main(String[] args) {
        Main app = new Main();
        try {
            app.test();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Write a Receiver to get the messages from a Transmitter.
    class CustomReceiver implements Receiver {
        @Override
        public void close() {
            System.out.print("Closed.");
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            byte[] bytes = message.getMessage();
            messageParser.parse(bytes);
        }
    }

    public int test() throws MidiUnavailableException, IOException, InterruptedException {
        setupSynth();

        messageParser = new MyParser();

        int result = 2;
        MidiDevice keyboard = MidiDeviceTools.findKeyboard();
        Receiver receiver = new CustomReceiver();
        // Just use default synthesizer.
        if (keyboard != null) {
            // If you forget to open them you will hear no sound.
            keyboard.open();
            // Put the receiver in the transmitter.
            // This gives fairly low latency playing.
            keyboard.getTransmitter().setReceiver(receiver);
            System.out.println("Play MIDI keyboard: " + keyboard.getDeviceInfo().getDescription());
            result = 0;
        } else {
            System.out.println("Could not find a keyboard.");
        }
        return result;
    }

    class MyParser extends MessageParser {

        @Override
        public void noteOff(int channel, int noteNumber, int velocity) {
            allocator.noteOff(noteNumber, synth.createTimeStamp());
        }

        @Override
        public void noteOn(int channel, int noteNumber, int velocity) {
            double frequency = convertPitchToFrequency(noteNumber);
            double amplitude = velocity / (4 * 128.0);           
            pitchFunctionOsc.phase.setValue(-1);
            filterFunctionOsc.phase.setValue(-1);
            TimeStamp timeStamp = synth.createTimeStamp();
            allocator.noteOn(noteNumber, frequency, amplitude, timeStamp);
        }

        @Override
        public void pitchBend(int channel, int bend) {
            double fraction = (bend - MidiConstants.PITCH_BEND_CENTER)
                    / ((double) MidiConstants.PITCH_BEND_CENTER);
            System.out.println("bend = " + bend + ", fraction = " + fraction);
        }
    }

    /**
     * Calculate frequency in Hertz based on MIDI pitch. Middle C is 60.0. You can use fractional
     * pitches so 60.5 would give you a pitch half way between C and C#.
     */
    double convertPitchToFrequency(double pitch) {
        final double concertA = 440.0;
        return concertA * Math.pow(2.0, ((pitch - 69) * (1.0 / 12.0)));
    }

    private void setupSynth() {
        synth = JSyn.createSynthesizer();

        // Add an output.
        synth.add(lineOut = new LineOut());
        
        duration = 0.9;
        cutoff = 800.0;
        Q = .9;
        
        
        frame = new Frame();
        frame.setMain(this);
        
        // --------- Pitch Stuff --------------------------------
        
        pitchArray = frame.pitchCanvas.getNormalizedTable();
        pitchFunctionOsc = new FunctionOscillator();
        pitchTable = new DoubleTable(pitchArray);
        pitchFunctionOsc.function.set(pitchTable);
        
        synth.add(pitchPowerOfTwo = new PowerOfTwo());
        synth.add(pitchFunctionOsc);
        
        pitchFunctionOsc.output.connect(pitchPowerOfTwo.input);
        pitchFunctionOsc.amplitude.set(0.5);
        pitchFunctionOsc.frequency.set(duration);
        
                
        // --------- Filter Stuff --------------------------------
        
        filterArray = frame.filterCanvas.getNormalizedTable();
        filterFunctionOsc = new FunctionOscillator();
        filterTable = new DoubleTable(filterArray);
        filterFunctionOsc.function.set(filterTable);
        
        synth.add(filterPowerOfTwo = new PowerOfTwo());
        synth.add(filterFunctionOsc);
        
        filterFunctionOsc.output.connect(filterPowerOfTwo.input);
        filterFunctionOsc.amplitude.set(0.5);
        filterFunctionOsc.frequency.set(duration);
        
        // --------- Amp Stuff -------------------------------------
        
        ampArray = frame.ampCanvas.getNormalizedTable();
        ampFunctionOsc = new FunctionOscillator();
        ampTable = new DoubleTable(ampArray);
        ampFunctionOsc.function.set(ampTable);
        
        synth.add(ampPowerOfTwo = new PowerOfTwo());
        synth.add(ampFunctionOsc);
        
        ampFunctionOsc.output.connect(ampPowerOfTwo.input);
        ampFunctionOsc.amplitude.set(0.2);
        ampFunctionOsc.frequency.set(duration);
        
        // ---------- Duration Stuff -------------------------------
        
        synth.add(holdAdd = new Add());
            
        holdAdd.inputB.set(calculateHold(hold));
        
        
        // --------- Setup Voices --------------------------------

        voices = new SynthVoice[MAX_VOICES];
        for (int i = 0; i < MAX_VOICES; i++) {
            SynthVoice voice = new SynthVoice();
            synth.add(voice);
            pitchPowerOfTwo.output.connect(voice.pitchModulation);
            filterPowerOfTwo.output.connect(voice.filterModulation);
            ampPowerOfTwo.output.connect(voice.ampModulation);
            holdAdd.output.connect(voice.duration);
            
            voice.cutoff.set(cutoff);            
            voice.Q.set(Q);
            voice.getOutput().connect(0, lineOut.input, 0);
            voice.getOutput().connect(0, lineOut.input, 1);
            voices[i] = voice;
            
        }
        allocator = new VoiceAllocator(voices);

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();
        // We only need to start the LineOut. It will pull data from the
        // oscillator.
        lineOut.start();

        // Get synthesizer time in seconds.
        double timeNow = synth.getCurrentTime();

        // Advance to a near future time so we have a clean start.
        double time = timeNow + 0.5;

    }
    
    public void setPitchArray(double [] d)
    {
    	pitchArray = d;
    }
    
    public double calculateHold(double d)
    {
    	return d;
    }
}
