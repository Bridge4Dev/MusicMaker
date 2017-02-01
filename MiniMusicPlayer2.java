import javax.sound.midi.*;

public class MiniMusicPlayer2 implements ControllerEventListener {

  public static void main (String[] args) {
    MiniMusicPlayer2 mini = new MiniMusicPlayer2();
    mini.go();
  }

  public void go() {

    try {
      Sequencer sequencer = MidiSystem.getSequencer();
      sequencer.open();

      int[] eventsIWant = {127};
      sequencer.addControllerEventListener(this, eventsIWant);

      Sequence seq = new Sequence(Sequence.PPQ, 4);
      Track track = seq.createTrack();

      for (int i = 35; i < 90; i += 4) {
        track.add(makeEvent(144, 1, i, 100, i));

        track.add(makeEvent(176, 1, 127, 0, i)); // insert our own controller event for us to listen to

        track.add(makeEvent(128, 1, i, 100, i + 2));
      }

      sequencer.setSequence(seq);
      sequencer.setTempoInBPM(220);
      sequencer.start();

      Thread.sleep(10000); // added this and next 2 lines to excape from application
      sequencer.close();
      System.exit(0);
    
    } catch (Exception ex)  {ex.printStackTrace();} 
  }

  public void controlChange(ShortMessage event) {
    System.out.println("la");  // print "la" to terminal for every controller event we hear
  }

  public static MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
    MidiEvent event = null;
    try {
      ShortMessage a = new ShortMessage();
      a.setMessage(comd, chan, one, two);
      event = new MidiEvent(a, tick);

    } catch (Exception e) { }
    return event;
  } 
}