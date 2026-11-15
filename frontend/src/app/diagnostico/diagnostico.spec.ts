import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { DiagnosticoPagina } from './diagnostico';
import { Diagnostico, Ocorrencia } from './modelos';
import { SessaoDiagnostico } from './sessao-diagnostico';

const XML = '<NFe xmlns="http://www.portalfiscal.inf.br/nfe"><infNFe Id="NFe1"><ide><cUF>99</cUF></ide><emit><CNPJ>11222333000182</CNPJ></emit></infNFe></NFe>';

function ocorrencia(codigo: string, xpath: string, titulo: string): Ocorrencia {
  return {
    codigo, xpath, titulo, valorEncontrado: 'x', valorEsperado: null,
    campo: 'campo', problema: 'problema do ' + codigo, correcao: 'correção do ' + codigo,
  };
}

const DIAGNOSTICO: Diagnostico = {
  valida: false,
  regrasApesarDeFalhaNoSchema: true,
  camadas: {
    ESTRUTURA: [ocorrencia('225', '/NFe/infNFe/ide/cUF', 'Rejeição: Falha no Schema XML do lote de NFe')],
    REGRAS_NEGOCIO: [ocorrencia('207', '/NFe/infNFe/emit/CNPJ', 'Rejeição: CNPJ do emitente inválido')],
  },
};

describe('DiagnosticoPagina', () => {
  let fixture: ComponentFixture<DiagnosticoPagina>;

  async function abrir(diagnostico: Diagnostico): Promise<HTMLElement> {
    await TestBed.configureTestingModule({ imports: [DiagnosticoPagina], providers: [provideRouter([])] }).compileComponents();
    TestBed.inject(SessaoDiagnostico).registrar({ nomeArquivo: 'nota.xml', xml: XML, diagnostico });
    fixture = TestBed.createComponent(DiagnosticoPagina);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('agrupa as ocorrências por camada, com código, título e explicação', async () => {
    const tela = await abrir(DIAGNOSTICO);

    const paineis = tela.querySelectorAll('mat-expansion-panel');
    expect(paineis.length).toBe(2);
    expect(paineis[0].textContent).toContain('Estrutura e assinatura');
    expect(paineis[0].textContent).toContain('225');
    expect(paineis[0].textContent).toContain('Rejeição: Falha no Schema XML do lote de NFe');
    expect(paineis[1].textContent).toContain('Regras de negócio');
    expect(paineis[1].textContent).toContain('correção do 207');
    expect(tela.textContent).toContain('2 ocorrências encontradas');
  });

  it('avisa quando há regras de negócio com falha de schema', async () => {
    const tela = await abrir(DIAGNOSTICO);

    expect(tela.querySelector('[role="note"]')?.textContent).toContain('falhas de schema');
  });

  it('destaca no XML o campo da ocorrência clicada', async () => {
    const tela = await abrir(DIAGNOSTICO);
    expect(tela.querySelector('.destacada')?.textContent).toContain('<cUF>99</cUF>');

    (tela.querySelectorAll('.ocorrencia')[1] as HTMLElement).click();
    await fixture.whenStable();

    expect(tela.querySelector('.destacada')?.textContent).toContain('<CNPJ>11222333000182</CNPJ>');
  });

  it('NF-e sem rejeições mostra o resumo e o XML, sem lista e sem destaque', async () => {
    const tela = await abrir({ valida: true, regrasApesarDeFalhaNoSchema: false, camadas: {} });

    expect(tela.textContent).toContain('Nenhuma rejeição encontrada');
    expect(tela.querySelector('mat-accordion')).toBeNull();
    expect(tela.querySelector('app-visualizador-xml')?.textContent).toContain('<cUF>99</cUF>');
    expect(tela.querySelectorAll('.destacada').length).toBe(0);
  });
});
