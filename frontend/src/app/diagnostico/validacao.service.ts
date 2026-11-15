import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { Diagnostico } from './modelos';

@Injectable({ providedIn: 'root' })
export class ValidacaoService {
  private readonly http = inject(HttpClient);

  validar(arquivo: File): Observable<Diagnostico> {
    const formulario = new FormData();
    formulario.append('arquivo', arquivo, arquivo.name);
    return this.http
      .post<Diagnostico>('/api/validacoes', formulario)
      .pipe(catchError((erro: HttpErrorResponse) => throwError(() => new Error(mensagemDeErro(erro)))));
  }
}

export function mensagemDeErro(erro: HttpErrorResponse): string {
  if (erro.status === 0) {
    return 'Não foi possível falar com o servidor de validação. Verifique se o backend está rodando.';
  }
  const detalhe = (erro.error as { detail?: string } | null)?.detail;
  return detalhe ?? `O servidor recusou a validação (HTTP ${erro.status}).`;
}
