package it.sogei.tabacchi.accertamenti.business.model.dto;

// riga di un gruppo a scaglioni di giorni di ritardo (aliquote di mora, sanzioni): serve a CommonValidator
// per controllare giorni e continuità su entrambi. I DTO lo implementano senza scrivere niente:
// i getter generati da Lombok (@Data) hanno già questi nomi
public interface ScaglioneGiorniRitardo {

    Integer getGiorniDa();

    // null = scaglione aperto (solo l'ultimo)
    Integer getGiorniA();
}
