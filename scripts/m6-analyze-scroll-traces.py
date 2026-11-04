#!/usr/bin/env python3
"""Summarize local FrameTimeline categories; do not upload traces or device data."""
import argparse
import csv
from pathlib import Path
from perfetto.trace_processor import TraceProcessor

parser = argparse.ArgumentParser()
parser.add_argument('directory', type=Path)
parser.add_argument('--output', type=Path, required=True)
parser.add_argument('--cpu-output', type=Path)
args = parser.parse_args()
traces = sorted(args.directory.rglob('*ChatScrollBenchmark_scrollWithPlayingFixture_iter*.perfetto-trace'))
if not traces: raise SystemExit('no scroll trace files')
query = """with m as (select ts,dur from slice where name='measureBlock' and dur>0 order by dur desc limit 1)
select coalesce(a.jank_type,'unknown') category,count(*) count
from actual_frame_timeline_slice a join process p on p.upid=a.upid cross join m
where p.name='dev.fanstage.android.benchmark' and a.ts < m.ts+m.dur and a.ts+a.dur > m.ts
group by 1 order by 1"""
cpu_query = """with m as (select ts,dur from slice where name='measureBlock' and dur>0 order by dur desc limit 1)
select count(*) slices,round(sum(max(0,min(s.ts+s.dur,m.ts+m.dur)-max(s.ts,m.ts)))/1e9,6) cpu_seconds,
round(m.dur/1e9,6) wall_seconds from sched s join thread t on t.utid=s.utid join process p on p.upid=t.upid cross join m
where p.name='dev.fanstage.android.benchmark' and s.dur>0 and s.ts<m.ts+m.dur and s.ts+s.dur>m.ts"""
cpu_rows = []
with args.output.open('x', newline='') as f:
    writer = csv.writer(f); writer.writerow(['iteration','category','count'])
    for trace in traces:
        with TraceProcessor(trace=str(trace)) as tp:
            iteration = trace.name.split('_iter')[1].split('_')[0]
            for row in tp.query(query):
                writer.writerow([iteration,row.category,row.count])
            row = next(iter(tp.query(cpu_query)))
            if row.slices == 0 or row.cpu_seconds is None or not row.wall_seconds:
                raise ValueError('missing app CPU scheduler data')
            cpu_rows.append([iteration,row.slices,row.cpu_seconds,row.wall_seconds,row.cpu_seconds / row.wall_seconds])
if args.cpu_output:
    with args.cpu_output.open('x', newline='') as f:
        writer = csv.writer(f); writer.writerow(['iteration','scheduler_slices','app_cpu_seconds','measure_wall_seconds','average_core_equivalents']); writer.writerows(cpu_rows)
print('PASS: trace categories recorded locally; correlation does not identify root cause')
