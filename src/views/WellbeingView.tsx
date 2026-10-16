import { useState } from "react"

import { Button } from "@/components/Button"

import { Icon } from "@/components/Icon"

import {
  wellbeingHistory,
  wellbeingPeople,
  type WellbeingPerson,
} from "@/data/dashboard"

export default function WellbeingView() {
  const [selectedPerson, setSelectedPerson] = useState<WellbeingPerson>(
    wellbeingPeople[0],
  )

  const [wellbeingFilter, setWellbeingFilter] = useState("Todos")

  const history = wellbeingHistory[selectedPerson.id] || []

  const filteredPeople = wellbeingPeople.filter(
    (person) =>
      wellbeingFilter === "Todos" ||
      (wellbeingFilter === "Em risco"
        ? person.risk === "Alto"
        : person.risk !== "Alto"),
  )

  return (
    <>
      <section className="wellbeing-heading">
        <div>
          <span className="eyebrow">SAÚDE DA EQUIPE</span>
          <h1>Bem-estar</h1>
          <p>
            Entenda a experiência de cada funcionário e antecipe sinais de
            desengajamento.
          </p>
        </div>
        <Button variant="secondary">
          <Icon name="file" size={16} /> Relatório de bem-estar
        </Button>
      </section>

      <section className="wellbeing-overview">
        <article className="wellbeing-score-card">
          <div className="score-ring">
            <strong>72</strong>
            <span>/100</span>
          </div>
          <div>
            <span className="eyebrow">ÍNDICE GERAL</span>
            <h2>Bem-estar da equipe</h2>
            <p>O índice subiu 6 pontos desde abril.</p>
            <small>
              <Icon name="trend" size={13} /> Tendência positiva
            </small>
          </div>
        </article>
        <article className="wellbeing-stat">
          <span className="summary-icon red">
            <Icon name="warning" size={18} />
          </span>
          <div>
            <small>Requerem atenção</small>
            <strong>7</strong>
            <p>3 com queda de motivação</p>
          </div>
        </article>
        <article className="wellbeing-stat">
          <span className="summary-icon yellow">
            <Icon name="chat" size={18} />
          </span>
          <div>
            <small>Dúvidas no mês</small>
            <strong>146</strong>
            <p>+18% via WhatsApp</p>
          </div>
        </article>
        <article className="wellbeing-stat">
          <span className="summary-icon green">
            <Icon name="check" size={18} />
          </span>
          <div>
            <small>Check-ins concluídos</small>
            <strong>31</strong>
            <p>82% dentro do prazo</p>
          </div>
        </article>
      </section>

      <section className="wellbeing-workspace">
        <article className="panel wellbeing-list-panel">
          <div className="wellbeing-list-head">
            <div>
              <h2>Acompanhamento individual</h2>
              <p>Funcionários nos primeiros 90 dias</p>
            </div>
            <select aria-label="Ordenar funcionários">
              <option>Maior risco</option>
              <option>Menor motivação</option>
              <option>Mais recentes</option>
            </select>
          </div>
          <div className="wellbeing-filters">
            {["Todos", "Em risco", "Estáveis"].map((item) => (
              <button
                key={item}
                className={wellbeingFilter === item ? "selected" : ""}
                onClick={() => setWellbeingFilter(item)}
              >
                {item}
              </button>
            ))}
          </div>
          <div className="wellbeing-people">
            {filteredPeople.map((person) => (
              <button
                key={person.id}
                className={`wellbeing-person ${
                  selectedPerson.id === person.id ? "selected" : ""
                }`}
                onClick={() => setSelectedPerson(person)}
              >
                <span className={`avatar avatar-${person.color}`}>
                  {person.initials}
                </span>
                <span className="wellbeing-person-copy">
                  <strong>{person.name}</strong>
                  <small>
                    {person.role} • {person.tenure}
                  </small>
                  <span>
                    <i style={{ width: `${person.motivation}%` }} />
                  </span>
                </span>
                <span className="person-score">
                  <strong>{person.motivation}</strong>
                  <small className={person.trend < 0 ? "down" : "up"}>
                    {person.trend > 0 ? "+" : ""}
                    {person.trend} pts
                  </small>
                </span>
                <Icon name="chevron" size={16} />
              </button>
            ))}
          </div>
        </article>

        <article className="panel wellbeing-profile">
          <div className="wellbeing-profile-head">
            <div className={`avatar avatar-${selectedPerson.color}`}>
              {selectedPerson.initials}
            </div>
            <div>
              <h2>{selectedPerson.name}</h2>
              <p>
                {selectedPerson.role} • Unidade {selectedPerson.unit}
              </p>
            </div>
            <span
              className={`risk-badge risk-${selectedPerson.risk.toLowerCase()}`}
            >
              Risco {selectedPerson.risk.toLowerCase()}
            </span>
          </div>
          <div className="wellbeing-profile-metrics">
            <div>
              <span>Motivação</span>
              <strong>
                {selectedPerson.motivation}
                <small>/100</small>
              </strong>
              <p className={selectedPerson.trend < 0 ? "down" : "up"}>
                {selectedPerson.trend > 0 ? "+" : ""}
                {selectedPerson.trend} pontos
              </p>
            </div>
            <div>
              <span>Presença</span>
              <strong>
                {selectedPerson.presence}
                <small>%</small>
              </strong>
              <p>{selectedPerson.absences} faltas</p>
            </div>
            <div>
              <span>Índice de dúvidas</span>
              <strong>
                {selectedPerson.doubts}
                <small>%</small>
              </strong>
              <p>via WhatsApp</p>
            </div>
            <div>
              <span>Relatos no ano</span>
              <strong>{selectedPerson.reports}</strong>
              <p>
                {selectedPerson.reports ? "1 em análise" : "nenhum registro"}
              </p>
            </div>
          </div>
          <div className="wellbeing-details">
            <div>
              <Icon name="heart" size={16} />
              <span>
                Última percepção<strong>{selectedPerson.survey}</strong>
              </span>
            </div>
            <div>
              <Icon name="briefcase" size={16} />
              <span>
                Gestor responsável<strong>{selectedPerson.manager}</strong>
              </span>
            </div>
            <div>
              <Icon name="calendar" size={16} />
              <span>
                Último check-in<strong>{selectedPerson.lastCheckin}</strong>
              </span>
            </div>
            <div>
              <Icon name="arrow" size={16} />
              <span>
                Próxima ação<strong>{selectedPerson.nextAction}</strong>
              </span>
            </div>
          </div>
          <div className="wellbeing-alert">
            <span>
              <Icon name="sparkles" size={18} />
            </span>
            <div>
              <strong>Leitura inteligente do momento</strong>
              <p>
                {selectedPerson.risk === "Alto"
                  ? "A combinação de queda na motivação, faltas recentes e dúvidas frequentes indica necessidade de contato nas próximas 48 horas."
                  : "Os indicadores não apresentam sinais críticos. Mantenha o ciclo regular de check-ins e reconhecimento."}
              </p>
            </div>
            <Button variant="secondary">Gerar análise</Button>
          </div>
        </article>

        <article className="panel wellbeing-history">
          <div className="history-head">
            <div>
              <h2>Histórico de acompanhamento</h2>
              <p>Eventos, relatórios e interações recentes</p>
            </div>
            <Button variant="ghost">
              Ver tudo <Icon name="arrow" size={14} />
            </Button>
          </div>
          <div className="history-timeline">
            {history.map((event, index) => (
              <div
                className="history-event"
                key={`${event.title}-${event.date}`}
              >
                <span className={`history-event-icon event-${event.tone}`}>
                  <Icon name={event.type} size={15} />
                </span>
                <div>
                  <span className="history-event-meta">
                    <strong>{event.title}</strong>
                    <small>{event.date}</small>
                  </span>
                  <p>{event.description}</p>
                  <i>{event.tag}</i>
                </div>
                {index < history.length - 1 && (
                  <span className="history-line" />
                )}
              </div>
            ))}
          </div>
        </article>

        <aside className="panel wellbeing-actions">
          <div>
            <h2>Ações recomendadas</h2>
            <p>Baseadas nos dados dos últimos 30 dias</p>
          </div>
          <button>
            <span className="action-icon yellow">
              <Icon name="calendar" size={17} />
            </span>
            <span>
              <strong>Agendar check-in</strong>
              <small>Conversa individual de 15 min</small>
            </span>
            <Icon name="chevron" size={15} />
          </button>
          <button>
            <span className="action-icon green">
              <Icon name="file" size={17} />
            </span>
            <span>
              <strong>Gerar relatório</strong>
              <small>Análise individual completa</small>
            </span>
            <Icon name="chevron" size={15} />
          </button>
          <button>
            <span className="action-icon red">
              <Icon name="warning" size={17} />
            </span>
            <span>
              <strong>Encaminhar ao RH</strong>
              <small>Abrir acompanhamento interno</small>
            </span>
            <Icon name="chevron" size={15} />
          </button>
          <div className="next-checkin">
            <span>PRÓXIMO MARCO</span>
            <strong>Check-in de 60 dias</strong>
            <p>Em 6 dias • 1 de junho</p>
            <div>
              <i style={{ width: "67%" }} />
            </div>
          </div>
        </aside>
      </section>
    </>
  )
}
