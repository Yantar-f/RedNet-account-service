package com.rednet.accountservice.componenttest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednet.accountservice.controller.AccountController;
import com.rednet.accountservice.entity.Account;
import com.rednet.accountservice.exception.AccountNotFoundException;
import com.rednet.accountservice.exception.OccupiedValueException;
import com.rednet.accountservice.exception.handler.GlobalExceptionHandler;
import com.rednet.accountservice.model.AccountCreationData;
import com.rednet.accountservice.model.AccountUniqueFields;
import com.rednet.accountservice.model.AccountUniqueFieldsOccupancy;
import com.rednet.accountservice.service.AccountService;
import org.instancio.GetMethodSelector;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(useDefaultFilters = false, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(print = MockMvcPrint.SYSTEM_OUT, addFilters = false)
@Import({AccountController.class, GlobalExceptionHandler.class})
public class AccountControllerTest {
    @MockBean
    private AccountService accountService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void Getting_account_by_id_is_successful() throws Exception {
        Account expectedAccount = Instancio.create(Account.class);
        String expectedAccountID = String.valueOf(expectedAccount.getId());

        when(accountService.getAccountByID(eq(expectedAccount.getId())))
                .thenReturn(expectedAccount);

        MvcResult result = mvc.perform(get("/accounts/by-id")
                .param("id", expectedAccountID))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Account actualAccount = objectMapper.readValue(responseBody, Account.class);

        assertEquals(expectedAccount, actualAccount);
    }

    @Test
    public void Getting_account_by_not_integer_value_id_is_not_successful() throws Exception {
        String invalidAccountID = Instancio.create(String.class);

        mvc.perform(get("/accounts/by-id")
                        .param("id", invalidAccountID))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Getting_account_by_blank_id_is_not_successful() throws Exception {
        String invalidAccountID = "";

        mvc.perform(get("/accounts/by-id")
                        .param("id", invalidAccountID))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Getting_account_by_invalid_id_is_not_successful() throws Exception {
        String invalidAccountID = String.valueOf(Instancio.create(long.class));

        when(accountService.getAccountByID(eq(Long.parseLong(invalidAccountID))))
                .thenThrow(AccountNotFoundException.class);

        mvc.perform(get("/accounts/by-id")
                        .param("id", invalidAccountID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Getting_account_by_username_and_email_is_successful() throws Exception {
        String expectedUsername = Instancio.create(String.class);
        String expectedEmail = Instancio.create(String.class);
        Account expectedAccount = Instancio.create(Account.class);

        when(accountService.getAccountByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail)))
                .thenReturn(expectedAccount);

        MvcResult result = mvc.perform(get("/accounts/by-username-or-email")
                .param("username", expectedUsername)
                .param("email", expectedEmail))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Account actualAccount = objectMapper.readValue(responseBody, Account.class);

        assertEquals(expectedAccount, actualAccount);
    }

    @Test
    public void Getting_account_by_blank_username_and_valid_email_is_not_successful() throws Exception {
        String username = Instancio.create(String.class);

        mvc.perform(get("/accounts/by-username-or-email")
                        .param("username", username)
                        .param("email", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Getting_account_by_blank_email_and_valid_username_is_not_successful() throws Exception {
        String email = Instancio.create(String.class);

        mvc.perform(get("/accounts/by-username-or-email")
                        .param("username", "")
                        .param("email", email))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Getting_account_by_invalid_username_and_email_is_not_successful() throws Exception {
        String expectedUsername = Instancio.create(String.class);
        String expectedEmail = Instancio.create(String.class);

        when(accountService.getAccountByUsernameOrEmail(eq(expectedUsername), eq(expectedEmail)))
                .thenThrow(AccountNotFoundException.class);

        mvc.perform(get("/accounts/by-username-or-email")
                .param("username", expectedUsername)
                .param("email", expectedEmail))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Getting_account_by_username_is_successful() throws Exception {
        String expectedUsername = Instancio.create(String.class);
        Account expectedAccount = Instancio.create(Account.class);

        when(accountService.getAccountByUsername(eq(expectedUsername)))
                .thenReturn(expectedAccount);

        MvcResult result = mvc.perform(get("/accounts/by-username")
                        .param("username", expectedUsername))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Account actualAccount = objectMapper.readValue(responseBody, Account.class);

        assertEquals(expectedAccount, actualAccount);
    }

    @Test
    public void Getting_account_by_blank_username_is_not_successful() throws Exception {
        mvc.perform(get("/accounts/by-username")
                        .param("username", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Getting_account_by_invalid_username_is_not_successful() throws Exception {
        String invalidUsername = Instancio.create(String.class);

        when(accountService.getAccountByUsername(eq(invalidUsername)))
                .thenThrow(AccountNotFoundException.class);

        mvc.perform(get("/accounts/by-username")
                        .param("username", invalidUsername))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Getting_account_by_email_is_successful() throws Exception {
        String expectedEmail = Instancio.create(String.class);
        Account expectedAccount = Instancio.create(Account.class);

        when(accountService.getAccountByEmail(eq(expectedEmail)))
                .thenReturn(expectedAccount);

        MvcResult result = mvc.perform(get("/accounts/by-email")
                        .param("email", expectedEmail))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Account actualAccount = objectMapper.readValue(responseBody, Account.class);

        assertEquals(expectedAccount, actualAccount);
    }

    @Test
    public void Getting_account_by_blank_email_is_not_successful() throws Exception {
        mvc.perform(get("/accounts/by-email")
                        .param("email", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Getting_account_by_invalid_email_is_not_successful() throws Exception {
        String invalidEmail = Instancio.create(String.class);

        when(accountService.getAccountByEmail(eq(invalidEmail)))
                .thenThrow(AccountNotFoundException.class);

        mvc.perform(get("/accounts/by-email")
                        .param("email", invalidEmail))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Existing_account_by_username_is_successful() throws Exception {
        String expectedUsername = Instancio.create(String.class);

        when(accountService.existsAccountByUsername(eq(expectedUsername)))
                .thenReturn(true);

        mvc.perform(head("/accounts/by-username")
                        .param("username", expectedUsername))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @Test
    public void Existing_account_by_blank_username_is_not_successful() throws Exception {
        mvc.perform(head("/accounts/by-username")
                        .param("username", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andReturn();
    }

    @Test
    public void Existing_account_by_invalid_username_is_not_successful() throws Exception {
        String invalidusername = Instancio.create(String.class);

        when(accountService.existsAccountByUsername(eq(invalidusername)))
                .thenReturn(false);

        mvc.perform(head("/accounts/by-username")
                        .param("username", invalidusername))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Existing_account_by_email_is_successful() throws Exception {
        String expectedEmail = Instancio.create(String.class);

        when(accountService.existsAccountByEmail(eq(expectedEmail)))
                .thenReturn(true);

        mvc.perform(head("/accounts/by-email")
                        .param("email", expectedEmail))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @Test
    public void Existing_account_by_blank_email_is_not_successful() throws Exception {
        mvc.perform(head("/accounts/by-email")
                        .param("email", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andReturn();
    }

    @Test
    public void Existing_account_by_invalid_email_is_not_successful() throws Exception {
        String invalidEmail = Instancio.create(String.class);

        when(accountService.existsAccountByEmail(eq(invalidEmail)))
                .thenReturn(false);

        mvc.perform(head("/accounts/by-email")
                        .param("email", invalidEmail))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Getting_unique_fields_occupancy_is_successful() throws Exception {
        AccountUniqueFields fields = Instancio.create(AccountUniqueFields.class);
        AccountUniqueFieldsOccupancy expecetdOccupancy = Instancio.create(AccountUniqueFieldsOccupancy.class);

        when(accountService.getAccountUniqueFieldsOccupancy(eq(fields)))
                .thenReturn(expecetdOccupancy);

        MvcResult result = mvc.perform(get("/accounts/unique-fields-occupancy")
                        .param("username", fields.username())
                        .param("email", fields.email()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AccountUniqueFieldsOccupancy actualOccupancy = objectMapper.readValue(responseBody, AccountUniqueFieldsOccupancy.class);

        assertEquals(expecetdOccupancy, actualOccupancy);
    }

    @Test
    public void Getting_unique_fields_occuapancy_by_blank_username_is_not_successful() throws Exception {
        String email = Instancio.create(String.class);

        mvc.perform(get("/accounts/unique-fields-occupancy")
                        .param("username", "")
                        .param("email", email))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Getting_unique_fields_occuapancy_by_blank_email_is_not_successful() throws Exception {
        String username = Instancio.create(String.class);

        mvc.perform(get("/accounts/unique-fields-occupancy")
                        .param("email", "")
                        .param("username", username))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Creating_account_is_successful() throws Exception {
        AccountCreationData expectedCreationData = Instancio.create(AccountCreationData.class);
        Account expectedAccount = Instancio.create(Account.class);

        when(accountService.createAccount(eq(expectedCreationData)))
                .thenReturn(expectedAccount);

        MvcResult result = mvc.perform(post("/accounts")
                        .content(objectMapper.writeValueAsString(expectedCreationData))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Account actualAccount = objectMapper.readValue(responseBody, Account.class);

        assertEquals(expectedAccount, actualAccount);
    }

    @Test
    public void Creating_account_with_invalid_content_type_is_not_successful() throws Exception {
        AccountCreationData creationData = Instancio.create(AccountCreationData.class);

        mvc.perform(post("/accounts")
                        .content(objectMapper.writeValueAsString(creationData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));

        verify(accountService, never())
                .createAccount(any());
    }

    @Test
    public void Creating_account_with_empty_body_is_not_successful() throws Exception {
        mvc.perform(post("/accounts")
                        .content("")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));

        verify(accountService, never())
                .createAccount(any());
    }

    @ParameterizedTest
    @MethodSource("accountCreationDataStringFields")
    public void Creating_account_with_blank_string_field_is_not_successful(
            GetMethodSelector<AccountCreationData, String> creationDataField) throws Exception {

        AccountCreationData creationData = Instancio.of(AccountCreationData.class)
                .set(field(creationDataField), "")
                .create();

        mvc.perform(post("/accounts")
                        .content(objectMapper.writeValueAsString(creationData))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));

        verify(accountService, never())
                .createAccount(any());
    }

    @ParameterizedTest
    @MethodSource("accountCreationDataStringFields")
    public void Creating_account_with_nullable_string_field_is_not_successful(
            GetMethodSelector<AccountCreationData, String> creationDataField) throws Exception {

        AccountCreationData creationData = Instancio.of(AccountCreationData.class)
                .ignore(field(creationDataField))
                .create();

        mvc.perform(post("/accounts")
                        .content(objectMapper.writeValueAsString(creationData))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));

        verify(accountService, never())
                .createAccount(any());
    }

    private static List<GetMethodSelector<AccountCreationData, String>> accountCreationDataStringFields() {
        return List.of(
                AccountCreationData::username,
                AccountCreationData::email,
                AccountCreationData::password,
                AccountCreationData::secretWord
        );
    }

    @Test
    public void Creating_account_with_blank_roles_is_not_successful() throws Exception {
        AccountCreationData creationData = Instancio.of(AccountCreationData.class)
                .set(field(AccountCreationData::roles), new String[]{})
                .create();

        mvc.perform(post("/accounts")
                        .content(objectMapper.writeValueAsString(creationData))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));

        verify(accountService, never())
                .createAccount(any());
    }

    @Test
    public void Creating_account_with_nullable_roles_is_not_successful() throws Exception {
        AccountCreationData creationData = Instancio.of(AccountCreationData.class)
                .ignore(field(AccountCreationData::roles))
                .create();

        mvc.perform(post("/accounts")
                        .content(objectMapper.writeValueAsString(creationData))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));

        verify(accountService, never())
                .createAccount(any());
    }

    @Test
    public void Creating_account_with_unique_fields_constraints_violation_is_not_successful() throws Exception {
        AccountCreationData creationData = Instancio.create(AccountCreationData.class);

        when(accountService.createAccount(eq(creationData)))
                .thenThrow(OccupiedValueException.class);

        mvc.perform(post("/accounts")
                        .content(objectMapper.writeValueAsString(creationData))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Updating_account_is_successful() throws Exception {
        Account expectedAccountToUpdate = Instancio.create(Account.class);

        mvc.perform(put("/accounts")
                        .content(objectMapper.writeValueAsString(expectedAccountToUpdate))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        verify(accountService)
                .updateAccount(eq(expectedAccountToUpdate));
    }

    @Test
    public void Updating_account_with_invalid_content_type_is_not_successful() throws Exception{
        Account expectedAccountToUpdate = Instancio.create(Account.class);

        mvc.perform(put("/accounts")
                        .content(objectMapper.writeValueAsString(expectedAccountToUpdate)))
                .andExpect(status().isBadRequest());

        verify(accountService, never())
                .updateAccount(any());
    }

    @ParameterizedTest
    @MethodSource("accountStringFields")
    public void Updating_account_with_blank_string_field_is_not_successful(
            GetMethodSelector<Account, String> field) throws Exception {

        Account expectedAccountToUpdate = Instancio.of(Account.class)
                .set(field(field), "")
                .create();

        mvc.perform(put("/accounts")
                        .content(objectMapper.writeValueAsString(expectedAccountToUpdate))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));

        verify(accountService, never())
                .updateAccount(any());
    }

    private static List<GetMethodSelector<Account, String>> accountStringFields() {
        return List.of(
                Account::getUsername,
                Account::getEmail,
                Account::getPassword,
                Account::getSecretWord
        );
    }

    @Test
    public void Updating_account_with_blank_roles_is_not_successful() throws Exception {
        Account expectedAccountToUpdate = Instancio.of(Account.class)
                .set(field(Account::getRoles), new String[]{})
                .create();

        mvc.perform(put("/accounts")
                        .content(objectMapper.writeValueAsString(expectedAccountToUpdate))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));

        verify(accountService, never())
                .updateAccount(any());
    }

    @ParameterizedTest
    @MethodSource("accountFields")
    public void Updating_account_with_nullable_field_is_not_successful(
            GetMethodSelector<Account, Object> field) throws Exception {

        Account expectedAccountToUpdate = Instancio.of(Account.class)
                .ignore(field(field))
                .create();

        mvc.perform(put("/accounts")
                        .content(objectMapper.writeValueAsString(expectedAccountToUpdate))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));

        verify(accountService, never())
                .updateAccount(any());
    }

    private static List<GetMethodSelector<Account, Object>> accountFields() {
        return List.of(
                Account::getId,
                Account::getUsername,
                Account::getEmail,
                Account::getPassword,
                Account::getSecretWord,
                Account::getRoles
        );
    }

    @Test
    public void Updating_account_with_unique_field_constraint_violation_is_not_successful() throws Exception {
        Account expectedAccountToUpdate = Instancio.create(Account.class);

        doThrow(OccupiedValueException.class)
                .when(accountService).updateAccount(eq(expectedAccountToUpdate));

        mvc.perform(put("/accounts")
                        .content(objectMapper.writeValueAsString(expectedAccountToUpdate))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Updating_not_existing_acount_is_not_successful() throws Exception {
        Account expectedAccountToUpdate = Instancio.create(Account.class);

        doThrow(AccountNotFoundException.class)
                .when(accountService).updateAccount(eq(expectedAccountToUpdate));

        mvc.perform(put("/accounts")
                        .content(objectMapper.writeValueAsString(expectedAccountToUpdate))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void Deleting_account_by_id_is_successful() throws Exception {
        long expectedUserID = Instancio.create(long.class);

        mvc.perform(delete("/accounts/by-id")
                        .param("id", String.valueOf(expectedUserID)))
                .andExpect(status().is2xxSuccessful());

        verify(accountService)
                .deleteAccountByID(eq(expectedUserID));
    }

    @Test
    public void Deleting_account_by_not_integer_id_is_not_successful() throws Exception {
        String invalidAccountID = Instancio.create(String.class);

        mvc.perform(delete("/accounts/by-id")
                        .param("id", invalidAccountID))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));

        verify(accountService, never())
                .deleteAccountByID(anyLong());
    }

    @Test
    public void Deleting_account_by_blank_id_is_not_successful() throws Exception {
        mvc.perform(delete("/accounts/by-id")
                        .param("id", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));

        verify(accountService, never())
                .deleteAccountByID(anyLong());
    }

    @Test
    public void Deleting_account_by_not_existing_id_is_not_successful() throws Exception {
        long notExistingAccountID = Instancio.create(long.class);

        doThrow(AccountNotFoundException.class)
                .when(accountService).deleteAccountByID(eq(notExistingAccountID));

        mvc.perform(delete("/accounts/by-id")
                        .param("id", String.valueOf(notExistingAccountID)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON));
    }
}
