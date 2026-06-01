import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RandonneeLoadTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(20))
            .build();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("=== Randonnee Load Test: START ===");
        runScenario("Planning Consultation", 50, RandonneeLoadTest::scenarioPlanningConsultation);
        runScenario("Sortie Browsing", 30, RandonneeLoadTest::scenarioSortieBrowsing);
        System.out.println("=== Randonnee Load Test: FINISHED ===");
    }

    private static void runScenario(String name, int threads, Callable<Integer> scenario) throws InterruptedException, ExecutionException {
        System.out.println("Running scenario: " + name + " with " + threads + " users");
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(scenario));
        }

        int successCount = 0;
        int errorCount = 0;

        for (Future<Integer> future : futures) {
            int status = future.get();
            if (status >= 200 && status < 300) {
                successCount++;
            } else {
                errorCount++;
            }
        }

        executor.shutdown();
        System.out.printf("Scenario '%s' finished: %d successes, %d failures\n", name, successCount, errorCount);
    }

    private static Integer scenarioPlanningConsultation() {
        String url = BASE_URL + "/api/planning/test-user";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        return execute(request);
    }

    private static Integer scenarioSortieBrowsing() {
        String url = BASE_URL + "/api/sorties";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        return execute(request);
    }

    private static Integer execute(Request request) {
        try (Response response = CLIENT.newCall(request).execute()) {
            System.out.printf("[%s] %s -> %d\n", Thread.currentThread().getName(), request.url(), response.code());
            return response.code();
        } catch (IOException e) {
            System.err.printf("[%s] Request failed: %s\n", Thread.currentThread().getName(), e.getMessage());
            return 500;
        }
    }
}
