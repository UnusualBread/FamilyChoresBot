package ru.unbread.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.unbread.dao.AppUserDAO;
import ru.unbread.dao.RawDataDAO;
import ru.unbread.entity.AppUser;
import ru.unbread.entity.RawData;
import ru.unbread.service.MainService;
import ru.unbread.service.ProducerService;
import ru.unbread.service.enums.ServiceCommand;

import static ru.unbread.enums.UserState.BASIC_STATE;
import static ru.unbread.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static ru.unbread.service.enums.ServiceCommand.CANCEL;
import static ru.unbread.service.enums.ServiceCommand.HELP;
import static ru.unbread.service.enums.ServiceCommand.REGISTRATION;
import static ru.unbread.service.enums.ServiceCommand.START;

@Log4j
@RequiredArgsConstructor
@Service
public class MainServiceImpl implements MainService {

    private final RawDataDAO rawDataDAO;

    private final ProducerService producerService;

    private final AppUserDAO appUserDAO;

    @Transactional
    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        var serviceCommand = ServiceCommand.fromValue(text);
        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            //TODO добавить обработку емейла
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка. Введите /cancel и попробуйте ещё раз";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowedToSendContent(chatId, appUser)) {
            return;
        }

        //TODO добавить сохранение документа
        var answer = "Документ успешно загружен. Ссылка для скачивания: http://test.ru/get-doc/777";
        sendAnswer(answer, chatId);
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowedToSendContent(chatId, appUser)) {
            return;
        }

        //TODO добавить сохранение фото
        var answer = "Фото успешно загружено. Ссылка для скачивания: http://test.ru/get-photo/777";
        sendAnswer(answer, chatId);
    }

    private boolean isNotAllowedToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if (!appUser.getIsActive()) {
            var error = "Зарегистрируйтесь или активируйте свою учётною запись для загрузки фото или файлов";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Отмените текущую команду с помощью /cancel для отправки фото или файлов";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        var serviceCommand = ServiceCommand.fromValue(cmd);
        if (REGISTRATION.equals(serviceCommand)) {
            //TODO добавить регистрацию
            return "Временно недоступно";
        } else if (HELP.equals(serviceCommand)) {
            return help();
        } else if (START.equals(serviceCommand)) {
            return "Приветствую! Чтобы посмотреть список доступных команд введите /help";
        } else {
            return "Неизвестная команда. Чтобы посмотреть список доступных команд введите /help";
        }
    }

    private String help() {
        return """
                Список доступных команд:
                /cancel - отмена выполнения текущей команды;
                /registration - регистрация пользователя.""";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Команда отменена";
    }

    private AppUser findOrSaveAppUser(Update update) {
        var telegramUser = update.getMessage().getFrom();
        var appUserOpt = appUserDAO.findByTelegramUserId(telegramUser.getId());
        if (appUserOpt.isEmpty()) {
            var transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO изменить значение по умолчанию после добавления регистрации
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return appUserOpt.get();
    }

    private void saveRawData(Update update) {
        var rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }
}
