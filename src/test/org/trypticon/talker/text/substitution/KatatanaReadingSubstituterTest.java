package org.trypticon.talker.text.substitution;

import org.junit.Test;
import org.trypticon.talker.text.Text;
import org.trypticon.talker.text.Token;
import org.trypticon.talker.text.TokenType;

import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link KatakanaReadingSubstituter}.
 */
public class KatatanaReadingSubstituterTest {

    @Test
    public void testAlreadyJapanese() {
        String result = new KatakanaReadingSubstituter().substitute(new Text(Collections.singletonList(
                new Token("テスト", TokenType.JAPANESE)))).getContent();

        assertThat(result, is(equalTo("テスト")));
    }

    @Test
    public void testEnglish() {
        assertThat(substitute("test"), is(equalTo("テスト")));
    }

    @Test
    public void testEnglish_ShortVowel() {
        assertThat(substitute("kit"), is(equalTo("キト")));
    }

    @Test
    public void testEnglish_Hyphen() {
        assertThat(substitute("gill-bearing"), is(equalTo("ギルベリン")));
    }

    @Test
    public void testEnglish_Schwas() {
        assertThat(substitute("aquaria"), is(equalTo("アクエリイア")));
    }

    @Test
    public void testEnglish_AdjoiningConsonants() {
        assertThat(substitute("standby"), is(equalTo("スタンドバイ")));
    }

    @Test
    public void testEnglish_Si() {
        assertThat(substitute("consistently"), is(equalTo("カンシスタントリイ")));
    }

    private String substitute(String input) {
        return new KatakanaReadingSubstituter().substitute(new Text(Collections.singletonList(
                new Token(input, TokenType.OTHER)))).getContent();

    }
}