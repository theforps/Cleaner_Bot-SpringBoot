package com.meet.cleaner_bot.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class History {

    private int messageId;
    private String username;
    private LocalDate createdDate;
}
