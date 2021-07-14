package de.intension.keycloak;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.seljup.SeleniumJupiter;

@ExtendWith(SeleniumJupiter.class)
class LoginFlowTest extends KeycloakExtensionTestBase
{

    private static final String ACCOUNT_URL = "http://localhost:" + keycloak.getHttpPort() + "/auth/realms/" + REALM + "/account/#/";
    FirefoxDriver driver;

    public LoginFlowTest(FirefoxDriver driver)
    {
        super();
        this.driver = driver;
    }

    /**
     * GIVEN: user with email & password
     * WHEN: credentials are entered
     * THEN: user gets logged in
     * WHEN: rememberme-decision is "yes"
     * THEN: cookie is created
     * WHEN: sign out button is clicked
     * THEN: user gets signed out
     */
    @Test
    void login_logout_flow_test()
        throws Exception
    {
        WebDriverWait wait = new WebDriverWait(driver, 5);

        String email = "test@test.com";
        String password = "keycloak";
        driver.get(ACCOUNT_URL);
        click(wait, "landingSignInButton");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("next")));
        driver.findElementById("identificatorInput").sendKeys(email);
        driver.findElementById("next").click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login")));
        driver.findElementById("passwordInput").sendKeys(password);
        driver.findElementById("login").click();
        click(wait, "rememberMeYes");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("landingSignOutButton")));
        assertEquals(ACCOUNT_URL, driver.getCurrentUrl());
        assertEquals("username:user", driver.manage().getCookieNamed("KEYCLOAK_REMEMBER_ME").getValue());
        click(wait, "landingSignOutButton");
        assertEquals(ACCOUNT_URL, driver.getCurrentUrl());
    }

    /**
     * GIVEN: user with email & password
     * WHEN: login is requested
     * THEN: user input field gets autofilled by passwordmanager
     * WHEN: rememberme-decision is made
     * THEN: user gets signed in
     */
    @Test
    void login_flow_test_pw_manager_supported()
        throws Exception
    {
        driver.quit();

        FirefoxOptions ffOptions = new FirefoxOptions();
        FirefoxProfile myProfile = new FirefoxProfile();

        myProfile.setPreference("signon.autofillForms", true);
        ffOptions.setCapability("marionette", true);
        ffOptions.setProfile(myProfile);
        ffOptions.setCapability("browser", "Firefox");
        ffOptions.setCapability("os", "Windows");

        driver = new FirefoxDriver(ffOptions);
        WebDriverWait wait = new WebDriverWait(driver, 5);

        String email = "test@test.com";
        String password = "keycloak";

        driver.get("about:config");
        Actions act = new Actions(driver);
        act.sendKeys(Keys.ENTER).perform();
        act.sendKeys("signon.rememberSignons").perform();
        TimeUnit.MILLISECONDS.sleep(200);
        act.sendKeys(Keys.TAB, Keys.TAB, Keys.TAB, Keys.ENTER).perform();

        driver.get("about:logins");
        act.sendKeys(Keys.TAB, Keys.TAB, Keys.TAB, Keys.ENTER).perform();
        act.sendKeys("http://localhost:" + keycloak.getHttpPort()).sendKeys(Keys.TAB).perform();
        act.sendKeys(email).sendKeys(Keys.TAB).perform();
        act.sendKeys(password).sendKeys(Keys.ENTER).perform();

        driver.get(ACCOUNT_URL);
        click(wait, "landingSignInButton");
        click(wait, "next");
        click(wait, "login");
        click(wait, "rememberMeYes");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("landingSignOutButton")));
        assertEquals(ACCOUNT_URL, driver.getCurrentUrl());
    }

    /**
     * GIVEN: user username: "username"
     * WHEN: login is requested
     * THEN: user is redirected to password input, skipping e-mail input
     */
    @Test
    void autofill_with_rememberMe_token()
        throws Exception
    {
        WebDriverWait wait = new WebDriverWait(driver, 5);

        Cookie cookie = new Cookie("KEYCLOAK_REMEMBER_ME", "username:username", null, null);

        driver.get(ACCOUNT_URL);
        driver.manage().addCookie(cookie);
        click(wait, "landingSignInButton");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login")));
        assertThat(driver.findElementById("login"), notNullValue());
    }

    private void click(WebDriverWait wait, String id)
    {
        wait.until(ExpectedConditions.elementToBeClickable(By.id(id)));
        driver.findElementById(id).click();
    }

    @AfterEach
    public void quitDriver()
    {
        driver.quit();
    }
}