package com.meet.cleaner_bot.service;

import com.meet.cleaner_bot.config.BotConfig;
import com.meet.cleaner_bot.models.History;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final JSONArray JSON;

    public TelegramBot(BotConfig config) throws IOException {
        this.config = config;
        JSON = new JSONArray(new String(Files.readAllBytes(Paths.get(config.getPathData())), StandardCharsets.UTF_8));
    }

    @Override
    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();

        if (message != null &&
                message.hasText() &&
                message.getReplyToMessage() == null &&
                message.getText().contains("@")
        )
        {

            Date messageDate = new Date(message.getDate() * 1000L); // Convert seconds to milliseconds
            LocalDate date = messageDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String username = message.getText().split("@")[1].trim();
            History history = new History(message.getMessageId(), username, date);

            deleteOldUserMessage(username, message.getChatId());
            deleteOldMessage(LocalDate.now(), message.getChatId());


            JSON.put(new JSONObject(history));

            try (FileWriter fileWriter = new FileWriter(config.getPathData()))
            {
                fileWriter.append(JSON.toString());
                System.out.println("Data has been written to data.json");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }

    }

    private void deleteOldUserMessage(String username, Long chatId)
    {
        for(int i = 0; i < JSON.length(); i++)
        {
            try{
                String tempUsername = JSON.getJSONObject(i).getString("username");

                if(tempUsername.equals(username))
                {
                    Integer mesId = JSON.getJSONObject(i).getInt("messageId");

                    DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), mesId);

                    try {
                        execute(deleteMessage);
                    } catch (TelegramApiException e) {

                    }
                    JSON.remove(i);
                }
            }
            catch (Exception e){}
        }
    }

    private void deleteOldMessage(LocalDate date, Long chatId)
    {
        for(int i = 0; i < JSON.length(); i++)
        {
            try{
                LocalDate tempDate = LocalDate.parse(JSON.getJSONObject(i).getString("createdDate"));

                if(date.minusMonths(1).isAfter(tempDate) ||
                        date.minusMonths(1).isEqual(tempDate))
                {
                    Integer mesId = JSON.getJSONObject(i).getInt("messageId");

                    DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), mesId);

                    try {
                        execute(deleteMessage);
                    } catch (TelegramApiException e) {

                    }
                    JSON.remove(i);
                    i--;
                }
            }
            catch (Exception e){}
        }
    }

    @Override
    public String getBotUsername() {

        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
