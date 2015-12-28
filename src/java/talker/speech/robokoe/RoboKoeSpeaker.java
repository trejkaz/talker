package talker.speech.robokoe;

import talker.speech.Speaker;
import talker.text.Text;
import talker.text.substitution.KatakanaReadingSubstituter;

import java.io.IOException;

/**
 * Speaker using {@code miku_speak.exe} AKA "初音ミクのロボ声" to do the speech.
 */
public class RoboKoeSpeaker implements Speaker {
    private final String executable;
    private final KatakanaReadingSubstituter readingSubstituter = new KatakanaReadingSubstituter();

    public RoboKoeSpeaker(String executable) {
        this.executable = executable;
    }

    @Override
    public void speak(Text text) {

        // This app can't read non-Japanese characters at all, so we substitute.
        text = readingSubstituter.substitute(text);

        try {
            Process process = new ProcessBuilder(executable, text.getContent())
                    .start();
            process.waitFor();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't launch speech program, perhaps you're not on OSX somehow?", e);
        } catch (InterruptedException e) {
            // No problem.
            Thread.currentThread().interrupt();
        }

    }
}
