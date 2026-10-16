import { useMemo, useState, type FormEvent } from "react"

import { Button } from "@/components/Button"

import { Icon } from "@/components/Icon"

import EmployeesView from "@/views/EmployeesView"

import WellbeingView from "@/views/WellbeingView"

import AnonymousInboxView from "@/views/AnonymousInboxView"

import ReportsView from "@/views/ReportsView"

import { cases, navItems, riskPeople } from "@/data/dashboard"

import "@/styles/index.css"

export default function App() {
  const [active, setActive] = useState("Funcionários")

  const [filter, setFilter] = useState("Todos")

  const [resolved, setResolved] = useState<number[]>([4])

  const [modal, setModal] = useState<"new-report" | null>(null)

  const [sent, setSent] = useState(false)

  const visibleCases = useMemo(
    () => cases.filter((item) => filter === "Todos" || item.route === filter),
    [filter],
  )

  const toggleSolved = (id: number) => {
    setResolved((current) =>
      current.includes(id)
        ? current.filter((item) => item !== id)
        : [...current, id],
    )
  }

  const sendReport = (event: FormEvent) => {
    event.preventDefault()

    setSent(true)

    window.setTimeout(() => {
      setModal(null)

      setSent(false)
    }, 1300)
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">
            <img src="/brand/mcdonalds-logo.svg" alt="" />
          </div>
          <div>
            <strong>Mc Lanches</strong>
            <small>Pessoas & Cultura</small>
          </div>
        </div>

        <nav className="nav-list" aria-label="Navegação principal">
          <span className="nav-label">MENU</span>
          {navItems.map((item) => (
            <button
              key={item.label}
              className={`nav-item ${active === item.label ? "active" : ""}`}
              onClick={() => setActive(item.label)}
            >
              <Icon name={item.icon} size={19} />
              <span>{item.label}</span>
              {item.label === "Caixa de relatos" && <em>3</em>}
            </button>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <button
            className="nav-item"
            onClick={() => setActive("Configurações")}
          >
            <Icon name="settings" size={19} />
            <span>Configurações</span>
          </button>
          <div className="profile">
            <div className="avatar avatar-lime">FR</div>
            <div>
              <strong>Fernanda Rocha</strong>
              <small>Gestora de RH</small>
            </div>
            <span className="more">•••</span>
          </div>
        </div>
      </aside>

      <main className="main">
        <header className="topbar">
          <div className="mobile-brand">
            <div className="brand-mark">
              <img src="/brand/mcdonalds-logo.svg" alt="" />
            </div>
            <strong>Mc Lanches</strong>
          </div>
          <div className="search">
            <Icon name="search" size={19} />
            <input
              aria-label="Buscar"
              placeholder="Buscar funcionário ou relato..."
            />
            <kbd>⌘ K</kbd>
          </div>
          <button className="icon-button" aria-label="Notificações">
            <Icon name="bell" />
            <span />
          </button>
          <Button onClick={() => setModal("new-report")}>
            <Icon name="plus" size={17} /> Novo relato
          </Button>
        </header>

        <div className="content">
          {active === "Funcionários" ? (
            <EmployeesView />
          ) : active === "Bem-estar" ? (
            <WellbeingView />
          ) : active === "Caixa de relatos" ? (
            <AnonymousInboxView />
          ) : active === "Relatórios" ? (
            <ReportsView />
          ) : (
            <>
              <section className="welcome">
                <div>
                  <span className="eyebrow">SEGUNDA-FEIRA, 26 DE MAIO</span>
                  <h1>Bom dia, Fernanda.</h1>
                  <p>
                    Acompanhe o bem-estar da equipe e os casos que precisam da
                    sua atenção.
                  </p>
                </div>
                <div className="period-select">
                  <span>Período</span>
                  <select aria-label="Selecionar período" defaultValue="90">
                    <option value="90">Últimos 90 dias</option>
                    <option value="30">Últimos 30 dias</option>
                  </select>
                </div>
              </section>

              <section className="metrics-grid" aria-label="Resumo">
                <article className="metric-card metric-dark">
                  <div className="metric-head">
                    <span>Índice de motivação</span>
                    <div className="metric-icon">
                      <Icon name="heart" size={18} />
                    </div>
                  </div>
                  <div className="metric-value">
                    72<span>/100</span>
                  </div>
                  <div className="mini-progress">
                    <span style={{ width: "72%" }} />
                  </div>
                  <small>
                    <Icon name="trend" size={14} /> 6% acima do mês passado
                  </small>
                </article>
                <article className="metric-card">
                  <div className="metric-head">
                    <span>Casos em aberto</span>
                    <div className="metric-icon yellow">
                      <Icon name="inbox" size={18} />
                    </div>
                  </div>
                  <div className="metric-value">12</div>
                  <small className="muted">
                    <b>3 críticos</b> aguardando retorno
                  </small>
                </article>
                <article className="metric-card">
                  <div className="metric-head">
                    <span>Risco de saída</span>
                    <div className="metric-icon coral">
                      <Icon name="warning" size={18} />
                    </div>
                  </div>
                  <div className="metric-value">7</div>
                  <small className="muted">
                    <b>2 novos</b> nesta semana
                  </small>
                </article>
                <article className="metric-card">
                  <div className="metric-head">
                    <span>Taxa de resolução</span>
                    <div className="metric-icon mint">
                      <Icon name="check" size={18} />
                    </div>
                  </div>
                  <div className="metric-value">
                    84<span>%</span>
                  </div>
                  <small className="positive">
                    <Icon name="trend" size={14} /> 11% nos últimos 30 dias
                  </small>
                </article>
              </section>

              <section className="dashboard-grid">
                <article className="panel cases-panel">
                  <div className="panel-header">
                    <div>
                      <h2>Casos prioritários</h2>
                      <p>Ordenados por gravidade e urgência</p>
                    </div>
                    <Button variant="ghost">
                      Ver todos <Icon name="arrow" size={16} />
                    </Button>
                  </div>
                  <div className="filter-row">
                    {["Todos", "RH", "Gestor"].map((item) => (
                      <button
                        key={item}
                        className={filter === item ? "selected" : ""}
                        onClick={() => setFilter(item)}
                      >
                        {item}
                        {item === "Todos" && <span>12</span>}
                      </button>
                    ))}
                  </div>
                  <div className="case-list">
                    {visibleCases.map((item) => {
                      const isSolved = resolved.includes(item.id)

                      return (
                        <div
                          className={`case-row ${isSolved ? "solved" : ""}`}
                          key={item.id}
                        >
                          <div className={`avatar avatar-${item.color}`}>
                            {item.initials}
                          </div>
                          <div className="case-info">
                            <strong>{item.name}</strong>
                            <span>{item.issue}</span>
                          </div>
                          <div className="case-route">
                            <span
                              className={`severity severity-${item.severity.toLowerCase()}`}
                            >
                              {item.severity}
                            </span>
                            <small>
                              Direcionado: <b>{item.route}</b>
                            </small>
                          </div>
                          <div className="case-time">
                            <Icon name="clock" size={14} /> {item.time}
                          </div>
                          <button
                            className={`solve-button ${isSolved ? "done" : ""}`}
                            onClick={() => toggleSolved(item.id)}
                            aria-label={
                              isSolved
                                ? "Marcar como não resolvido"
                                : "Marcar como resolvido"
                            }
                          >
                            <Icon name="check" size={15} />{" "}
                            {isSolved ? "Resolvido" : "Resolver"}
                          </button>
                        </div>
                      )
                    })}
                  </div>
                </article>

                <article className="panel risk-panel">
                  <div className="panel-header">
                    <div>
                      <h2>Alerta de retenção</h2>
                      <p>Funcionários com maior chance de saída</p>
                    </div>
                    <div className="alert-icon">
                      <Icon name="warning" size={19} />
                    </div>
                  </div>
                  <div className="risk-chart">
                    <div className="chart-copy">
                      <strong>7 funcionários</strong>
                      <span>precisam de atenção</span>
                    </div>
                    <div
                      className="bars"
                      aria-label="Evolução do risco nos últimos seis meses"
                    >
                      {[34, 46, 39, 58, 52, 76].map((height, index) => (
                        <div className="bar-slot" key={index}>
                          <div
                            className={`bar ${index === 5 ? "current" : ""}`}
                            style={{ height: `${height}%` }}
                          />
                        </div>
                      ))}
                    </div>
                    <div className="chart-labels">
                      <span>Dez</span>
                      <span>Jan</span>
                      <span>Fev</span>
                      <span>Mar</span>
                      <span>Abr</span>
                      <span>Mai</span>
                    </div>
                  </div>
                  <div className="risk-list">
                    {riskPeople.map((person, index) => (
                      <div className="risk-person" key={person.name}>
                        <div
                          className={`avatar avatar-${["orange", "purple", "blue"][index]}`}
                        >
                          {person.initials}
                        </div>
                        <div>
                          <strong>{person.name}</strong>
                          <span>{person.role}</span>
                        </div>
                        <div className="risk-score">
                          <strong>{person.score}%</strong>
                          <span
                            className={person.level === "Alto" ? "high" : ""}
                          >
                            {person.level}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                  <Button variant="secondary" className="full-button">
                    Ver análise completa <Icon name="arrow" size={16} />
                  </Button>
                </article>
              </section>

              <section className="bottom-grid">
                <article className="insight-card">
                  <div className="insight-icon">
                    <Icon name="trend" />
                  </div>
                  <div>
                    <span className="eyebrow">INSIGHT DA SEMANA</span>
                    <h3>Faltas no primeiro mês aumentaram 18%</h3>
                    <p>
                      A unidade Centro concentra 62% dos casos. Considere
                      conversar com os gestores locais.
                    </p>
                  </div>
                  <Button variant="secondary">
                    Ver detalhes <Icon name="arrow" size={16} />
                  </Button>
                </article>
                <article className="anonymous-card">
                  <div className="anonymous-icon">
                    <Icon name="lock" />
                  </div>
                  <div>
                    <span>CANAL SEGURO</span>
                    <h3>Caixa de relatos anônimos</h3>
                    <p>
                      Denúncias de abuso chegam somente ao RH, sem
                      identificação.
                    </p>
                  </div>
                  <Button onClick={() => setActive("Caixa de relatos")}>
                    Ver relatos recebidos <span className="unread-dot">3</span>
                  </Button>
                </article>
              </section>
            </>
          )}
        </div>
      </main>

      {modal === "new-report" && (
        <div
          className="modal-backdrop"
          role="presentation"
          onMouseDown={() => setModal(null)}
        >
          <div
            className="modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="report-title"
            onMouseDown={(event) => event.stopPropagation()}
          >
            <button
              className="modal-close"
              onClick={() => setModal(null)}
              aria-label="Fechar"
            >
              <Icon name="close" />
            </button>
            <div className="modal-lock">
              <Icon name="lock" />
            </div>
            <span className="eyebrow">CANAL CONFIDENCIAL</span>
            <h2 id="report-title">Enviar relato anônimo</h2>
            <p>
              Sua identidade não será registrada. Este relato será encaminhado
              exclusivamente à equipe de RH.
            </p>
            <form onSubmit={sendReport}>
              <label>
                Tipo de relato
                <select required defaultValue="">
                  <option value="" disabled>
                    Selecione uma opção
                  </option>
                  <option>Assédio ou abuso</option>
                  <option>Humilhação</option>
                  <option>Conflito com a chefia</option>
                  <option>Outro</option>
                </select>
              </label>
              <label>
                Conte o que aconteceu
                <textarea
                  required
                  rows={5}
                  placeholder="Descreva a situação com o máximo de detalhes que se sentir confortável..."
                />
              </label>
              <div className="privacy-note">
                <Icon name="lock" size={16} /> Seus dados estão protegidos e o
                envio é 100% anônimo.
              </div>
              <Button type="submit" className="full-button">
                {sent ? (
                  <>
                    <Icon name="check" size={17} /> Relato enviado
                  </>
                ) : (
                  <>
                    Enviar de forma anônima <Icon name="arrow" size={17} />
                  </>
                )}
              </Button>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
