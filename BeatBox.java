import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;

public class BeatBox implements Serializable{

  // set up instance variables

  JPanel mainPanel;
  ArrayList<JCheckBox> checkboxList; // checkboxes go in an ArrayList
  Sequencer sequencer;
  Sequence sequence;
  Track track;
  JFrame theFrame;

  String[] instrumentNames = { "Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga" };
  int[] instruments = { 35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63 };

  
  public static void main (String[] args) {
    new BeatBox().buildGUI();
  }


  public void buildGUI() {
    theFrame = new JFrame("Cyber Beatbox");  // use the instance variable
    theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    BorderLayout layout = new BorderLayout();
    JPanel background = new JPanel(layout);
    background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // padding between background and panel

    checkboxList = new ArrayList<JCheckBox>();
    Box buttonBox = new Box(BoxLayout.Y_AXIS); // container for buttons, stacked vertically

    JButton start = new JButton("Start");
    start.addActionListener(new MyStartListener());
    buttonBox.add(start);  // add start button to the button container

    JButton stop = new JButton("Stop");
    stop.addActionListener(new MyStopListener());
    buttonBox.add(stop);

    JButton upTempo = new JButton("Tempo Up");
    upTempo.addActionListener(new MyUpTempoListener());
    buttonBox.add(upTempo);

    JButton downTempo = new JButton("Tempo Down");
    downTempo.addActionListener(new MyDownTempoListener());
    buttonBox.add(downTempo);

    JButton serializeIt = new JButton("serialize it");
    serializeIt.addActionListener(new MySendListener());
    buttonBox.add(serializeIt);

    JButton restore = new JButton("restore");
    restore.addActionListener(new MyReadInListener());
    buttonBox.add(restore);

    Box nameBox = new Box(BoxLayout.Y_AXIS); // container for instrument labels, stacked vertically
    for ( int i = 0; i < 16; i++ ) {
      nameBox.add(new Label(instrumentNames[i])); // add each label to the container
    }

    GridLayout grid = new GridLayout(16, 16);
    grid.setVgap(1);
    grid.setHgap(2);
    mainPanel = new JPanel(grid);

    background.add(BorderLayout.EAST, buttonBox);  // add smaller containers to the main JPanel
    background.add(BorderLayout.WEST, nameBox);
    background.add(BorderLayout.CENTER, mainPanel);

    theFrame.getContentPane().add(background);  // add Jpanel to the JFrame

    for ( int i = 0; i < 256; i ++ ) {
      JCheckBox c = new JCheckBox(); // add checkboxes to the main (center) panel
      c.setSelected(false);
      checkboxList.add(c);
      mainPanel.add(c);
    }

    setUpMidi();

    theFrame.setBounds(50, 50, 300, 300);
    theFrame.pack();
    theFrame.setVisible(true);

  }


  public void setUpMidi() {
    try {
      sequencer = MidiSystem.getSequencer();
      sequencer.open();
      sequence = new Sequence(Sequence.PPQ, 4);
      track = sequence.createTrack();
      sequencer.setTempoInBPM(120);
    } catch (Exception e) {e.printStackTrace();}
  }


  public void buildTrackAndStart() {
    int[] trackList = null;

    sequence.deleteTrack(track);      // deletes old data before starting new track
    track = sequence.createTrack();

    for ( int i = 0; i < 16; i++ ) {  // for each instrument in the beat box
      trackList = new int[16];

      int key = instruments[i];

      for ( int j = 0; j < 16; j++ ) {               // for each beat in the beat box
        JCheckBox jc = checkboxList.get(j + 16*i);   // 16*i to access boxes on each instrument

        if ( jc.isSelected()) {
          trackList[j] = key; // if checkbox is selected, play the 'key' at this beat for this instrument
        } else {
          trackList[j] = 0;   // else play nothing at this beat for this instrument
        }
      }

      makeTracks(trackList);                    // add all checked events to the track for this instrument
      track.add(makeEvent(176, 1, 127, 0, 16));
    }

    track.add(makeEvent(192, 9, 1, 0, 15));   // need an event at beat 16 so it goes the full 16 beats
    try {
      sequencer.setSequence(sequence);
      sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);  // continue looping until stopped
      sequencer.start();
      sequencer.setTempoInBPM(120);
    } catch (Exception e) {e.printStackTrace();}

  }


  public class MyStartListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      buildTrackAndStart();
    }
  }


  public class MyStopListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      sequencer.stop();
    }
  }


  public class MyUpTempoListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      float tempoFactor = sequencer.getTempoFactor();             // get current tempo factor
      sequencer.setTempoFactor( (float) (tempoFactor * 1.03 ) );  // adjust tempo factor to speed it up
    }
  }


  public class MyDownTempoListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      float tempoFactor = sequencer.getTempoFactor();
      sequencer.setTempoFactor( (float) (tempoFactor * .97 ) );
    }
  }


  public class MySendListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      boolean[] checkboxState = new boolean[256];

      for (int i = 0; i < 256; i++) {
        JCheckBox check = (JCheckBox) checkboxList.get(i);
        if (check.isSelected()) {
          checkboxState[i] = true;
        }
      }

      try {
        FileOutputStream fileStream = new FileOutputStream(new File("Checkbox.ser"));
        ObjectOutputStream os = new ObjectOutputStream(fileStream);
        os.writeObject(checkboxState);

      } catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  }


  public class MyReadInListener implements ActionListener {
    public void actionPerformed(ActionEvent a) {
      
      boolean[] checkboxState = null;
      
      try {
        FileInputStream fileIn = new FileInputStream(new File("Checkbox.ser"));
        ObjectInputStream is = new ObjectInputStream(fileIn);
        checkboxState = (boolean[]) is.readObject();

      } catch(Exception ex) {
        ex.printStackTrace();
      }

      for (int i = 0; i < 256; i++) {
        JCheckBox check = (JCheckBox) checkboxList.get(i);
        if (checkboxState[i]) {
          check.setSelected(true);
        } else {
          check.setSelected(false);
        }
      }

      sequencer.stop();
      buildTrackAndStart();
    }
  }


  public void makeTracks(int[] list) {
    for (int i = 0; i < 16; i++) {
      int key = list[i];

      if (key != 0) {
        track.add(makeEvent(144, 9, key, 100, i));    // start and stop notes if key isn't set to 0
        track.add(makeEvent(128, 9, key, 100, i+1));
      }
    }
  }


  public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
    MidiEvent event = null;
    try {
      ShortMessage a = new ShortMessage();
      a.setMessage(comd, chan, one, two);
      event = new MidiEvent(a, tick);

    } catch (Exception e) {e.printStackTrace();}

    return event;
  }



}