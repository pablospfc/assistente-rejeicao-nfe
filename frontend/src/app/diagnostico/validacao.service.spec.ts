import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { Diagnostico } from './modelos';
import { ValidacaoService } from './validacao.service';

describe('ValidacaoService', () => {
  let servico: ValidacaoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servico = TestBed.inject(ValidacaoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia o arquivo em multipart, no campo "arquivo", só para a API da aplicação', async () => {
    const arquivo = new File(['<NFe/>'], 'nota.xml', { type: 'application/xml' });
    const resposta = firstValueFrom(servico.validar(arquivo));

    const requisicao = http.expectOne('/api/validacoes');
    expect(requisicao.request.method).toBe('POST');
    const enviado = (requisicao.request.body as FormData).get('arquivo') as File;
    expect(enviado.name).toBe('nota.xml');
    expect(await enviado.text()).toBe('<NFe/>');
    const diagnostico: Diagnostico = { valida: true, regrasApesarDeFalhaNoSchema: false, camadas: {} };
    requisicao.flush(diagnostico);

    expect(await resposta).toEqual(diagnostico);
  });

  it('traduz o erro do backend usando o detail do ProblemDetail', async () => {
    const resposta = firstValueFrom(servico.validar(new File(['x'], 'nota.xml')));

    http.expectOne('/api/validacoes').flush(
      { detail: 'O arquivo excede o tamanho máximo de 1 MB.' },
      { status: 413, statusText: 'Payload Too Large' },
    );

    await expect(resposta).rejects.toThrow('O arquivo excede o tamanho máximo de 1 MB.');
  });

  it('avisa quando o servidor não responde', async () => {
    const resposta = firstValueFrom(servico.validar(new File(['x'], 'nota.xml')));

    http.expectOne('/api/validacoes').error(new ProgressEvent('error'), { status: 0 });

    await expect(resposta).rejects.toThrow('Não foi possível falar com o servidor de validação');
  });
});
