package com.epam.upskillproject.controller.command;

import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.impl.CommandWrapper;
import com.epam.upskillproject.controller.command.impl.admin.*;
import com.epam.upskillproject.controller.command.impl.common.*;
import com.epam.upskillproject.controller.command.enumeration.EndpointEnum;
import com.epam.upskillproject.controller.command.enumeration.TargetType;
import com.epam.upskillproject.controller.command.impl.payservice.*;
import com.epam.upskillproject.exception.CommandNotFoundException;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ActionFactory {

    private static final Logger logger = LogManager.getLogger(ActionFactory.class.getName());
    private static final String BASE_PATH = "/controller";
    private static final String TARGET_PARAM = "command";

    private final EnumSet<EndpointEnum> endpointEnums = EnumSet.allOf(EndpointEnum.class);
    private final CommandRouteFactory routeFactory;
    private final ParamReader paramReader;
    private final Map<CommandRoute, Command> dispatcherMap = new HashMap<>();

    private final AccountListCommand accountListCommand;
    private final AccountBlockCommand accountBlockCommand;
    private final AccountActivateCommand accountActivateCommand;
    private final AccountDeleteCommand accountDeleteCommand;
    private final CardListCommand cardListCommand;
    private final CardBlockCommand cardBlockCommand;
    private final CardActivateCommand cardActivateCommand;
    private final CardDeleteCommand cardDeleteCommand;
    private final AdminListCommand adminListCommand;
    private final AdminProfileCommand adminProfileCommand;
    private final AdminUpdateCommand adminUpdateCommand;
    private final AdminDeleteCommand adminDeleteCommand;
    private final CustomerListCommand customerListCommand;
    private final CustomerProfileCommand customerProfileCommand;
    private final CustomerUpdateCommand customerUpdateCommand;
    private final CustomerDeleteCommand customerDeleteCommand;
    private final CustomerAccountsCommand customerAccountsCommand;
    private final PaymentListCommand paymentListCommand;
    private final SystemIncomeCommand systemIncomeCommand;
    private final MyAccountsCommand myAccountsCommand;
    private final MyAccountsServiceCommand myAccountsServiceCommand;
    private final BlockUserAccountCommand blockUserAccountCommand;
    private final DeleteUserAccountCommand deleteUserAccountCommand;
    private final AddUserAccountCommand addUserAccountCommand;
    private final BlockUserCardCommand blockUserCardCommand;
    private final DeleteUserCardCommand deleteUserCardCommand;
    private final AddUserCardCommand addUserCardCommand;
    private final TopupUserAccountCommand topupUserAccountCommand;
    private final PerformPaymentCommand performPaymentCommand;
    private final MyAccountIncomingCommand myAccountIncomingCommand;
    private final MyAccountOutgoingCommand myAccountOutgoingCommand;
    private final IndexCommand indexCommand;
    private final LogoutCommand logoutCommand;
    private final LoginErrorCommand loginErrorCommand;
    private final SignUpCommand signUpCommand;
    private final UserProfileCommand userProfileCommand;
    private final UserUpdateCommand userUpdateCommand;
    private final ChangeLangCommand changeLangCommand;

    @Inject
    public ActionFactory(CommandRouteFactory routeFactory,
                         ParamReader paramReader,
                         AccountListCommand accountListCommand,
                         AccountBlockCommand accountBlockCommand,
                         AccountActivateCommand accountActivateCommand,
                         AccountDeleteCommand accountDeleteCommand,
                         CardListCommand cardListCommand,
                         CardBlockCommand cardBlockCommand,
                         CardActivateCommand cardActivateCommand,
                         CardDeleteCommand cardDeleteCommand,
                         AdminListCommand adminListCommand,
                         AdminProfileCommand adminProfileCommand,
                         AdminUpdateCommand adminUpdateCommand,
                         AdminDeleteCommand adminDeleteCommand,
                         CustomerListCommand customerListCommand,
                         CustomerProfileCommand customerProfileCommand,
                         CustomerUpdateCommand customerUpdateCommand,
                         CustomerDeleteCommand customerDeleteCommand,
                         CustomerAccountsCommand customerAccountsCommand,
                         PaymentListCommand paymentListCommand,
                         SystemIncomeCommand systemIncomeCommand,
                         MyAccountsCommand myAccountsCommand,
                         MyAccountsServiceCommand myAccountsServiceCommand,
                         BlockUserAccountCommand blockUserAccountCommand,
                         DeleteUserAccountCommand deleteUserAccountCommand,
                         AddUserAccountCommand addUserAccountCommand,
                         BlockUserCardCommand blockUserCardCommand,
                         DeleteUserCardCommand deleteUserCardCommand,
                         AddUserCardCommand addUserCardCommand,
                         TopupUserAccountCommand topupUserAccountCommand,
                         PerformPaymentCommand performPaymentCommand,
                         MyAccountIncomingCommand myAccountIncomingCommand,
                         MyAccountOutgoingCommand myAccountOutgoingCommand,
                         IndexCommand indexCommand,
                         LogoutCommand logoutCommand,
                         LoginErrorCommand loginErrorCommand,
                         SignUpCommand signUpCommand,
                         UserProfileCommand userProfileCommand,
                         UserUpdateCommand userUpdateCommand,
                         ChangeLangCommand changeLangCommand) {
        this.routeFactory = routeFactory;
        this.paramReader = paramReader;
        this.accountListCommand = accountListCommand;
        this.accountBlockCommand = accountBlockCommand;
        this.accountActivateCommand = accountActivateCommand;
        this.accountDeleteCommand = accountDeleteCommand;
        this.cardListCommand = cardListCommand;
        this.cardBlockCommand = cardBlockCommand;
        this.cardActivateCommand = cardActivateCommand;
        this.cardDeleteCommand = cardDeleteCommand;
        this.adminListCommand = adminListCommand;
        this.adminProfileCommand = adminProfileCommand;
        this.adminUpdateCommand = adminUpdateCommand;
        this.adminDeleteCommand = adminDeleteCommand;
        this.customerListCommand = customerListCommand;
        this.customerProfileCommand = customerProfileCommand;
        this.customerUpdateCommand = customerUpdateCommand;
        this.customerDeleteCommand = customerDeleteCommand;
        this.customerAccountsCommand = customerAccountsCommand;
        this.paymentListCommand = paymentListCommand;
        this.systemIncomeCommand = systemIncomeCommand;
        this.myAccountsCommand = myAccountsCommand;
        this.myAccountsServiceCommand = myAccountsServiceCommand;
        this.blockUserAccountCommand = blockUserAccountCommand;
        this.deleteUserAccountCommand = deleteUserAccountCommand;
        this.addUserAccountCommand = addUserAccountCommand;
        this.blockUserCardCommand = blockUserCardCommand;
        this.deleteUserCardCommand = deleteUserCardCommand;
        this.addUserCardCommand = addUserCardCommand;
        this.topupUserAccountCommand = topupUserAccountCommand;
        this.performPaymentCommand = performPaymentCommand;
        this.myAccountIncomingCommand = myAccountIncomingCommand;
        this.myAccountOutgoingCommand = myAccountOutgoingCommand;
        this.indexCommand = indexCommand;
        this.logoutCommand = logoutCommand;
        this.loginErrorCommand = loginErrorCommand;
        this.signUpCommand = signUpCommand;
        this.userProfileCommand = userProfileCommand;
        this.userUpdateCommand = userUpdateCommand;
        this.changeLangCommand = changeLangCommand;
    }

    public Command produce(HttpServletRequest req, HttpServletResponse resp) throws CommandNotFoundException {
        if (req != null && resp != null) {
            try {
                Optional<TargetType> target = paramReader.readTargetTypeParamOrAttr(req, TARGET_PARAM);
                if (target.isPresent()) {
                    String uri = req.getRequestURI().replaceFirst(BASE_PATH, "");
                    EndpointEnum endpoint = endpointEnums.stream()
                            .filter(ep -> uri.matches(ep.getPattern()))
                            .findAny()
                            .orElseThrow(IllegalArgumentException::new);
                    CommandRoute commandRoute = routeFactory.produce(endpoint, target.get());
                    return dispatcherMap.get(commandRoute);
                }
            } catch (IllegalArgumentException e) {
                logger.log(Level.INFO, String.format("Cannot resolve request (command not found), uri: %s",
                        req.getRequestURI()));
            }
        } else {
            logger.log(Level.INFO, String.format("Bad parameters passed - request is present: %s, response is " +
                            "present: %s", (req != null), (resp != null)));
        }
        throw new CommandNotFoundException("Command not found");
    }

    @PostConstruct
    public void init() {
        // Command-endpoint-target associations
        addCommand(EndpointEnum.ACCOUNT_LIST, TargetType.GET, accountListCommand);
        addCommand(EndpointEnum.ACCOUNT_LIST, TargetType.BLOCK_ACCOUNT, accountBlockCommand, accountListCommand);
        addCommand(EndpointEnum.ACCOUNT_LIST, TargetType.ACTIVATE_ACCOUNT, accountActivateCommand, accountListCommand);
        addCommand(EndpointEnum.CARD_LIST, TargetType.GET, cardListCommand);
        addCommand(EndpointEnum.CARD_LIST, TargetType.BLOCK_CARD, cardBlockCommand, cardListCommand);
        addCommand(EndpointEnum.CARD_LIST, TargetType.ACTIVATE_CARD, cardActivateCommand, cardListCommand);
        addCommand(EndpointEnum.ADMIN_LIST, TargetType.GET, adminListCommand);
        addCommand(EndpointEnum.ADMIN_PROFILE, TargetType.GET, adminProfileCommand);
        addCommand(EndpointEnum.ADMIN_PROFILE, TargetType.UPDATE_PERSON, adminUpdateCommand, adminProfileCommand);
        addCommand(EndpointEnum.ADMIN_PROFILE, TargetType.DELETE_PERSON, adminDeleteCommand, adminProfileCommand);
        addCommand(EndpointEnum.CUSTOMER_LIST, TargetType.GET, customerListCommand);
        addCommand(EndpointEnum.CUSTOMER_PROFILE, TargetType.GET, customerProfileCommand);
        addCommand(EndpointEnum.CUSTOMER_PROFILE, TargetType.UPDATE_PERSON, customerUpdateCommand, customerProfileCommand);
        addCommand(EndpointEnum.CUSTOMER_PROFILE, TargetType.DELETE_PERSON, customerDeleteCommand, customerProfileCommand);
        addCommand(EndpointEnum.CUSTOMER_ACCOUNTS, TargetType.GET, customerAccountsCommand);
        addCommand(EndpointEnum.CUSTOMER_ACCOUNTS, TargetType.BLOCK_ACCOUNT, accountBlockCommand, customerAccountsCommand);
        addCommand(EndpointEnum.CUSTOMER_ACCOUNTS, TargetType.ACTIVATE_ACCOUNT, accountActivateCommand, customerAccountsCommand);
        addCommand(EndpointEnum.CUSTOMER_ACCOUNTS, TargetType.DELETE_ACCOUNT, accountDeleteCommand, customerAccountsCommand);
        addCommand(EndpointEnum.CUSTOMER_ACCOUNTS, TargetType.BLOCK_CARD, cardBlockCommand, customerAccountsCommand);
        addCommand(EndpointEnum.CUSTOMER_ACCOUNTS, TargetType.ACTIVATE_CARD, cardActivateCommand, customerAccountsCommand);
        addCommand(EndpointEnum.CUSTOMER_ACCOUNTS, TargetType.DELETE_CARD, cardDeleteCommand, customerAccountsCommand);
        addCommand(EndpointEnum.PAYMENT_LIST, TargetType.GET, paymentListCommand);
        addCommand(EndpointEnum.SYSTEM_INCOME, TargetType.GET, systemIncomeCommand);
        addCommand(EndpointEnum.PAYSERVICE_ACCOUNTS, TargetType.GET, myAccountsCommand);
        addCommand(EndpointEnum.PAYSERVICE_ACCOUNTS, TargetType.ADD_ACCOUNT, addUserAccountCommand, myAccountsCommand);
        addCommand(EndpointEnum.PAYSERVICE_ACCOUNTS, TargetType.INCREASE_ACCOUNT, topupUserAccountCommand, myAccountsCommand);
        addCommand(EndpointEnum.PAYSERVICE_SERVICE, TargetType.GET, myAccountsServiceCommand);
        addCommand(EndpointEnum.PAYSERVICE_SERVICE, TargetType.BLOCK_ACCOUNT, blockUserAccountCommand, myAccountsServiceCommand);
        addCommand(EndpointEnum.PAYSERVICE_SERVICE, TargetType.DELETE_ACCOUNT, deleteUserAccountCommand, myAccountsServiceCommand);
        addCommand(EndpointEnum.PAYSERVICE_SERVICE, TargetType.BLOCK_CARD, blockUserCardCommand, myAccountsServiceCommand);
        addCommand(EndpointEnum.PAYSERVICE_SERVICE, TargetType.DELETE_CARD, deleteUserCardCommand, myAccountsServiceCommand);
        addCommand(EndpointEnum.PAYSERVICE_SERVICE, TargetType.ADD_CARD, addUserCardCommand, myAccountsServiceCommand);
        addCommand(EndpointEnum.PAYSERVICE_SERVICE, TargetType.PAYMENT, performPaymentCommand, myAccountsServiceCommand);
        addCommand(EndpointEnum.PAYSERVICE_INCOMING, TargetType.GET, myAccountIncomingCommand);
        addCommand(EndpointEnum.PAYSERVICE_OUTGOING, TargetType.GET, myAccountOutgoingCommand);
        addCommand(EndpointEnum.INDEX, TargetType.GET, indexCommand);
        addCommand(EndpointEnum.LOGOUT, TargetType.GET, logoutCommand);
        addCommand(EndpointEnum.LOGIN_ERROR, TargetType.GET, loginErrorCommand);
        addCommand(EndpointEnum.SIGN_UP, TargetType.ADD_PERSON, signUpCommand);
        addCommand(EndpointEnum.USER_PROFILE, TargetType.GET, userProfileCommand);
        addCommand(EndpointEnum.USER_PROFILE, TargetType.UPDATE_PERSON, userUpdateCommand, userProfileCommand);
        addCommand(EndpointEnum.CHANGE_LANG, TargetType.GET, changeLangCommand);
    }

    private void addCommand(EndpointEnum endpoint, TargetType target, Command... commands) {
        Command builtCommand = commands[0];
        if (commands.length > 1) {
            for (int i = 1; i < commands.length; i++) {
                builtCommand = new CommandWrapper(builtCommand, commands[i]);
            }
        }
        dispatcherMap.put(routeFactory.produce(endpoint, target), builtCommand);
    }

}
