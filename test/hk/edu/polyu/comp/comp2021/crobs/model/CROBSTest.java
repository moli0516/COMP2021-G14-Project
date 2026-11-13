package hk.edu.polyu.comp.comp2021.crobs.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("MissingJavadoc")
class CROBSTest {

    @SuppressWarnings("MissingJavadoc")
    @Test
    void testCROBSConstructor() {
        CROBS crobs = new CROBS();

        assertNotNull(crobs);
    }
}