"""Check shipped runtime fixtures and translation coverage using Python 3."""
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

root = Path(__file__).resolve().parents[1]
resources = root / "src/wasmJsMain/resources"
ui = "\n".join(p.read_text() for p in (root / "src/wasmJsMain/kotlin").rglob("*.kt"))
languages = [{entry.attrib["name"]: entry.text for entry in ET.parse(root / f"src/commonMain/composeResources/{folder}/strings.xml").getroot()} for folder in ("values-ja", "values")]
assert languages[0].keys() == languages[1].keys()
for strings in languages:
    assert all(isinstance(s, str) and s.strip() for s in strings.values())
    assert set(re.findall(r'(?:\bt|d\.text)\("([a-zA-Z]+)"\)', ui)) <= strings.keys()
    assert {"catalog", "cart", "review", "system", "light", "dark", "back", "retry", "error"} <= strings.keys()
config = json.loads((resources / "config.json").read_text())
catalog = json.loads((resources / config["catalogUrl"]).read_text())
assert catalog["currency"] == "JPY"
products = catalog["products"]
assert products and len({p["id"] for p in products}) == len(products)
for product in products:
    assert set(product["name"]) == set(product["description"]) == {"ja", "en"}
    assert isinstance(product["unitPrice"], int) and product["unitPrice"] >= 0
print(f"{len(languages[0])} paired messages; {len(products)} demo SKUs")
