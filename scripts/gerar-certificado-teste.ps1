# Gera um certificado AUTOASSINADO de teste (RSA 2048) para assinar NF-e sintéticas.
# O arquivo fica em certificados/ (fora do git). Não é um certificado ICP-Brasil e não tem valor fiscal.
#
# Uso:  .\scripts\gerar-certificado-teste.ps1
# Senha: defina $env:SENHA_CERTIFICADO_TESTE antes; sem ela, uma senha aleatória é gerada e exibida.

$ErrorActionPreference = 'Stop'

if (-not $env:JAVA_HOME) { throw 'Defina JAVA_HOME apontando para um JDK 21.' }
$keytool = Join-Path $env:JAVA_HOME 'bin\keytool.exe'

$senha = $env:SENHA_CERTIFICADO_TESTE
if (-not $senha) {
    $senha = [guid]::NewGuid().ToString('N')
    Write-Host "Senha gerada (guarde-a; ela não é gravada em arquivo): $senha"
}

$pasta = Join-Path $PSScriptRoot '..\certificados'
New-Item -ItemType Directory -Force $pasta | Out-Null
$arquivo = Join-Path $pasta 'certificado-teste.p12'
if (Test-Path $arquivo) { throw "Já existe $arquivo. Apague-o para gerar outro." }

& $keytool -genkeypair -alias nfe-teste -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -validity 3650 `
    -dname 'CN=EMPRESA FICTICIA DE TESTES LTDA, OU=Certificado de teste autoassinado, O=Assistente de Rejeicoes NF-e, C=BR' `
    -storetype PKCS12 -keystore $arquivo -storepass $senha -keypass $senha
if ($LASTEXITCODE -ne 0) { throw 'keytool falhou.' }

Write-Host "Certificado de teste gravado em $arquivo"
