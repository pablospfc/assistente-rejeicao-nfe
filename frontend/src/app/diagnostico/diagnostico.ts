import { Component, computed, inject, linkedSignal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { VisualizadorXml } from '../visualizador/visualizador-xml';
import { CAMADAS, Ocorrencia } from './modelos';
import { SessaoDiagnostico } from './sessao-diagnostico';

@Component({
  selector: 'app-diagnostico',
  imports: [MatButtonModule, MatCardModule, MatChipsModule, MatExpansionModule, MatIconModule, VisualizadorXml],
  templateUrl: './diagnostico.html',
  styleUrl: './diagnostico.scss',
})
export class DiagnosticoPagina {
  private readonly sessao = inject(SessaoDiagnostico);
  private readonly router = inject(Router);

  protected readonly validacao = computed(() => this.sessao.validacao()!);

  protected readonly grupos = computed(() =>
    CAMADAS.map((camada) => ({
      ...camada,
      ocorrencias: this.validacao().diagnostico.camadas[camada.chave] ?? [],
    })),
  );

  protected readonly total = computed(() => this.grupos().reduce((soma, g) => soma + g.ocorrencias.length, 0));

  protected readonly selecionada = linkedSignal<Ocorrencia | null>(
    () => this.grupos().flatMap((g) => g.ocorrencias)[0] ?? null,
  );

  protected selecionar(ocorrencia: Ocorrencia): void {
    this.selecionada.set(ocorrencia);
  }

  protected async validarOutro(): Promise<void> {
    this.sessao.limpar();
    await this.router.navigate(['/']);
  }
}
