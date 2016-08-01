package org.appverse.builder.auth.ldap;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import org.appverse.builder.Application;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.appverse.builder.security.SecurityUtils;
import org.appverse.builder.service.UserService;
import org.appverse.builder.service.util.RandomUtil;
import org.appverse.builder.web.rest.AuthTokenResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;
import java.net.URI;

import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 * Created by panthro on 01/08/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebAppConfiguration
@IntegrationTest
public class LdapAuthenticationTest {


    @Inject
    private ApplicationContext context;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    private InMemoryDirectoryServer inMemoryDirectoryServer;

    @Inject
    private UserService userService;

    @Inject
    private AuthTokenResource authTokenResource;


    private static final String BOB_USERNAME = "bob";
    private static final String BOB_PASSWORD = "bobspassword";
    private static final String BOB_DN = "uid=bob,ou=people,dc=appverse,dc=org";


    private static final String NOEMAIL_USERNAME = "noemail";
    private static final String NOEMAIL_DN = "dn: uid=noemail,ou=people,dc=appverse,dc=org";
    private static final String NOEMAIL_PASSWORD = "noemailpassword";


    @Before
    public void startupMockLdapServer() throws Exception {

        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=appverse,dc=org");

        config.setListenerConfigs(new InMemoryListenerConfig("myListener", null, URI.create(appverseBuilderProperties.getAuth().getLdap().getUrl()).getPort(), null, null, null));

        inMemoryDirectoryServer = new InMemoryDirectoryServer(config);

        // import your test data from ldif files
        inMemoryDirectoryServer.importFromLDIF(true, getClass().getClassLoader().getResource("test-server.ldif").getFile());

        inMemoryDirectoryServer.startListening();

        assertThat(inMemoryDirectoryServer.getEntry(BOB_DN)).isNotNull();


    }

    @After
    public void shutdownMockLdapServer() {
        if (inMemoryDirectoryServer != null) {
            inMemoryDirectoryServer.shutDown(true);
        }
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testLdapLogin() {
        testAuthentication(BOB_USERNAME, BOB_PASSWORD);
    }

    /**
     * This user does not have email setup in the LDAP, a default email should be assigned to him.
     */
    @Test
    public void testEmailIsNotRequired() {
        testAuthentication(NOEMAIL_USERNAME, NOEMAIL_PASSWORD);
    }


    @Test(expected = BadCredentialsException.class)
    public void testBadCredentials() {
        testAuthentication(BOB_USERNAME, RandomUtil.generatePassword());
    }

    @Test(expected = BadCredentialsException.class)
    public void testUsernameNotFound() {
        testAuthentication(RandomUtil.generatePassword(), RandomUtil.generatePassword());
    }


    private void testAuthentication(String username, String password) {
        AuthenticationManager authenticationManager = this.context.getBean(AuthenticationManager.class);
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(SecurityUtils.getCurrentUser().getUsername()).isEqualToIgnoringCase(username);

        assertThat(userService.getUserWithAuthoritiesByLogin(username)).isNotNull();
    }

}
