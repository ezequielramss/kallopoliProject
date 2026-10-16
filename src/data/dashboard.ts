import type { IconName } from "@/components/Icon"

const navItems: { label: string; icon: IconName }[] = [
  { label: "Visão geral", icon: "grid" },

  { label: "Funcionários", icon: "people" },

  { label: "Bem-estar", icon: "heart" },

  { label: "Caixa de relatos", icon: "inbox" },

  { label: "Relatórios", icon: "chart" },
]

const cases = [
  {
    id: 1,
    initials: "MS",
    name: "Marina Silva",
    issue: "Relato de humilhação",
    route: "RH",
    time: "Há 18 min",
    severity: "Crítico",
    color: "purple",
    solved: false,
  },

  {
    id: 2,
    initials: "RL",
    name: "Rafael Lima",
    issue: "Desmotivação e cansaço",
    route: "Gestor",
    time: "Há 1h",
    severity: "Atenção",
    color: "blue",
    solved: false,
  },

  {
    id: 3,
    initials: "CN",
    name: "Camila Nunes",
    issue: "Conflito com a chefia",
    route: "RH",
    time: "Há 3h",
    severity: "Alto",
    color: "orange",
    solved: false,
  },

  {
    id: 4,
    initials: "JF",
    name: "João Ferreira",
    issue: "Dúvidas sobre a função",
    route: "Gestor",
    time: "Ontem",
    severity: "Moderado",
    color: "green",
    solved: true,
  },
]

const riskPeople = [
  {
    initials: "BS",
    name: "Bruno Santos",
    role: "Atendente • 42 dias",
    score: 82,
    level: "Alto",
  },

  {
    initials: "AM",
    name: "Ana Martins",
    role: "Caixa • 68 dias",
    score: 64,
    level: "Médio",
  },

  {
    initials: "LC",
    name: "Lucas Costa",
    role: "Cozinha • 31 dias",
    score: 47,
    level: "Médio",
  },
]

const anonymousReports = [
  {
    id: "AN-1048",
    title: "Tratamento inadequado durante o turno",
    category: "Humilhação",
    time: "Há 18 min",
    date: "26 mai, 09:24",
    severity: "Crítico",
    status: "Novo",
    context: "Unidade Centro • Turno da noite",
    preview:
      "Gostaria de relatar uma situação que tem acontecido durante os fechamentos...",
    content:
      "Gostaria de relatar uma situação que tem acontecido durante os fechamentos. O responsável pelo turno costuma chamar a atenção das pessoas na frente de toda a equipe, usando palavras ofensivas e fazendo comparações. Isso já aconteceu comigo e com outros colegas. Tenho me sentido constrangido e com receio de voltar ao trabalho.",
  },

  {
    id: "AN-1047",
    title: "Conflito recorrente com a liderança",
    category: "Conflito com a chefia",
    time: "Hoje, 08:21",
    date: "26 mai, 08:21",
    severity: "Alto",
    status: "Em análise",
    context: "Unidade Centro • Turno da manhã",
    preview:
      "Nas últimas semanas, a forma como as orientações são passadas tem afetado...",
    content:
      "Nas últimas semanas, a forma como as orientações são passadas tem afetado o clima da equipe. Pedidos são feitos em tom agressivo e não existe espaço para tirar dúvidas. Gostaria que a situação fosse acompanhada pelo RH sem revelar quem fez este relato.",
  },

  {
    id: "AN-1046",
    title: "Comentário desconfortável no ambiente",
    category: "Conduta inadequada",
    time: "Ontem, 17:46",
    date: "25 mai, 17:46",
    severity: "Atenção",
    status: "Novo",
    context: "Unidade Jardins • Turno da tarde",
    preview:
      "Foi feito um comentário na frente da equipe que me deixou desconfortável...",
    content:
      "Foi feito um comentário na frente da equipe que me deixou desconfortável. Mesmo depois de eu demonstrar incômodo, a situação foi tratada como brincadeira. Peço que o RH converse com a liderança sobre respeito no ambiente de trabalho.",
  },

  {
    id: "AN-1045",
    title: "Escala alterada sem comunicação",
    category: "Condições de trabalho",
    time: "24 mai, 14:10",
    date: "24 mai, 14:10",
    severity: "Moderado",
    status: "Em análise",
    context: "Shopping Norte • Turno da tarde",
    preview: "Minha escala foi alterada mais de uma vez sem aviso prévio...",
    content:
      "Minha escala foi alterada mais de uma vez sem aviso prévio, o que tem dificultado minha organização pessoal. Já tentei conversar, mas ainda não tive uma resposta clara sobre como as mudanças devem ser comunicadas.",
  },
]

const employees = [
  {
    id: 1,
    initials: "BS",
    name: "Bruno Santos",
    role: "Atendente",
    unit: "Centro",
    tenure: "42 dias",
    reports: 3,
    presence: 88,
    absences: 3,
    justifications: 2,
    doubts: 78,
    risk: "Alto",
    motivation: 46,
    color: "orange",
    status: "Atenção",
  },

  {
    id: 2,
    initials: "AM",
    name: "Ana Martins",
    role: "Caixa",
    unit: "Centro",
    tenure: "68 dias",
    reports: 1,
    presence: 96,
    absences: 1,
    justifications: 1,
    doubts: 34,
    risk: "Médio",
    motivation: 68,
    color: "purple",
    status: "Atenção",
  },

  {
    id: 3,
    initials: "LC",
    name: "Lucas Costa",
    role: "Cozinha",
    unit: "Shopping Norte",
    tenure: "31 dias",
    reports: 0,
    presence: 100,
    absences: 0,
    justifications: 0,
    doubts: 42,
    risk: "Baixo",
    motivation: 81,
    color: "blue",
    status: "Estável",
  },

  {
    id: 4,
    initials: "MS",
    name: "Marina Silva",
    role: "Atendente",
    unit: "Centro",
    tenure: "87 dias",
    reports: 2,
    presence: 93,
    absences: 2,
    justifications: 2,
    doubts: 19,
    risk: "Baixo",
    motivation: 76,
    color: "green",
    status: "Estável",
  },

  {
    id: 5,
    initials: "JF",
    name: "João Ferreira",
    role: "Entregador",
    unit: "Jardins",
    tenure: "19 dias",
    reports: 1,
    presence: 97,
    absences: 1,
    justifications: 0,
    doubts: 61,
    risk: "Médio",
    motivation: 62,
    color: "lime",
    status: "Atenção",
  },
]

type Employee = typeof employees[number]

type AnonymousReport = typeof anonymousReports[number]

const monthlyStatus = [
  { month: "Dez", stable: 62, attention: 27, risk: 11 },

  { month: "Jan", stable: 66, attention: 25, risk: 9 },

  { month: "Fev", stable: 59, attention: 30, risk: 11 },

  { month: "Mar", stable: 68, attention: 23, risk: 9 },

  { month: "Abr", stable: 72, attention: 20, risk: 8 },

  { month: "Mai", stable: 76, attention: 17, risk: 7 },
]

const wellbeingPeople = [
  {
    ...employees[0],
    trend: -12,
    lastCheckin: "Hoje",
    survey: "Cansado",
    manager: "Paulo Mendes",
    nextAction: "Conversa com RH",
  },

  {
    ...employees[1],
    trend: -4,
    lastCheckin: "Ontem",
    survey: "Neutro",
    manager: "Carla Souza",
    nextAction: "Check-in em 3 dias",
  },

  {
    ...employees[2],
    trend: 8,
    lastCheckin: "24 mai",
    survey: "Motivado",
    manager: "Rogério Alves",
    nextAction: "Acompanhamento regular",
  },

  {
    ...employees[4],
    trend: -7,
    lastCheckin: "23 mai",
    survey: "Sobrecarregado",
    manager: "Carla Souza",
    nextAction: "Revisar treinamento",
  },
]

type WellbeingPerson = typeof wellbeingPeople[number]

const wellbeingHistory: Record<number, {
  type: IconName
  title: string
  description: string
  date: string
  tag: string
  tone: string
}[]> = {
  1: [
    {
      type: "file",
      title: "Relatório individual gerado",
      description: "IA identificou queda conjunta de presença e motivação.",
      date: "Hoje, 09:12",
      tag: "Relatório IA",
      tone: "red",
    },

    {
      type: "heart",
      title: "Questionário de bem-estar respondido",
      description:
        "Relatou cansaço frequente e dificuldade para manter o ritmo.",
      date: "25 mai, 18:40",
      tag: "Motivação 46",
      tone: "yellow",
    },

    {
      type: "calendar",
      title: "Falta justificada registrada",
      description: "Atestado analisado e aprovado pela equipe responsável.",
      date: "22 mai, 08:15",
      tag: "Justificada",
      tone: "green",
    },

    {
      type: "chat",
      title: "Aumento nas dúvidas pelo WhatsApp",
      description: "12 perguntas sobre rotina, escala e fechamento em 7 dias.",
      date: "19 mai, 16:30",
      tag: "12 dúvidas",
      tone: "blue",
    },

    {
      type: "check",
      title: "Check-in de 30 dias concluído",
      description:
        "Gestor registrou necessidade de reforço no treinamento inicial.",
      date: "14 mai, 14:00",
      tag: "Concluído",
      tone: "green",
    },
  ],

  2: [
    {
      type: "heart",
      title: "Questionário de bem-estar respondido",
      description: "Indicadores estáveis, com pequena queda na motivação.",
      date: "Ontem, 11:20",
      tag: "Motivação 68",
      tone: "yellow",
    },

    {
      type: "file",
      title: "Relatório de acompanhamento",
      description: "Relatório dos primeiros 60 dias enviado ao gestor.",
      date: "20 mai, 09:45",
      tag: "Compartilhado",
      tone: "blue",
    },

    {
      type: "check",
      title: "Check-in de 60 dias concluído",
      description: "Funcionária demonstrou boa adaptação à função.",
      date: "18 mai, 15:10",
      tag: "Concluído",
      tone: "green",
    },
  ],

  3: [
    {
      type: "heart",
      title: "Questionário de bem-estar respondido",
      description: "Relatou sentir-se motivado e apoiado pela equipe.",
      date: "24 mai, 17:30",
      tag: "Motivação 81",
      tone: "green",
    },

    {
      type: "check",
      title: "Check-in inicial concluído",
      description:
        "Sem pontos críticos identificados no primeiro acompanhamento.",
      date: "16 mai, 10:20",
      tag: "Concluído",
      tone: "green",
    },
  ],

  5: [
    {
      type: "chat",
      title: "Volume elevado de dúvidas",
      description:
        "Maioria das perguntas está relacionada ao processo de entregas.",
      date: "23 mai, 19:05",
      tag: "Índice 61%",
      tone: "yellow",
    },

    {
      type: "heart",
      title: "Primeiro questionário respondido",
      description: "Relatou sobrecarga durante os horários de pico.",
      date: "21 mai, 16:45",
      tag: "Motivação 62",
      tone: "yellow",
    },

    {
      type: "file",
      title: "Análise inicial criada",
      description: "Recomendado reforço prático com funcionário tutor.",
      date: "17 mai, 08:30",
      tag: "Relatório IA",
      tone: "blue",
    },
  ],
}

export {
  navItems,
  cases,
  riskPeople,
  anonymousReports,
  employees,
  monthlyStatus,
  wellbeingPeople,
  wellbeingHistory,
}

export type { Employee, AnonymousReport, WellbeingPerson }
