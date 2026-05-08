package com.logobing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;

@Data
@NoArgsConstructor
public class WithdrawRequestDto {
    @NotNull @Min(10) @Max(50000)
    private Double amount;
    @NotBlank @Pattern(regexp = "^09[0-9]{8}$")
    private String recipientPhoneNumber;
    @NotBlank @Size(min = 2, max = 100)
    private String recipientName;
}
