import { useState } from "react"

import { Button } from "@/components/Button"

import { Icon } from "@/components/Icon"

import { employees, monthlyStatus } from "@/data/dashboard"

export default function ReportsView() {
  const [scope, setScope] = useState<"Geral" | "Funcionário">("Geral")

  const [audience, setAudience] = useState("Gestor")

  const [employeeName, setEmployeeName] = useState(employees[0].name)

  const [generated, setGenerated] = useState(false)

  const generateReport = () => {
    setGenerated(false)

    window.setTimeout(() => setGenerated(true), 450)
  }

  return (
    <>
      <section className="reports-heading">
        <div>
          <span className="eyebrow">ANÁLISE DE PESSOAS</span>
          <h1>Relatórios</h1>
          <p>
            Visualize a evolução da equipe e gere análises executivas com
            inteligência artificial.
          </p>
        </div>
        <Button variant="secondary">
          <Icon name="download" size={16} /> Exportar dados
        </Button>
      </section>

      <section className="reports-metrics">
        <article>
          <span>Motivação média</span>
          <strong>
            72<small>/100</small>
          </strong>
          <p className="report-positive">
            <Icon name="trend" size={13} /> +6% no mês
          </p>
        </article>
        <article>
          <span>Funcionários estáveis</span>
          <strong>
            76<small>%</small>
          </strong>
          <p>29 de 38 acompanhados</p>
        </article>
        <article>
          <span>Em atenção</span>
          <strong>7</strong>
          <p className="report-warning">
            <Icon name="warning" size={13} /> 2 novos casos
          </p>
        </article>
        <article>
          <span>Presença média</span>
          <strong>
            94<small>%</small>
          </strong>
          <p className="report-positive">
            <Icon name="trend" size={13} /> +2% desde abril
          </p>
        </article>
      </section>

      <section className="reports-grid">
        <article className="panel monthly-status-panel">
          <div className="report-panel-head">
            <div>
              <h2>Status dos funcionários</h2>
              <p>Distribuição mensal dos funcionários nos primeiros 90 dias</p>
            </div>
            <select aria-label="Período do gráfico" defaultValue="6">
              <option value="6">Últimos 6 meses</option>
              <option value="12">Últimos 12 meses</option>
            </select>
          </div>
          <div className="chart-legend">
            <span>
              <i className="legend-stable" /> Estáveis
            </span>
            <span>
              <i className="legend-attention" /> Em atenção
            </span>
            <span>
              <i className="legend-risk" /> Risco alto
            </span>
          </div>
          <div className="monthly-chart">
            <div className="chart-axis">
              <span>100%</span>
              <span>75%</span>
              <span>50%</span>
              <span>25%</span>
              <span>0</span>
            </div>
            <div className="monthly-columns">
              {monthlyStatus.map((item) => (
                <div className="monthly-column" key={item.month}>
                  <div
                    className="stacked-bar"
                    aria-label={`${item.month}: ${item.stable}% estáveis, ${item.attention}% em atenção e ${item.risk}% em risco`}
                  >
                    <span
                      className="stack-risk"
                      style={{ height: `${item.risk}%` }}
                    />
                    <span
                      className="stack-attention"
                      style={{ height: `${item.attention}%` }}
                    />
                    <span
                      className="stack-stable"
                      style={{ height: `${item.stable}%` }}
                    />
                  </div>
                  <strong>{item.month}</strong>
                </div>
              ))}
            </div>
          </div>
          <div className="chart-insight">
            <span className="insight-icon">
              <Icon name="trend" size={17} />
            </span>
            <div>
              <strong>Melhora consistente nos últimos 3 meses</strong>
              <p>
                A proporção de funcionários estáveis cresceu 17 pontos desde
                fevereiro.
              </p>
            </div>
          </div>
        </article>

        <article className="panel motivation-panel">
          <div className="report-panel-head">
            <div>
              <h2>Motivação e presença</h2>
              <p>Evolução dos principais indicadores</p>
            </div>
          </div>
          <div className="line-legend">
            <span>
              <i /> Motivação
            </span>
            <span>
              <i /> Presença
            </span>
          </div>
          <div className="line-chart">
            <div className="line-grid">
              <i />
              <i />
              <i />
              <i />
            </div>
            <svg
              viewBox="0 0 500 180"
              preserveAspectRatio="none"
              aria-label="Gráfico de motivação e presença"
            >
              <path
                className="presence-area"
                d="M10 49 L106 42 L202 47 L298 35 L394 31 L490 27 L490 170 L10 170 Z"
              />
              <path
                className="presence-line"
                d="M10 49 L106 42 L202 47 L298 35 L394 31 L490 27"
              />
              <path
                className="motivation-line"
                d="M10 118 L106 105 L202 112 L298 86 L394 74 L490 60"
              />
              {[
                ["10", "118"],
                ["106", "105"],
                ["202", "112"],
                ["298", "86"],
                ["394", "74"],
                ["490", "60"],
              ].map(([cx, cy]) => (
                <circle
                  key={cx}
                  className="motivation-dot"
                  cx={cx}
                  cy={cy}
                  r="5"
                />
              ))}
            </svg>
            <div className="line-labels">
              {monthlyStatus.map((item) => (
                <span key={item.month}>{item.month}</span>
              ))}
            </div>
          </div>
          <div className="motivation-summary">
            <div>
              <span>Motivação atual</span>
              <strong>72</strong>
            </div>
            <div>
              <span>Presença atual</span>
              <strong>94%</strong>
            </div>
          </div>
        </article>
      </section>

      <section className="panel ai-report-section">
        <div className="ai-report-intro">
          <span className="ai-mark">
            <Icon name="sparkles" size={22} />
          </span>
          <div>
            <span className="eyebrow">ASSISTENTE DE RELATÓRIOS</span>
            <h2>Crie uma análise com IA</h2>
            <p>
              Transforme os indicadores da plataforma em um relatório claro e
              pronto para compartilhar.
            </p>
          </div>
          <span className="ai-secure">
            <Icon name="lock" size={13} /> Dados protegidos
          </span>
        </div>
        <div className="ai-report-body">
          <div className="ai-config">
            <div className="config-block">
              <label>Tipo de relatório</label>
              <div className="scope-options">
                {(["Geral", "Funcionário"] as const).map((item) => (
                  <button
                    key={item}
                    className={scope === item ? "selected" : ""}
                    onClick={() => {
                      setScope(item)
                      setGenerated(false)
                    }}
                  >
                    <Icon
                      name={item === "Geral" ? "people" : "people"}
                      size={17}
                    />
                    <span>
                      <strong>
                        {item === "Geral"
                          ? "Visão geral do mês"
                          : "Funcionário específico"}
                      </strong>
                      <small>
                        {item === "Geral"
                          ? "Resumo de toda a equipe"
                          : "Análise individual detalhada"}
                      </small>
                    </span>
                  </button>
                ))}
              </div>
            </div>
            {scope === "Funcionário" && (
              <label className="config-select">
                Funcionário
                <select
                  value={employeeName}
                  onChange={(event) => setEmployeeName(event.target.value)}
                >
                  {employees.map((employee) => (
                    <option key={employee.id}>{employee.name}</option>
                  ))}
                </select>
              </label>
            )}
            <div className="config-row">
              <label className="config-select">
                Destinatário
                <select
                  value={audience}
                  onChange={(event) => setAudience(event.target.value)}
                >
                  <option>Gestor</option>
                  <option>RH</option>
                  <option>Gestor e RH</option>
                </select>
              </label>
              <label className="config-select">
                Período
                <select defaultValue="Maio de 2025">
                  <option>Maio de 2025</option>
                  <option>Abril de 2025</option>
                  <option>1º trimestre de 2025</option>
                </select>
              </label>
            </div>
            <label className="config-notes">
              Orientações adicionais
              <textarea
                rows={3}
                placeholder="Ex.: destaque riscos de saída e sugira ações para o próximo mês..."
              />
            </label>
            <Button onClick={generateReport} className="generate-button">
              <Icon name="sparkles" size={17} /> Gerar relatório com IA
            </Button>
          </div>

          <div className={`ai-preview ${generated ? "generated" : ""}`}>
            {generated ? (
              <>
                <div className="preview-head">
                  <div>
                    <span>RELATÓRIO GERADO</span>
                    <strong>
                      {scope === "Geral"
                        ? "Panorama mensal da equipe"
                        : `Análise de ${employeeName}`}
                    </strong>
                    <small>Preparado para: {audience}</small>
                  </div>
                  <Button variant="secondary">
                    <Icon name="download" size={15} /> Baixar PDF
                  </Button>
                </div>
                <div className="preview-content">
                  <h3>Resumo executivo</h3>
                  <p>
                    {scope === "Geral"
                      ? "Em maio, a equipe apresentou evolução positiva nos indicadores de estabilidade, motivação e presença. 76% dos funcionários acompanhados estão em situação estável, um avanço de 8 pontos em relação ao mês anterior."
                      : `${employeeName} apresenta indicadores que merecem acompanhamento durante o período de experiência. A análise combina presença, motivação, volume de dúvidas e relatos associados ao período.`}
                  </p>
                  <h3>Pontos de atenção</h3>
                  <ul>
                    <li>7 funcionários permanecem classificados em atenção.</li>
                    <li>
                      O índice de dúvidas é maior entre colaboradores com até 45
                      dias.
                    </li>
                    <li>
                      Dois casos precisam de acompanhamento do RH nesta semana.
                    </li>
                  </ul>
                  <div className="ai-recommendation">
                    <Icon name="sparkles" size={15} />
                    <p>
                      <strong>Recomendação da IA</strong>Realizar check-ins
                      individuais e reforçar o treinamento inicial nas unidades
                      com maior volume de dúvidas.
                    </p>
                  </div>
                </div>
              </>
            ) : (
              <div className="empty-preview">
                <span>
                  <Icon name="sparkles" size={27} />
                </span>
                <strong>Seu relatório aparecerá aqui</strong>
                <p>
                  Configure as opções ao lado e use a IA para gerar uma análise
                  estruturada em poucos segundos.
                </p>
              </div>
            )}
          </div>
        </div>
      </section>
    </>
  )
}
