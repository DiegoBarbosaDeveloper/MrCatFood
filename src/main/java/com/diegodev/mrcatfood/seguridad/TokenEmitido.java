package com.diegodev.mrcatfood.seguridad;

import java.time.Instant;

/** Token recién emitido junto con los datos que el cliente necesita para gestionarlo. */
public record TokenEmitido(String valor, Instant expiraEn, String jti) {}
