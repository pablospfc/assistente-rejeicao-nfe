import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { DiagnosticoPagina } from './diagnostico/diagnostico';
import { SessaoDiagnostico } from './diagnostico/sessao-diagnostico';
import { Envio } from './envio/envio';

export const exigeValidacao: CanActivateFn = () =>
  inject(SessaoDiagnostico).temValidacao() || inject(Router).createUrlTree(['/']);

export const routes: Routes = [
  { path: '', component: Envio, title: 'Assistente de Rejeições da NF-e' },
  { path: 'diagnostico', component: DiagnosticoPagina, canActivate: [exigeValidacao], title: 'Diagnóstico da NF-e' },
  { path: '**', redirectTo: '' },
];
