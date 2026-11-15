# Registro de decisões de arquitetura

Cada decisão segue o formato: contexto, decisão, alternativas descartadas.

## Índice

| Fase | Decisões |
|---|---|
| 1 — Estrutura | D001 versão dos schemas · D002 NF-e como DOM · D003 segurança e não persistência · D004 tradução do 225 · D005 namespace (404 × 225) · D006 assinatura estrutural nos XMLs de base · D007 regras seguras contra campos ausentes |
| 2 — Chave e identificação | D008 502 e 253 no lugar de 236 e 614–618 · D009 emitente pessoa física · D010 chave composta e DV (ASCII − 48) · D011 IE por UF (Strategy) · D012 relógio e tolerância do 703 · D013 conferência no MOC 7.0 · D014 212 → 703 |
| 3 — Totais e operação | D015 premissa da Reforma no 610 · D016 598 no lugar do 373 e fonte do 441 · D017 interpretações do 321 · D018 tolerâncias e arredondamento · D019 Tabela CFOP (327/328) |
| 4 — Assinatura | D020 alcance do 297 · D021 SHA-1 só no contexto de validação · D022 certificado gerado na execução |
| 5 — Avaliação | D023 metodologia por mutações · D024 característica do 225 |
| 6 — Frontend | D025 Angular 22 e Material · D026 visualizador sem persistência e sem terceiros · D027 exemplos assinados |
| 7 — Revisão final | D028 migração para Spring Boot 4.1.1 e Java 25 |

A seção [Limitações e extensões futuras](#limitações-e-extensões-futuras), no fim, reúne o que ficou fora do escopo.

## D001 — Versão dos schemas XSD

**Contexto.** A validação estrutural (rejeição 225) depende dos schemas oficiais publicados no Portal da NF-e. Os schemas mudam a cada Pacote de Liberação (PL), e o resultado da validação só é reproduzível se a versão usada estiver registrada.

**Decisão.** Usar o **Pacote de Liberação 010g v1.03 (NT 2026.006 v1.00 e NT 2026.008 v1.00), publicado em 26/10/2026**, baixado manualmente pelo autor do Portal da NF-e e mantido em `backend/src/main/resources/xsd/`.

Os arquivos do pacote não trazem o identificador do PL no próprio conteúdo, então a versão acima é a informada pelo autor no momento do download. Para permitir conferência posterior, registram-se os hashes SHA-256 dos arquivos usados:

| Arquivo | SHA-256 |
|---|---|
| `nfe_v4.00.xsd` | `adce3646c13ceb54922ec3142fc1dc45bd4fb839ac35ad583e86c733c07d27df` |
| `leiauteNFe_v4.00.xsd` | `e8470f5d85ded3d99010676087907214aef71c9626140b672abbafa72656c0f5` |
| `tiposBasico_v4.00.xsd` | `772619c85723e598840667ca66e7298a250442df47eeb94b397d2a333ce62047` |
| `DFeTiposBasicos_v1.00.xsd` | `2da0785a6c70db4f93571187a28514dde715cfd0b0f41e746176fbf3444549f2` |
| `xmldsig-core-schema_v1.01.xsd` | `f56744a5f51c03f027de13f39f869307091781a9ef1d91b1ebe14719ce28e1ac` |

O ponto de entrada da validação é `nfe_v4.00.xsd` (elemento `NFe`), que inclui `leiauteNFe_v4.00.xsd` e, por meio dele, os tipos básicos e o schema de assinatura.

**Alternativas descartadas.**
- *Baixar os schemas em tempo de execução:* viola a regra de validação 100% offline e torna o resultado dependente da disponibilidade do portal.
- *Reconstruir ou editar os XSDs manualmente:* perderia o caráter oficial da validação; os arquivos são usados exatamente como publicados.

## D002 — NF-e representada pelo DOM, não por classes JAXB

**Contexto.** As regras de negócio precisam ler os campos da NF-e. O projeto exige mostrar todos os problemas de uma vez, inclusive quando o XML também falha no schema (225).

**Decisão.** A classe `NFe` envolve o `Document` DOM e lê campos por caminho de nomes locais (`texto("/NFe/infNFe/ide/cUF")`). As regras de negócio rodam mesmo quando há erros de schema; só o 243 (XML mal formado) interrompe a validação.

**Alternativas descartadas.**
- *Classes geradas pelo `xjc` (JAXB):* a leitura falha ou perde dados num XML inválido, e as regras não rodariam justamente nos casos mais úteis. Também acrescentaria centenas de classes geradas, difíceis de explicar no artigo.
- *Interromper tudo no primeiro erro de schema:* mais simples, mas obrigaria o usuário a corrigir um problema por vez.

## D003 — Segurança e não persistência do XML

**Contexto.** O arquivo enviado é de terceiros (não confiável) e, pela regra do projeto, não pode ser gravado.

**Decisão.**
- O parser rejeita `DOCTYPE` e entidades externas (proteção contra XXE). Um XML com `DOCTYPE` é tratado como 243.
- O validador de schema não acessa nenhum recurso externo: os `include`/`import` do XSD só podem ser resolvidos a partir do próprio classpath (protocolos `file` e `nested`, o segundo usado pelo jar do Spring Boot).
- O upload é limitado a 1 MB e `spring.servlet.multipart.file-size-threshold` é maior que esse limite. Assim, o Spring nunca grava o arquivo em disco temporário (o padrão, com limite 0, gravaria).

**Alternativas descartadas.**
- *Receber o XML como corpo `application/xml`:* evitaria o multipart, mas complica o upload com arrastar e soltar no frontend.
- *Liberar acesso externo no `SchemaFactory`:* violaria a validação 100% offline.

## D004 — Tradução das mensagens do XSD (225)

**Contexto.** O validador do JDK produz mensagens genéricas, em inglês ou português conforme a JVM (ex.: `cvc-pattern-valid: Value '200,00' is not facet-valid...`), sem indicar o caminho do campo.

**Decisão.**
- O idioma do validador é fixado em inglês, para que o formato das mensagens não dependa da máquina.
- O `TradutorErroSchema` lê a chave (`cvc-*`) e os argumentos entre aspas e gera uma `Ocorrencia` com valor encontrado, valor esperado e um *motivo* (`formato`, `lista`, `tamanhoMaximo`, `elementoFaltando`...).
- O XPath vem do nó em validação, lido pela propriedade `dom/current-element-node` do Xerces embutido no JDK.
- Mensagens de acompanhamento que repetem o erro anterior no mesmo elemento (`cvc-type.3.1.3`, `cvc-attribute.3`...) são descartadas. A `cvc-attribute.3` só serve para apontar o XPath para o atributo (`/NFe/infNFe/@versao`).
- O texto ao usuário fica no catálogo YAML, em `225.motivos`, e não no código.

**Alternativas descartadas.**
- *Mostrar a mensagem original do validador:* técnica, às vezes em inglês, e sem o caminho do campo.
- *Validação via SAX acompanhando a pilha de elementos:* também daria o XPath, mas com mais código. Fica como plano B caso a propriedade do Xerces deixe de existir.

## D005 — Namespace com prefixo é 404; ausente ou diferente do padrão é 225

**Contexto.** O código 404 trata de *prefixo* de namespace. Um namespace ausente ou diferente do padrão também é rejeitado pelo XSD. Existe um código específico para esse caso, o 587 ("Usar somente o namespace padrão da NF-e"), mas ele está fora do escopo das 28 rejeições do projeto.

**Decisão.** O 404 cobre só o uso de prefixo. Namespace ausente ou diferente do padrão é tratado como 225, com o motivo `raizDesconhecida` e uma explicação que indica o namespace correto.

**Alternativas descartadas.**
- *Implementar o 587 agora:* ampliaria o escopo definido para o TCC. Fica como **possível extensão futura**: bastaria um verificador na camada de estrutura que compare o namespace da raiz com `http://www.portalfiscal.inf.br/nfe`, mais uma entrada no catálogo.
- *Reportar namespace errado como 404:* o texto do 404 fala de prefixo, e misturar os dois casos tornaria a explicação imprecisa.

## D006 — Assinatura estrutural nos XMLs de base (Fase 1)

**Contexto.** No `leiauteNFe_v4.00.xsd`, o tipo `TNFe` declara `<xs:element ref="ds:Signature"/>` sem `minOccurs`, ou seja, a assinatura é obrigatória. A conferência real da assinatura (297) só entra na Fase 4.

**Decisão.** Os XMLs válidos da Fase 1 levam uma `Signature` correta só na forma:
- os algoritmos fixados pelo `xmldsig-core-schema_v1.01.xsd` (c14n, `rsa-sha1`, `sha1`, as duas `Transform`);
- `Reference URI="#NFe..."` igual ao `Id` de `infNFe`;
- valores base64 de preenchimento em `DigestValue`, `SignatureValue` e `X509Certificate`.

Isso basta para o XSD, que não confere criptografia.

**Consequências para a Fase 4 em diante.**
- Quando o 297 for implementado, todo XML de base com assinatura de preenchimento passará a ser rejeitado. Os XMLs de base precisarão ser assinados de verdade com o certificado de teste.
- **Toda mutação altera o conteúdo assinado.** O gerador de mutações (Fase 5) precisará reassinar o XML depois de cada mutação, exceto nas mutações que testam o próprio 297. Sem isso, toda mutação também dispararia 297 e poluiria a medição de "associada ao código correto". As mutações da chave de acesso (Fase 2) mudam também o `Id` e, portanto, o `Reference URI`.
- **Verificar na Fase 4:** o schema fixa `rsa-sha1`/`sha1`, e a validação segura de assinaturas XML do JDK 17+ recusa SHA-1 por padrão. Será preciso ajustar isso explicitamente no contexto de validação.
- A forma de organizar isso (assinar os XMLs de base no disco ou assinar durante os testes) será decidida na Fase 4.

**Resolvido na Fase 4:**
- o que o 297 cobre: D020;
- SHA-1 restrito ao contexto de validação: D021;
- assinatura em memória, com certificado gerado na execução dos testes: D022.

A assinatura de preenchimento continua nos arquivos, e o `VerificadorAssinatura` a ignora, porque o certificado "AAAA" não pode ser lido.

## D007 — Regras de negócio seguras contra campos ausentes e critério de detecção

**Contexto.** Pela D002, as regras de negócio rodam mesmo depois de um 225. Nesse caso o documento pode estar sem campos de que a regra precisa.

**Decisão.**
- Só o 243 interrompe a validação. Depois de um 225, as regras de negócio rodam normalmente.
- Toda regra precisa ser segura contra campos ausentes. Se faltar um campo de que ela precisa, ela é ignorada: devolve lista vazia, sem exceção e sem ocorrência. `NFe.texto(...)` devolve `Optional` para tornar isso natural. Esse contrato está documentado em `RegraValidacao`.
- A resposta da API traz `regrasApesarDeFalhaNoSchema`, verdadeiro quando há ocorrências de regras de negócio num XML que também falhou no schema. Assim o frontend pode avisar que essas ocorrências foram encontradas sobre um documento estruturalmente inválido.
- **Critério da avaliação (Fase 5):** uma mutação conta como detectada se o código esperado estiver entre as ocorrências, mesmo que outras ocorrências também apareçam.

**Alternativas descartadas.**
- *Capturar exceções das regras no orquestrador:* esconderia defeitos das regras em vez de corrigi-los.
- *Gerar uma ocorrência quando falta um campo:* duplicaria o 225, que já aponta o campo ausente.
- *Exigir que o código esperado seja a única ocorrência:* penalizaria mutações que, corretamente, também quebram o schema.

## D008 — Mudança de escopo na chave de acesso: 502 e 253 no lugar de 236 e 614–618

**Contexto.** O escopo original previa sete códigos para a chave de acesso: 502, 236, 614, 615, 616, 617 e 618. Conferindo o MOC, o 236 e os códigos 614 a 618 pertencem à validação de chaves informadas em consultas e eventos, e não à autorização da própria NF-e. Aplicá-los à chave do `infNFe/@Id` daria à ferramenta um comportamento que a SEFAZ não tem.

**Decisão.**
- Na chave da própria NF-e, o projeto usa dois códigos:
  - **502:** o `Id` não corresponde à concatenação dos campos que compõem a chave (`cUF`, AAMM de `dhEmi`, CNPJ do emitente, `mod`, `serie`, `nNF`, `tpEmis`, `cNF`, `cDV`).
  - **253:** o dígito verificador da chave composta a partir desses campos não confere com o `cDV`.
- **Mutações nos campos da chave são detectadas pelo 502.** Alterar qualquer campo que compõe a chave (UF, data, CNPJ, modelo, série, número, tipo de emissão ou código numérico) faz o `Id` deixar de corresponder à composição. Para medir cada código isoladamente, o gerador de mutações recalcula o `cDV` quando altera um campo (dispara só o 502) e altera o `cDV` junto com o dígito do `Id` quando testa o 253 (dispara só o 253).
- Saem do escopo: 236, 614, 615, 616, 617 e 618. Entram, mantendo as 28 rejeições:
  - **212** (data de emissão posterior à de recebimento), na Fase 2 — depois substituído pelo **703** (D014);
  - **533, 534, 535 e 537** (totais de BC ICMS-ST, ICMS-ST, frete e desconto), na Fase 3.

**Alternativas descartadas.**
- *Manter 614–618 aplicados à chave da própria NF-e:* cobriria os mesmos defeitos com códigos que a SEFAZ não devolve nesse contexto, o que comprometeria a fidelidade do diagnóstico, que é o objetivo do TCC.
- *Manter o 236 junto com o 253:* os dois falam de dígito verificador, mas o 236 é o código usado quando a chave é informada em consultas e eventos.

## D009 — Emitente pessoa física fora do escopo

**Contexto.** O leiaute permite emitente com CPF (`emit/CPF`), por exemplo produtor rural. A rejeição própria desse caso é o 401 (CPF do emitente inválido), que não está entre as 28 do projeto.

**Decisão.** As regras de CNPJ (207 e a composição da chave no 502/253) ignoram emitente com CPF. Os XMLs de base usam sempre emitente com CNPJ.

**Alternativas descartadas.**
- *Validar `emit/CPF` com o código 237:* o 237 é do destinatário, e usá-lo para o emitente daria um diagnóstico errado.
- *Incluir o 401 agora:* ampliaria o escopo. Fica como **extensão futura**: reaproveita o cálculo de dígito verificador do CPF que o 237 já usa.

## D010 — Chave composta e dígito verificador com ASCII − 48

**Contexto.** As rejeições 502 e 253 dependem da chave formada a partir dos campos da NF-e. Desde o pacote atual, o CNPJ pode ser alfanumérico (`TCnpj` = `[0-9A-Z]{12}[0-9]{2}`), e as letras também aparecem dentro da chave (`Id` = `NFe[0-9]{6}[0-9A-Z]{12}[0-9]{26}`).

**Decisão.**
- O record `ChaveAcesso` compõe as 43 posições (`cUF`, AAMM de `dhEmi`, CNPJ, `mod`, `serie` e `nNF` completados com zeros, `tpEmis`, `cNF`). Se algum campo faltar, não couber na sua posição ou o emitente for pessoa física, a composição devolve vazio e as regras são ignoradas (D007, D009).
- A classe `DigitoVerificador` concentra o módulo 11. Cada caractere vale seu código ASCII − 48, então os algarismos valem 0–9 e as letras valem 17 ('A') a 42 ('Z'), como define a Receita Federal para o CNPJ alfanumérico. Um único método, `modulo11(numero, pesoMaximo)`, atende a chave e o CNPJ (pesos 2–9), o CPF (pesos crescentes, sem reinício) e as IEs do MA (2–9) e do PR (2–7).
- Conferências: o exemplo da Receita `12ABC34501DE35` e os DVs das chaves dos XMLs de base estão nos testes.
- **CPF:** além do dígito verificador, um número com todos os dígitos iguais é inválido (237).

**Alternativas descartadas.**
- *Uma função de DV por documento:* repetiria o mesmo laço cinco vezes, com risco de divergência.
- *Tratar só o CNPJ numérico:* o schema do pacote já aceita o alfanumérico, e um CNPJ válido seria rejeitado.

## D011 — Inscrição Estadual: uma Strategy por UF (MA, SP e PR)

**Contexto.** O cálculo do dígito da IE (209/210) é diferente em cada UF. O escopo cobre MA, SP e PR, que têm algoritmos bem distintos entre si.

**Decisão.** A interface `DigitoIE` tem uma implementação por UF, e `EstrategiasIE` escolhe a implementação pela UF do endereço (`enderEmit/UF` ou `enderDest/UF`). Se não houver IE, UF ou estratégia para a UF, a regra é ignorada. A IE `ISENTO` do emitente é aceita.

| UF | Formato | Dígitos | Fonte |
|---|---|---|---|
| MA | 9 dígitos, começa por `12` | 9º: módulo 11, pesos 9 a 2; resto 0 ou 1 → 0 | SINTEGRA, http://www.sintegra.gov.br/Cad_Estados/cad_MA.html |
| SP | 12 dígitos (industriais e comerciantes) | 9º: pesos 1,3,4,5,6,7,8,10; 12º: pesos 3,2,10,9,8,7,6,5,4,3,2; dígito = algarismo da unidade do resto da divisão por 11 | SINTEGRA, http://www.sintegra.gov.br/Cad_Estados/cad_SP.html |
| PR | 10 dígitos (8 + 2) | 9º e 10º: módulo 11, pesos 2 a 7 da direita para a esquerda; resto 0 ou 1 → 0 | SINTEGRA, http://www.sintegra.gov.br/Cad_Estados/cad_PR.html |

- Os exemplos das três páginas (`120000385`, `110042490114` e `1234567850`) estão nos testes. Os dois últimos cobrem os casos de resto 10 (SP) e resto 1 (PR).
- A página de SP cita "118" no texto do 2º dígito, mas a soma do próprio exemplo é 125. Prevaleceu o cálculo do exemplo.
- A IE de produtor rural de SP (iniciada por `P`) está fora do escopo. Ela também não passa no `TIe` do schema (`[0-9]{2,14}|ISENTO`).
- **Normalização do tamanho (acrescentada na Fase 3):** pelo MOC 7.0, Anexo I, nota (*2), "o tamanho da IE deve ser normalizado [...], desprezando os zeros não significativos antes da verificação do dígito de controle". A interface `DigitoIE` retira os zeros à esquerda e completa a IE com zeros até o tamanho da UF (MA 9, SP 12, PR 10) antes de conferir. Assim, `0123456789` é aceita no MA, e uma IE com dígito significativo além do tamanho é recusada.

**Alternativas descartadas.**
- *Um `switch` por UF numa única classe:* misturaria algoritmos diferentes. Com a Strategy, incluir uma UF é acrescentar uma classe e uma linha em `EstrategiasIE`.
- *Biblioteca externa de validação de IE:* o algoritmo ficaria fora do código descrito no artigo, e a fonte não ficaria explícita.

## D012 — Relógio e tolerância da data-hora de emissão (703)

**Contexto.** A regra compara a data-hora de emissão com o horário de recebimento pela SEFAZ. Como a validação é offline, não há horário de recebimento. (A regra nasceu como 212 e passou a ser o 703 na D014.)

**Decisão.**
- O "recebimento" é o instante da validação, lido de um `Clock` injetado (`Clock.systemUTC()` em produção, `Clock.fixed` nos testes). A comparação é feita entre instantes, então o fuso informado em `dhEmi` é respeitado.
- A tolerância fica configurável em `validacao.tolerancia-dh-emi` (`application.yml`), com **padrão de 5 minutos**, o valor que o MOC 7.0 (Anexo I, grupo B) aceita "devido ao sincronismo de horário do servidor da Empresa e o servidor da SEFAZ". Até a conferência no MOC (D013), o padrão era zero.
- Um `dhEmi` ausente ou ilegível faz a regra ser ignorada (o 225 já aponta esse caso).

**Alternativas descartadas.**
- *Usar `Instant.now()` direto na regra:* os testes passariam a depender da data em que rodam.
- *Fixar a tolerância no código:* ela é uma regra da SEFAZ que pode mudar por Nota Técnica, então fica em configuração.

## D013 — Conferência no MOC 7.0 e o problema do 212

**Contexto.** Os títulos do catálogo eram provisórios (`# conferir MOC`). O autor forneceu o *Manual de Orientação do Contribuinte, versão 7.0* e o *Anexo I – Leiaute e Regras de Validação da NF-e e da NFC-e* (versão 7.00, novembro de 2020). O texto dos PDFs foi extraído e cada código foi procurado na tabela de códigos e na regra de validação correspondente.

**Resultado.**

| Código | Título confirmado | Regra no Anexo I | Confere com a implementação |
|---|---|---|---|
| 243 | Rejeição: XML Mal Formado | B02 | sim |
| 225 | Rejeição: Falha no Schema XML do lote de NFe | D01 | sim |
| 404 | Rejeição: Uso de prefixo de namespace não permitido | verificação de namespace | sim |
| 587 | Rejeição: Usar somente o namespace padrão da NF-e | "namespace diverso do padrão" | fora do escopo (D005) |
| 502 | Rejeição: Erro na Chave de Acesso - Campo Id não corresponde à concatenação dos campos correspondentes | A03-10 | sim |
| 253 | Rejeição: Digito Verificador da chave de acesso composta inválida | B25b-30 | sim |
| 207 | Rejeição: CNPJ do emitente inválido | C02-20: "CNPJ com zeros, nulo ou DV inválido" | sim |
| 208 | Rejeição: CNPJ do destinatário inválido | E03a-20: "CNPJ com zeros ou dígito de controle inválido" | sim |
| 237 | Rejeição: CPF do destinatário inválido | E05-10: "CPF com zeros, nulo, 111..., 222..., ... ou dígito de controle inválido" | sim (confirma a regra dos dígitos iguais) |
| 209 | Rejeição: IE do emitente inválida | C17: "Se IE diferente de ISENTO, validar a IE" | ajustada (ver abaixo) |
| 210 | Rejeição: IE do destinatário inválida | E18: "IE inválida para a UF: erro no tamanho, na composição ou no DV" | sim |
| 401 | Rejeição: CPF do emitente inválido | C02a-30 | fora do escopo (D009) |
| 533, 534, 535, 537 | títulos iguais aos provisórios | W05/W06, W12-10 | entram na Fase 3 |

- **Ajuste no 209:** pelo Anexo I, uma IE do emitente com zeros ou nula gera o 229 ("IE do emitente não informada"), e não o 209. A regra 209 passou a ignorar IE só com zeros, para não associar o defeito ao código errado. O 229 fica como extensão futura.
- **O 212 não é da autorização da NF-e.** No MOC 7.0, o 212 só aparece nas regras do Evento Prévio de Emissão em Contingência (EPEC, tabela 5-45). Na autorização da própria NF-e (Anexo I, grupo B), "Data-Hora de Emissão posterior ao horário de recepção na SEFAZ" é o **703** ("Rejeição: Data-Hora de Emissão posterior ao horário de recebimento"), com **tolerância de até 5 minutos** pelo sincronismo de horário. É o mesmo tipo de problema da D008. Resolvido na D014.
- **Limite da conferência:** o MOC 7.0 é de 2020 e os schemas do projeto são de 2026 (D001). As Notas Técnicas posteriores podem ter alterado algum texto. Os títulos foram confirmados contra esta versão do manual.

## D014 — O 212 é substituído pelo 703

**Contexto.** A D013 mostrou que o 212 ("Data de emissão NF-e posterior a data de recebimento") só aparece no MOC 7.0 nas regras do EPEC. Na autorização da própria NF-e, a mesma verificação usa o 703 ("Data-Hora de Emissão posterior ao horário de recebimento"), com tolerância de até 5 minutos.

**Decisão.** O autor aprovou a troca:
- `Regra212DataEmissao` passou a ser `Regra703DataEmissao`, com o código 703.
- A tolerância padrão passou a ser 5 minutos (D012).
- O catálogo traz o título oficial do 703, e o grupo Identificação do escopo passou a ser 703, 207, 208, 237, 209, 210. O total continua em 28 rejeições.
- Os testes cobrem: diferença de 0, 4 e exatamente 5 minutos passa; 6 minutos dispara; com tolerância zero, 4 minutos dispara; um `dhEmi` em outro fuso é comparado pelo instante.

**Alternativas descartadas.**
- *Manter o 212:* o diagnóstico citaria um código que a SEFAZ não devolve na autorização da NF-e, o mesmo problema já corrigido na D008.
- *Usar os dois códigos:* o 212 do EPEC não se aplica à validação de uma NF-e antes da transmissão, que é o objetivo do projeto.

## D015 — Premissa: a Reforma Tributária não altera a regra 610 (vNF)

**Contexto.** O 610 confere o `vNF` contra a soma dos valores que o compõem. O pacote de schemas 010g (D001) inclui as Notas Técnicas da Reforma Tributária, e seria preciso saber se elas mudaram a composição do `vNF`. Não temos os textos das NTs, só os XSDs e o MOC 7.0 (2020).

**O que os XSDs do pacote mostram.**
- O grupo `total/ICMSTot` continua com o `vNF` e com todos os campos da fórmula do MOC 7.0 (`vProd`, `vDesc`, `vICMSDeson`, `vST`, `vFCPST`, `vFrete`, `vSeg`, `vOutro`, `vII`, `vIPI`, `vIPIDevol`), na mesma ordem.
- A Reforma acrescentou, **fora** do `ICMSTot`:
  - `ISTot` (Imposto Seletivo);
  - `IBSCBSTot` (IBS/CBS);
  - `vNFTot`, documentado como "Valor Total da NF considerando os impostos por fora IBS, CBS e IS";
  - no item, `det/vItem`.
- O ICMS monofásico (`qBCMono`, `vICMSMono` etc.) também aparece no `ICMSTot`, mas não consta da fórmula do 610 no MOC 7.0.

**Premissa adotada.** O 610 segue a fórmula do MOC 7.0 (Anexo I, W16-10) sobre o `vNF`. O IBS, a CBS e o IS são tributos "por fora" e entram no `vNFTot`, não no `vNF`. Por isso, `vNFTot`, `IBSCBSTot`, `ISTot` e `vItem` ficam fora do escopo, e as regras que a Reforma criou para eles não são implementadas.

**Risco.** Se uma NT do pacote tiver mudado a fórmula do `vNF` ou a do ICMS monofásico, o 610 pode acusar divergência em NF-e corretas, ou deixar de acusar. Para eliminar a premissa, é preciso conferir o texto das NTs da Reforma (a NT 2025.002 e as do pacote 010g, NT 2026.006 e NT 2026.008).

## D016 — Regras da operação: 598 no lugar do 373, e a fonte do 441

**Contexto.** O escopo trazia o 373 e o 441. Conferindo as fontes:
- o 373 ("Descrição do primeiro item diferente de NOTA FISCAL EMITIDA EM AMBIENTE DE HOMOLOGACAO...") é só da NFC-e: o Anexo I (I04-10) diz "Para a NFC-e, se ambiente de homologação";
- o 441 não aparece no MOC 7.0 nem no Anexo I, porque é posterior a eles.

**Decisão.**
- **598 no lugar do 373**, como foi feito com o 212 (D014). O 598 vale para os modelos 55 e 65 (Anexo I, E04; NT 2011/002): se `tpAmb = 2`, o `dest/xNome` deve ser "NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL". Os XMLs de base já usavam esse texto, então não precisaram de ajuste.
- **441 mantido.** A fonte é a NT 2020.006 v1.30 (Intermediador e Marketplace, julho de 2021), regra YA02a-10, modelos 55 e 65: se `tPag = 99`, a descrição `xPag` é obrigatória. O título vem da tabela de códigos da NT: "Rejeição: Descrição do pagamento obrigatória para meio de pagamento 99-outros". O XSD não cobre esse caso, porque `xPag` é opcional. A regra gera uma ocorrência por `detPag` e considera ausente um `xPag` só com espaços.

**Alternativas descartadas.**
- *Manter o 373:* o diagnóstico citaria um código que a SEFAZ só devolve para a NFC-e.
- *Substituir o 441:* o autor localizou a fonte, e a regra é verificável offline.

## D017 — Interpretações do autor no 321

**Contexto.** O Anexo I diz que a NF-e de devolução (`finNFe = 4`) deve referenciar o documento fiscal "NF-e, NFC-e, NF modelo 1, NF Produtor, ECF", e que a regra não se aplica "para os CFOP 1.201, 1.202, 1.410, 1.411, 5.921 e 6.921". O texto deixa três pontos em aberto, decididos pelo autor:

1. **A exceção vale quando *qualquer* item tem um desses CFOPs.** O Anexo I não diz "todos os itens". Exigir todos acusaria devoluções mistas que a regra quis dispensar.
2. **`refNFeSig` conta como NF-e referenciada.** O grupo é novo no pacote 010g e o próprio XSD o descreve como uma NF-e referenciada pela chave com código numérico zerado, para manter sigilo. Continua sendo uma NF-e, que é um dos documentos aceitos.
3. **`refCTe` não conta.** O CT-e não está na lista do Anexo I, e a devolução de mercadoria deve referenciar o documento da mercadoria, não o do transporte.

A regra também se restringe ao modelo 55, como no Anexo I.

## D018 — Tolerâncias e arredondamento nos totais

**Contexto.** As regras de totais comparam valores monetários, e o Anexo I define tolerância só em alguns casos.

**Decisão.**
- **`BigDecimal` em tudo**, lido do texto do XML e comparado com `compareTo`. Nunca `double`.
- **Somatórios (564, 531, 532, 533, 534, 535, 536, 537 e 538): comparação exata.** O Anexo I não prevê tolerância nesses casos. Campo opcional ausente no item vale zero ("Campos opcionais não informados serão considerados como se tiverem sido informados com valor = zero"). Se faltar o total, ou se algum valor não for numérico, a regra é ignorada (D007).
- **528:** o produto vBC × pICMS / 100 é arredondado para duas casas com `HALF_UP` e se aceita **R$ 0,01** para mais ou para menos (nota (*4)). O Anexo I não fixa o modo de arredondamento, mas com essa tolerância a escolha não muda o resultado.
- **610: tolerância de R$ 0,50 para a regra inteira** (nota (*3), NT 2012/003). *Interpretação do autor:* no Anexo I, o marcador (*3) está impresso ao lado da linha do `vServ`, o que é ambíguo. Aplicá-lo só a essa parcela geraria falsos positivos por arredondamento em NF-e corretas. Os testes de fronteira mostram que R$ 0,50 de diferença passa e R$ 0,51 dispara. O XML de base da indústria tem desconto de R$ 150,00, bem acima da tolerância, para a mutação "esquecer de subtrair o desconto" não cair nela.
- **610, exceções do Anexo I:** faturamento direto de veículo novo (`tpOp = 2`) não soma `vST`, `vFCPST` e `vIPIDevol`; importação (algum CFOP iniciado por 3) não passa pela regra; e um `vNF` calculado sem subtrair o `vICMSDeson` também é aceito.
- **Nomes de campo:** no item, o ICMS-ST (N23) se chama `vICMSST`; no total (W06), `vST`. O 534 soma `vICMSST`.

**Alternativas descartadas.**
- *`double`:* erro de representação binária em valores como 0,1 + 0,2 e risco de falso positivo exatamente nas regras sem tolerância.
- *Uma classe genérica parametrizada para todos os somatórios:* contrariaria a regra do projeto de uma classe por rejeição. O compromisso é a classe abstrata `RegraSomaDosItens`, com uma subclasse de três métodos por código.

## D019 — Tabela de CFOP para os códigos 327 e 328

**Contexto.** O 327 (I08-140) e o 328 (I08-144) dependem da lista de CFOPs de devolução. O Anexo I não traz essa lista e remete à "tabela de apoio publicada no Portal da NF-e (Tabela CFOP, indDevol=1)". Também define a exceção do 327: aceitar os CFOPs 1.949 e 2.949 quando `finNFe = 4` e `indIEDest = 9`.

**Decisão.** A tabela será baixada pelo autor do Portal da NF-e, como os XSDs, e guardada em `backend/src/main/resources/tabelas/`. As regras lerão a coluna `indDevol`, sem lista escrita de memória. **Pendente:** a versão e a data de publicação da tabela, a registrar aqui quando o arquivo chegar. Até lá, o 327 e o 328 não estão implementados. O XML de devolução usa o CFOP 5202, que também precisa ser confirmado na tabela.

**Atualização (12/11/2026).** O autor enviou `cfop.xlsx`. Trata-se da tabela de CFOP em uma planilha de aba única, com o título "CFOP: Código Fiscal de Operações e Prestações" e o aviso de que "o conteúdo desta tabela não substitui o publicado na página do CONFAZ". Os metadados indicam criação em 19/06/2018 e última modificação em 04/06/2021. A planilha tem 591 CFOPs e quatro colunas: `GRUPO CFOP`, `CFOP`, `DESCRIÇÃO_CFOP` e `Início Vigência`. **Ela não tem a coluna `indDevol`** exigida pelo Anexo I, então não serve para o 327 e o 328 como está. O CFOP 5202 ("Devolução de compra p/ comercialização") existe nela. A planilha não foi copiada para `resources`.

**Resolução (14/11/2026).** O autor enviou a tabela correta.

- **Fonte:** Informe Técnico 2023.002, versão 2.10, "Tabela CFOP Descrição", baixada pelo autor do Portal da NF-e em Documentos → Diversos (https://www.nfe.fazenda.gov.br/portal/listaConteudo.aspx?tipoConteudo=/NJarYc9nus=). Arquivo original: `IT_2023_002_v2_10_Tabela_CFOP_Descrição.xlsx`.
- **Data:** versão 2.10 publicada em **setembro de 2026**, segundo o controle de versões do Informe Técnico (PDF `IT_2023.002_v2.10_Tabela_CFOP_Descrição.pdf`, p. 3). O Informe dá só o mês. Os metadados da planilha registram a última modificação em 24/09/2026, o que é coerente.
- **O que diz o Informe** (p. 3–4):
  - A v1.00 (abril de 2023) publicou a tabela "com os indicadores de uso por documento fiscal eletrônico".
  - A v2.00 (agosto de 2026) incluiu `indExcIBSCBS`, ligada à NT 2026.007.
  - A v2.10 apenas incluiu as colunas `Título` e `Descrição`, que reproduzem o Anexo II do Convênio SINIEF s/nº de 1970. Ela tem "finalidade exclusivamente informativa", "não altera as demais colunas nem os indicadores já publicados" e "não produz efeito de rejeição", com datas de homologação e produção "não aplicável".
  - O `indDevol` está marcado como "inalterado" e "indica se o CFOP corresponde a operação de devolução".
  - "Fim de vigência" fica "em branco, quando vigente", o que é coerente com nenhuma linha preenchida.

  Nada disso muda o 327 nem o 328.
- **Fonte oficial dos textos:** o Informe ressalva que, havendo divergência entre o título ou a descrição na tabela e o Convênio SINIEF, prevalece o Convênio, consultado no sítio do CONFAZ. O projeto só usa as colunas `CFOP` e `indDevol`, então a ressalva não afeta as regras.
- **Conteúdo:** 619 CFOPs em 14 colunas: `CFOP`, `Início de vigência`, `Fim de vigência`, `indNFe`, `indComunica`, `indTransp`, **`indDevol`**, `indRetor`, `indAnula`, `indRemes`, `indComb`, `indExcIBSCBS` (da Reforma Tributária, não usada pelo projeto), `Título do CFOP` e `Descrição (nota explicativa)`. 105 CFOPs têm `indDevol = 1`, e nenhum tem fim de vigência preenchido.

**Como a tabela entra no projeto.**
- O script `backend/ferramentas/ConverterTabelaCfop.java` (só JDK) converteu a planilha para `backend/src/main/resources/tabelas/cfop-it-2023-002-v2.10.tsv`. Todas as linhas e colunas foram mantidas, na mesma ordem. A única mudança de formato: as datas de vigência, que o Excel guarda como número serial, foram escritas no padrão ISO.
- A classe `TabelaCfop` localiza as colunas pelo nome do cabeçalho e expõe `devolucao(cfop)` (`indDevol = 1`).
- **327:** se `finNFe = 4` (modelo 55), todo item deve ter CFOP com `indDevol = 1`, exceto 1.949 e 2.949 com `indIEDest = 9`.
- **328:** se `finNFe` não é 2 nem 4 (modelo 55), nenhum item pode ter CFOP com `indDevol = 1`.
- As duas regras geram uma ocorrência por item, apontando `det[n]/prod/CFOP`. Um CFOP ausente da tabela é tratado como "não é de devolução".
- A vigência do CFOP não é conferida, porque a rejeição de CFOP inexistente ou fora de vigência não está no escopo.
- Os CFOPs dos XMLs de base estão todos na tabela: 5101, 5102, 5401 e 6102 com `indDevol = 0`, e **5202 com `indDevol = 1`**. Isso confirma o XML de devolução.

**Por que a tabela, e não a descrição do CFOP.** A planilha mostra que o critério "a descrição começa por Devolução" erraria nos dois sentidos. Os CFOPs 1.503 a 1.506 ("Entrada decorrente de devolução...") têm `indDevol = 1`. O 7.930 ("Lançamento efetuado a título de devolução de bem...") tem `indDevol = 0`. Os testes de `TabelaCfop` cobrem esses casos.

**Alternativas descartadas.**
- *Ler o `.xlsx` em tempo de execução com Apache POI:* acrescentaria uma dependência pesada para ler uma tabela estática.
- *Guardar só as colunas `CFOP` e `indDevol`:* o TSV completo permite conferir qualquer linha contra a planilha original.

## D020 — O que o 297 verifica, e o que fica fora

**Contexto.** O MOC 7.0 (Anexo I) separa a validação da assinatura em dois grupos:
- **Grupo E, certificado:** 290 (certificado inválido: versão, *Basic Constraint*, *KeyUsage*), 291 (validade), 292 (sem CNPJ/CPF), 293 (cadeia de certificação), 294 (revogado), 295 (raiz diferente da ICP-Brasil) e 296 (erro no acesso à LCR).
- **Grupo F, assinatura:** 298 (Reference URI e Transforms fora do padrão; o próprio Anexo diz "Estas validações são implementadas pelo Schema XML da Signature"), **297** (F02: "Valor da assinatura (SignatureValue) difere do valor calculado") e 213/227 (CNPJ/CPF do certificado diferente do emitente).

**Decisão.**
- O 297 confere a **integridade criptográfica** da assinatura com a chave pública do certificado que vem no próprio `KeyInfo/X509Data`:
  1. o `DigestValue` da `Reference` contra o `infNFe` canonicalizado (enveloped + C14N);
  2. o `SignatureValue` contra o `SignedInfo` canonicalizado.
- **O digest também gera 297.** O F02 cita só o `SignatureValue`. O digest foi incluído porque faz parte da validação da assinatura (o `SignedInfo`, que é o que se assina, contém o digest) e porque um conteúdo alterado depois de assinar é a causa mais comum do 297 na prática.
- **Dois motivos no catálogo:**
  - `conteudoAlterado`: o `SignatureValue` confere, mas o digest não. O `SignedInfo` está íntegro, e quem mudou foi o `infNFe`. A ocorrência aponta `/NFe/infNFe`.
  - `valorDaAssinatura`: o `SignatureValue` não confere com o `SignedInfo` e a chave. A ocorrência aponta `/NFe/Signature/SignatureValue`. Isso cobre o `SignatureValue` alterado, o `DigestValue` alterado (que está dentro do `SignedInfo`), o certificado trocado por outro de chave diferente e um valor de preenchimento junto com um certificado real.
- **Quando o 297 é ignorado:**
  - sem `Signature`, ou sem `Id` no `infNFe`: o 225 aponta o problema;
  - assinatura que não pode ser lida;
  - certificado ilegível: seria o 290;
  - chave RSA com menos de 1024 bits;
  - `Reference URI` que não seja o próprio `infNFe`.

  Em todos esses casos não há como calcular a assinatura, e o código correto seria do grupo E ou do 298, fora do escopo.

**Limitações (fora do escopo offline).**
- A validade do certificado, a cadeia ICP-Brasil, a LCR, a revogação e a raiz (291 a 296) não são verificadas. A cadeia e a LCR dependem do cadastro de ACs da SEFAZ e de acesso à rede.
- As checagens que seriam possíveis offline (versão 3, *KeyUsage* e datas de validade; 290 e 291) não estão entre as 28 rejeições e ficam como **extensão futura**.
- O CNPJ do certificado contra o do emitente (213) também fica fora.

Na prática, o projeto aceita qualquer certificado, inclusive autoassinado, e confere apenas que a NF-e não foi alterada depois de assinada com aquela chave.

## D021 — SHA-1 e a validação segura do JDK, desligada só no contexto da NF-e

**Contexto.** O leiaute fixa `rsa-sha1` e `sha1` (atributos `fixed` no `xmldsig-core-schema_v1.01.xsd` e tabela de padrões do MOC). No JDK 21, a propriedade `jdk.xml.dsig.secureValidationPolicy` do `java.security` tem `disallowAlg` para `...#sha1` e `...#rsa-sha1`, e a validação segura vem ligada por padrão. Sem ajuste, nenhuma NF-e válida conseguiria ser conferida.

**Decisão.**
- `org.jcp.xml.dsig.secureValidation = false` **somente no `DOMValidateContext` de cada validação**, em `VerificadorAssinatura`. Nada muda no `java.security` nem em propriedades globais (`Security.setProperty`), então o resto da JVM continua com a política completa.
- Desligar a validação segura também desliga outras proteções dela, que passam a ser feitas explicitamente:
  - `SomenteInfNFe` (um `URIDereferencer`) só resolve `#` + `Id` do `infNFe`. Como o XSD aceita qualquer `anyURI` em `Reference URI`, isso impede que um XML enviado faça o servidor ler `file:` ou acessar `http:`.
  - `ChaveDoCertificado` (um `KeySelector`) só usa o `X509Certificate` do `KeyInfo`. Nada de `RetrievalMethod` ou `KeyName`. A chave precisa ser RSA com pelo menos 1024 bits, o mínimo citado pelo MOC.
  - Uma única `Reference`, com as duas Transforms (enveloped e C14N), já garantidas pelo XSD.
- **Justificativa:** o SHA-1 é imposto pelo próprio leiaute oficial. O projeto só confere a integridade de um documento enviado pelo usuário e não toma nenhuma decisão de confiança ou autenticação com base nessa assinatura (D020).

**Alternativas descartadas.**
- *Alterar `jdk.xml.dsig.secureValidationPolicy` (no `java.security` ou por `Security.setProperty`):* liberaria o SHA-1 para toda a JVM, inclusive para outros usos de assinatura XML.
- *Desligar a validação segura sem compensações:* exporia leitura de arquivos e requisições de rede a partir de um XML não confiável.

## D022 — Certificado gerado na execução e assinatura em memória nos testes

**Contexto.** Com o 297 implementado, os XMLs de base precisam de assinatura real. Os testes não podem depender de um certificado que só existe na máquina do autor, e o gerador de mutações (Fase 5) precisará reassinar o XML depois de cada mutação, exceto nas do 297.

**Decisão.**
- **Certificado:** `CertificadoTeste` (fontes de teste) gera, uma vez por execução, um certificado RSA 2048 autoassinado numa pasta temporária, com o `keytool` do JDK em uso. O caminho vem de `System.getProperty("java.home")` + `bin/keytool` (`keytool.exe` no Windows), sem depender do `PATH`. A senha é aleatória e nada fica no repositório.
- **Fora dos testes:** `scripts/gerar-certificado-teste.ps1` e `.sh` usam o mesmo `keytool` e gravam em `certificados/`, que está no `.gitignore`. Documentado no README.
- **Assinatura:** `AssinadorNFe` (fontes de teste) assina em memória no padrão do MOC (enveloped, C14N, RSA-SHA1, SHA-1, `X509Data` só com o certificado do usuário final, sem prefixo de namespace) e substitui uma assinatura que já exista.
- **XMLs de base:** no disco, continuam com a assinatura de preenchimento (D006). `XmlDeTeste.valido(nome)` devolve o XML assinado com a chave da execução, e `XmlDeTeste.assinado(xml)` reassina depois de uma mutação, que é o que o gerador da Fase 5 fará. `XmlDeTeste.noDisco(nome)` devolve o arquivo como está.
- **Quebras de linha:** no Windows, o serializador grava CRLF. O assinador devolve LF, para as mutações por trecho de texto continuarem valendo. Como a leitura do XML normaliza CRLF para LF, o digest não muda.
- **Cópias assinadas para a Fase 6** (demonstração no frontend): serão geradas por um comando reaproveitável (classe `main` ou tarefa Maven), não manualmente.

**Alternativas descartadas.**
- *Assinar os XMLs no disco:* cada edição quebraria a assinatura, a chave teria de ser fixa e versionada (contra a regra de não ter segredos no repositório), e o gerador precisaria do assinador em memória de qualquer jeito.
- *BouncyCastle para gerar o certificado:* seria uma dependência nova só para isso, e o `keytool` é o mesmo comando do script.
- *`keytool` pelo `PATH`:* poderia achar outro JDK ou nenhum.

## D023 — Metodologia da avaliação por mutações

**Contexto.** A avaliação alimenta a Tabela 1 e o Quadro 1 do artigo. Para ser reproduzível e não divergir do código, ela precisa de um catálogo explícito, de métricas definidas por escrito e de um controle contra falsos positivos.

**Decisão.**
- **Catálogo como dados.** `CatalogoMutacoes` (fontes de teste) lista as mutações. Cada uma tem:
  - um identificador;
  - o código esperado e, no 225 e no 297, o motivo esperado;
  - a descrição curta do Quadro 1;
  - o XPath esperado (`{det}` vira `det` ou `det[1]` conforme a base, e `{icms}` vira o grupo de ICMS do 1º item);
  - o momento da assinatura;
  - o ajuste de isolamento, quando houver;
  - a função que aplica a mutação.

  As mutações atuam no DOM, sem depender dos valores de cada base. A exceção é o 243, que precisa quebrar o texto.
- **Bases.** Cada mutação roda em todos os XMLs de base em que o campo existe. Se o campo não existe, o caso é "não aplicável" e fica fora do denominador. **Casos = mutações × bases aplicáveis.** O relatório mostra, por grupo, o número de mutações e o de casos.
- **Momento da assinatura**, explícito em cada mutação:
  - `REASSINAR` (padrão): a mutação é aplicada e o XML é assinado de novo (D006, D022);
  - `APOS_ASSINAR`: a mutação é aplicada sobre o XML já assinado. Vale para o 297 (é o que ele testa) e para o 243 (XML mal formado não pode ser assinado);
  - o 404 é reassinado, porque usa um prefixo para o próprio namespace da NF-e, o que mantém o schema válido.
- **Isolamento**, quando corresponde ao que um emissor real faria, registrado na própria mutação:
  - 502: cDV recalculado e Id mantido (D008);
  - 253: cDV e Id alterados juntos (D008);
  - 207: chave recomposta com o CNPJ alterado;
  - 528: total do ICMS ajustado junto;
  - 404: prefixo para o mesmo namespace.

  O que não é isolável de forma realista (ex.: 531 e 532, que também alteram o produto base × alíquota e disparam o 528) fica nas ocorrências extras.
- **Métricas** (cumulativas, por caso):
  - **detectada:** a validação devolveu ao menos uma ocorrência;
  - **código correto:** o código esperado está entre as ocorrências (D007);
  - **campo correto:** alguma ocorrência do código esperado aponta o XPath esperado e, no 225 e no 297, o motivo esperado. No 243 não há XPath, e o critério é a ocorrência sem XPath;
  - **explicação completa:** `Explicacao.completa()` (campo, problema e correção) para o código e o motivo da ocorrência. Sem ocorrência do código esperado, conta como "não";
  - **extras:** os outros códigos do caso, só como informação para a discussão; não entram nas taxas.
- **Controle negativo.** Os 5 XMLs de base, sem mutação, passam pelo validador completo e devem ter zero ocorrências. `AvaliacaoTest` falha se houver alguma.
- **Reprodutibilidade:**
  - o relógio fica fixo em 2026-11-12T12:00Z, com a tolerância do 703 igual à da aplicação (5 minutos);
  - as mutações são deterministas;
  - a chave do certificado muda a cada execução, mas altera só os bytes da assinatura, não os resultados;
  - o cabeçalho registra a data de geração, o hash do commit (e se há alterações pendentes), o JDK e as versões das fontes.
- **Saída e execução:**
  - `AvaliacaoTest` roda em todo `mvnw test` sem gravar nada;
  - `-Davaliacao.saida=<pasta>` grava `resultados.csv` (uma linha por caso), `tabela-1.md` e `quadro-1.md`, todos gerados do mesmo catálogo;
  - a execução inicial, antes de qualquer correção, fica em `docs/avaliacao/inicial/`, e a final em `docs/avaliacao/final/`, cada uma no seu commit.
- **Honestidade:**
  - um caso fora do esperado aparece no relatório como está;
  - nenhuma regra é ajustada só para fazer um caso passar;
  - um defeito real é corrigido, registrado numa decisão e listado na seção "Correções feitas durante a avaliação" (`CatalogoMutacoes.correcoes()`).

**Alternativas descartadas.**
- *Uma base por mutação:* menos casos, e o resultado dependeria da base escolhida.
- *Mutações por troca de texto:* dependeriam dos valores de cada base e não se aplicariam a todas.
- *Cobrar no teste que todas as mutações acertem:* induziria a ajustar regras ou mutações até "passar". O teste cobra só o controle negativo e a cobertura do escopo.

**Atualização (Fase 7): só a renderização do relatório mudou.**
- A execução final anterior (`docs/avaliacao/final/`, commit **c08af90**) mostrava no Quadro 1 a descrição "(</natOp>)" sem escape, e o GitHub trata esse trecho como tag HTML e o esconde.
- O gerador passou a escapar `<`, `>`, `&` e `|` nas células (`RelatorioAvaliacao.celula`, com teste).
- A execução final foi gerada de novo no commit **659070f**, com a árvore limpa.
- Nada mudou nos resultados:
  - o `resultados.csv` é idêntico byte a byte ao da execução inicial (792e81a) e ao da final anterior;
  - a Tabela 1 só mudou nas linhas de data e commit;
  - no Quadro 1, só as duas linhas do 243 mudaram, agora escapadas (`&lt;/natOp&gt;` e `&amp;`).
- Nenhuma regra, mutação ou expectativa foi alterada, e a seção "Correções feitas durante a avaliação" continua "Nenhuma".

## D024 — Característica do 225: elemento obrigatório ausente no meio de uma sequência

**Contexto.** Na execução inicial da avaliação (`docs/avaliacao/inicial/`, commit 792e81a), a mutação `M225-faltando` remove o `verProc` do grupo `ide`. Em quatro bases o `verProc` é o último elemento do grupo. O validador XSD acusa então "grupo incompleto" (`cvc-complex-type.2.4.b`), e a ocorrência aponta `/NFe/infNFe/ide`, como esperado. Na base de devolução, o `verProc` é seguido pelo `NFref`. Ali o validador acusa o `NFref` como elemento inesperado (`cvc-complex-type.2.4.a`, motivo `elementoInesperado`), e a ocorrência aponta `/NFe/infNFe/ide/NFref`. O 225 foi detectado e a explicação está completa ("O elemento NFref apareceu numa posição em que era esperado: verProc…"), mas o critério "campo correto" falhou nesse caso (111 de 112).

**Decisão.** Manter o caso como está, sem alterar a regra nem a expectativa da mutação, e tratá-lo na discussão do artigo como uma característica da validação por schema:
- quando falta um elemento obrigatório **no meio** de uma sequência, o validador XSD só percebe a falta ao encontrar o **elemento seguinte**, e é esse o nó que ele aponta;
- a explicação gerada cita o elemento esperado (`verProc`), mas o XPath destacado é o do elemento seguinte (`NFref`);
- quando o elemento ausente é o último da sequência, o validador aponta o grupo pai, e o destaque coincide com o lugar da falta.

**Melhoria futura.** No motivo `elementoInesperado`, destacar também a posição do elemento esperado. A ocorrência ganharia um segundo XPath, o do lugar onde o elemento ausente deveria estar (logo antes do elemento encontrado, no mesmo pai), e o frontend mostraria os dois: o elemento que o validador encontrou e a posição do que falta.

**Fechamento da avaliação.** Não houve correções entre a execução inicial e a final:
- a final (`docs/avaliacao/final/`) foi gerada no commit c08af90, que só acrescenta os arquivos da inicial;
- o código do backend é o mesmo do 792e81a;
- o `resultados.csv` das duas execuções é idêntico byte a byte, e a Tabela 1 e o Quadro 1 diferem só na data de geração e no hash do commit;
- a seção "Correções feitas durante a avaliação" do relatório final registra "Nenhuma".

**Alternativas descartadas.**
- *Corrigir a expectativa da mutação para a base de devolução:* faria o resultado chegar a 100%, mas seria um ajuste feito depois de ver o resultado, o que comprometeria a avaliação.
- *Trocar a mutação por outra em que o elemento removido seja sempre o último:* esconderia uma característica real da validação por schema, que interessa à discussão.

## D025 — Frontend: Angular 22 com Angular Material

**Contexto.** O frontend tem duas telas, o envio do arquivo e o diagnóstico, e vai virar figura no artigo. A stack prevista é o Angular na versão estável atual, com componentes standalone.

**Decisão.**
- **Versão:** Angular 22.2 (CLI 22.2.7), criado com `npx @angular/cli@22 new` e com o CLI como dependência local (`npx ng`), sem instalação global. O Angular 22 exige Node `^22.22.3 || ^24.15.0 || >=26`, e foi usado o **Node 24.22.3**, instalado pelo `nvm`. A versão fica documentada em `frontend/.nvmrc` (`24`) e no campo `engines` do `package.json` (`^24.15.0`).
- **Arquitetura:**
  - componentes standalone, *signals*, o novo *control flow* (`@if`, `@for`) e aplicação *zoneless*;
  - testes com Vitest, o executor padrão do CLI 22;
  - comunicação com o backend por `HttpClient` (`withFetch`) e, em desenvolvimento, pelo proxy `proxy.conf.json` (`/api` → `localhost:8080`), sem precisar configurar CORS no backend.
- **Angular Material** com o tema padrão do Material 3 (`mat.theme`) e o mínimo de CSS próprio. Os componentes do Material são usados sempre que existem:
  - `mat-toolbar` na barra;
  - `mat-card` no envio, no resumo, em cada ocorrência e no XML;
  - `mat-expansion-panel` nos grupos por camada;
  - `mat-chip` no código de rejeição;
  - `mat-progress-bar`, `mat-button` e `mat-icon`.

  A área de arrastar e soltar é um componente próprio simples (`AreaArquivo`), porque o Material não tem um, e usa as variáveis de cor e forma do tema (`--mat-sys-*`).
- **Telas:**
  - **envio:** confere `.xml`, até 1 MB e arquivo não vazio antes de enviar;
  - **diagnóstico:** resumo, aviso de `regrasApesarDeFalhaNoSchema`, ocorrências agrupadas por camada e visualizador do XML com o campo destacado (D026).

**Alternativas descartadas.**
- *CSS próprio:* daria mais controle sobre o visual, mas mais código para manter e para explicar. O Material entrega acessibilidade e um visual consistente para as figuras.
- *Instalar o Angular CLI globalmente:* a versão do CLI ficaria fora do controle do projeto.
- *CORS no backend:* desnecessário em desenvolvimento com o proxy, e o backend não precisa mudar.

## D026 — Visualizador do XML no navegador, sem persistência e sem terceiros (extensão da D003)

**Contexto.** Para destacar o campo apontado pela ocorrência, o diagnóstico mostra o XML enviado. A D003 proíbe persistir o XML no backend. Esta decisão estende a mesma regra ao navegador.

**Decisão.**
- **Só em memória:** o conteúdo do XML é lido do arquivo escolhido (`File.text()`) e guardado num *signal* (`SessaoDiagnostico`). Nada vai para `localStorage`, `sessionStorage`, IndexedDB ou cookies. Ao recarregar a página, a validação some e a guarda de rota volta para o envio. Um teste confere que o `Storage.setItem` não é chamado e que os dois armazenamentos e os cookies ficam vazios.
- **Sem terceiros:** não há analytics nem envio a terceiros. O XML só vai para `POST /api/validacoes`, o backend da própria aplicação. O `ng add @angular/material` tinha incluído fontes e ícones carregados de `fonts.googleapis.com`. Eles foram trocados pelos pacotes `@fontsource/roboto` e `material-symbols`, servidos pelo próprio bundle. A verificação no navegador mostrou todas as requisições na própria origem (`localhost:4200`), e o build não tem nenhuma referência a domínios do Google.
- **Destaque do campo:**
  - **XML bem formado:** o `DOMParser` lê o XML, e `resolverXPath` segue o formato dos XPath do backend (nomes locais, índice só com irmãos de mesmo nome, `/@atributo` no fim). O visualizador desenha a árvore indentada e marca o **nó** (identidade do elemento), não uma linha de texto. Isso funciona com NF-e escritas numa linha só, como as dos emissores reais. Em XPath de atributo, o elemento e o atributo ficam destacados. Ao clicar numa ocorrência, a área do XML rola até o campo sem mover a página.
  - **XML mal formado (243):** sem árvore, o visualizador mostra o texto original e destaca a linha informada pelo backend ("linha L, coluna C").
- **Limitação:** no motivo `elementoInesperado`, o destaque vai para o elemento seguinte (D024). Destacar também a posição do elemento esperado continua como melhoria futura.

**Alternativas descartadas.**
- *O backend devolver o XML na resposta:* duplicaria o tráfego e manteria o conteúdo circulando sem necessidade, já que o navegador já tem o arquivo.
- *Guardar a validação no `sessionStorage` para sobreviver ao recarregamento:* contrariaria a não persistência.
- *Fontes do Google Fonts:* fariam o navegador do usuário chamar servidores de terceiros a cada acesso.

## D027 — Exemplos assinados para a demonstração

**Contexto.** Os XMLs de base no disco têm assinatura de preenchimento (D006) e, enviados pela interface, dariam 297. A demonstração e as figuras do artigo precisam de XMLs assinados de verdade, gerados por um comando reaproveitável (D022).

**Decisão.**
- `ExemplosAssinadosTest` (fontes de teste do backend) gera em `exemplos/`:
  - os 5 XMLs de base assinados (`valido-*.xml`);
  - 8 XMLs com erro, criados pelas mesmas mutações da avaliação (`erro-<código>-*.xml`): 243, 225, 297, 502, 210, 610, 531 (com o 528 como extra) e 327;
  - um `README.md` com o que cada arquivo mostra e os códigos esperados.
- Comando: `cd backend && ./mvnw test -Dtest=ExemplosAssinadosTest -Dexemplos.saida=../exemplos`. Sem a propriedade, o teste só confere os exemplos e roda em todo `mvnw test`.
- Os códigos esperados são calculados com o **relógio real**, como a aplicação fará ao receber o arquivo. Por isso a mutação do 703, que depende do relógio fixo da avaliação, não entra nos exemplos.
- **Só o certificado público:** cada exemplo leva apenas o `X509Certificate` em `KeyInfo/X509Data`. O teste falha se aparecer "PRIVATE KEY" ou `RSAKeyValue`, ou se houver mais de um certificado. A chave privada fica só na memória do teste e é descartada. Os exemplos podem ser versionados.
- O README dos exemplos e o README do projeto explicam que as NF-e são sintéticas e assinadas com um certificado de teste autoassinado, sem valor fiscal.

**Alternativas descartadas.**
- *Assinar os exemplos à mão:* não seria reproduzível.
- *Gerar os exemplos com uma chave fixa versionada:* contrariaria a regra de não ter segredos no repositório.

**Observação: `&#13;` nas assinaturas geradas.**
- A API de assinatura XML do JDK quebra os valores base64 (`SignatureValue` e `X509Certificate`) em linhas de 76 caracteres e grava o retorno de carro como `&#13;`.
- O XML continua válido: o tipo `base64Binary` ignora espaços em branco. O validador do projeto confere essas assinaturas sem problema, e os exemplos e os testes passam.
- Há relatos de ambientes de autorização que recusam assinaturas com essas quebras. Isso não foi verificado, porque o projeto não se conecta à SEFAZ.
- Os exemplos são para demonstração dentro do próprio projeto e ficam como estão. Para gerar uma assinatura sem quebras, seria preciso configurar o assinador para não quebrar as linhas. O JDK tem uma propriedade de sistema para isso no Santuario embutido, que fica **a conferir** antes de qualquer uso.

## D028 — Migração para Spring Boot 4.1.1 e Java 25

**Contexto.** O backend foi construído com Spring Boot 3.5.16 e Java 21, como previa o CLAUDE.md. Na revisão final, o autor pediu a atualização para o **Spring Boot 4.1.1** e o **Java 25** (LTS), as versões atuais. A 4.1.2 já existia, mas foi mantida a versão pedida. A migração não podia alterar o comportamento da validação.

**Decisão.**
- **JDK:** Microsoft Build of OpenJDK 25.0.5 LTS, instalado pelo autor em `~/.jdks/ms-25.0.5.0.2`. No `pom.xml`, `java.version` passou a 25 e o *parent* passou a `spring-boot-starter-parent` 4.1.1 (Tomcat 11.0.24).
- **Starters:**
  - `spring-boot-starter-web` foi trocado por `spring-boot-starter-webmvc`, o nome atual no Boot 4;
  - entrou o `spring-boot-starter-webmvc-test`, porque no Boot 4 a autoconfiguração do `MockMvc` saiu do `spring-boot-starter-test`;
  - o SnakeYAML continua gerenciado pelo Boot (2.6).
- **Código:** o código principal compilou sem mudanças. Nos testes, só o import do `@AutoConfigureMockMvc` mudou de pacote (`org.springframework.boot.webmvc.test.autoconfigure`).
- **Defeito do `keytool` do JDK 25 em português:** ao gerar a chave RSA de 512 bits do teste de segurança do 297, o aviso de chave fraca traduzido para pt-BR tem um erro de formatação (`MissingFormatArgumentException: '%2$s'`), e o comando termina com falha. O mesmo comando funciona com `-J-Duser.language=en`. O `CertificadoTeste` passou a chamar o `keytool` com o idioma fixado em inglês, e a regra conferida pelo teste não mudou. Os scripts de `scripts/` usam chaves de 2048 bits, que não geram esse aviso.
- **SHA-1 (D021):** a política de validação segura do JDK 25 continua recusando `sha1` e `rsa-sha1`. O ajuste restrito ao `DOMValidateContext` segue funcionando, e os testes do 297 passam.

**Verificação.**
- Os 233 testes do backend passam no JDK 25 com o Boot 4.1.1.
- A avaliação completa, gravada fora de `docs/`, deu um `resultados.csv` **idêntico byte a byte** ao da execução final (659070f). A Tabela 1 e o Quadro 1 só diferem no cabeçalho, que registra o JDK.
- Os exemplos foram conferidos pelo `ExemplosAssinadosTest` e mantêm os códigos esperados.
- O jar empacotado sobe sem avisos da aplicação no JDK 25 e responde corretamente aos exemplos válidos e com erro (243, 297 e 225/207/610/598), ao arquivo ausente (400) e ao acima de 1 MB (413).
- Os avisos de `sun.misc.Unsafe` vistos no build vêm do Maven 3.9.11 (biblioteca Guice), não da aplicação.
- **Execução final regenerada.** Depois do commit da migração (**41eceee**), a execução final (`docs/avaliacao/final/`) foi gerada de novo no JDK 25, para o cabeçalho refletir a versão da tag `v1.0.0`. A execução anterior era a do commit **659070f**, no JDK 21. Só as linhas de data, commit e JDK mudaram na Tabela 1 e no Quadro 1, e o `resultados.csv` é idêntico. A execução inicial (`docs/avaliacao/inicial/`, commit 792e81a, JDK 21) foi mantida como estava.

**Alternativas descartadas.**
- *Manter o Boot 3.5 e o Java 21:* o suporte gratuito da linha 3.5 era incerto no período de uso do projeto.
- *Spring Boot 4.1.2:* mais nova, mas a versão pedida pelo autor foi a 4.1.1.
- *Contornar o defeito do `keytool` gerando a chave fraca de outro jeito (APIs internas do JDK ou outra biblioteca):* fixar o idioma resolve com uma linha e mantém o mesmo comando dos scripts.

## Limitações e extensões futuras

Esta seção consolida o que as decisões acima deixaram fora do escopo, para a seção de Considerações Finais do artigo.

**Limitações do escopo offline**
- Não há consulta à SEFAZ: duplicidade, situação cadastral, IE não cadastrada e demais regras que dependem da base da SEFAZ ficam fora (CLAUDE.md).
- O certificado é aceito como está: a validade, a cadeia ICP-Brasil, a LCR, a revogação e a raiz não são verificadas (290 a 296). O 297 confere só a integridade da assinatura com a chave do próprio certificado (D020).
- O 610 segue a fórmula do MOC 7.0. A premissa de que a Reforma Tributária não alterou o `vNF` não foi confirmada nas NTs (D015).
- Na falta de um elemento obrigatório no meio de uma sequência, o 225 aponta o elemento seguinte (D024).
- As fontes conferidas foram o MOC 7.0 (2020), a NT 2020.006 v1.30 e o IT 2023.002 v2.10. Os schemas são de 2026 (PL 010g), e Notas Técnicas posteriores podem ter alterado textos ou regras (D013).
- A IE é conferida só para MA, SP e PR (sem o produtor rural de SP). Nas demais UFs, as regras 209 e 210 são ignoradas (D011).

**Extensões futuras**
- **587** (usar somente o namespace padrão da NF-e): um verificador na camada de estrutura (D005).
- **401** (CPF do emitente inválido): reaproveita o cálculo do 237 (D009).
- **229** (IE do emitente não informada): hoje a IE só com zeros é ignorada pelo 209 (D013).
- **290 e 291**: as checagens de certificado possíveis offline, como versão 3, *KeyUsage* e datas de validade (D020).
- **Destacar a posição do elemento esperado** no motivo `elementoInesperado`, além do elemento seguinte (D024).
- **Eliminar a premissa do 610** conferindo as NTs da Reforma (NT 2025.002, NT 2026.006 e NT 2026.008) (D015).
- **Mais UFs para a IE**: basta uma nova classe de estratégia em `EstrategiasIE` (D011).
- **Tolerância do 703 e textos do catálogo**: o texto do 703 cita os 5 minutos, e mudar a tolerância na configuração exige ajustar o catálogo (D012).
