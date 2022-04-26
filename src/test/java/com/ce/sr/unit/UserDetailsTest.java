package com.ce.sr.unit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ce.sr.services.UserDetailsImpl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserDetailsTest {

    @Test
    public void UserFieldsTest() {
        UserDetailsImpl userDetailsImpl = new UserDetailsImpl("id", "username", "user@email.com", "password", null);
        UserDetailsImpl userDetailsImpl2 = new UserDetailsImpl("id", "username", "user@email.com", "password", null);

        assertNotNull(userDetailsImpl.getEmail());
        assert (userDetailsImpl.isAccountNonExpired());
        assert (userDetailsImpl.isAccountNonLocked());
        assert (userDetailsImpl.isEnabled());
        assert (userDetailsImpl.isCredentialsNonExpired());
        assert (userDetailsImpl.equals(userDetailsImpl));
        assert (userDetailsImpl.equals(userDetailsImpl2));
        assert (!userDetailsImpl.equals(null));

        assertInstanceOf(Integer.class, userDetailsImpl.hashCode());
    }
}
