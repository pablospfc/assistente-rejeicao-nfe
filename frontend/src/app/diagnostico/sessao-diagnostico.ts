import { Injectable, computed, signal } from '@angular/core';
import { Diagnostico } from './modelos';

export interface Validacao {
  nomeArquivo: string;
  xml: string;
  diagnostico: Diagnostico;
}

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
