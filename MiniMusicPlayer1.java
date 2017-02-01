import javax.sound.midi.*;

public class MiniMusicPlayer1 {

  public static void main (String[] args) {

    try {
      Sequencer sequencer = MidiSystem.getSequencer();
      sequencer.open();

      Sequence seq = new Sequence(Sequence.PPQ, 4);
      Track track = seq.createTrack();

      for (int i = 45; i < 101; i += 4) {
        track.add(makeEvent(144, 1, i, 100, i));
        track.add(makeEvent(128, 1, i, 100, i + 2));
      }

      sequencer.setSequence(seq);
      sequencer.setTempoInBPM(220);
      sequencer.start();
      Thread.sleep(10000); // added this and next 2 lines to excape from application
      sequencer.close();
      System.exit(0);

    } catch (Exception ex) {ex.printStackTrace();}

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