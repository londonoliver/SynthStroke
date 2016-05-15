import com.jsyn.ports.UnitInputPort;
import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.Add;
import com.jsyn.unitgen.Circuit;
import com.jsyn.unitgen.EnvelopeDAHDSR;
import com.jsyn.unitgen.FilterLowPass;
import com.jsyn.unitgen.Multiply;
import com.jsyn.unitgen.SawtoothOscillatorBL;
import com.jsyn.unitgen.UnitOscillator;
import com.jsyn.unitgen.UnitVoice;
import com.jsyn.util.VoiceDescription;
import com.softsynth.shared.time.TimeStamp;

/**
 * Typical synthesizer voice with an oscillator and resonant filter. Modulate the amplitude and
 * filter using DAHDSR envelopes.
 * 
 * @author Phil Burk (C) 2010 Mobileer Inc
 */
public class SynthVoice extends Circuit implements UnitVoice {
    private static final long serialVersionUID = -2704222221111608377L;
    public UnitOscillator osc;  // made public, used to be private
    public FilterLowPass filter; // made public, was private
    private EnvelopeDAHDSR ampEnv;
    private Add cutoffAdder;
    private Multiply frequencyScaler;
    private Multiply filterScaler;
    private Multiply ampScaler;
    double release = 1.0;
    

    public UnitInputPort amplitude;
    public UnitInputPort frequency;
    /**
     * This scales the frequency value. You can use this to modulate a group of instruments using a
     * shared LFO and they will stay in tune.
     */
    public UnitInputPort pitchModulation;
    public UnitInputPort filterModulation;
    public UnitInputPort ampModulation;
    public UnitInputPort cutoff;
    
    public UnitInputPort cutoffRange;
    public UnitInputPort Q;

    public UnitInputPort duration;
    
    public SynthVoice() {
        add(frequencyScaler = new Multiply());
        add(filterScaler = new Multiply());
        add(ampScaler = new Multiply());
        
        // Add a tone generator.
        add(osc = new SawtoothOscillatorBL());

        // Use an envelope to control the amplitude.
        add(ampEnv = new EnvelopeDAHDSR());

        // Use an envelope to control the filter cutoff.
        add(filter = new FilterLowPass());
        add(cutoffAdder = new Add());

        cutoffAdder.output.connect(filter.frequency);
        frequencyScaler.output.connect(osc.frequency);
        filterScaler.output.connect(filter.frequency);
        ampScaler.output.connect(osc.amplitude);
        osc.output.connect(filter.input);
        filter.output.connect(ampEnv.amplitude);
        


        addPort(frequency = frequencyScaler.inputA, "Frequency");
        addPort(pitchModulation = frequencyScaler.inputB, "PitchMod");
        addPort(cutoff = filterScaler.inputA, "Cutoff");
        addPort(filterModulation = filterScaler.inputB, "FilterModulation");
        addPort(Q = filter.Q);
        addPort(amplitude = ampScaler.inputA, "Amplitude");
        addPort(ampModulation = ampScaler.inputB, "AmpMod");
        addPort(duration = ampEnv.hold, "Duration");
        
        ampEnv.export(this, "Amp");
     
        pitchModulation.setup(0.2, 1.0, 4.0);
        frequency.setup(osc.frequency);
        filterModulation.setup(0.2, 1.0, 4.0);
        cutoff.setup(filter.frequency);
        amplitude.setup(osc.amplitude);
        ampModulation.setup(0.2, 1.0, 4.0);
        

        // Make the circuit turn off when the envelope finishes to reduce CPU load.
        ampEnv.setupAutoDisable(this);

        usePreset(0);
    }

    @Override
    public void noteOff(TimeStamp timeStamp) {
        ampEnv.input.off(timeStamp);
    }

    @Override
    public void noteOn(double freq, double ampl, TimeStamp timeStamp) {
        frequency.set(freq, timeStamp);
        amplitude.set(ampl, timeStamp);

        ampEnv.input.on(timeStamp);
    }

    @Override
    public UnitOutputPort getOutput() {
        return ampEnv.output;
    }

    @Override
    public void usePreset(int presetIndex) {
        int n = presetIndex % presetNames.length;
        switch (n) {
            case 0:
            	ampEnv.sustain.set(0.0);
                ampEnv.attack.set(0.01);
                ampEnv.hold.set(duration.getValue() - 0.03);
                ampEnv.decay.set(0.02);
                cutoff.set(800.0);
                filter.Q.set(1.0);
                break;
            case 1:
                ampEnv.attack.set(0.5);
                ampEnv.decay.set(0.3);
                ampEnv.release.set(0.2);
                cutoff.set(500.0);
                filter.Q.set(3.0);
                break;
            case 2:
            default:
                ampEnv.attack.set(0.1);
                ampEnv.decay.set(0.3);
                ampEnv.release.set(0.5);
                cutoff.set(2000.0);
                filter.Q.set(2.0);
                break;
        }
    }

    static String[] presetNames = {
            "FastSaw", "SlowSaw", "BrightSaw"
    };

    static class MyVoiceDescription extends VoiceDescription {
        String[] tags = {
                "electronic", "filter", "clean"
        };

        public MyVoiceDescription() {
            super("SubtractiveSynth", presetNames);
        }

        @Override
        public UnitVoice createUnitVoice() {
            return new SubtractiveSynthVoice();
        }

        @Override
        public String[] getTags(int presetIndex) {
            return tags;
        }

        @Override
        public String getVoiceClassName() {
            return SubtractiveSynthVoice.class.getName();
        }
    }

    public static VoiceDescription getVoiceDescription() {
        return new MyVoiceDescription();
    }
    
    public double getRelease()
    {
    	return release;
    }

}