import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Ocorrencia } from '../diagnostico/modelos';
import { VisualizadorXml } from './visualizador-xml';

const XML = `<NFe xmlns="http://www.portalfiscal.inf.br/nfe"><infNFe Id="NFe123"><ide><cUF>99</cUF><natOp>VENDA</natOp></ide></infNFe></NFe>`;

function ocorrencia(parcial: Partial<Ocorrencia>): Ocorrencia {
  return {
    codigo: '225', xpath: null, valorEncontrado: null, valorEsperado: null,
    titulo: null, campo: null, problema: null, correcao: null, ...parcial,
  };
}

describe('VisualizadorXml', () => {
  let fixture: ComponentFixture<VisualizadorXml>;

  async function mostrar(xml: string, oc: Ocorrencia | null): Promise<HTMLElement> {
    fixture = TestBed.createComponent(VisualizadorXml);
    fixture.componentRef.setInput('xml', xml);
    fixture.componentRef.setInput('ocorrencia', oc);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('destaca o elemento apontado pelo XPath, mesmo com o XML numa linha só', async () => {
    const tela = await mostrar(XML, ocorrencia({ xpath: '/NFe/infNFe/ide/cUF' }));

    const destacadas = tela.querySelectorAll('.destacada');
    expect(destacadas.length).toBe(1);
    expect(destacadas[0].textContent).toContain('<cUF>99</cUF>');
  });

  it('destaca o atributo quando o XPath termina em @', async () => {
    const tela = await mostrar(XML, ocorrencia({ codigo: '502', xpath: '/NFe/infNFe/@Id' }));

    expect(tela.querySelector('.destacada')?.textContent).toContain('<infNFe');
    expect(tela.querySelector('.atributo-destacado')?.textContent).toContain('Id="NFe123"');
  });

  it('sem ocorrência selecionada, não destaca nada', async () => {
    const tela = await mostrar(XML, null);

    expect(tela.querySelectorAll('.destacada').length).toBe(0);
    expect(tela.textContent).toContain('<natOp>VENDA</natOp>');
  });

  it('XML mal formado (243): mostra o texto original e destaca a linha do erro', async () => {
    const malFormado = '<NFe>\n  <infNFe>\n    <natOp>VENDA\n  </infNFe>\n</NFe>';
    const tela = await mostrar(malFormado, ocorrencia({ codigo: '243', valorEncontrado: 'linha 4, coluna 5' }));

    expect(tela.textContent).toContain('não é bem formado');
    expect(tela.querySelector('.destacada')?.textContent).toContain('</infNFe>');
  });
});
