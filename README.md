> **Projeto acadêmico (TCC), sem vínculo oficial com nenhuma Secretaria de Fazenda. Não substitui a validação da SEFAZ.**

# Assistente de Rejeições da NF-e

Aplicação que valida o XML de uma NF-e (modelo 55, leiaute 4.00) **antes da transmissão** e explica, em português, por que ela seria rejeitada e como corrigir. Projeto de TCC; a validação é 100% offline e o XML enviado não é gravado.

> README inicial. A versão completa entra na Fase 7.

## Backend

Requer JDK 21 (`JAVA_HOME`). O Maven vem pelo wrapper.

```bash
cd backend
./mvnw test
```

Para subir a API (porta 8080):

```bash
cd backend
./mvnw spring-boot:run
```

## Frontend

Angular 22 com Angular Material. Requer Node 24 (versão em `frontend/.nvmrc`); o Angular CLI é dependência do projeto (`npx ng`), sem instalação global.

```bash
cd frontend
npm install
npm start
```

A aplicação abre em `http://localhost:4200` e encaminha `/api` para o backend em `localhost:8080` (`proxy.conf.json`). O XML enviado fica só na memória do navegador, para o visualizador destacar o campo: nada é gravado no navegador nem enviado a terceiros, e as fontes e os ícones vêm do próprio bundle.

Testes de componentes (Vitest):

```bash
cd frontend
npx ng test --watch=false
```

## Exemplos para demonstração

A pasta [`exemplos/`](exemplos/) tem NF-e **sintéticas**, sem nenhum dado fiscal real, assinadas com um **certificado de teste autoassinado, sem valor fiscal**. São 5 válidas e 8 com erros, para enviar pela interface. Cada arquivo leva só o certificado público; a chave privada nunca é gravada. Para gerar de novo:

```bash
cd backend
./mvnw test -Dtest=ExemplosAssinadosTest -Dexemplos.saida=../exemplos
```

## Certificado de teste (assinatura, rejeição 297)

Os testes **não** dependem de nenhum certificado da sua máquina: cada execução gera o seu, numa pasta temporária, com o `keytool` do próprio JDK.

Para assinar XMLs fora dos testes, gere um certificado **autoassinado** de teste:

```powershell
.\scripts\gerar-certificado-teste.ps1
```

```bash
./scripts/gerar-certificado-teste.sh
```

- O certificado vai para `certificados/certificado-teste.p12`, pasta que está no `.gitignore`.
- A senha vem da variável `SENHA_CERTIFICADO_TESTE`. Sem ela, o script gera uma senha aleatória e a mostra uma única vez.
- O certificado não é ICP-Brasil e não tem valor fiscal. Serve apenas para NF-e sintéticas em homologação.

## Licença

[MIT](LICENSE) © 2026 Claudio Pablo Silva.
