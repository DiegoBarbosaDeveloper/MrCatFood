package com.diegodev.mrcatfood.usuario;

import java.time.Instant;

public record TokenResponse(String token, String tipo, Instant expiraEn) {}
