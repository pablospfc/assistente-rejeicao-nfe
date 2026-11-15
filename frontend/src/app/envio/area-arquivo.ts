import { Component, input, output, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

/** Área de arrastar e soltar (o Material não tem um componente próprio), com seletor de arquivo. */
@Component({
  selector: 'app-area-arquivo',
  imports: [MatButtonModule, MatIconModule],
  templateUrl: './area-arquivo.html',
  styleUrl: './area-arquivo.scss',
})
export class AreaArquivo {
  readonly desabilitada = input(false);
  readonly arquivo = output<File>();

  protected readonly sobre = signal(false);

  protected aoArrastar(evento: DragEvent): void {
    evento.preventDefault();
    this.sobre.set(!this.desabilitada());
  }

  protected aoSoltar(evento: DragEvent): void {
    evento.preventDefault();
    this.sobre.set(false);
    const arquivo = evento.dataTransfer?.files.item(0);
    if (arquivo && !this.desabilitada()) {
      this.arquivo.emit(arquivo);
    }
  }

  protected aoEscolher(campo: HTMLInputElement): void {
    const arquivo = campo.files?.item(0);
    if (arquivo) {
      this.arquivo.emit(arquivo);
    }
    campo.value = '';
  }
}
