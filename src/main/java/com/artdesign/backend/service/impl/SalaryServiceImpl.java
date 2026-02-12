package com.artdesign.backend.service.impl;

import com.artdesign.backend.entity.Salary;
import com.artdesign.backend.repository.SalaryRepository;
import com.artdesign.backend.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SalaryServiceImpl implements SalaryService {

    @Autowired
    private SalaryRepository salaryRepository;

    @Override
    public Salary findById(Long id) {
        return salaryRepository.findById(id).orElse(null);
    }

    @Override
    public Salary findByUserId(Long userId) {
        return salaryRepository.findByUserId(userId);
    }

    @Override
    public Salary save(Salary salary) {
        return salaryRepository.save(salary);
    }

    @Override
    public void deleteById(Long id) {
        salaryRepository.deleteById(id);
    }

    @Override
    public void deleteByUserId(Long userId) {
        salaryRepository.deleteByUserId(userId);
    }

}
