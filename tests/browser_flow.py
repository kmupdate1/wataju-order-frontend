"""Canvas interaction checks: locale/theme recomposition, lifecycle and persisted retry.
Requires Playwright Chromium and tesseract (OCR locates rendered button labels).
All orders are intercepted; no real server is contacted.
"""
import csv
import io
import json
import os
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import subprocess
from threading import Thread
from playwright.sync_api import sync_playwright
from PIL import Image

root = Path(__file__).resolve().parents[1]
dist = root / 'build/dist/wasmJs/productionExecutable'
shots = root / 'build/browser-check'
shots.mkdir(parents=True, exist_ok=True)


def click_text(page, label):
    wanted = label.lower().split()
    for _ in range(12):
        page.wait_for_timeout(250)
        screenshot = page.screenshot()
        # OCR otherwise drops light filled buttons against a dark surface.
        normalized = io.BytesIO()
        Image.open(io.BytesIO(screenshot)).convert('L').point(
            lambda value: 255 if value < 40 or value > 140 else 0
        ).save(normalized, format='PNG')
        for pixels in (screenshot, normalized.getvalue()):
            result = subprocess.run(['tesseract', 'stdin', 'stdout', '--psm', '11', 'tsv'],
                                    input=pixels, capture_output=True, check=True)
            words = [r for r in csv.DictReader(io.StringIO(result.stdout.decode()), delimiter='\t') if r['text'].strip()]
            for start in range(len(words) - len(wanted) + 1):
                group = words[start:start + len(wanted)]
                if [w['text'].lower() for w in group] == wanted:
                    left = min(int(w['left']) for w in group)
                    top = min(int(w['top']) for w in group)
                    right = max(int(w['left']) + int(w['width']) for w in group)
                    bottom = max(int(w['top']) + int(w['height']) for w in group)
                    page.mouse.click((left + right) / 2, (top + bottom) / 2)
                    return
    page.screenshot(path=shots / 'flow-failure.png')
    raise AssertionError(f'Rendered label not found: {label}')

server = ThreadingHTTPServer(('127.0.0.1', 0), partial(SimpleHTTPRequestHandler, directory=str(dist)))
Thread(target=server.serve_forever, daemon=True).start()
try:
    with sync_playwright() as pw:
        options = dict(args=['--enable-unsafe-swiftshader'])
        if os.environ.get('WATAJU_CHROMIUM_PATH'):
            options.update(executable_path=os.environ['WATAJU_CHROMIUM_PATH'],
                           args=['--enable-unsafe-swiftshader', '--single-process', '--no-zygote'])
        browser = pw.chromium.launch(**options)
        base = f'http://127.0.0.1:{server.server_port}/'
        page = browser.new_page(locale='ja', viewport=dict(width=390, height=844))
        errors, lifecycle = [], []
        page.on('pageerror', lambda e: errors.append(str(e)))
        page.on('console', lambda m: lifecycle.append(m.text))
        with page.expect_response(lambda r: r.url.endswith('/demo/catalog.json'), timeout=60000):
            page.goto(base)
        page.wait_for_timeout(1500)
        page.mouse.click(351, 802)  # mobile Account tab, independent of Japanese OCR
        with page.expect_response(lambda r: r.url.endswith('/demo/catalog.json')) as catalog:
            click_text(page, 'English')
        assert catalog.value.request.headers['accept-language'] == 'en'
        assert page.title() == 'Wataju Shoten'
        assert page.evaluate("localStorage.getItem('wataju.locale')") == 'en'
        page.wait_for_timeout(800)
        page.mouse.click(351, 802)
        click_text(page, 'Dark theme')
        page.wait_for_timeout(300)
        assert page.evaluate("localStorage.getItem('wataju.theme')") == 'dark'
        # Changing appearance must not recreate the application or restart participants.
        assert lifecycle.count('org.b3.foundation.application.Application.onCreate()') == 1
        assert lifecycle.count('org.b3.foundation.application.Application.onStart()') == 1
        page.evaluate("""() => {
            Object.defineProperty(document, 'hidden', {configurable:true, value:true});
            document.dispatchEvent(new Event('visibilitychange'));
            Object.defineProperty(document, 'hidden', {configurable:true, value:false});
            document.dispatchEvent(new Event('visibilitychange'));
            delete document.hidden;
        }""")
        assert lifecycle.count('org.b3.foundation.application.Application.onStop()') == 1
        assert lifecycle.count('org.b3.foundation.application.Application.onStart()') == 2
        page.screenshot(path=shots / 'foundation-locale-theme.png')
        assert not errors, errors

        checkout = browser.new_page(locale='en', viewport=dict(width=1280, height=900))
        # Use an explicit appearance: contexts must not rely on prior page preferences.
        checkout.add_init_script("localStorage.setItem('wataju.theme', 'dark');")
        checkout.on('pageerror', lambda e: errors.append(str(e)))
        settings = json.loads((dist / 'config.json').read_text())
        settings['orderApi'] = '/api/customer'
        namespace = 'wataju.' + settings['storeId'] + './api/customer'
        product = json.loads((dist / 'demo/catalog.json').read_text())['products'][0]
        quote = dict(id='quote-test', currency='JPY', lines=[dict(sku=product['id'], name=product['name'],
                     size=product['size'], quantity=1, unitPrice=650)], shipping=0, tax=65, total=715)
        pending = dict(quote=quote, destination=dict(recipient='Test Customer', postal='100-0001', address='Tokyo'), key='stable-retry-key')
        checkout.add_init_script('''if (!sessionStorage.getItem('test-seeded')) {
            sessionStorage.setItem('test-seeded', 'yes');
            sessionStorage.setItem(%s, %s);
        }''' % (json.dumps(namespace + '.pending'), json.dumps(json.dumps(pending))))
        checkout.route('**/config.json', lambda route: route.fulfill(json=settings))
        requests = []
        def place(route):
            requests.append(route.request)
            if len(requests) == 1: route.fulfill(status=503, body='temporary failure')
            else: route.fulfill(json=dict(id='order-test', quote=quote))
        checkout.route('**/api/customer/orders', place)
        with checkout.expect_response(lambda r: r.url.endswith('/demo/catalog.json'), timeout=60000):
            checkout.goto(base)
        with checkout.expect_response(lambda r: r.url.endswith('/api/customer/orders')):
            click_text(checkout, 'Confirm order')
        assert checkout.evaluate('(k) => sessionStorage.getItem(k)', namespace + '.submitted') == 'stable-retry-key'
        with checkout.expect_response(lambda r: r.url.endswith('/demo/catalog.json')):
            checkout.reload()
        with checkout.expect_response(lambda r: r.url.endswith('/api/customer/orders')):
            click_text(checkout, 'Retry this order')
        checkout.wait_for_function('(k) => sessionStorage.getItem(k) === null', arg=namespace + '.pending')
        assert len(requests) == 2
        assert all(r.headers['idempotency-key'] == 'stable-retry-key' for r in requests)
        assert all(r.post_data_json == {'quoteId': 'quote-test'} for r in requests)
        checkout.screenshot(path=shots / 'foundation-order-complete.png')
        assert not errors, errors
        browser.close()
finally:
    server.shutdown()
print('Locale switch, theme retention, lifecycle resume and persisted order retry passed')
