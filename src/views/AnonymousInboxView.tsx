import { useState } from "react"

import { Button } from "@/components/Button"

import { Icon } from "@/components/Icon"

import { anonymousReports, type AnonymousReport } from "@/data/dashboard"

export default function AnonymousInboxView() {
  const [selectedReport, setSelectedReport] = useState<AnonymousReport>(
    anonymousReports[0],
  )

  const [reportFilter, setReportFilter] = useState("Todos")

  const [reportQuery, setReportQuery] = useState("")

  const visibleReports = anonymousReports.filter((report) => {
    const matchesFilter =
      reportFilter === "Todos" || report.status === reportFilter

    const matchesQuery = `${report.title} ${report.category} ${report.id}`
      .toLowerCase()
      .includes(reportQuery.toLowerCase())

    return matchesFilter && matchesQuery
  })

  return (
    <>
      <section className="inbox-page-heading">
        <div>
          <span className="eyebrow">CANAL SEGURO • ACESSO EXCLUSIVO DO RH</span>
          <h1>Caixa de relatos anônimos</h1>
          <p>
            Receba, analise e encaminhe manifestações com confidencialidade.
          </p>
        </div>
        <div className="inbox-security">
          <Icon name="lock" size={16} />
          <span>
            Ambiente protegido<strong>Identidade não coletada</strong>
          </span>
        </div>
      </section>

      <section className="inbox-metrics">
        <article>
          <span className="inbox-metric-icon red">
            <Icon name="inbox" size={18} />
          </span>
          <div>
            <small>Novos relatos</small>
            <strong>3</strong>
            <p>aguardando triagem</p>
          </div>
        </article>
        <article>
          <span className="inbox-metric-icon yellow">
            <Icon name="clock" size={18} />
          </span>
          <div>
            <small>Em análise</small>
            <strong>8</strong>
            <p>2 próximos do prazo</p>
          </div>
        </article>
        <article>
          <span className="inbox-metric-icon green">
            <Icon name="check" size={18} />
          </span>
          <div>
            <small>Resolvidos no ano</small>
            <strong>21</strong>
            <p>tempo médio de 4 dias</p>
          </div>
        </article>
        <article className="confidentiality-card">
          <Icon name="lock" size={20} />
          <div>
            <strong>100% confidencial</strong>
            <p>Os dados do remetente não são armazenados.</p>
          </div>
        </article>
      </section>

      <section className="inbox-workspace">
        <article className="panel report-queue">
          <div className="report-queue-head">
            <div>
              <h2>Relatos recebidos</h2>
              <p>{visibleReports.length} registros exibidos</p>
            </div>
            <div className="employee-search report-search">
              <Icon name="search" size={16} />
              <input
                value={reportQuery}
                onChange={(event) => setReportQuery(event.target.value)}
                placeholder="Buscar protocolo..."
                aria-label="Buscar relato"
              />
            </div>
          </div>
          <div className="report-filters">
            {["Todos", "Novo", "Em análise"].map((item) => (
              <button
                className={reportFilter === item ? "selected" : ""}
                key={item}
                onClick={() => setReportFilter(item)}
              >
                {item}
                {item === "Novo" && <span>3</span>}
              </button>
            ))}
          </div>
          <div className="report-queue-list">
            {visibleReports.map((report) => (
              <button
                className={`report-queue-row ${
                  selectedReport.id === report.id ? "selected" : ""
                }`}
                key={report.id}
                onClick={() => setSelectedReport(report)}
              >
                <span
                  className={`queue-priority priority-${report.severity.toLowerCase()}`}
                />
                <span className="queue-report-copy">
                  <span className="queue-meta">
                    <b>{report.id}</b>
                    <small>{report.time}</small>
                  </span>
                  <strong>{report.title}</strong>
                  <p>{report.preview}</p>
                  <span className="queue-tags">
                    <i
                      className={`severity severity-${report.severity.toLowerCase()}`}
                    >
                      {report.severity}
                    </i>
                    <i>{report.category}</i>
                  </span>
                </span>
              </button>
            ))}
          </div>
        </article>

        <article className="panel report-reader">
          <div className="reader-head">
            <div>
              <span className="eyebrow">PROTOCOLO {selectedReport.id}</span>
              <h2>{selectedReport.title}</h2>
              <p>Recebido em {selectedReport.date}</p>
            </div>
            <span
              className={`severity severity-${selectedReport.severity.toLowerCase()}`}
            >
              {selectedReport.severity}
            </span>
          </div>
          <div className="reader-context">
            <div>
              <Icon name="file" size={16} />
              <span>
                Categoria<strong>{selectedReport.category}</strong>
              </span>
            </div>
            <div>
              <Icon name="briefcase" size={16} />
              <span>
                Contexto informado<strong>{selectedReport.context}</strong>
              </span>
            </div>
            <div>
              <Icon name="clock" size={16} />
              <span>
                Status<strong>{selectedReport.status}</strong>
              </span>
            </div>
          </div>
          <div className="report-content">
            <span>RELATO DO FUNCIONÁRIO</span>
            <p>{selectedReport.content}</p>
            <div>
              <Icon name="lock" size={14} /> Mensagem recebida de forma anônima.
              Não é possível identificar ou responder diretamente ao remetente.
            </div>
          </div>
          <div className="case-timeline">
            <h3>Histórico do caso</h3>
            <div className="timeline-item current">
              <span />
              <div>
                <strong>Relato recebido</strong>
                <p>
                  Classificado automaticamente como{" "}
                  {selectedReport.severity.toLowerCase()}
                </p>
                <small>{selectedReport.date}</small>
              </div>
            </div>
            {selectedReport.status === "Em análise" && (
              <div className="timeline-item">
                <span />
                <div>
                  <strong>Análise iniciada pelo RH</strong>
                  <p>Fernanda Rocha assumiu o acompanhamento</p>
                  <small>Atualização interna</small>
                </div>
              </div>
            )}
          </div>
          <div className="reader-actions">
            <Button variant="secondary">
              <Icon name="file" size={16} /> Adicionar nota
            </Button>
            <Button>
              <Icon name="arrow" size={16} /> Iniciar acompanhamento
            </Button>
          </div>
        </article>

        <aside className="panel case-management">
          <div className="management-head">
            <h2>Gestão do caso</h2>
            <p>Informações internas do RH</p>
          </div>
          <div className="management-field">
            <span>Responsável</span>
            <div className="assignee">
              <span className="avatar avatar-lime">FR</span>
              <div>
                <strong>Fernanda Rocha</strong>
                <small>Gestora de RH</small>
              </div>
            </div>
          </div>
          <div className="management-field">
            <span>Prazo de retorno interno</span>
            <strong className="deadline">
              <Icon name="calendar" size={15} /> 28 de maio
            </strong>
          </div>
          <div className="management-field">
            <span>Direcionamento</span>
            <strong>Recursos Humanos</strong>
            <small>Restrito à equipe responsável</small>
          </div>
          <div className="management-note">
            <Icon name="warning" size={16} />
            <p>
              Casos críticos devem receber uma primeira análise em até 24 horas.
            </p>
          </div>
          <Button variant="secondary" className="management-button">
            <Icon name="check" size={16} /> Marcar como resolvido
          </Button>
        </aside>
      </section>
    </>
  )
}
