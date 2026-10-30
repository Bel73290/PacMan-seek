"""Generate the checked-in Spark rules from the skin catalogue."""
import argparse
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument("--check", action="store_true")
args = parser.parse_args()
catalog = json.loads((root / "functions/catalog.json").read_text(encoding="utf-8"))
items = {s["id"]: {"price": s["price"], "level": s["unlockLevel"], "role": s["role"]} for s in catalog}
rules = (root / "tools/firestore.rules.template").read_text(encoding="utf-8").replace(
    "__CATALOG__", json.dumps(items, separators=(",", ":")))
target = root / "firestore.rules"
if args.check:
    if target.read_text(encoding="utf-8") != rules:
        raise SystemExit("Run python tools/generate_firestore_rules.py before committing.")
else:
    target.write_text(rules, encoding="utf-8")
