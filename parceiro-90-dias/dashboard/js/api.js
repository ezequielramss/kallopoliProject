const USE_MOCK = false;
const MOCK_BASE = "mock";
const MOCK_PERFIL = "RH";

async function request(path, mockFile, options = {}) {
  const headers = { Accept: "application/json", ...options.headers };
  const method = (options.method || "GET").toUpperCase();
  if (!USE_MOCK && !["GET", "HEAD", "OPTIONS"].includes(method)) {
    let csrf = document.cookie.split("; ").find((cookie) => cookie.startsWith("XSRF-TOKEN="))?.split("=").slice(1).join("=");
    if (!csrf) {
      await fetch("/csrf", { credentials: "same-origin" });
      csrf = document.cookie.split("; ").find((cookie) => cookie.startsWith("XSRF-TOKEN="))?.split("=").slice(1).join("=");
    }
    if (csrf) headers["X-XSRF-TOKEN"] = decodeURIComponent(csrf);
  }
  const response = await fetch(USE_MOCK ? `${MOCK_BASE}/${mockFile}` : path, { ...options, method, headers, credentials: "same-origin" });
  if (response.status === 401) {
    const error = new Error("Sua sessão expirou. Entre novamente para continuar.");
    error.status = 401;
    window.location.assign("/login.html");
    throw error;
  }
  if (response.status === 403) throw new Error("Acesso restrito para este perfil.");
  if (!response.ok) throw new Error(`Não foi possível carregar os dados (${response.status}).`);
  return response.json();
}

function getKpis() { return request("/api/kpis", "kpis.json"); }
function getColaboradores() { return request("/api/colaboradores", "colaboradores.json"); }
async function getPerfil() {
  if (USE_MOCK) return MOCK_PERFIL;
  const result = await request("/api/perfil");
  return typeof result === "string" ? result : result.perfil;
}
async function getLinhaDoTempo(id) {
  if (!USE_MOCK) return request(`/api/colaboradores/${encodeURIComponent(id)}/linha-do-tempo`);
  const colaboradores = await getColaboradores();
  const person = colaboradores.find((item) => item.id === id);
  return { colaboradorId: id, eventos: person ? [{ tipo: "CONTATO", titulo: "Último contato", criadoEm: person.ultimoContato, resumo: "Contato de acompanhamento pelo WhatsApp." }] : [] };
}
function getAlertas(params = {}) {
  const query = new URLSearchParams();
  if (params.status) query.set("status", params.status);
  if (params.destino) query.set("destino", params.destino);
  return request(`/api/alertas${query.size ? `?${query}` : ""}`, "alertas.json");
}
function getRelatos(params = {}) {
  const query = new URLSearchParams();
  if (params.status) query.set("status", params.status);
  return request(`/api/relatos${query.size ? `?${query}` : ""}`, "relatos.json");
}
function getRelato(protocolo) { return request(`/api/relatos/${encodeURIComponent(protocolo)}`); }
function atualizarRelato(protocolo, dados) {
  return request(`/api/relatos/${encodeURIComponent(protocolo)}`, undefined, {
    method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify(dados)
  });
}
async function logout() {
  let csrf = document.cookie.split("; ").find((cookie) => cookie.startsWith("XSRF-TOKEN="))?.split("=").slice(1).join("=");
  if (!csrf) {
    await fetch("/csrf", { credentials: "same-origin" });
    csrf = document.cookie.split("; ").find((cookie) => cookie.startsWith("XSRF-TOKEN="))?.split("=").slice(1).join("=");
  }
  await fetch("/logout", { method: "POST", credentials: "same-origin", headers: { "X-XSRF-TOKEN": decodeURIComponent(csrf || "") } });
  window.location.replace("/login.html");
}
async function getBemestar(profile = MOCK_PERFIL) {
  if (USE_MOCK && profile === "GERENTE") return request("/api/bem-estar", "bemestar-gerente.json");
  const data = await request("/api/bem-estar", "bemestar.json");
  if (profile !== "GERENTE") return data;
  return {
    distribuicaoCategorias: data.distribuicaoCategorias,
    pessoas: data.pessoas.map(({ categoria, sugestao }) => ({ categoria, sugestao }))
  };
}
function getSerieMensal() { return request("/api/kpis/serie-mensal", "serie-mensal.json"); }
async function gerarRelatorio({ escopo, publico, colaboradorId }) {
  if (!USE_MOCK) return request("/api/relatorios/gerar", undefined, {
    method: "POST", headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ escopo, publico, colaboradorId })
  });
  await new Promise((resolve) => window.setTimeout(resolve, 800));
  return request("/api/relatorios/gerar", "relatorio-exemplo.json");
}
async function getDashboardData() {
  const [kpis, colaboradores, alertas] = await Promise.all([getKpis(), getColaboradores(), getAlertas()]);
  return { kpis, colaboradores, alertas };
}

export { USE_MOCK, getKpis, getColaboradores, getLinhaDoTempo, getPerfil, getAlertas, getRelatos, getRelato, atualizarRelato, logout, getBemestar, getSerieMensal, gerarRelatorio, getDashboardData };
