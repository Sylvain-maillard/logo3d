package logo3d.language;

import org.assertj.core.api.Assertions;
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
import static org.mockito.ArgumentCaptor.forClass;
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
    Program.TurtleActionCallbacks turtleControl;

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

}