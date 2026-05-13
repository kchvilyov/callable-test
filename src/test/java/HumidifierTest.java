import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HumidifierTest {

    @Test
    void solve1() {
        var res = Humidifier.solve(
                """
                        4
                        1 3
                        3 1
                        4 4
                        7 1""");
        assertEquals(3, Integer.parseInt(res));
    }

    @Test
    void solve2() {
        var res = Humidifier.solve("""
3
1 8
10 11
21 5""");
        assertEquals(5, Integer.parseInt(res));
    }

    @Test
    void solve3() {
        var res = Humidifier.solve("""
10
2 1
22 10
26 17
29 2
45 20
47 32
72 12
75 1
81 31
97 7""");
        assertEquals(57, Integer.parseInt(res));
    }
}