package com.epam.upskillproject.model.dao;

import com.epam.upskillproject.model.dto.Card;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.CardSortType;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CardDao {
    Optional<Card> getSingleCardById(BigInteger id) throws SQLException;
    Optional<Card> getSingleCardByIdAndOwner(BigInteger cardId, BigInteger ownerId) throws SQLException;
    List<Card> getAllCards(CardSortType sortType) throws SQLException;
    List<Card> getCardsPage(int limit, int offset, CardSortType sortType) throws SQLException;
    List<Card> getCardsByOwner(BigInteger id) throws SQLException;
    List<Card> getCardsByAccount(BigInteger id) throws SQLException;
    int countCards() throws SQLException;
    int countCardsByOwner(BigInteger id) throws SQLException;
    int countCardsByAccount(BigInteger id) throws SQLException;
    boolean updateCardStatus(BigInteger id, StatusType statusType) throws SQLException;
    Optional<StatusType> getCardStatus(BigInteger id) throws SQLException;
    Optional<BigInteger> getCardAccountId(BigInteger cardId) throws SQLException;
    String addCard(Connection conn, Card cardDto) throws SQLException;
    boolean deleteCardById(BigInteger id) throws SQLException;
    boolean deleteCardByIdAndOwner(BigInteger cardId, BigInteger ownerId) throws SQLException;
    boolean deleteCardsByAccount(BigInteger id) throws SQLException;
    boolean deleteCardsByAccount(Connection conn, BigInteger id) throws SQLException;
    boolean deleteCardsByOwner(BigInteger id) throws SQLException;
    boolean deleteCardsByOwner(Connection conn, BigInteger id) throws SQLException;
}
