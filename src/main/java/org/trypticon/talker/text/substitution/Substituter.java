package org.trypticon.talker.text.substitution;

import org.trypticon.talker.text.Text;

/**
 * Interface for something which can substitute text.
 */
public interface Substituter {
    Text substitute(Text text);
}
