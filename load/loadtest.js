#!/usr/bin/env node
// Node >= 18 (global fetch)
// Usage example:
//  node loadtest.mjs \
//    --token "xxx" \
//    --token_id "yyyy" \
//    --url https://api.example.com/secure \
//    --clients 25 \
//    --rate 200 \
//    --invalidateUrl https://otoroshi.example.com/apis/_invalidatetoken \
//    --otoroshiApikey "user:password" \
//    --csv results.csv

import { setTimeout as delay } from 'node:timers/promises';
import { argv, env, exit } from 'node:process';
import { createWriteStream } from 'node:fs';

function parseArgs(args) {
  const out = {
    url: null,
    method: 'GET',
    clients: 10,
    rate: 50, // req/s global
    forgeUrl: null,
    invalidateUrl: null,
    invalidateMethod: 'POST',
    otoroshiApikey: 'foo:bar',
    invalidateAfter: 20, // seconds
    stabilityWindow: 10,  // seconds without 200 after invalidation
    maxRun: 120,         // seconds (guardrail)
    headers: {},
    body: null,
    csv: null,
    quiet: false,
    timeoutMs: 10000,    // timeout per request
    token_id: null,
  };

  const getVal = (name) => {
    const i = args.indexOf(`--${name}`);
    if (i >= 0 && i + 1 < args.length) return args[i + 1];
    return null;
  };
  const has = (name) => args.includes(`--${name}`);

  out.url = getVal('url') ?? out.url;
  out.method = (getVal('method') ?? out.method).toUpperCase();
  out.clients = Number(getVal('clients') ?? out.clients);
  out.rate = Number(getVal('rate') ?? out.rate);
  out.token_id = getVal('token_id') ?? out.token_id;
  out.otoroshiApikey = getVal('otoroshiApikey') ?? out.otoroshiApikey;
  out.forgeUrl = getVal('forgeUrl') ?? out.forgeUrl;
  out.invalidateUrl = getVal('invalidateUrl') ?? out.invalidateUrl;
  out.invalidateMethod = (getVal('invalidateMethod') ?? out.invalidateMethod).toUpperCase();
  out.invalidateAfter = Number(getVal('invalidateAfter') ?? out.invalidateAfter);
  out.stabilityWindow = Number(getVal('stabilityWindow') ?? out.stabilityWindow);
  out.maxRun = Number(getVal('maxRun') ?? out.maxRun);
  out.csv = getVal('csv');
  out.quiet = has('quiet');
  out.timeoutMs = Number(getVal('timeoutMs') ?? out.timeoutMs);

  // Headers optionnels multiples: --header "Key:Value"
  const headerVals = args.flatMap((a, i) => a === '--header' ? [args[i + 1]] : []);
  for (const hv of headerVals) {
    if (!hv) continue;
    const idx = hv.indexOf(':');
    if (idx > 0) {
      const k = hv.slice(0, idx).trim();
      const v = hv.slice(idx + 1).trim();
      out.headers[k] = v;
    }
  }

  // Body optional (fo POST/PUT) via --body 'raw json or text'
  const body = getVal('body');
  if (body !== null) out.body = body;

  // Token via env var (recommandé) ou --token
  const token = env.AUTH_TOKEN ?? getVal('token');
  if (token) {
    out.headers['Authorization'] = `Bearer ${token}`;
  }

  return out;
}

const cfg = parseArgs(argv.slice(2));
if (!cfg.url || !cfg.invalidateUrl) {
  console.error('Missing required args: --url and --invalidateUrl');
  exit(1);
}
if (!cfg.headers['Authorization']) {
  console.warn('Warning: no Authorization header set. Provide AUTH_TOKEN env var or --token.');
}

if (!cfg.quiet) {
  console.log('Config:', {
    url: cfg.url,
    method: cfg.method,
    clients: cfg.clients,
    rate: cfg.rate,
    token_id: cfg.token_id,
    otoroshiApikey: cfg.otoroshiApikey,
    forgeUrl: cfg.forgeUrl,
    invalidateUrl: cfg.invalidateUrl,
    invalidateMethod: cfg.invalidateMethod,
    invalidateAfter: cfg.invalidateAfter,
    stabilityWindow: cfg.stabilityWindow,
    maxRun: cfg.maxRun,
    timeoutMs: cfg.timeoutMs,
    hasAuth: !!cfg.headers['Authorization'],
    extraHeaders: Object.keys(cfg.headers).filter(k => k.toLowerCase() !== 'authorization'),
    csv: cfg.csv
  });
}

// --- Metrics state ---
const startTs = Date.now();
let invalidationTs = null;           // ms epoch when we called invalidate
let last200SeenTs = null;            // ms epoch of last 200 after invalidation
let stopped = false;

const totals = {
  sent: 0,
  ok200: 0,
  non200: 0,
  errors: 0,
};

const perSecond200AfterInvalid = new Map(); // secondOffset => count200

// CSV stream (optional)
let csvStream = null;
if (cfg.csv) {
  csvStream = createWriteStream(cfg.csv, { flags: 'w' });
  csvStream.write('ts_iso,phase,status,duration_ms\n');
}

function nowSec() { return Math.floor((Date.now() - startTs) / 1000); }
function phase() {
  if (invalidationTs === null) return 'pre-invalidation';
  return 'post-invalidation';
}

// --- HTTP helper with timeout ---
async function timedFetch(url, options, timeoutMs) {
  const ac = new AbortController();
  const t = setTimeout(() => ac.abort(), timeoutMs);
  const started = Date.now();
  try {
    const res = await fetch(url, { ...options, signal: ac.signal });
    const dur = Date.now() - started;
    return { res, dur, err: null };
  } catch (err) {
    const dur = Date.now() - started;
    return { res: null, dur, err };
  } finally {
    clearTimeout(t);
  }
}

// --- Worker (client) loop ---
// Each worker pulls "tickets" emitted by the global rate scheduler.
function makeWorker(id, ticketQueue) {
  return (async () => {
    while (!stopped) {
      // Wait for a ticket
      const ticket = await ticketQueue.get();
      if (!ticket) continue;
      if (stopped) break;

      const { headers, method, body } = cfg;
      const { res, dur, err } = await timedFetch(cfg.url, {
        method: 'GET',
        headers,
        body: body ?? undefined
      }, cfg.timeoutMs);

      totals.sent += 1;
      const tsIso = new Date().toISOString();

      if (err) {
        totals.errors += 1;
        if (csvStream) csvStream.write(`${tsIso},${phase()},ERROR,${dur}\n`);
        continue;
      }

      if (res.status === 200) {
        totals.ok200 += 1;
        if (csvStream) csvStream.write(`${tsIso},${phase()},200,${dur}\n`);

        // Only track the 200s after invalidation for the small time series
        if (invalidationTs !== null) {
          last200SeenTs = Date.now();
          const secOffset = Math.max(0, Math.floor((Date.now() - invalidationTs) / 1000));
          perSecond200AfterInvalid.set(
            secOffset,
            (perSecond200AfterInvalid.get(secOffset) ?? 0) + 1
          );
        }
      } else {
        totals.non200 += 1;
        if (csvStream) csvStream.write(`${tsIso},${phase()},${res.status},${dur}\n`);
      }
    }
  })();
}

// --- Simple async ticket queue for rate control ---
class AsyncQueue {
  constructor() { this.q = []; this.waiters = []; }
  push(v) {
    if (this.waiters.length > 0) {
      const w = this.waiters.shift();
      w(v);
    } else {
      this.q.push(v);
    }
  }
  get() {
    return new Promise(resolve => {
      if (this.q.length > 0) {
        const v = this.q.shift();
        resolve(v);
      } else {
        this.waiters.push(resolve);
      }
    });
  }
}

// --- Global rate scheduler (token-bucket-ish) ---
// Emits `rate` tickets per second, distributed in 10 slices for smoother pacing.
async function runScheduler(ticketQueue) {
  const slices = 10;
  const perSlice = cfg.rate / slices;

  while (!stopped) {
    for (let i = 0; i < slices; i++) {
      const toEmit = Math.floor(perSlice) + ((Math.random() < (perSlice % 1)) ? 1 : 0);
      for (let j = 0; j < toEmit; j++) ticketQueue.push({ t: Date.now() });
      await delay(100); // 100ms slice
      if (stopped) break;
    }
  }
}

// --- Invalidation task ---
async function invalidateLater() {
  await delay(cfg.invalidateAfter * 1000);
  const t0 = Date.now();
  const { res, dur, err } = await timedFetch(cfg.invalidateUrl, {
    method: cfg.invalidateMethod,
    headers: {
      authorization: `Basic ${Buffer.from(cfg.otoroshiApikey, 'utf8').toString('base64')}`
    },
    body: JSON.stringify([
      {
        id: cfg.token_id
      }
    ])
  }, cfg.timeoutMs);

  invalidationTs = t0;
  if (!cfg.quiet) {
    if (err) console.error(`[invalidate] ERROR after ${dur}ms:`, err);
    else console.log(`[invalidate] status=${res.status} after ${dur}ms @ ${new Date(t0).toISOString()}`);
  }
}

// generate a token
async function generateToken() {
  console.log('generating a token ...')
  const { res, dur, err } = await timedFetch(cfg.forgeUrl, {
    method: "POST",
    headers: {
      authorization: `Basic ${Buffer.from(cfg.otoroshiApikey, 'utf8').toString('base64')}`
    },
    body: ''
  }, cfg.timeoutMs);
  const json = await res.json();
  console.log('token used will be:', JSON.stringify(json, null, 2));
  cfg.token = json.token;
  cfg.token_id = json.token_revocation_ids[0];
  cfg.headers['Authorization'] = `Bearer ${json.token}`;
}

// --- Stop condition monitor ---
// Stop when we've seen no 200s for `stabilityWindow` seconds post-invalidation or we hit maxRun.
async function stopWhenStable() {
  while (!stopped) {
    await delay(250);
    const elapsed = (Date.now() - startTs) / 1000;
    if (elapsed >= cfg.maxRun) {
      if (!cfg.quiet) console.warn(`[stop] maxRun ${cfg.maxRun}s reached.`);
      break;
    }
    if (invalidationTs !== null) {
      const sinceInv = (Date.now() - invalidationTs) / 1000;
      if (last200SeenTs === null && sinceInv >= cfg.stabilityWindow) {
        // No 200 has been seen at all post-invalidation
        break;
      }
      if (last200SeenTs !== null) {
        const no200For = (Date.now() - last200SeenTs) / 1000;
        if (no200For >= cfg.stabilityWindow) break;
      }
    }
  }
  stopped = true;
}

// --- Main ---
(async () => {
  await generateToken();

  const ticketQueue = new AsyncQueue();

  // Spawn workers
  const workers = [];
  for (let i = 0; i < cfg.clients; i++) {
    workers.push(makeWorker(i, ticketQueue));
  }

  // Fire off tasks
  const scheduler = runScheduler(ticketQueue);
  const invalidator = invalidateLater();
  const stopper = stopWhenStable();

  // Wait stop
  await stopper;

  // Give a tiny grace period to let workers finish current tickets
  await delay(300);
  if (csvStream) csvStream.end();

  // Compute metrics
  const endTs = Date.now();
  const totalRunS = ((endTs - startTs) / 1000).toFixed(3);
  const invIso = invalidationTs ? new Date(invalidationTs).toISOString() : 'n/a';

  let timeToZero200s = null;
  if (invalidationTs !== null) {
    if (last200SeenTs === null) timeToZero200s = 0;
    else timeToZero200s = Math.max(0, (last200SeenTs - invalidationTs) / 1000);
  }

  // Pretty small timeseries of 200/s after invalidation
  const series = [];
  if (invalidationTs !== null) {
    const maxSec = Math.max(...Array.from(perSecond200AfterInvalid.keys()), 0);
    for (let s = 0; s <= maxSec; s++) {
      series.push({ t_sec_after_invalidation: s, http_200: perSecond200AfterInvalid.get(s) ?? 0 });
    }
  }

  // Print report
  console.log('\n=== Load Test Report ===');
  console.log(`Total run:            ${totalRunS}s`);
  console.log(`Clients:              ${cfg.clients}`);
  console.log(`Global rate target:   ${cfg.rate} req/s`);
  console.log(`Invalidate at:        T+${cfg.invalidateAfter}s  (${invIso})`);
  console.log(`Requests sent:        ${totals.sent}`);
  console.log(`HTTP 200:             ${totals.ok200}`);
  console.log(`HTTP non-200:         ${totals.non200}`);
  console.log(`Errors (network/TO):  ${totals.errors}`);
  if (invalidationTs !== null) {
    console.log(`Time until 0x200:     ${timeToZero200s !== null ? timeToZero200s.toFixed(3) + 's' : 'n/a'}  (window=${cfg.stabilityWindow}s)`);
  }

  if (series.length > 0) {
    console.log('\n200/s after invalidation (first seconds):');
    const first = series.slice(0, 30); // print first 30s
    for (const p of first) {
      console.log(` +${String(p.t_sec_after_invalidation).padStart(2, ' ')}s : ${p.http_200}`);
    }
    if (series.length > first.length) {
      console.log(` ... (${series.length - first.length} more seconds)`);
    }
  } else {
    console.log('\n(no 200 observed after invalidation)');
  }

  // Clean exit
  exit(0);
})();