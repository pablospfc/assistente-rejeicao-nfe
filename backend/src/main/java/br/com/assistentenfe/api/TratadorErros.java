package br.com.assistentenfe.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/** Erros da requisição (não da NF-e) com mensagens em português. */
@RestControllerAdvice
public class TratadorErros {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail arquivoGrandeDemais() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE,
                "O arquivo excede o tamanho máximo de 1 MB.");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    ProblemDetail arquivoAusente() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Envie o XML da NF-e no campo \"arquivo\".");
    }
}
