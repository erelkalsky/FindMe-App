package com.example.findme.classes.cases;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class MissingPerson implements Serializable {
    // Personal Information
    private String firstName;
    private String lastName;
    private String id;
    private String phoneNumber;
    private String address;
    private String age;
    private String height;
    private String eyeColor;
    private String hair;
    private String beard;

    // Clothing and Accessories
    private String upperClothing;
    private String lowerClothing;
    private String hat;
    private String shoes;
    private String glasses;
    private String bag;

    // Supplies
    private String water;
    private String food;

    // Weapons and Defense
    private String weapon;
    private String scars;

    // Identifying Features
    private String tattoos;
    private String disabilities;
    private String limp; //צליעה
    private String hearing;
    private String earringsPiercings;

    // Personal Belongings
    private String watch;
    private String jewelry;
    private String wallet;
    private String money;
    private String creditCard;
    private String RavKav;

    // Financial Information
    private String bankDetails;

    // Health Information
    private String diseases;
    private String mentalHealth;
    private String medicationTherapy;
    private String survivalSkills;
    private String pastMissingCases;

    // Hobbies and Preferences
    private String hobbies;

    // Substance Use
    private String drugsAlcohol;

    // Vehicle Information
    private String vehicle;

    // Language Proficiency
    private String languages;

    public static final Map<String, String[]> fields = new LinkedHashMap<String, String[]>() {
        {
            put("firstName", new String[]{"שם פרטי", "hebrew"});
            put("lastName", new String[]{"שם משפחה", "hebrew"});
            put("id", new String[]{"תעודת זהות", "number"});
            put("phoneNumber", new String[]{"מספר טלפון", "phone"});
            put("address", new String[]{"כתובת", "big"});
            put("age", new String[]{"גיל", "number"});
            put("height", new String[]{"גובה", "number"});
            put("eyeColor", new String[]{"צבע עיניים", "big"});
            put("hair", new String[]{"צבע שיער", "big"});
            put("beard", new String[]{"זקן", "big"});
            put("upperClothing", new String[]{"לבוש עליון", "big"});
            put("lowerClothing", new String[]{"לבוש תחתון", "big"});
            put("hat", new String[]{"כובע", "big"});
            put("shoes", new String[]{"נעליים", "big"});
            put("glasses", new String[]{"משקפיים", "big"});
            put("bag", new String[]{"תיק", "big"});
            put("water", new String[]{"מים", "big"});
            put("food", new String[]{"מזון", "big"});
            put("weapon", new String[]{"נשק", "big"});
            put("scars", new String[]{"צלקות", "big"});
            put("tattoos", new String[]{"קעקועים", "big"});
            put("disabilities", new String[]{"נכויות", "big"});
            put("limp", new String[]{"צליעה", "big"});
            put("hearing", new String[]{"שמיעה", "big"});
            put("earringsPiercings", new String[]{"עגילים ופירסינג", "big"});
            put("watch", new String[]{"שעון", "big"});
            put("jewelry", new String[]{"תכשיטים", "big"});
            put("wallet", new String[]{"ארנק", "big"});
            put("money", new String[]{"כסף", "big"});
            put("creditCard", new String[]{"כרטיס אשראי", "big"});
            put("RavKav", new String[]{"רב קו", "big"});
            put("bankDetails", new String[]{"פרטי בנק", "big"});
            put("diseases", new String[]{"מחלות", "big"});
            put("mentalHealth", new String[]{"בריאות נפשית", "big"});
            put("medicationTherapy", new String[]{"טיפול תרופתי", "big"});
            put("survivalSkills", new String[]{"יכולת שרידות", "big"});
            put("pastMissingCases", new String[]{"היעדרויות עבר", "big"});
            put("hobbies", new String[]{"תחביבים", "big"});
            put("drugsAlcohol", new String[]{"סמים ואלכוהול", "big"});
            put("vehicle", new String[]{"רכב (סוג וצבע)", "big"});
            put("languages", new String[]{"שפות", "big"});
        }
    };


    public MissingPerson() {

    }

    public MissingPerson(String firstName, String lastName, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public String getHair() {
        return hair;
    }

    public void setHair(String hair) {
        this.hair = hair;
    }

    public String getBeard() {
        return beard;
    }

    public void setBeard(String beard) {
        this.beard = beard;
    }

    public String getUpperClothing() {
        return upperClothing;
    }

    public void setUpperClothing(String upperClothing) {
        this.upperClothing = upperClothing;
    }

    public String getLowerClothing() {
        return lowerClothing;
    }

    public void setLowerClothing(String lowerClothing) {
        this.lowerClothing = lowerClothing;
    }

    public String getHat() {
        return hat;
    }

    public void setHat(String hat) {
        this.hat = hat;
    }

    public String getShoes() {
        return shoes;
    }

    public void setShoes(String shoes) {
        this.shoes = shoes;
    }

    public String getGlasses() {
        return glasses;
    }

    public void setGlasses(String glasses) {
        this.glasses = glasses;
    }

    public String getBag() {
        return bag;
    }

    public void setBag(String bag) {
        this.bag = bag;
    }

    public String getWater() {
        return water;
    }

    public void setWater(String water) {
        this.water = water;
    }

    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }

    public String getWeapon() {
        return weapon;
    }

    public void setWeapon(String weapon) {
        this.weapon = weapon;
    }

    public String getScars() {
        return scars;
    }

    public void setScars(String scars) {
        this.scars = scars;
    }

    public String getTattoos() {
        return tattoos;
    }

    public void setTattoos(String tattoos) {
        this.tattoos = tattoos;
    }

    public String getDisabilities() {
        return disabilities;
    }

    public void setDisabilities(String disabilities) {
        this.disabilities = disabilities;
    }

    public String getLimp() {
        return limp;
    }

    public void setLimp(String limp) {
        this.limp = limp;
    }

    public String getHearing() {
        return hearing;
    }

    public void setHearing(String hearing) {
        this.hearing = hearing;
    }

    public String getEarringsPiercings() {
        return earringsPiercings;
    }

    public void setEarringsPiercings(String earringsPiercings) {
        this.earringsPiercings = earringsPiercings;
    }

    public String getWatch() {
        return watch;
    }

    public void setWatch(String watch) {
        this.watch = watch;
    }

    public String getJewelry() {
        return jewelry;
    }

    public void setJewelry(String jewelry) {
        this.jewelry = jewelry;
    }

    public String getWallet() {
        return wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    public String getRavKav() {
        return RavKav;
    }

    public void setRavKav(String ravKav) {
        RavKav = ravKav;
    }

    public String getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(String bankDetails) {
        this.bankDetails = bankDetails;
    }

    public String getDiseases() {
        return diseases;
    }

    public void setDiseases(String diseases) {
        this.diseases = diseases;
    }

    public String getMentalHealth() {
        return mentalHealth;
    }

    public void setMentalHealth(String mentalHealth) {
        this.mentalHealth = mentalHealth;
    }

    public String getMedicationTherapy() {
        return medicationTherapy;
    }

    public void setMedicationTherapy(String medicationTherapy) {
        this.medicationTherapy = medicationTherapy;
    }

    public String getSurvivalSkills() {
        return survivalSkills;
    }

    public void setSurvivalSkills(String survivalSkills) {
        this.survivalSkills = survivalSkills;
    }

    public String getPastMissingCases() {
        return pastMissingCases;
    }

    public void setPastMissingCases(String pastMissingCases) {
        this.pastMissingCases = pastMissingCases;
    }

    public String getHobbies() {
        return hobbies;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public String getDrugsAlcohol() {
        return drugsAlcohol;
    }

    public void setDrugsAlcohol(String drugsAlcohol) {
        this.drugsAlcohol = drugsAlcohol;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }
}
