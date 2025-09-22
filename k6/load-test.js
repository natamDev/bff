import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  scenarios: {
    host_page: {
      executor: 'constant-vus',
      vus: 20,
      duration: '30s',
      exec: 'hostScenario',
    },
    invite_page: {
      executor: 'constant-vus',
      vus: 50,
      duration: '30s',
      exec: 'inviteScenario',
    },
  },
};

const BASE = 'https://calm-genevieve-bfftest-2a43b006.koyeb.app';
const EVENT_ID = '68d08d051d76d0129cfa737c';
const SIG = 'z3Y7k3bIr_-YaABfU6h5aeeeb1s3be8qolDf_mzJwCU';
const INVITE_ID = 'fDqpP-xMy2K7';

export function hostScenario() {
  let res = http.get(`${BASE}/events/${EVENT_ID}`);
  check(res, { 'event 200': (r) => r.status === 200 });

  res = http.get(`${BASE}/events/${EVENT_ID}/invites/links?sig=${SIG}`);
  check(res, { 'invites 200': (r) => r.status === 200 });

  res = http.get(`${BASE}/events/${EVENT_ID}/predictions/matrix?sig=${SIG}`);
  check(res, { 'matrix 200': (r) => r.status === 200 });

  sleep(1);
}

export function inviteScenario() {
  let res = http.get(`${BASE}/events/${EVENT_ID}/as-invite?inviteId=${INVITE_ID}&sig=${SIG}`);
  check(res, { 'invite 200': (r) => r.status === 200 });
  sleep(1);
}
