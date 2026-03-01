package com.apetitto.apetittoerpbackend.erp.finance.service;

import com.apetitto.apetittoerpbackend.erp.finance.dto.PosSaleRequestDto;

public interface PosSaleService {
    void processSale(PosSaleRequestDto request);
}
