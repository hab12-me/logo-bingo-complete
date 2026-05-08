package com.logobing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;

@Data
@NoArgsConstructor
public class DepositRequestDto {
    @NotNull @Min(10) @Max(50000)
    private Double amount;
    @NotBlank @Size(min = 5, max = 50)
    private String transactionId;
    @NotBlank @Pattern(regexp = "^09[0-9]{8}$")
    private String senderPhoneNumber;
}
