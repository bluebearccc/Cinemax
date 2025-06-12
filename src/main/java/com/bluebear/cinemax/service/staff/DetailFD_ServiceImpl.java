package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.repository.staff.Detail_FDRepository;
import com.bluebear.cinemax.entity.Detail_FD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetailFD_ServiceImpl implements DetaillFD_Service {

    @Autowired
    Detail_FDRepository detail_FDRepository;

    public List<Detail_FD> findByTheaterStockID(Integer id) {
        return detail_FDRepository.findAllByTheaterStock_TheaterStockId(id);
    }
}