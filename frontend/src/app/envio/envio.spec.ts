import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { Diagnostico } from '../diagnostico/modelos';
import { SessaoDiagnostico } from '../diagnostico/sessao-diagnostico';
import { ValidacaoService } from '../diagnostico/validacao.service';
import { Envio, TAMANHO_MAXIMO, motivoDeRecusa } from './envio';

const DIAGNOSTICO: Diagnostico = { valida: true, regrasApesarDeFalhaNoSchema: false, camadas: {} };

describe('motivoDeRecusa', () => {
  it('aceita só .xml de até 1 MB e não vazio', () => {
    expect(motivoDeRecusa(new File(['<NFe/>'], 'nota.xml'))).toBeNull();
    expect(motivoDeRecusa(new File(['<NFe/>'], 'NOTA.XML'))).toBeNull();
    expect(motivoDeRecusa(new File(['x'], 'nota.txt'))).toContain('.xml');
    expect(motivoDeRecusa(new File([new Uint8Array(TAMANHO_MAXIMO + 1)], 'nota.xml'))).toContain('1 MB');
    expect(motivoDeRecusa(new File([], 'nota.xml'))).toContain('vazio');
  });
});

describe('Envio', () => {
  const validar = vi.fn();

  beforeEach(async () => {
    validar.mockReset();
    await TestBed.configureTestingModule({
      imports: [Envio],
      providers: [provideRouter([]), { provide: ValidacaoService, useValue: { validar } }],
    }).compileComponents();
  });

  it('recusa arquivo que não é XML sem chamar a API', async () => {
    const fixture = TestBed.createComponent(Envio);

    await fixture.componentInstance.aoReceber(new File(['x'], 'nota.pdf'));
    await fixture.whenStable();

    expect(validar).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).querySelector('[role="alert"]')?.textContent).toContain('.xml');
  });

  it('valida, guarda o resultado em memória e abre o diagnóstico', async () => {
    validar.mockReturnValue(of(DIAGNOSTICO));
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const fixture = TestBed.createComponent(Envio);

    await fixture.componentInstance.aoReceber(new File(['<NFe/>'], 'nota.xml'));

    const validacao = TestBed.inject(SessaoDiagnostico).validacao();
    expect(validacao?.nomeArquivo).toBe('nota.xml');
    expect(validacao?.xml).toBe('<NFe/>');
    expect(validacao?.diagnostico).toEqual(DIAGNOSTICO);
    expect(navegar).toHaveBeenCalledWith(['/diagnostico']);
  });

  it('não grava o XML em nenhum armazenamento do navegador (D026)', async () => {
    validar.mockReturnValue(of(DIAGNOSTICO));
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const gravarStorage = vi.spyOn(Storage.prototype, 'setItem');
    const fixture = TestBed.createComponent(Envio);

    await fixture.componentInstance.aoReceber(new File(['<NFe>conteudo</NFe>'], 'nota.xml'));

    expect(gravarStorage).not.toHaveBeenCalled();
    expect(localStorage.length).toBe(0);
    expect(sessionStorage.length).toBe(0);
    expect(document.cookie).toBe('');
  });
});
