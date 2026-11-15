"""Smoke-test deployed Wasm assets, font loading and locale-specific requests."""
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from threading import Thread

from playwright.sync_api import sync_playwright

root = Path(__file__).resolve().parents[1]
dist = root / "build/dist/wasmJs/productionExecutable"
shots = root / "build/browser-check"
shots.mkdir(parents=True, exist_ok=True)
server = ThreadingHTTPServer(("127.0.0.1", 0), partial(SimpleHTTPRequestHandler, directory=str(dist)))
Thread(target=server.serve_forever, daemon=True).start()
try:
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(args=["--enable-unsafe-swiftshader"])
        for lang, title, width in [("ja", "綿重商店", 390), ("en", "Wataju Shoten", 1280)]:
            page = browser.new_page(locale=lang, viewport={"width": width, "height": 844})
            errors, fonts = [], []
            page.on("pageerror", lambda error: errors.append(str(error)))
            page.on("response", lambda response: fonts.append(response.status)
                    if response.url.endswith("/fonts/NotoSansJP.ttf") else None)
            with page.expect_response(lambda r: r.url.endswith("/demo/catalog.json"), timeout=60000) as result:
                page.goto(f"http://127.0.0.1:{server.server_port}/")
            assert result.value.ok and result.value.request.headers["accept-language"] == lang
            assert fonts == [200] and page.title() == title
            page.locator("canvas").wait_for(state="visible")
            page.wait_for_timeout(1000)
            page.screenshot(path=shots / f"{lang}.png")
            assert not errors, errors
            page.close()
        browser.close()
finally:
    server.shutdown()
