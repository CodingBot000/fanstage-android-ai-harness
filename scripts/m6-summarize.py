#!/usr/bin/env python3
"""Summarize AndroidX Macrobenchmark per-iteration data without deleting outliers."""
import argparse
import csv
import json
import math
import statistics
from pathlib import Path


def numbers(values):
    if not isinstance(values, list) or not values:
        raise ValueError('empty or invalid run list')
    if any(isinstance(v, bool) or not isinstance(v, (int, float)) or not math.isfinite(v) for v in values):
        raise ValueError('non-numeric or non-finite measurement')
    return [float(v) for v in values]


def percentile(values, p):
    """Nearest rank, p in (0,1]; n=10 means p95 and p99 are the maximum."""
    return sorted(values)[max(0, math.ceil(p * len(values)) - 1)]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('report', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    data = json.loads(args.report.read_text())
    records = []
    for bench in data['benchmarks']:
        label = bench['className'] + '#' + bench['name']
        for name, metric in bench.get('metrics', {}).items():
            for iteration, value in enumerate(numbers(metric['runs']), 1):
                records.append((label, name, 'iteration', iteration, value))
        for name, metric in bench.get('sampledMetrics', {}).items():
            # Preserve iteration boundaries; do not pool frames and call them independent runs.
            runs = metric['runs']
            if not isinstance(runs, list) or not runs: raise ValueError('empty sampled runs')
            for iteration, frames in enumerate(runs, 1):
                frames = numbers(frames)
                for suffix, value in [('frame_median', statistics.median(frames)), ('frame_p95', percentile(frames, .95))]:
                    records.append((label, name, suffix, iteration, value))
    if not records: raise ValueError('no raw measurements')
    args.output.mkdir(parents=True, exist_ok=False)
    with (args.output / 'iterations.csv').open('w', newline='') as f:
        writer = csv.writer(f); writer.writerow(['benchmark', 'metric', 'kind', 'iteration', 'value']); writer.writerows(records)
    groups = {}
    for bench, metric, kind, iteration, value in records:
        groups.setdefault((bench, metric, kind), []).append(value)
    with (args.output / 'summary.csv').open('w', newline='') as f:
        writer = csv.writer(f); writer.writerow(['benchmark', 'metric', 'kind', 'n', 'median', 'p95_nearest_rank', 'p99_nearest_rank', 'min', 'max', 'stdev_sample'])
        for (bench, metric, kind), values in sorted(groups.items()):
            writer.writerow([bench, metric, kind, len(values), statistics.median(values), percentile(values,.95), percentile(values,.99), min(values), max(values), statistics.stdev(values) if len(values)>1 else ''])
    print('PASS: retained all iterations; no outlier filtering; nearest-rank percentiles')


if __name__ == '__main__': main()
