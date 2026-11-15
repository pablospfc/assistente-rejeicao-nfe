import { Component, ElementRef, afterRenderEffect, computed, input, viewChild } from '@angular/core';
import { Ocorrencia } from '../diagnostico/modelos';
import { LinhaXml, linhasDoXml } from './linhas-xml';
import { linhaDoErro, resolverXPath } from './xpath';

@Component({
  selector: 'app-visualizador-xml',
  templateUrl: './visualizador-xml.html',
  styleUrl: './visualizador-xml.scss',
})
export class VisualizadorXml {
  readonly xml = input.required<string>();
  readonly ocorrencia = input<Ocorrencia | null>(null);

  private readonly area = viewChild<ElementRef<HTMLElement>>('area');

  protected readonly documento = computed(() => {
    const documento = new DOMParser().parseFromString(this.xml(), 'application/xml');
    return documento.getElementsByTagName('parsererror').length > 0 ? null : documento;
  });

  protected readonly linhas = computed<LinhaXml[]>(() => {
    const documento = this.documento();
    return documento ? linhasDoXml(documento) : [];
  });

  protected readonly linhasTexto = computed(() => this.xml().split(/\r?\n/));

  protected readonly alvo = computed(() => {
    const documento = this.documento();
    return documento ? resolverXPath(documento, this.ocorrencia()?.xpath ?? null) : null;
  });

  protected readonly linhaDoErro = computed(() => linhaDoErro(this.ocorrencia()?.valorEncontrado ?? null));

  constructor() {
    afterRenderEffect(() => {
      this.alvo();
      this.linhaDoErro();
      const area = this.area()?.nativeElement;
      const destacada = area?.querySelector<HTMLElement>('.destacada');
      if (area && destacada) {
        area.scrollTo?.({ top: destacada.offsetTop - area.clientHeight / 2, behavior: 'smooth' });
      }
    });
  }

  protected destacada(linha: LinhaXml): boolean {
    const alvo = this.alvo();
    return alvo !== null && linha.tipo !== 'fecha' && linha.elemento === alvo.elemento;
  }

  protected atributoDestacado(linha: LinhaXml, nome: string): boolean {
    return this.destacada(linha) && this.alvo()?.atributo === nome;
  }
}
