/** Alvo de um XPath da ocorrência: o elemento e, se for o caso, o atributo. */
export interface AlvoXPath {
  elemento: Element;
  atributo: string | null;
}

/**
 * Resolve os XPath gerados pelo backend: nomes locais (sem namespace), índice só quando
 * há irmãos de mesmo nome e atributo no fim. Ex.: /NFe/infNFe/det[2]/prod/vProd, /NFe/infNFe/@Id.
 * Devolve null se o caminho não existir no documento.
 */
export function resolverXPath(documento: Document, xpath: string | null): AlvoXPath | null {
  if (!xpath?.startsWith('/')) {
    return null;
  }
  const passos = xpath.split('/').filter((p) => p.length > 0);
  let atributo: string | null = null;
  if (passos.at(-1)?.startsWith('@')) {
    atributo = passos.pop()!.substring(1);
  }

  const raiz = documento.documentElement;
  const [primeiro, ...demais] = passos.map(lerPasso);
  if (!primeiro || raiz.localName !== primeiro.nome) {
    return null;
  }

  let atual: Element = raiz;
  for (const passo of demais) {
    const irmaos = Array.from(atual.children).filter((filho) => filho.localName === passo.nome);
    const proximo = irmaos[passo.indice - 1];
    if (!proximo) {
      return null;
    }
    atual = proximo;
  }
  if (atributo !== null && !atual.hasAttribute(atributo)) {
    return null;
  }
  return { elemento: atual, atributo };
}

function lerPasso(passo: string): { nome: string; indice: number } {
  const m = /^([^[\]]+)(?:\[(\d+)])?$/.exec(passo);
  return m ? { nome: m[1], indice: m[2] ? Number(m[2]) : 1 } : { nome: passo, indice: 1 };
}

/** Linha do erro 243, informada pelo backend como "linha L, coluna C". */
export function linhaDoErro(valorEncontrado: string | null): number | null {
  const m = /linha (\d+)/.exec(valorEncontrado ?? '');
  return m ? Number(m[1]) : null;
}
