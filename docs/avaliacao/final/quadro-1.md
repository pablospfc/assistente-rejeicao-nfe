# Avaliação do Assistente de Rejeições da NF-e

- **Gerado em:** 2026-11-15T16:56:36.309077-03:00
- **Commit:** `c08af901c04134ecd824549b268f412a14942683`
- **JDK:** Eclipse Adoptium 21.0.7
- **Instante fixo da validação:** 2026-11-12T12:00:00Z; tolerância do 703: PT5M
- **Fontes:**
  - Schemas: Pacote de Liberação 010g v1.03 (NT 2026.006 v1.00 e NT 2026.008 v1.00) — D001
  - MOC 7.0 e Anexo I (Leiaute e Regras de Validação), versão 7.00 — D013
  - NT 2020.006 v1.30 (Intermediador e Marketplace), código 441 — D016
  - Informe Técnico 2023.002 v2.10 (Tabela CFOP), códigos 327 e 328 — D019

## Quadro 1 — Mutações aplicadas por código de rejeição

| Grupo | Código | Rejeição | Mutação | Assinatura | Ajuste de isolamento | Casos |
|---|---|---|---|---|---|---:|
| Estrutura e assinatura | 243 | Rejeição: XML Mal Formado | Remove o fechamento de uma tag (</natOp>) | muta após assinar | — | 5 |
| Estrutura e assinatura | 243 | Rejeição: XML Mal Formado | Insere "&" sem escape na razão social do emitente | muta após assinar | — | 5 |
| Estrutura e assinatura | 225 | Rejeição: Falha no Schema XML do lote de NFe | Informa tpImp = 9, fora da lista de valores do schema | muta e reassina | — | 5 |
| Estrutura e assinatura | 225 | Rejeição: Falha no Schema XML do lote de NFe | Remove o campo obrigatório verProc, último do grupo ide | muta e reassina | — | 5 |
| Estrutura e assinatura | 297 | Rejeição: Assinatura difere do calculado | Altera a natureza da operação depois de assinar | muta após assinar | — | 5 |
| Estrutura e assinatura | 297 | Rejeição: Assinatura difere do calculado | Altera um caractere do SignatureValue | muta após assinar | — | 5 |
| Estrutura e assinatura | 404 | Rejeição: Uso de prefixo de namespace não permitido | Escreve emit/xNome com prefixo (nfe:) para o próprio namespace da NF-e | muta e reassina | prefixo para o mesmo namespace, para o schema continuar válido | 5 |
| Chave de acesso | 502 | Rejeição: Erro na Chave de Acesso - Campo Id não corresponde à concatenação dos campos correspondentes | Soma 1 ao nNF e recalcula o cDV, sem atualizar o Id | muta e reassina | cDV recalculado, para isolar o 502 do 253 (D008) | 5 |
| Chave de acesso | 253 | Rejeição: Digito Verificador da chave de acesso composta inválida | Altera o cDV e o último dígito do Id juntos | muta e reassina | Id alterado junto com o cDV, para isolar o 253 do 502 (D008) | 5 |
| Identificação | 703 | Rejeição: Data-Hora de Emissão posterior ao horário de recebimento | Informa dhEmi uma hora depois do instante da validação | muta e reassina | — | 5 |
| Identificação | 207 | Rejeição: CNPJ do emitente inválido | Altera o dígito verificador do CNPJ do emitente | muta e reassina | chave de acesso recomposta com o CNPJ alterado, como faria o emissor | 5 |
| Identificação | 208 | Rejeição: CNPJ do destinatário inválido | Altera o dígito verificador do CNPJ do destinatário | muta e reassina | — | 4 |
| Identificação | 237 | Rejeição: CPF do destinatário inválido | Altera o dígito verificador do CPF do destinatário | muta e reassina | — | 1 |
| Identificação | 237 | Rejeição: CPF do destinatário inválido | Informa CPF do destinatário com todos os dígitos iguais | muta e reassina | — | 1 |
| Identificação | 209 | Rejeição: IE do emitente inválida | Altera o dígito verificador da IE do emitente | muta e reassina | — | 5 |
| Identificação | 210 | Rejeição: IE do destinatário inválida | Altera o dígito verificador da IE do destinatário | muta e reassina | — | 4 |
| Totais e cálculos | 564 | Rejeição: Total do Produto / Serviço difere do somatório dos itens | Soma R$ 1,00 ao vProd do 1º item | muta e reassina | — | 5 |
| Totais e cálculos | 610 | Rejeição: Total da NF difere do somatório dos Valores compõe o valor Total da NF. | Soma R$ 1,00 ao vNF, acima da tolerância de R$ 0,50 | muta e reassina | — | 5 |
| Totais e cálculos | 531 | Rejeição: Total da BC ICMS difere do somatório dos itens | Soma R$ 1,00 à base de cálculo do ICMS do 1º item | muta e reassina | — | 5 |
| Totais e cálculos | 532 | Rejeição: Total do ICMS difere do somatório dos itens | Soma R$ 1,00 ao ICMS do 1º item | muta e reassina | — | 5 |
| Totais e cálculos | 528 | Rejeição: Valor do ICMS difere do produto BC e Alíquota [nItem: 999] | Soma R$ 1,00 ao ICMS do 1º item (CST 00/10/20/70, NF-e normal) | muta e reassina | total do ICMS ajustado junto, como faria o emissor, para isolar o 528 do 532 | 4 |
| Totais e cálculos | 538 | Rejeição: Total do IPI difere do somatório dos itens | Soma R$ 1,00 ao IPI tributado do 1º item | muta e reassina | — | 1 |
| Totais e cálculos | 536 | Rejeição: Total do Seguro difere do somatório dos itens | Soma R$ 1,00 ao seguro do 1º item | muta e reassina | — | 1 |
| Totais e cálculos | 533 | Rejeição: Total da BC ICMS-ST difere do somatório dos itens | Soma R$ 1,00 à base de cálculo do ICMS-ST do 1º item | muta e reassina | — | 1 |
| Totais e cálculos | 534 | Rejeição: Total do ICMS-ST difere do somatório dos itens | Soma R$ 1,00 ao ICMS-ST do 1º item | muta e reassina | — | 1 |
| Totais e cálculos | 535 | Rejeição: Total do Frete difere do somatório dos itens | Soma R$ 1,00 ao frete do 1º item | muta e reassina | — | 1 |
| Totais e cálculos | 537 | Rejeição: Total do Desconto difere do somatório dos itens | Soma R$ 1,00 ao desconto do 1º item | muta e reassina | — | 1 |
| Regras da operação | 321 | Rejeição: NF-e de devolução de mercadoria não possui documento fiscal referenciado | Remove o documento referenciado de uma NF-e de devolução | muta e reassina | — | 1 |
| Regras da operação | 327 | Rejeição: CFOP inválido para Nota Fiscal com finalidade de devolução de mercadoria[nItem:nnn] | Usa CFOP de venda (x102) numa NF-e de devolução | muta e reassina | — | 1 |
| Regras da operação | 328 | Rejeição: CFOP de devolução de mercadoria para NF-e que não tem finalidade de devolução de mercadoria [nItem:nnn] | Usa CFOP de devolução (x202) numa NF-e normal | muta e reassina | — | 4 |
| Regras da operação | 598 | Rejeição: NF-e emitida em ambiente de homologação com Razão Social do destinatário diferente de NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL | Informa a razão social real do destinatário em homologação | muta e reassina | — | 5 |
| Regras da operação | 441 | Rejeição: Descrição do pagamento obrigatória para meio de pagamento 99-outros | Remove a descrição (xPag) de um pagamento 99 (Outros) | muta e reassina | — | 1 |
