import { linhaDoErro, resolverXPath } from './xpath';

const XML = `<NFe xmlns="http://www.portalfiscal.inf.br/nfe">
  <infNFe Id="NFe123" versao="4.00">
    <ide><cUF>21</cUF></ide>
    <det nItem="1"><prod><vProd>300.00</vProd></prod></det>
    <det nItem="2"><prod><vProd>200.00</vProd></prod></det>
  </infNFe>
  <Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignatureValue>AAAA</SignatureValue></Signature>
</NFe>`;

const documento = new DOMParser().parseFromString(XML, 'application/xml');

describe('resolverXPath', () => {
  it('resolve caminhos por nome local, ignorando namespaces', () => {
    expect(resolverXPath(documento, '/NFe/infNFe/ide/cUF')?.elemento.textContent).toBe('21');
    expect(resolverXPath(documento, '/NFe/Signature/SignatureValue')?.elemento.textContent).toBe('AAAA');
  });

  it('usa o índice quando há irmãos de mesmo nome', () => {
    expect(resolverXPath(documento, '/NFe/infNFe/det[2]/prod/vProd')?.elemento.textContent).toBe('200.00');
    expect(resolverXPath(documento, '/NFe/infNFe/det[1]/prod/vProd')?.elemento.textContent).toBe('300.00');
  });

  it('sem índice, pega o primeiro', () => {
    expect(resolverXPath(documento, '/NFe/infNFe/det/prod/vProd')?.elemento.textContent).toBe('300.00');
  });

  it('aponta o elemento e o atributo', () => {
    const alvo = resolverXPath(documento, '/NFe/infNFe/@Id');

    expect(alvo?.elemento.localName).toBe('infNFe');
    expect(alvo?.atributo).toBe('Id');
  });

  it('devolve null para caminho inexistente, raiz errada ou XPath ausente', () => {
    expect(resolverXPath(documento, '/NFe/infNFe/det[3]/prod')).toBeNull();
    expect(resolverXPath(documento, '/NFe/infNFe/@xyz')).toBeNull();
    expect(resolverXPath(documento, '/CTe/infCte')).toBeNull();
    expect(resolverXPath(documento, null)).toBeNull();
  });
});

describe('linhaDoErro', () => {
  it('lê a linha informada no 243', () => {
    expect(linhaDoErro('linha 12, coluna 7')).toBe(12);
    expect(linhaDoErro(null)).toBeNull();
  });
});
