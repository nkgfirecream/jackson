package org.codehaus.jackson.main;

import java.math.BigDecimal;

import org.codehaus.jackson.*;

/**
 * Set of basic unit tests for verifying that the basic parser
 * functionality works as expected.
 */
public class TestNumericValues
    extends main.BaseTest
{
    public void testSimpleInt()
        throws Exception
    {
        int EXP_I = 1234;

        JsonParser jp = createParserUsingReader("[ "+EXP_I+" ]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(""+EXP_I, jp.getText());

        assertEquals(EXP_I, jp.getIntValue());
        assertEquals((long) EXP_I, jp.getLongValue());
        assertEquals((double) EXP_I, jp.getDoubleValue());
        assertEquals(BigDecimal.valueOf((long) EXP_I), jp.getDecimalValue());
    }

    public void testSimpleLong()
        throws Exception
    {
        long EXP_L = 12345678907L;

        JsonParser jp = createParserUsingReader("[ "+EXP_L+" ]");
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
        assertEquals(""+EXP_L, jp.getText());

        assertEquals(EXP_L, jp.getLongValue());
        // Should get an exception if trying to convert to int 
        try {
            jp.getIntValue();
        } catch (JsonParseException jpe) {
            verifyException(jpe, "out of range");
        }
        assertEquals((double) EXP_L, jp.getDoubleValue());
        assertEquals(BigDecimal.valueOf((long) EXP_L), jp.getDecimalValue());
    }

    public void testSimpleDouble()
        throws Exception
    {
        final String[] INPUTS = new String[] {
            "1234.00", "2.1101567E-16", "1.0e5", "2.5e+5", "9e4", "-12e-3"
        };
        for (int input = 0; input < 2; ++input) {
            for (int i = 0; i < INPUTS.length; ++i) {

                /* Testing double is more difficult, given the rounding
                 * errors and such. But let's try anyways.
                 */
                String STR = INPUTS[i];
                double EXP_D = Double.parseDouble(STR);
                String DOC = "["+STR+"]";
                
                JsonParser jp;
                
                if (input == 0) {
                    jp = createParserUsingStream(DOC, "UTF-8");
                } else {
                    jp = createParserUsingReader(DOC);
                }
                assertToken(JsonToken.START_ARRAY, jp.nextToken());
                assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
                assertEquals(STR, jp.getText());
                assertEquals(EXP_D, jp.getDoubleValue());
                assertToken(JsonToken.END_ARRAY, jp.nextToken());
                jp.close();
            }
        }
    }

    public void testNumbers()
        throws Exception
    {
        final String DOC = "[ -13, 8100200300, 13.5, 0.00010, -2.033 ]";

        for (int input = 0; input < 2; ++input) {
            JsonParser jp;

            if (input == 0) {
                jp = createParserUsingStream(DOC, "UTF-8");
            } else {
                jp = createParserUsingReader(DOC);
            }

            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(-13, jp.getIntValue());
            assertEquals(-13L, jp.getLongValue());
            assertEquals(-13., jp.getDoubleValue());
            assertEquals("-13", jp.getText());
            
            assertToken(JsonToken.VALUE_NUMBER_INT, jp.nextToken());
            assertEquals(8100200300L, jp.getLongValue());
            // Should get exception for overflow:
            try {
                /*int x =*/ jp.getIntValue();
                fail("Expected an exception for overflow");
            } catch (Exception e) {
                verifyException(e, "out of range");
            }
            assertEquals(8100200300., jp.getDoubleValue());
            assertEquals("8100200300", jp.getText());
            
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
            assertEquals(13, jp.getIntValue());
            assertEquals(13L, jp.getLongValue());
            assertEquals(13.5, jp.getDoubleValue());
            assertEquals("13.5", jp.getText());
            
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
            assertEquals(0, jp.getIntValue());
            assertEquals(0L, jp.getLongValue());
            assertEquals(0.00010, jp.getDoubleValue());
            assertEquals("0.00010", jp.getText());
            
            assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
            assertEquals(-2, jp.getIntValue());
            assertEquals(-2L, jp.getLongValue());
            assertEquals(-2.033, jp.getDoubleValue());
            assertEquals("-2.033", jp.getText());
            
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
        }
    }

    /**
     * Method that tries to test that number parsing works in cases where
     * input is split between buffer boundaries.
     */
    public void testParsingOfLongerSequences()
        throws Exception
    {
        double[] values = new double[] { 0.01, -10.5, 2.1e9, 4.0e-8 };
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.length; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(values[i]);
        }
        String segment = sb.toString();

        int COUNT = 1000;
        sb = new StringBuilder(COUNT * segment.length() + 20);
        sb.append("[");
        for (int i = 0; i < COUNT; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(segment);
            sb.append('\n');
            // let's add somewhat arbitray number of spaces
            int x = (i & 3);
            if (i > 300) {
                x += i % 5;
            }
            while (--x > 0) {
                sb.append(' ');
            }
        }
        sb.append("]");
        String DOC = sb.toString();

        for (int input = 0; input < 2; ++input) {
            JsonParser jp;

            if (input == 0) {
                jp = createParserUsingStream(DOC, "UTF-8");
            } else {
                jp = createParserUsingReader(DOC);
            }

            assertToken(JsonToken.START_ARRAY, jp.nextToken());
            for (int i = 0; i < COUNT; ++i) {
                for (double d : values) {
                    assertToken(JsonToken.VALUE_NUMBER_FLOAT, jp.nextToken());
                    assertEquals(d, jp.getDoubleValue());
                }
            }
            assertToken(JsonToken.END_ARRAY, jp.nextToken());
        }
    }
}