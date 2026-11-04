#!/usr/bin/env python3
"""Summarize selected slices in local Perfetto traces; no trace is uploaded."""
import argparse
import csv
from pathlib import Path
from perfetto.trace_processor import TraceProcessor

parser = argparse.ArgumentParser()
parser.add_argument('directory', type=Path)
parser.add_argument('--output', type=Path, required=True)
args = parser.parse_args()
traces = sorted(args.directory.rglob('*ColdStartupBenchmark_coldStartup_iter*.perfetto-trace'))
if not traces: raise SystemExit('no startup trace files')
query = """select round(max(case when s.name='bindApplication' then s.dur end)/1e6,2) bind_ms,
round(max(case when s.name like 'Texture upload%' then s.dur end)/1e6,2) texture_ms,
round(max(case when s.name='Compose:recompose' then s.dur end)/1e6,2) compose_ms,
round(max(case when s.name like 'OpenDexFilesFromOat%' then s.dur end)/1e6,2) oat_ms,
round(max(case when s.name='FontProvider.query' then s.dur end)/1e6,2) font_ms
from slice s join thread_track tr on s.track_id=tr.id join thread t on t.utid=tr.utid
join process p on p.upid=t.upid where p.name='dev.fanstage.android.benchmark' and s.dur>=0"""
with args.output.open('x', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(['iteration','bind_ms','max_texture_upload_ms','max_compose_recompose_ms','max_oat_open_ms','max_font_query_ms'])
    for trace in traces:
        with TraceProcessor(trace=str(trace)) as tp:
            row = next(iter(tp.query(query)))
            writer.writerow([trace.name.split('_iter')[1].split('_')[0],row.bind_ms,row.texture_ms,row.compose_ms,row.oat_ms,row.font_ms])
print('PASS: startup trace summaries retained locally; slice times are not additive and do not establish causality')
