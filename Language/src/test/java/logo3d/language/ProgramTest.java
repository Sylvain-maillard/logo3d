package logo3d.language;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by SylvainMaillard on 02/04/2015.
 */
public class ProgramTest {

    private static final Logger LOG = LoggerFactory.getLogger(ProgramTest.class);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    TurtleActionCallbacks turtleControl;

    @InjectMocks
    Program program;

    @Mock
    Program.SyntaxErrorHandler syntaxErrorHandler;

    @Test
    public void test_interprete_multiplication_expression() throws Exception {

        // capture the call to move the turtle:
        ArgumentCaptor<Float> captor = ArgumentCaptor.forClass(Float.class);

        program.interpret("fd 40 * 10");

        // check that forward was called with the expected value.
        verify(turtleControl).forward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(400f);

    }

    @Test
    public void test_interprete_multiplication_expression_again() throws Exception {

        // capture the call to move the turtle:
        ArgumentCaptor<Float> captor = ArgumentCaptor.forClass(Float.class);

        program.interpret("fd 40 * 10 / 10");

        // check that forward was called with the expected value.
        verify(turtleControl).forward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(40f);

    }

    @Test
    public void test_interprete_addition_expression() throws Exception {

        // capture the call to move the turtle:
        ArgumentCaptor<Float> captor = ArgumentCaptor.forClass(Float.class);

        program.interpret("fd 40 + 10 - 20");

        // check that forward was called with the expected value.
        verify(turtleControl).forward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(30f);

    }

    @Test
    public void test_interprete_substraction_expression() throws Exception {

        // capture the call to move the turtle:
        ArgumentCaptor<Float> captor = ArgumentCaptor.forClass(Float.class);

        program.interpret("fd 40-10");

        // check that forward was called with the expected value.
        verify(turtleControl).forward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(30f);

    }

    @Test
    public void test_interprete_divide_expression() throws Exception {

        // capture the call to move the turtle:
        ArgumentCaptor<Float> captor = ArgumentCaptor.forClass(Float.class);

        program.interpret("fd 40/10");

        // check that forward was called with the expected value.
        verify(turtleControl).forward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(4f);

    }

    @Test
    public void test_interprete_sign_expression() throws Exception {

        // capture the call to move the turtle:
        ArgumentCaptor<Float> captor = ArgumentCaptor.forClass(Float.class);

        program.interpret("fd -10");

        // check that forward was called with the expected value.
        verify(turtleControl).backward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(10f);
    }

    @Test
    public void test_print_text() throws Exception {

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        program.interpret("print [hello [little] world]");

        verify(turtleControl).print(captor.capture());
        // expected: -- it will strip the spaces !
        assertThat(captor.getValue()).isEqualTo("hello[little]world");
    }

    @Test
    public void test_print_value() throws Exception {

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        program.interpret("print 2+2");

        // check that forward was called with the expected value.
        verify(turtleControl).print(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo("4.0");
    }

    @Test
    public void test_for_loop() throws Exception {

        ArgumentCaptor<Float> captor = ArgumentCaptor.forClass(Float.class);

        program.interpret("for [i 9 13 1] [fd 1]");

        // check that forward was called with the expected value.
        verify(turtleControl, Mockito.times(4)).forward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(1);
    }

    @Test
    public void test_for_loop_many_blocks() throws Exception {

        ArgumentCaptor<Float> captorFd = ArgumentCaptor.forClass(Float.class);
        ArgumentCaptor<Float> captorRt = ArgumentCaptor.forClass(Float.class);

        program.interpret("for [ i 1 10 1 ][ fd 1 rt 90 ]");

        // check that forward was called with the expected value.
        verify(turtleControl, Mockito.times(10)).forward(captorFd.capture());
        verify(turtleControl, Mockito.times(10)).turnRight(captorRt.capture());
        // expected:
        assertThat(captorFd.getValue()).isEqualTo(1);
        assertThat(captorRt.getValue()).isEqualTo(90);
    }

    @Test
    public void test_for_loop_with_dereference() throws Exception {

        ArgumentCaptor<Float> captorFd = ArgumentCaptor.forClass(Float.class);

        program.interpret("for [ i 1 10 1 ][ fd :i ]");

        // check that forward was called with the expected value.
        verify(turtleControl, Mockito.times(10)).forward(captorFd.capture());
        // expected:
        assertThat(captorFd.getValue()).isEqualTo(10);
    }

    @Test
    public void test_if_structure() throws Exception {

        ArgumentCaptor<Float> captor = ArgumentCaptor.forClass(Float.class);

        program.interpret("if 10 > 20 [fd 1]");

        // check that forward was called with the expected value.
        verify(turtleControl).forward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(1);
    }

    @Test
    public void test_reference() throws Exception {
        program.interpret("make \"valeur 1");

        // expect that the program use a variable named "valeur".
        assertThat(program.memory.get("valeur")).isNotNull();
        assertThat(program.memory.get("valeur").asFloat()).isEqualTo(1f);
    }

    @Test
    public void test_dereference() throws Exception {

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        program.interpret("make \"valeur 1\n print :valeur"); // -> should print "1"

        verify(turtleControl).print(captor.capture());
        assertThat(captor.getValue()).isEqualTo("1.0");
    }

}