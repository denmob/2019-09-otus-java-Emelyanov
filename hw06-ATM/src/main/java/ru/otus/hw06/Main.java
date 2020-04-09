package ru.otus.hw06;

import ru.otus.hw06.atm.ATMImp;
import ru.otus.hw06.atm.MoneyValue;

public class Main {

    public static void main(String[] args)  {

        System.out.println("Инициализация АТМ");
        ATMImp atm = new ATMImp.Builder()
                .addMoneyValue20(15)
                .addMoneyValue50(5)
                .addMoneyValue100(2)
                .addMoneyValue200(5)
                .build();

        atm.printATMValues();

        System.out.println("Добавление банкнот АТМ");
        atm.depositMoney(MoneyValue.unit20,3);
        atm.depositMoney(MoneyValue.unit50,5);
        atm.depositMoney(MoneyValue.unit100,2);
        atm.depositMoney(MoneyValue.unit200,5);
        atm.depositMoney(MoneyValue.unit20,5);

        atm.printATMValues();

        System.out.println("Снятие суммы из АТМ");
        atm.withdrawMoney(540);

        atm.printATMValues();
    }
}
