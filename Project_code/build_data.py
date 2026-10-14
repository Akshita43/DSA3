import csv, json
rows = [[r['name'], r['category'], float(r['price']), r['description']]
        for r in csv.DictReader(open("products.csv", encoding="utf-8"))]
words = [w.strip().lower() for w in open("dictionary.txt", encoding="utf-8") if w.strip()]
open("data.js", "w", encoding="utf-8").write(
    "const RAW=" + json.dumps(rows, ensure_ascii=False) + ";\nconst DICT=" + json.dumps(words) + ";\n")