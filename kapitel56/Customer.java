/*
 *  Copyright (c) NOAMO Tech - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Noah Moritz Hölterhoff <noah.hoelterhoff@gmail.com>, 29.5.2020
 */

package kapitel56;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Repräsentiert einen Kunden einer Firma
 *
 * @author Noah Moritz Hölterhoff
 * @version 21.05.2020
 * @since 20.05.2020
 */
public class Customer {
    private String adresse;
    private ArrayList<Integer> auftragsnummern = new ArrayList<>();
    private String email;
    private String hausnummer;
    private String name = "Customer";
    private String plz;
    private float rabattInProzent = 0;

    /**
     * Fügt einen Auftrag dem Kundenkonto hinzu
     *
     * @param auftragsnummer Die Nummer der Bestellung des Kundens
     * @return Dieses Customer Objekt für die FluentAPI
     */
    public Customer addAuftrag(int auftragsnummer) {
        if (auftragsnummer > 0) auftragsnummern.add(auftragsnummer);
        return this;
    }

    /**
     * Setzt die Adresse der des Kundens (ohne Hausnummer)
     *
     * @param adresse Die Adresse des Kundens ohne Hausnummer (z.B. "Musterstr.")
     * @return Dieses Customer Objekt für die FluentAPI
     */
    public Customer adresse(String adresse) {
        this.adresse = adresse;
        return this;
    }

    /**
     * Löscht alle Daten des Kunden
     *
     * @return Dieses Customer Objekt für die FluentAPI
     */
    private Customer clear() {
        adresse = null;
        auftragsnummern = new ArrayList<>();
        email = null;
        hausnummer = null;
        name = "Customer";
        plz = null;
        rabattInProzent = 0;
        return this;
    }

    /**
     * Setzt die Email-Adresse des Kundens. Die Email-Adresse muss folgendes Format haben:<br>
     * {@code <text>@<text>}
     *
     * @param email Die Email-Adresse des Kundens
     * @return Dieses Customer Objekt für die FluentAPI
     */
    public Customer email(String email) {
        if (email.matches("^(.+)@(.+)$")) this.email = email;
        return this;
    }

    /**
     * Ändert die Hausnummer des Kunden
     *
     * @param hausnummer Hausnummer des Kunden (im Format gem. DIN 5008)
     * @return Dieses Customer Objekt für die FluentAPI
     */
    public Customer hausnummer(String hausnummer) {
        if (hausnummer.matches("^([\\d-\\s]*?)\\s*([\\w])?$")) this.hausnummer = hausnummer;
        return this;
    }

    public static void main(String[] args) {
        // Test
        Customer c = new Customer().name("Noah Hölterhoff").adresse("Panoramastr.").hausnummer("4").plz("69226").email("noah.hoelterhoff@gmail.com").rabatt(20).addAuftrag(30).addAuftrag(37).print();
        System.out.println("Preis für ein 57€ teures etwas: " + c.preisAusrechnen(57));
        c.clear().print();
    }

    /**
     * Setzt den Namen des Kunden (Der Standart Name des Kundens "Customer" wird dadruch
     * ersetzt
     *
     * @param name Der Name des Kundens
     * @return Dieses Customer Objekt für die FluentAPI
     */
    public Customer name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Ändert die PLZ des Kundens.
     *
     * @param plz Die PLZ muss aus 5 Ziffern bestehen
     * @return Dieses Customer Objekt für die FluentAPI
     */
    public Customer plz(String plz) {
        if (plz.matches("\\b\\d{5}\\b")) this.plz = plz;
        return this;
    }

    /**
     * Rechnet den individuellen Preis für diesen Kunden aus (unter Beachtung des individuellen Rabattes/Aufschlages)
     *
     * @param preis Der ursprüngliche Preis
     * @return Der Preis nach beachtung der Rabatte
     */
    public double preisAusrechnen(double preis) {
        double faktor = 1 - (rabattInProzent * 0.01);
        return preis * faktor;
    }

    /**
     * Gibt den Kunden in der Konsole aus
     *
     * @return Dieses Customer Objekt für die FluentAPI
     */
    private Customer print() {
        System.out.println("Name: " + name + System.lineSeparator() + // Name des Kunden
                "Email: " + email + System.lineSeparator() + // Email
                "Adresse: " + adresse + " " + hausnummer + System.lineSeparator() + // Adresse
                "PLZ: " + plz + System.lineSeparator() + // PLZ
                "Kundenrabbat: " + rabattInProzent + System.lineSeparator() + // Individueller Rabatt
                "Aufträge: " + Arrays.toString(auftragsnummern.toArray(new Integer[0]))); // Liste an Aufträgen
        return this;
    }

    /**
     * Setzt den Rabbet in Prozent für den Kunden fest. Der Rabatt kann auch negativ sein (Aufschlag). Der Rabatt
     * muss zwischen -500 und +100 Prozent liegen.
     *
     * @param rabattInProzent Der Rabbat in Prozent 35.3% z.B. wäre "35.3"
     * @return Dieses Customer Objekt für die FluentAPI
     */
    public Customer rabatt(float rabattInProzent) {
        if (rabattInProzent > -500 && rabattInProzent < 100) this.rabattInProzent = rabattInProzent;
        return this;
    }
}
