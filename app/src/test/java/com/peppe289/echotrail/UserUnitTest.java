package com.peppe289.echotrail;

import com.peppe289.echotrail.controller.user.UserController;
import com.peppe289.echotrail.dao.user.UserDAO;
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
            UserDAO.SignInCallback callback = invocation.getArgument(2);
            // Simulazione del risultato basato su credenziali fisse
            callback.onComplete(email.equals("correct@email.com") && pwd.equals("correctPassword"));
            return null;
        }).when(userDAO).signIn(any(), any(), any());
        UserController.init(userDAO);
    }

    @Test
    public void shouldLoginSuccessfully_whenCredentialsAreCorrect() {
        UserController.login("correct@email.com", "correctPassword", Assert::assertTrue);
    }

    @Test
    public void shouldFailToLogin_whenPasswordIsIncorrect() {
        UserController.login("correct@email.com", "wrongPassword", Assert::assertFalse);
    }
}
