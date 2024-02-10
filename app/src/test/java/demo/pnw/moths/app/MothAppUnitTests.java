package demo.pnw.moths.app;


import org.junit.Test;
import demo.pnw.moths.app.ui.IDkeyFragment;

import static org.junit.Assert.assertEquals;

public class MothAppUnitTests{
    @Test
    public void nameBeautificationTest1() {
        assertEquals("Abagrotis alampeta", IDkeyFragment.beautifyName("abagrotis_alampeta"));
    }

    @Test
    public void nameBeautificationTest2() {
        assertEquals("Hecatera dysodea", IDkeyFragment.beautifyName("hecatera_dysodea"));
    }

}