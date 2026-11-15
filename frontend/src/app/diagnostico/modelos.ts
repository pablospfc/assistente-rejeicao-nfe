/** Contrato da API POST /api/validacoes (backend: DiagnosticoResposta e OcorrenciaResposta). */

export type Camada = 'ESTRUTURA' | 'REGRAS_NEGOCIO';

export interface Ocorrencia {
  codigo: string;
  xpath: string | null;
  valorEncontrado: string | null;
  valorEsperado: string | null;
  titulo: string | null;
  campo: string | null;
  problema: string | null;
  correcao: string | null;
}

export interface Diagnostico {
  valida: boolean;
  regrasApesarDeFalhaNoSchema: boolean;
  camadas: Partial<Record<Camada, Ocorrencia[]>>;
}

/** Ordem e nomes das camadas, como no fluxo de validação. */
export const CAMADAS: { chave: Camada; nome: string; icone: string }[] = [
  { chave: 'ESTRUTURA', nome: 'Estrutura e assinatura', icone: 'schema' },
  { chave: 'REGRAS_NEGOCIO', nome: 'Regras de negócio', icone: 'rule' },
];
