package logo3d.desktop;

import de.lessvoid.nifty.controls.Console;
import de.lessvoid.nifty.controls.ConsoleExecuteCommandEvent;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by SylvainMaillard on 02/04/2015.
 */
public class CommandInterpreterTest {

    private static final Logger LOG = getLogger(CommandInterpreterTest.class);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    TurtleControl turtleControl;

    @InjectMocks
    CommandInterpreter commandInterpreter;

    @Mock
    Console console;

    @Test
    public void test_interprete_multiplication_expression() throws Exception {

        // capture the call to move the turtle:
        ArgumentCaptor<Float> captor = forClass(Float.class);

        ConsoleExecuteCommandEvent cEvent = new ConsoleExecuteCommandEvent(console, "fd 40 * 10");
        commandInterpreter.interpret(cEvent);

        // check that forward was called with the expected value.
        verify(turtleControl).forward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(400f);

    }

    @Test
    public void test_interprete_addition_expression() throws Exception {

        // capture the call to move the turtle:
        ArgumentCaptor<Float> captor = forClass(Float.class);

        ConsoleExecuteCommandEvent cEvent = new ConsoleExecuteCommandEvent(console, "fd 40 + 10 - 20");
        commandInterpreter.interpret(cEvent);

        // check that forward was called with the expected value.
        verify(turtleControl).forward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(30f);

    }

    @Test
    public void test_interprete_substraction_expression() throws Exception {

        // capture the call to move the turtle:
        ArgumentCaptor<Float> captor = forClass(Float.class);

        ConsoleExecuteCommandEvent cEvent = new ConsoleExecuteCommandEvent(console, "fd 40-10");
        commandInterpreter.interpret(cEvent);

        // check that forward was called with the expected value.
        verify(turtleControl).forward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(30f);

    }

    @Test
    public void test_interprete_divide_expression() throws Exception {

        // capture the call to move the turtle:
        ArgumentCaptor<Float> captor = forClass(Float.class);

        ConsoleExecuteCommandEvent cEvent = new ConsoleExecuteCommandEvent(console, "fd 40/10");
        commandInterpreter.interpret(cEvent);

        // check that forward was called with the expected value.
        verify(turtleControl).forward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(4f);

    }

    @Test
    public void test_interprete_sign_expression() throws Exception {

        // capture the call to move the turtle:
        ArgumentCaptor<Float> captor = forClass(Float.class);

        ConsoleExecuteCommandEvent cEvent = new ConsoleExecuteCommandEvent(console, "fd -10");
        commandInterpreter.interpret(cEvent);

        // check that forward was called with the expected value.
        verify(turtleControl).forward(captor.capture());
        // expected:
        assertThat(captor.getValue()).isEqualTo(10f);

    }
}