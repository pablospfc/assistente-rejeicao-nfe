export interface LinhaXml {
  nivel: number;
  tipo: 'abre' | 'fecha' | 'folha' | 'comentario';
  nome: string;
  atributos: { nome: string; valor: string }[];
  texto: string;
  elemento: Element | null;
}

export function linhasDoXml(documento: Document): LinhaXml[] {
  const linhas: LinhaXml[] = [];
  percorrer(documento.documentElement, 0, linhas);
  return linhas;
}

function percorrer(elemento: Element, nivel: number, linhas: LinhaXml[]): void {
  const atributos = Array.from(elemento.attributes).map((a) => ({ nome: a.name, valor: a.value }));
  const filhos = Array.from(elemento.childNodes).filter(
    (n) => n.nodeType === Node.ELEMENT_NODE || n.nodeType === Node.COMMENT_NODE,
  );

  if (filhos.every((n) => n.nodeType !== Node.ELEMENT_NODE)) {
    linhas.push({ nivel, tipo: 'folha', nome: elemento.tagName, atributos, texto: textoProprio(elemento), elemento });
    return;
  }

  linhas.push({ nivel, tipo: 'abre', nome: elemento.tagName, atributos, texto: '', elemento });
  for (const filho of filhos) {
    if (filho.nodeType === Node.COMMENT_NODE) {
      linhas.push({ nivel: nivel + 1, tipo: 'comentario', nome: '', atributos: [], texto: (filho.textContent ?? '').trim(), elemento: null });
    } else {
      percorrer(filho as Element, nivel + 1, linhas);
    }
  }
  linhas.push({ nivel, tipo: 'fecha', nome: elemento.tagName, atributos: [], texto: '', elemento });
}

function textoProprio(elemento: Element): string {
  return Array.from(elemento.childNodes)
    .filter((n) => n.nodeType === Node.TEXT_NODE || n.nodeType === Node.CDATA_SECTION_NODE)
    .map((n) => n.textContent ?? '')
    .join('')
    .trim();
}
