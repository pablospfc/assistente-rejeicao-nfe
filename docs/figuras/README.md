# Figuras do artigo

Capturas das telas do Assistente de Rejeições da NF-e, feitas a partir dos XMLs sintéticos de [`exemplos/`](../../exemplos/). Os nomes dos arquivos são descritivos; a numeração das figuras fica a cargo do artigo.

| Arquivo | O que mostra | Como gerar |
|---|---|---|
| `arquitetura.png` | Fluxo da validação em três camadas (estrutura, regras de negócio, explicação) | Exportar [`arquitetura.mmd`](arquitetura.mmd) (o mesmo diagrama do README) em [mermaid.live](https://mermaid.live) |
| `tela-envio.png` | Tela de envio, com a área de arrastar e soltar | Abrir `http://localhost:4200` |
| `diagnostico-valido.png` | NF-e sem rejeições ("Nenhuma rejeição encontrada") e o XML enviado, sem nenhum campo em destaque | Enviar [`figura-1-valido.xml`](../../exemplos/figura-1-valido.xml) |
| `diagnostico-varias-ocorrencias.png` | Ocorrências nas duas camadas (225 em Estrutura; 207, 610 e 598 em Regras de negócio), aviso de falha de schema e campo destacado no XML | Enviar [`figura-2-varias-ocorrencias.xml`](../../exemplos/figura-2-varias-ocorrencias.xml) |
| `diagnostico-225-devolucao.png` | Característica do 225: a explicação cita o `verProc` ausente e o destaque vai para o `<NFref>`, elemento seguinte (D024) | Enviar [`figura-3-225-devolucao.xml`](../../exemplos/figura-3-225-devolucao.xml) |

## Como capturar

1. Suba o backend (`cd backend` e `./mvnw spring-boot:run`) e o frontend (`cd frontend` e `npm start`).
2. Abra `http://localhost:4200` e arraste o XML indicado para a área de envio. Para voltar, use "Validar outro arquivo".
3. No Chrome ou no Edge, abra as DevTools (F12), ligue a barra de dispositivos (Ctrl+Shift+M), defina **1440 × 900** com *Device pixel ratio* **2** e use **Capture screenshot** no menu ⋮ da barra.

Na figura de várias ocorrências, a primeira ocorrência (225) já vem selecionada, com o `tpImp` destacado. Para destacar um campo de regra de negócio, clique no cartão do 207 ou do 610 antes de capturar.

As NF-e são sintéticas, sem nenhum dado fiscal real, e assinadas com um certificado de teste autoassinado, sem valor fiscal.
