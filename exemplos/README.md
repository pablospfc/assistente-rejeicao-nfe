# Exemplos de NF-e para demonstração

**NF-e sintéticas, sem nenhum dado fiscal real**, em ambiente de homologação (`tpAmb = 2`).
Estão assinadas com um **certificado de teste autoassinado, sem valor fiscal** (não é ICP-Brasil),
gerado na hora e descartado: cada arquivo leva só o certificado público em `KeyInfo/X509Data`;
a chave privada nunca é gravada.

Gerados pelo comando (D022/D027), a partir dos XMLs de base e das mutações da avaliação:

```bash
cd backend
./mvnw test -Dtest=ExemplosAssinadosTest -Dexemplos.saida=../exemplos
```

| Arquivo | O que mostra | Códigos esperados |
|---|---|---|
| `valido-nfe-ma-devolucao.xml` | XML de base, sem rejeições | nenhum |
| `valido-nfe-ma-industria-st-ipi.xml` | XML de base, sem rejeições | nenhum |
| `valido-nfe-ma-pr-contribuinte.xml` | XML de base, sem rejeições | nenhum |
| `valido-nfe-ma-sp-cnpj-alfanumerico.xml` | XML de base, sem rejeições | nenhum |
| `valido-nfe-ma-venda-interna.xml` | XML de base, sem rejeições | nenhum |
| `erro-243-fechamento.xml` | Remove o fechamento de uma tag (&lt;/natOp&gt;) (base nfe-ma-venda-interna.xml) | 243 |
| `erro-225-lista.xml` | Informa tpImp = 9, fora da lista de valores do schema (base nfe-ma-venda-interna.xml) | 225 |
| `erro-297-conteudo.xml` | Altera a natureza da operação depois de assinar (base nfe-ma-venda-interna.xml) | 297 |
| `erro-502-nNF.xml` | Soma 1 ao nNF e recalcula o cDV, sem atualizar o Id (base nfe-ma-sp-cnpj-alfanumerico.xml) | 502 |
| `erro-210-dv.xml` | Altera o dígito verificador da IE do destinatário (base nfe-ma-pr-contribuinte.xml) | 210 |
| `erro-610-total.xml` | Soma R$ 1,00 ao vNF, acima da tolerância de R$ 0,50 (base nfe-ma-industria-st-ipi.xml) | 610 |
| `erro-531-item.xml` | Soma R$ 1,00 à base de cálculo do ICMS do 1º item (base nfe-ma-industria-st-ipi.xml) | 531, 528 |
| `erro-327-cfop-venda.xml` | Usa CFOP de venda (x102) numa NF-e de devolução (base nfe-ma-devolucao.xml) | 327 |
| `figura-1-valido.xml` | Figura: NF-e válida (base nfe-ma-venda-interna.xml) | nenhum |
| `figura-2-varias-ocorrencias.xml` | Figura: ocorrências nas duas camadas — tpImp fora da lista (225), DV do CNPJ do emitente (207), vNF + R$ 1,00 (610) e razão social real em homologação (598) (base nfe-ma-industria-st-ipi.xml) | 225, 207, 610, 598 |
| `figura-3-225-devolucao.xml` | Figura: verProc removido na devolução; o validador aponta o NFref, elemento seguinte (D024) (base nfe-ma-devolucao.xml) | 225 |
