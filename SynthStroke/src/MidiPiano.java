import java.awt.*;
import java.awt.event.*;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.swing.*;

import com.softsynth.shared.time.TimeStamp;

public class MidiPiano extends JPanel implements MouseListener {

    final int OCTAVES = 6; // change as desired
    Main main;

    private WhiteKey[] whites = new WhiteKey [7 * OCTAVES + 1];
    private BlackKey[] blacks = new BlackKey [5 * OCTAVES];

    MidiChannel channel;

    public MidiPiano () {
    	createAndShowGUI();
    }

    public void mousePressed (MouseEvent e) {
        Key key = (Key) e.getSource ();
        int noteNumber = key.getNote();
        double frequency = main.convertPitchToFrequency(noteNumber);
        TimeStamp timeStamp = main.synth.createTimeStamp();
        main.pitchFunctionOsc.phase.setValue(-1);
        main.filterFunctionOsc.phase.setValue(-1);
        main.ampFunctionOsc.phase.setValue(-1);
        main.allocator.noteOn(noteNumber, frequency, 0.2, timeStamp);
    }

    public void mouseReleased (MouseEvent e) {
        Key key = (Key) e.getSource ();
        int noteNumber = key.getNote();
        main.allocator.noteOff(noteNumber, main.synth.createTimeStamp());
    }

    public void mouseClicked (MouseEvent e) { }
    public void mouseEntered (MouseEvent e) { }
    public void mouseExited (MouseEvent e) { }

    void createAndShowGUI () {

        JPanel contentPane = new JPanel(null)
        {
            @Override
            public Dimension getPreferredSize()
            {
                int count = getComponentCount();
                Component last = getComponent(count - 1);
                Rectangle bounds = last.getBounds();
                int width = 10 + bounds.x + bounds.width;
                int height = 10 + bounds.y + bounds.height;

                return new Dimension(width, height);
            }

            @Override
            public boolean isOptimizedDrawingEnabled()
            {
                return false;
            }
        };


        for (int i = 0; i < blacks.length; i++) {
            blacks [i] = new BlackKey (i);
            contentPane.add (blacks [i]);
            blacks [i].addMouseListener (this);
        }
        for (int i = 0; i < whites.length; i++) {
            whites [i] = new WhiteKey (i);
            contentPane.add (whites [i]);
            whites [i].addMouseListener (this);
        }
        add(contentPane);
    }

    public static void main (String[] args) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                new MidiPiano ();
            }
        });
    }
    
    public void setMain(Main m)
    {
    	main = m;
    }
}

interface Key {
    // change WD to suit your screen
    int WD = 13;
    int HT = (WD * 9) / 2;
    // change baseNote for starting octave
    // multiples of 16 only
    int baseNote = 48;

    int getNote ();
}


class BlackKey extends JButton implements Key {

    final int note;

    public BlackKey (int pos) {
        note = baseNote + 1 + 2 * pos + (pos + 3) / 5 + pos / 5;
        int left = 10 + WD
                + ((WD * 3) / 2) * (pos + (pos / 5)
                + ((pos + 3) / 5));
        setBackground (Color.BLACK);
        setBounds (left, 10, WD, HT);
    }

    public int getNote () {
        return note;
    }
}


class WhiteKey  extends JButton implements Key {

    static int WWD = (WD * 3) / 2;
    static int WHT = (HT * 3) / 2;
    final int note;

    public WhiteKey (int pos) {

        note = baseNote + 2 * pos
                - (pos + 4) / 7
                - pos / 7;
        int left = 10 + WWD * pos;
        // I think metal looks better!
        setBackground (Color.WHITE);
        setBounds (left, 10, WWD, WHT);

    }

    public int getNote () {
        return note;
    }
}
