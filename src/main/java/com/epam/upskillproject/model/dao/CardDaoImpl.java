package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.init.PropertiesKeeper;
import com.epam.upskillproject.model.dao.queryhandlers.QueryExecutor;
import com.epam.upskillproject.model.dao.queryhandlers.constructors.CardQueryConstructor;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.OrderStrategy;
import com.epam.upskillproject.model.dto.Card;
import com.epam.upskillproject.model.dto.CardNetworkType;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.CardSortType;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.servlet.ServletException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigInteger;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Singleton
public class CardDaoImpl implements CardDao {

    private static final Logger logger = LogManager.getLogger(CardDaoImpl.class.getName());

    private static final int CVC_DIGITS = 3;
    private static final int EXP_PERIOD_YEARS = 3;
    private static final String ID_COLUMN_NAME = "ID";
    private static final String OWNER_COLUMN_NAME = "OWNER";
    private static final String ACCOUNT_COLUMN_NAME = "ACCOUNT";
    private static final String CVC_COLUMN_NAME = "CVC";
    private static final String EXPDATE_COLUMN_NAME = "EXPDATE";
    private static final String NETWORK_COLUMN_ALIAS = "networkName";
    private static final String STATUS_COLUMN_ALIAS = "statName";
    private static final String PASSWORD_HASH_ITERATIONS_PROP = "Pbkdf2PasswordHash.Iterations";
    private static final String PASSWORD_HASH_ALGORITHM_PROP = "Pbkdf2PasswordHash.Algorithm";
    private static final String PASSWORD_HASH_KEY_SIZE_PROP = "Pbkdf2PasswordHash.KeySizeBytes";
    private static final String PASSWORD_HASH_SALT_SIZE_PROP = "Pbkdf2PasswordHash.SaltSizeBytes";

    private final PropertiesKeeper propertiesKeeper;
    private final CardQueryConstructor queryConstructor;
    private final QueryExecutor queryExecutor;
    private final OrderStrategy orderStrategy;
    private final Pbkdf2PasswordHash passwordHash;

    @Inject
    public CardDaoImpl(PropertiesKeeper propertiesKeeper, CardQueryConstructor queryConstructor,
                       QueryExecutor queryExecutor, @Named("cardOrder") OrderStrategy orderStrategy,
                       Pbkdf2PasswordHash passwordHash) {
        this.propertiesKeeper = propertiesKeeper;
        this.queryConstructor = queryConstructor;
        this.queryExecutor = queryExecutor;
        this.orderStrategy = orderStrategy;
        this.passwordHash = passwordHash;
    }

    @Override
    public Optional<Card> getSingleCardById(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.singleById();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        Card card = null;
        if (rs != null && rs.next()) {
            card = buildInstance(rs);
            rs.close();
        }
        return (card != null) ? Optional.of(card) : Optional.empty();
    }

    @Override
    public Optional<Card> getSingleCardByIdAndOwner(BigInteger cardId, BigInteger ownerId) throws SQLException {
        String rawQuery = queryConstructor.singleByIdAndOwner();
        ResultSet rs = queryExecutor.execute(rawQuery, cardId, ownerId);
        Card card = null;
        if (rs != null && rs.next()) {
            card = buildInstance(rs);
            rs.close();
        }
        return (card != null) ? Optional.of(card) : Optional.empty();
    }

    @Override
    public List<Card> getAllCards(CardSortType sortType) throws SQLException {
        String rawQuery = queryConstructor.all();
        ResultSet rs = queryExecutor.execute(String.format(rawQuery, orderStrategy.getOrder(sortType)));
        List<Card> cards = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                cards.add(buildInstance(rs));
            }
            rs.close();
        }
        return cards;
    }

    @Override
    public List<Card> getCardsPage(int limit, int offset, CardSortType sortType) throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        String rawQuery = queryConstructor.page();
        ResultSet rs = queryExecutor.execute(String.format(rawQuery, orderStrategy.getOrder(sortType)), limit, offset);
        List<Card> cards = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                cards.add(buildInstance(rs));
            }
            rs.close();
        }
        return cards;
    }

    @Override
    public List<Card> getCardsByOwner(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.byOwner();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        List<Card> cards = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                cards.add(buildInstance(rs));
            }
            rs.close();
        }
        return cards;
    }

    @Override
    public List<Card> getCardsByAccount(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.byAccount();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        List<Card> cards = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                cards.add(buildInstance(rs));
            }
            rs.close();
        }
        return cards;
    }

    @Override
    public int countCards() throws SQLException {
        String query = queryConstructor.countAll();
        ResultSet rs = queryExecutor.execute(query);
        int amount = 0;
        if (rs != null && rs.next()) {
            amount = rs.getInt(1);
            rs.close();
        }
        return amount;
    }

    @Override
    public int countCardsByOwner(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.countByOwner();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        int amount = 0;
        if (rs != null && rs.next()) {
            amount = rs.getInt(1);
            rs.close();
        }
        return amount;
    }

    @Override
    public int countCardsByAccount(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.countByAccount();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        int amount = 0;
        if (rs != null && rs.next()) {
            amount = rs.getInt(1);
            rs.close();
        }
        return amount;
    }

    @Override
    public synchronized boolean updateCardStatus(BigInteger id, StatusType statusType) throws SQLException {
        String rawQuery = queryConstructor.updateStatus();
        int result = queryExecutor.executeUpdate(rawQuery, statusType, id);
        Optional<StatusType> updatedStatusType = getCardStatus(id);
        return (result != 0 && updatedStatusType.isPresent() && updatedStatusType.get().equals(statusType));
    }

    @Override
    public Optional<StatusType> getCardStatus(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.status();
        ResultSet rs = queryExecutor.execute(rawQuery, id);
        StatusType statusType = null;
        if (rs != null && rs.next()) {
            try {
                statusType = StatusType.valueOf(rs.getString(1).toUpperCase());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            } finally {
                rs.close();
            }
        }
        return (statusType != null) ? Optional.of(statusType) : Optional.empty();
    }

    @Override
    public Optional<BigInteger> getCardAccountId(BigInteger cardId) throws SQLException {
        String rawQuery = queryConstructor.accountId();
        ResultSet rs = queryExecutor.execute(rawQuery, cardId);
        BigInteger accountId = null;
        if (rs != null && rs.next()) {
            try {
                accountId = new BigInteger(rs.getString(1));
            } catch (NumberFormatException e) {
                return Optional.empty();
            } finally {
                rs.close();
            }
        }
        return (accountId != null) ? Optional.of(accountId) : Optional.empty();
    }

    // should use the method as a part of a transaction (with payment performing)
    @Override
    public synchronized String addCard(Connection conn, BigInteger ownerId, BigInteger accountId,
                                       CardNetworkType cardNetworkType) throws SQLException {
        String rawQuery = queryConstructor.add();
        String randomCvc = generateCvc();
        LocalDate now = LocalDate.now();
        int result = queryExecutor.executeUpdate(
                conn,
                rawQuery,
                ownerId,
                accountId,
                cardNetworkType.getId(),
                passwordHash.generate(randomCvc.toCharArray()),
                StatusType.ACTIVE.getId(),
                Date.valueOf(now.plusYears(EXP_PERIOD_YEARS))
        );
        return (result != 0) ? randomCvc : "";
    }

    @Override
    public synchronized boolean deleteCardById(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.delSingleById();
        queryExecutor.executeUpdate(rawQuery, id);
        return getSingleCardById(id).isEmpty();
    }

    @Override
    public synchronized boolean deleteCardByIdAndOwner(BigInteger cardId, BigInteger ownerId) throws SQLException {
        String rawQuery = queryConstructor.delSingleByIdAndOwner();
        int result = queryExecutor.executeUpdate(rawQuery, cardId, ownerId);
        return (result != 0 && getSingleCardByIdAndOwner(cardId, ownerId).isEmpty());
    }

    @Override
    public synchronized boolean deleteCardsByAccount(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.delByAccount();
        int result = queryExecutor.executeUpdate(rawQuery, id);
        return (result != 0 && getCardsByAccount(id).isEmpty());
    }

    // should use the method as a part of a transaction
    @Override
    public synchronized boolean deleteCardsByAccount(Connection conn, BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.delByAccount();
        int result = queryExecutor.executeUpdate(conn, rawQuery, id);
        return (result != 0);
    }

    @Override
    public synchronized boolean deleteCardsByOwner(BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.delByOwner();
        int result = queryExecutor.executeUpdate(rawQuery, id);
        return (result != 0 && getCardsByOwner(id).isEmpty());
    }

    // should use the method as a part of a transaction
    @Override
    public synchronized boolean deleteCardsByOwner(Connection conn, BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.delByOwner();
        int result = queryExecutor.executeUpdate(conn, rawQuery, id);
        return (result != 0);
    }

    private Card buildInstance(ResultSet rs) throws SQLException {
        try {
            return new Card(
                    new BigInteger(rs.getString(ID_COLUMN_NAME)),
                    new BigInteger(rs.getString(OWNER_COLUMN_NAME)),
                    new BigInteger(rs.getString(ACCOUNT_COLUMN_NAME)),
                    CardNetworkType.valueOf(rs.getString(NETWORK_COLUMN_ALIAS)),
                    rs.getString(CVC_COLUMN_NAME),
                    StatusType.valueOf(rs.getString(STATUS_COLUMN_ALIAS)),
                    rs.getDate(EXPDATE_COLUMN_NAME).toLocalDate()
            );
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARN, String.format("Invalid field values were obtained from database (%s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            throw new SQLException("Cannot create card instance (invalid field values were obtained from database)", e);
        }
    }

    private String generateCvc() {
        int random = (int) (Math.random() * Math.pow(10, CVC_DIGITS));
        return String.format("%03d", random);
    }

    @PostConstruct
    public void init() throws ServletException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PASSWORD_HASH_ITERATIONS_PROP, propertiesKeeper.getString(PASSWORD_HASH_ITERATIONS_PROP));
        parameters.put(PASSWORD_HASH_ALGORITHM_PROP, propertiesKeeper.getString(PASSWORD_HASH_ALGORITHM_PROP));
        parameters.put(PASSWORD_HASH_KEY_SIZE_PROP, propertiesKeeper.getString(PASSWORD_HASH_KEY_SIZE_PROP));
        parameters.put(PASSWORD_HASH_SALT_SIZE_PROP, propertiesKeeper.getString(PASSWORD_HASH_SALT_SIZE_PROP));
        passwordHash.initialize(parameters);
    }
}
