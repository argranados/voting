import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: Number(__ENV.VUS || 1),
  iterations: Number(__ENV.ITERATIONS || 1),
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1500'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SEASON_ID = Number(__ENV.SEASON_ID || 1);
const RULE_TYPE = __ENV.RULE_TYPE || 'ELIMINATE_LOWEST';
const CONTESTANT_IDS = (__ENV.CONTESTANT_IDS || '1,2')
  .split(',')
  .map((v) => Number(v.trim()))
  .filter((v) => !Number.isNaN(v));

if (CONTESTANT_IDS.length < 2) {
  throw new Error('CONTESTANT_IDS debe tener al menos 2 ids. Ejemplo: 1,2');
}

const jsonHeaders = {
  headers: {
    'Content-Type': 'application/json',
  },
};

function postJson(url, payload) {
  return http.post(url, JSON.stringify(payload), jsonHeaders);
}

function parseJson(res, label) {
  try {
    return res.json();
  } catch (e) {
    throw new Error(`${label}: respuesta no es JSON. Status=${res.status}, body=${res.body}`);
  }
}

export default function () {
  const suffix = `${__VU}-${__ITER}-${Date.now()}`;
  const roundName = `k6-round-${suffix}`;

  // 0) Health check
  const healthRes = http.get(`${BASE_URL}/actuator/health`);
  check(healthRes, {
    'health: status 200': (r) => r.status === 200,
  });

  // 1) Crear round
  const createPayload = {
    seasonId: SEASON_ID,
    name: roundName,
    startsAt: new Date(Date.now() + 60 * 1000).toISOString(),
    endsAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
    ruleType: RULE_TYPE,
  };

  const createRes = postJson(`${BASE_URL}/api/v1/admin/rounds`, createPayload);
  check(createRes, {
    'create round: status 201': (r) => r.status === 201,
  });

  const createdRound = parseJson(createRes, 'create round');
  const roundId = createdRound.id;

  check(createdRound, {
    'create round: has id': (r) => !!r.id,
    'create round: status SCHEDULED': (r) => r.status === 'SCHEDULED',
  });

  // 2) Nominar contestants
  const nominatePayload = {
    contestantIds: CONTESTANT_IDS,
  };

  const nominateRes = postJson(
    `${BASE_URL}/api/v1/admin/rounds/${roundId}/nominees`,
    nominatePayload
  );

  check(nominateRes, {
    'nominate: status 200': (r) => r.status === 200,
  });

  const nomineesCreated = parseJson(nominateRes, 'nominate');
  check(nomineesCreated, {
    'nominate: returned nominees': (r) => Array.isArray(r) && r.length >= 2,
  });

  // 3) Abrir round
  const openRes = http.post(
    `${BASE_URL}/api/v1/admin/rounds/${roundId}/open`,
    null,
    { headers: {} }
  );

  check(openRes, {
    'open round: status 200': (r) => r.status === 200,
  });

  const openedRound = parseJson(openRes, 'open round');
  check(openedRound, {
    'open round: status OPEN': (r) => r.status === 'OPEN',
  });

  // 4) Obtener current round
  const currentRes = http.get(`${BASE_URL}/api/v1/public/rounds/current`);
  check(currentRes, {
    'current round: status 200': (r) => r.status === 200,
  });

  const currentRound = parseJson(currentRes, 'current round');
  check(currentRound, {
    'current round: matches roundId': (r) => r.id === roundId,
    'current round: OPEN': (r) => r.status === 'OPEN',
  });

  // 5) Obtener nominees
  const nomineesRes = http.get(`${BASE_URL}/api/v1/public/rounds/${roundId}/nominees`);
  check(nomineesRes, {
    'get nominees: status 200': (r) => r.status === 200,
  });

  const nominees = parseJson(nomineesRes, 'get nominees');
  check(nominees, {
    'get nominees: list not empty': (r) => Array.isArray(r) && r.length >= 2,
  });

  // 6) Votar varias veces
  const voteTargets = [
    CONTESTANT_IDS[0],
    CONTESTANT_IDS[0],
    CONTESTANT_IDS[1],
  ];

  for (const contestantId of voteTargets) {
    const voteRes = postJson(`${BASE_URL}/api/v1/public/votes`, {
      roundId,
      contestantId,
    });

    check(voteRes, {
      'vote: status 201': (r) => r.status === 201,
    });
  }

  // 7) Ver resultados
  const resultsRes = http.get(`${BASE_URL}/api/v1/public/rounds/${roundId}/results`);
  check(resultsRes, {
    'results: status 200': (r) => r.status === 200,
  });

  const results = parseJson(resultsRes, 'results');
  check(results, {
    'results: has roundId': (r) => r.roundId === roundId,
    'results: has results array': (r) => Array.isArray(r.results),
  });

  // 8) Cerrar round
  const closeRes = http.post(
    `${BASE_URL}/api/v1/admin/rounds/${roundId}/close`,
    null,
    { headers: {} }
  );

  check(closeRes, {
    'close round: status 200': (r) => r.status === 200,
  });

  const closedRound = parseJson(closeRes, 'close round');
  check(closedRound, {
    'close round: status CLOSED': (r) => r.status === 'CLOSED',
  });

  // 9) Confirmar que votar en CLOSED falla
  const voteClosedRes = postJson(`${BASE_URL}/api/v1/public/votes`, {
    roundId,
    contestantId: CONTESTANT_IDS[0],
  });

  check(voteClosedRes, {
    'vote closed: status 400': (r) => r.status === 400,
  });

  sleep(Number(__ENV.SLEEP_SECONDS || 0));
}