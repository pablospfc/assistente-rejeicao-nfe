# Frontend — Assistente de Rejeições da NF-e

Angular 22 com Angular Material (tema Material 3). Requer Node 24 (`.nvmrc`); o CLI é dependência local.

```bash
npm install
npm start                    # http://localhost:4200, com /api encaminhado para localhost:8080
npx ng test --watch=false    # testes de componentes (Vitest)
npx ng build                 # build de produção em dist/
```

- `src/app/envio/` — tela de envio, com a área de arrastar e soltar.
- `src/app/diagnostico/` — tela de diagnóstico, contrato da API e estado em memória.
- `src/app/visualizador/` — XML enviado com o campo da ocorrência destacado (resolução de XPath no navegador).

Decisões: D025 (arquitetura e Material) e D026 (visualizador sem persistência e sem terceiros), em [`../docs/decisoes.md`](../docs/decisoes.md). Instruções gerais no [README do projeto](../README.md).
