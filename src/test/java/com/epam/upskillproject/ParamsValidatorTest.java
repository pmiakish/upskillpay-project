package com.epam.upskillproject;

import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.util.ParamsValidator;
import org.junit.Test;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class ParamsValidatorTest {

    private final ParamsValidator paramsValidator = new ParamsValidator();

    @Test
    public void testCorrectEmailValidation() {
        assertTrue(paramsValidator.validateEmail("correct_email@gmail.com"));
        assertTrue(paramsValidator.validateEmail("correct-email@mail.ru"));
        assertTrue(paramsValidator.validateEmail("CorrectEmail2021@fxmail.net"));
    }

    @Test
    public void testIncorrectEmailValidation() {
        assertFalse(paramsValidator.validateEmail("incorrect email@gmail.com"));
        assertFalse(paramsValidator.validateEmail("incorrect-emailmail.ru"));
        assertFalse(paramsValidator.validateEmail("InorrectEmail2021@fxmail"));
        assertFalse(paramsValidator.validateEmail("InorrectEmail2021@fxmail.z1"));
    }

    @Test
    public void testCorrectNameValidation() {
        assertTrue(paramsValidator.validateName("Ihar"));
        assertTrue(paramsValidator.validateName("Santa Maria"));
        assertTrue(paramsValidator.validateName("Maria-Emilia"));
        assertTrue(paramsValidator.validateName("Иван"));
    }

    @Test
    public void testIncorrectNameValidation() {
        assertFalse(paramsValidator.validateName("Petr 1"));
        assertFalse(paramsValidator.validateName("Petr&&#"));
        assertFalse(paramsValidator.validateName("TooooooooooooooooooooooooooooLongName"));
        assertFalse(paramsValidator.validateName(null));
    }

    @Test
    public void testCorrectPositiveIntValidation() {
        assertTrue(paramsValidator.validatePositiveInt(12));
        assertTrue(paramsValidator.validatePositiveInt(120001));
    }

    @Test
    public void testIncorrectPositiveIntValidation() {
        assertFalse(paramsValidator.validatePositiveInt(-1));
        assertFalse(paramsValidator.validatePositiveInt(0));
    }

    @Test
    public void testCorrectIdValidation() {
        assertTrue(paramsValidator.validateId(new BigInteger("15")));
        assertTrue(paramsValidator.validateId(new BigInteger("1000005")));
    }

    @Test
    public void testIncorrectIdValidation() {
        assertFalse(paramsValidator.validateId(new BigInteger("-15")));
        assertFalse(paramsValidator.validateId(new BigInteger("0")));
    }

    @Test
    public void testCorrectPaymentIdValidation() {
        assertTrue(paramsValidator.validatePaymentId(new BigInteger("15")));
        assertTrue(paramsValidator.validatePaymentId(BigInteger.ZERO));
    }

    @Test
    public void testIncorrectPaymentIdValidation() {
        assertFalse(paramsValidator.validatePaymentId(new BigInteger("-15")));
        assertFalse(paramsValidator.validatePaymentId(new BigInteger("-11111")));
    }

    @Test
    public void testCorrectPaymentAmountValidation() {
        assertTrue(paramsValidator.validatePaymentAmount(new BigDecimal("15.50")));
        assertTrue(paramsValidator.validatePaymentAmount(new BigDecimal("1000000")));
    }

    @Test
    public void testIncorrectPaymentAmountValidation() {
        assertFalse(paramsValidator.validatePaymentAmount(new BigDecimal("-15.50")));
        assertFalse(paramsValidator.validatePaymentAmount(BigDecimal.ZERO));
    }

    @Test
    public void testCorrectPageParamsValidation() {
        assertTrue(paramsValidator.validatePageParams(12, 3));
        assertTrue(paramsValidator.validatePageParams(5, 15));
    }

    @Test
    public void testIncorrectPageParamsValidation() {
        assertFalse(paramsValidator.validatePageParams(-1, 5));
        assertFalse(paramsValidator.validatePageParams(12, 0));
    }


    @Test
    public void testCorrectPersonAddParamsValidation() {
        assertTrue(paramsValidator.validatePersonAddParams("correct-email@mail.ru", "password", "Ihar", "Alekseev"));
        assertTrue(paramsValidator.validatePersonAddParams("correctemail@mail.com", "password", "Игорь", "Алексеев"));
    }

    @Test
    public void testIncorrectPersonAddParamsValidation() {
        assertFalse(paramsValidator.validatePersonAddParams("correct-email@mail.ru", null, "Ihar", "Alekseev"));
        assertFalse(paramsValidator.validatePersonAddParams("correctemail@mail.com", "password", "Игорь", "Алексеев 12"));
    }

    @Test
    public void testCorrectPersonUpdateParamsValidation() {
        assertTrue(paramsValidator.validatePersonUpdateParams(new BigInteger("12"), "correctemail@mail.com", "password",
                "Ihar", "Alekseev", StatusType.ACTIVE, LocalDate.now()));
        assertTrue(paramsValidator.validatePersonUpdateParams(new BigInteger("1"), "correct-email@mail.ru", null,
                "Игорь", "Алексеев", StatusType.BLOCKED, LocalDate.now()));
    }

    @Test
    public void testIncorrectPersonUpdateParamsValidation() {
        assertFalse(paramsValidator.validatePersonUpdateParams(new BigInteger("-12"), "correctemail@mail.com", "password",
                "Ihar", "Alekseev", StatusType.ACTIVE, LocalDate.now()));
        assertFalse(paramsValidator.validatePersonUpdateParams(new BigInteger("1"), "incorrect email@mail.ru", null,
                "Игорь", "Алексеев", StatusType.BLOCKED, LocalDate.now()));
    }

}
