import { Injectable, computed, signal } from '@angular/core';
import { Diagnostico } from './modelos';

export interface Validacao {
  nomeArquivo: string;
  /** Conteúdo do XML enviado, só em memória, para o visualizador destacar o campo (D026). */
  xml: string;
  diagnostico: Diagnostico;
}

/**
 * Guarda a última validação só em memória (um signal). Nada vai para localStorage,
 * sessionStorage, IndexedDB ou cookies: ao recarregar a página, o XML some (D026, extensão da D003).
 */
@Injectable({ providedIn: 'root' })
export class SessaoDiagnostico {
  private readonly atual = signal<Validacao | null>(null);

  readonly validacao = this.atual.asReadonly();
  readonly temValidacao = computed(() => this.atual() !== null);

  registrar(validacao: Validacao): void {
    this.atual.set(validacao);
  }

  limpar(): void {
    this.atual.set(null);
  }
}
