package geogebra.sound;

import geogebra.common.kernel.geos.GeoFunction;
import geogebra.main.AppD;

/**
 * Class to handle GeoGebra sound features. Calls to midi and streaming audio
 * methods are managed from here.
 * 
 * @author G. Sturr
 * 
 */
public class SoundManager implements geogebra.common.sound.SoundManager {

	private AppD app;
	private MidiSound midiSound;
	private FunctionSound functionSound;

	private static final int SOUNDTYPE_NONE = -1;
	private static final int SOUNDTYPE_MIDI = 0;
	private static final int SOUNDTYPE_FUNCTION = 1;
	private int currentSoundType = SOUNDTYPE_NONE;

	private boolean isRunning = false;
	private boolean isPaused = false;

	/**
	 * Constructor
	 * 
	 * @param app
	 */
	public SoundManager(AppD app) {
		this.app = app;
	}

	// ====================================
	// Getters/setters
	// ====================================

	public boolean isRunning() {
		return isRunning;
	}

	public boolean isPaused() {
		return isPaused;
	}

	/**
	 * Retrieves field midiSound. Creates a new instance of MidiSound if none
	 * exists.
	 */
	private MidiSound getMidiSound() {
		if (midiSound == null)
			try {
				midiSound = new MidiSound(app);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return midiSound;
	}

	/**
	 * Retrieves field functionSound. Creates a new instance of FunctionSound if
	 * none exists.
	 */
	private FunctionSound getFunctionSound() {
		if (functionSound == null)
			try {
				functionSound = new FunctionSound(app);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return functionSound;
	}

	// ====================================
	// Sound playing methods
	// ====================================

	/**
	 * Plays a single note using the midi sequencer.
	 * 
	 * @param note
	 * @param duration
	 * @param instrument
	 * @param velocity
	 */
	public void playSequenceNote(final int note, final double duration,
			final int instrument, final int velocity) {
		try {
			stopCurrentSound();
			currentSoundType = SOUNDTYPE_MIDI;
			getMidiSound().playSequenceNote(note, duration, instrument,
					velocity);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Plays an audio file with the .mid extension using the midi sequencer.
	 * 
	 * @param fileName
	 */
	public void playFile(String fileName) {
		try {
			stopCurrentSound();
			currentSoundType = SOUNDTYPE_MIDI;
			getMidiSound().playMidiFile(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Plays a sequence of notes generated by the string noteSring using the
	 * midi sequencer.
	 * 
	 * @param noteString
	 * @param instrument
	 */
	public void playSequenceFromString(String noteString, int instrument) {
		try {
			stopCurrentSound();
			currentSoundType = SOUNDTYPE_MIDI;
			getMidiSound().playSequenceFromJFugueString(noteString, instrument);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Plays a tone generated by the time-valued input function f(t) for t = min
	 * to t = max seconds. Also allows adjustment of the sampling rate and bit
	 * depth.
	 * 
	 * @param f
	 * @param min
	 * @param max
	 * @param sampleRate
	 * @param bitDepth
	 */
	public void playFunction(final GeoFunction f, final double min,
			final double max, final int sampleRate, final int bitDepth) {
		try {
			stopCurrentSound();
			currentSoundType = SOUNDTYPE_FUNCTION;
			getFunctionSound().playFunction(f, min, max, sampleRate, bitDepth);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Plays a tone generated by the time-valued input function f(t) for t = min
	 * to t = max seconds.
	 * 
	 * @param f
	 * @param min
	 * @param max
	 */
	public void playFunction(final GeoFunction f, final double min,
			final double max) {
		try {
			stopCurrentSound();
			currentSoundType = SOUNDTYPE_FUNCTION;
			getFunctionSound().playFunction(f, min, max);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ====================================
	// Control methods
	// ====================================

	/**
	 * Stops all sound creation and closes all sound-related resources.
	 */
	public void stopCurrentSound() {
		if (midiSound != null)
			midiSound.stop();
		if (functionSound != null)
			functionSound.pause(true);
	}

	/**
	 * Pauses/resumes current sound.
	 * 
	 * @param doResume
	 *            : true = resume play, false = pause
	 */
	public void pauseResumeSound(boolean doResume) {

		if (currentSoundType == SOUNDTYPE_MIDI && midiSound != null) {
			midiSound.pause(!doResume);
		}

		if (currentSoundType == SOUNDTYPE_FUNCTION && functionSound != null)
			functionSound.pause(!doResume);

		isPaused = !doResume;
	}

}
