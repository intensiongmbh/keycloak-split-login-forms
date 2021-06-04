package de.intension.keycloak;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.intension.keycloak.test.KeycloakDevContainer;

@Testcontainers
public class KeycloakExtensionTestBase
{

    protected static final String         REALM  = "TestRealm";
    protected static KeycloakDevContainer keycloak;

    protected static Keycloak             keycloakClient;

    protected ObjectMapper                mapper = new ObjectMapper();

    public Wiser                          smtpServer;

    @BeforeAll
    public static void beforeClass()
    {
        keycloak = new KeycloakDevContainer("keycloak-split-login-forms");
        keycloak.withReuse(true);
        keycloak.withExposedPorts(8080, 8787);
        keycloak.withFixedExposedPort(8787, 8787);
        keycloak.withRealmImportFile("realm.json");
        keycloak.withClassFolderChangeTrackingEnabled(true);
        keycloak.start();

        keycloakClient = Keycloak.getInstance(keycloak.getAuthServerUrl(), "master", keycloak.getAdminUsername(),
                                              keycloak.getAdminPassword(), "admin-cli");

        keycloakClient.realm(REALM);
    }

    @BeforeEach
    public void startSMTPServer()
    {
//		logger.debug("Setting up and starting SMTP Server before test run");

        int retryCount = 5;
        while (retryCount >= 0) {
            try {
                startServer();
                break;
            } catch (RuntimeException re) {
                if ((re.getCause() != null) && (re.getCause() instanceof java.net.BindException)) {
//                    logger.debug("Setup failed for SMTP Server before test run: " + re.getCause().getMessage());
                    retryCount--;
                }
                else {
                    throw re;
                }
            }
        }

    }

    protected String getAdminToken()
        throws Exception
    {
        HttpClient client = HttpClient.newHttpClient();
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "password");
        params.put("client_id", "admin-cli");
        params.put("username", keycloak.getAdminUsername());
        params.put("password", keycloak.getAdminPassword());
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + keycloak.getHttpPort() + "/auth/realms/" + REALM
                    + "/protocol/openid-connect/token"))
            .header("Content-Type", "application/x-www-form-urlencoded").POST(buildFormDataFromMap(params)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return retrieveToken(response.body());
    }

    protected HttpResponse<String> post(String url, String body)
        throws Exception
    {
        HttpClient javaClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
            .header("Authorization", "Bearer " + getAdminToken())
            .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
        return javaClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void startServer()
    {
        smtpServer = new Wiser();
        smtpServer.setHostname("localhost");
        smtpServer.setPort(25);
        smtpServer.start();
        smtpServer.getMessages().clear();
    }

    private String retrieveToken(String responseBody)
        throws Exception
    {
        return mapper.readValue(responseBody, AccessTokenResponse.class).getToken();
    }

    private HttpRequest.BodyPublisher buildFormDataFromMap(Map<String, String> params)
    {
        var builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    @AfterEach
    public void stopSMTPServer()
    {
        smtpServer.stop();
        smtpServer = null;
    }

    public List<WiserMessage> getMessages()
    {
        return smtpServer.getMessages();
    }

}