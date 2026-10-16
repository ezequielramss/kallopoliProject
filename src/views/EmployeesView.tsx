import { useState } from "react"

import { Button } from "@/components/Button"

import { Icon } from "@/components/Icon"

import { employees, type Employee } from "@/data/dashboard"

export default function EmployeesView() {
  const [query, setQuery] = useState("")

  const [status, setStatus] = useState("Todos")

  const [selected, setSelected] = useState<Employee>(employees[0])

  const filteredEmployees = employees.filter((employee) => {
    const matchesQuery = `${employee.name} ${employee.role} ${employee.unit}`
      .toLowerCase()
      .includes(query.toLowerCase())

    const matchesStatus = status === "Todos" || employee.status === status

    return matchesQuery && matchesStatus
  })

  return (
    <>
      <section className="employees-heading">
        <div>
          <span className="eyebrow">GESTÃO DE PESSOAS</span>
          <h1>Funcionários</h1>
          <p>
            Acompanhe os primeiros 90 dias e identifique quem precisa de
            atenção.
          </p>
        </div>
        <Button>
          <Icon name="plus" size={17} /> Adicionar funcionário
        </Button>
      </section>

      <section
        className="employee-summary"
        aria-label="Resumo dos funcionários"
      >
        <article className="summary-card">
          <div className="summary-icon dark">
            <Icon name="people" size={19} />
          </div>
          <div>
            <span>Em acompanhamento</span>
            <strong>38</strong>
            <small>nos primeiros 90 dias</small>
          </div>
        </article>
        <article className="summary-card">
          <div className="summary-icon red">
            <Icon name="warning" size={19} />
          </div>
          <div>
            <span>Precisam de atenção</span>
            <strong>7</strong>
            <small>2 novos nesta semana</small>
          </div>
        </article>
        <article className="summary-card">
          <div className="summary-icon yellow">
            <Icon name="file" size={19} />
          </div>
          <div>
            <span>Relatos no ano</span>
            <strong>24</strong>
            <small>18 resolvidos pelo time</small>
          </div>
        </article>
        <article className="summary-card">
          <div className="summary-icon green">
            <Icon name="check" size={19} />
          </div>
          <div>
            <span>Presença média</span>
            <strong>94%</strong>
            <small>+2% comparado a abril</small>
          </div>
        </article>
      </section>

      <section className="employees-layout">
        <article className="panel employees-panel">
          <div className="employees-toolbar">
            <div>
              <h2>Equipe em experiência</h2>
              <p>{filteredEmployees.length} funcionários encontrados</p>
            </div>
            <div className="employee-search">
              <Icon name="search" size={17} />
              <input
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                aria-label="Buscar na equipe"
                placeholder="Buscar funcionário..."
              />
            </div>
          </div>
          <div className="employee-filters">
            {["Todos", "Atenção", "Estável"].map((item) => (
              <button
                key={item}
                className={status === item ? "selected" : ""}
                onClick={() => setStatus(item)}
              >
                {item}
              </button>
            ))}
            <span>Atualizado hoje, 09:42</span>
          </div>
          <div className="employee-table-head">
            <span>Funcionário</span>
            <span>Tempo de empresa</span>
            <span>Presença</span>
            <span>Dúvidas</span>
            <span>Relatos</span>
            <span>Risco</span>
            <span />
          </div>
          <div className="employee-list">
            {filteredEmployees.map((employee) => (
              <button
                className={`employee-row ${
                  selected.id === employee.id ? "selected" : ""
                }`}
                key={employee.id}
                onClick={() => setSelected(employee)}
              >
                <span className="employee-person">
                  <span className={`avatar avatar-${employee.color}`}>
                    {employee.initials}
                  </span>
                  <span>
                    <strong>{employee.name}</strong>
                    <small>
                      {employee.role} • {employee.unit}
                    </small>
                  </span>
                </span>
                <span className="employee-tenure">
                  <Icon name="briefcase" size={14} /> {employee.tenure}
                </span>
                <span className="attendance-cell">
                  <strong>{employee.presence}%</strong>
                  <span className="table-progress">
                    <i style={{ width: `${employee.presence}%` }} />
                  </span>
                </span>
                <span
                  className={`doubt-index ${
                    employee.doubts >= 60 ? "high" : ""
                  }`}
                >
                  <Icon name="chat" size={14} /> {employee.doubts}%
                </span>
                <span className="reports-count">{employee.reports}</span>
                <span
                  className={`risk-badge risk-${employee.risk.toLowerCase()}`}
                >
                  {employee.risk}
                </span>
                <Icon name="chevron" size={17} />
              </button>
            ))}
          </div>
        </article>

        <aside className="panel employee-detail">
          <div className="detail-cover" />
          <div className="detail-profile">
            <div className={`avatar avatar-${selected.color}`}>
              {selected.initials}
            </div>
            <span className={`risk-badge risk-${selected.risk.toLowerCase()}`}>
              Risco {selected.risk.toLowerCase()}
            </span>
            <h2>{selected.name}</h2>
            <p>
              {selected.role} • Unidade {selected.unit}
            </p>
          </div>
          <div className="journey-block">
            <div className="journey-copy">
              <span>Jornada de experiência</span>
              <strong>{selected.tenure} de 90</strong>
            </div>
            <div className="journey-track">
              <span
                style={{
                  width: `${(Math.min(parseInt(selected.tenure), 90) / 90) * 100}%`,
                }}
              />
            </div>
            <small>Próximo check-in em 6 dias</small>
          </div>
          <div className="detail-stats">
            <div>
              <span>
                <Icon name="calendar" size={15} /> Presenças
              </span>
              <strong>{selected.presence}%</strong>
              <small>{selected.absences} faltas no período</small>
            </div>
            <div>
              <span>
                <Icon name="file" size={15} /> Justificativas
              </span>
              <strong>{selected.justifications}</strong>
              <small>
                {selected.justifications === selected.absences
                  ? "Todas analisadas"
                  : "1 aguardando análise"}
              </small>
            </div>
            <div>
              <span>
                <Icon name="chat" size={15} /> Índice de dúvidas
              </span>
              <strong>{selected.doubts}%</strong>
              <small>via chatbot do WhatsApp</small>
            </div>
            <div>
              <span>
                <Icon name="heart" size={15} /> Motivação
              </span>
              <strong>{selected.motivation}/100</strong>
              <small>último questionário</small>
            </div>
          </div>
          <div className="detail-alert">
            <Icon
              name={selected.risk === "Alto" ? "warning" : "trend"}
              size={18}
            />
            <div>
              <strong>
                {selected.risk === "Alto"
                  ? "Acompanhamento recomendado"
                  : "Evolução acompanhada"}
              </strong>
              <p>
                {selected.risk === "Alto"
                  ? "Dúvidas frequentes e queda de presença elevaram o risco de saída."
                  : "Os indicadores estão dentro do esperado para esta etapa."}
              </p>
            </div>
          </div>
          <Button variant="secondary" className="detail-button">
            Ver perfil completo <Icon name="arrow" size={16} />
          </Button>
        </aside>
      </section>
    </>
  )
}
