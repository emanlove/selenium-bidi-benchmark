import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.module.Script;
import org.openqa.selenium.bidi.script.ContextTarget;
import org.openqa.selenium.bidi.script.EvaluateParameters;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class JsEvaluationBenchmark {

    private static final int ITERATIONS = 5000;

    public static void main(String[] args) {
        ChromeOptions options = new ChromeOptions();
        options.setCapability("webSocketUrl", true);

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("about:blank");

            // Set up a professional-looking dashboard on the page
            JavascriptExecutor setupExec = (JavascriptExecutor) driver;
            setupExec.executeScript(
                    "document.body.innerHTML = `" +
                            "<div style='font-family:Segoe UI, Tahoma, sans-serif; padding:40px; background:#f4f7f6; min-height:100vh;'>" +
                            "  <h1 style='color:#2c3e50;'>WebDriver Protocol Benchmark</h1>" +
                            "  <p style='color:#7f8c8d;'>Comparing Legacy HTTP vs WebSocket BiDi (No Warmup)</p>" +
                            "  <div style='display:flex; gap:20px; margin-top:20px;'>" +
                            "    <div style='flex:1; padding:20px; background:white; border-radius:8px; border-left:5px solid #e74c3c; box-shadow:0 2px 5px rgba(0,0,0,0.1);'>" +
                            "      <div style='font-weight:bold; color:#e74c3c;'>Classic (HTTP POST)</div>" +
                            "      <div id='classic-counter' style='font-size:32px; margin:10px 0;'>Ready</div>" +
                            "      <div id='classic-res' style='color:#7f8c8d;'>-</div>" +
                            "    </div>" +
                            "    <div style='flex:1; padding:20px; background:white; border-radius:8px; border-left:5px solid #2ecc71; box-shadow:0 2px 5px rgba(0,0,0,0.1);'>" +
                            "      <div style='font-weight:bold; color:#2ecc71;'>BiDi (WebSocket Frame)</div>" +
                            "      <div id='bidi-counter' style='font-size:32px; margin:10px 0;'>Ready</div>" +
                            "      <div id='bidi-res' style='color:#7f8c8d;'>-</div>" +
                            "    </div>" +
                            "  </div>" +
                            "</div>`;"
            );

            String handle = driver.getWindowHandle();
            Script bidiModule = new Script(driver);
            ContextTarget target = new ContextTarget(handle);

            // --- Test 1: Classic executeScript (HTTP POST) ---
            System.out.println("Measuring Classic path...");
            long startClassic = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                String script = "document.getElementById('classic-counter').innerText = 'Iter: " + (i + 1) + "';";
                setupExec.executeScript(script);
            }
            long endClassic = System.nanoTime();

            // Display final Classic results on page
            double classicAvg = ((endClassic - startClassic) / 1_000_000.0) / ITERATIONS;
            setupExec.executeScript("document.getElementById('classic-res').innerText = 'Avg: " + String.format("%.4f", classicAvg) + " ms/call';");

            // --- Test 2: BiDi evaluateFunction (WebSocket Frame) ---
            System.out.println("Measuring BiDi path...");
            long startBidi = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                String script = "document.getElementById('bidi-counter').innerText = 'Iter: " + (i + 1) + "';";
                bidiModule.evaluateFunction(new EvaluateParameters(target, script, false));
            }
            long endBidi = System.nanoTime();

            // Display final BiDi results on page
            double bidiAvg = ((endBidi - startBidi) / 1_000_000.0) / ITERATIONS;
            setupExec.executeScript("document.getElementById('bidi-res').innerText = 'Avg: " + String.format("%.4f", bidiAvg) + " ms/call';");

            // Final Console Summary
            System.out.println("\n--- FINAL RESULTS ---");
            System.out.printf("Classic: %.4f ms/call%n", classicAvg);
            System.out.printf("BiDi:    %.4f ms/call%n", bidiAvg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            driver.quit();
        }
    }
}