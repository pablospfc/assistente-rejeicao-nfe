import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { SessaoDiagnostico } from '../diagnostico/sessao-diagnostico';
import { ValidacaoService } from '../diagnostico/validacao.service';
import { AreaArquivo } from './area-arquivo';

/** Mesmo limite do backend (spring.servlet.multipart.max-file-size). */
export const TAMANHO_MAXIMO = 1024 * 1024;

/** Tela 1: envio do XML. */
@Component({
  selector: 'app-envio',
  imports: [AreaArquivo, MatCardModule, MatIconModule, MatProgressBarModule],
  templateUrl: './envio.html',
  styleUrl: './envio.scss',
})
export class Envio {
  private readonly validacao = inject(ValidacaoService);
  private readonly sessao = inject(SessaoDiagnostico);
  private readonly router = inject(Router);

  protected readonly validando = signal(false);
  protected readonly erro = signal<string | null>(null);

  async aoReceber(arquivo: File): Promise<void> {
    this.erro.set(null);
    const recusa = motivoDeRecusa(arquivo);
    if (recusa) {
      this.erro.set(recusa);
      return;
    }

    this.validando.set(true);
    try {
      const xml = await arquivo.text();
      const diagnostico = await firstValueFrom(this.validacao.validar(arquivo));
      this.sessao.registrar({ nomeArquivo: arquivo.name, xml, diagnostico });
      await this.router.navigate(['/diagnostico']);
    } catch (e) {
      this.erro.set(e instanceof Error ? e.message : 'Não foi possível validar o arquivo.');
    } finally {
      this.validando.set(false);
    }
  }
}

/** Verificações feitas no navegador antes de enviar; o backend repete as dele. */
export function motivoDeRecusa(arquivo: File): string | null {
  if (!arquivo.name.toLowerCase().endsWith('.xml')) {
    return 'Envie um arquivo .xml com a NF-e.';
  }
  if (arquivo.size > TAMANHO_MAXIMO) {
    return 'O arquivo excede o tamanho máximo de 1 MB.';
  }
  if (arquivo.size === 0) {
    return 'O arquivo está vazio.';
  }
  return null;
}
