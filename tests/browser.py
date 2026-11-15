"""Smoke-test deployed Wasm assets, font loading and locale-specific requests."""
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
import json
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
        # Exercise the shared navigation through the same-view-width mobile shell.
        mobile = browser.new_page(locale="ja", viewport={"width": 390, "height": 844})
        settings = json.loads((dist / "config.json").read_text())
        settings["orderApi"] = "/api/customer"
        mobile.route("**/config.json", lambda route: route.fulfill(json=settings))
        mobile.route("**/api/customer/orders", lambda route: route.fulfill(json=[]))
        with mobile.expect_response(lambda r: r.url.endswith("/demo/catalog.json")):
            mobile.goto(f"http://127.0.0.1:{server.server_port}/")
        mobile.locator("canvas").wait_for(state="visible")
        # The fourth of five equally spaced navigation items opens order history.
        with mobile.expect_request(lambda r: r.url.endswith("/api/customer/orders"), timeout=15000):
            for _ in range(3):
                mobile.wait_for_timeout(300)
                mobile.mouse.click(273, 802)
        mobile.close()
        browser.close()
finally:
    server.shutdown()
