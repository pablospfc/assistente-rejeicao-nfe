# Avaliação do Assistente de Rejeições da NF-e

- **Gerado em:** 2026-11-15T19:16:59.5971622-03:00
- **Commit:** `104ba3616534b4e9ddae1146183ddaaf3a928dae`
- **JDK:** Microsoft 25.0.5.0.2
- **Instante fixo da validação:** 2026-11-12T12:00:00Z; tolerância do 703: PT5M
- **Fontes:**
  - Schemas: Pacote de Liberação 010g v1.03 (NT 2026.006 v1.00 e NT 2026.008 v1.00) — D001
  - MOC 7.0 e Anexo I (Leiaute e Regras de Validação), versão 7.00 — D013
  - NT 2020.006 v1.30 (Intermediador e Marketplace), código 441 — D016
  - Informe Técnico 2023.002 v2.10 (Tabela CFOP), códigos 327 e 328 — D019

## Tabela 1 — Resultado da avaliação por grupo

Casos = mutações × XMLs de base em que o campo mutado existe. As porcentagens usam os casos como denominador.

| Grupo | Códigos | Mutações | Casos | Detectadas | Código correto | Campo correto | Explicação completa |
|---|---|---:|---:|---:|---:|---:|---:|
| Estrutura e assinatura | 4 | 7 | 35 | 35 (100,0%) | 35 (100,0%) | 34 (97,1%) | 35 (100,0%) |
| Chave de acesso | 2 | 2 | 10 | 10 (100,0%) | 10 (100,0%) | 10 (100,0%) | 10 (100,0%) |
| Identificação | 6 | 7 | 25 | 25 (100,0%) | 25 (100,0%) | 25 (100,0%) | 25 (100,0%) |
| Totais e cálculos | 11 | 11 | 30 | 30 (100,0%) | 30 (100,0%) | 30 (100,0%) | 30 (100,0%) |
| Regras da operação | 5 | 5 | 12 | 12 (100,0%) | 12 (100,0%) | 12 (100,0%) | 12 (100,0%) |
| **Total** | 28 | 32 | 112 | 112 (100,0%) | 112 (100,0%) | 111 (99,1%) | 112 (100,0%) |

### Definições

- **Detectada:** a validação devolveu ao menos uma ocorrência.
- **Código correto:** o código esperado está entre as ocorrências (D007).
- **Campo correto:** alguma ocorrência do código esperado aponta o XPath esperado (e o motivo esperado, no 225 e no 297). No 243 não há XPath.
- **Explicação completa:** o catálogo traz campo, problema e correção para o código e o motivo da ocorrência.
- **Ocorrências extras:** outros códigos que apareceram no mesmo caso; não entram nas taxas.

### Controle negativo (XMLs de base sem mutação)

| XML de base | Ocorrências |
|---|---:|
| `nfe-ma-devolucao.xml` | 0 |
| `nfe-ma-industria-st-ipi.xml` | 0 |
| `nfe-ma-pr-contribuinte.xml` | 0 |
| `nfe-ma-sp-cnpj-alfanumerico.xml` | 0 |
| `nfe-ma-venda-interna.xml` | 0 |

### Casos fora do esperado

| Mutação | Base | Código correto | Campo correto | Explicação completa | Encontrado |
|---|---|---|---|---|---|
| M225-faltando | `nfe-ma-devolucao.xml` | sim | não | sim | 225 em `/NFe/infNFe/ide/NFref` (elementoInesperado) |

### Ocorrências extras (informativo)

| Mutação | Casos com extras | Códigos extras |
|---|---:|---|
| M531-item | 4 | 528 |
| M532-item | 4 | 528 |

### Correções feitas durante a avaliação

Nenhuma.
