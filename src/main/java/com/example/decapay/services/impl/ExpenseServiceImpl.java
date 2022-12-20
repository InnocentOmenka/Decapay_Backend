package com.example.decapay.services.impl;

import com.example.decapay.exceptions.ResourceNotFoundException;
import com.example.decapay.models.Expense;
import com.example.decapay.models.LineItem;
import com.example.decapay.pojos.expenseDto.ExpenseRequestDto;
import com.example.decapay.pojos.expenseDto.ExpenseResponseDto;
import com.example.decapay.repositories.ExpenseRepository;
import com.example.decapay.repositories.LineItemRepository;
import com.example.decapay.services.ExpenseService;
import com.example.decapay.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final LineItemRepository lineItemRepository;


    @Override
    public ResponseEntity<ExpenseResponseDto> createExpense(ExpenseRequestDto expenseRequestDto, Long lineId) {
        //get total amount from the line item repo and assign to a variable
        LineItem lineItem = lineItemRepository.findById(lineId).orElseThrow(() -> new ResourceNotFoundException("Line item does not exist", HttpStatus.NOT_FOUND, "Please select a valid line item"));
        BigDecimal totalAmount = lineItem.getTotalAmountSpent();

        //create new expense in the repo
        Expense expense = new Expense();
        expense.setAmount(expenseRequestDto.getAmount());
        expense.setDescription(expenseRequestDto.getDescription());
        expenseRepository.save(expense);

        //set new total by adding currently logged expense to the existing expense in the lineitem repo
        BigDecimal newTotal = totalAmount.add(expense.getAmount());
        lineItem.setTotalAmountSpent(newTotal);
        lineItemRepository.save(lineItem);

        //set the response to the user
        ExpenseResponseDto expenseResponseDto = new ExpenseResponseDto();
        expenseResponseDto.setAmount(expense.getAmount());
        expenseResponseDto.setDescription(expense.getDescription());
        expenseResponseDto.setCreatedAt(expense.getCreatedAt());

        return ResponseEntity.ok(expenseResponseDto);
    }
}
