package ru.unbread.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.unbread.dao.*;
import ru.unbread.entity.*;
import ru.unbread.service.MainService;
import ru.unbread.service.ProducerService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static ru.unbread.enums.UserState.BASIC_STATE;
import static ru.unbread.service.enums.ServiceCommand.*;

@Log4j
@RequiredArgsConstructor
@Service
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProducerService producerService;
    private final AppUserDAO appUserDAO;
    private final GroupDAO groupDAO;
    private final MemberDAO memberDAO;
    private final ZoneDAO zoneDAO;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        var serviceCommand = fromValue(text);

        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text, update);
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка. Введите /cancel и попробуйте ещё раз";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    private String processServiceCommand(AppUser appUser, String text, Update update) {
        String cmd = normalizeCommand(text);
        var serviceCommand = fromValue(cmd);
        return switch (serviceCommand) {
            case HELP -> help();
            case START -> "Приветствую! Чтобы посмотреть список доступных команд введите /help";
            case CREATE_GROUP -> processCreateGroupCommand(update);
            case JOIN -> processJoinCommand(update);
            case LIST_MEMBERS -> processListMembersCommand(update);
            case ADD_ZONES -> processAddZoneCommand(update);
            case LIST_ZONES -> processListZonesCommand(update);
            case DUTY -> processDutyCommand(update);
            default -> "Неизвестная команда. Чтобы посмотреть список доступных команд введите /help";
        };
    }

    private String processDutyCommand(Update update) {
        return createDutySchedule(update);
    }

    public String createDutySchedule(Update update) {
        Group group = findOrCreateGroup(update);
        List<Member> members = memberDAO.findByGroupId(group.getId());
        List<Zone> zones = zoneDAO.findByGroup(group);

        if (members.isEmpty() || zones.isEmpty()) {
            return "Нет участников или зон для ротации";
        }

        int membersCount = members.size();
        long weeksSinceStart = ChronoUnit.WEEKS.between(group.getCreationDate().toLocalDate(), LocalDate.now());
        int orderNumber = (int) (weeksSinceStart % membersCount);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < zones.size(); i++) {
            Member member = members.get((i - orderNumber + membersCount) % membersCount);
            Zone zone = zones.get(i);
            sb.append(zone.getName())
                    .append(" — @")
                    .append(member.getUsername())
                    .append(" ")
                    .append(member.getFirstName() != null ? member.getFirstName() : "")
                    .append(" ")
                    .append(member.getLastName() != null ? member.getLastName() : "")
                    .append("\n");
        }

        return sb.toString();
    }

    private String processListZonesCommand(Update update) {
        Long chatId = update.getMessage().getChatId();
        Group group = groupDAO.findByChatId(chatId)
                .orElse(null);
        if (group == null) {
            return "Группа не найдена";
        }
        List<Zone> zones = zoneDAO.findByGroup(group);
        StringBuilder sb = new StringBuilder("Зоны группы:\n");
        zones.forEach(z ->
                sb
                        .append(z.getName())
                        .append("\n")
        );
        return sb.toString();
    }

    private String processAddZoneCommand(Update update) {
        List<String> input = Arrays.stream(update.getMessage().getText().split(" "))
                .skip(1)
                .filter(s -> !s.isBlank())
                .toList();
        input.forEach(z -> findOrCreateZone(z, update));
        return "Зоны добавлены в группу";
    }

    private Zone findOrCreateZone(String zoneName, Update update) {
        Group group = findOrCreateGroup(update);
        return zoneDAO.findByNameAndGroup(zoneName, Optional.ofNullable(group))
                .orElseGet(() -> {
                    Zone zone = Zone.builder()
                            .name(zoneName)
                            .group(group)
                            .isActive(true)
                            .build();
                    return zoneDAO.save(zone);
                });
    }

    private String processListMembersCommand(Update update) {
        Long chatId = update.getMessage().getChatId();
        Group group = groupDAO.findByChatId(chatId)
                .orElse(null);
        if (group == null) {
            return "Группа не найдена";
        }
        List<Member> members = memberDAO.findByGroup(group);
        StringBuilder sb = new StringBuilder("Участники группы:\n");
        members.forEach(m ->
                sb
                        .append("@")
                        .append(m.getUsername())
                        .append(" ")
                        .append(m.getFirstName() != null ? m.getFirstName() : "")
                        .append(" ")
                        .append(m.getLastName() != null ? m.getLastName() : "")
                        .append("\n")
        );
        return sb.toString();
    }

    private String processCreateGroupCommand(Update update) {
        Group group = findOrCreateGroup(update);
        //List<Member> members = processAddMembers(update);
        return """
                Группа %s создана
                Для вступления в группу, каждому необходимо отправить /join, чтобы вступить в неё"""
                .formatted(group.getName());
    }

    private String processJoinCommand(Update update) {
        Member member = findOrCreateMember(update);
        return "@" + member.getUsername() + " добавлен в группу";
    }

    private Member findOrCreateMember(Update update) {
        User telegramUser = update.getMessage().getFrom();
        Group group = findOrCreateGroup(update);
        return memberDAO.findByTelegramUserIdAndGroupId(telegramUser.getId(), group.getId())
                .orElseGet(() -> {
                    Member member = Member.builder()
                            .telegramUserId(telegramUser.getId())
                            .firstName(telegramUser.getFirstName())
                            .lastName(telegramUser.getLastName())
                            .username(telegramUser.getUserName())
                            .isActive(true)
                            .group(group)
                            .build();
                    return memberDAO.save(member);
                });
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

    private Group findOrCreateGroup(Update update) {
        Long chatId = update.getMessage().getChatId();
        String groupName = update.getMessage().getChat().getTitle();
        return groupDAO.findByChatId(update.getMessage().getChatId())
                .orElseGet(() -> {
                    Group group = Group.builder()
                            .chatId(chatId)
                            .name(groupName)
                            .isActive(true)
                            .build();
                    return groupDAO.save(group);
                });
    }

    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .username(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();
            return appUserDAO.save(transientAppUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
                .event(update)
                .build();
        rawDataDAO.save(rawData);
    }

    private String normalizeCommand(String cmd) {
        int index = cmd.indexOf("@");
        if (index != -1) {
            cmd = cmd.substring(0, index);
        }
        return cmd;
    }

    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }
}
