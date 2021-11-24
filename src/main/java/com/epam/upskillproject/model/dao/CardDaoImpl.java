package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.exceptions.CustomSQLCode;
import com.epam.upskillproject.util.init.PropertiesKeeper;
import com.epam.upskillproject.model.dao.queryhandlers.QueryExecutor;
import com.epam.upskillproject.model.dao.queryhandlers.constructors.CardQueryConstructor;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.OrderStrategy;
import com.epam.upskillproject.model.dto.Card;
import com.epam.upskillproject.model.dto.CardNetworkType;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.CardSortType;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.servlet.ServletException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.sql.DataSource;
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
    private static final String INVALID_PARAM_SQLSTATE = "22023";
    private static final String PASSWORD_HASH_ITERATIONS_PROP = "Pbkdf2PasswordHash.Iterations";
    private static final String PASSWORD_HASH_ALGORITHM_PROP = "Pbkdf2PasswordHash.Algorithm";
    private static final String PASSWORD_HASH_KEY_SIZE_PROP = "Pbkdf2PasswordHash.KeySizeBytes";
    private static final String PASSWORD_HASH_SALT_SIZE_PROP = "Pbkdf2PasswordHash.SaltSizeBytes";

    @Resource(lookup = "java:global/customProjectDB")
    private DataSource dataSource;
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
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.singleById();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        return retrieveCard(conn, rs);
    }

    @Override
    public Optional<Card> getSingleCardByIdAndOwner(BigInteger cardId, BigInteger ownerId) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.singleByIdAndOwner();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, cardId, ownerId);
        return retrieveCard(conn, rs);
    }

    @Override
    public List<Card> getAllCards(CardSortType sortType) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.all();
        String query = String.format(rawQuery, orderStrategy.getOrder(sortType));
        ResultSet rs = queryExecutor.execute(conn, query);
        return retrieveCardList(conn, rs);
    }

    @Override
    public List<Card> getCardsPage(int limit, int offset, CardSortType sortType) throws SQLException {
        if (limit < 1) {
            return new ArrayList<>();
        }
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.page();
        String query = String.format(rawQuery, orderStrategy.getOrder(sortType));
        ResultSet rs = queryExecutor.execute(conn, query, limit, offset);
        return retrieveCardList(conn, rs);
    }

    @Override
    public List<Card> getCardsByOwner(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.byOwner();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        return retrieveCardList(conn, rs);
    }


    @Override
    public List<Card> getCardsByAccount(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.byAccount();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        return retrieveCardList(conn, rs);
    }

    @Override
    public int countCards() throws SQLException {
        Connection conn = dataSource.getConnection();
        String query = queryConstructor.countAll();
        ResultSet rs = queryExecutor.execute(conn, query);
        return retrieveCardsNumber(conn, rs);
    }

    @Override
    public int countCardsByOwner(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.countByOwner();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        return retrieveCardsNumber(conn, rs);
    }

    @Override
    public int countCardsByAccount(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.countByAccount();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        return retrieveCardsNumber(conn, rs);
    }

    @Override
    public boolean updateCardStatus(BigInteger id, StatusType statusType) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.updateStatus();
        int result = queryExecutor.executeUpdate(conn, rawQuery, statusType, id);
        conn.close();
        Optional<StatusType> updatedStatusType = getCardStatus(id);
        return (result != 0 && updatedStatusType.isPresent() && updatedStatusType.get().equals(statusType));
    }

    @Override
    public Optional<StatusType> getCardStatus(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.status();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, id);
        StatusType statusType = null;
        if (rs != null && rs.next()) {
            try {
                statusType = StatusType.valueOf(rs.getString(1).toUpperCase());
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            } finally {
                rs.getStatement().close();
            }
        }
        conn.close();
        return (statusType != null) ? Optional.of(statusType) : Optional.empty();
    }

    @Override
    public Optional<BigInteger> getCardAccountId(BigInteger cardId) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.accountId();
        ResultSet rs = queryExecutor.execute(conn, rawQuery, cardId);
        BigInteger accountId = null;
        if (rs != null && rs.next()) {
            try {
                accountId = new BigInteger(rs.getString(1));
            } catch (NumberFormatException e) {
                return Optional.empty();
            } finally {
                rs.getStatement().close();
            }
        }
        conn.close();
        return (accountId != null) ? Optional.of(accountId) : Optional.empty();
    }

    /**
     * Creates a new card. The method is intended for use as a part of a transaction with a card issue payment
     * performing.
     * Notice that the passed connection will not be closed after execution of the method
     * @param conn a valid java.sql.Connection (auto-commit mode of passed connection must be set to false)
     * @param cardDto a card DTO (NonNull fields required: ownerId, accountId, network; other fields might be null)
     * @return String with CVC (represents three-digit number) if a card was created, and an empty String if card
     * creation is failed
     * @throws SQLException
     */
    @Override
    public synchronized String addCard(Connection conn, Card cardDto) throws SQLException {
        if (cardDto == null) {
            return null;
        }
        String rawQuery = queryConstructor.add();
        String randomCvc = generateCvc();
        LocalDate now = LocalDate.now();
        int result = queryExecutor.executeUpdate(conn, rawQuery,
                cardDto.getOwnerId(),
                cardDto.getAccountId(),
                cardDto.getNetwork().getId(),
                passwordHash.generate(randomCvc.toCharArray()),
                (cardDto.getStatus() != null) ? cardDto.getStatus() : StatusType.ACTIVE,
                Date.valueOf(now.plusYears(EXP_PERIOD_YEARS))
        );
        return (result != 0) ? randomCvc : "";
    }

    @Override
    public synchronized boolean deleteCardById(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.delSingleById();
        queryExecutor.executeUpdate(conn, rawQuery, id);
        conn.close();
        return getSingleCardById(id).isEmpty();
    }

    @Override
    public synchronized boolean deleteCardByIdAndOwner(BigInteger cardId, BigInteger ownerId) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.delSingleByIdAndOwner();
        int result = queryExecutor.executeUpdate(conn, rawQuery, cardId, ownerId);
        conn.close();
        return (result != 0 && getSingleCardByIdAndOwner(cardId, ownerId).isEmpty());
    }

    @Override
    public synchronized boolean deleteCardsByAccount(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.delByAccount();
        int result = queryExecutor.executeUpdate(conn, rawQuery, id);
        conn.close();
        return (result != 0 && getCardsByAccount(id).isEmpty());
    }

    /**
     * Removes all the cards which has a specified account id. The method is intended for use as a part of a transaction
     * before removal of an account.
     * Notice that the passed connection will not be closed after execution of the method
     * @param conn a valid java.sql.Connection (auto-commit mode of passed connection must be set to false)
     * @param id an account id (a positive BigInteger)
     * @return true in case of success, otherwise false
     * @throws SQLException
     */
    @Override
    public synchronized boolean deleteCardsByAccount(Connection conn, BigInteger id) throws SQLException {
        String rawQuery = queryConstructor.delByAccount();
        int result = queryExecutor.executeUpdate(conn, rawQuery, id);
        return (result != 0);
    }

    @Override
    public synchronized boolean deleteCardsByOwner(BigInteger id) throws SQLException {
        Connection conn = dataSource.getConnection();
        String rawQuery = queryConstructor.delByOwner();
        int result = queryExecutor.executeUpdate(conn, rawQuery, id);
        conn.close();
        return (result != 0 && getCardsByOwner(id).isEmpty());
    }

    /**
     * Removes all the cards which has a specified owner id. The method is intended for use as a part of a transaction
     * before removal of person's accounts.
     * Notice that the passed connection will not be closed after execution of the method
     * @param conn a valid java.sql.Connection (auto-commit mode of passed connection must be set to false)
     * @param id an owner's id (a positive BigInteger)
     * @return true in case of success, otherwise false
     * @throws SQLException
     */
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
            logger.log(Level.WARN, String.format("Invalid field values were obtained from database (method: %s)",
                    Thread.currentThread().getStackTrace()[1].getMethodName()), e);
            throw new SQLException("Cannot create card instance (invalid field values were obtained from database)",
                    INVALID_PARAM_SQLSTATE, CustomSQLCode.INVALID_DB_PARAMETER.getCode(), e);
        }
    }

    private Optional<Card> retrieveCard(Connection conn, ResultSet rs) throws SQLException {
        Card card = null;
        if (rs != null && rs.next()) {
            card = buildInstance(rs);
            rs.getStatement().close();
        }
        conn.close();
        return (card != null) ? Optional.of(card) : Optional.empty();
    }

    private List<Card> retrieveCardList(Connection conn, ResultSet rs) throws SQLException {
        List<Card> cards = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                cards.add(buildInstance(rs));
            }
            rs.getStatement().close();
        }
        conn.close();
        return cards;
    }

    private int retrieveCardsNumber(Connection conn, ResultSet rs) throws SQLException {
        int amount = 0;
        if (rs != null && rs.next()) {
            amount = rs.getInt(1);
            rs.getStatement().close();
        }
        conn.close();
        return amount;
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
