#!/usr/bin/env python3
"""Draws plots/*.png from results/*.csv (produced by textanalysis.bench.Benchmark).
Requires: pip install matplotlib"""
import csv
import os
from collections import defaultdict

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "results")
OUT = os.path.join(ROOT, "plots")
os.makedirs(OUT, exist_ok=True)


def read(name):
    with open(os.path.join(RES, name), newline="") as f:
        return list(csv.DictReader(f))


# 1. DP: time vs input size
dp = read("dp.csv")
series = defaultdict(list)
for r in dp:
    series[r["algorithm"]].append((int(r["n"]), float(r["avg_ms"])))
fig, ax = plt.subplots(figsize=(7, 4.5))
for name, pts in series.items():
    pts.sort()
    ax.plot([p[0] for p in pts], [p[1] for p in pts], marker="o", label=name)
ax.set_title("LCS and Edit Distance: execution time vs input size (n = m)")
ax.set_xlabel("String length n")
ax.set_ylabel("Average time, ms (5 runs)")
ax.grid(True, alpha=0.3)
ax.legend()
fig.tight_layout()
fig.savefig(os.path.join(OUT, "dp_time.png"), dpi=150)
plt.close(fig)

# 2. String matching: time vs text length, one panel per scenario
match = read("matching.csv")
scenarios = ["present", "absent", "worst_case"]
titles = {"present": "Pattern present (random text)",
          "absent": "Pattern absent (random text)",
          "worst_case": "Adversarial: a^n, pattern a^15 b"}
fig, axes = plt.subplots(1, 3, figsize=(15, 4.5))
for ax, sc in zip(axes, scenarios):
    s = defaultdict(list)
    for r in match:
        if r["scenario"] == sc:
            s[r["algorithm"]].append((int(r["text_length"]), float(r["avg_ms"])))
    for name, pts in s.items():
        pts.sort()
        ax.plot([p[0] for p in pts], [p[1] for p in pts], marker="o", label=name)
    ax.set_title(titles[sc])
    ax.set_xlabel("Text length n")
    ax.set_ylabel("Average time, ms (5 runs)")
    ax.grid(True, alpha=0.3)
    ax.legend()
fig.suptitle("String matching: execution time vs text length (pattern length 16)")
fig.tight_layout()
fig.savefig(os.path.join(OUT, "matching_time.png"), dpi=150)
plt.close(fig)

# 3. String matching: character comparisons (machine-independent)
fig, axes = plt.subplots(1, 2, figsize=(11, 4.5))
for ax, sc in zip(axes, ["present", "worst_case"]):
    s = defaultdict(list)
    for r in match:
        if r["scenario"] == sc:
            total = int(r["search_comparisons"]) + int(r["preprocess_comparisons"])
            s[r["algorithm"]].append((int(r["text_length"]), total))
    for name, pts in s.items():
        pts.sort()
        ax.plot([p[0] for p in pts], [p[1] for p in pts], marker="o", label=name)
    ax.set_title(titles[sc])
    ax.set_xlabel("Text length n")
    ax.set_ylabel("Character comparisons")
    ax.grid(True, alpha=0.3)
    ax.legend()
fig.suptitle("String matching: character comparisons vs text length")
fig.tight_layout()
fig.savefig(os.path.join(OUT, "matching_comparisons.png"), dpi=150)
plt.close(fig)

# 4. Kasai
kasai = read("kasai.csv")
n = [int(r["n"]) for r in kasai]
t = [float(r["avg_ms"]) for r in kasai]
c = [int(r["char_comparisons"]) for r in kasai]
fig, ax1 = plt.subplots(figsize=(7, 4.5))
ax1.plot(n, t, marker="o", color="tab:blue", label="time")
ax1.set_xlabel("String length n")
ax1.set_ylabel("Average time, ms (5 runs)", color="tab:blue")
ax2 = ax1.twinx()
ax2.plot(n, c, marker="s", linestyle="--", color="tab:orange", label="comparisons")
ax2.set_ylabel("Character comparisons", color="tab:orange")
ax1.set_title("Kasai LCP: time and comparisons vs n")
ax1.grid(True, alpha=0.3)
fig.tight_layout()
fig.savefig(os.path.join(OUT, "kasai_time.png"), dpi=150)
plt.close(fig)

print("Plots written to", OUT)
