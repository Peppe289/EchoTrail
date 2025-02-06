package com.peppe289.echotrail;

import com.peppe289.echotrail.controller.callback.ControllerCallback;
import com.peppe289.echotrail.controller.callback.UserCallback;
import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.dao.user.UserDAO;
import com.peppe289.echotrail.exceptions.UserCollectionException;
import com.peppe289.echotrail.utils.ErrorType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UserUnitTest {

    @Before
    public void setUp() {
        UserDAO userDAO = Mockito.mock(UserDAO.class);
        Mockito.doAnswer(invocation -> {
            String email = invocation.getArgument(0);
            String pwd = invocation.getArgument(1);
            UserCallback<Void, Exception> callback = invocation.getArgument(2);
            // Simulazione del risultato basato su credenziali fisse
            if (email.equals("correct@email.com") && pwd.equals("correctPassword")) {
                callback.onSuccess(null);
            } else {
                callback.onError(new UserCollectionException());
            }
            return null;
        }).when(userDAO).signIn(any(), any(), any());
        UserController.init(userDAO);
    }

    @Test
    public void shouldLoginSuccessfully_whenCredentialsAreCorrect() {
        UserController.login("correct@email.com", "correctPassword", new ControllerCallback<Void, ErrorType>() {
            @Override
            public void onSuccess(Void result) {
                Assert.assertTrue(true);
            }

            @Override
            public void onError(ErrorType error) {
                Assert.fail();
            }
        });
    }

    @Test
    public void shouldFailToLogin_whenPasswordIsIncorrect() {
        UserController.login("correct@email.com", "wrongPassword", new ControllerCallback<Void, ErrorType>() {
            @Override
            public void onSuccess(Void result) {
                Assert.fail();
            }

            @Override
            public void onError(ErrorType error) {
                Assert.assertEquals(ErrorType.UNKNOWN_ERROR, error);
            }
        });
    }
}
