import time

import traceback
from selenium import webdriver

ITERATIONS = 500

chrome_options = webdriver.ChromeOptions()
chrome_options.enable_bidi = True
driver = webdriver.Chrome(options=chrome_options)

try:
    driver.get("about:blank")

    # Set up a professional-looking dashboard on the page
    driver.execute_script("""
document.body.innerHTML = `
<div style='font-family:Segoe UI, Tahoma, sans-serif; padding:40px; background:#f4f7f6; min-height:100vh;'>
  <h1 style='color:#2c3e50;'>WebDriver Protocol Benchmark</h1>
  <p style='color:#7f8c8d;'>Comparing Legacy HTTP vs WebSocket BiDi (No Warmup)</p>
  <div style='display:flex; gap:20px; margin-top:20px;'>
    <div style='flex:1; padding:20px; background:white; border-radius:8px; border-left:5px solid #e74c3c; box-shadow:0 2px 5px rgba(0,0,0,0.1);'>
      <div style='font-weight:bold; color:#e74c3c;'>Classic (HTTP POST)</div>
      <div id='classic-counter' style='font-size:32px; margin:10px 0;'>Ready</div>
      <div id='classic-res' style='color:#7f8c8d;'>-</div>
    </div>
    <div style='flex:1; padding:20px; background:white; border-radius:8px; border-left:5px solid #2ecc71; box-shadow:0 2px 5px rgba(0,0,0,0.1);'>
      <div style='font-weight:bold; color:#2ecc71;'>BiDi (WebSocket Frame)</div>
      <div id='bidi-counter' style='font-size:32px; margin:10px 0;'>Ready</div>
      <div id='bidi-res' style='color:#7f8c8d;'>-</div>
    </div>
  </div>
</div>`;
    """)


    # String handle = driver.getWindowHandle();
    # Script bidiModule = new Script(driver);
    # ContextTarget target = new ContextTarget(handle);

    # --- Test 1: Classic executeScript (HTTP POST) ---
    print("Measuring Classic path...")
    start_classic = time.perf_counter_ns()
    for i in range(ITERATIONS):
        # driver.execute_script("document.getElementById('classic-counter').innerText = 'Iter: " + str(i + 1) + "';")
        driver.execute_script(f"document.getElementById('classic-counter').innerText = 'Iter: {int(i + 1)}';")
    end_classic = time.perf_counter_ns()

    # Display final Classic results on page
    classic_avg = ((end_classic - start_classic) / 1_000_000.0) / ITERATIONS
    driver.execute_script(f"document.getElementById('classic-res').innerText = 'Avg: {classic_avg} ms/call';")

    # --- Test 2: BiDi evaluateFunction (WebSocket Frame) ---
    print("Measuring BiDi path...")
    start_bidi = time.perf_counter_ns()
    for i in range(ITERATIONS):
        driver.script._evaluate(f"document.getElementById('bidi-counter').innerText = 'Iter: {(i + 1)}';", {"context": driver.current_window_handle}, await_promise=False)
    end_bidi = time.perf_counter_ns()

    # Display final BiDi results on page
    bidi_avg = ((end_bidi - start_bidi) / 1_000_000.0) / ITERATIONS
    driver.execute_script(f"document.getElementById('bidi-res').innerText = 'Avg: {bidi_avg} ms/call';")

    # Final Console Summary
    print("\n--- FINAL RESULTS ---")
    print(f"Classic: {classic_avg} ms/call")
    print(f"BiDi:    {bidi_avg} ms/call%n")
except Exception:
    print(traceback.format_exc())
finally:
    time.sleep(5)
    driver.quit()