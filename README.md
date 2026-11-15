# Assistente de Rejeições da NF-e

Aplicação que valida o XML de uma NF-e (modelo 55, leiaute 4.00) **antes da transmissão** e explica, em português, por que ela seria rejeitada e como corrigir. Projeto de TCC; a validação é 100% offline e o XML enviado não é gravado.

> README inicial. A versão completa entra na Fase 7.

## Backend

Requer JDK 21 (`JAVA_HOME`). O Maven vem pelo wrapper.

```bash
cd backend
./mvnw test
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
