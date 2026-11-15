#!/usr/bin/env sh
# Gera um certificado AUTOASSINADO de teste (RSA 2048) para assinar NF-e sintéticas.
# O arquivo fica em certificados/ (fora do git). Não é um certificado ICP-Brasil e não tem valor fiscal.
#
# Uso:  ./scripts/gerar-certificado-teste.sh
# Senha: defina SENHA_CERTIFICADO_TESTE antes; sem ela, uma senha aleatória é gerada e exibida.
set -eu

: "${JAVA_HOME:?Defina JAVA_HOME apontando para um JDK 21.}"
KEYTOOL="$JAVA_HOME/bin/keytool"

SENHA="${SENHA_CERTIFICADO_TESTE:-}"
if [ -z "$SENHA" ]; then
    SENHA=$(od -An -N16 -tx1 /dev/urandom | tr -d ' \n')
    echo "Senha gerada (guarde-a; ela não é gravada em arquivo): $SENHA"
fi

PASTA="$(dirname "$0")/../certificados"
mkdir -p "$PASTA"
ARQUIVO="$PASTA/certificado-teste.p12"
if [ -e "$ARQUIVO" ]; then
    echo "Já existe $ARQUIVO. Apague-o para gerar outro." >&2
    exit 1
fi

"$KEYTOOL" -genkeypair -alias nfe-teste -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -validity 3650 \
    -dname "CN=EMPRESA FICTICIA DE TESTES LTDA, OU=Certificado de teste autoassinado, O=Assistente de Rejeicoes NF-e, C=BR" \
    -storetype PKCS12 -keystore "$ARQUIVO" -storepass "$SENHA" -keypass "$SENHA"

echo "Certificado de teste gravado em $ARQUIVO"
