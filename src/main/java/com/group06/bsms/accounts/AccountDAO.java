package com.group06.bsms.accounts;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import javax.swing.SortOrder;

public interface AccountDAO {

    List<Account> selectTop10EmployeesWithHighestRevenue(Map<Integer, SortOrder> sortAttributeAndOrder,
            Date startDate, Date endDate) throws Exception;
    List<Account> selectAllAccounts() throws Exception;

    public Account selectAccount(int id) throws Exception;

    public Account selectAccountByName(String accountName) throws Exception;

    void unlockAccount(int id) throws Exception;

    void lockAccount(int id) throws Exception;

    void updateAccount(Account account, AccountWithPassword updatedAccount) throws Exception;

    void insertAccount(AccountWithPassword account) throws Exception;

    void updateAccountAttributeById(int accountId, String attr, Object value) throws Exception;
    
    boolean checkPasswordById(int id, String password) throws Exception;
    
    void updatePasswordById(int id, String password) throws Exception;

    public List<Account> selectSearchSortFilterAccounts(
            int offset, int limit, Map<Integer, SortOrder> sortValue,
            String searchString, String searchChoice
    ) throws Exception;
}
