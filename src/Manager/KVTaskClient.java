package Manager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private final String apiToken;

    public KVTaskClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.apiToken = generateApiToken(serverUrl);
    }

    private String generateApiToken(String serverUrl) {
        String token = "";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/register"))
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            token = response.body();
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время запроса создания токена возникла ошибка. " +
                    "Проверьте доступность сервера: " + serverUrl + "/register");
        }
        return token;
    }

    public void put(String key, String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/save/" + key + "?API_TOKEN=" + apiToken))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время запроса записи возникла ошибка. " +
                    "Проверьте доступность сервера: " + serverUrl + "/save");
        }
    }

    public String load(String key) {
        String loadString = "";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/load/" + key + "?API_TOKEN=" + apiToken))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            loadString = response.body();
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время запроса чтения возникла ошибка. " +
                    "Проверьте доступность сервера: " + serverUrl + "/load");
        }
        return loadString;
    }
}
