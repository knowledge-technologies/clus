
package si.ijs.kt.clus.distance.primitive.relief;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import org.junit.Test;


public class LevenshteinTest {

    @Test
    public void computeDistTest() {
        Object[][] listOfExamples = new Object[][] { new Object[] { "asd", "1asd", 1 }, new Object[] { "grk", "garac", 3 },
                // When the values are the same, the distance is zero.
                new Object[] { "", "", 0 }, new Object[] { "1", "1", 0 }, new Object[] { "12", "12", 0 }, new Object[] { "123", "123", 0 }, new Object[] { "1234", "1234", 0 }, new Object[] { "12345", "12345", 0 }, new Object[] { "password", "password", 0 },
                // When one of the values is empty, the distance is the length of the
                // other value.
                new Object[] { "", "1", 1 }, new Object[] { "", "12", 2 }, new Object[] { "", "123", 3 }, new Object[] { "", "1234", 4 }, new Object[] { "", "12345", 5 }, new Object[] { "", "password", 8 }, new Object[] { "1", "", 1 }, new Object[] { "12", "", 2 }, new Object[] { "123", "", 3 }, new Object[] { "1234", "", 4 }, new Object[] { "12345", "", 5 }, new Object[] { "password", "", 8 },
                // Whenever a single character is inserted or removed, the distance is
                // one.
                new Object[] { "password", "1password", 1 }, new Object[] { "password", "p1assword", 1 }, new Object[] { "password", "pa1ssword", 1 }, new Object[] { "password", "pas1sword", 1 }, new Object[] { "password", "pass1word", 1 }, new Object[] { "password", "passw1ord", 1 }, new Object[] { "password", "passwo1rd", 1 }, new Object[] { "password", "passwor1d", 1 }, new Object[] { "password", "password1", 1 }, new Object[] { "password", "assword", 1 }, new Object[] { "password", "pssword", 1 }, new Object[] { "password", "pasword", 1 }, new Object[] { "password", "pasword", 1 }, new Object[] { "password", "passord", 1 }, new Object[] { "password", "passwrd", 1 }, new Object[] { "password", "passwod", 1 }, new Object[] { "password", "passwor", 1 },
                // Whenever a single character is replaced, the distance is one.
                new Object[] { "password", "Xassword", 1 }, new Object[] { "password", "pXssword", 1 }, new Object[] { "password", "paXsword", 1 }, new Object[] { "password", "pasXword", 1 }, new Object[] { "password", "passXord", 1 }, new Object[] { "password", "passwXrd", 1 }, new Object[] { "password", "passwoXd", 1 }, new Object[] { "password", "passworX", 1 },
                // If characters are taken off the front and added to the back and all of
                // the characters are unique, then the distance is two times the number of
                // characters shifted, until you get halfway (and then it becomes easier
                // to shift from the other direction).
                new Object[] { "12345678", "23456781", 2 }, new Object[] { "12345678", "34567812", 4 }, new Object[] { "12345678", "45678123", 6 }, new Object[] { "12345678", "56781234", 8 }, new Object[] { "12345678", "67812345", 6 }, new Object[] { "12345678", "78123456", 4 }, new Object[] { "12345678", "81234567", 2 },
                // If all the characters are unique and the values are reversed, then the
                // distance is the number of characters for an even number of characters,
                // and one less for an odd number of characters (since the middle
                // character will stay the same).
                new Object[] { "12", "21", 2 }, new Object[] { "123", "321", 2 }, new Object[] { "1234", "4321", 4 }, new Object[] { "12345", "54321", 4 }, new Object[] { "123456", "654321", 6 }, new Object[] { "1234567", "7654321", 6 }, new Object[] { "12345678", "87654321", 8 },
                // The rest of these are miscellaneous interesting examples. They will
                // be illustrated using the following key:
                // = (the characters are equal)
                // + (the character is inserted)
                // - (the character is removed)
                // # (the character is replaced)

                // Mississippi
                // ippississiM
                // -=##====##=+ --> 6
                new Object[] { "Mississippi", "ippississiM", 6 },
                // eieio
                // oieie
                // #===# --> 2
                new Object[] { "eieio", "oieie", 2 },
                // brad+angelina
                // bra ngelina
                // ===+++======= --> 3
                new Object[] { "brad+angelina", "brangelina", 3 },
                // test international chars
                // ?e?uli?ka
                // e?uli?ka
                // -======== --> 1
                new Object[] { "?e?uli?ka", "e?uli?ka", 1 }, };
        for (int i = 0; i < listOfExamples.length; i++) {
            String a = (String) listOfExamples[i][0], b = (String) listOfExamples[i][1];
            Levenshtein lev = new Levenshtein(a, b);
            double razd = Math.max(a.length(), Math.max(1, b.length()));
            assertEquals("test: " + i + ": " + Arrays.toString(listOfExamples[i]), Double.parseDouble(listOfExamples[i][2].toString()) / razd, lev.getDist(), 0.0001);
        }
    }

}
