package org.trypticon.talker.speech;

import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Queue to put text on to get it spoken.
 */
public class SpeechQueue {
    private final Speaker speaker;
    private final Queue<String> textQueue = new ConcurrentLinkedQueue<>();
    private volatile Thread thread;

    public SpeechQueue(Properties config) {
        speaker = new SpeakerFactory().create(config);
    }

    public void post(String text) {
        textQueue.add(text);
        synchronized (textQueue) {
            textQueue.notifyAll();
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(new Consumer(), "SpeechQueue");
            thread.start();
        }
    }

    public void stop() {
        Thread thread = this.thread;
        if (thread != null) {
            this.thread = null;
            thread.interrupt();
        }
    }

    private class Consumer implements Runnable {
        @Override
        public void run() {
            while (thread != null) {
                String text;
                while ((text = textQueue.poll()) != null) {
                    speaker.speak(text);
                }

                synchronized (textQueue) {
                    try {
                        textQueue.wait(100);
                    } catch (InterruptedException e) {
                        // Stop gracefully.
                        break;
                    }
                }
            }
        }
    }
}
