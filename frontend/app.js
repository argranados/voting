const API = '';
const alertBox = document.getElementById('alertBox');
const healthBadge = document.getElementById('healthBadge');
const currentRoundState = document.getElementById('currentRoundState');
const nomineesList = document.getElementById('nomineesList');
const resultsState = document.getElementById('resultsState');
const resultsList = document.getElementById('resultsList');
const seasonList = document.getElementById('seasonList');
const contestantList = document.getElementById('contestantList');

// Token en memoria — nunca en localStorage
let authToken = null;

function showAlert(message, type = 'success') { alertBox.className = `alert alert-${type}`; alertBox.textContent = message; alertBox.classList.remove('d-none'); window.scrollTo({ top: 0, behavior: 'smooth' }); }
function hideAlert() { alertBox.classList.add('d-none'); }
function toIsoFromLocal(value) { return new Date(value).toISOString(); }

async function apiFetch(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };

  // Si hay token, lo agrega automáticamente
  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }

  const response = await fetch(`${API}${path}`, { ...options, headers });
  const text = await response.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }

  // Si el token expiró o es inválido, forzar logout
  if (response.status === 401) {
    logout();
    throw new Error('Sesión expirada. Por favor inicia sesión de nuevo.');
  }

  if (!response.ok) throw new Error(data?.message || `HTTP ${response.status}`);
  return data;
}

// Auth
function logout() {
  authToken = null;
  updateAdminUI();
}

function updateAdminUI() {
  const loginSection = document.getElementById('loginSection');
  const adminContent = document.getElementById('adminContent');
  if (authToken) {
    loginSection.classList.add('d-none');
    adminContent.classList.remove('d-none');
  } else {
    loginSection.classList.remove('d-none');
    adminContent.classList.add('d-none');
  }
}

async function login(username, password) {
  const data = await apiFetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  });
  authToken = data.token;
  updateAdminUI();
  await loadSeasons();
  await loadContestants();
  showAlert('Sesión iniciada correctamente.', 'success');
}

// Health
async function loadHealth() {
  try {
    const response = await fetch('/actuator/health');
    const data = await response.json();
    healthBadge.className = 'badge text-bg-success';
    healthBadge.textContent = `Backend: ${data.status}`;
  } catch {
    healthBadge.className = 'badge text-bg-danger';
    healthBadge.textContent = 'Backend: DOWN';
  }
}

function renderCurrentRound(round) {
  currentRoundState.innerHTML = `<div class="p-3 bg-body-tertiary rounded-4">
    <div><span class="small-label">Round ID</span><div class="fw-semibold">${round.id}</div></div>
    <div class="mt-2"><span class="small-label">Name</span><div>${round.name}</div></div>
    <div class="mt-2"><span class="small-label">Status</span><div>${round.status}</div></div>
    <div class="mt-2"><span class="small-label">Season ID</span><div>${round.seasonId}</div></div>
  </div>`;
  document.getElementById('voteRoundId').value = round.id;
  document.getElementById('resultsRoundId').value = round.id;
}

function renderNominees(nominees, roundId) {
  nomineesList.innerHTML = '';
  if (!nominees.length) { nomineesList.innerHTML = '<div class="text-muted">No hay nominados para esta ronda.</div>'; return; }
  nominees.forEach((n) => {
    const col = document.createElement('div');
    col.className = 'col-md-6';
    col.innerHTML = `<div class="card nominee-card border"><div class="card-body">
      <div class="small-label">Contestant ID</div>
      <div class="fs-4 fw-bold mb-3">${n.contestantName}</div>
      <div class="small-label mb-3">ID: ${n.contestantId}</div>
      <button class="btn btn-outline-success" data-round-id="${roundId}" data-contestant-id="${n.contestantId}">Votar por ${n.contestantName}</button>
    </div></div>`;
    nomineesList.appendChild(col);
  });
  nomineesList.querySelectorAll('button[data-contestant-id]').forEach((btn) => {
    btn.addEventListener('click', async () => {
      try {
        hideAlert();
        const contestantId = Number(btn.dataset.contestantId);
        document.getElementById('voteContestantId').value = contestantId;
        await castVote(roundId, contestantId);
      } catch (error) { showAlert(error.message, 'danger'); }
    });
  });
}

function renderResults(resultDto) {
  resultsState.innerHTML = `<div class="fw-semibold">Resultados del round ${resultDto.roundId}</div>`;
  resultsList.innerHTML = '';
  if (!resultDto.results?.length) { resultsList.innerHTML = '<div class="text-muted mt-2">No hay resultados todavía.</div>'; return; }
  resultDto.results.forEach((item, index) => {
    const row = document.createElement('div');
    row.className = 'list-group-item d-flex justify-content-between align-items-center';
    row.innerHTML = `<div><div class="small-label">Posición ${index + 1}</div><div class="fw-semibold">${item.contestantName}</div><div class="small-label">ID: ${item.contestantId}</div></div><span class="badge text-bg-primary rounded-pill">${item.votes} votos</span>`;
    resultsList.appendChild(row);
  });
}

async function loadCurrentRound() {
  try {
    hideAlert();
    currentRoundState.textContent = 'Cargando ronda actual...';
    nomineesList.innerHTML = '';
    const round = await apiFetch('/api/v1/public/rounds/current');
    renderCurrentRound(round);
    const nominees = await apiFetch(`/api/v1/public/rounds/${round.id}/nominees`);
    renderNominees(nominees, round.id);
  } catch {
    currentRoundState.innerHTML = '<div class="text-muted">No hay ronda OPEN en este momento.</div>';
    nomineesList.innerHTML = '';
  }
}

async function castVote(roundId, contestantId) {
  await apiFetch('/api/v1/public/votes', { method: 'POST', body: JSON.stringify({ roundId, contestantId }) });
  showAlert('Voto registrado correctamente.', 'success');
}

async function loadResults(roundId) { renderResults(await apiFetch(`/api/v1/public/rounds/${roundId}/results`)); }

async function createRound(payload) {
  const round = await apiFetch('/api/v1/admin/rounds', { method: 'POST', body: JSON.stringify(payload) });
  showAlert(`Round creado con ID ${round.id}.`, 'success');
  document.getElementById('nominateRoundId').value = round.id;
  document.getElementById('actionRoundId').value = round.id;
}

async function nominate(roundId, contestantIds) {
  await apiFetch(`/api/v1/admin/rounds/${roundId}/nominees`, { method: 'POST', body: JSON.stringify({ contestantIds }) });
  showAlert('Contestants nominados correctamente.', 'success');
}

async function openRound(roundId) { await apiFetch(`/api/v1/admin/rounds/${roundId}/open`, { method: 'POST' }); showAlert(`Round ${roundId} abierto.`, 'success'); }
async function closeRound(roundId) { await apiFetch(`/api/v1/admin/rounds/${roundId}/close`, { method: 'POST' }); showAlert(`Round ${roundId} cerrado.`, 'success'); }

function renderSeasons(seasons) {
  seasonList.innerHTML = '';
  if (!seasons.length) { seasonList.innerHTML = '<div class="text-muted">No hay seasons.</div>'; return; }
  seasons.forEach((season) => {
    const row = document.createElement('div');
    row.className = 'list-group-item d-flex justify-content-between align-items-center';
    row.innerHTML = `<div><div class="fw-semibold">${season.name}</div><div class="small-label">ID: ${season.id}</div></div><span class="badge text-bg-secondary">${season.status}</span>`;
    seasonList.appendChild(row);
  });
}

function renderContestants(contestants) {
  contestantList.innerHTML = '';
  if (!contestants.length) { contestantList.innerHTML = '<div class="text-muted">No hay contestants.</div>'; return; }
  contestants.forEach((contestant) => {
    const row = document.createElement('div');
    row.className = 'list-group-item d-flex justify-content-between align-items-center';
    row.innerHTML = `<div><div class="fw-semibold">${contestant.name}</div><div class="small-label">ID: ${contestant.id} · Season ID: ${contestant.seasonId}</div></div><span class="badge text-bg-secondary">${contestant.status}</span>`;
    contestantList.appendChild(row);
  });
}

async function loadSeasons() {
  const seasons = await apiFetch('/api/v1/admin/seasons');
  renderSeasons(seasons);
}

async function loadContestants() {
  const contestants = await apiFetch('/api/v1/admin/contestants');
  renderContestants(contestants);
}

async function createSeason(name) {
  const season = await apiFetch('/api/v1/admin/seasons', { method: 'POST', body: JSON.stringify({ name }) });
  showAlert(`Season creada con ID ${season.id}.`, 'success');
  return season;
}

async function createContestant(seasonId, name) {
  const contestant = await apiFetch('/api/v1/admin/contestants', { method: 'POST', body: JSON.stringify({ seasonId, name }) });
  showAlert(`Contestant creado con ID ${contestant.id}.`, 'success');
  return contestant;
}

// Event listeners
document.getElementById('loginBtn').addEventListener('click', async () => {
  try {
    hideAlert();
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    await login(username, password);
  } catch (error) { showAlert(error.message, 'danger'); }
});

document.getElementById('logoutBtn').addEventListener('click', () => {
  logout();
  showAlert('Sesión cerrada.', 'secondary');
});

document.getElementById('refreshPublicBtn').addEventListener('click', loadCurrentRound);
document.getElementById('voteForm').addEventListener('submit', async (e) => { e.preventDefault(); try { hideAlert(); await castVote(Number(voteRoundId.value), Number(voteContestantId.value)); } catch (error) { showAlert(error.message, 'danger'); } });
document.getElementById('resultsForm').addEventListener('submit', async (e) => { e.preventDefault(); try { hideAlert(); await loadResults(Number(resultsRoundId.value)); } catch (error) { resultsState.textContent = 'No se pudieron cargar los resultados.'; resultsList.innerHTML = ''; showAlert(error.message, 'danger'); } });
document.getElementById('createRoundForm').addEventListener('submit', async (e) => { e.preventDefault(); try { hideAlert(); await createRound({ seasonId: Number(seasonId.value), name: roundName.value, startsAt: toIsoFromLocal(startsAt.value), endsAt: toIsoFromLocal(endsAt.value), ruleType: ruleType.value }); } catch (error) { showAlert(error.message, 'danger'); } });
document.getElementById('nominateForm').addEventListener('submit', async (e) => { e.preventDefault(); try { hideAlert(); const ids = contestantIds.value.split(',').map(v => Number(v.trim())).filter(v => !Number.isNaN(v)); await nominate(Number(nominateRoundId.value), ids); } catch (error) { showAlert(error.message, 'danger'); } });
document.getElementById('openRoundBtn').addEventListener('click', async () => { try { hideAlert(); await openRound(Number(actionRoundId.value)); await loadCurrentRound(); } catch (error) { showAlert(error.message, 'danger'); } });
document.getElementById('closeRoundBtn').addEventListener('click', async () => { try { hideAlert(); await closeRound(Number(actionRoundId.value)); await loadCurrentRound(); } catch (error) { showAlert(error.message, 'danger'); } });
document.getElementById('createSeasonForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  try {
    hideAlert();
    const name = document.getElementById('seasonName').value;
    const season = await createSeason(name);
    document.getElementById('seasonId').value = season.id;
    document.getElementById('contestantSeasonId').value = season.id;
    await loadSeasons();
  } catch (error) { showAlert(error.message, 'danger'); }
});
document.getElementById('createContestantForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  try {
    hideAlert();
    const seasonId = Number(document.getElementById('contestantSeasonId').value);
    const name = document.getElementById('contestantName').value;
    await createContestant(seasonId, name);
    await loadContestants();
  } catch (error) { showAlert(error.message, 'danger'); }
});

(function initDefaultDates() {
  const now = new Date();
  const plusOneHour = new Date(now.getTime() + 3600000);
  const plusOneDay = new Date(now.getTime() + 86400000);
  startsAt.value = plusOneHour.toISOString().slice(0, 16);
  endsAt.value = plusOneDay.toISOString().slice(0, 16);
})();

// Init
(async function init() {
  await loadHealth();
  await loadCurrentRound();
  updateAdminUI(); // Muestra login por defecto en Admin
})();