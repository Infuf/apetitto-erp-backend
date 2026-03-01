package com.apetitto.apetittoerpbackend.erp.finance.controller;

import com.apetitto.apetittoerpbackend.erp.finance.controller.api.PosApi;
import com.apetitto.apetittoerpbackend.erp.finance.dto.PosSaleRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.service.PosSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PosController implements PosApi {
    private final PosSaleService posSaleService;

    @Override
    public ResponseEntity<Void> processSale(PosSaleRequestDto request) {
        posSaleService.processSale(request);
        return ResponseEntity.ok().build();
    }
}
