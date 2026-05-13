import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaximizeNumberTest {

    @Test
    void maximize() {
        assertNull(MaximizeNumber.maximize(null, null));
        assertNull(MaximizeNumber.maximize(null, "321"));
        assertEquals("321", MaximizeNumber.maximize("321", null));
        assertEquals("333", MaximizeNumber.maximize("123", "3213"));
        assertEquals("433", MaximizeNumber.maximize("123", "32134"));
        assertEquals("43335", MaximizeNumber.maximize("11235", "32134"));
        assertEquals("433325", MaximizeNumber.maximize("112315", "32134"));
        assertEquals("433321", MaximizeNumber.maximize("112311", "32134"));
        assertEquals("433322", MaximizeNumber.maximize("112311", "321342"));
        assertEquals("433322", MaximizeNumber.maximize("012311", "321342"));
        assertEquals("433322", MaximizeNumber.maximize("012310", "321342"));
        assertEquals("cba", MaximizeNumber.maximize("cba", "abc"));
        assertEquals("c1a", MaximizeNumber.maximize("c1a", "abc"));
        assertEquals("c1a", MaximizeNumber.maximize("c1a", "0bc"));
        assertEquals("c2a", MaximizeNumber.maximize("c1a", "0b2"));
        assertEquals("c3a", MaximizeNumber.maximize("c1a", "0b23"));
        assertEquals("32a", MaximizeNumber.maximize("01a", "0b23"));
        assertEquals("43a", MaximizeNumber.maximize("01a", "0b234"));
        assertEquals("44a", MaximizeNumber.maximize("41a", "0b234"));
        assertEquals("54a", MaximizeNumber.maximize("51a", "0b234"));
    }
}